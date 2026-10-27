package alan.alan.quizapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ScoreActivity extends AppCompatActivity {

    TextView finalScoreText;
    Button emailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_score);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scoreMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        finalScoreText = findViewById(R.id.finalScoreText);
        emailButton = findViewById(R.id.emailButton);

        // Read the score and the number of questions answered that MainActivity sent us
        final int score = getIntent().getIntExtra("score", 0);
        final int total = getIntent().getIntExtra("total", 0);
        finalScoreText.setText("You got " + score + " out of " + total);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implicit intent: we only say "send an email", Android picks the email app
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"arriazaalan7@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Quiz App Score");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "I scored " + score + " out of " + total);

                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(ScoreActivity.this, "No email app found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
