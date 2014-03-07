package com.unc.cs.graderprogramplugin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 
 * @author Andrew Vitkus
 */
public class ZipWriter {
	
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	private static int BUFFER_SIZE = 4096;
	
	public static void zip(File toZip, File target) throws FileNotFoundException, IOException {
		ZipOutputStream zipStream = null;
		try {
			if(!target.exists()) {
				target.createNewFile();
			}
			zipStream = new ZipOutputStream(new FileOutputStream(target));
			zipStream.setLevel(Deflater.BEST_COMPRESSION);
			
			if (toZip.isDirectory()) {
				for(File next : toZip.listFiles()) {
					addToZip(next, "", zipStream);
				}
			} else {
				addToZip(toZip, "", zipStream);
			}
			
			zipStream.flush();
		} finally {
			if (zipStream != null) {
				zipStream.close();
			}
		}
	}
	
	private static void addToZip(File next, String parent, ZipOutputStream zipStream) throws IOException {
		if(next.isDirectory()) {
			addFolder(next, parent, zipStream);
		} else {
			addFile(next, parent, zipStream);
		}
	}
	
	private static void addFolder(File folder, String parent, ZipOutputStream zipStream) throws IOException {
		if (parent != "") {
			parent += FILE_SEPARATOR;
		}
		for(File next : folder.listFiles()) {
			addToZip(next, parent + folder.getName(), zipStream);
		}
	}

	private static void addFile(File file, String parent, ZipOutputStream zipStream) throws IOException {
		if (parent != "") {
			parent += FILE_SEPARATOR;
		}
		ZipEntry entry = new ZipEntry(parent + file.getName());
		zipStream.putNextEntry(entry);
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			
			byte[] data = new byte[BUFFER_SIZE];
	        int bytesRead = -1;
	        while ((bytesRead = fis.read(data)) != -1) {
	        	zipStream.write(data, 0, bytesRead);
	        }
	        
			zipStream.flush();
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}
}
