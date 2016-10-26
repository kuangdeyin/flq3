package com.ifenglian.module.update;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {
	public static final String ENCODING_UTF_8 = "utf-8";

	public static String httpUrlConnection(String urlStr,String content,String method) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			httpConn.setUseCaches(false);
			httpConn.setRequestMethod(method);
			httpConn.setReadTimeout(5000);
			httpConn.setConnectTimeout(6000);
			if (content != null) {
				byte[] contents = content.getBytes();
				httpConn.setRequestProperty("Content-length", "" + contents.length);
				OutputStream outputStream = httpConn.getOutputStream();
				outputStream.write(contents);
				outputStream.close();
			}
			int responseCode = httpConn.getResponseCode();
			if (HttpURLConnection.HTTP_OK == responseCode) {
				StringBuffer sb = new StringBuffer();
				String readLine;
				BufferedReader responseReader;
				responseReader = new BufferedReader(new InputStreamReader(
						httpConn.getInputStream(), ENCODING_UTF_8));
				while ((readLine = responseReader.readLine()) != null) {
					sb.append(readLine);
				}
				responseReader.close();
				return sb.toString();
			}
		} catch (Exception ex) {
			System.out.println("http request error " + ex.getLocalizedMessage());
		}
		return null;
	}

	private byte[] readTextFile(InputStream inputStream) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte buf[] = new byte[1024];
		int len;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, len);
			}
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputStream.toByteArray();
	}
}
