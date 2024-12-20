// app/src/main/java/com/example/quiz/MainActivity.java
package com.example.quiz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;
import retrofit2.*;

public class MainActivity extends AppCompatActivity {

    // Layout References
    private LinearLayout settingsLayout, quizLayout, resultLayout;
    private Spinner spinnerCategory, spinnerDifficulty, spinnerQuizType, spinnerNumQuestions;
    private Button buttonStart, buttonNext, buttonRestart;
    private TextView textQuestion, textTimer, textQuestionCount, textScore, textDetailedResults;
    private ProgressBar progressBar;
    private LinearLayout layoutOptions;
    private RecyclerView recyclerViewHighScores;

    // Quiz State Variables
    private List<QuizQuestion> questionsList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer countDownTimer;
    private final long timePerQuestion = 30 * 1000; // 30 seconds
    private String selectedQuizType = "multiple";
    private int selectedNumQuestions = 10;
    private List<String> userAnswers;

    // High Scores
    private List<HighScore> highScoreList;
    private HighScoreAdapter highScoreAdapter;

    // Retrofit API Interface
    private OpenTriviaAPI openTriviaAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Layouts
        settingsLayout = findViewById(R.id.settingsLayout);
        quizLayout = findViewById(R.id.quizLayout);
        resultLayout = findViewById(R.id.resultLayout);

        // Initialize Settings Views
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerQuizType = findViewById(R.id.spinnerQuizType);
        spinnerNumQuestions = findViewById(R.id.spinnerNumQuestions);
        buttonStart = findViewById(R.id.buttonStart);

        // Initialize Quiz Views
        progressBar = findViewById(R.id.progressBar);
        textTimer = findViewById(R.id.textTimer);
        textQuestionCount = findViewById(R.id.textQuestionCount);
        textQuestion = findViewById(R.id.textQuestion);
        layoutOptions = findViewById(R.id.layoutOptions);
        buttonNext = findViewById(R.id.buttonNext);

        // Initialize Result Views
        textScore = findViewById(R.id.textScore);
        textDetailedResults = findViewById(R.id.textDetailedResults);
        recyclerViewHighScores = findViewById(R.id.recyclerViewHighScores);
        buttonRestart = findViewById(R.id.buttonRestart);

        // Initialize High Scores
        highScoreList = new ArrayList<>();
        highScoreAdapter = new HighScoreAdapter(highScoreList);
        recyclerViewHighScores.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHighScores.setAdapter(highScoreAdapter);

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        openTriviaAPI = retrofit.create(OpenTriviaAPI.class);

        // Populate Spinners
        populateCategorySpinner();
        populateDifficultySpinner();
        populateQuizTypeSpinner();
        populateNumQuestionsSpinner();

        // Initialize User Answers List
        userAnswers = new ArrayList<>();

        // Load High Scores
        loadHighScores();

        // Start Button Click Listener
        buttonStart.setOnClickListener(v -> {
            startQuiz();
        });

        // Next Button Click Listener
        buttonNext.setOnClickListener(v -> {
            if (!buttonNext.isEnabled()) return;
            currentQuestionIndex++;
            if (currentQuestionIndex < questionsList.size()) {
                displayQuestion();
            } else {
                showResults();
            }
        });

        // Restart Button Click Listener
        buttonRestart.setOnClickListener(v -> {
            resetQuiz();
        });
    }

    // Populate Category Spinner
    private void populateCategorySpinner() {
        // Default Categories
        List<String> categories = new ArrayList<>();
        categories.add("Any Category");

        // Fetch Categories from API
        Call<CategoryResponse> call = openTriviaAPI.getCategories();
        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                    return;
                }
                CategoryResponse categoryResponse = response.body();
                for (Category cat : categoryResponse.trivia_categories) {
                    categories.add(cat.name);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, categories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error loading categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Populate Difficulty Spinner
    private void populateDifficultySpinner() {
        List<String> difficulties = new ArrayList<>();
        difficulties.add("Any Difficulty");
        difficulties.add("Easy");
        difficulties.add("Medium");
        difficulties.add("Hard");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficulties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);
    }

    // Populate Quiz Type Spinner
    private void populateQuizTypeSpinner() {
        List<String> quizTypes = new ArrayList<>();
        quizTypes.add("Multiple Choice");
        quizTypes.add("True/False");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, quizTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuizType.setAdapter(adapter);

        spinnerQuizType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (i == 0) {
                    selectedQuizType = "multiple";
                } else {
                    selectedQuizType = "boolean";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedQuizType = "multiple";
            }
        });
    }

    // Populate Number of Questions Spinner
    private void populateNumQuestionsSpinner() {
        List<String> numQuestions = new ArrayList<>();
        numQuestions.add("5");
        numQuestions.add("10");
        numQuestions.add("15");
        numQuestions.add("20");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, numQuestions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNumQuestions.setAdapter(adapter);
    }

    // Start Quiz
    private void startQuiz() {
        // Get Selected Options
        String category = spinnerCategory.getSelectedItem().toString();
        String difficulty = spinnerDifficulty.getSelectedItem().toString();
        String numQ = spinnerNumQuestions.getSelectedItem().toString();

        selectedNumQuestions = Integer.parseInt(numQ);

        // Build API Call
        String categoryParam = "";
        if (!category.equals("Any Category")) {
            categoryParam = "&category=" + getCategoryId(category);
        }

        String difficultyParam = "";
        if (!difficulty.equals("Any Difficulty")) {
            difficultyParam = "&difficulty=" + difficulty.toLowerCase();
        }

        String typeParam = "&type=" + selectedQuizType;

        String apiURL = "api.php?amount=" + selectedNumQuestions + typeParam + categoryParam + difficultyParam;

        // Make API Call
        Call<QuizResponse> call = openTriviaAPI.getQuestions(apiURL);
        call.enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(Call<QuizResponse> call, Response<QuizResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
                    return;
                }

                QuizResponse quizResponse = response.body();
                if (quizResponse.response_code != 0) {
                    Toast.makeText(MainActivity.this, "No questions available for the selected options", Toast.LENGTH_LONG).show();
                    return;
                }

                questionsList = quizResponse.results;
                userAnswers = new ArrayList<>(Collections.nCopies(questionsList.size(), ""));

                // Initialize Quiz
                currentQuestionIndex = 0;
                score = 0;
                progressBar.setProgress(0);
                textQuestionCount.setText("Question 1 of " + questionsList.size());
                displayQuestion();

                // Switch Layouts
                settingsLayout.setVisibility(View.GONE);
                resultLayout.setVisibility(View.GONE);
                quizLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<QuizResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error loading questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get Category ID based on Category Name
    private int getCategoryId(String categoryName) {
        // For simplicity, hardcode some category IDs
        // In a real application, you should map category names to their IDs dynamically
        switch (categoryName) {
            case "General Knowledge":
                return 9;
            case "Entertainment: Books":
                return 10;
            case "Entertainment: Film":
                return 11;
            case "Science & Nature":
                return 17;
            case "Sports":
                return 21;
            case "Geography":
                return 22;
            case "History":
                return 23;
            case "Politics":
                return 24;
            case "Art":
                return 25;
            case "Celebrities":
                return 26;
            case "Animals":
                return 27;
            case "Vehicles":
                return 28;
            case "Entertainment: Comics":
                return 29;
            case "Science: Computers":
                return 18;
            case "Entertainment: Musicals & Theatres":
                return 12;
            case "Entertainment: Television":
                return 14;
            case "Entertainment: Video Games":
                return 15;
            case "Entertainment: Board Games":
                return 16;
            case "Science: Mathematics":
                return 19;
            case "Mythology":
                return 20;
            default:
                return 0;
        }
    }

    // Display Current Question
    private void displayQuestion() {
        if (currentQuestionIndex >= questionsList.size()) {
            showResults();
            return;
        }

        // Reset Options Layout
        layoutOptions.removeAllViews();

        QuizQuestion currentQuestion = questionsList.get(currentQuestionIndex);
        textQuestion.setText(currentQuestion.question);
        textQuestionCount.setText("Question " + (currentQuestionIndex + 1) + " of " + questionsList.size());

        // Shuffle Options
        List<String> options = new ArrayList<>(currentQuestion.incorrect_answers);
        options.add(currentQuestion.correct_answer);
        Collections.shuffle(options);

        // Create Buttons for Options
        for (String option : options) {
            Button btnOption = new Button(this);
            btnOption.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            btnOption.setText(option);
            btnOption.setAllCaps(false);
            btnOption.setTextSize(16);
            btnOption.setBackgroundResource(android.R.drawable.btn_default);
            btnOption.setOnClickListener(v -> {
                selectOption(option, btnOption);
            });
            layoutOptions.addView(btnOption);
        }

        // Reset Next Button
        buttonNext.setEnabled(false);

        // Start Timer
        startTimer();
    }

    // Select Option
    private void selectOption(String selectedOption, Button selectedButton) {
        // Save User Answer
        userAnswers.set(currentQuestionIndex, selectedOption);

        // Check if Correct
        QuizQuestion currentQuestion = questionsList.get(currentQuestionIndex);
        if (selectedOption.equals(currentQuestion.correct_answer)) {
            score++;
            selectedButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            selectedButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            // Highlight Correct Answer
            highlightCorrectAnswer(currentQuestion.correct_answer);
        }

        // Disable All Options
        disableAllOptions();

        // Enable Next Button
        buttonNext.setEnabled(true);

        // Stop Timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    // Highlight Correct Answer
    private void highlightCorrectAnswer(String correctAnswer) {
        for (int i = 0; i < layoutOptions.getChildCount(); i++) {
            Button btn = (Button) layoutOptions.getChildAt(i);
            if (btn.getText().toString().equals(correctAnswer)) {
                btn.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            }
        }
    }

    // Disable All Option Buttons
    private void disableAllOptions() {
        for (int i = 0; i < layoutOptions.getChildCount(); i++) {
            Button btn = (Button) layoutOptions.getChildAt(i);
            btn.setEnabled(false);
        }
    }

    // Start Timer
    private void startTimer() {
        textTimer.setText("Time Left: 30s");
        countDownTimer = new CountDownTimer(timePerQuestion, 1000) {
            public void onTick(long millisUntilFinished) {
                textTimer.setText("Time Left: " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                textTimer.setText("Time Left: 0s");
                // Handle Unanswered Question
                userAnswers.set(currentQuestionIndex, "No Answer");
                highlightCorrectAnswer(questionsList.get(currentQuestionIndex).correct_answer);
                disableAllOptions();
                buttonNext.setEnabled(true);
            }
        }.start();
    }

    // Show Results
    private void showResults() {
        // Stop Timer if Running
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Hide Quiz Layout
        quizLayout.setVisibility(View.GONE);

        // Show Result Layout
        resultLayout.setVisibility(View.VISIBLE);

        // Display Score
        textScore.setText("You scored " + score + " out of " + questionsList.size());

        // Display Detailed Results
        StringBuilder detailedResults = new StringBuilder();
        for (int i = 0; i < questionsList.size(); i++) {
            QuizQuestion q = questionsList.get(i);
            String userAnswer = userAnswers.get(i);
            boolean isCorrect = q.correct_answer.equals(userAnswer);
            detailedResults.append("Question ").append(i + 1).append(": ").append(q.question).append("\n");
            detailedResults.append("Your answer: ").append(userAnswer).append(isCorrect ? " (Correct)" : " (Incorrect)").append("\n");
            if (!isCorrect && !userAnswer.equals("No Answer")) {
                detailedResults.append("Correct answer: ").append(q.correct_answer).append("\n");
            }
            detailedResults.append("\n");
        }
        textDetailedResults.setText(detailedResults.toString());

        // Save High Score
        saveHighScore();

        // Load High Scores
        loadHighScores();
    }

    // Save High Score to SharedPreferences
    private void saveHighScore() {
        HighScore newHighScore = new HighScore();
        newHighScore.score = score;
        newHighScore.total = questionsList.size();
        newHighScore.date = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

        highScoreList.add(newHighScore);

        // Sort High Scores Descending
        Collections.sort(highScoreList, (hs1, hs2) -> hs2.score - hs1.score);

        // Keep Top 10
        if (highScoreList.size() > 10) {
            highScoreList = highScoreList.subList(0, 10);
        }

        // Save to SharedPreferences
        SharedPrefManager.getInstance(this).saveHighScores(highScoreList);
    }

    // Load High Scores from SharedPreferences
    private void loadHighScores() {
        highScoreList = SharedPrefManager.getInstance(this).getHighScores();
        highScoreAdapter.setHighScoreList(highScoreList);
        highScoreAdapter.notifyDataSetChanged();
    }

    // Reset Quiz
    private void resetQuiz() {
        // Reset Variables
        currentQuestionIndex = 0;
        score = 0;
        userAnswers = new ArrayList<>();

        // Hide Result Layout
        resultLayout.setVisibility(View.GONE);

        // Show Settings Layout
        settingsLayout.setVisibility(View.VISIBLE);
    }

    // Retrofit API Interface
    public interface OpenTriviaAPI {
        @GET("api_category.php")
        Call<CategoryResponse> getCategories();

        @GET
        Call<QuizResponse> getQuestions(@Url String url);
    }

    // Data Models
    public class CategoryResponse {
        List<Category> trivia_categories;
    }

    public class Category {
        int id;
        String name;
    }

    public class QuizResponse {
        @SerializedName("response_code")
        int response_code;

        List<QuizQuestion> results;
    }

    public class QuizQuestion {
        String category;
        String type;
        String difficulty;
        String question;

        @SerializedName("correct_answer")
        String correct_answer;

        @SerializedName("incorrect_answers")
        List<String> incorrect_answers;
    }
}
