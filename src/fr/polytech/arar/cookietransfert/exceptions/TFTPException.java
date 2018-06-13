package fr.polytech.arar.cookietransfert.exceptions;

public class TFTPException extends RuntimeException {
	
	public TFTPException(String message, Exception innerException) {
		super(message, innerException);
	}
	public TFTPException(String message) {
		super(message);
	}
	public TFTPException() {
		super();
	}
}
