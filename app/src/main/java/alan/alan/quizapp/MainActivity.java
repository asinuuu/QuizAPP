package alan.alan.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Question[] questions;

    int currentQuestion = 0;
    int score = 0;
    int questionsAnswered = 0;
    boolean currentAnswer = true;
    String currentHint = "";
    boolean alreadyAnswered = false;
    boolean thinking = false;
    boolean usedHint = false;

    TextView scoreText, questionText, feedbackText, progressText;
    Button trueButton, falseButton, nextButton, finishedButton, generateButton, hintButton;
    EditText topicInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(systemBars.bottom, keyboard.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });

        questions = new Question[] {
                new Question(getString(R.string.q1_text), true, getString(R.string.q1_hint)),
                new Question(getString(R.string.q2_text), false, getString(R.string.q2_hint)),
                new Question(getString(R.string.q3_text), true, getString(R.string.q3_hint)),
                new Question(getString(R.string.q4_text), true, getString(R.string.q4_hint)),
                new Question(getString(R.string.q5_text), true, getString(R.string.q5_hint))
        };

        scoreText = findViewById(R.id.scoreText);
        questionText = findViewById(R.id.questionText);
        feedbackText = findViewById(R.id.feedbackText);
        progressText = findViewById(R.id.progressText);
        trueButton = findViewById(R.id.trueButton);
        falseButton = findViewById(R.id.falseButton);
        nextButton = findViewById(R.id.nextButton);
        finishedButton = findViewById(R.id.finishedButton);
        generateButton = findViewById(R.id.generateButton);
        hintButton = findViewById(R.id.hintButton);
        topicInput = findViewById(R.id.topicInput);

        showQuestion();

        trueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
            }
        });

        falseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuestion < questions.length - 1) {
                    currentQuestion = currentQuestion + 1;
                    showQuestion();
                }
            }
        });

        finishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScoreActivity.class);
                intent.putExtra("score", score);
                intent.putExtra("total", questionsAnswered);
                startActivity(intent);
                finish();
            }
        });

        hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!alreadyAnswered) {
                    usedHint = true;
                }
                Intent intent = new Intent(MainActivity.this, ViewHintActivity.class);
                intent.putExtra("hint", currentHint);
                startActivity(intent);
            }
        });

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = topicInput.getText().toString().trim();
                if (topic.isEmpty()) {
                    Toast.makeText(MainActivity.this, getString(R.string.ai_need_topic), Toast.LENGTH_SHORT).show();
                    return;
                }
                questionText.setText(getString(R.string.ai_thinking));
                feedbackText.setText("");
                thinking = true;
                updateButtons();

                QuestionGenerator.generate(topic, new QuestionGenerator.Listener() {
                    @Override
                    public void onQuestion(final String statement, final boolean answer) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentAnswer = answer;
                                currentHint = "";
                                alreadyAnswered = false;
                                usedHint = false;
                                thinking = false;
                                questionText.setText(statement);
                                hintButton.setEnabled(false);
                                updateButtons();
                            }
                        });
                    }

                    @Override
                    public void onError(final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                alreadyAnswered = true;
                                thinking = false;
                                questionText.setText(getString(R.string.ai_error));
                                Toast.makeText(MainActivity.this, getString(R.string.ai_error), Toast.LENGTH_SHORT).show();
                                updateButtons();
                            }
                        });
                    }
                });
            }
        });
    }

    private void showQuestion() {
        questionText.setText(questions[currentQuestion].getText());
        currentAnswer = questions[currentQuestion].getAnswer();
        currentHint = questions[currentQuestion].getHint();
        progressText.setText(getString(R.string.question_word) + " " + (currentQuestion + 1) + " "
                + getString(R.string.of_word) + " " + questions.length);
        alreadyAnswered = false;
        usedHint = false;
        feedbackText.setText("");
        hintButton.setEnabled(true);
        updateButtons();
    }

    private void updateButtons() {
        boolean canAnswer = !alreadyAnswered && !thinking;
        boolean canMoveOn = alreadyAnswered && !thinking;
        trueButton.setEnabled(canAnswer);
        falseButton.setEnabled(canAnswer);
        nextButton.setEnabled(canMoveOn);
        finishedButton.setEnabled(canMoveOn);
        generateButton.setEnabled(canMoveOn);

        if (currentQuestion == questions.length - 1) {
            nextButton.setVisibility(View.GONE);
            finishedButton.setVisibility(View.VISIBLE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
            finishedButton.setVisibility(View.GONE);
        }
    }

    private void checkAnswer(boolean userSaidTrue) {
        if (alreadyAnswered) {
            return;
        }
        alreadyAnswered = true;
        questionsAnswered = questionsAnswered + 1;

        if (userSaidTrue == currentAnswer) {
            if (usedHint) {
                feedbackText.setText(getString(R.string.right_no_point));
            } else {
                score = score + 1;
                feedbackText.setText(getString(R.string.right));
            }
            Toast.makeText(MainActivity.this, getString(R.string.right), Toast.LENGTH_SHORT).show();
        } else {
            feedbackText.setText(getString(R.string.wrong));
            Toast.makeText(MainActivity.this, getString(R.string.wrong), Toast.LENGTH_SHORT).show();
        }
        scoreText.setText(getString(R.string.score) + " " + score);
        updateButtons();
    }
}
