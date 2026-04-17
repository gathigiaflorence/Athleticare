package com.example.athleticare;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ViewInjuriesActivity extends AppCompatActivity {

    EditText editTextSearchId, editTextSearchSport, editTextBodyPart, editTextSeverity;
    Button btnSearchInjuries;

    RecyclerView recyclerInjuries;
    TextView textEmptyState, textLoading;

    FirebaseFirestore db;

    List<InjuryModel> injuryList;
    InjuryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewinjuries);

        db = FirebaseFirestore.getInstance();

        // views (MATCHED TO XML)
        editTextSearchId = findViewById(R.id.editTextSearchId);
        editTextSearchSport = findViewById(R.id.editTextSearchSport);
        editTextBodyPart = findViewById(R.id.editTextBodyPart);
        editTextSeverity = findViewById(R.id.editTextSeverity);
        btnSearchInjuries = findViewById(R.id.btnSearchInjuries);

        recyclerInjuries = findViewById(R.id.recyclerInjuries);
        textEmptyState = findViewById(R.id.textEmptyState);
        textLoading = findViewById(R.id.textLoading);

        injuryList = new ArrayList<>();
        adapter = new InjuryAdapter(injuryList);

        recyclerInjuries.setLayoutManager(new LinearLayoutManager(this));
        recyclerInjuries.setAdapter(adapter);

        btnSearchInjuries.setOnClickListener(v -> searchInjuries());
    }

    private void searchInjuries() {

        String schoolId = editTextSearchId.getText().toString().trim();
        String sport = editTextSearchSport.getText().toString().trim();
        String bodyPart = editTextBodyPart.getText().toString().trim();
        String severity = editTextSeverity.getText().toString().trim();

        injuryList.clear();
        adapter.notifyDataSetChanged();

        textEmptyState.setVisibility(View.GONE);
        textLoading.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(schoolId)
                && TextUtils.isEmpty(sport)
                && TextUtils.isEmpty(bodyPart)
                && TextUtils.isEmpty(severity)) {

            textLoading.setVisibility(View.GONE);
            Toast.makeText(this, "Enter at least one filter", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Injuries")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    textLoading.setVisibility(View.GONE);

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        String docSchoolId = doc.getString("schoolId");
                        String docSport = doc.getString("sport");
                        String docBodyPart = doc.getString("injuryArea");
                        String docSeverity = doc.getString("severity");

                        String name = doc.getString("name");
                        String injuryType = doc.getString("injuryType");
                        String injuryArea = doc.getString("injuryArea");
                        String date = doc.getString("injuryDate");

                        boolean matchId = TextUtils.isEmpty(schoolId)
                                || (docSchoolId != null && docSchoolId.equalsIgnoreCase(schoolId));

                        boolean matchSport = TextUtils.isEmpty(sport)
                                || (docSport != null && docSport.equalsIgnoreCase(sport));

                        boolean matchBody = TextUtils.isEmpty(bodyPart)
                                || (docBodyPart != null && docBodyPart.equalsIgnoreCase(bodyPart));

                        boolean matchSeverity = TextUtils.isEmpty(severity)
                                || (docSeverity != null && docSeverity.equalsIgnoreCase(severity));

                        if (matchId && matchSport && matchBody && matchSeverity) {

                            injuryList.add(new InjuryModel(
                                    name != null ? name : "Unknown",
                                    docSchoolId != null ? docSchoolId : "Unknown",
                                    docSport != null ? docSport : "Unknown",
                                    injuryType != null ? injuryType : "Unknown",
                                    injuryArea != null ? injuryArea : "Unknown",
                                    date != null ? date : "Unknown"
                            ));
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (injuryList.isEmpty()) {
                        textEmptyState.setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e -> {
                    textLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}