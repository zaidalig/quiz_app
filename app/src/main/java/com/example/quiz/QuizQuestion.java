// app/src/main/java/com/example/quiz/QuizQuestion.java
package com.example.quiz;

import com.google.gson.annotations.SerializedName;
import java.util.List;

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
