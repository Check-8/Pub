package tab.exception;

public class NotEnough extends TabException {

	private static final long serialVersionUID = -73823798743009556L;

	public NotEnough() {
		super();
	}

	public NotEnough(String message, Throwable cause) {
		super(message, cause);
	}

	public NotEnough(String message) {
		super(message);
	}

	public NotEnough(Throwable cause) {
		super(cause);
	}

}
