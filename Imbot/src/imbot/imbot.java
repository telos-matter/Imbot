package imbot;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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
	private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
	
	public static final int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
	public static final int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
	public static final Rectangle SCREEN_RECTANGLE = new Rectangle (0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

	
	
	private static Robot robot = null;
	private static Point last_location = new Point (-1, -1);
	private static boolean exit_int = false;
	
	/**
	 * Shorthand for {@link #init(boolean)}
	 * with the default values of true
	 */
	public static void init () {
		init (true);
	}
	
	/**
	 * Initialize imbot with the given parameters. imbot
	 * should always be initialized before use.
	 * 
	 * @param exit_on_interruption	Bool to whether or not exit the script
	 * if the user is interrupting by moving the mouse
	 * @throws AlreadyInitException if imbot has already been initialized
	 */
	public static void init (boolean exit_on_interruption) {
		if (robot == null) {
			try {
				robot = new Robot (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
				robot.setAutoDelay(0);	
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
	 * <p>Differs from {@link #write(char)} in that it allows
	 * to write what ever key on the keyboard. Meanwhile {@link #write(char)}
	 * ~only~ works ~properly~ with alphanumeric keys
	 * <p>Use {@link KeyEvent} constants.
	 * @param keyCode	KeyCode of the key to write
	 */
	public static void writeKey (int keyCode) {
		safeExitIfInt();
		robot.keyPress(keyCode);
		robot.keyRelease(keyCode);
		safeExitIfInt();
	}
	
	/**
	 * Holds a key pressed. Use {@link #releaseK(int)} to release the key.
	 * @param keyCode	KeyCode of the key to press
	 */
	public static void pressKey (int keyCode) {
		safeExitIfInt();
		robot.keyPress(keyCode);
		safeExitIfInt();
	}
	
	/**
	 * Releases a key.
	 * @param keyCode	KeyCode of the key to release
	 */
	public static void releaseKey (int keyCode) {
		safeExitIfInt();
		robot.keyRelease(keyCode);
		safeExitIfInt();
	}
	
	/**
	 * @return	A {@link Point} representing the coordinates 
	 * of the mouse on the screen
	 */
	public static Point getLocation () {
		return MouseInfo.getPointerInfo().getLocation();
	}
	
	/**
	 * Same as {@link #getColor(Point)} but with {@link Point}
	 */
	public static Color getColor (Point location) {
		return getColor(location.x, location.y);
	}
	
	/**
	 * @return	The {@link Color} of the pixel at the specified 
	 * location on the screen
	 * @throws OutOfScreenBoundsException if the passed 
	 * location is outside the screen
	 */
	public static Color getColor (int x, int y) {
		if ((x >= 0) && (x < SCREEN_WIDTH) && (y >= 0) && (y < SCREEN_HEIGHT)) {
			return robot.getPixelColor(x, y);
		} else {
			throw new OutOfScreenBoundsException(x, y);
		}
	}
    
    /**
     * <p>Simulate user-like mouse movement to the passed x and y
     * trough <a href= "https://github.com/BenLand100">Benjamin J. Land</a>s' <a href= "https://ben.land/post/2021/04/25/windmouse-human-mouse-movement/">WindMouse algorithm</a>.
     * <p><strong>Authors' note:</strong>
     * <p>I personally don't like
     * using <i>external code</i> and <strong>specially</strong>
     * <i>external code</i> which I don't fully understand. The use
     * of this algorithm falls in between the two mentioned cases.
     * @implNote The end point is not always
     * the passed x and y, it may differ by 1 pixel.
     * @throws OutOfScreenBoundsException if the passed location is outside the screen
     */
	public static void realisticMove (int x, int y) {
		safeExitIfInt();
		if ((x >= 0) && (x < SCREEN_WIDTH) && (y >= 0) && (y < SCREEN_HEIGHT)) {
			
			final double sqrt_3 = Math.sqrt(3);
			final double sqrt_5 = Math.sqrt(5);
			final double speed = (Math.random()*15 +15)/5;
			final double gravity = 9;
			final double min_wait = 5/speed;
			final double max_wait = 10/speed;
			final double targetArea = 8*speed;
			double wind = 3;
			double max_step = 10*speed;
			
			Point location = getLocation();
			double moving_x = location.x;
			double moving_y = location.y;
			double dist, velocity_x = 0, velocity_y = 0, wind_x = 0, wind_y = 0;
			
			while ((dist = Math.hypot(moving_x - x,moving_y - y)) >= 1) {
				safeExitIfInt();
				
				wind = Math.min(wind, dist);
				
				if (dist >= targetArea) {
					wind_x = wind_x/sqrt_3 + (2*Math.random() -1)*wind/sqrt_5;
					wind_y = wind_y/sqrt_3 + (2*Math.random() -1)*wind/sqrt_5;
				} else {
					wind_x /= sqrt_3;
					wind_y /= sqrt_3;
					
					if (max_step < 3) {
						max_step = Math.random()*3 +3;
					} else {
						max_step /= sqrt_5;
					}
				}
				
				velocity_x += wind_x +gravity*(x -moving_x)/dist;
				velocity_y += wind_y +gravity*(y -moving_y)/dist;
				
				double velocity_m = Math.hypot(velocity_x, velocity_y);
				if (velocity_m > max_step) {
					double random_dist = max_step/2 +Math.random()*max_step/2D;
					velocity_x = (velocity_x/velocity_m)*random_dist;
					velocity_y = (velocity_y/velocity_m)*random_dist;
				}
				
				moving_x += velocity_x;
				moving_y += velocity_y;
				
				location = getLocation();
				int goto_x = (int) Math.round(moving_x);
				int goto_y = (int) Math.round(moving_y);
				if (location.x != goto_x || location.y != goto_y) {
					robot.mouseMove(goto_x, goto_y);
					last_location.setLocation(goto_x, goto_y);
				}
				
				double step = Math.hypot(moving_x - location.x, moving_y - location.y);
				sleep(Math.round((max_wait -min_wait) * (step / max_step) +min_wait));
			}
			
			if (! SCREEN_RECTANGLE.contains(last_location)) {
				robot.mouseMove(x, y);	
				last_location.setLocation(x, y);
			}
			
		} else {
			throw new OutOfScreenBoundsException(x, y);
		}
		safeExitIfInt();
	}
	
	/**
	 * Slides the mouse by dx and dy with user-like mouse movement 
	 * @implNote Uses {@link #realisticMove(int, int)}
	 */
	public static void realisticSlide (int dx, int dy) {
		Point p = getLocation();
		realisticMove(p.x +dx, p.y +dy);
	}
	
	/**
	 * First moves the mouse to x and y with user-like mouse movement 
	 * then left clicks
	 * @implNote Uses {@link #realisticMove(int, int)}
	 */
	public static void realisticClick (int x, int y) {
		move(x, y);
		click();
	}
	
	
	/**
	 * First moves the mouse to x and y with user-like mouse movement 
	 * then right clicks
	 * @implNote Uses {@link #realisticMove(int, int)}
	 */
	public static void realisticClickRight (int x, int y) {
		move(x, y);
		clickRight();
	}
	
	/**
	 * Simulate user-like mouse drag (with left click) from where 
	 * ever the mouse currently is to the passed x and y
	 * @implNote Uses {@link #realisticMove(int, int)}
	 */
	public static void realisticDrag (int x, int y) {
		safeExitIfInt();
		robot.mousePress(LEFT_BUTTON);		
		move(x,y);
		robot.mouseRelease(LEFT_BUTTON);
		safeExitIfInt();
	}
	
	/**
	 * Simulate user-like mouse drag (with left click) from the first
	 * location to the second location
	 * @implNote Uses {@link #realisticMove(int, int)}
	 */
	public static void realisticDrag (int from_x, int from_y, int to_x, int to_y) {
		move(from_x, from_y);
		robot.mousePress(LEFT_BUTTON);	
		move(to_x, to_y);
		robot.mouseRelease(LEFT_BUTTON);
		safeExitIfInt();
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
	 * @param text	to be copied to the clipboard
	 */
	public static void copy (String text) {
		CLIPBOARD.setContents(new StringSelection (text), null);
	}
	
	/**
	 * Pastes what's in the clipboard trough {@link #write(String)}
	 * @return	True if all went well. False otherwise (in case 
	 * the content of the clipboard cannot be represented as a string).
	 */
	public static boolean paste () {
		try {
			write((String) CLIPBOARD.getContents(null).getTransferData(DataFlavor.stringFlavor));
			return true;
		} catch (Exception e) {
			System.out.println("Was not able to paste the clipboards' content as a String");
			return false;
		}
	}
	
	/**
	 * Clears the clipboard content
	 */
	public static void clearClipboard () {
		copy("");
	}
	
	/**
	 * @return	String representing the clipboard content, or null
	 * if the clipboard content cannot be represented as a string
	 */
	public static String getClipboardContent () {
		try {
			return (String) CLIPBOARD.getContents(null).getTransferData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			System.out.println("Was not able to get the clipboards' content as a String");
			return null;
		}
	}
	
	/**
	 * Exits the script if the user has interrupted and 
	 * exit_on_interruption flag has been set
	 */
	public static void safeExitIfInt () {
		if (exit_int) {
			exitIfInt();
		}
	}
	
	/**
	 * Exits the script if the user has interrupted 
	 * regardless of exit_on_interruption flag
	 */
	public static void exitIfInt () {
		if (isUserControlling()) {
			System.exit(0);
		}
	}
	
	/**
	 * @return True if the user has moved the mouse, 
	 * False if not or if it moved but then returned to 
	 * the exact last position (highly improbable..)
	 */
	public static boolean isUserControlling () {
		return !last_location.equals(getLocation());
	}
	
	/**
	 * <p>Continuously report the location of the mouse on 
	 * the screen and the color at that location.
	 * <p>Useful when trying to hard code locations
	 * of certain elements into a script.
	 * <ul> Press / type (while the window is in focus):
	 * <li><strong>space</strong>:	to copy the color into the clipboard</li>
	 * <li><strong>l</strong> or <strong>L</strong>:	to copy the location into the clipboard</li>
	 * <li><strong>enter</strong> or <strong>return</strong>:	to copy the location and the color into the clipboard</li>
	 * <li><strong>q</strong> or <strong>Q</strong>:	to quit</li>
	 * </ul>
	 * @implNote	The method never exits,
	 * as it contains an infinite loop. It's not meant
	 * to be used within a script
	 * @implNote	A bug may be encountered where,
	 * when leaving the mouse stationary for a while,
	 * the color would start reading white. No certain
	 * idea of why that's the case or how to fix it.
	 */
	public static void reportLocation () {
		setExitIfInt(false);
		
		JFrame frame = new JFrame ("Imbot");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.addKeyListener(new KeyAdapter () {
			@Override
			public void keyTyped(KeyEvent e) {
				Point location = getLocation();
				Color pixel_clr = getColor(location);
				switch(e.getKeyChar()) {
				case ' ':
					copy(String.format("0x%02X%02X%02X", pixel_clr.getRed(), pixel_clr.getGreen(), pixel_clr.getBlue()));
					break;
				case 'l': case 'L':
					copy(String.format("(%d,%d)",location.x, location.y)); 
					break;
				case '\n':
					copy(String.format("(%d,%d) -> 0x%02X%02X%02X",location.x, location.y, pixel_clr.getRed(), pixel_clr.getGreen(), pixel_clr.getBlue())); 
					break;
				case 'q': case 'Q':
					frame.dispose(); System.exit(0); 
					break;
				}
			}
		});
		
		JPanel panel = new JPanel ();
		panel.setLayout(new GridLayout (3, 1));
		
		JLabel x = new JLabel("x: 0000", SwingConstants.CENTER);
		JLabel y = new JLabel("y: 0000", SwingConstants.CENTER);
		JLabel color = new JLabel("0x000000", SwingConstants.CENTER);
		
		panel.add(x);
		panel.add(y);
		panel.add(color);
		
		frame.setContentPane(panel);
		frame.setSize(150, 100);
		frame.setVisible(true);
		
		while (true) {
			sleep(50);
			EventQueue.invokeLater(() -> {
				Point location = getLocation();
				Color pixel_clr = getColor(location);
				
				panel.setBackground(pixel_clr);
				
				if ((0.2126 * pixel_clr.getRed() + 0.7152 * pixel_clr.getGreen() + 0.0722 * pixel_clr.getBlue()) < 100) {
					x.setForeground(Color.WHITE);
					y.setForeground(Color.WHITE);
					color.setForeground(Color.WHITE);
				} else {
					x.setForeground(Color.BLACK);
					y.setForeground(Color.BLACK);
					color.setForeground(Color.BLACK);
				}
				
				x.setText("x: " +location.x);
				y.setText("y: " +location.y);
				color.setText(String.format("0x%02X%02X%02X", pixel_clr.getRed(), pixel_clr.getGreen(), pixel_clr.getBlue()));
			});
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
	public static BufferedImage loadImage (String path) {
		try {
			return ImageIO.read(new File (path));
		} catch (IOException e) {
			System.out.println("Unable to read image: " +path);
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Shorthand for {@link #loadImage(String)} and {@link #searchForImage(BufferedImage, double)}
	 */
	public static Point searchForImage (String path, double s) {
		return searchForImage(loadImage(path), s);
	}
	
	/**
	 * Searches for an image on the screen
	 * for a set period of time.
	 * @param image	to be searched for
	 * @param s	amount of seconds to search for. Negative values
	 * mean search forever
	 * @return	Upper-left location of where the image is
	 * located on the screen, or null if not found.
	 */
	public static Point searchForImage (BufferedImage image, double s) {
		s = (s < 0)? Double.POSITIVE_INFINITY : s * 1000000000;
		
		Point location;
		long start;
		do {
			start = System.nanoTime();
			location = locateImage(image);
			s -= System.nanoTime() -start;
			
			if (location != null) {
				break;
			}
		} while (s >= 0);
		
		return location;
	}
	
	/**
	 * Shorthand for {@link #loadImage(String)} and {@link #locateImage(BufferedImage)}
	 */
	public static Point locateImage (String path) {
		return locateImage(loadImage(path));
	}
	
	/**
	 * Locates an image within the screen
	 * @param image
	 * @return	A {@link Point} representing the upper-left location of where
	 * the image has been found on the screen, or null if found nothing
	 * @implNote Only works with PNG type of images as that they have a
	 * lossless compression.
	 * @implNote Some screens or operating systems (such as MacOS), not sure which, do
	 * NOT take physically accurate screenshot using their normal screenshot binding.
	 * As they increase the actual physical resolution for better screenshot. This
	 * won't work with locateImage as it uses the actual physical resolution of the screen.
	 * Use {@link #captureScreen(Rectangle)} and {@link #saveImage(BufferedImage, String, String)}
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
	 * @implNote Helper method for {@link #locateImage(BufferedImage)}
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
	 * @param image	Image to save
	 * @param path	Path of the folder in which to store the image. For example "/Users/telos_matter/Pictures"
	 * @param name	Name under which to store the image
	 * @return True if the image was stored successfully, False otherwise.
	 */
	public static boolean saveImage (BufferedImage image, String path, String name) {
		try {
			return ImageIO.write(image, "png", new File(path + File.separatorChar +name +".png"));
		} catch (IOException e) {
			System.out.println("Unable to save image under: " +path + File.separatorChar +name +".png");
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * An easy alternative for the tedious System.out.println() with 
	 * better support for Lists, Maps and arrays.
	 */
	public static void out (Object any) {
		if (any == null) {
			System.out.println("Null");
		} else if (any instanceof Iterable <?>) {
			
			Iterator <?> iter = ((Iterable <?>) any).iterator();
			int i;
			for (i = 0; iter.hasNext(); i++) {
				System.out.println(i +": " +iter.next());
			}
			if (i == 0) {
				System.out.println("Empty");
			}
				
		} else if (any instanceof Map <?, ?>) {
			
			if (((Map <?, ?>) any).isEmpty()) {
				System.out.println("Empty map");
			} else {
				((Map <?, ?>) any).forEach((Object key, Object value) -> {System.out.println(key +" -> " +value);});
			}
			
		} else if (any.getClass().isArray()) {
			
			Object [] array = (Object[]) any;
			if (array.length == 0) {
				System.out.println("Empty array");
			} else {
				for (int i = 0; i < array.length; i++) {
					System.out.println(i +": " +array[i]);
				}
			}
			
		} else {
			System.out.println(any.toString());
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
