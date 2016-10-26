package com.ifenglian.module.update;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpDownLoader {
	private URL url;
	private static String TAG = HttpDownLoader.class.getCanonicalName();

	/**
	 * 
	 * 
	 * @param urlStr
	 * @return
	 */
	public String download(String urlStr) {
		StringBuffer sb = new StringBuffer();
		String line = null;
		BufferedReader buffer = null;
		try {
			//
			url = new URL(urlStr);
			//
			HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
			//
			buffer = new BufferedReader(new InputStreamReader(
					urlcon.getInputStream()));
			//
			while ((line = buffer.readLine()) != null) {
				sb.append(line);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				buffer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * download file
	 * 
	 * @param urlStr
	 * @param path
	 * @param fileName
	 * @return -1:error 1:ok 0: exist
	 */
	public int downFile(String urlStr, String path, String fileName) {
		Log.i(TAG, urlStr);
		int result = 1;
		InputStream inputStream = null;
		try {
			if (!FileUtils.isFolderExist(path))
				FileUtils.createSDDir(path);
			if (FileUtils.isFileExist(path + "/" + fileName)) {
				result = 0;
			} else {
				inputStream = getInputStreamFromUrl(urlStr);
				File resultFile = FileUtils.write2FileFromInput(path, fileName,
						inputStream);
				if (resultFile == null) {
					result = -1;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			result = -1;
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;

	}

	public static int loadRmoteFile(String url, String path, String fileName, boolean replace) {
		int result = 1;
		byte[] imgData = null;
		URL fileURL = null;
		InputStream is = null;
		try {
			fileURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) fileURL
					.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.setReadTimeout(10 * 1000);

			conn.setDoInput(true);
			conn.connect();
			is = conn.getInputStream();
			int length = conn.getContentLength();
			if (length != -1) {
				imgData = new byte[length];
				byte[] buffer = new byte[512];
				int readLen = 0;
				int destPos = 0;
				while ((readLen = is.read(buffer)) > 0) {
					System.arraycopy(buffer, 0, imgData, destPos, readLen);
					destPos += readLen;
				}
				if (!FileUtils.createSDDir(path)) {
					result = -1;
				}else {
					if (FileUtils.isFileExist(path + "/" + fileName) && !replace) {
						result = 0;
					} else {
						File resultFile = FileUtils.write2FileFromInput(path, fileName, imgData);
						if (resultFile == null) {
							result = -1;
						}
					}
				}
				
			}
		} catch (Exception e) {
			result = -1;
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;

	}

	/**
	 * 
	 * 
	 * @param urlStr
	 * @return
	 * @throws IOException
	 */
	public InputStream getInputStreamFromUrl(String urlStr) throws IOException {
		url = new URL(urlStr);
		HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
		InputStream inputStream = urlCon.getInputStream();
		return inputStream;
	}
}
