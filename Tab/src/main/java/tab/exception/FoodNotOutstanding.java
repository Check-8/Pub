package tab.exception;

public class FoodNotOutstanding extends TabException {

	private static final long serialVersionUID = 1471520676830377297L;

	public FoodNotOutstanding() {
		super();
	}

	public FoodNotOutstanding(String message, Throwable cause) {
		super(message, cause);
	}

	public FoodNotOutstanding(String message) {
		super(message);
	}

	public FoodNotOutstanding(Throwable cause) {
		super(cause);
	}

}
