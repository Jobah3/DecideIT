package jovan.janjic;

public interface TableManager {
	String getTableName();
	String[] getColumnNames();
	java.util.List<String> getAllRowsAsStrings();
	void deleteRowById(long id);
}
