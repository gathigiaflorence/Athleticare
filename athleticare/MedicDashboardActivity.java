package com.example.athleticare;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.OutputStream;
import java.util.Date;

public class MedicDashboardActivity extends AppCompatActivity {

    private TextView welcomeTextMedic;
    private EditText editTextPlayerId;
    private Button btnGenerateReport, btnExportPdf;
    private LinearLayout tableInjuries;

    private LinearLayout btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String playerName = "";
    private String injuryType = "";
    private Date injuryDate = null;
    private String latestFollowUp = "";
    private String latestRecommendation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicdashboard);

        welcomeTextMedic = findViewById(R.id.welcomeTextMedic);
        editTextPlayerId = findViewById(R.id.editTextPlayerId);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        tableInjuries = findViewById(R.id.tableInjuries);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnLogout = findViewById(R.id.btnLogout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fetchMedicName();

        btnGenerateReport.setOnClickListener(v -> {
            String schoolId = editTextPlayerId.getText().toString().trim();
            if (TextUtils.isEmpty(schoolId)) {
                Toast.makeText(this, "Enter Player ID", Toast.LENGTH_SHORT).show();
                return;
            }
            generateReport(schoolId);
        });

        btnExportPdf.setOnClickListener(v -> exportReportToPdf());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void fetchMedicName() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        welcomeTextMedic.setText("Welcome, Medic " +
                                (name != null && name.contains(" ")
                                        ? name.split(" ")[0]
                                        : "Medic") + "!");
                    }
                });
    }

    private void generateReport(String schoolId) {
        resetData();

        db.collection("Injuries")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        Toast.makeText(this, "No injuries found", Toast.LENGTH_SHORT).show();
                        displayReport();
                        return;
                    }

                    DocumentSnapshot latest = null;
                    Date latestDate = null;

                    for (DocumentSnapshot doc : query) {
                        Date docDate = getDate(doc);

                        if (latestDate == null || (docDate != null && docDate.after(latestDate))) {
                            latestDate = docDate;
                            latest = doc;
                        }
                    }

                    if (latest != null) {
                        playerName = safe(latest.getString("name"));
                        injuryType = safe(latest.getString("injuryType"));
                        injuryDate = latestDate;
                    }

                    fetchLatestFollowUp(schoolId);
                });
    }

    private void fetchLatestFollowUp(String schoolId) {
        db.collection("FollowUps")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(query -> {

                    Date latestDate = null;
                    latestFollowUp = "";

                    for (DocumentSnapshot doc : query) {
                        Date d = getDate(doc);

                        if (latestDate == null || (d != null && d.after(latestDate))) {
                            latestDate = d;
                            latestFollowUp = safe(doc.getString("treatmentDone"));
                        }
                    }

                    fetchLatestRecommendation(schoolId);
                });
    }

    private void fetchLatestRecommendation(String schoolId) {
        db.collection("FollowUps")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(query -> {

                    Date latestDate = null;
                    latestRecommendation = "";

                    for (DocumentSnapshot doc : query) {
                        Date d = getDate(doc);

                        if (latestDate == null || (d != null && d.after(latestDate))) {
                            latestDate = d;
                            latestRecommendation = safe(doc.getString("recommendation"));
                        }
                    }

                    displayReport();
                });
    }

    private void displayReport() {
        tableInjuries.removeAllViews();

        LinearLayout reportCard = new LinearLayout(this);
        reportCard.setOrientation(LinearLayout.VERTICAL);
        reportCard.setPadding(32, 32, 32, 32);
        reportCard.setBackgroundResource(R.drawable.rounded_white_card);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(0, 0, 0, 24);
        reportCard.setLayoutParams(params);

        TextView header = new TextView(this);
        header.setText("ATHLETICARE MEDICAL REPORT");
        header.setTextSize(18f);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setPadding(0, 0, 0, 16);
        reportCard.addView(header);

        reportCard.addView(sectionTitle("PATIENT INFORMATION"));
        reportCard.addView(simpleText("Name: " + playerName));

        reportCard.addView(sectionTitle("INJURY DETAILS"));
        reportCard.addView(simpleText("Injury Type: " + injuryType));
        reportCard.addView(simpleText("Injury Duration: " + calculateDuration(injuryDate)));

        reportCard.addView(sectionTitle("FOLLOW-UP"));
        reportCard.addView(simpleText(latestFollowUp));

        reportCard.addView(sectionTitle("RECOMMENDATION"));
        reportCard.addView(simpleText(latestRecommendation));

        tableInjuries.addView(reportCard);
    }

    private TextView sectionTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setTextSize(14f);
        tv.setPadding(0, 16, 0, 8);
        return tv;
    }

    private TextView simpleText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(0, 6, 0, 6);
        return tv;
    }

    private void exportReportToPdf() {

        if (TextUtils.isEmpty(playerName)) {
            Toast.makeText(this, "Generate report first", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();

        try {

            Paint titlePaint = new Paint();
            titlePaint.setTextSize(18);
            titlePaint.setFakeBoldText(true);
            titlePaint.setAntiAlias(true);
            titlePaint.setColor(android.graphics.Color.BLACK);

            Paint textPaint = new Paint();
            textPaint.setTextSize(12);
            textPaint.setAntiAlias(true);
            textPaint.setColor(android.graphics.Color.BLACK);
            textPaint.setFakeBoldText(true); // makes it visibly darker

            PdfDocument.PageInfo pageInfo =
                    new PdfDocument.PageInfo.Builder(595, 842, 1).create();

            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int y = 60;
            int centerX = 200;

            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.login_background);
            Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, 120, 120, false);
            canvas.drawBitmap(scaledLogo, centerX, y, null);

            y += 150;

            Paint appNamePaint = new Paint();
            appNamePaint.setTextSize(22);
            appNamePaint.setFakeBoldText(true);
            appNamePaint.setColor(android.graphics.Color.parseColor("#00695C"));

            canvas.drawText("AthletiCare", centerX, y, appNamePaint);

            y += 30;

            Paint mottoPaint = new Paint();
            mottoPaint.setTextSize(14);
            mottoPaint.setAntiAlias(true);
            mottoPaint.setColor(android.graphics.Color.BLACK);

            canvas.drawText("Injury Recovery Starts Here", centerX - 40, y, mottoPaint);

            y += 40;

            Paint linePaint = new Paint();
            linePaint.setStrokeWidth(2);
            linePaint.setColor(android.graphics.Color.BLACK);

            canvas.drawLine(40, y, 555, y, linePaint);

            y += 40;

            canvas.drawText("MEDICAL REPORT", 200, y, titlePaint);
            y += 40;

            canvas.drawText("Name: " + playerName, 40, y, textPaint);
            y += 25;

            canvas.drawText("Injury: " + injuryType, 40, y, textPaint);
            y += 25;

            canvas.drawText("Duration: " + calculateDuration(injuryDate), 40, y, textPaint);
            y += 25;

            canvas.drawText("Follow-Up: " + latestFollowUp, 40, y, textPaint);
            y += 25;

            canvas.drawText("Recommendation: " + latestRecommendation, 40, y, textPaint);

            pdfDocument.finishPage(page);

            String fileName = "Medical_Report_" + System.currentTimeMillis() + ".pdf";

            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_DOWNLOADS + "/AthletiCare");

            android.net.Uri uri = getContentResolver().insert(
                    android.provider.MediaStore.Files.getContentUri("external"),
                    values
            );

            OutputStream out = getContentResolver().openOutputStream(uri);

            pdfDocument.writeTo(out);
            out.flush();
            out.close();
            pdfDocument.close();

            Toast.makeText(this,
                    "Saved to Downloads/AthletiCare",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this,
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private Date getDate(DocumentSnapshot doc) {
        Timestamp ts = doc.getTimestamp("timestamp");
        return ts != null ? ts.toDate() : null;
    }

    private String calculateDuration(Date injuryDate) {
        if (injuryDate == null) return "N/A";

        long diff = System.currentTimeMillis() - injuryDate.getTime();
        long days = diff / (1000 * 60 * 60 * 24);

        if (days < 7) return days + " days";
        return (days / 7) + " weeks " + (days % 7) + " days";
    }

    private void resetData() {
        playerName = "";
        injuryType = "";
        injuryDate = null;
        latestFollowUp = "";
        latestRecommendation = "";
        tableInjuries.removeAllViews();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}