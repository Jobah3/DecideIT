package jovan.janjic;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class BaseTableManager implements TableManager {
	protected final DatabaseHelper dbh;
	protected final String tableName;

	public BaseTableManager(Context ctx, String tableName) {
		this.dbh = new DatabaseHelper(ctx);
		this.tableName = tableName;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public String[] getColumnNames() {
		SQLiteDatabase db = dbh.getReadableDatabase();
		Cursor c = null;
		try {
			c = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 1", null);
			if (c != null && c.moveToFirst()) {
				return c.getColumnNames();
			}
			return new String[0];
		} finally {
			if (c != null) c.close();
		}
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
						String val = c.isNull(i) ? "NULL" : c.getString(i);
						sb.append(cols[i]).append(": ").append(val);
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

	@Override
	public void deleteRowById(long id) {
		SQLiteDatabase db = dbh.getWritableDatabase();
		db.delete(tableName, "_id=?", new String[]{String.valueOf(id)});
	}
}
