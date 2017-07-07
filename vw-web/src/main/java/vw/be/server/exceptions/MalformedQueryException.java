package vw.be.server.exceptions;

public class MalformedQueryException extends BadRequestException {

	private static final long serialVersionUID = 6944004793665251714L;

	public MalformedQueryException(String msg) {
		super(msg);
	}

	public MalformedQueryException(String msg, Throwable cause) {
		super(msg, cause);
	}

	@Override
	public String getMessage() {
		Throwable cause = getCause();
		String msg = super.getMessage();
		if (cause != null)
			msg = String.format("%s\n%s", msg, cause.getMessage());
		return msg;
	}
}
