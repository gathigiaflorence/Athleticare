package com.example.athleticare;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AddTeamActivity extends AppCompatActivity {

    EditText editTextName, editTextSchoolId, editTextAge;
    Spinner spinnerGender;
    Button btnSave;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    String[] genders = {"Male", "Female"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addteam);

        editTextName = findViewById(R.id.editTextName);
        editTextSchoolId = findViewById(R.id.editTextSchoolId);
        editTextAge = findViewById(R.id.editTextAge);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSave = findViewById(R.id.btnSavePlayer);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        btnSave.setOnClickListener(v -> savePlayer());
    }

    private void savePlayer() {

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = editTextName.getText().toString().trim();
        String schoolId = editTextSchoolId.getText().toString().trim();
        String ageStr = editTextAge.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(schoolId) || TextUtils.isEmpty(ageStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid age", Toast.LENGTH_SHORT).show();
            return;
        }

        String coachId = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(coachId).get()
                .addOnSuccessListener(doc -> {

                   
                    String sport = doc.getString("sport");

                    if (sport == null || sport.trim().isEmpty()) {
                        sport = "Unknown";
                    }

                    createPlayer(name, schoolId, age, gender, coachId, sport);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch coach sport", Toast.LENGTH_SHORT).show()
                );
    }

    private void createPlayer(String name, String schoolId, int age,
                              String gender, String coachId, String sport) {

        String teamType = gender.equals("Male") ? "Men" : "Women";

        HashMap<String, Object> player = new HashMap<>();
        player.put("name", name);
        player.put("schoolId", schoolId);
        player.put("age", age);
        player.put("gender", gender);
        player.put("teamType", teamType);
        player.put("coachId", coachId);

       
        player.put("sport", sport);

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        db.collection("Teams")
                .add(player)
                .addOnSuccessListener(ref -> {

                    Toast.makeText(this, "Player added successfully", Toast.LENGTH_SHORT).show();

                    editTextName.setText("");
                    editTextSchoolId.setText("");
                    editTextAge.setText("");
                    spinnerGender.setSelection(0);

                    btnSave.setEnabled(true);
                    btnSave.setText("Save Player");
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    btnSave.setEnabled(true);
                    btnSave.setText("Save Player");
                });
    }
}
