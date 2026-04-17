package com.example.athleticare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView goToSignupText;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.google.firebase.FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        goToSignupText = findViewById(R.id.signupRedirect);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = auth.getCurrentUser().getUid();

                        db.collection("Users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String role = documentSnapshot.getString("role");

                                        if (role == null) {
                                            Toast.makeText(MainActivity.this, "Role not found for this user.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        Toast.makeText(MainActivity.this, "Role: " + role, Toast.LENGTH_SHORT).show(); 

                                        switch (role.toLowerCase()) {
                                            case "coach":
                                                startActivity(new Intent(MainActivity.this, CoachDashboardActivity.class));
                                                break;
                                            case "physio":
                                                startActivity(new Intent(MainActivity.this, PhysioDashboardActivity.class));
                                                break;
                                            case "other medic":
                                                startActivity(new Intent(MainActivity.this, MedicDashboardActivity.class));
                                                break;
                                            default:
                                                Toast.makeText(MainActivity.this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "User record not found in Firestore", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error loading role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        goToSignupText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
