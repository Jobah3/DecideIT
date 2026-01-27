package jovan.janjic;

import android.content.Context;

public class TableFactory {

	public static TableManager getTableInstance(Context context, String tableName) {
		if (DatabaseHelper.TABLE_USERS.equals(tableName)) {
			return new UsersTableManager(context);
		} else if (DatabaseHelper.TABLE_SESSIONS.equals(tableName)) {
			return new BaseTableManager(context, DatabaseHelper.TABLE_SESSIONS);
		} else if (DatabaseHelper.TABLE_VOTES.equals(tableName)) {
			return new BaseTableManager(context, DatabaseHelper.TABLE_VOTES);
		} else {
			return new BaseTableManager(context, tableName);
		}
	}

	public static String[] availableTables() {
		return new String[]{ DatabaseHelper.TABLE_USERS, DatabaseHelper.TABLE_SESSIONS, DatabaseHelper.TABLE_VOTES };
	}
}
