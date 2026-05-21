package com.codeconnect.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * SignInActivity - Handles Firebase Google Authentication.
 * Uses GoogleSignInClient for a simple sign-in flow.
 */
public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressSignIn;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToDashboard();
            return;
        }

        // Find views
        progressSignIn = findViewById(R.id.progressSignIn);
        tvError = findViewById(R.id.tvError);

        // Configure Google Sign-In
        // NOTE: default_web_client_id is auto-generated from google-services.json
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up sign-in button click
        findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> signIn());
    }

    /**
     * Launch Google Sign-In intent
     */
    private void signIn() {
        showLoading(true);
        tvError.setVisibility(View.GONE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Google sign-in successful: " + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign-In failed
                Log.w(TAG, "Google sign-in failed", e);
                showLoading(false);
                showError("Sign-in failed: " + e.getStatusCode());
            }
        }
    }

    /**
     * Authenticate with Firebase using the Google ID token
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase auth successful");
                        navigateToDashboard();
                    } else {
                        Log.w(TAG, "Firebase auth failed", task.getException());
                        showError("Authentication failed. Please try again.");
                    }
                });
    }

    /**
     * Navigate to DashboardActivity and finish this activity
     */
    private void navigateToDashboard() {
        Intent intent = new Intent(SignInActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showLoading(boolean show) {
        progressSignIn.setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnGoogleSignIn).setEnabled(!show);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
