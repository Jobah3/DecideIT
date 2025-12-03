package jovan.janjic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SessionAdapter extends ArrayAdapter<Session> {
	public SessionAdapter(Context context, List<Session> sessions) {
		super(context, 0, sessions);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_session, parent, false);
		}
		Session s = getItem(position);
		TextView title = convertView.findViewById(R.id.txtSessionTitle);
		TextView date = convertView.findViewById(R.id.txtSessionDate);
		String displayTitle = s.getSessionName() != null ? s.getSessionName() : s.getTitle();
		title.setText(displayTitle);
		date.setText(s.getDate());
		return convertView;
	}
}
