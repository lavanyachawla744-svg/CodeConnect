package com.codeconnect.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * StudentProfileCompareActivity
 *
 * Two students each enter:
 *   • LeetCode username (MANDATORY)
 *   • CodeChef username (optional)
 *
 * Live API calls:
 *   LeetCode → alfa-leetcode-api.onrender.com
 *   CodeChef → codechef-api.vercel.app (with web-scrape fallback)
 *
 * Results show side-by-side per platform + overall winner.
 */
public class StudentProfileCompareActivity extends AppCompatActivity {

    private static final int STEP_INPUT   = 0;
    private static final int STEP_RESULTS = 1;
    private int currentStep = STEP_INPUT;

    // Input views
    private ScrollView stepInputLayout;
    private EditText etLc1, etCc1, etLc2, etCc2;
    private MaterialButton btnCompare;
    private ProgressBar progressBar;
    private TextView tvInputError;

    // Results views
    private ScrollView stepResultsLayout;

    // Student 1 summary
    private TextView tvS1Name, tvS1LcScore, tvS1CcScore, tvS1Total;
    // Student 1 LeetCode details
    private TextView tvS1LcEasy, tvS1LcMedium, tvS1LcHard, tvS1LcTotal, tvS1LcRank;
    // Student 1 CodeChef details
    private TextView tvS1CcRating, tvS1CcHighest, tvS1CcProblems, tvS1CcStars, tvS1CcRank;

    // Student 2 summary
    private TextView tvS2Name, tvS2LcScore, tvS2CcScore, tvS2Total;
    // Student 2 LeetCode details
    private TextView tvS2LcEasy, tvS2LcMedium, tvS2LcHard, tvS2LcTotal, tvS2LcRank;
    // Student 2 CodeChef details
    private TextView tvS2CcRating, tvS2CcHighest, tvS2CcProblems, tvS2CcStars, tvS2CcRank;

    // Winner + actions
    private MaterialCardView cardWinner;
    private TextView tvWinnerText;
    private MaterialButton btnInfoScore, btnCompareAgain;

    // Network
    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Fetched data
    private JSONObject lc1, cc1, lc2, cc2;
    private String errLc1, errCc1, errLc2, errCc2;

    private int expectedCalls;
    private final AtomicInteger doneCalls = new AtomicInteger(0);

    // Usernames
    private String u1lc, u1cc, u2lc, u2cc;

    // Scores
    private double s1LcScore, s1CcScore, s2LcScore, s2CcScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile_compare);
        bindViews();
        setupListeners();
        showStep(STEP_INPUT);
    }

    private void bindViews() {
        stepInputLayout   = findViewById(R.id.stepInputLayout);
        stepResultsLayout = findViewById(R.id.stepResultsLayout);

        etLc1 = findViewById(R.id.etLc1);
        etCc1 = findViewById(R.id.etCc1);
        etLc2 = findViewById(R.id.etLc2);
        etCc2 = findViewById(R.id.etCc2);

        btnCompare  = findViewById(R.id.btnCompare);
        progressBar = findViewById(R.id.progressBar);
        tvInputError = findViewById(R.id.tvInputError);

        tvS1Name    = findViewById(R.id.tvS1Name);
        tvS1LcScore = findViewById(R.id.tvS1LcScore);
        tvS1CcScore = findViewById(R.id.tvS1CcScore);
        tvS1Total   = findViewById(R.id.tvS1Total);
        tvS1LcEasy  = findViewById(R.id.tvS1LcEasy);
        tvS1LcMedium= findViewById(R.id.tvS1LcMedium);
        tvS1LcHard  = findViewById(R.id.tvS1LcHard);
        tvS1LcTotal = findViewById(R.id.tvS1LcTotal);
        tvS1LcRank  = findViewById(R.id.tvS1LcRank);
        tvS1CcRating  = findViewById(R.id.tvS1CcRating);
        tvS1CcHighest = findViewById(R.id.tvS1CcHighest);
        tvS1CcProblems= findViewById(R.id.tvS1CcProblems);
        tvS1CcStars   = findViewById(R.id.tvS1CcStars);
        tvS1CcRank    = findViewById(R.id.tvS1CcRank);

        tvS2Name    = findViewById(R.id.tvS2Name);
        tvS2LcScore = findViewById(R.id.tvS2LcScore);
        tvS2CcScore = findViewById(R.id.tvS2CcScore);
        tvS2Total   = findViewById(R.id.tvS2Total);
        tvS2LcEasy  = findViewById(R.id.tvS2LcEasy);
        tvS2LcMedium= findViewById(R.id.tvS2LcMedium);
        tvS2LcHard  = findViewById(R.id.tvS2LcHard);
        tvS2LcTotal = findViewById(R.id.tvS2LcTotal);
        tvS2LcRank  = findViewById(R.id.tvS2LcRank);
        tvS2CcRating  = findViewById(R.id.tvS2CcRating);
        tvS2CcHighest = findViewById(R.id.tvS2CcHighest);
        tvS2CcProblems= findViewById(R.id.tvS2CcProblems);
        tvS2CcStars   = findViewById(R.id.tvS2CcStars);
        tvS2CcRank    = findViewById(R.id.tvS2CcRank);

        cardWinner    = findViewById(R.id.cardWinner);
        tvWinnerText  = findViewById(R.id.tvWinnerText);
        btnInfoScore  = findViewById(R.id.btnInfoScore);
        btnCompareAgain = findViewById(R.id.btnCompareAgain);
    }

    private void setupListeners() {
        btnCompare.setOnClickListener(v -> onCompareClicked());
        btnInfoScore.setOnClickListener(v -> showScoreInfoDialog());
        btnCompareAgain.setOnClickListener(v -> { resetData(); showStep(STEP_INPUT); });
    }

    private void showStep(int step) {
        currentStep = step;
        stepInputLayout.setVisibility(step == STEP_INPUT ? View.VISIBLE : View.GONE);
        stepResultsLayout.setVisibility(step == STEP_RESULTS ? View.VISIBLE : View.GONE);
    }

    // ── COMPARE BUTTON ──────────────────────────────────────────────────────

    private void onCompareClicked() {
        tvInputError.setVisibility(View.GONE);

        u1lc = etLc1.getText().toString().trim();
        u1cc = etCc1.getText().toString().trim();
        u2lc = etLc2.getText().toString().trim();
        u2cc = etCc2.getText().toString().trim();

        // LeetCode mandatory for both
        if (u1lc.isEmpty() || u2lc.isEmpty()) {
            tvInputError.setText("LeetCode username is mandatory for both students.");
            tvInputError.setVisibility(View.VISIBLE);
            return;
        }

        resetData();
        doneCalls.set(0);

        // Always 2 LeetCode calls, plus CodeChef only if username entered
        expectedCalls = 2;
        if (!u1cc.isEmpty()) expectedCalls++;
        if (!u2cc.isEmpty()) expectedCalls++;

        setLoading(true);

        executor.execute(() -> fetchLeetCode(u1lc, true));
        executor.execute(() -> fetchLeetCode(u2lc, false));
        if (!u1cc.isEmpty()) executor.execute(() -> fetchCodeChef(u1cc, true));
        if (!u2cc.isEmpty()) executor.execute(() -> fetchCodeChef(u2cc, false));
    }

    private void resetData() {
        lc1 = cc1 = lc2 = cc2 = null;
        errLc1 = errCc1 = errLc2 = errCc2 = null;
        s1LcScore = s1CcScore = s2LcScore = s2CcScore = 0;
    }

    // ── API FETCHERS ────────────────────────────────────────────────────────

    private void fetchLeetCode(String username, boolean isStudent1) {
        try {
            String solvedUrl = "https://alfa-leetcode-api.onrender.com/" + username + "/solved";
            Request req1 = new Request.Builder().url(solvedUrl).build();
            JSONObject stats = new JSONObject();
            try (Response r = http.newCall(req1).execute()) {
                if (r.isSuccessful() && r.body() != null) {
                    JSONObject j = new JSONObject(r.body().string());
                    if (!j.has("errors")) {
                        stats.put("totalSolved",  j.optInt("solvedProblem", 0));
                        stats.put("easySolved",   j.optInt("easySolved",    0));
                        stats.put("mediumSolved", j.optInt("mediumSolved",  0));
                        stats.put("hardSolved",   j.optInt("hardSolved",    0));
                    }
                }
            }

            String profileUrl = "https://alfa-leetcode-api.onrender.com/" + username;
            Request req2 = new Request.Builder().url(profileUrl).build();
            try (Response r = http.newCall(req2).execute()) {
                if (r.isSuccessful() && r.body() != null) {
                    JSONObject j = new JSONObject(r.body().string());
                    if (!j.has("errors")) {
                        stats.put("ranking",  j.optInt("ranking", 0));
                        stats.put("username", j.optString("username", username));
                    }
                }
            }

            if (stats.optInt("totalSolved", 0) == 0 && stats.optInt("ranking", 0) == 0) {
                String err = "LeetCode: User '" + username + "' not found.";
                if (isStudent1) errLc1 = err; else errLc2 = err;
            } else {
                if (!stats.has("username")) stats.put("username", username);
                if (isStudent1) lc1 = stats; else lc2 = stats;
            }
        } catch (Exception e) {
            String err = "LeetCode: Network error for '" + username + "'.";
            if (isStudent1) errLc1 = err; else errLc2 = err;
        }
        checkAllLoaded();
    }

    private void fetchCodeChef(String username, boolean isStudent1) {
        try {
            JSONObject data = null;
            String[] apiUrls = {
                "https://codechef-api.vercel.app/handle/" + username,
                "https://codechef-api.vercel.app/user/" + username,
            };

            for (String apiUrl : apiUrls) {
                if (data != null) break;
                try {
                    Request req = new Request.Builder()
                            .url(apiUrl).header("Accept", "application/json")
                            .header("User-Agent", "CodeConnect-Android").build();
                    try (Response r = http.newCall(req).execute()) {
                        if (r.isSuccessful() && r.body() != null) {
                            JSONObject j = new JSONObject(r.body().string());
                            String status = j.optString("status", "OK");
                            if (!"error".equalsIgnoreCase(status) && !"false".equalsIgnoreCase(status)) {
                                if (j.has("currentRating") || j.has("rating") || j.has("username")) {
                                    data = j;
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            // Fallback: scrape CodeChef profile page
            if (data == null) {
                try {
                    String profileUrl = "https://www.codechef.com/users/" + username;
                    Request req = new Request.Builder().url(profileUrl)
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36").build();
                    try (Response r = http.newCall(req).execute()) {
                        if (r.isSuccessful() && r.body() != null) {
                            String html = r.body().string();
                            if (!html.contains("Page Not Found") && html.contains("rating")) {
                                data = parseCodeChefHtml(html, username);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (data == null) {
                String err = "CodeChef: Could not fetch data for '" + username + "'.";
                if (isStudent1) errCc1 = err; else errCc2 = err;
            } else {
                if (!data.has("username") || data.optString("username", "").isEmpty())
                    data.put("username", username);
                if (isStudent1) cc1 = data; else cc2 = data;
            }
        } catch (Exception e) {
            String err = "CodeChef: Service temporarily unavailable.";
            if (isStudent1) errCc1 = err; else errCc2 = err;
        }
        checkAllLoaded();
    }

    private JSONObject parseCodeChefHtml(String html, String username) {
        JSONObject data = new JSONObject();
        try {
            data.put("username", username);
            int rating = extractNumber(html, "rating");
            if (rating > 0) data.put("currentRating", rating);
            int highest = extractNumber(html, "highest rating");
            if (highest > 0) data.put("highestRating", highest);
            int starCount = 0;
            int idx = html.indexOf("rating-star");
            if (idx > 0) {
                String sub = html.substring(Math.max(0, idx - 200), Math.min(html.length(), idx + 200));
                for (char c : sub.toCharArray()) { if (c == '\u2605' || c == '\u2B50') starCount++; }
            }
            if (starCount > 0) data.put("stars", starCount);
            int gRank = extractNumber(html, "Global Rank");
            if (gRank > 0) data.put("globalRank", gRank);
            int problems = extractNumber(html, "Problems Solved");
            if (problems > 0) data.put("totalProblemsSolved", problems);
            if (data.length() <= 1) return null;
        } catch (Exception e) { return null; }
        return data;
    }

    private int extractNumber(String html, String keyword) {
        try {
            int idx = html.toLowerCase().indexOf(keyword.toLowerCase());
            if (idx < 0) return 0;
            String sub = html.substring(idx, Math.min(html.length(), idx + 200));
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(sub);
            if (m.find()) return Integer.parseInt(m.group());
        } catch (Exception ignored) {}
        return 0;
    }

    // ── SYNCHRONISE COMPLETION ──────────────────────────────────────────────

    private synchronized void checkAllLoaded() {
        if (doneCalls.incrementAndGet() == expectedCalls) {
            mainHandler.post(() -> {
                setLoading(false);

                // LeetCode is mandatory – show error if either fails
                if (errLc1 != null) { showError(errLc1); return; }
                if (errLc2 != null) { showError(errLc2); return; }

                // CodeChef errors are non-fatal
                computeAndDisplay();
            });
        }
    }

    private void showError(String msg) {
        tvInputError.setText(msg);
        tvInputError.setVisibility(View.VISIBLE);
    }

    // ── SCORING ─────────────────────────────────────────────────────────────

    private double scoreLeetCode(JSONObject d) {
        if (d == null) return 0;
        int rank = d.optInt("ranking", Integer.MAX_VALUE);
        double sc = Math.min(d.optInt("easySolved",   0) * 1.0, 25)
                  + Math.min(d.optInt("mediumSolved", 0) * 2.0, 40)
                  + Math.min(d.optInt("hardSolved",   0) * 5.0, 30)
                  + (rank > 0 && rank < 100_000 ? 5 : 0);
        return Math.min(sc, 100);
    }

    private double scoreCodeChef(JSONObject d) {
        if (d == null) return 0;
        int rating   = d.optInt("currentRating", d.optInt("rating", d.optInt("current_rating", 0)));
        int problems = d.optInt("totalProblemsSolved",
                       d.optInt("problems_solved", d.optInt("problemsSolved", 0)));
        int starsInt = d.optInt("stars", 0);
        if (starsInt == 0) {
            String s = d.optString("star", d.optString("stars_string", ""));
            starsInt = s.replaceAll("[^\u2605\u2B50*]", "").length();
            if (starsInt == 0) {
                try { starsInt = Integer.parseInt(s.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
            }
        }
        int globalRank = d.optInt("globalRank", d.optInt("global_rank", d.optInt("rankNumber", 0)));
        double sc = Math.min(rating / 50.0, 40)
                  + Math.min(problems,      30)
                  + Math.min(starsInt * 4,  28)
                  + (globalRank > 0 && globalRank < 10_000 ? 2 : 0);
        return Math.min(sc, 100);
    }

    // ── DISPLAY ─────────────────────────────────────────────────────────────

    private void computeAndDisplay() {
        s1LcScore = scoreLeetCode(lc1);
        s2LcScore = scoreLeetCode(lc2);
        s1CcScore = cc1 != null ? scoreCodeChef(cc1) : (errCc1 != null ? 0 : -1);
        s2CcScore = cc2 != null ? scoreCodeChef(cc2) : (errCc2 != null ? 0 : -1);

        double s1Total = s1LcScore + (s1CcScore >= 0 ? s1CcScore : 0);
        double s2Total = s2LcScore + (s2CcScore >= 0 ? s2CcScore : 0);

        // Student 1
        tvS1Name.setText("\uD83C\uDF93 " + u1lc);
        tvS1LcScore.setText(String.format("LeetCode: %.0f/100", s1LcScore));
        tvS1CcScore.setText(s1CcScore >= 0 ? String.format("CodeChef: %.0f/100", s1CcScore) : "CodeChef: N/A");
        tvS1Total.setText(String.format("Total: %.0f", s1Total));

        fillLcDetails(lc1, errLc1, u1lc, tvS1LcEasy, tvS1LcMedium, tvS1LcHard, tvS1LcTotal, tvS1LcRank);
        fillCcDetails(cc1, errCc1, u1cc, tvS1CcRating, tvS1CcHighest, tvS1CcProblems, tvS1CcStars, tvS1CcRank);

        // Student 2
        tvS2Name.setText("\uD83C\uDF93 " + u2lc);
        tvS2LcScore.setText(String.format("LeetCode: %.0f/100", s2LcScore));
        tvS2CcScore.setText(s2CcScore >= 0 ? String.format("CodeChef: %.0f/100", s2CcScore) : "CodeChef: N/A");
        tvS2Total.setText(String.format("Total: %.0f", s2Total));

        fillLcDetails(lc2, errLc2, u2lc, tvS2LcEasy, tvS2LcMedium, tvS2LcHard, tvS2LcTotal, tvS2LcRank);
        fillCcDetails(cc2, errCc2, u2cc, tvS2CcRating, tvS2CcHighest, tvS2CcProblems, tvS2CcStars, tvS2CcRank);

        // Winner
        cardWinner.setVisibility(View.VISIBLE);
        if (s1Total > s2Total) {
            tvWinnerText.setText("\uD83C\uDFC6  " + u1lc + " wins!\n"
                    + String.format("%.0f vs %.0f points", s1Total, s2Total));
        } else if (s2Total > s1Total) {
            tvWinnerText.setText("\uD83C\uDFC6  " + u2lc + " wins!\n"
                    + String.format("%.0f vs %.0f points", s2Total, s1Total));
        } else {
            tvWinnerText.setText("\uD83E\uDD1D  It's a tie!  Both scored " + (int) s1Total + " pts");
        }

        showStep(STEP_RESULTS);
    }

    private void fillLcDetails(JSONObject d, String err, String inputUsername,
            TextView easy, TextView medium, TextView hard, TextView total, TextView rank) {
        if (d == null) {
            String msg = err != null ? "Error fetching data" : "\u2014";
            easy.setText("Easy: " + msg); medium.setText("Medium: " + msg);
            hard.setText("Hard: " + msg); total.setText("Total Solved: " + msg); rank.setText("Rank: " + msg);
            return;
        }
        easy.setText("Easy Solved: " + d.optInt("easySolved", 0));
        medium.setText("Medium Solved: " + d.optInt("mediumSolved", 0));
        hard.setText("Hard Solved: " + d.optInt("hardSolved", 0));
        total.setText("Total Solved: " + d.optInt("totalSolved", 0));
        int r = d.optInt("ranking", 0);
        rank.setText("Global Rank: " + (r > 0 ? "#" + r : "N/A"));
    }

    private void fillCcDetails(JSONObject d, String err, String inputUsername,
            TextView rating, TextView highest, TextView problems, TextView stars, TextView rank) {
        if (inputUsername.isEmpty()) {
            rating.setText("Rating: \u2014"); highest.setText("Highest: \u2014");
            problems.setText("Problems: \u2014"); stars.setText("Stars: \u2014"); rank.setText("Global Rank: \u2014");
            return;
        }
        if (d == null) {
            String msg = err != null ? "Error fetching data" : "\u2014";
            rating.setText("Rating: " + msg); highest.setText("Highest: " + msg);
            problems.setText("Problems: " + msg); stars.setText("Stars: " + msg); rank.setText("Rank: " + msg);
            return;
        }
        int r = d.optInt("currentRating", d.optInt("rating", d.optInt("current_rating", 0)));
        rating.setText("Current Rating: " + (r > 0 ? r : "N/A"));
        int h = d.optInt("highestRating", d.optInt("highest_rating", 0));
        highest.setText("Highest Rating: " + (h > 0 ? h : "N/A"));
        int p = d.optInt("totalProblemsSolved", d.optInt("problems_solved", d.optInt("problemsSolved", 0)));
        problems.setText("Problems Solved: " + (p > 0 ? p : "N/A"));

        int starsInt = d.optInt("stars", 0);
        if (starsInt == 0) {
            String s = d.optString("star", d.optString("stars_string", ""));
            starsInt = s.replaceAll("[^\u2605\u2B50*]", "").length();
            if (starsInt == 0) {
                try { starsInt = Integer.parseInt(s.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
            }
        }
        stars.setText("Stars: " + (starsInt > 0 ? starsInt + " \u2605" : "N/A"));

        int gr = d.optInt("globalRank", d.optInt("global_rank", d.optInt("rankNumber", 0)));
        rank.setText("Global Rank: " + (gr > 0 ? "#" + gr : "N/A"));
    }

    // ── SCORE INFO DIALOG ───────────────────────────────────────────────────

    private void showScoreInfoDialog() {
        String msg =
            "LeetCode Score (max 100)\n" +
            "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\n" +
            "\u2022 Easy Solved   \u00d7 1   \u2192 capped at 25 pts\n" +
            "\u2022 Medium Solved \u00d7 2   \u2192 capped at 40 pts\n" +
            "\u2022 Hard Solved   \u00d7 5   \u2192 capped at 30 pts\n" +
            "\u2022 Ranking < 100K      \u2192 bonus  5 pts\n\n" +
            "CodeChef Score (max 100)\n" +
            "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\n" +
            "\u2022 Rating \u00f7 50         \u2192 capped at 40 pts\n" +
            "\u2022 Problems Solved \u00d7 1 \u2192 capped at 30 pts\n" +
            "\u2022 Stars (1\u20137) \u00d7 4     \u2192 capped at 28 pts\n" +
            "\u2022 Global Rank < 10K   \u2192 bonus  2 pts\n\n" +
            "Overall = LeetCode + CodeChef";

        new AlertDialog.Builder(this, R.style.Theme_CodeConnect)
                .setTitle("\u2139\uFE0F  Score Formula")
                .setMessage(msg)
                .setPositiveButton("Got it", null)
                .show();
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private void setLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCompare.setEnabled(!show);
        btnCompare.setText(show ? "Fetching live data\u2026" : "\u2694  Compare Students");
        etLc1.setEnabled(!show); etCc1.setEnabled(!show);
        etLc2.setEnabled(!show); etCc2.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
