
package jovan.janjic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class StudentListFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_student_list, container, false);
		ListView list = v.findViewById(R.id.listStudents);
		TextView empty = v.findViewById(R.id.txtEmptyStudents);
		List<Student> data = loadAllStudents();
		StudentAdapter adapter = new StudentAdapter(getContext(), data);
		list.setAdapter(adapter);
		empty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
		return v;
	}

	private List<Student> loadAllStudents() {
		List<Student> res = new ArrayList<>();
		DatabaseHelper db = new DatabaseHelper(getContext());
		android.database.sqlite.SQLiteDatabase rdb = db.getReadableDatabase();
		android.database.Cursor c = rdb.query(DatabaseHelper.TABLE_USERS,
				new String[]{DatabaseHelper.U_COL_NAME, DatabaseHelper.U_COL_SURNAME, DatabaseHelper.U_COL_USERNAME, DatabaseHelper.U_COL_ROLE},
				null, null, null, null, DatabaseHelper.U_COL_NAME + ", " + DatabaseHelper.U_COL_SURNAME);
		if (c.moveToFirst()) {
			do {
				String role = c.getString(3);
				if (!"Admin".equalsIgnoreCase(role)) {
					String fullName = c.getString(0) + " " + c.getString(1);
					String index = c.getString(2);
					res.add(new Student(fullName, index));
				}
			} while (c.moveToNext());
		}
		c.close();
		return res;
	}
}
