package vw.be.server.exceptions;

public class BadRequestException extends Exception {

	private static final long serialVersionUID = 1L;

	public BadRequestException(String string) {
		super(string);
	}

	public BadRequestException(String string, Throwable cause) {
		super(string, cause);
	}
}
