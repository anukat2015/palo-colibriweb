package com.proclos.colibriweb.session.system;

import java.io.File;
import java.net.URL;

public class FileUtil {
	
	public static boolean isURL(String filename) {
		try {
			new URL(filename);
		}	
		catch (Exception e) {
			return false;
		}
		return true;
	}	
		
	public static boolean isRelativ(String filename) {
		if (filename != null) {			
			if (isURL(filename)) {
				return false;
			}	
			else {
				File f = new File(filename);
				return  ! f.isAbsolute();
			}
		}
		return true;
	}


}
