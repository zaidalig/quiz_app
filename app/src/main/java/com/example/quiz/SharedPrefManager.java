// app/src/main/java/com/example/quiz/SharedPrefManager.java
package com.example.quiz;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "quiz_app_prefs";
    private static final String HIGH_SCORES_KEY = "high_scores";

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
            return new java.util.ArrayList<>();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<HighScore>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
