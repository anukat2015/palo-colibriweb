package at.adaptive.components.documentmanagement.reader;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

public class DocumentReader
{
	private static final Logger logger = Logger.getLogger(DocumentReader.class);

	public String read(InputStream in)
	{
		try
		{
			Tika tika = new Tika();
			tika.setMaxStringLength(Integer.MAX_VALUE);
			Metadata metadata = new Metadata();
			return tika.parseToString(in, metadata);
		}
		catch(Exception e)
		{
			logger.error("Error reading document", e);
			return null;
		}
	}

	// public static void main(String[] args)
	// {
	// try
	// {
	// FileInputStream in = new FileInputStream(new File("c:\\test.pptx"));
	// System.err.println(new DocumentReader().read(in));
	// }
	// catch(Exception e)
	// {
	// e.printStackTrace();
	// }
	// }
}
