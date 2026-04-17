package com.example.athleticare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class AddInjuryActivity extends AppCompatActivity {

    private static final String TAG = "AddInjuryActivity";

    LinearLayout cardKnee, cardShoulder, cardBack, cardQuad, cardHamstring,
            cardAnkle, cardGroin, cardElbow, cardWrist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addinjury);

        initViews();
        initClicks();
    }

    private void initViews() {

        cardKnee = findViewById(R.id.cardKnee);
        cardShoulder = findViewById(R.id.cardShoulder);
        cardBack = findViewById(R.id.cardBack);
        cardQuad = findViewById(R.id.cardQuad);
        cardHamstring = findViewById(R.id.cardHamstring);
        cardAnkle = findViewById(R.id.cardAnkle);
        cardGroin = findViewById(R.id.cardGroin);
        cardElbow = findViewById(R.id.cardElbow);
        cardWrist = findViewById(R.id.cardWrist);
    }

    private void initClicks() {

        // central mapping (much easier to maintain)
        Map<LinearLayout, String> injuryMap = new HashMap<>();

        injuryMap.put(cardKnee, "KNEE");
        injuryMap.put(cardShoulder, "SHOULDER");
        injuryMap.put(cardBack, "BACK");
        injuryMap.put(cardQuad, "QUADRICEPS");
        injuryMap.put(cardHamstring, "HAMSTRING");
        injuryMap.put(cardAnkle, "ANKLE");
        injuryMap.put(cardGroin, "GROIN");
        injuryMap.put(cardElbow, "ELBOW");
        injuryMap.put(cardWrist, "WRIST");

        for (Map.Entry<LinearLayout, String> entry : injuryMap.entrySet()) {
            attach(entry.getKey(), entry.getValue());
        }
    }

    private void attach(LinearLayout card, String bodyPart) {

        if (card == null) {
            Log.w(TAG, "Null card detected for bodyPart: " + bodyPart);
            return;
        }

        card.setOnClickListener(v -> {

            Intent intent = new Intent(this, AddInjuryFormActivity.class);
            intent.putExtra("BODY_PART", bodyPart);

            startActivity(intent);
        });
    }
}