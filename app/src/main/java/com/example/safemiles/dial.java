package com.example.safemiles;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class dial extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dial);

        setupDialer(R.id.policeNumber, "100");
        setupDialer(R.id.ambulanceNumber, "108");
        setupDialer(R.id.womenHelpline, "1091");
        setupDialer(R.id.roadHelpline, "1073");
    }

    private void setupDialer(int textViewId, String phoneNumber) {
        TextView numberView = findViewById(textViewId);
        numberView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });
    }
}
