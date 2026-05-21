# CodeConnect - Android App

A coding community app with Firebase Google Authentication, platform links, and LeetCode student comparison.

## Features
- **Welcome Screen** — Animated splash with CodeConnect branding
- **Google Sign-In** — Firebase Authentication via Google account
- **Platform Cards** — Quick access to LinkedIn, GitHub, CodeChef, LeetCode
- **Student Comparison** — Compare two LeetCode profiles side-by-side

## Setup Instructions

### 1. Open in Android Studio
1. Open Android Studio
2. Select **File → Open** and navigate to this `CodeConnect` folder
3. Wait for Gradle sync to complete

### 2. Firebase Setup (REQUIRED)
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use existing)
3. Click **Add App → Android**
4. Enter package name: `com.codeconnect.app`
5. Add your **SHA-1 fingerprint**:
   - In Android Studio terminal, run: `./gradlew signingReport`
   - Copy the SHA-1 value from the debug variant
6. Download `google-services.json`
7. Place it in the `app/` directory
8. In Firebase Console → **Authentication → Sign-in method** → Enable **Google**

### 3. Build & Run
1. Connect an Android device or start an emulator (with Google Play Services)
2. Click **Run** (▶) in Android Studio
3. The app should launch with the Welcome screen

## Project Structure
```
app/src/main/
├── java/com/codeconnect/app/
│   ├── WelcomeActivity.java      # Splash screen
│   ├── SignInActivity.java        # Google Sign-In
│   ├── DashboardActivity.java     # Main dashboard
│   ├── CompareActivity.java       # LeetCode comparison
│   └── LeetCodeApiService.java    # API helper
├── res/
│   ├── layout/                    # XML layouts
│   ├── drawable/                  # Backgrounds, shapes
│   ├── anim/                      # Animations
│   └── values/                    # Colors, strings, themes
└── AndroidManifest.xml
```

## Tech Stack
- **Language:** Java
- **Auth:** Firebase Authentication (Google Sign-In)
- **HTTP:** OkHttp 4.x
- **Images:** Glide + CircleImageView
- **UI:** Material Design 3
- **LeetCode API:** [alfa-leetcode-api](https://github.com/alfaarghya/alfa-leetcode-api)

## Min Requirements
- Android 7.0 (API 24) or higher
- Google Play Services on device
