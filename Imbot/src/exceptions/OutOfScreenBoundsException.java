package exceptions;

public class OutOfScreenBoundsException extends RuntimeException {
	
	public OutOfScreenBoundsException(int x, int y) {
		super("The passed location (" +x +"," +y +") is out of the screen bounds.");
	}

}
