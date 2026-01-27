
package jovan.janjic;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		EditText edtName = findViewById(R.id.edtName);
		EditText edtSurname = findViewById(R.id.edtSurname);
		EditText edtUsername = findViewById(R.id.edtUsername);
		EditText edtPassword = findViewById(R.id.edtPassword);
		Spinner roleSpinner = findViewById(R.id.roleSpinner);
		Button btnRegister = findViewById(R.id.registerButton);

		ArrayAdapter<String> rolesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Admin", "Student"});
		rolesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		roleSpinner.setAdapter(rolesAdapter);

		DatabaseHelper db = new DatabaseHelper(this);
		btnRegister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = edtName.getText().toString().trim();
				String surname = edtSurname.getText().toString().trim();
				String username = edtUsername.getText().toString().trim();
				String password = edtPassword.getText().toString();
				String role = String.valueOf(roleSpinner.getSelectedItem());

				if (name.isEmpty() || surname.isEmpty() || username.isEmpty() || password.isEmpty()) {
					Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
					return;
				}

				if (db.isUsernameExists(username)) {
					Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
					return;
				}

				boolean ok = db.insertUser(name, surname, username, password, role);
				if (ok) {
					Toast.makeText(RegisterActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
