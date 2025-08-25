package com.family.store;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView tvScore, tvQuestion, tvFeedback;
    private EditText etAnswer;
    private Button btnCheck, btnSkip, btnShare;

    private int a, b, score = 0, streak = 0, questionCount = 0, correctCount = 0;
    private char op;
    private Random random = new Random();
    private final int TOTAL_QUESTIONS = 5; // Limit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvScore    = findViewById(R.id.tvScore);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvFeedback = findViewById(R.id.tvFeedback);
        etAnswer   = findViewById(R.id.etAnswer);
        btnCheck   = findViewById(R.id.btnCheck);
        btnSkip    = findViewById(R.id.btnSkip);
        btnShare   = findViewById(R.id.btnShare);

        // Initially hide feedback
        tvFeedback.setVisibility(TextView.GONE);

        startNewQuiz();

        btnCheck.setOnClickListener(v -> checkAnswer());
        btnSkip.setOnClickListener(v -> skipQuestion());

        etAnswer.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswer();
                return true;
            }
            return false;
        });

        btnShare.setOnClickListener(v -> shareScore());
    }

    // Start or reset quiz
    private void startNewQuiz() {
        score = 0;
        streak = 0;
        questionCount = 0;
        correctCount = 0;

        tvFeedback.setVisibility(TextView.GONE); // Hide feedback
        btnCheck.setEnabled(true);
        btnSkip.setEnabled(true);
        btnShare.setVisibility(Button.GONE);

        nextQuestion();
    }

    // Generate a new random question
    private void nextQuestion() {
        if (questionCount >= TOTAL_QUESTIONS) {
            showResult();
            return;
        }

        char[] ops = {'+', '-', '×', '÷'};
        op = ops[random.nextInt(ops.length)];

        switch (op) {
            case '+':
                a = random.nextInt(50) + 1;
                b = random.nextInt(50) + 1;
                break;
            case '-':
                a = random.nextInt(50) + 1;
                b = random.nextInt(a + 1);
                break;
            case '×':
                a = random.nextInt(11) + 2;
                b = random.nextInt(11) + 2;
                break;
            case '÷':
                b = random.nextInt(11) + 2;
                int r = random.nextInt(11) + 2;
                a = b * r;
                break;
        }

        tvQuestion.setText(a + " " + op + " " + b + " = ?");
        etAnswer.setText("");
        etAnswer.requestFocus();

        tvFeedback.setVisibility(TextView.GONE); // hide feedback until user answers
        updateScore();
    }

    private int correctAnswer() {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '×': return a * b;
            case '÷': return a / b;
        }
        return 0;
    }

    private void checkAnswer() {
        if (questionCount >= TOTAL_QUESTIONS) return;

        String user = etAnswer.getText().toString().trim();
        if (user.isEmpty()) {
            etAnswer.setError("Enter answer");
            return;
        }

        int ans;
        try {
            ans = Integer.parseInt(user);
        } catch (NumberFormatException e) {
            etAnswer.setError("Numbers only");
            return;
        }

        int right = correctAnswer();
        if (ans == right) {
            score += 10;
            correctCount++;
            streak++;
            tvFeedback.setText("✅ Correct! (+10)");
        } else {
            streak = 0;
            tvFeedback.setText("❌ " + ans + " is wrong. Correct: " + right);
        }

        tvFeedback.setVisibility(TextView.VISIBLE); // Show feedback
        questionCount++;

        if (questionCount >= TOTAL_QUESTIONS) {
            showResult();
        } else {
            // Wait 1.5 seconds before showing next question
            etAnswer.postDelayed(this::nextQuestion, 1500);
        }

        updateScore();
    }

    private void skipQuestion() {
        tvFeedback.setText("❌ Skipped!");
        tvFeedback.setVisibility(TextView.VISIBLE);
        streak = 0;
        questionCount++;

        if (questionCount >= TOTAL_QUESTIONS) {
            showResult();
        } else {
            etAnswer.postDelayed(this::nextQuestion, 1500);
        }

        updateScore();
    }


    private void checkEnd() {
        if (questionCount >= TOTAL_QUESTIONS) {
            showResult();
        } else {
            nextQuestion();
        }
    }

    private void showResult() {
        btnCheck.setEnabled(false);
        btnSkip.setEnabled(false);
        btnShare.setVisibility(Button.VISIBLE);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Quiz Finished!");
        dialog.setMessage("Your Score: " + correctCount + "/" + TOTAL_QUESTIONS);
        dialog.setPositiveButton("OK", (d, w) -> startNewQuiz()); // Reset quiz
        dialog.setCancelable(false);
        dialog.show();
    }

    private void updateScore() {
        tvScore.setText("Score: " + score +
                "   Question: " + (questionCount + 1) + "/" + TOTAL_QUESTIONS +
                "   Streak: " + streak + "🔥");
    }

    private void shareScore() {
        String shareText = "I scored " + correctCount + "/" + TOTAL_QUESTIONS +
                " in LearnMath Quiz! 🎉";
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share via"));
    }
}
