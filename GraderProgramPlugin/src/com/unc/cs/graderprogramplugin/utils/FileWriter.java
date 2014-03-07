package com.unc.cs.graderprogramplugin.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Andrew Vitkus
 *
 */
public class FileWriter {
	public static void write(String data, File file) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(data.getBytes("UTF-8"));
		fos.flush();
		fos.close();
	}
}
