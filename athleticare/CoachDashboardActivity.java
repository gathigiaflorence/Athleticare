package com.example.athleticare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CoachDashboardActivity extends AppCompatActivity {

    LinearLayout cardViewTeam, cardViewRecommendations, cardLogout;
    TextView welcomeText;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coachdashboard);

       
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

       
        welcomeText = findViewById(R.id.welcomeTextCoach);
        cardViewTeam = findViewById(R.id.cardViewTeam);
        cardViewRecommendations = findViewById(R.id.cardViewRecommendations);
        cardLogout = findViewById(R.id.cardLogout);

      
        fetchCoachName();

        
        cardViewTeam.setOnClickListener(v -> {
            Intent intent = new Intent(CoachDashboardActivity.this, TeamListActivity.class);
            startActivity(intent);
        });

       
        cardViewRecommendations.setOnClickListener(v -> {
            Intent intent = new Intent(CoachDashboardActivity.this, ViewRecommendationsActivity.class);
            startActivity(intent);
        });

      
        cardLogout.setOnClickListener(v -> {
            mAuth.signOut();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(CoachDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
        });
    }

    private void fetchCoachName() {

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String fullName = documentSnapshot.getString("name");

                        if (fullName != null && !fullName.trim().isEmpty()) {

                            String firstName = fullName.contains(" ")
                                    ? fullName.split(" ")[0]
                                    : fullName;

                            welcomeText.setText("Welcome Coach " + firstName + "!");
                        } else {
                            welcomeText.setText("Welcome Coach!");
                        }

                    } else {
                        welcomeText.setText("Welcome Coach!");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load name", Toast.LENGTH_SHORT).show();
                    welcomeText.setText("Welcome Coach!");
                });
    }
}
