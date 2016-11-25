package tab.exception;

public class TabNotOpen extends TabException {

	private static final long serialVersionUID = -5780433117540134236L;

	public TabNotOpen() {
		super();
	}

	public TabNotOpen(String message, Throwable cause) {
		super(message, cause);
	}

	public TabNotOpen(String message) {
		super(message);
	}

	public TabNotOpen(Throwable cause) {
		super(cause);
	}

}
