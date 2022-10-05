package exceptions;

public class AlreadyInitException extends RuntimeException {
	
	public AlreadyInitException() {
		super ("Robot has already been initialized!");
	}

}
