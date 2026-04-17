package com.example.athleticare;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

public class AppointmentsActivity extends AppCompatActivity {

    private EditText editTextIDNumber, editTextDate, editTextTime;
    private Button buttonFetchAthlete, buttonBookAppointment, buttonClearFields;
    private Spinner spinnerAppointmentType, spinnerDayFilter;
    private TextView textViewName, textViewInjuryType, textViewNoAppointments;
    private ProgressBar progressBarLoading;
    private RecyclerView recyclerViewAppointments;

    private FirebaseFirestore db;

    private AppointmentAdapter appointmentAdapter;
    private List<AppointmentModel> appointmentList = new ArrayList<>();
    private List<AppointmentModel> filteredList = new ArrayList<>();

    private String currentAthleteName;
    private String currentInjuryType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecycler();
        setupSpinners();
        setupListeners();

        buttonBookAppointment.setEnabled(false);

        loadAllAppointments();
    }

    // ================= INIT =================
    private void initViews() {

        editTextIDNumber = findViewById(R.id.editTextIDNumber);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);

        buttonFetchAthlete = findViewById(R.id.buttonFetchAthlete);
        buttonBookAppointment = findViewById(R.id.buttonBookAppointment);
        buttonClearFields = findViewById(R.id.buttonClearFields);

        spinnerAppointmentType = findViewById(R.id.spinnerAppointmentType);
        spinnerDayFilter = findViewById(R.id.spinnerDayFilter);

        textViewName = findViewById(R.id.textViewName);
        textViewInjuryType = findViewById(R.id.textViewInjuryType);
        textViewNoAppointments = findViewById(R.id.textViewNoAppointments);

        progressBarLoading = findViewById(R.id.progressBarLoading);
        recyclerViewAppointments = findViewById(R.id.recyclerViewAppointments);
    }

    // ================= RECYCLER =================
    private void setupRecycler() {

        appointmentAdapter = new AppointmentAdapter();

        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAppointments.setAdapter(appointmentAdapter);

        // 🔥 STATUS UPDATE LISTENER
        appointmentAdapter.setOnStatusChangeListener((appointment, newStatus) -> {

            db.collection("Appointments")
                    .document(appointment.getDocId())
                    .update("status", newStatus)
                    .addOnSuccessListener(unused -> loadAllAppointments());
        });

        // 🗑 DELETE LISTENER
        appointmentAdapter.setOnDeleteListener(appointment -> {

            db.collection("Appointments")
                    .document(appointment.getDocId())
                    .delete()
                    .addOnSuccessListener(unused -> loadAllAppointments());
        });
    }

    // ================= LISTENERS =================
    private void setupListeners() {

        buttonFetchAthlete.setOnClickListener(v -> fetchAthlete());
        buttonBookAppointment.setOnClickListener(v -> bookAppointment());
        buttonClearFields.setOnClickListener(v -> clearFields());

        editTextDate.setOnClickListener(v -> showDatePicker());
        editTextTime.setOnClickListener(v -> showTimePicker());
    }

    // ================= FETCH ATHLETE =================
    private void fetchAthlete() {

        String idNumber = editTextIDNumber.getText().toString().trim();

        if (TextUtils.isEmpty(idNumber)) {
            editTextIDNumber.setError("Enter Athlete ID");
            return;
        }

        setLoading(true);

        db.collection("Injuries")
                .whereEqualTo("schoolId", idNumber)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    setLoading(false);

                    if (snapshot.isEmpty()) {
                        clearAthleteDisplay();
                        Toast.makeText(this, "No athlete found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot doc = snapshot.getDocuments().get(0);

                    currentAthleteName = doc.getString("name");
                    currentInjuryType = doc.getString("injuryType");

                    textViewName.setText("Name: " + safe(currentAthleteName));
                    textViewInjuryType.setText("Injury: " + safe(currentInjuryType));

                    buttonBookAppointment.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ================= BOOK =================
    private void bookAppointment() {

        String idNumber = editTextIDNumber.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();
        String type = spinnerAppointmentType.getSelectedItem().toString();

        if (TextUtils.isEmpty(idNumber) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentAthleteName == null) {
            Toast.makeText(this, "Fetch athlete first", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        db.collection("Appointments")
                .whereEqualTo("schoolId", idNumber)
                .whereEqualTo("date", date)
                .whereEqualTo("time", time)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.isEmpty()) {
                        setLoading(false);
                        Toast.makeText(this, "Slot already booked", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    saveAppointment(idNumber, date, time, type);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveAppointment(String idNumber, String date, String time, String type) {

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("schoolId", idNumber);
        appointment.put("date", date);
        appointment.put("time", time);
        appointment.put("appointmentType", type);
        appointment.put("injuryType", currentInjuryType);
        appointment.put("athleteName", currentAthleteName);
        appointment.put("status", "PENDING");
        appointment.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("Appointments")
                .add(appointment)
                .addOnSuccessListener(ref -> {
                    setLoading(false);
                    Toast.makeText(this, "Booked", Toast.LENGTH_SHORT).show();
                    clearFields();
                    loadAllAppointments();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ================= STATUS UPDATE (FIXED) =================
    private void updateAppointmentStatus(String appointmentId, String newStatus) {

        db.collection("Appointments")
                .document(appointmentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    loadAllAppointments();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ================= LOAD =================
    private void loadAllAppointments() {

        db.collection("Appointments")
                .get()
                .addOnSuccessListener(snapshot -> {

                    appointmentList.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        AppointmentModel model = doc.toObject(AppointmentModel.class);
                        model.setDocId(doc.getId()); // 🔥 IMPORTANT
                        appointmentList.add(model);
                    }

                    applyFilter();
                });
    }

    // ================= FILTER =================
    private void applyFilter() {

        if (spinnerDayFilter.getSelectedItem() == null) return;

        String selectedDay = spinnerDayFilter.getSelectedItem().toString();

        filteredList.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        for (AppointmentModel a : appointmentList) {

            try {
                Date d = sdf.parse(a.getDate());
                if (d == null) continue;

                String day = new SimpleDateFormat("EEEE", Locale.getDefault()).format(d);

                if (day.equalsIgnoreCase(selectedDay)) {
                    filteredList.add(a);
                }

            } catch (Exception ignored) {}
        }

        appointmentAdapter.updateList(filteredList);

        textViewNoAppointments.setVisibility(
                filteredList.isEmpty() ? View.VISIBLE : View.GONE
        );
    }

    // ================= HELPERS =================
    private void clearFields() {

        editTextIDNumber.setText("");
        editTextDate.setText("");
        editTextTime.setText("");

        clearAthleteDisplay();

        currentAthleteName = null;
        currentInjuryType = null;

        buttonBookAppointment.setEnabled(false);
    }

    private void clearAthleteDisplay() {
        textViewName.setText("Name:");
        textViewInjuryType.setText("Injury:");
    }

    private String safe(String v) {
        return v != null ? v : "-";
    }

    private void setLoading(boolean state) {
        progressBarLoading.setVisibility(state ? View.VISIBLE : View.GONE);
        buttonFetchAthlete.setEnabled(!state);
    }

    // ================= DATE / TIME =================
    private void showDatePicker() {

        Calendar c = Calendar.getInstance();

        new DatePickerDialog(this,
                (view, y, m, d) ->
                        editTextDate.setText((m + 1) + "/" + d + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {

        Calendar c = Calendar.getInstance();

        new TimePickerDialog(this,
                (view, h, m) ->
                        editTextTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    // ================= SPINNER =================
    private void setupSpinners() {

        spinnerAppointmentType.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Physio", "Sports Massage"}
        ));

        spinnerDayFilter.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"}
        ));

        spinnerDayFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}