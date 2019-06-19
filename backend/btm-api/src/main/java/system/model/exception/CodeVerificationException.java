package system.model.exception;

public class CodeVerificationException extends Exception {

	public CodeVerificationException(String message)
	{
		super("CodeVerificationException-"+message);
	}

	public CodeVerificationException(String message, Throwable cause)
	{
		super("CodeVerificationException-"+message,cause);
	}
	
}
