package com.example.athleticare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddInjuryFormActivity extends AppCompatActivity {

    EditText editTextSearchId, editTextInjuryType, editTextInjuryNotes, editTextRecommendation;
    TextView textViewName, textViewSport, textViewDate, textBodyPart;
    Button btnSearch, btnSave;
    Spinner spinnerSeverity;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    String injuryArea = "";
    String fetchedName = "";
    String fetchedSport = "";
    String fetchedCoachId = "";
    String selectedDate = "";

    boolean studentLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addinjuryform);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        injuryArea = getIntent().getStringExtra("BODY_PART");

        bindViews();
        setupSeveritySpinner();
        setupListeners();

        textBodyPart.setText("Body Part: " + injuryArea);
    }

    private void bindViews() {

        editTextSearchId = findViewById(R.id.editTextSearchId);
        editTextInjuryType = findViewById(R.id.editTextInjuryType);
        editTextInjuryNotes = findViewById(R.id.editTextInjuryNotes);
        editTextRecommendation = findViewById(R.id.editTextRecommendation);

        textViewName = findViewById(R.id.textViewName);
        textViewSport = findViewById(R.id.textViewSport);
        textViewDate = findViewById(R.id.textViewDate);
        textBodyPart = findViewById(R.id.textBodyPart);

        btnSearch = findViewById(R.id.btnSearch);
        btnSave = findViewById(R.id.btnSaveInjury);

        spinnerSeverity = findViewById(R.id.spinnerSeverity);
    }

    private void setupSeveritySpinner() {

        String[] levels = {"Mild", "Moderate", "Severe"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                levels
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeverity.setAdapter(adapter);
    }

    private void setupListeners() {
        textViewDate.setOnClickListener(v -> showDatePicker());
        btnSearch.setOnClickListener(v -> searchStudent());
        btnSave.setOnClickListener(v -> saveInjury());
    }

    private void searchStudent() {

        String schoolId = editTextSearchId.getText().toString().trim();

        if (TextUtils.isEmpty(schoolId)) {
            Toast.makeText(this, "Enter Student ID", Toast.LENGTH_SHORT).show();
            return;
        }

        studentLoaded = false;
        fetchedName = "";
        fetchedSport = "";
        fetchedCoachId = "";

        db.collection("Teams")
                .whereEqualTo("schoolId", schoolId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (!querySnapshot.isEmpty()) {

                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

                        fetchedName = safe(doc.getString("name"));
                        fetchedSport = safe(doc.getString("sport"));
                        fetchedCoachId = safe(doc.getString("coachId"));

                        textViewName.setText("Name: " + fetchedName);
                        textViewSport.setText("Sport: " +
                                (TextUtils.isEmpty(fetchedSport) ? "Unknown" : fetchedSport));

                        studentLoaded = true;

                    } else {
                        Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show();
                        resetStudentUI();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void resetStudentUI() {
        textViewName.setText("Name: -");
        textViewSport.setText("Sport: -");
    }

    
    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);

                    SimpleDateFormat sdf =
                            new SimpleDateFormat("yyyy-MM-dd");

                    selectedDate = sdf.format(selected.getTime());
                    textViewDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        dialog.show();
    }

    private void saveInjury() {

        String schoolId = editTextSearchId.getText().toString().trim();
        String injuryType = editTextInjuryType.getText().toString().trim();
        String notes = editTextInjuryNotes.getText().toString().trim();
        String recommendation = editTextRecommendation.getText().toString().trim();
        String severity = spinnerSeverity.getSelectedItem().toString();

        if (!studentLoaded) {
            Toast.makeText(this, "Search and select a student first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(injuryType)) {
            Toast.makeText(this, "Enter injury type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(selectedDate)) {
            Toast.makeText(this, "Select injury date", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> injuryMap = new HashMap<>();
        injuryMap.put("schoolId", schoolId);
        injuryMap.put("name", fetchedName);
        injuryMap.put("sport",
                TextUtils.isEmpty(fetchedSport) ? "Unknown" : fetchedSport);
        injuryMap.put("injuryArea", injuryArea);
        injuryMap.put("injuryType", injuryType);
        injuryMap.put("severity", severity);
        injuryMap.put("notes", notes);
        injuryMap.put("recommendation", recommendation);
        injuryMap.put("injuryDate", selectedDate);
        injuryMap.put("timestamp", FieldValue.serverTimestamp());
        injuryMap.put("loggedBy", mAuth.getCurrentUser().getUid());
        injuryMap.put("coachId", fetchedCoachId);

        db.collection("Injuries")
                .add(injuryMap)
                .addOnSuccessListener(doc ->
                        saveRecommendation(schoolId, recommendation))
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveRecommendation(String schoolId, String recommendation) {

        if (TextUtils.isEmpty(recommendation)) {
            Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Map<String, Object> rec = new HashMap<>();
        rec.put("schoolId", schoolId);
        rec.put("recommendation", recommendation);
        rec.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Recommendations")
                .add(rec)
                .addOnSuccessListener(r -> {
                    Toast.makeText(this, "Injury saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Saved injury but failed recommendation",
                                Toast.LENGTH_SHORT).show()
                );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
