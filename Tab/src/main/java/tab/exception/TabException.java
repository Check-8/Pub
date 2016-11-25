package tab.exception;

public class TabException extends RuntimeException {

	private static final long serialVersionUID = 4306852546727353913L;

	public TabException() {
		super();
	}

	public TabException(String message, Throwable cause) {
		super(message, cause);
	}

	public TabException(String message) {
		super(message);
	}

	public TabException(Throwable cause) {
		super(cause);
	}

}
