// app/src/main/java/com/example/quiz/SharedPrefManager.java
package com.example.quiz;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "quiz_app_prefs";
    private static final String HIGH_SCORES_KEY = "high_scores";
    private static final String BADGES_KEY = "badges";
    private static final String USER_NAME_KEY = "user_name";
    private static final String TOTAL_QUIZZES_KEY = "total_quizzes";
    private static final String TOTAL_POINTS_KEY = "total_points";
    private static final String TOTAL_SCORE_KEY = "total_score";

    private static SharedPrefManager mInstance;
    private Context mCtx;

    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    // Save High Scores
    public void saveHighScores(List<HighScore> highScores) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(highScores);
        editor.putString(HIGH_SCORES_KEY, json);
        editor.apply();
    }

    // Get High Scores
    public List<HighScore> getHighScores() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(HIGH_SCORES_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<HighScore>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Save Badges
    public void saveBadges(List<Badge> badges) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(badges);
        editor.putString(BADGES_KEY, json);
        editor.apply();
    }

    // Get Badges
    public List<Badge> getBadges() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(BADGES_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Badge>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Save User Name
    public void saveUserName(String userName) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_NAME_KEY, userName);
        editor.apply();
    }

    // Get User Name
    public String getUserName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_NAME_KEY, "Guest");
    }

    // Increment Total Quizzes
    public void incrementTotalQuizzes() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        int total = sharedPreferences.getInt(TOTAL_QUIZZES_KEY, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(TOTAL_QUIZZES_KEY, total + 1);
        editor.apply();
    }

    // Get Total Quizzes
    public int getTotalQuizzes() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(TOTAL_QUIZZES_KEY, 0);
    }

    // Add Points
    public void addPoints(int points) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        int totalPoints = sharedPreferences.getInt(TOTAL_POINTS_KEY, 0);
        int totalScore = sharedPreferences.getInt(TOTAL_SCORE_KEY, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(TOTAL_POINTS_KEY, totalPoints + points);
        editor.putInt(TOTAL_SCORE_KEY, totalScore + points); // Assuming points are equivalent to score
        editor.apply();
    }

    // Get Total Points
    public int getTotalPoints() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(TOTAL_POINTS_KEY, 0);
    }

    // Get Average Score
    public float getAverageScore() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        int totalQuizzes = sharedPreferences.getInt(TOTAL_QUIZZES_KEY, 0);
        int totalScore = sharedPreferences.getInt(TOTAL_SCORE_KEY, 0);
        if (totalQuizzes == 0) return 0;
        return (float) totalScore / totalQuizzes;
    }
}
