package tab.exception;

public class DrinksNotOutstanding extends TabException {

	private static final long serialVersionUID = -169221099809091244L;

	public DrinksNotOutstanding() {
		super();
	}

	public DrinksNotOutstanding(String message, Throwable cause) {
		super(message, cause);
	}

	public DrinksNotOutstanding(String message) {
		super(message);
	}

	public DrinksNotOutstanding(Throwable cause) {
		super(cause);
	}

}
