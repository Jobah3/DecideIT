package jovan.janjic;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DecideActivity extends AppCompatActivity {
    private String sessionName;
    private String sessionDate;
    private DatabaseHelper db;
    private RadioGroup radioGroup;
    private String serverSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decide);
        db = new DatabaseHelper(this);

        sessionName = getIntent().getStringExtra("session_name");
        sessionDate = getIntent().getStringExtra("session_date");
        serverSessionId = new DatabaseHelper(this).getServerIdForSession(sessionName, sessionDate);

        radioGroup = findViewById(R.id.radioGroup);

        Button submitVote = findViewById(R.id.submitVote);
        submitVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(DecideActivity.this, "Please select an option", Toast.LENGTH_SHORT).show();
                    return;
                }
                RadioButton rb = findViewById(selectedId);
                String choice = rb.getText().toString().toLowerCase();
                final String voteVal = choice.contains("yes") ? "yes" : (choice.contains("no") ? "no" : "abstain");
                // send to server and update local
                new Thread(() -> {
                    try {
                        if (serverSessionId != null && !serverSessionId.isEmpty()) {
                            HttpHelper helper = new HttpHelper();
                            String jsonBody = "{\"sessionId\":\"" + serverSessionId + "\",\"vote\":\"" + voteVal + "\"}";
                            StringBuilder resp = new StringBuilder();
                            new HttpThread(helper, CommandEnum.POST_VOTE, jsonBody, resp).run();
                        }
                        db.addVote(sessionName, sessionDate, voteVal);
                        runOnUiThread(() -> {
                            Toast.makeText(DecideActivity.this, "Vote recorded", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(DecideActivity.this, "Failed to send vote to server", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }
        });
    }
}
