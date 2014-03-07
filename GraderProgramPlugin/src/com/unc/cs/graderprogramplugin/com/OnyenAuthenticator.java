package com.unc.cs.graderprogramplugin.com;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Andrew Vitkus
 *
 */
public class OnyenAuthenticator {

	private static HttpsURLConnection grader;

	public static String authenticate(String username, String password) {
		try {
			return getVFYKey(username, password);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		} finally {
			if (grader != null) {
				disconnect();
			}
		}
	}
	
	private static String getFormHTML() throws MalformedURLException, IOException {
		connect();
		grader.setRequestMethod("POST");
		grader.setDoOutput(true);
		grader.setDoInput(true);
		grader.getOutputStream().write(42);
		
		return getResponse();
	}
	
	private static String getVFYKey(String username, String password) throws IOException {
		String html = getFormHTML();
		connect();
		grader.setRequestMethod("POST");
		
		//System.out.println(html);

		String cookie = getCookieValue(html);
		String point = getPointValue(html);
		
		grader.setRequestProperty("_COOKIE_", cookie);
		grader.setRequestProperty("_POINT_", point);
		grader.setRequestProperty("onyen", username);
		grader.setRequestProperty("pw", password);
		grader.setRequestProperty("submit", "Continue");
		
		StringBuilder post = new StringBuilder();
		post.append("_COOKIE_=").append(cookie);
		post.append("&_POINT_=").append(point);
		post.append("&onyen=").append(username);
		post.append("&pw=").append(password);
		post.append("&submit=").append("Continue");
		
		//System.out.println(post.toString());
		
		grader.setDoOutput(true);
		grader.setDoInput(true);
		grader.getOutputStream().write(post.toString().getBytes());

		html = getResponse();
		//System.out.println(html);
		return getVFYKeyValue(html);
	}

	private static void connect() throws MalformedURLException, IOException {
		URL graderURL = new URL("https", "onyen.unc.edu", 443, "/cgi-bin/unc_id/authenticator.pl");
		grader = (HttpsURLConnection) graderURL.openConnection();
	}

	private static void disconnect() {
		grader.disconnect();
	}
	
	private static String getValueFromName(String name, String html) {
		int loc = html.indexOf(name);
		int start = html.indexOf("\"", loc + 10) + 1;
		int end = html.indexOf("\"", start);
		String value = html.substring(start, end);
		//System.out.println(start + "-" + end + ": " + value);
		return value;
	}
	
	private static String getCookieValue(String html) {
		return getValueFromName("_COOKIE_", html);
	}
	
	private static String getPointValue(String html) {
		return getValueFromName("_POINT_", html);
	}

	private static String getVFYKeyValue(String html) {
		return getValueFromName("vfykey", html);
	}
	
	private static String getResponse() throws IOException {
		byte[] bytes = new byte[512];
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(grader.getInputStream());
			StringBuilder response = new StringBuilder();
			int in = -1;
			while((in = bis.read(bytes)) != -1) {
				response.append(new String(bytes, 0, in));
			}
			return response.toString();
		} finally {
			if (bis != null) {
				bis.close();
			}
		}
	}
}
