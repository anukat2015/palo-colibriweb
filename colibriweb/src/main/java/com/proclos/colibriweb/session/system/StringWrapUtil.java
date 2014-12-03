package com.proclos.colibriweb.session.system;


public class StringWrapUtil {
	
	public static String wrapString(String string, int distance) {
		if (string != null && string.length() > distance) {
			char sep = '\u263A';
			string = string.replace(" ", " "+sep);
			string = string.replace("-", "-"+sep);
			string = string.replace(":", ":"+sep);
			string = string.replace(",", ","+sep);
			string = string.replace("/", "/"+sep);
			String[] parts = string.split(String.valueOf(sep));
			StringBuffer result = new StringBuffer();
			for (String p : parts) {
				if (p.length() > distance) {
					StringBuffer buffer = new StringBuffer(p);
					for (int i=0; i<buffer.length(); i+=distance+6) {
						buffer.insert(i, "<wbr/>");
					}
					p = buffer.toString();
				}
				result.append(p);
				if (p.endsWith(":") || p.endsWith(",") || p.endsWith("/")) result.append("<wbr/>");
			}
			return result.toString();
		}
		return string;
	}

}
