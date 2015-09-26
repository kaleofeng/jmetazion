package com.metazion.jm.util;

public class FileUtil {

	public static String getAbsolutePath(String relativePath) {
		return String.format("%s/%s", System.getProperty("user.dir").replace("\\", "/"), relativePath);
	}
}