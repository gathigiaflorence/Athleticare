package com.example.athleticare;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FollowUpActivity extends AppCompatActivity {

    private EditText editTextSearchId, editTextTreatment, editTextRecommendation;
    private TextView textViewName, textViewSport, textViewInjuryType, textViewDuration;
    private Spinner spinnerProgress;
    private Button btnSearch, btnUpdate;

    private FirebaseFirestore db;

    private String currentSchoolId = "";
    private String fetchedName = "";
    private String fetchedSport = "";

    private String fetchedCoachId = "";   // ✅ IMPORTANT
    private String currentInjuryId = null;
    private String currentInjuryType = "";
    private String currentInjuryArea = "";
    private Date currentInjuryDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followup);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinner();

        btnSearch.setOnClickListener(v -> searchAthlete());
        btnUpdate.setOnClickListener(v -> saveFollowUp());
    }

    private void initViews() {

        editTextSearchId = findViewById(R.id.editTextSearchId);
        editTextTreatment = findViewById(R.id.editTextTreatment);
        editTextRecommendation = findViewById(R.id.editTextRecommendation);

        textViewName = findViewById(R.id.textViewName);
        textViewSport = findViewById(R.id.textViewSport);
        textViewInjuryType = findViewById(R.id.textViewInjuryType);
        textViewDuration = findViewById(R.id.textViewDuration);

        spinnerProgress = findViewById(R.id.spinnerProgress);

        btnSearch = findViewById(R.id.btnSearch);
        btnUpdate = findViewById(R.id.btnUpdate);
    }

    private void setupSpinner() {

        String[] progressOptions = {
                "Select Progress",
                "Excellent",
                "Improving",
                "Moderate",
                "Slow Recovery",
                "No Change"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                progressOptions
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProgress.setAdapter(adapter);
    }

    private void searchAthlete() {

        String schoolId = editTextSearchId.getText().toString().trim();

        if (TextUtils.isEmpty(schoolId)) {
            showToast("Enter Athlete ID");
            return;
        }

        db.collection("Teams")
                .whereEqualTo("schoolId", schoolId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        showToast("Athlete not found");
                        return;
                    }

                    DocumentSnapshot doc = snapshot.getDocuments().get(0);

                    fetchedName = safe(doc.getString("name"));
                    fetchedSport = safe(doc.getString("sport"));
                    fetchedCoachId = safe(doc.getString("coachId")); // ✅ IMPORTANT
                    currentSchoolId = schoolId;

                    textViewName.setText("Name: " + fetchedName);
                    textViewSport.setText("Sport: " + fetchedSport);

                    fetchLatestInjury();
                });
    }

    private void fetchLatestInjury() {

        db.collection("Injuries")
                .whereEqualTo("schoolId", currentSchoolId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        textViewInjuryType.setText("Injury Type: None");
                        textViewDuration.setText("Recovery Duration: N/A");
                        return;
                    }

                    DocumentSnapshot doc = snapshot.getDocuments().get(0);

                    currentInjuryId = doc.getId();
                    currentInjuryType = safe(doc.getString("injuryType"));
                    currentInjuryArea = safe(doc.getString("injuryArea"));

                    Timestamp ts = doc.getTimestamp("timestamp");
                    if (ts != null) {
                        currentInjuryDate = ts.toDate();
                    }

                    textViewInjuryType.setText("Injury Type: " + currentInjuryType);

                    if (currentInjuryDate != null) {
                        long diff = System.currentTimeMillis() - currentInjuryDate.getTime();
                        long days = diff / (1000 * 60 * 60 * 24);
                        textViewDuration.setText("Recovery Duration: " + days + " days");
                    } else {
                        textViewDuration.setText("Recovery Duration: N/A");
                    }
                });
    }

    private void saveFollowUp() {

        if (TextUtils.isEmpty(currentSchoolId) || currentInjuryId == null) {
            showToast("Search athlete first");
            return;
        }

        String treatment = editTextTreatment.getText().toString().trim();
        String recommendation = editTextRecommendation.getText().toString().trim();
        String progress = spinnerProgress.getSelectedItem().toString();

        if (progress.equals("Select Progress")) {
            showToast("Select progress status");
            return;
        }

        // ✅ THIS IS THE IMPORTANT PART
        Map<String, Object> rec = new HashMap<>();

        rec.put("coachId", fetchedCoachId); // 🔴 FIX THAT CAUSED YOUR ERROR
        rec.put("schoolId", currentSchoolId);
        rec.put("name", fetchedName);
        rec.put("sport", fetchedSport);
        rec.put("injuryType", currentInjuryType);
        rec.put("injuryArea", currentInjuryArea);
        rec.put("recommendation", recommendation);
        rec.put("timestamp", FieldValue.serverTimestamp());

        db.collection("recommendations").add(rec);

        showToast("Follow-up saved");
        clearInputs();
    }

    private void clearInputs() {
        editTextTreatment.setText("");
        editTextRecommendation.setText("");
        spinnerProgress.setSelection(0);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}