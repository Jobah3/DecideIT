
package jovan.janjic;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StudentViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_view);

        // Show profile first with greeting
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new ProfileFragment())
                .commit();

        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnCalendar = findViewById(R.id.btnCalendar);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ProfileFragment())
                        .commit();
            }
        });
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new SessionListFragment())
                        .commit();
            }
        });
    }
}
