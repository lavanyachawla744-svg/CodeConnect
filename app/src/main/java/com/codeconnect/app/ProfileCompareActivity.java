package com.codeconnect.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * ProfileCompareActivity - Shows detailed comparison using REAL data passed from CompareActivity.
 * No more hardcoded Alex Rivera / Jordan Smith — uses the actual LeetCode usernames and stats.
 */
public class ProfileCompareActivity extends AppCompatActivity {

    private RadarChartView compareRadarChart;

    // Stats views in layout
    private TextView tvUser1Name, tvUser2Name;
    private TextView tvLcUser1, tvLcUser2;
    private TextView tvUser1Legend, tvUser2Legend;
    private ProgressBar pbLcUser1, pbLcUser2;

    // Easy / Medium / Hard breakdown
    private TextView tvUser1Easy, tvUser1Medium, tvUser1Hard;
    private TextView tvUser2Easy, tvUser2Medium, tvUser2Hard;
    private ProgressBar pbUser1Easy, pbUser1Medium, pbUser1Hard;
    private ProgressBar pbUser2Easy, pbUser2Medium, pbUser2Hard;

    // Platform labels
    private TextView tvUser1PlatformLabel, tvUser2PlatformLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_compare);

        initViews();

        // Get data passed from CompareActivity
        Intent intent = getIntent();
        String user1 = intent.getStringExtra("user1_name");
        String user2 = intent.getStringExtra("user2_name");
        int u1Total = intent.getIntExtra("user1_total", 0);
        int u2Total = intent.getIntExtra("user2_total", 0);
        int u1Easy = intent.getIntExtra("user1_easy", 0);
        int u1Medium = intent.getIntExtra("user1_medium", 0);
        int u1Hard = intent.getIntExtra("user1_hard", 0);
        int u2Easy = intent.getIntExtra("user2_easy", 0);
        int u2Medium = intent.getIntExtra("user2_medium", 0);
        int u2Hard = intent.getIntExtra("user2_hard", 0);
        int totalEasy = intent.getIntExtra("total_easy", 800);
        int totalMedium = intent.getIntExtra("total_medium", 1700);
        int totalHard = intent.getIntExtra("total_hard", 600);

        // Use defaults if no data passed (standalone launch)
        if (user1 == null || user1.isEmpty()) user1 = "User 1";
        if (user2 == null || user2.isEmpty()) user2 = "User 2";

        populateUI(user1, user2, u1Total, u2Total,
                u1Easy, u1Medium, u1Hard,
                u2Easy, u2Medium, u2Hard,
                totalEasy, totalMedium, totalHard);

        setupRadarChart(u1Easy, u1Medium, u1Hard, u2Easy, u2Medium, u2Hard);
        setupCompareButton();
    }

    private void initViews() {
        compareRadarChart = findViewById(R.id.compareRadarChart);
        tvUser1Name = findViewById(R.id.tvUser1Name);
        tvUser2Name = findViewById(R.id.tvUser2Name);
        tvLcUser1 = findViewById(R.id.tvLcUser1);
        tvLcUser2 = findViewById(R.id.tvLcUser2);
        pbLcUser1 = findViewById(R.id.pbLcUser1);
        pbLcUser2 = findViewById(R.id.pbLcUser2);
        tvUser1Legend = findViewById(R.id.tvUser1Legend);
        tvUser2Legend = findViewById(R.id.tvUser2Legend);
    }

    private void populateUI(String user1, String user2,
                             int u1Total, int u2Total,
                             int u1Easy, int u1Medium, int u1Hard,
                             int u2Easy, int u2Medium, int u2Hard,
                             int totalEasy, int totalMedium, int totalHard) {
        // Names in header
        if (tvUser1Name != null) tvUser1Name.setText(user1);
        if (tvUser2Name != null) tvUser2Name.setText(user2);

        // Legend names
        if (tvUser1Legend != null) tvUser1Legend.setText(user1);
        if (tvUser2Legend != null) tvUser2Legend.setText(user2);

        // LeetCode total solved
        if (tvLcUser1 != null) tvLcUser1.setText(String.valueOf(u1Total));
        if (tvLcUser2 != null) tvLcUser2.setText(String.valueOf(u2Total));

        // Progress bars for total (max 3100 = easy+med+hard totals approx)
        int maxTotal = totalEasy + totalMedium + totalHard;
        if (pbLcUser1 != null) { pbLcUser1.setMax(maxTotal); pbLcUser1.setProgress(u1Total); }
        if (pbLcUser2 != null) { pbLcUser2.setMax(maxTotal); pbLcUser2.setProgress(u2Total); }

        // Update platform labels dynamically
        TextView user1PlatformLabel = findViewById(R.id.tvUser1PlatformLabel);
        TextView user2PlatformLabel = findViewById(R.id.tvUser2PlatformLabel);
        if (user1PlatformLabel != null) user1PlatformLabel.setText(user1.toUpperCase() + "'S CONNECTED PLATFORMS");
        if (user2PlatformLabel != null) user2PlatformLabel.setText(user2.toUpperCase() + "'S CONNECTED PLATFORMS");
    }

    private void setupRadarChart(int u1Easy, int u1Medium, int u1Hard,
                                  int u2Easy, int u2Medium, int u2Hard) {
        // Normalize to 0..1 for radar chart based on typical max values
        float maxEasy = 850f, maxMedium = 1800f, maxHard = 650f;
        float u1Dsa = Math.min(1f, (u1Easy + u1Medium) / (maxEasy + maxMedium));
        float u1Problem = Math.min(1f, u1Hard / maxHard);
        float u1Total = Math.min(1f, (u1Easy + u1Medium + u1Hard) / (maxEasy + maxMedium + maxHard));

        float u2Dsa = Math.min(1f, (u2Easy + u2Medium) / (maxEasy + maxMedium));
        float u2Problem = Math.min(1f, u2Hard / maxHard);
        float u2Total = Math.min(1f, (u2Easy + u2Medium + u2Hard) / (maxEasy + maxMedium + maxHard));

        float[] user1Skills = {u1Total, u1Dsa, u1Problem, Math.min(1f, u1Easy / maxEasy), Math.min(1f, u1Medium / maxMedium)};
        float[] user2Skills = {u2Total, u2Dsa, u2Problem, Math.min(1f, u2Easy / maxEasy), Math.min(1f, u2Medium / maxMedium)};

        String[] skillLabels = {"Total", "Easy+Med", "Hard", "Easy", "Medium"};

        if (compareRadarChart != null) {
            compareRadarChart.setLabels(skillLabels);
            compareRadarChart.setData1(user1Skills);
            compareRadarChart.setData2(user2Skills);
        }
    }

    private void setupCompareButton() {
        View btn = findViewById(R.id.btnCompareAnother);
        if (btn != null) {
            btn.setOnClickListener(v -> finish()); // go back to CompareActivity
        }
    }
}
