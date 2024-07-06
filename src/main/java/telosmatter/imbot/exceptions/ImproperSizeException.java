package telosmatter.imbot.exceptions;

public class ImproperSizeException extends RuntimeException {
	
	public ImproperSizeException() {
		super ("The passed images do not comply to the specifications");
	}

}

