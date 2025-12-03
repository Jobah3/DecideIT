
package jovan.janjic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class SessionListFragment extends Fragment {

    CalendarView calendarView;
    Button submitButton;
    ListView listSessions;
    TextView emptyView;
    DatabaseHelper db;
    String selectedDate; // in yyyy-MM-dd

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_session_list, container, false);
        calendarView = view.findViewById(R.id.calendarView);
        submitButton = view.findViewById(R.id.btnSubmitSession);
        listSessions = view.findViewById(R.id.listSessions);
        emptyView = view.findViewById(R.id.txtEmptySessions);
        db = new DatabaseHelper(getContext());

        // default selected date
        Calendar cal = Calendar.getInstance();
        selectedDate = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = String.format("%04d-%02d-%02d", year, month+1, dayOfMonth);
                loadSessionsForDate(selectedDate);
            }
        });

        // Hide add-session button for students; only Admin can create sessions
        if (getActivity() instanceof AdminActivity) {
            submitButton.setOnClickListener(v -> showAddSessionDialog());
            submitButton.setVisibility(View.VISIBLE);
        } else {
            submitButton.setVisibility(View.GONE);
        }

        loadSessionsForDate(selectedDate);
        return view;
    }

    private void loadSessionsForDate(String date) {
        // try to sync from server first (background)
        new Thread(() -> {
            try {
                HttpHelper helper = new HttpHelper();
                StringBuilder resp = new StringBuilder();
                new HttpThread(helper, CommandEnum.GET_SESSIONS, null, resp).run();
                String json = resp.toString();
                if (!json.isEmpty()) {
                    org.json.JSONArray arr = new org.json.JSONArray(json);
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject o = arr.getJSONObject(i);
                        String serverId = o.optString("_id");
                        String isoDate = o.optString("date");
                        String name = o.optString("sessionName");
                        String desc = o.optString("description");
                        String endIso = o.optString("endOfVotingTime");
                        String localDate = isoDate != null && isoDate.length() >= 10 ? isoDate.substring(0, 10) : "";
                        db.upsertSessionFromServer(serverId, localDate, name, desc, endIso);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> loadSessionsForDateInternal(date));
                }
            } catch (Exception ignored) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> loadSessionsForDateInternal(date));
                }
            }
        }).start();
    }

    private void loadSessionsForDateInternal(String date) {
        // read sessions from DB and show in list
        List<Session> sessions = new ArrayList<>();
        // we will query DB directly
        android.database.sqlite.SQLiteDatabase rdb = db.getReadableDatabase();
        android.database.Cursor c = rdb.query(DatabaseHelper.TABLE_SESSIONS,
                new String[]{DatabaseHelper.S_COL_ID, DatabaseHelper.S_COL_DATE, DatabaseHelper.S_COL_NAME, DatabaseHelper.S_COL_DESC, DatabaseHelper.S_COL_END},
                DatabaseHelper.S_COL_DATE + "=?",
                new String[]{date}, null, null, null);
        if (c.moveToFirst()) {
            do {
                Session s = new Session(c.getString(1), c.getString(2), c.getString(3), c.getString(4));
                sessions.add(s);
            } while (c.moveToNext());
        }
        c.close();
        SessionAdapter adapter = new SessionAdapter(getContext(), sessions);
        listSessions.setAdapter(adapter);
        listSessions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Session s = (Session) adapter.getItem(position);
                // Decide target based on hosting activity type: Admin -> Results, Student -> Decide
                android.content.Context ctx = getActivity();
                if (ctx instanceof AdminActivity) {
                    android.content.Intent i = new android.content.Intent(getContext(), ResultsActivity.class);
                    i.putExtra("session_name", s.getSessionName());
                    i.putExtra("session_date", s.getDate());
                    startActivity(i);
                } else {
                    android.content.Intent i = new android.content.Intent(getContext(), DecideActivity.class);
                    i.putExtra("session_name", s.getSessionName());
                    i.putExtra("session_date", s.getDate());
                    startActivity(i);
                }
            }
        });
        emptyView.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddSessionDialog() {
        // simple dialog with fields: name, description, end_of_voting (as text datetime)
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_session, null);
        final EditText edtName = dialogView.findViewById(R.id.edtSessionName);
        final EditText edtDesc = dialogView.findViewById(R.id.edtSessionDesc);
        final EditText edtEnd = dialogView.findViewById(R.id.edtSessionEnd);

        new AlertDialog.Builder(getContext())
                .setTitle("Add session for " + selectedDate)
                .setView(dialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = edtName.getText().toString().trim();
                        String desc = edtDesc.getText().toString().trim();
                        String end = edtEnd.getText().toString().trim();
                        if (name.isEmpty() || end.isEmpty()) {
                            Toast.makeText(getContext(), "Enter name and end time", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // check duplicate (same date + name)
                        android.database.sqlite.SQLiteDatabase rdb = db.getReadableDatabase();
                        android.database.Cursor c = rdb.query(DatabaseHelper.TABLE_SESSIONS,
                                new String[]{DatabaseHelper.S_COL_ID},
                                DatabaseHelper.S_COL_DATE + "=? AND " + DatabaseHelper.S_COL_NAME + "=?",
                                new String[]{selectedDate, name}, null, null, null);
                        boolean exists = c.getCount() > 0;
                        c.close();
                        if (exists) {
                            Toast.makeText(getContext(), "Session with same name already exists on this date", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // basic check: end string should be after date (simple lexicographic check with yyyy-MM-dd HH:mm)
                        // send to server then insert locally with server_id
                        new Thread(() -> {
                            try {
                                String startIso = selectedDate + "T10:00:00Z";
                                String endIso;
                                if (end.contains("T")) {
                                    String tmp = end.endsWith("Z") ? end : (end + "Z");
                                    endIso = tmp.replaceFirst("\\.\\d{3}Z$", "Z");
                                } else {
                                    String time = end.length() == 5 ? (end + ":00") : end;
                                    endIso = selectedDate + "T" + time + "Z";
                                }
                                org.json.JSONObject body = new org.json.JSONObject();
                                body.put("date", startIso);
                                body.put("sessionName", name);
                                body.put("description", desc);
                                body.put("endOfVotingTime", endIso);
                                HttpHelper helper = new HttpHelper();
                                StringBuilder resp = new StringBuilder();
                                new HttpThread(helper, CommandEnum.POST_SESSION, body.toString(), resp).run();
                                String json = resp.toString();
                                String serverId = null;
                                if (!json.isEmpty()) {
                                    org.json.JSONObject res = new org.json.JSONObject(json);
                                    org.json.JSONObject sessionObj = res.optJSONObject("session");
                                    serverId = sessionObj != null ? sessionObj.optString("_id") : null;
                                }
                                db.upsertSessionFromServer(serverId, selectedDate, name, desc, endIso);
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "Session saved", Toast.LENGTH_SHORT).show();
                                        loadSessionsForDate(selectedDate);
                                    });
                                }
                            } catch (Exception e) {
                                android.util.Log.e("SessionCreate", "Failed to sync with server", e);
                                if (getActivity() != null) {
                                    String msg = e.getMessage();
                                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to sync with server: " + (msg != null ? msg : ""), Toast.LENGTH_LONG).show());
                                }
                            }
                        }).start();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
