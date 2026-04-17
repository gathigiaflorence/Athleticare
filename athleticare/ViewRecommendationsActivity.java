package com.example.athleticare;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ViewRecommendationsActivity extends AppCompatActivity {

    private LinearLayout recommendationsContainer;
    private TextView textEmptyState;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewrecommendations);

        recommendationsContainer = findViewById(R.id.recommendationsContainer);
        textEmptyState = findViewById(R.id.textEmptyState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        listenToUpdates();
    }

    private void listenToUpdates() {

        if (mAuth.getCurrentUser() == null) return;

        String coachId = mAuth.getCurrentUser().getUid();

        db.collection("Injuries")
                .whereEqualTo("coachId", coachId)
                .addSnapshotListener((injurySnap, error) -> {

                    if (error != null) {
                        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (injurySnap == null) return;

                    Set<String> coachSports = new HashSet<>();

                    for (DocumentSnapshot doc : injurySnap.getDocuments()) {
                        String sport = doc.getString("sport");
                        if (sport != null) coachSports.add(sport);
                    }

                    db.collection("FollowUps")
                            .addSnapshotListener((followSnap, e2) -> {

                                if (e2 != null) {
                                    Toast.makeText(this, e2.getMessage(), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                recommendationsContainer.removeAllViews();

                                if (followSnap == null || followSnap.isEmpty()) {
                                    textEmptyState.setVisibility(View.VISIBLE);
                                    return;
                                }

                                textEmptyState.setVisibility(View.GONE);

                                Map<String, DocumentSnapshot> latestPerPlayer = new HashMap<>();

                                for (DocumentSnapshot doc : followSnap.getDocuments()) {

                                    String playerId = doc.getString("schoolId");
                                    String sport = doc.getString("sport");

                                    if (playerId == null || sport == null) continue;
                                    if (!coachSports.contains(sport)) continue;

                                    Timestamp ts = doc.getTimestamp("timestamp");
                                    if (ts == null) continue;

                                    if (!latestPerPlayer.containsKey(playerId)) {
                                        latestPerPlayer.put(playerId, doc);
                                    } else {
                                        Timestamp existingTs =
                                                latestPerPlayer.get(playerId).getTimestamp("timestamp");

                                        if (existingTs != null && ts.compareTo(existingTs) > 0) {
                                            latestPerPlayer.put(playerId, doc);
                                        }
                                    }
                                }

                                List<DocumentSnapshot> finalList =
                                        new ArrayList<>(latestPerPlayer.values());

                                finalList.sort((a, b) -> {
                                    Timestamp t1 = a.getTimestamp("timestamp");
                                    Timestamp t2 = b.getTimestamp("timestamp");
                                    if (t1 == null || t2 == null) return 0;
                                    return t2.compareTo(t1);
                                });

                                for (DocumentSnapshot doc : finalList) {
                                    addCard(doc);
                                }
                            });
                });
    }

    private void addCard(DocumentSnapshot doc) {

        String name = safe(doc.getString("name"));
        String sport = safe(doc.getString("sport"));
        String injuryType = safe(doc.getString("injuryType"));
        String injuryArea = safe(doc.getString("injuryArea"));
        String recommendation = safe(doc.getString("recommendation"));

        Timestamp ts = doc.getTimestamp("timestamp");

        String time = getTimeAgo(ts);

        TextView card = new TextView(this);

        card.setText(
                "Athlete: " + name + "\n" +
                        "Sport: " + sport + "\n" +
                        "Injury Type: " + injuryType + "\n" +
                        "Injury Area: " + injuryArea + "\n" +
                        "Updated: " + time + "\n\n" +
                        "Recommendation:\n" + recommendation
        );

        card.setPadding(24, 24, 24, 24);
        card.setTextSize(14f);
        card.setBackgroundResource(R.drawable.rounded_white_card);

        card.setTextColor(android.graphics.Color.BLACK);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);

        recommendationsContainer.addView(card);
    }

    private String getTimeAgo(Timestamp timestamp) {

        if (timestamp == null) return "N/A";

        long time = timestamp.toDate().getTime();
        long now = System.currentTimeMillis();

        long diff = now - time;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hours ago";
        return days + " days ago";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}