package edu.sjsu.cinequest.comm;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionHelper {
	public static HttpURLConnection open(String url)  throws IOException {
		return (HttpURLConnection) new URL(url).openConnection();		
	}
	
	public static byte[] getBytes(HttpURLConnection connection)
			throws IOException {
		InputStream inputStream = connection.getInputStream();
		byte[] responseData = new byte[10000];
		int length = 0;
		try {
			int count;
			while (-1 != (count = inputStream.read(responseData, length,
					responseData.length - length))) {
				length += count;
				if (length == responseData.length) {
					byte[] newData = new byte[2 * responseData.length];
					System.arraycopy(responseData, 0, newData, 0, length);
					responseData = newData;
				}
			}
		} finally {
			connection.disconnect();
		}
		byte[] response = new byte[length];
		System.arraycopy(responseData, 0, response, 0, length);
		return response;
	}
}
