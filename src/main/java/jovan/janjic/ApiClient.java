package jovan.janjic;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiClient {

	private static final String TAG = "ApiClient";
	private static final String BASE_URL = "http://10.0.2.2:8080/api"; // emulator to host

	public static JSONArray getSessions() throws IOException, JSONException {
		String response = httpGetWithClearError(BASE_URL + "/sessions");
		return new JSONArray(response);
	}

	public static JSONObject createSession(String dateIso, String name, String description, String endIso) throws IOException, JSONException {
		String url = BASE_URL + "/session";
		JSONObject body = new JSONObject();
		body.put("date", dateIso);
		body.put("sessionName", name);
		body.put("description", description);
		body.put("endOfVotingTime", endIso);
		String response = httpPostWithClearError(url, body.toString());
		Log.d(TAG, "createSession response: " + response);
		return new JSONObject(response);
	}

	public static JSONArray getVotes(String sessionId) throws IOException, JSONException {
		String response = httpGetWithClearError(BASE_URL + "/votes?sessionId=" + sessionId);
		return new JSONArray(response);
	}

	public static JSONObject postVote(String sessionId, String vote) throws IOException, JSONException {
		String url = BASE_URL + "/results/vote";
		JSONObject body = new JSONObject();
		body.put("sessionId", sessionId);
		body.put("vote", vote);
		String response = httpPostWithClearError(url, body.toString());
		Log.d(TAG, "postVote response: " + response);
		return new JSONObject(response);
	}

	private static String httpGetWithClearError(String url) throws IOException {
		try {
			return httpGet(url);
		} catch (IOException e) {
			throw new IOException("GET " + url + " failed: " + e.getMessage(), e);
		}
	}

	private static String httpPostWithClearError(String url, String body) throws IOException {
		try {
			return httpPost(url, body);
		} catch (IOException e) {
			throw new IOException("POST " + url + " failed: " + e.getMessage(), e);
		}
	}

	private static String httpGet(String urlString) throws IOException {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestProperty("Accept", "application/json");
			int code = conn.getResponseCode();
			InputStream in = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
			String text = readAll(in);
			if (code < 200 || code >= 300) {
				throw new IOException("GET " + urlString + " failed: " + code + " => " + text);
			}
			return text;
		} finally {
			if (conn != null) conn.disconnect();
		}
	}

	private static String httpPost(String urlString, String jsonBody) throws IOException {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(true);
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
			writer.write(jsonBody);
			writer.flush();
			writer.close();
			os.close();
			int code = conn.getResponseCode();
			InputStream in = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
			String text = readAll(in);
			if (code < 200 || code >= 300) {
				throw new IOException("POST " + urlString + " failed: " + code + " => " + text);
			}
			return text;
		} finally {
			if (conn != null) conn.disconnect();
		}
	}

	private static String readAll(InputStream in) throws IOException {
		if (in == null) return "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		reader.close();
		return sb.toString();
	}
}


