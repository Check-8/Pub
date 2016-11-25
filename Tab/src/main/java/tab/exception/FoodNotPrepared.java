package tab.exception;

public class FoodNotPrepared extends TabException {

	private static final long serialVersionUID = -9125157856344068140L;

	public FoodNotPrepared() {
		super();
	}

	public FoodNotPrepared(String message, Throwable cause) {
		super(message, cause);
	}

	public FoodNotPrepared(String message) {
		super(message);
	}

	public FoodNotPrepared(Throwable cause) {
		super(cause);
	}

}
