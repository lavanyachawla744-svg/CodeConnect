package com.codeconnect.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * CompareProfilesActivity
 *
 * Step 1 – User selects Platform B (LeetCode or CodeChef).
 *           GitHub is always Platform A (mandatory).
 *
 * Step 2 – User enters GitHub username + Platform B username.
 *
 * Step 3 – Real API calls fetch ACTUAL live data:
 *   • GitHub:   api.github.com  (official REST API v3)
 *   • LeetCode: alfa-leetcode-api.onrender.com  (public unofficial REST)
 *   • CodeChef: codechef-api.vercel.app  (public unofficial REST)
 */
public class CompareProfilesActivity extends AppCompatActivity {

    private static final int STEP_SELECT  = 0;
    private static final int STEP_INPUT   = 1;
    private static final int STEP_RESULTS = 2;

    private int currentStep = STEP_SELECT;
    private String platformB = null; // "leetcode" or "codechef"

    // ── Step 0 ────────────────────────────────────────────────────────────────
    private LinearLayout stepSelectLayout;
    private MaterialCardView cardLeetCode, cardCodeChef;

    // ── Step 1 ────────────────────────────────────────────────────────────────
    private LinearLayout stepInputLayout;
    private EditText etGithubUsername, etPlatformBUsername;
    private TextView tvPlatformBLabel, tvPlatformBHint;
    private MaterialButton btnFetch;
    private ProgressBar progressFetch;
    private TextView tvFetchError;

    // ── Step 2 ────────────────────────────────────────────────────────────────
    private ScrollView stepResultsLayout;
    private TextView tvResultsTitle;

    private TextView tvGhUsername, tvGhRepos, tvGhFollowers, tvGhFollowing;
    private TextView tvGhStars, tvGhForks, tvGhBio, tvGhScore;

    private TextView tvPbUsername, tvPbScore;
    private TextView tvPbStat1Label, tvPbStat1Val;
    private TextView tvPbStat2Label, tvPbStat2Val;
    private TextView tvPbStat3Label, tvPbStat3Val;
    private TextView tvPbStat4Label, tvPbStat4Val;
    private TextView tvPbStat5Label, tvPbStat5Val;

    private MaterialCardView cardWinner;
    private TextView tvWinnerText;
    private MaterialButton btnInfoScore, btnCompareAnother;

    // ── Network ───────────────────────────────────────────────────────────────
    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ── State ─────────────────────────────────────────────────────────────────
    private JSONObject githubData;
    private JSONObject platformBData;
    private String githubError    = null;
    private String platformBError = null;
    private final AtomicInteger doneCount = new AtomicInteger(0);
    private double ghScore, pbScore;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_profiles);
        initViews();
        showStep(STEP_SELECT);
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        stepSelectLayout = findViewById(R.id.stepSelectLayout);
        cardLeetCode     = findViewById(R.id.cardSelectLeetCode);
        cardCodeChef     = findViewById(R.id.cardSelectCodeChef);

        cardLeetCode.setOnClickListener(v -> {
            platformB = "leetcode";
            highlightSelection(cardLeetCode, cardCodeChef);
            mainHandler.postDelayed(() -> showStep(STEP_INPUT), 250);
        });
        cardCodeChef.setOnClickListener(v -> {
            platformB = "codechef";
            highlightSelection(cardCodeChef, cardLeetCode);
            mainHandler.postDelayed(() -> showStep(STEP_INPUT), 250);
        });

        stepInputLayout     = findViewById(R.id.stepInputLayout);
        etGithubUsername    = findViewById(R.id.etGithubUsername);
        etPlatformBUsername = findViewById(R.id.etPlatformBUsername);
        tvPlatformBLabel    = findViewById(R.id.tvPlatformBLabel);
        tvPlatformBHint     = findViewById(R.id.tvPlatformBHint);
        btnFetch            = findViewById(R.id.btnFetch);
        progressFetch       = findViewById(R.id.progressFetch);
        tvFetchError        = findViewById(R.id.tvFetchError);
        btnFetch.setOnClickListener(v -> startFetch());

        stepResultsLayout = findViewById(R.id.stepResultsLayout);
        tvResultsTitle    = findViewById(R.id.tvResultsTitle);

        tvGhUsername  = findViewById(R.id.tvGhUsername);
        tvGhRepos     = findViewById(R.id.tvGhRepos);
        tvGhFollowers = findViewById(R.id.tvGhFollowers);
        tvGhFollowing = findViewById(R.id.tvGhFollowing);
        tvGhStars     = findViewById(R.id.tvGhStars);
        tvGhForks     = findViewById(R.id.tvGhForks);
        tvGhBio       = findViewById(R.id.tvGhBio);
        tvGhScore     = findViewById(R.id.tvGhScore);

        tvPbUsername   = findViewById(R.id.tvPbUsername);
        tvPbScore      = findViewById(R.id.tvPbScore);
        tvPbStat1Label = findViewById(R.id.tvPbStat1Label);
        tvPbStat1Val   = findViewById(R.id.tvPbStat1Val);
        tvPbStat2Label = findViewById(R.id.tvPbStat2Label);
        tvPbStat2Val   = findViewById(R.id.tvPbStat2Val);
        tvPbStat3Label = findViewById(R.id.tvPbStat3Label);
        tvPbStat3Val   = findViewById(R.id.tvPbStat3Val);
        tvPbStat4Label = findViewById(R.id.tvPbStat4Label);
        tvPbStat4Val   = findViewById(R.id.tvPbStat4Val);
        tvPbStat5Label = findViewById(R.id.tvPbStat5Label);
        tvPbStat5Val   = findViewById(R.id.tvPbStat5Val);

        cardWinner        = findViewById(R.id.cardWinner);
        tvWinnerText      = findViewById(R.id.tvWinnerText);
        btnInfoScore      = findViewById(R.id.btnInfoScore);
        btnCompareAnother = findViewById(R.id.btnCompareAnother);

        btnInfoScore.setOnClickListener(v -> showScoreInfoDialog());
        btnCompareAnother.setOnClickListener(v -> resetToStart());
    }

    private void showStep(int step) {
        currentStep = step;
        stepSelectLayout.setVisibility(step == STEP_SELECT  ? View.VISIBLE : View.GONE);
        stepInputLayout.setVisibility( step == STEP_INPUT   ? View.VISIBLE : View.GONE);
        stepResultsLayout.setVisibility(step == STEP_RESULTS ? View.VISIBLE : View.GONE);
        if (step == STEP_INPUT) {
            String name = "leetcode".equals(platformB) ? "LeetCode" : "CodeChef";
            tvPlatformBLabel.setText(name + " Username");
            tvPlatformBHint.setText("Enter " + name + " username");
        }
    }

    private void resetToStart() {
        platformB      = null;
        githubData     = null;
        platformBData  = null;
        githubError    = null;
        platformBError = null;
        doneCount.set(0);
        etGithubUsername.setText("");
        etPlatformBUsername.setText("");
        tvFetchError.setVisibility(View.GONE);
        showStep(STEP_SELECT);
    }

    private void highlightSelection(MaterialCardView selected, MaterialCardView other) {
        selected.setStrokeWidth(4);
        other.setStrokeWidth(1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FETCH ORCHESTRATION
    // ─────────────────────────────────────────────────────────────────────────

    private void startFetch() {
        String ghUser = etGithubUsername.getText().toString().trim();
        String pbUser = etPlatformBUsername.getText().toString().trim();

        if (ghUser.isEmpty()) {
            tvFetchError.setText("Please enter a GitHub username.");
            tvFetchError.setVisibility(View.VISIBLE);
            return;
        }
        if (pbUser.isEmpty()) {
            String name = "leetcode".equals(platformB) ? "LeetCode" : "CodeChef";
            tvFetchError.setText("Please enter a " + name + " username.");
            tvFetchError.setVisibility(View.VISIBLE);
            return;
        }

        tvFetchError.setVisibility(View.GONE);
        showLoading(true);
        doneCount.set(0);
        githubData     = null;
        platformBData  = null;
        githubError    = null;
        platformBError = null;

        executor.execute(() -> fetchGitHub(ghUser));
        if ("leetcode".equals(platformB)) {
            executor.execute(() -> fetchLeetCode(pbUser));
        } else {
            executor.execute(() -> fetchCodeChef(pbUser));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GITHUB  — api.github.com  (official REST API v3)
    // GET /users/{username}          → profile
    // GET /users/{username}/repos    → for stars + forks aggregation
    // ─────────────────────────────────────────────────────────────────────────

    private void fetchGitHub(String username) {
        try {
            Request req = new Request.Builder()
                    .url("https://api.github.com/users/" + username)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "CodeConnect-Android")
                    .build();

            try (Response resp = http.newCall(req).execute()) {
                if (resp.code() == 404) {
                    githubError = "GitHub: user '" + username + "' not found. Check the username.";
                    checkBothLoaded();
                    return;
                }
                if (resp.code() == 403) {
                    githubError = "GitHub API rate limit exceeded. Try again in a few minutes.";
                    checkBothLoaded();
                    return;
                }
                if (!resp.isSuccessful() || resp.body() == null) {
                    githubError = "GitHub: request failed (HTTP " + resp.code() + "). Try again.";
                    checkBothLoaded();
                    return;
                }
                JSONObject user = new JSONObject(resp.body().string());
                if ("Not Found".equalsIgnoreCase(user.optString("message", ""))) {
                    githubError = "GitHub: user '" + username + "' not found.";
                    checkBothLoaded();
                    return;
                }

                // Aggregate stars & forks from first 100 repos
                Request repoReq = new Request.Builder()
                        .url("https://api.github.com/users/" + username + "/repos?per_page=100&sort=updated")
                        .header("Accept", "application/vnd.github+json")
                        .header("X-GitHub-Api-Version", "2022-11-28")
                        .header("User-Agent", "CodeConnect-Android")
                        .build();

                int totalStars = 0, totalForks = 0;
                try (Response repoResp = http.newCall(repoReq).execute()) {
                    if (repoResp.isSuccessful() && repoResp.body() != null) {
                        JSONArray repos = new JSONArray(repoResp.body().string());
                        for (int i = 0; i < repos.length(); i++) {
                            JSONObject r = repos.getJSONObject(i);
                            totalStars += r.optInt("stargazers_count", 0);
                            totalForks += r.optInt("forks_count", 0);
                        }
                    }
                } catch (Exception ignored) { /* repo fetch is best-effort */ }
                user.put("total_stars", totalStars);
                user.put("total_forks", totalForks);
                githubData = user;
            }
        } catch (Exception e) {
            githubError = "GitHub: Network error. Check your internet connection.";
        }
        checkBothLoaded();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LEETCODE  — alfa-leetcode-api.onrender.com  (public unofficial REST)
    //
    // GET /{username}/solved
    //   → { solvedProblem, easySolved, mediumSolved, hardSolved }
    //
    // GET /{username}
    //   → { username, ranking, reputation }
    //
    // Both calls are made; results merged into one JSONObject.
    // ─────────────────────────────────────────────────────────────────────────

    private void fetchLeetCode(String username) {
        try {
            JSONObject merged = new JSONObject();
            merged.put("username", username);

            // 1. Solved stats
            String solvedUrl = "https://alfa-leetcode-api.onrender.com/" + username + "/solved";
            Request solvedReq = new Request.Builder().url(solvedUrl).build();
            try (Response r = http.newCall(solvedReq).execute()) {
                if (r.isSuccessful() && r.body() != null) {
                    JSONObject j = new JSONObject(r.body().string());
                    if (!j.has("errors")) {
                        merged.put("totalSolved",  j.optInt("solvedProblem", 0));
                        merged.put("easySolved",   j.optInt("easySolved",    0));
                        merged.put("mediumSolved", j.optInt("mediumSolved",  0));
                        merged.put("hardSolved",   j.optInt("hardSolved",    0));
                    }
                }
            }

            // 2. Profile (ranking)
            String profileUrl = "https://alfa-leetcode-api.onrender.com/" + username;
            Request profileReq = new Request.Builder().url(profileUrl).build();
            try (Response r = http.newCall(profileReq).execute()) {
                if (r.isSuccessful() && r.body() != null) {
                    JSONObject j = new JSONObject(r.body().string());
                    if (!j.has("errors")) {
                        if (!j.optString("username", "").isEmpty()) {
                            merged.put("username", j.optString("username", username));
                        }
                        merged.put("ranking",    j.optInt("ranking",    0));
                        merged.put("reputation", j.optInt("reputation", 0));
                        // Fallback if /solved failed
                        if (merged.optInt("totalSolved", 0) == 0) {
                            merged.put("totalSolved",  j.optInt("totalSolved",  0));
                            merged.put("easySolved",   j.optInt("easySolved",   0));
                            merged.put("mediumSolved", j.optInt("mediumSolved", 0));
                            merged.put("hardSolved",   j.optInt("hardSolved",   0));
                        }
                    }
                }
            }

            // Validate: at least something came back
            if (merged.optInt("totalSolved", 0) == 0 && merged.optInt("ranking", 0) == 0) {
                platformBError = "LeetCode: user '" + username + "' not found or has no data.";
                checkBothLoaded();
                return;
            }

            platformBData = merged;
        } catch (Exception e) {
            platformBError = "LeetCode fetch error: " + e.getMessage();
        }
        checkBothLoaded();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CODECHEF  — codechef-api.vercel.app  (public unofficial REST)
    //
    // GET /handle/{username}
    //   → { currentRating, highestRating, globalRank, countryRank,
    //       stars, totalProblemsSolved, username }
    //
    // Fallback: /user/{username}  (alternate path on same API)
    // ─────────────────────────────────────────────────────────────────────────

    private void fetchCodeChef(String username) {
        try {
            JSONObject data = null;

            // Try multiple CodeChef API endpoints (community APIs can go down)
            String[] apiUrls = {
                "https://codechef-api.vercel.app/handle/" + username,
                "https://codechef-api.vercel.app/user/" + username,
            };

            for (String apiUrl : apiUrls) {
                if (data != null) break;
                try {
                    Request req = new Request.Builder()
                            .url(apiUrl)
                            .header("Accept", "application/json")
                            .header("User-Agent", "CodeConnect-Android")
                            .build();
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
                } catch (Exception ignored) { /* try next endpoint */ }
            }

            // Fallback: scrape CodeChef profile page directly
            if (data == null) {
                try {
                    String profileUrl = "https://www.codechef.com/users/" + username;
                    Request req = new Request.Builder()
                            .url(profileUrl)
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                            .build();
                    try (Response r = http.newCall(req).execute()) {
                        if (r.isSuccessful() && r.body() != null) {
                            String html = r.body().string();
                            if (!html.contains("Page Not Found") && html.contains("rating")) {
                                data = parseCodeChefHtml(html, username);
                            }
                        }
                    }
                } catch (Exception ignored) { /* scraping is best-effort */ }
            }

            if (data == null) {
                platformBError = "CodeChef: Could not fetch data for '" + username + "'. Service may be unavailable.";
                checkBothLoaded();
                return;
            }

            if (!data.has("username") || data.optString("username", "").isEmpty()) {
                data.put("username", username);
            }
            platformBData = data;

        } catch (Exception e) {
            platformBError = "CodeChef: Service temporarily unavailable.";
        }
        checkBothLoaded();
    }

    /**
     * Parse CodeChef profile HTML to extract rating, stars, and problems solved.
     * Fallback when all API endpoints are down.
     */
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
                for (char c : sub.toCharArray()) {
                    if (c == '\u2605' || c == '\u2B50') starCount++;
                }
            }
            if (starCount > 0) data.put("stars", starCount);

            int gRank = extractNumber(html, "Global Rank");
            if (gRank > 0) data.put("globalRank", gRank);
            int problems = extractNumber(html, "Problems Solved");
            if (problems > 0) data.put("totalProblemsSolved", problems);

            if (data.length() <= 1) return null;
        } catch (Exception e) {
            return null;
        }
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

    // ─────────────────────────────────────────────────────────────────────────
    // SYNCHRONISE
    // ─────────────────────────────────────────────────────────────────────────

    private synchronized void checkBothLoaded() {
        if (doneCount.incrementAndGet() == 2) {
            mainHandler.post(() -> {
                showLoading(false);
                if (githubError != null) {
                    tvFetchError.setText(githubError);
                    tvFetchError.setVisibility(View.VISIBLE);
                    return;
                }
                if (platformBError != null) {
                    tvFetchError.setText(platformBError);
                    tvFetchError.setVisibility(View.VISIBLE);
                    return;
                }
                if (githubData != null && platformBData != null) {
                    computeScoresAndDisplay();
                } else {
                    tvFetchError.setText("Could not retrieve data. Check usernames and try again.");
                    tvFetchError.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SCORING
    // ─────────────────────────────────────────────────────────────────────────

    /** GitHub Score (max 100):
     *  repos×2 (cap 40) + stars×1 (cap 25) + forks×2 (cap 15) + followers×1 (cap 20) */
    private double computeGitHubScore(JSONObject d) {
        double sc = Math.min(d.optInt("public_repos", 0) * 2, 40)
                  + Math.min(d.optInt("total_stars",  0),     25)
                  + Math.min(d.optInt("total_forks",  0) * 2, 15)
                  + Math.min(d.optInt("followers",    0),     20);
        return Math.min(sc, 100);
    }

    /** LeetCode Score (max 100):
     *  easy×1 (cap 25) + medium×2 (cap 40) + hard×5 (cap 30) + rank<100K bonus 5 */
    private double computeLeetCodeScore(JSONObject d) {
        int rank = d.optInt("ranking", Integer.MAX_VALUE);
        double sc = Math.min(d.optInt("easySolved",   0),     25)
                  + Math.min(d.optInt("mediumSolved", 0) * 2, 40)
                  + Math.min(d.optInt("hardSolved",   0) * 5, 30)
                  + (rank > 0 && rank < 100_000 ? 5 : 0);
        return Math.min(sc, 100);
    }

    /** CodeChef Score (max 100):
     *  rating/50 (cap 40) + problems×1 (cap 30) + stars×4 (cap 28) + globalRank<10K bonus 2 */
    private double computeCodeChefScore(JSONObject d) {
        int rating   = d.optInt("currentRating", d.optInt("rating", d.optInt("current_rating", 0)));
        int problems = d.optInt("totalProblemsSolved",
                       d.optInt("problems_solved", d.optInt("problemsSolved", 0)));

        // Stars can be int or "★★★" or "3★"
        int starsInt = d.optInt("stars", 0);
        if (starsInt == 0) {
            String s = d.optString("star", d.optString("stars_string", ""));
            starsInt = s.replaceAll("[^★⭐*]", "").length();
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

    // ─────────────────────────────────────────────────────────────────────────
    // DISPLAY
    // ─────────────────────────────────────────────────────────────────────────

    private void computeScoresAndDisplay() {
        ghScore = computeGitHubScore(githubData);
        pbScore = "leetcode".equals(platformB)
                ? computeLeetCodeScore(platformBData)
                : computeCodeChefScore(platformBData);

        // GitHub card
        tvGhUsername.setText("@" + githubData.optString("login", "—"));
        String bio = githubData.optString("bio", "");
        tvGhBio.setText(bio.isEmpty() ? "No bio" : bio);
        tvGhRepos.setText(    String.valueOf(githubData.optInt("public_repos", 0)));
        tvGhFollowers.setText(String.valueOf(githubData.optInt("followers",    0)));
        tvGhFollowing.setText(String.valueOf(githubData.optInt("following",    0)));
        tvGhStars.setText(    String.valueOf(githubData.optInt("total_stars",  0)));
        tvGhForks.setText(    String.valueOf(githubData.optInt("total_forks",  0)));
        tvGhScore.setText(String.format("Score: %.0f / 100", ghScore));

        // Platform B card
        String pbName = "leetcode".equals(platformB) ? "LeetCode" : "CodeChef";
        tvResultsTitle.setText("GitHub  vs  " + pbName);
        if ("leetcode".equals(platformB)) populateLeetCodeCard();
        else                              populateCodeChefCard();
        tvPbScore.setText(String.format("Score: %.0f / 100", pbScore));

        // Winner
        String ghUser = etGithubUsername.getText().toString().trim();
        String pbUser = etPlatformBUsername.getText().toString().trim();
        cardWinner.setVisibility(View.VISIBLE);
        if (ghScore > pbScore) {
            tvWinnerText.setText("🏆  " + ghUser + " (GitHub) wins!\nScore: "
                    + (int) ghScore + " vs " + (int) pbScore);
        } else if (pbScore > ghScore) {
            tvWinnerText.setText("🏆  " + pbUser + " (" + pbName + ") wins!\nScore: "
                    + (int) pbScore + " vs " + (int) ghScore);
        } else {
            tvWinnerText.setText("🤝  It's a tie!  Both scored " + (int) ghScore);
        }

        showStep(STEP_RESULTS);
    }

    private void populateLeetCodeCard() {
        JSONObject d = platformBData;
        tvPbUsername.setText("@" + d.optString("username", etPlatformBUsername.getText().toString().trim()));

        tvPbStat1Label.setText("Total Solved");
        tvPbStat1Val.setText(String.valueOf(d.optInt("totalSolved", 0)));

        tvPbStat2Label.setText("Easy Solved");
        tvPbStat2Val.setText(String.valueOf(d.optInt("easySolved", 0)));

        tvPbStat3Label.setText("Medium Solved");
        tvPbStat3Val.setText(String.valueOf(d.optInt("mediumSolved", 0)));

        tvPbStat4Label.setText("Hard Solved");
        tvPbStat4Val.setText(String.valueOf(d.optInt("hardSolved", 0)));

        tvPbStat5Label.setText("Global Ranking");
        int rank = d.optInt("ranking", 0);
        tvPbStat5Val.setText(rank > 0 ? "#" + rank : "N/A");
    }

    private void populateCodeChefCard() {
        JSONObject d = platformBData;
        tvPbUsername.setText("@" + d.optString("username", etPlatformBUsername.getText().toString().trim()));

        int rating = d.optInt("currentRating", d.optInt("rating", d.optInt("current_rating", 0)));
        tvPbStat1Label.setText("Current Rating");
        tvPbStat1Val.setText(rating > 0 ? String.valueOf(rating) : "N/A");

        int highestRating = d.optInt("highestRating", d.optInt("highest_rating", 0));
        tvPbStat2Label.setText("Highest Rating");
        tvPbStat2Val.setText(highestRating > 0 ? String.valueOf(highestRating) : "N/A");

        int problems = d.optInt("totalProblemsSolved",
                       d.optInt("problems_solved", d.optInt("problemsSolved", 0)));
        tvPbStat3Label.setText("Problems Solved");
        tvPbStat3Val.setText(problems > 0 ? String.valueOf(problems) : "N/A");

        int gr = d.optInt("globalRank", d.optInt("global_rank", d.optInt("rankNumber", 0)));
        tvPbStat4Label.setText("Global Rank");
        tvPbStat4Val.setText(gr > 0 ? "#" + gr : "N/A");

        tvPbStat5Label.setText("Stars");
        int starsInt = d.optInt("stars", 0);
        if (starsInt > 0) {
            tvPbStat5Val.setText(starsInt + " ★");
        } else {
            String s = d.optString("star", d.optString("stars_string", ""));
            tvPbStat5Val.setText(s.isEmpty() ? "N/A" : s);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SCORE INFO DIALOG
    // ─────────────────────────────────────────────────────────────────────────

    private void showScoreInfoDialog() {
        String ghFormula =
                "GitHub Score (max 100)\n" +
                "────────────────────────\n" +
                "• Public Repos  × 2   → capped at 40 pts\n" +
                "• Total Stars   × 1   → capped at 25 pts\n" +
                "• Total Forks   × 2   → capped at 15 pts\n" +
                "• Followers     × 1   → capped at 20 pts";

        String pbFormula;
        if ("leetcode".equals(platformB)) {
            pbFormula =
                "LeetCode Score (max 100)\n" +
                "────────────────────────\n" +
                "• Easy Solved   × 1   → capped at 25 pts\n" +
                "• Medium Solved × 2   → capped at 40 pts\n" +
                "• Hard Solved   × 5   → capped at 30 pts\n" +
                "• Ranking < 100K      → bonus  5 pts";
        } else {
            pbFormula =
                "CodeChef Score (max 100)\n" +
                "────────────────────────\n" +
                "• Rating ÷ 50         → capped at 40 pts\n" +
                "• Problems Solved × 1 → capped at 30 pts\n" +
                "• Stars (1–7) × 4     → capped at 28 pts\n" +
                "• Global Rank < 10K   → bonus  2 pts";
        }

        new AlertDialog.Builder(this, R.style.Theme_CodeConnect)
                .setTitle("ℹ️  Score Calculation")
                .setMessage(ghFormula + "\n\n" + pbFormula)
                .setPositiveButton("Got it", null)
                .show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void showLoading(boolean show) {
        progressFetch.setVisibility(show ? View.VISIBLE : View.GONE);
        btnFetch.setEnabled(!show);
        btnFetch.setText(show ? "Fetching…" : "Compare Profiles");
        etGithubUsername.setEnabled(!show);
        etPlatformBUsername.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
