package com.codeconnect.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * DashboardActivity - Main screen after sign-in.
 * Shows user profile, 4 platform cards, and compare button.
 */
public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In (needed for sign-out)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up user profile
        setupUserProfile();

        // Set up platform card clicks
        setupPlatformCards();

        // Set up Student Profile Compare button
        findViewById(R.id.cardCompare).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, StudentProfileCompareActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Set up Resume Builder
        findViewById(R.id.cardResumeBuilder).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ResumeBuilderActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Set up Skill Analytics
        findViewById(R.id.cardSkillAnalytics).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SkillAnalyticsActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // cardEnhancedCompare removed – replaced by the single "Compare Profiles" card

        // Set up sign-out button
        ImageButton btnSignOut = findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(v -> signOut());
    }

    /**
     * Display the signed-in user's profile info
     */
    private void setupUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            TextView tvUserName = findViewById(R.id.tvUserName);
            TextView tvUserEmail = findViewById(R.id.tvUserEmail);
            CircleImageView ivProfilePhoto = findViewById(R.id.ivProfilePhoto);

            tvUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");

            // Load profile photo using Glide
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(android.R.drawable.ic_menu_myplaces)
                        .into(ivProfilePhoto);
            }
        }
    }

    /**
     * Set up click listeners for the 4 platform cards.
     * Each opens the respective platform login page in the device browser.
     */
    private void setupPlatformCards() {
        findViewById(R.id.cardLinkedIn).setOnClickListener(v ->
                openUrl(getString(R.string.url_linkedin)));

        findViewById(R.id.cardGitHub).setOnClickListener(v ->
                openUrl(getString(R.string.url_github)));

        findViewById(R.id.cardCodeChef).setOnClickListener(v ->
                openUrl(getString(R.string.url_codechef)));

        findViewById(R.id.cardLeetCode).setOnClickListener(v ->
                openUrl(getString(R.string.url_leetcode)));
    }

    /**
     * Open a URL in the device's default browser
     */
    private void openUrl(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    /**
     * Sign out from Firebase and Google, then return to SignInActivity
     */
    private void signOut() {
        // Sign out from Firebase
        mAuth.signOut();

        // Sign out from Google
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent intent = new Intent(DashboardActivity.this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
