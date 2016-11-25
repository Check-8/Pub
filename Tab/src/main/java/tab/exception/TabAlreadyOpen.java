package tab.exception;

public class TabAlreadyOpen extends TabException {

	private static final long serialVersionUID = 8890753940628581680L;

	public TabAlreadyOpen() {
		super();
	}

	public TabAlreadyOpen(String message, Throwable cause) {
		super(message, cause);
	}

	public TabAlreadyOpen(String message) {
		super(message);
	}

	public TabAlreadyOpen(Throwable cause) {
		super(cause);
	}

}
