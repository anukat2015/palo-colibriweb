package at.adaptive.components.datahandling.dataexport;

public class ExportException extends Exception
{
	private static final long serialVersionUID = -8787659610198817317L;

	public ExportException(String message)
	{
		super(message);
	}

	public ExportException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
