package com.example.quiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private Button startQuizBtn, restartQuizBtn, viewResultsBtn;
    private Button option1, option2, option3, option4;
    private TextView questionNumber, questionText, scoreText, finalScore, errorMessage;
    private ProgressBar progressBar;
    private LinearLayout startScreen, questionScreen, endScreen;
    private TextView previousResultsText;

    // Quiz Data
    private JSONArray questionsArray;
    private int currentQuestionIndex = 0;
    private int score = 0;

    // Settings
    private int totalQuestions = 10;
    private String questionType = "multiple"; // "multiple" for MCQ, "boolean" for True/False

    // SharedPreferences for storing quiz results
    private SharedPreferences sharedPreferences;
    private final String PREFS_NAME = "QuizResults";
    private final String RESULTS_KEY = "Results";

    // Constants
    private final String QUIZ_API_URL_BASE = "https://opentdb.com/api.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI Components
        startScreen = findViewById(R.id.startScreen);
        questionScreen = findViewById(R.id.questionScreen);
        endScreen = findViewById(R.id.endScreen);

        startQuizBtn = findViewById(R.id.startQuizBtn);
        restartQuizBtn = findViewById(R.id.restartQuizBtn);
        viewResultsBtn = findViewById(R.id.viewResultsBtn);

        questionNumber = findViewById(R.id.questionNumber);
        questionText = findViewById(R.id.questionText);
        scoreText = findViewById(R.id.scoreText);
        finalScore = findViewById(R.id.finalScore);
        errorMessage = findViewById(R.id.errorMessage);
        progressBar = findViewById(R.id.progressBar);

        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        previousResultsText = findViewById(R.id.previousResultsText);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load and display previous results
        loadPreviousResults();

        // Set onClick Listener for Start Quiz Button
        startQuizBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numQuestionsStr = ((TextView) findViewById(R.id.numQuestionsInput)).getText().toString().trim();
                if(numQuestionsStr.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter the number of questions.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int num = Integer.parseInt(numQuestionsStr);
                    if(num <= 0){
                        Toast.makeText(MainActivity.this, "Please enter a positive number.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    totalQuestions = num;
                } catch (NumberFormatException e){
                    Toast.makeText(MainActivity.this, "Invalid number format.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get selected question type
                RadioGroup questionTypeGroup = findViewById(R.id.questionTypeGroup);
                int selectedId = questionTypeGroup.getCheckedRadioButtonId();
                if(selectedId == R.id.mcqRadioBtn){
                    questionType = "multiple";
                } else {
                    questionType = "boolean";
                }

                // Start the quiz
                startScreen.setVisibility(View.GONE);
                questionScreen.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                // Reset score and index
                currentQuestionIndex = 0;
                score = 0;
                scoreText.setText("Score: 0");
                // Fetch Quiz Questions
                new FetchQuizTask().execute(buildQuizApiUrl());
            }
        });

        // Set onClick Listener for Restart Quiz Button
        restartQuizBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetQuiz();
            }
        });

        // Set onClick Listener for View Results Button
        viewResultsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayFinalScore();
            }
        });

        // Set onClick Listeners for Answer Options
        View.OnClickListener answerClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button selectedOption = (Button) view;
                String selectedAnswer = selectedOption.getText().toString();
                checkAnswer(selectedAnswer);
            }
        };

        option1.setOnClickListener(answerClickListener);
        option2.setOnClickListener(answerClickListener);
        option3.setOnClickListener(answerClickListener);
        option4.setOnClickListener(answerClickListener);
    }

    // Build the API URL based on settings
    private String buildQuizApiUrl(){
        return QUIZ_API_URL_BASE + "?amount=" + totalQuestions + "&type=" + questionType;
    }

    // AsyncTask to fetch quiz questions from API
    private class FetchQuizTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String apiUrl = params[0];
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;

                    while ((inputLine = in.readLine()) != null){
                        result.append(inputLine);
                    }
                    in.close();
                } else {
                    return "Failed to fetch data. Response Code: " + responseCode;
                }

            } catch (IOException e){
                e.printStackTrace();
                return "An error occurred: " + e.getMessage();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.GONE);
            if(s.startsWith("An error occurred") || s.startsWith("Failed to fetch")){
                // Show error message
                errorMessage.setText(s);
                errorMessage.setVisibility(View.VISIBLE);
                questionScreen.setVisibility(View.GONE);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    int responseCode = jsonObject.getInt("response_code");
                    if(responseCode == 0){
                        questionsArray = jsonObject.getJSONArray("results");
                        displayQuestion();
                    } else {
                        errorMessage.setText("No questions available.");
                        errorMessage.setVisibility(View.VISIBLE);
                        questionScreen.setVisibility(View.GONE);
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                    errorMessage.setText("Error parsing data.");
                    errorMessage.setVisibility(View.VISIBLE);
                    questionScreen.setVisibility(View.GONE);
                }
            }
        }
    }

    // Display Current Question
    private void displayQuestion(){
        if(currentQuestionIndex < totalQuestions){
            try {
                JSONObject currentQuestion = questionsArray.getJSONObject(currentQuestionIndex);
                String question = currentQuestion.getString("question");
                String correctAnswer = currentQuestion.getString("correct_answer");
                JSONArray incorrectAnswers = currentQuestion.getJSONArray("incorrect_answers");

                // Combine correct and incorrect answers
                String[] allAnswers = new String[incorrectAnswers.length() + 1];
                for(int i=0; i < incorrectAnswers.length(); i++){
                    allAnswers[i] = Html.fromHtml(incorrectAnswers.getString(i)).toString();
                }
                allAnswers[incorrectAnswers.length()] = Html.fromHtml(correctAnswer).toString();

                // Shuffle the answers
                shuffleArray(allAnswers);

                // Update UI
                questionNumber.setText("Question " + (currentQuestionIndex + 1) + "/" + totalQuestions);
                questionText.setText(question);

                option1.setText(allAnswers[0]);
                option2.setText(allAnswers[1]);
                option3.setText(allAnswers[2]);
                option4.setText(allAnswers[3]);

            } catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(this, "Error displaying question.", Toast.LENGTH_SHORT).show();
            }
        } else {
            endQuiz();
        }
    }

    // Check if the selected answer is correct
    private void checkAnswer(String selectedAnswer){
        try {
            JSONObject currentQuestion = questionsArray.getJSONObject(currentQuestionIndex);
            String correctAnswer = Html.fromHtml(currentQuestion.getString("correct_answer")).toString();

            if(selectedAnswer.equals(correctAnswer)){
                score++;
                Toast.makeText(this, "✅ Correct!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Wrong! Correct Answer: " + correctAnswer, Toast.LENGTH_LONG).show();
            }

            scoreText.setText("Score: " + score);
            currentQuestionIndex++;
            displayQuestion();

        } catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(this, "Error checking answer.", Toast.LENGTH_SHORT).show();
        }
    }

    // Shuffle the answers array
    private void shuffleArray(String[] array){
        for(int i=array.length -1; i >0; i--){
            int index = (int)(Math.random() * (i +1));
            // Simple swap
            String temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    // End the quiz and show final score
    private void endQuiz(){
        questionScreen.setVisibility(View.GONE);
        endScreen.setVisibility(View.VISIBLE);
        finalScore.setText("Your Score: " + score + "/" + totalQuestions);
        // Save the result
        saveQuizResult(score, totalQuestions);
    }

    // Reset the quiz to start over
    private void resetQuiz(){
        endScreen.setVisibility(View.GONE);
        questionScreen.setVisibility(View.VISIBLE);
        currentQuestionIndex = 0;
        score = 0;
        scoreText.setText("Score: 0");
        progressBar.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.GONE);
        // Fetch new set of questions
        new FetchQuizTask().execute(buildQuizApiUrl());
    }

    // Save quiz result to SharedPreferences
    private void saveQuizResult(int obtainedScore, int totalQuestions){
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String newResult = "Score: " + obtainedScore + "/" + totalQuestions + " | " + timestamp;

        // Get existing results
        String existingResults = sharedPreferences.getString(RESULTS_KEY, "");
        if(existingResults.isEmpty()){
            existingResults = newResult;
        } else {
            existingResults += "\n" + newResult;
        }

        // Save back to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(RESULTS_KEY, existingResults);
        editor.apply();

        // Update previous results display
        loadPreviousResults();
    }

    // Load and display previous results
    private void loadPreviousResults(){
        String results = sharedPreferences.getString(RESULTS_KEY, "");
        if(results.isEmpty()){
            previousResultsText.setText("No previous results.");
        } else {
            previousResultsText.setText(results);
        }
    }

    // Display final score in a Toast
    private void displayFinalScore(){
        Toast.makeText(this, "Your Score: " + score + "/" + totalQuestions, Toast.LENGTH_LONG).show();
    }
}
