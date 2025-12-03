package jovan.janjic;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);

		String sessionName = getIntent().getStringExtra("session_name");
		String sessionDate = getIntent().getStringExtra("session_date");

		DatabaseHelper db = new DatabaseHelper(this);
        // try to fetch from server, then show local
        String serverId = db.getServerIdForSession(sessionName, sessionDate);
        if (serverId != null && !serverId.isEmpty()) {
            new Thread(() -> {
                try {
                    HttpHelper helper = new HttpHelper();
                    StringBuilder resp = new StringBuilder();
                    new HttpThread(helper, CommandEnum.GET_VOTES, serverId, resp).run();
                    String json = resp.toString();
                    if (!json.isEmpty()) {
                        org.json.JSONArray arr = new org.json.JSONArray(json);
                        if (arr.length() > 0) {
                            org.json.JSONObject o = arr.getJSONObject(0);
                            int yes = o.optInt("yes", 0);
                            int no = o.optInt("no", 0);
                            int abstain = o.optInt("abstain", 0);
                            db.setVotes(sessionName, sessionDate, yes, no, abstain);
                        }
                    }
                } catch (Exception ignored) {}
                runOnUiThread(() -> updateUiWithLocal(db, sessionName, sessionDate));
            }).start();
        } else {
            updateUiWithLocal(db, sessionName, sessionDate);
        }
	}

	private void updateUiWithLocal(DatabaseHelper db, String sessionName, String sessionDate) {
		int[] votes = db.getVotes(sessionName, sessionDate);
		TextView resText = findViewById(R.id.resultsText);
		resText.setText("Results for " + sessionName + " (" + sessionDate + ")\nYes: " + votes[0] + "\nNo: " + votes[1] + "\nAbstain: " + votes[2]);
	}
}
