package com.codeconnect.app;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * LeetCodeApiService
 *
 * Fetches LeetCode user solved-problem stats from the unofficial
 * alfa-leetcode-api REST service.
 *
 * Endpoint 1 (solved stats):
 *   GET https://alfa-leetcode-api.onrender.com/{username}/solved
 *   Response: { solvedProblem, easySolved, mediumSolved, hardSolved,
 *               totalEasy, totalMedium, totalHard }
 *
 * Endpoint 2 (profile / ranking):
 *   GET https://alfa-leetcode-api.onrender.com/{username}
 *   Response: { username, ranking, reputation, ... }
 */
public class LeetCodeApiService {

    private static final String BASE_URL = "https://alfa-leetcode-api.onrender.com/";

    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public LeetCodeApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        executor    = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    /** Holds parsed LeetCode stats for one user. */
    public static class UserStats {
        public String username;
        public int totalSolved;
        public int easySolved;
        public int mediumSolved;
        public int hardSolved;
        public int totalEasy;
        public int totalMedium;
        public int totalHard;
        public int ranking;

        public UserStats(String username) {
            this.username = username;
        }
    }

    public interface OnStatsLoadedListener {
        void onSuccess(UserStats stats);
        void onError(String errorMessage);
    }

    /**
     * Fetch solved-problem stats + ranking for a given LeetCode username.
     * Runs on a background thread; posts result to the main thread.
     */
    public void fetchUserStats(String username, OnStatsLoadedListener listener) {
        executor.execute(() -> {
            try {
                UserStats stats = new UserStats(username);

                // ── 1. Solved stats ──────────────────────────────────────────
                String solvedUrl = BASE_URL + username + "/solved";
                Request solvedReq = new Request.Builder().url(solvedUrl).get().build();
                try (Response resp = client.newCall(solvedReq).execute()) {
                    if (resp.isSuccessful() && resp.body() != null) {
                        JSONObject json = new JSONObject(resp.body().string());
                        if (!json.has("errors")) {
                            stats.totalSolved  = json.optInt("solvedProblem", 0);
                            stats.easySolved   = json.optInt("easySolved",    0);
                            stats.mediumSolved = json.optInt("mediumSolved",  0);
                            stats.hardSolved   = json.optInt("hardSolved",    0);
                            stats.totalEasy    = json.optInt("totalEasy",    800);
                            stats.totalMedium  = json.optInt("totalMedium", 1700);
                            stats.totalHard    = json.optInt("totalHard",    600);
                        }
                    }
                }

                // ── 2. Profile / ranking ─────────────────────────────────────
                String profileUrl = BASE_URL + username;
                Request profileReq = new Request.Builder().url(profileUrl).get().build();
                try (Response resp = client.newCall(profileReq).execute()) {
                    if (resp.isSuccessful() && resp.body() != null) {
                        JSONObject json = new JSONObject(resp.body().string());
                        if (!json.has("errors")) {
                            stats.ranking = json.optInt("ranking", 0);
                            if (!json.optString("username", "").isEmpty()) {
                                stats.username = json.optString("username", username);
                            }
                        }
                    }
                }

                // Validate
                if (stats.totalSolved == 0 && stats.ranking == 0) {
                    postError(listener, "User '" + username + "' not found on LeetCode.");
                    return;
                }

                final UserStats result = stats;
                mainHandler.post(() -> listener.onSuccess(result));

            } catch (IOException e) {
                postError(listener, "Network error: " + e.getMessage());
            } catch (Exception e) {
                postError(listener, "Error parsing LeetCode data for '" + username + "'.");
            }
        });
    }

    private void postError(OnStatsLoadedListener listener, String message) {
        mainHandler.post(() -> listener.onError(message));
    }

    public void shutdown() {
        executor.shutdown();
    }
}
