package com.example.athleticare;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    Spinner spinnerRole, spinnerTeam;
    TextView textViewSelectTeam;
    Button btnSignUp;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    String[] roles = {"Coach", "Physio", "Other Medic"};
    String[] teams = {"Football", "Volleyball", "Swimming", "Handball", "Basketball",
            "Hockey", "Netball", "Rugby", "Athletics", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        spinnerTeam = findViewById(R.id.spinnerTeam);
        textViewSelectTeam = findViewById(R.id.textViewSelectTeam);
        btnSignUp = findViewById(R.id.btnSignUp);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ArrayAdapter<String> roleAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        ArrayAdapter<String> teamAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teams);
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeam.setAdapter(teamAdapter);

        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean isCoach = roles[position].equals("Coach");
                spinnerTeam.setVisibility(isCoach ? View.VISIBLE : View.GONE);
                textViewSelectTeam.setVisibility(isCoach ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSignUp.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        String sport = role.equals("Coach")
                ? spinnerTeam.getSelectedItem().toString()
                : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (role.equals("Coach") && TextUtils.isEmpty(sport)) {
            Toast.makeText(this, "Please select a sport", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Toast.makeText(this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = mAuth.getCurrentUser().getUid();

                    String firstName = name.contains(" ")
                            ? name.split(" ")[0]
                            : name;

                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("name", name);
                    userMap.put("firstName", firstName);
                    userMap.put("email", email);
                    userMap.put("role", role);

                   
                    userMap.put("sport", role.equals("Coach") ? sport : "None");

                    db.collection("Users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                redirectToDashboard(role);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Failed to save user data: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                            );
                });
    }

    private void redirectToDashboard(String role) {
        Intent intent;

        if (role.equals("Coach")) {
            intent = new Intent(this, CoachDashboardActivity.class);
        } else if (role.equals("Physio")) {
            intent = new Intent(this, PhysioDashboardActivity.class);
        } else {
            intent = new Intent(this, MedicDashboardActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
