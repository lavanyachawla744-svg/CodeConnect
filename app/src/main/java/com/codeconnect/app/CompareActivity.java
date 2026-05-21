package com.codeconnect.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * CompareActivity - Compare two LeetCode users side by side.
 * Fetches stats from the alfa-leetcode-api and displays a comparison.
 */
public class CompareActivity extends AppCompatActivity {

    private EditText etUsername1, etUsername2;
    private MaterialButton btnCompare;
    private ProgressBar progressCompare;
    private TextView tvCompareError;
    private LinearLayout resultsSection;
    private MaterialCardView cardWinner;

    // User 1 views
    private TextView tvUser1Name, tvUser1Total, tvUser1Easy, tvUser1Medium, tvUser1Hard;
    private ProgressBar pbUser1Easy, pbUser1Medium, pbUser1Hard;

    // User 2 views
    private TextView tvUser2Name, tvUser2Total, tvUser2Easy, tvUser2Medium, tvUser2Hard;
    private ProgressBar pbUser2Easy, pbUser2Medium, pbUser2Hard;

    private TextView tvWinner;

    private LeetCodeApiService apiService;
    private LeetCodeApiService.UserStats user1Stats, user2Stats;
    private int loadedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        apiService = new LeetCodeApiService();

        initViews();
        setupListeners();
    }

    private void initViews() {
        // Input views
        etUsername1 = findViewById(R.id.etUsername1);
        etUsername2 = findViewById(R.id.etUsername2);
        btnCompare = findViewById(R.id.btnCompare);
        progressCompare = findViewById(R.id.progressCompare);
        tvCompareError = findViewById(R.id.tvCompareError);
        resultsSection = findViewById(R.id.resultsSection);
        cardWinner = findViewById(R.id.cardWinner);

        // User 1 result views
        tvUser1Name = findViewById(R.id.tvUser1Name);
        tvUser1Total = findViewById(R.id.tvUser1Total);
        tvUser1Easy = findViewById(R.id.tvUser1Easy);
        tvUser1Medium = findViewById(R.id.tvUser1Medium);
        tvUser1Hard = findViewById(R.id.tvUser1Hard);
        pbUser1Easy = findViewById(R.id.pbUser1Easy);
        pbUser1Medium = findViewById(R.id.pbUser1Medium);
        pbUser1Hard = findViewById(R.id.pbUser1Hard);

        // User 2 result views
        tvUser2Name = findViewById(R.id.tvUser2Name);
        tvUser2Total = findViewById(R.id.tvUser2Total);
        tvUser2Easy = findViewById(R.id.tvUser2Easy);
        tvUser2Medium = findViewById(R.id.tvUser2Medium);
        tvUser2Hard = findViewById(R.id.tvUser2Hard);
        pbUser2Easy = findViewById(R.id.pbUser2Easy);
        pbUser2Medium = findViewById(R.id.pbUser2Medium);
        pbUser2Hard = findViewById(R.id.pbUser2Hard);

        tvWinner = findViewById(R.id.tvWinner);
    }

    private void setupListeners() {
        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Compare button
        btnCompare.setOnClickListener(v -> startComparison());
    }

    /**
     * Validate inputs and start fetching data for both users
     */
    private void startComparison() {
        String username1 = etUsername1.getText().toString().trim();
        String username2 = etUsername2.getText().toString().trim();

        if (username1.isEmpty() || username2.isEmpty()) {
            showError(getString(R.string.error_empty_username));
            return;
        }

        // Reset state
        loadedCount = 0;
        user1Stats = null;
        user2Stats = null;
        showLoading(true);
        hideError();
        resultsSection.setVisibility(View.GONE);
        cardWinner.setVisibility(View.GONE);

        // Fetch both users in parallel
        apiService.fetchUserStats(username1, new LeetCodeApiService.OnStatsLoadedListener() {
            @Override
            public void onSuccess(LeetCodeApiService.UserStats stats) {
                user1Stats = stats;
                onUserDataLoaded();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showError(errorMessage);
            }
        });

        apiService.fetchUserStats(username2, new LeetCodeApiService.OnStatsLoadedListener() {
            @Override
            public void onSuccess(LeetCodeApiService.UserStats stats) {
                user2Stats = stats;
                onUserDataLoaded();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showError(errorMessage);
            }
        });
    }

    /**
     * Called when each user's data is loaded. When both are ready, display results.
     */
    private synchronized void onUserDataLoaded() {
        loadedCount++;
        if (loadedCount == 2 && user1Stats != null && user2Stats != null) {
            showLoading(false);
            displayResults();
        }
    }

    /**
     * Display the comparison results in the UI
     */
    private void displayResults() {
        resultsSection.setVisibility(View.VISIBLE);

        // User 1 stats
        tvUser1Name.setText(user1Stats.username);
        tvUser1Total.setText(String.valueOf(user1Stats.totalSolved));
        tvUser1Easy.setText(user1Stats.easySolved + " / " + user1Stats.totalEasy);
        tvUser1Medium.setText(user1Stats.mediumSolved + " / " + user1Stats.totalMedium);
        tvUser1Hard.setText(user1Stats.hardSolved + " / " + user1Stats.totalHard);

        // Progress bars for User 1
        setProgress(pbUser1Easy, user1Stats.easySolved, user1Stats.totalEasy);
        setProgress(pbUser1Medium, user1Stats.mediumSolved, user1Stats.totalMedium);
        setProgress(pbUser1Hard, user1Stats.hardSolved, user1Stats.totalHard);

        // User 2 stats
        tvUser2Name.setText(user2Stats.username);
        tvUser2Total.setText(String.valueOf(user2Stats.totalSolved));
        tvUser2Easy.setText(user2Stats.easySolved + " / " + user2Stats.totalEasy);
        tvUser2Medium.setText(user2Stats.mediumSolved + " / " + user2Stats.totalMedium);
        tvUser2Hard.setText(user2Stats.hardSolved + " / " + user2Stats.totalHard);

        // Progress bars for User 2
        setProgress(pbUser2Easy, user2Stats.easySolved, user2Stats.totalEasy);
        setProgress(pbUser2Medium, user2Stats.mediumSolved, user2Stats.totalMedium);
        setProgress(pbUser2Hard, user2Stats.hardSolved, user2Stats.totalHard);

        // Determine winner
        showWinner();
    }

    /**
     * Set a progress bar's value as a percentage
     */
    private void setProgress(ProgressBar pb, int solved, int total) {
        if (total > 0) {
            int percentage = (int) ((solved / (float) total) * 100);
            pb.setProgress(percentage);
        } else {
            pb.setProgress(0);
        }
    }

    /**
     * Determine and display the winner based on total solved questions
     */
    private void showWinner() {
        cardWinner.setVisibility(View.VISIBLE);

        if (user1Stats.totalSolved > user2Stats.totalSolved) {
            tvWinner.setText(user1Stats.username + " wins with " + user1Stats.totalSolved + " solved!");
        } else if (user2Stats.totalSolved > user1Stats.totalSolved) {
            tvWinner.setText(user2Stats.username + " wins with " + user2Stats.totalSolved + " solved!");
        } else {
            tvWinner.setText("It's a tie! Both solved " + user1Stats.totalSolved + " problems!");
        }

        // Allow tapping the winner card to open the detailed radar comparison
        cardWinner.setOnClickListener(v -> openDetailedComparison());
    }

    private void openDetailedComparison() {
        Intent intent = new Intent(this, ProfileCompareActivity.class);
        intent.putExtra("user1_name", user1Stats.username);
        intent.putExtra("user2_name", user2Stats.username);
        intent.putExtra("user1_total", user1Stats.totalSolved);
        intent.putExtra("user2_total", user2Stats.totalSolved);
        intent.putExtra("user1_easy", user1Stats.easySolved);
        intent.putExtra("user1_medium", user1Stats.mediumSolved);
        intent.putExtra("user1_hard", user1Stats.hardSolved);
        intent.putExtra("user2_easy", user2Stats.easySolved);
        intent.putExtra("user2_medium", user2Stats.mediumSolved);
        intent.putExtra("user2_hard", user2Stats.hardSolved);
        intent.putExtra("total_easy", user1Stats.totalEasy);
        intent.putExtra("total_medium", user1Stats.totalMedium);
        intent.putExtra("total_hard", user1Stats.totalHard);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressCompare.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCompare.setEnabled(!show);
        btnCompare.setText(show ? getString(R.string.comparing) : getString(R.string.compare_btn));
    }

    private void showError(String message) {
        tvCompareError.setText(message);
        tvCompareError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvCompareError.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiService != null) {
            apiService.shutdown();
        }
    }
}
