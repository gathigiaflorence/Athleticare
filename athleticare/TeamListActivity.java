package com.example.athleticare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class TeamListActivity extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    RecyclerView recyclerTeam;
    LinearLayout teamButtonsLayout;
    Button btnAddPlayer;

    CardView cardMensTeam, cardWomensTeam;

    List<Player> playerList;
    PlayerAdapter adapter;

    String currentTeamType = "Men";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teamlist);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Views
        recyclerTeam = findViewById(R.id.recyclerTeam);
        teamButtonsLayout = findViewById(R.id.teamButtonsLayout);
        btnAddPlayer = findViewById(R.id.btnAddPlayer);

        cardMensTeam = findViewById(R.id.cardMensTeam);
        cardWomensTeam = findViewById(R.id.cardWomensTeam);

        // Recycler setup
        playerList = new ArrayList<>();
        adapter = new PlayerAdapter(this, playerList);

        recyclerTeam.setLayoutManager(new LinearLayoutManager(this));
        recyclerTeam.setAdapter(adapter);

        // Default load
        loadTeamData(currentTeamType);

        // Team switch
        cardMensTeam.setOnClickListener(v -> {
            currentTeamType = "Men";
            loadTeamData(currentTeamType);
        });

        cardWomensTeam.setOnClickListener(v -> {
            currentTeamType = "Women";
            loadTeamData(currentTeamType);
        });

        // Add player
        btnAddPlayer.setOnClickListener(v -> {
            startActivity(new Intent(TeamListActivity.this, AddTeamActivity.class));
        });
    }

    private void loadTeamData(String teamType) {

        playerList.clear();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        String coachId = mAuth.getCurrentUser().getUid();

        db.collection("Teams")
                .whereEqualTo("coachId", coachId)
                .whereEqualTo("teamType", teamType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        Player player = new Player(
                                doc.getString("name"),
                                doc.getString("schoolId"),
                                doc.getLong("age")
                        );

                        playerList.add(player);
                    }

                    adapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}