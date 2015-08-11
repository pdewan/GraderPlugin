package com.unc.cs.graderprogramplugin.com;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Andrew Vitkus
 *
 */
public class OnyenAuthenticator implements AutoCloseable {

	private HttpsURLConnection grader;
	private final URL GRADER_URL = new URL("https", "onyen.unc.edu", 443, "/cgi-bin/unc_id/authenticator.pl");
	private boolean isConnected;
	
	private OnyenAuthenticator() throws IOException {
//		grader = (HttpsURLConnection) GRADER_URL.openConnection();
//		isConnected = true;
	}
	
	public static OnyenAuthenticator instanceOf() throws IOException {
		return new OnyenAuthenticator();
	}

	public String authenticate(String username, String password) throws IOException {
		return getVFYKey(username, password);
	}
	
	private void setup() throws IOException {
		if (isConnected) {
			grader.disconnect();
		}
		grader = (HttpsURLConnection) GRADER_URL.openConnection();
		isConnected = true;
	}
	
	private String getFormHTML() throws MalformedURLException, IOException {
		setup();
		grader.setRequestMethod("POST");
		grader.setRequestProperty("getpid", "pid");
		grader.setRequestProperty("getpid", "givenName");
		grader.setRequestProperty("getpid", "sn");
		
		StringBuilder post = new StringBuilder();
		post.append("getpid=").append("pid");
		post.append("&getpid=").append("givenName");
		post.append("&getpid=").append("sn");

		grader.setDoOutput(true);
		grader.setDoInput(true);
		grader.getOutputStream().write(post.toString().getBytes());
		
		return getResponse(grader.getInputStream());
	}
	
	private String getVFYKey(String username, String password) throws IOException {
		String html = getFormHTML();
		setup();
		grader.setRequestMethod("POST");
		
		//System.out.println(html);

		String cookie = getCookieValue(html);
		String point = getPointValue(html);
		grader.setRequestProperty("_COOKIE_", cookie);
		grader.setRequestProperty("_POINT_", point);
		grader.setRequestProperty("onyen", username);
		grader.setRequestProperty("pw", password);
		grader.setRequestProperty("getpid", "pid");
		grader.setRequestProperty("getpid", "givenName");
		grader.setRequestProperty("getpid", "sn");
		grader.setRequestProperty("submit", "Continue");
		
		StringBuilder post = new StringBuilder();
		post.append("_COOKIE_=").append(cookie);
		post.append("&_POINT_=").append(point);
		post.append("&onyen=").append(username);
		post.append("&pw=").append(password);
		post.append("&getpid=").append("pid");
		post.append("&getpid=").append("givenName");
		post.append("&getpid=").append("sn");
		post.append("&submit=").append("Continue");
		
		//System.out.println(post.toString());
		
		grader.setDoOutput(true);
		grader.setDoInput(true);
		grader.getOutputStream().write(post.toString().getBytes());

		html = getResponse(grader.getInputStream());
		//System.out.println(html);
		return getVFYKeyValue(html);
	}
	
	private void disconnect() {
		grader.disconnect();
		isConnected = false;
	}
	
	public boolean isConnected() {
		return isConnected;
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
	
	private static String getResponse(InputStream is) throws IOException {
		byte[] bytes = new byte[512];
		try (BufferedInputStream bis = new BufferedInputStream(is)){
			StringBuilder response = new StringBuilder();
			int in = -1;
			while((in = bis.read(bytes)) != -1) {
				response.append(new String(bytes, 0, in));
			}
			return response.toString();
		}
	}

	@Override
	public void close() throws Exception {
		disconnect();
	}
}
