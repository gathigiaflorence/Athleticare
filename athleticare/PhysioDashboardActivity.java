package com.example.athleticare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PhysioDashboardActivity extends AppCompatActivity {

    TextView welcomeTextPhysio;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    LinearLayout cardAddInjury, cardViewInjuries, cardAppointments, cardFollowUp, cardLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physiodashboard);

        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        
        welcomeTextPhysio = findViewById(R.id.welcomeTextPhysio);
        cardAddInjury = findViewById(R.id.cardAddInjury);
        cardViewInjuries = findViewById(R.id.cardViewInjuries);
        cardAppointments = findViewById(R.id.cardAppointments);
        cardFollowUp = findViewById(R.id.cardFollowUp);
        cardLogout = findViewById(R.id.cardLogout);

       
        fetchPhysioName();

        
        cardAddInjury.setOnClickListener(v ->
                startActivity(new Intent(this, AddInjuryActivity.class)));

        cardViewInjuries.setOnClickListener(v ->
                startActivity(new Intent(this, ViewInjuriesActivity.class)));

        cardAppointments.setOnClickListener(v ->
                startActivity(new Intent(this, AppointmentsActivity.class)));

        cardFollowUp.setOnClickListener(v ->
                startActivity(new Intent(this, FollowUpActivity.class)));

       
        cardLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void fetchPhysioName() {

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {

                    String firstName = "Physio";

                    if (documentSnapshot.exists() && documentSnapshot.getString("name") != null) {

                        String fullName = documentSnapshot.getString("name");

                        if (fullName != null && !fullName.trim().isEmpty()) {
                            firstName = fullName.contains(" ")
                                    ? fullName.split(" ")[0]
                                    : fullName;
                        }
                    }

                    welcomeTextPhysio.setText("Welcome, Physio " + firstName + "!");

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load name", Toast.LENGTH_SHORT).show();
                    welcomeTextPhysio.setText("Welcome, Physio!");
                });
    }
}
