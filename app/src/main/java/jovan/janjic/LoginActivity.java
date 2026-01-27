
package jovan.janjic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);


		EditText edtUsername = findViewById(R.id.edtUsername);
		EditText edtPassword = findViewById(R.id.edtPassword);
		DatabaseHelper db = new DatabaseHelper(this);
		Button btnLogin = findViewById(R.id.loginButton);
		btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = edtUsername.getText().toString().trim();
				String password = edtPassword.getText().toString();
				if (username.isEmpty() || password.isEmpty()) {
					Toast.makeText(LoginActivity.this, "Enter username and password", Toast.LENGTH_SHORT).show();
					return;
				}
				boolean ok = db.validateLogin(username, password);
				if (!ok) {
					Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
					return;
				}
				String role = db.getUserRole(username);
				android.content.SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
				sp.edit().putString("username", username).apply();
				if ("Admin".equalsIgnoreCase(role)) {
					startActivity(new Intent(LoginActivity.this, AdminActivity.class));
				} else {
					startActivity(new Intent(LoginActivity.this, StudentViewActivity.class));
				}
			}
		});

		Button btnRegister = findViewById(R.id.registerButton);
		btnRegister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
			}
		});
	}
}
