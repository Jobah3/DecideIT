package jovan.janjic;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


class Converter {

	String extractSessionId(String jsonString) {
		try {
			JSONObject obj = new JSONObject(jsonString);
			JSONObject session = obj.getJSONObject("session");
			return session.getString("_id");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	Votes votesConverter(String jsonString)
	{
		if (jsonString == null || jsonString.isEmpty()) return null;

		try {
			JSONArray jsonArray = new JSONArray(jsonString);
			JSONObject obj = jsonArray.getJSONObject(0);
			String sessionId = obj.getString("sessionId");
			String sessionName = obj.getString("sessionName");
			String sessionDate = obj.getString("sessionDate");
			int yes = obj.getInt("yes");
			int no = obj.getInt("no");
			int abstain = obj.getInt("abstain");

			return new Votes(yes, no, abstain, sessionName, sessionDate);

		} catch (Exception e) {
			Log.e("HttpHelper", "JSON parsing failed: " + e.getMessage());
			return null;
		}
	}

	List<Session> parseSessions(String jsonString) {
		List<Session> sessions = new ArrayList<>();

		Log.d("Pomoc", jsonString);


		try {
			JSONArray jsonArray = new JSONArray(jsonString);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);

				String date = obj.getString("date");
				String sessionName = obj.getString("sessionName");
				String description = obj.getString("description");
				String endOfVotingTime = obj.getString("endOfVotingTime");
				String sessionId = obj.getString("_id");

				String realDate = toSlashDate(date);
				String realEndOfVotingTime = toDotDate(endOfVotingTime);

				Session session = new Session(realDate, sessionName, description, realEndOfVotingTime);
				sessions.add(session);
			}

		} catch (Exception e) {
			Log.e("HttpHelper", "Parsing sessions failed: " + e.getMessage());
		}

		return sessions;
	}

	String toSlashDate(String isoDate) {
		try {
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date date = isoFormat.parse(isoDate);

			SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
			return outputFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	String toDotDate(String isoDate) {
		try {
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date date = isoFormat.parse(isoDate);

			SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault());
			return outputFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	String convertSlashDate(String inputDate) {
		try {
			SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
			Date date = inputFormat.parse(inputDate);

			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			return isoFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	String convertDotDate(String inputDate) {
		try {
			SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault());
			Date date = inputFormat.parse(inputDate);

			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			return isoFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}



