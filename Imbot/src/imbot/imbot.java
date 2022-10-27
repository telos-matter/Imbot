package imbot;

import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import exceptions.AlreadyInitException;
import exceptions.OutOfScreenBoundsException;

/**
 * <p>A class that facilitates and adds features to the already existing {@link java.awt.Robot}.
 * <p>imbot is intentionally not capitalized (opposing to the class naming convention) to facilitate its use.
 * <br>
 * <br>
 * <a href="https://github.com/telos-matter/Imbot"> Github link</a> 
 * @author telos_matter
 */
public class imbot {

	private static final int LEFT_BUTTON = InputEvent.getMaskForButton(1);
	private static final int RIGHT_BUTTON = InputEvent.getMaskForButton(3);
	
	public static final int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
	public static final int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
	public static final Rectangle SCREEN_RECTANGLE = new Rectangle (0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
	
	private static Robot robot = null;
	private static Point last_location = new Point (-1, -1);
	private static boolean exit_int = false;
	
	/**
	 * Shorthand for {@link #init(int, boolean)}
	 * with the default values of 0 and true
	 */
	public static void init () {
		init (0, true);
	}
	
	/**
	 * Initialize imbot with the given parameters. imbot
	 * should always be initialized before use.
	 * 
	 * @param ms	Amount of milliseconds to sleep after every event
	 * @param exit_on_interruption	Bool to whether or not exit the script
	 * if the user is interrupting by moving the mouse
	 * @throws AlreadyInitException if imbot has already been initialized
	 */
	public static void init (int ms, boolean exit_on_interruption) {
		if (robot == null) {
			try {
				robot = new Robot (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
				robot.setAutoDelay(ms);	
				robot.setAutoWaitForIdle(true);
				robot.mouseMove(SCREEN_WIDTH/2, SCREEN_HEIGHT/2);
				
				last_location.setLocation(SCREEN_WIDTH/2, SCREEN_HEIGHT/2);
				exit_int = exit_on_interruption;
			} catch (Exception e) {
				System.out.println("Failed to init Imbot.");
				e.printStackTrace();
				System.exit(-1);
			}
		} else {
			throw new AlreadyInitException();
		}
	}
	
	/**
	 * @param s	Seconds to sleep
	 */
	public static void sleeps (double s) {
		sleep ((long)(s *1000));
	}
	
	/**
	 * @param ms	Milliseconds to sleep
	 */
	public static void sleep (long ms) {
		safeExitIfInt();
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		safeExitIfInt();
	}
	
	/**
	 * @param s	String to write
	 */
	public static void write (String s) {
		for (char c : s.toCharArray()) {
			write (c);
		}
	}
	
	/**
	 * @param c	Char to write
	 */
	public static void write (char c) {
		safeExitIfInt();
		int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
		robot.keyPress(keyCode);
		robot.keyRelease(keyCode);
		safeExitIfInt();
	}
	
	/**
	 * 
	 * @return	A {@link Point} representing the coordinates of the mouse on the screen
	 */
	public static Point getLocation () {
		return MouseInfo.getPointerInfo().getLocation();
	}
	
	/**
	 * Moves the mouse to the given x and y
	 * @param x
	 * @param y
	 * @throws OutOfScreenBoundsException if the passed location is outside the screen
	 */
	public static void move (int x, int y) {
		safeExitIfInt();
		if ((x >= 0) && (x < SCREEN_WIDTH) && (y >= 0) && (y < SCREEN_HEIGHT)) {
			robot.mouseMove(x, y);	
			last_location.setLocation(x, y);
		} else {
			throw new OutOfScreenBoundsException(x, y);
		}
		safeExitIfInt();
	}
	
	/**
	 * Slides the mouse from where ever it is right now by dx and dy
	 * @param dx
	 * @param dy
	 */
	public static void slide (int dx, int dy) {
		Point p = getLocation();
		move(p.x +dx, p.y +dy);
	}
	
	/**
	 * First moves the mouse to x and y and then left clicks
	 * @param x
	 * @param y
	 */
	public static void click (int x, int y) {
		move(x, y);
		click();
	}
	
	/**
	 * Left clicks at the current position of the mouse
	 */
	public static void click () {
		safeExitIfInt();
		robot.mousePress(LEFT_BUTTON);
		robot.mouseRelease(LEFT_BUTTON);
		safeExitIfInt();
	}
	
	/**
	 * First moves the mouse to x and y and then right clicks
	 * @param x
	 * @param y
	 */
	public static void clickRight (int x, int y) {
		move(x, y);
		clickRight();
	}
	
	/**
	 * Right clicks at the current position of the mouse
	 */
	public static void clickRight () {
		safeExitIfInt();
		robot.mousePress(RIGHT_BUTTON);
		robot.mouseRelease(RIGHT_BUTTON);
		safeExitIfInt();
	}
	
	/**
	 * Drags (with left click) from where ever the mouse currently is to x and y
	 * @param x
	 * @param y
	 */
	public static void drag (int x, int y) {
		safeExitIfInt();
		robot.mousePress(LEFT_BUTTON);		
		move(x,y);
		robot.mouseRelease(LEFT_BUTTON);
		safeExitIfInt();
	}
	
	/**
	 * Drags (with left click) from the first location to the second location
	 * @param from_x
	 * @param from_y
	 * @param to_x
	 * @param to_y
	 */
	public static void drag (int from_x, int from_y, int to_x, int to_y) {
		move(from_x, from_y);
		robot.mousePress(LEFT_BUTTON);	
		move(to_x, to_y);
		robot.mouseRelease(LEFT_BUTTON);
		safeExitIfInt();
	}
	
	/**
	 * Exits the script if the user has interrupted and exit_on_interruption flag has been set
	 */
	public static void safeExitIfInt () {
		if (exit_int) {
			exitIfInt();
		}
	}
	
	/**
	 * Exits the script if the user has interrupted regardless of the exit_on_interruption flag
	 */
	public static void exitIfInt () {
		if (isUserControlling()) {
			System.exit(0);
		}
	}
	
	/**
	 * @return True if the user has moved the mouse, 
	 * False if not or if it moved but then returned to the same old position
	 */
	public static boolean isUserControlling () {
		return !last_location.equals(getLocation());
	}
	
	/**
	 * Continuously report location of the mouse on 
	 * the screen. Useful when trying to hard code location 
	 * of certain elements into the script
	 * @param s	Number of seconds to wait between each report
	 */
	public static void reportLocation (double s) {
		setExitIfInt(false);
		
		while(true) {
			sleeps(s);
			System.out.println(getLocation());
		}
	}
	
	/**
	 * Sets the flag exit_on_interruption
	 * @param exit_on_interruption
	 */
	public static void setExitIfInt (boolean exit_on_interruption) {
		exit_int = exit_on_interruption;
	}
	
	/**
	 * @param path
	 * @return {@link BufferedImage} representation of the image at the path
	 */
	public static BufferedImage readImage (String path) {
		try {
			return ImageIO.read(new File (path));
		} catch (IOException e) {
			System.out.println("Unable to read image: " +path);
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Shorthand for {@link #readImage(String)} and {@link #locateImage(BufferedImage)}
	 */
	public static Point locateImage (String path) {
		return locateImage (readImage(path));
	}
	
	/**
	 * Locates an image within the screen
	 * @param image
	 * @return	A {@link Point} representing the upper-left location of where
	 * the image has been found on the screen, or null if found nothing
	 * @apiNote Only works with PNG type of images as that they have a
	 * lossless compression.
	 * @apiNote Some screens or operating systems (such as MacOS), not sure which, do
	 * NOT take physically accurate screenshot using their normal screenshot binding.
	 * As they increase the actual physical resolution for better screenshot. This
	 * won't work with locateImage as it uses the actual physical resolution of the screen.
	 * Use {@link #captureScreen(Rectangle)} and {@link #storeImage(BufferedImage, String)}
	 * instead to be certain when taking screenshots and saving them!
	 */
	public static Point locateImage (BufferedImage image) {
		BufferedImage capture = robot.createScreenCapture(SCREEN_RECTANGLE);
		
		int x_limit = capture.getWidth() -image.getWidth() +1;
		int y_limit = capture.getHeight() -image.getHeight() +1;
		for (int x = 0; x < x_limit; x++) {
			for (int y = 0; y < y_limit; y++) {
				if (isSubImage(capture, image, x, y)) {
					return new Point (x, y);
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @apiNote Helper method for {@link #locateImage(BufferedImage)}
	 */
	private static boolean isSubImage (BufferedImage capture, BufferedImage image, int start_x, int start_y) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (capture.getRGB(start_x +x,  start_y +y) != image.getRGB(x, y)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * @return A screenshot of the entire screen
	 */
	public static BufferedImage captureScreen () {
		return captureScreen (SCREEN_RECTANGLE);
	}
	
	/**
	 * @param starting_point
	 * @return A screenshot of the screen from the starting_point to bottom-right end
	 */
	public static BufferedImage captureScreen (Point starting_point) {
		return captureScreen(new Rectangle(starting_point.x, starting_point.y, SCREEN_WIDTH -starting_point.x, SCREEN_HEIGHT -starting_point.y));
	}
	
	/**
	 * @param zone
	 * @return A screenshot of the specified zone on the screen
	 */
	public static BufferedImage captureScreen (Rectangle zone) {
		return robot.createScreenCapture(zone);
	}
	
	/**
	 * @param image	Image to store
	 * @param path	Path of the folder in which to store the image. For example "/Users/telos_matter/Pictures"
	 * @param name	Name under which to store the image
	 * @return True if the image was stored successfully, False otherwise.
	 */
	public static boolean storeImage (BufferedImage image, String path, String name) {
		try {
			ImageIO.write(image, "png", new File(path + File.separatorChar +name +".png"));
			return true;
		} catch (IOException e) {
			System.out.println("Unable to save image under: " +path + File.separatorChar +name +".png");
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Utility function to keep the computer from sleeping
	 * by periodically moving the mouse
	 * @param initial_delay	Delay to sleep before starting is milliseconds
	 */
	public static void STAY_AWAKE (int initial_delay) {
		sleep (initial_delay);
		move(SCREEN_WIDTH/2 +50, SCREEN_HEIGHT/2 +50);
		while (true) {
			slide(0, -100);
			sleep(2500);
			slide(-100, 0);
			sleep(2500);
			slide(0, 100);
			sleep(2500);
			slide(100, 0);
			sleep(2500);
		}
	}
	
}
