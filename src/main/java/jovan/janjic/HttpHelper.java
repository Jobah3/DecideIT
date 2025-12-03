package jovan.janjic;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

class HttpHelper {

	private static final String BASE_URL = "http://10.0.2.2:8080/api";

	String sendGetRequest(String endpoint) {
		StringBuilder response = new StringBuilder();
		HttpURLConnection conn = null;
		Log.d("Send get" , "End point is " + endpoint);

		try {
			URL url = new URL(BASE_URL + endpoint);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(conn.getInputStream())
			);
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();

		} catch (Exception e) {
			Log.e("HttpHelper", "GET request failed: " + e.getMessage());
		} finally {
			if (conn != null) conn.disconnect();
		}

		Log.d("HttpHelper ", response.toString());
		return response.toString();
	}

	String sendPostRequest(String endpoint, String jsonBody) {
		Log.d("JsonBody", jsonBody);

		StringBuilder response = new StringBuilder();
		HttpURLConnection conn = null;
		try {
			URL url = new URL(BASE_URL + endpoint);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
			writer.write(jsonBody);
			writer.flush();
			writer.close();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(conn.getInputStream())
			);
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();

		} catch (Exception e) {
			Log.e("HttpHelper", "POST request failed: " + e.getMessage());
		} finally {
			if (conn != null) conn.disconnect();
		}


		return response.toString();
	}

	String getSessions() {
		return sendGetRequest("/sessions");
	}

	String postSession(String jsonBody) {
		return sendPostRequest("/session", jsonBody);
	}

	String getVotes(String sessionId) {
		return sendGetRequest("/votes?sessionId=" + sessionId);
	}

	String postVote(String jsonBody) {
		return sendPostRequest("/results/vote", jsonBody);
	}
}



