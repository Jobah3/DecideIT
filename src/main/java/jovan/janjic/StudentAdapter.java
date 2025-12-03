package jovan.janjic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<Student> {
	public StudentAdapter(Context context, List<Student> students) {
		super(context, 0, students);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_student, parent, false);
		}
		Student s = getItem(position);
		TextView name = convertView.findViewById(R.id.txtFullName);
		TextView index = convertView.findViewById(R.id.txtIndex);
		name.setText(s.name);
		index.setText(s.index);
		return convertView;
	}
}
