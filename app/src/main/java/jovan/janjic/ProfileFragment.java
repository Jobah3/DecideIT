package jovan.janjic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_profile, container, false);
		TextView hello = v.findViewById(R.id.helloText);
		String username = null;
		Bundle args = getArguments();
		if (args != null) {
			username = args.getString("username");
		}
		if (username == null && getActivity() != null) {
			android.content.SharedPreferences sp = getActivity().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE);
			username = sp.getString("username", null);
		}
		if (username != null) {
			DatabaseHelper db = new DatabaseHelper(getContext());
			String[] ns = db.getNameSurnameByUsername(username);
			if (ns != null) {
				hello.setText("Hello: " + ns[0] + " " + ns[1]);
			} else {
				hello.setText("Hello: " + username);
			}
		}
		return v;
	}
}
