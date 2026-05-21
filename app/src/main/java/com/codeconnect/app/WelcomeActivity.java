package com.codeconnect.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * WelcomeActivity - Splash screen with animated branding.
 * Auto-navigates to SignInActivity after 2.5 seconds.
 */
public class WelcomeActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Find views
        TextView tvLogoIcon = findViewById(R.id.tvLogoIcon);
        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvTagline = findViewById(R.id.tvTagline);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);

        // Load animations
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Start animations
        tvLogoIcon.startAnimation(scaleUp);
        tvAppName.startAnimation(fadeIn);
        tvTagline.startAnimation(slideUp);
        tvSubtitle.startAnimation(slideUp);

        // Navigate to Sign-In after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(WelcomeActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
            // Transition animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DELAY);
    }
}
