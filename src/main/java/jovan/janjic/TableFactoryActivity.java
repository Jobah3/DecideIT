package jovan.janjic;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class TableFactoryActivity extends AppCompatActivity {
	Spinner spinnerTables;
	ListView listViewRows;
	Button btnRefresh;
	TableManager currentManager;
	List<String> currentRows;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_table_factory);

		spinnerTables = findViewById(R.id.spinnerTables);
		listViewRows = findViewById(R.id.listViewRows);
		btnRefresh = findViewById(R.id.btnRefresh);

		String[] tables = TableFactory.availableTables();
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tables);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerTables.setAdapter(spinnerAdapter);

		spinnerTables.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String t = tables[position];
				currentManager = TableFactory.getTableInstance(TableFactoryActivity.this, t);
				refreshRows();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		btnRefresh.setOnClickListener(v -> refreshRows());

		listViewRows.setOnItemLongClickListener((parent, view, position, id) -> {
			if (currentManager == null || currentRows == null) return true;
			String row = currentRows.get(position);
			long rowId = extractId(row);
			if (rowId == -1) {
				Toast.makeText(this, "No _id column found in this row.", Toast.LENGTH_SHORT).show();
				return true;
			}
			new AlertDialog.Builder(this)
					.setTitle("Delete row")
					.setMessage("Delete row with _id=" + rowId + "?")
					.setPositiveButton("Yes", (d, w) -> {
						currentManager.deleteRowById(rowId);
						refreshRows();
						Toast.makeText(this, "Row deleted", Toast.LENGTH_SHORT).show();
					})
					.setNegativeButton("No", null)
					.show();
			return true;
		});
	}

	private void refreshRows() {
		if (currentManager == null) return;
		currentRows = currentManager.getAllRowsAsStrings();
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currentRows);
		listViewRows.setAdapter(adapter);
	}

	private long extractId(String rowStr) {
		// naive parsing: look for "_id: <number>"
		try {
			int idx = rowStr.indexOf("_id:");
			if (idx == -1) return -1;
			String sub = rowStr.substring(idx + 4).trim();
			String[] parts = sub.split("[| ]");
			return Long.parseLong(parts[0].trim());
		} catch (Exception e) {
			return -1;
		}
	}
}
