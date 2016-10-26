package com.ifenglian.module.update;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.Environment;


public class FileUtils {
	private static String SDPATH; // 


	public static String getSDPATH() {
		if(SDPATH == null){
			SDPATH = Environment.getExternalStorageDirectory() + "/";
		}
		return SDPATH;
	}

	public void setSDPATH(String sDPATH) {
		SDPATH = sDPATH;
	}

	/**
	 * 
	 */
	public FileUtils() {
		// 
		SDPATH = Environment.getExternalStorageDirectory() + "/";
	}

	/**
	 * 
	 * 
	 * @throws IOException
	 */
	public static File createSDFile(String fileName) throws IOException {
		File file = new File(fileName);
		file.createNewFile();
		return file;
	}

	/**
	 * create dir
	 */
	public static boolean createSDDir(String dirName) {
		File dir = new File(dirName);
		if (!dir.exists()) {
		    return dir.mkdirs();
        }
		return true;
	}

	/**
	 * file exist
	 */
	public static boolean isFileExist(String fileName) {
		File file = new File(fileName);
		if(file.isFile())
			return file.exists();
		return false;
	}
	/**
	 * folder exist
	 */
	public static boolean isFolderExist(String fileName) {
		File file = new File(fileName);
		if(file.isDirectory())
			return file.exists();
		return false;
	}


	public static File write2FileFromInput(String path, String fileName,
			InputStream inputStream) {
		File file = null;
		OutputStream output = null;
		try {
			file = createSDFile(path +"/"+ fileName);
			output = new FileOutputStream(file);
			byte buffer[] = new byte[1024];
			int len;
			while ((len = inputStream.read(buffer)) > 0) {
				output.write(buffer,0,len);
			}
			output.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
			    if (output != null) {
			        output.close();
                }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return file;
	}
	public static File write2FileFromInput(String path, String fileName,
            byte[] data) {
        File file = null;
        OutputStream output = null;
        try {
            file = createSDFile(path +"/"+ fileName);
            output = new FileOutputStream(file);
            output.write(data);
            output.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }
}
