package at.adaptive.components.documentmanagement.exception;

public class DocumentReadException extends Exception
{
	private static final long serialVersionUID = -2574574030711145979L;

	public DocumentReadException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DocumentReadException(String message)
	{
		super(message);
	}
}
