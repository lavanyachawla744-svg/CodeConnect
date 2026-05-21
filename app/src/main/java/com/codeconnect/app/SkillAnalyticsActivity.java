package com.codeconnect.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * SkillAnalyticsActivity - Displays expertise matrix radar chart,
 * time distribution bars, and weekly growth bar chart.
 * Uses sample data to demonstrate skill visualization.
 */
public class SkillAnalyticsActivity extends AppCompatActivity {

    private RadarChartView radarChart;
    private BarChartView barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_analytics);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupRadarChart();
        setupBarChart();
        setupDownloadButton();
    }

    private void setupRadarChart() {
        radarChart = findViewById(R.id.radarChart);
        // Sample skill data (0.0 to 1.0)
        float[] skillData = {0.85f, 0.65f, 0.45f, 0.70f, 0.55f};
        String[] skillLabels = {"DSA", "Web Dev", "Sys Design", "ML/AI", "DevOps"};
        radarChart.setLabels(skillLabels);
        radarChart.setData1(skillData);
    }

    private void setupBarChart() {
        barChart = findViewById(R.id.barChart);
        // Sample weekly data (problems solved per day)
        float[] weeklyData = {12, 8, 15, 22, 18, 25, 20};
        String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        barChart.setData(weeklyData, dayLabels);
        barChart.setHighlightIndex(5); // Saturday is highlighted
    }

    private void setupDownloadButton() {
        findViewById(R.id.btnDownloadLog).setOnClickListener(v ->
                Toast.makeText(this, "Full activity log downloaded!", Toast.LENGTH_SHORT).show());
    }
}
