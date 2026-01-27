package jovan.janjic;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class AdminActivity extends AppCompatActivity {

    Button btnStudents, btnSessions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        btnStudents = findViewById(R.id.btnStudents);
        btnSessions = findViewById(R.id.btnSessions);

        // Enable buttons
        btnStudents.setEnabled(true);
        btnSessions.setEnabled(true);

        btnStudents.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                loadFragment(new StudentListFragment());
                setActiveButton(btnStudents);
            }
        });

        btnSessions.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                loadFragment(new SessionListFragment());
                setActiveButton(btnSessions);
            }
        });

        // default
        loadFragment(new StudentListFragment());
        setActiveButton(btnStudents);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.adminFragmentContainer, fragment);
        transaction.commit();
    }

    private void setActiveButton(Button active) {
        int activeColor = ContextCompat.getColor(this, R.color.activeTab);
        int inactiveColor = ContextCompat.getColor(this, R.color.inactiveTab);

        btnStudents.setBackgroundColor(inactiveColor);
        btnSessions.setBackgroundColor(inactiveColor);
        active.setBackgroundColor(activeColor);
    }
}
