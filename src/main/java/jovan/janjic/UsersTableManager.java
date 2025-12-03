package jovan.janjic;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class UsersTableManager extends BaseTableManager {

	public UsersTableManager(Context ctx) {
		super(ctx, DatabaseHelper.TABLE_USERS);
	}

	@Override
	public List<String> getAllRowsAsStrings() {
		List<String> rows = new ArrayList<>();
		SQLiteDatabase db = dbh.getReadableDatabase();
		Cursor c = null;
		try {
			c = db.rawQuery("SELECT * FROM " + tableName, null);
			if (c != null) {
				while (c.moveToNext()) {
					StringBuilder sb = new StringBuilder();
					String[] cols = c.getColumnNames();
					for (int i = 0; i < cols.length; i++) {
						if (i > 0) sb.append(" | ");
						String colName = cols[i];
						String val = c.isNull(i) ? "NULL" : c.getString(i);
						if (colName.toLowerCase().contains("password")) {
							val = "***"; // mask sensitive fields
						}
						sb.append(colName).append(": ").append(val);
					}
					rows.add(sb.toString());
				}
			}
		} catch (Exception e) {
			rows.add("Error reading table: " + e.getMessage());
		} finally {
			if (c != null) c.close();
		}
		return rows;
	}
}
