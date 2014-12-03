package at.adaptive.components.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for various file-operations
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class FileUtil
{
	/**
	 * Returns the extension of a specfied file name
	 * 
	 * @param fileName
	 *            the name of the file
	 * @return the extension of the specified file name or <code>null</code> if no extension is specified
	 */
	public static String getFileExtension(String fileName)
	{
		if(fileName.contains("."))
		{
			return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase().trim();
		}
		return null;
	}

	/**
	 * Reads a file into a byte array
	 * 
	 * @param file
	 *            the file
	 * @return the byte array
	 * @throws IOException
	 *             on error
	 */
	public static byte[] getBytesFromFile(File file) throws IOException
	{
		InputStream is = new FileInputStream(file);
		// Get the size of the file
		long length = file.length();
		if(length > Integer.MAX_VALUE)
		{
			// File is too large
		}
		// Create the byte array to hold the data
		byte[] bytes = new byte[(int)length];
		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while(offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
		{
			offset += numRead;
		}
		// Ensure all the bytes have been read in
		if(offset < bytes.length)
		{
			throw new IOException("Could not completely read file " + file.getName());
		}
		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	public static String getSimpleFileName(String fileName)
	{
		if(fileName == null)
		{
			return null;
		}
		int index = fileName.lastIndexOf("/");
		if(index == -1)
		{
			index = fileName.lastIndexOf("\\");
		}
		if(index != -1)
		{
			return fileName.substring(index + 1);
		}
		return fileName;
	}
	
	/**
	 * Removes the extension of the given FileName("test.pdf" -> "test")
	 * @param fileName
	 * the name of the file
	 * @return
	 * the filename without extension
	 */
	public static String removeFileExtension(String fileName) {
		if(fileName.contains("."))
			return fileName.substring(0, fileName.lastIndexOf('.'));
		
		return fileName;
	}
}
