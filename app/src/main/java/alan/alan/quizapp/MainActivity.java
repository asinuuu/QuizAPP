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

    Question[] questions = {
            new Question("Android apps can be written in Java.", true),
            new Question("A TextView is used to show text on the screen.", true),
            new Question("The findViewById method deletes a view.", false),
            new Question("A Toast is a small pop-up message.", true),
            new Question("XML is used to build the layout of an app.", true)
    };

    int currentQuestion = 0;
    int score = 0;
    int questionsAnswered = 0;
    boolean currentAnswer = true;
    boolean alreadyAnswered = false;
    boolean thinking = false;

    TextView scoreText, questionText, feedbackText;
    Button trueButton, falseButton, nextButton, finishedButton, generateButton;
    EditText topicInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        scoreText = findViewById(R.id.scoreText);
        questionText = findViewById(R.id.questionText);
        feedbackText = findViewById(R.id.feedbackText);
        trueButton = findViewById(R.id.trueButton);
        falseButton = findViewById(R.id.falseButton);
        nextButton = findViewById(R.id.nextButton);
        finishedButton = findViewById(R.id.finishedButton);
        generateButton = findViewById(R.id.generateButton);
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
                currentQuestion = currentQuestion + 1;
                if (currentQuestion >= questions.length) {
                    currentQuestion = 0;
                }
                showQuestion();
            }
        });

        finishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScoreActivity.class);
                intent.putExtra("score", score);
                intent.putExtra("total", questionsAnswered);
                startActivity(intent);
            }
        });

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = topicInput.getText().toString().trim();
                if (topic.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Type a topic first", Toast.LENGTH_SHORT).show();
                    return;
                }
                questionText.setText("Thinking...");
                feedbackText.setText("");
                thinking = true;
                updateButtons();

                QuestionGenerator.generate(topic, new QuestionGenerator.Listener() {
                    @Override
                    public void onQuestion(final String statement, final boolean answer) {
                        // This runs on a background thread, so hop back to the screen thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentAnswer = answer;
                                alreadyAnswered = false;
                                thinking = false;
                                questionText.setText(statement);
                                updateButtons();
                            }
                        });
                    }

                    @Override
                    public void onError(final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Nothing to answer now, so let the user move on instead of scoring an old answer
                                alreadyAnswered = true;
                                thinking = false;
                                questionText.setText(message);
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
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
        alreadyAnswered = false;
        feedbackText.setText("");
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
    }

    private void checkAnswer(boolean userSaidTrue) {
        if (alreadyAnswered) {
            return;
        }
        alreadyAnswered = true;
        questionsAnswered = questionsAnswered + 1;

        if (userSaidTrue == currentAnswer) {
            score = score + 1;
            feedbackText.setText("Right!");
            Toast.makeText(MainActivity.this, "Right!", Toast.LENGTH_SHORT).show();
        } else {
            feedbackText.setText("Wrong!");
            Toast.makeText(MainActivity.this, "Wrong!", Toast.LENGTH_SHORT).show();
        }
        scoreText.setText("Score: " + score);
        updateButtons();
    }
}
