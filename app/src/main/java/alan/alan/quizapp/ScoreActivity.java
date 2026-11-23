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
    Button emailButton, playAgainButton;

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
        playAgainButton = findViewById(R.id.playAgainButton);

        final int score = getIntent().getIntExtra("score", 0);
        final int total = getIntent().getIntExtra("total", 0);
        finalScoreText.setText(getString(R.string.you_got) + " " + score + " "
                + getString(R.string.out_of) + " " + total);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"arriazaalan7@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.i_scored) + " " + score + " "
                        + getString(R.string.out_of) + " " + total);

                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(ScoreActivity.this, getString(R.string.no_email_app), Toast.LENGTH_SHORT).show();
                }
            }
        });

        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScoreActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
