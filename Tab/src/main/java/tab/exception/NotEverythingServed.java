package tab.exception;

public class NotEverythingServed extends TabException {

	private static final long serialVersionUID = 540596476687090051L;

	public NotEverythingServed() {
		super();
	}

	public NotEverythingServed(String message, Throwable cause) {
		super(message, cause);
	}

	public NotEverythingServed(String message) {
		super(message);
	}

	public NotEverythingServed(Throwable cause) {
		super(cause);
	}

}
