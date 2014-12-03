package at.adaptive.components.datahandling.dataimport;

public class ImportException extends Exception
{
	private static final long serialVersionUID = -8787659610198817317L;

	public ImportException(String message)
	{
		super(message);
	}

	public ImportException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
