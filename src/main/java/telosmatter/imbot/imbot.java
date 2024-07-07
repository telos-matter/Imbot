package telosmatter.imbot;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

///**
// * <p>A class that facilitates and adds features
// * to the already existing {@link java.awt.Robot}.
// * <p>imbot is intentionally not capitalized (as opposed
// * to the class naming convention) to facilitate its use.
// * <br>
// * <br>
// * <a href="https://github.com/telos-matter/Imbot"> Github link</a>
// * @author telos_matter
// */
public class imbot {

	/**
	 * The singleton robot instance that does the work.
	 */
	private static final Robot robot; // TODO refactor to ROBOT

	static {
		try {
			robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
		} catch (AWTException e) {
			System.err.println("Unable to initialize imbot.");
			throw new RuntimeException(e);
		}
		robot.setAutoDelay(0);
		robot.setAutoWaitForIdle(true);
		robot.mouseMove(scr.MIDDLE.x, scr.MIDDLE.y);
	}


//	private static Point last_location = new Point (-1, -1);
//	private static boolean exit_int = false;


	/**
	 * All utilities related to the mouse
	 */
	public static class mouse {

		private static final int LEFT_BUTTON = InputEvent.getMaskForButton(1);
		private static final int RIGHT_BUTTON = InputEvent.getMaskForButton(3);

		/**
		 * Should the movement be realistic (human like)
		 * or robotic (linear movements).
		 */
		private static boolean realistic = true;

		/**
		 * The time in milliseconds between the first and second click.
		 * There is no standard time difference, but
		 * it shouldn't surpass 500 ms.
		 */
		private static int doubleClickDelta = 200;

		/**
		 * Set how to preform mouse movements.
		 * Should they be realistic / human like? Or
		 * robotic? (linear movement)
		 */
		public static void setRealistic (boolean value) {
			realistic = value;
		}

		/**
		 * Are the movements preformed realistic?
		 * @see #setRealistic(boolean)
		 */
		public static boolean isRealistic () {
			return realistic;
		}

		/**
		 * Set the time difference in milliseconds
		 * between the first and second click
		 * when double-clicking.
		 * There is no standard time difference, but
		 * it shouldn't surpass 500 ms.
		 */
		public static void setDoubleClickDelta(int ms) {
			doubleClickDelta = ms;
		}

		/**
		 * @return the time difference in milliseconds
		 * between the first and second click
		 * when double-clicking.
		 */
		public static int getDoubleClickDelta () {
			return doubleClickDelta;
		}

		/**
		 * Get location.
		 * @return A {@link Point} representing the location
		 * of the mouse on the screen
		 */
		public static Point location() {
			return MouseInfo.getPointerInfo().getLocation();
		}

		/**
		 * The only place where
		 * robotMouseMove is actually
		 * called
		 */
		private static void robotMove (int x, int y) {
			// TODO add the thread check here for interuption
			robot.mouseMove(x, y);
		}

		/**
		 * <p>Simulate user-like mouse movement to the passed x and y
		 * trough <a href= "https://github.com/BenLand100">Benjamin J. Land</a>s' <a href= "https://ben.land/post/2021/04/25/windmouse-human-mouse-movement/">WindMouse algorithm</a>.
		 * <p><strong>Authors' note:</strong>
		 * <p>I personally don't like
		 * using <i>external code</i> and <strong>especially</strong>
		 * <i>external code</i> which I don't fully understand. The use
		 * of this algorithm falls in between the two mentioned cases.
		 * @implNote The end point is not always
		 * the passed x and y, it may differ by 1 pixel.
		 */
		private static void realisticMove (int x, int y) {
			final double sqrt_3 = Math.sqrt(3);
			final double sqrt_5 = Math.sqrt(5);
			final double speed = (Math.random()*15 +15)/5;
			final double gravity = 9;
			final double min_wait = 5/speed;
			final double max_wait = 10/speed;
			final double targetArea = 8*speed;
			double wind = 3;
			double max_step = 10*speed;

			Point location = location();
			double moving_x = location.x;
			double moving_y = location.y;
			double dist, velocity_x = 0, velocity_y = 0, wind_x = 0, wind_y = 0;

			while ((dist = Math.hypot(moving_x - x,moving_y - y)) >= 2) { // TODO was 1 made it 2. Check how it affects
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

				location = location();
				int goto_x = (int) Math.round(moving_x);
				int goto_y = (int) Math.round(moving_y);
				if (location.x != goto_x || location.y != goto_y) {
					robotMove(goto_x, goto_y);
				}

				double step = Math.hypot(moving_x - location.x, moving_y - location.y);
				util.sleep(Math.round((max_wait -min_wait) * (step / max_step) +min_wait));
			}
		}

		/**
		 * Move to the given <code>x</code> and
		 * <code>y</code>.
		 * @see #setRealistic(boolean) 
		 */
		public static void move (int x, int y) {
			if (realistic) {
				realisticMove(x, y);
			} else {
				robotMove(x, y);
			}
		}

		/**
		 * @see #move(int, int)
		 */
		public static void move (Point point) {
			move(point.x, point.y);
		}

		/**
		 * Slides the mouse by dx and dy
		 */
		public static void slide (int dx, int dy) {
			Point point = location();
			move(point.x +dx, point.y +dy);
		}

		/**
		 * Left click
		 */
		public static void click () {
			robot.mousePress(LEFT_BUTTON);
			robot.mouseRelease(LEFT_BUTTON);
		}

		/**
		 * Left click at <code>x</code> and <code>y</code>
		 */
		public static void click (int x, int y) {
			move(x, y);
			click();
		}

		/**
		 * @see #click(int, int)
		 */
		public static void click (Point point) {
			click(point.x, point.y);
		}

		/**
		 * Double left click
		 * @see #setDoubleClickDelta(int)
		 */
		public static void doubleClick () {
			click();
			util.sleep(doubleClickDelta);
			click();
		}

		/**
		 * Double left click at <code>x</code>
		 * and <code>y</code>
		 */
		public static void doubleClick (int x, int y) {
			move(x, y);
			doubleClick();
		}

		/**
		 * @see #doubleClick(int, int)
		 */
		public static void doubleClick (Point point) {
			doubleClick(point.x, point.y);
		}

		/**
		 * Right click
		 */
		public static void rightClick() {
			robot.mousePress(RIGHT_BUTTON);
			robot.mouseRelease(RIGHT_BUTTON);
		}

		/**
		 * Right click at <code>x</code> and <code>y</code>
		 */
		public static void rightClick(int x, int y) {
			move(x, y);
			rightClick();
		}

		/**
		 * @see #rightClick(int, int)
		 */
		public static void rightClick (Point point) {
			rightClick(point.x, point.y);
		}

		/**
		 * Drags, with a left click, from the first point to the second point
		 */
		public static void drag (int from_x, int from_y, int to_x, int to_y) {
			move(from_x, from_y);
			robot.mousePress(LEFT_BUTTON);
			move(to_x, to_y);
			robot.mouseRelease(LEFT_BUTTON);
		}

		/**
		 * @see #drag(int, int, int, int)
		 */
		public static void drag (Point from, Point to) {
			drag(from.x, from.y, to.x, to.y);
		}

		/**
		 * Drags, with left click, the mouse
		 * to <code>x</code> and <code>y</code>
		 */
		public static void drag (int to_x, int to_y) {
			robot.mousePress(LEFT_BUTTON);
			move(to_x,to_y);
			robot.mouseRelease(LEFT_BUTTON);
		}

		/**
		 * @see #drag(int, int)
		 */
		public static void drag (Point to) {
			drag(to.x, to.y);
		}

		/**
		 * <p>Hover / move the mouse around inside a square
		 * centered at the current mouse location for a random
		 * amount of time.
		 * <p>For example; suppose the mouse is at <code>(20, 20)</code>
		 * and this function is called with these arguments:
		 * <code>hover (5, 2, 0.5)</code>, then the mouse
		 * would hover around inside the square whose top left corner
		 * is <code>(15,15)</code>, and bottom right corner is
		 * <code>(25, 25)</code>, for a random duration between
		 * <code>1.5</code> seconds and <code>2.5</code> seconds.
		 * @param halfSide half the length of the side of the square
		 *                 in which to hover
		 * @param duration	time in seconds to hover around for
		 * @param delta	the minimum and maximum time difference
		 *              in seconds
		 */
		public static void hover (int halfSide, double duration, double delta) {
			Point location = location();
			// Get upper left corner
			int minX = location.x - halfSide;
			int minY = location.y - halfSide;
			// Compute side
			int side = halfSide * 2;

			// Get random duration within bounds, in nanoseconds
			duration = (Math.random()*2*delta + (duration - delta)) * 1_000_000_000 ;

			// Hover until duration has elapsed
			while (duration >= 0) {
				long start = System.nanoTime();
				// Get random x and y within square
				int x = (int)(Math.random()*side + minX);
				int y = (int)(Math.random()*side + minY);
				// Move to x and y
				move(x, y);
				// Subtract however long this took
				duration -= System.nanoTime() - start; // duration -= (end - start)
			}
		}
	}

	/**
	 * All utilities related to the keyboard
	 */
	public static class kyb {
		/**
		 * Presses a key.
		 * Use {@link #release(int)} to release it.
		 * @param keyCode KeyCode of the key to press. (e.g. KeyEvent.VK_A)
		 */
		public static void press (int keyCode) {
			robot.keyPress(keyCode);
		}

		/**
		 * Releases a key.
		 * @param keyCode KeyCode of the key to release. (e.g. KeyEvent.VK_A)
		 */
		public static void release (int keyCode) {
			robot.keyRelease(keyCode);
		}

		/**
		 * @param c	char to write
		 */
		public static void type (char c) {
			int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
			robot.keyPress(keyCode);
			robot.keyRelease(keyCode);
		}

		/**
		 * @param s	string to write
		 */
		public static void write (String s) {
			for (char c : s.toCharArray()) {
				type(c);
			}
		}
	}

	/**
	 * All utilities related to the screen
	 */
	public static class scr {
		/**
		 * Your screen width
		 */
		public static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
		/**
		 * Your screen height
		 */
		public static final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
		/**
		 * The middle of your screen.
		 * READ-ONLY, do not edit.
		 */
		public static final Point MIDDLE = new Point(WIDTH/2, HEIGHT/2);
		/**
		 * Your screen as a rectangle.
		 * READ-ONLY, do not edit.
		 */
		public static final Rectangle SCREEN_RECTANGLE = new Rectangle (0, 0, WIDTH, HEIGHT);

		/**
		 * @return <code>true</code> if the position
		 * is within the screen bounds. <code>false</code>
		 * otherwise.
		 */
		public static boolean isWithin (int x, int y) {
			return SCREEN_RECTANGLE.contains(x, y);
		}

		/**
		 * @see #isWithin(int, int)
		 */
		public static boolean isWithin (Point point) {
			return isWithin(point.x, point.y);
		}

		// TODO here on down
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
		 * @param location	of the pixel on the screen
		 * @param color	to check against
		 * @param dc	(delta color) difference in color
		 * @return	True if the color difference (by euclidean distance) is
		 * less than or equal to , False otherwise.
		 */
		public static boolean isColor (Point location, Color color, double dc) {
			Color screen_color = getColor(location);

			return Math.sqrt(Math.pow(color.getRed() -screen_color.getRed(), 2)
						 +Math.pow(color.getGreen() -screen_color.getGreen(), 2)
						 +Math.pow(color.getBlue() -screen_color.getBlue(), 2)) <= dc;
		}
	}

	/**
	 * All utilities related to
	 * the clipboard
	 */
	public static class cb {

		private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

		/**
		 * Copies text to the clipboard
		 */
		public static void copy (String text) {
			CLIPBOARD.setContents(new StringSelection(text), null);
		}

		/**
		 * @return the content in the clipboard. Or
		 * <code>null</code> if unable to.
		 */
		public static String getContent () {
			try {
				return (String) CLIPBOARD.
						getContents(null).
						getTransferData(DataFlavor.stringFlavor);
			} catch (Exception e) {
				return null;
			}
		}

		/**
		 * Pastes what's in the clipboard
		 * trough {@link kyb#write(String)}
		 * @return <code>true</code> if all went well.
		 * <code>false</code> if unable to paste the content
		 * of the clipboard
		 */
		public static boolean paste () {
			String content = getContent();
			if (content != null) {
				kyb.write(content);
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Clear the clipboard content
		 */
		public static void clear () {
			copy("");
		}
	}

	/**
	 * Other utilities that do
	 * not fall in any of the above
	 * categories
	 */
	public static class util {

		/**
		 * @param ms milliseconds to sleep
		 */
		public static void sleep (long ms) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException ignored) {}
		}

		/**
		 * @param secs seconds to sleep
		 */
		public static void sleeps (double secs) {
			sleep ((long)(secs *1000));
		}

		/**
		 * TODO yet to check this one
		 * Sleep a random amount of seconds between
		 * s -delta and s +delta
		 */
		public static void sleepRandom (double s, double delta) {
			sleeps(Math.random()*2*delta +(s -delta));
		}
	}






// TODO keep this one
//
//	/**
//	 * @return True if the user has moved the mouse,
//	 * False if not or if it moved but then returned to
//	 * the exact last position (highly improbable..)
//	 */
//	public static boolean isUserControlling () {
//		return !last_location.equals(getLocation());
//	}

	// TODO put these "programs" somewhere
//	/**
//	 * Continuously report location of the mouse on
//	 * the screen trough the console. Useful when trying to hard code location
//	 * of certain elements into a script
//	 * @param s	Number of seconds to wait between each report
//	 * @implNote	The method never exits,
//	 * as it contains an infinite loop. It's not meant
//	 * to be used within a script
//	 */
//	public static void reportLocation (double s) {
//		setExitIfInt(false);
//
//		Point location;
//		while(true) {
//			sleeps(s);
//			location = getLocation();
//			System.out.println(String.format("(%d,%d)", location.x, location.y));
//		}
//	}
//
//	/**
//	 * <p>Continuously report the location of the mouse on
//	 * the screen and the color at that location.
//	 * <p>Useful when trying to hard code locations
//	 * of certain elements into a script.
//	 * <ul> Press / type (while the window is in focus):
//	 * <li><strong>space</strong>:	to copy the color into the clipboard</li>
//	 * <li><strong>l</strong> or <strong>L</strong>:	to copy the location into the clipboard</li>
//	 * <li><strong>enter</strong> or <strong>return</strong>:	to copy the location and the color into the clipboard</li>
//	 * <li><strong>q</strong> or <strong>Q</strong>:	to quit</li>
//	 * </ul>
//	 * @implNote	The method never exits,
//	 * as it contains an infinite loop. It's not meant
//	 * to be used within a script
//	 * @implNote	A bug may be encountered where,
//	 * when leaving the mouse stationary for a while,
//	 * the color would start reading white. No certain
//	 * idea of why that's the case or how to fix it.
//	 */
//	public static void reportLocation () {
//		setExitIfInt(false);
//
//		JFrame frame = new JFrame ("Imbot");
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setLocationRelativeTo(null);
//		frame.addKeyListener(new KeyAdapter () {
//			@Override
//			public void keyTyped(KeyEvent e) {
//				Point location = getLocation();
//				Color pixel_clr = getColor(location);
//				switch(e.getKeyChar()) {
//				case ' ':
//					copy(String.format("0x%02X%02X%02X", pixel_clr.getRed(), pixel_clr.getGreen(), pixel_clr.getBlue()));
//					break;
//				case 'l': case 'L':
//					copy(String.format("(%d,%d)",location.x, location.y));
//					break;
//				case '\n':
//					copy(String.format("(%d,%d) -> 0x%02X%02X%02X",location.x, location.y, pixel_clr.getRed(), pixel_clr.getGreen(), pixel_clr.getBlue()));
//					break;
//				case 'q': case 'Q':
//					frame.dispose(); System.exit(0);
//					break;
//				}
//			}
//		});
//
//		JPanel panel = new JPanel ();
//		panel.setLayout(new GridLayout (3, 1));
//
//		JLabel x = new JLabel("x: 0000", SwingConstants.CENTER);
//		JLabel y = new JLabel("y: 0000", SwingConstants.CENTER);
//		JLabel color = new JLabel("0x000000", SwingConstants.CENTER);
//
//		panel.add(x);
//		panel.add(y);
//		panel.add(color);
//
//		frame.setContentPane(panel);
//		frame.setSize(150, 100);
//		frame.setVisible(true);
//
//		while (true) {
//			sleep(50);
//			EventQueue.invokeLater(() -> {
//				Point location = getLocation();
//				Color pixel_clr = getColor(location);
//
//				panel.setBackground(pixel_clr);
//
//				if ((0.2126 * pixel_clr.getRed() + 0.7152 * pixel_clr.getGreen() + 0.0722 * pixel_clr.getBlue()) < 100) {
//					x.setForeground(Color.WHITE);
//					y.setForeground(Color.WHITE);
//					color.setForeground(Color.WHITE);
//				} else {
//					x.setForeground(Color.BLACK);
//					y.setForeground(Color.BLACK);
//					color.setForeground(Color.BLACK);
//				}
//
//				x.setText("x: " +location.x);
//				y.setText("y: " +location.y);
//				color.setText(String.format("0x%02X%02X%02X", pixel_clr.getRed(), pixel_clr.getGreen(), pixel_clr.getBlue()));
//			});
//		}
//	}
//	/**
//	 * @param path
//	 * @return {@link BufferedImage} representation of the image at the path
//	 */
//	public static BufferedImage loadImage (String path) {
//		try {
//			return ImageIO.read(new File (path));
//		} catch (IOException e) {
//			System.out.println("Unable to read image: " +path);
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	/**
//	 * Shorthand for {@link #loadImage(String)} and {@link #searchForImage(BufferedImage, double)}
//	 */
//	public static Point searchForImage (String path, double s) {
//		return searchForImage(loadImage(path), s);
//	}
//
//	/**
//	 * Searches for an image on the screen
//	 * for a set period of time.
//	 * Shorthand for {@link #searchForImage(BufferedImage, BufferedImage, int, double, double)}
//	 * and {@link #captureScreen()} with default values of 0 and 0 for dp and dc
//	 * @param image	to be searched for
//	 * @param s	amount of seconds to search for. Negative values
//	 * mean search forever
//	 * @return	Upper-left location of where the image is
//	 * located on the screen, or null if not found.
//	 */
//	public static Point searchForImage (BufferedImage image, double s) {
//		return searchForImage(image, SCREEN_RECTANGLE, 0, 0, s);
//	}
//
//	/**
//	 * Searches for an image for a set period of time. Uses
//	 * {@link #locateImage(BufferedImage, BufferedImage, int, double)}
//	 * @param s	amount of seconds to search for. Negative values
//	 * mean search forever
//	 * @return	Upper-left location of where the image is
//	 * located, or null if not found.
//	 */
//	public static Point searchForImage (BufferedImage image, Rectangle zone, int dp, double dc, double s) {
//		s = (s < 0)? Double.POSITIVE_INFINITY : s * 1000000000;
//
//		Point location;
//		long start;
//		do {
//			start = System.nanoTime();
//			location = locateImage(image, imbot.captureScreen(zone), dp, dc);
//			s -= System.nanoTime() -start;
//
//			if (location != null) {
//				break;
//			}
//		} while (s >= 0);
//
//		return location;
//	}
//
//	/**
//	 * Shorthand for {@link #loadImage(String)} and {@link #locateImage(BufferedImage)}
//	 */
//	public static Point locateImage (String path) {
//		return locateImage(loadImage(path));
//	}
//
//
//	/**
//	 * Shorthand for {@link #locateImage(BufferedImage, Rectangle, int, double)}
//	 * with the entire screen and default values of 0 and 0 for dp and dc.
//	 */
//	public static Point locateImage (BufferedImage image) {
//		return locateImage(image, SCREEN_RECTANGLE, 0, 0);
//	}
//
//	/**
//	 * Shorthand for {@link #locateImage(BufferedImage, BufferedImage, int, double)}
//	 * with the searched image being a screen capture of the specified zone
//	 */
//	public static Point locateImage (BufferedImage image, Rectangle zone, int dp, double dc) {
//		return locateImage(image, robot.createScreenCapture(zone), dp, dc);
//	}
//
//	/**
//	 * Locates an image within another
//	 * @param small	Image to look for
//	 * @param big	Image to look in for the small one
//	 * @param dp	delta_pixel: Threshold of the number of wrong pixel colors
//	 * @param dc	delta_color: Threshold of the difference in
//	 * colors. Calculated with euclidean distance
//	 * @return	A {@link Point} representing the upper-left location of where
//	 * the small image has been found in the big one, or null if found nothing
//	 * @implNote Only works with PNG type of images as that they have a
//	 * lossless compression.
//	 * @implNote Some screens or operating systems (such as MacOS), not sure which, do
//	 * NOT take physically accurate screenshot using their normal screenshot binding.
//	 * As they increase the actual physical resolution for better screenshot. This
//	 * won't work with locateImage as it uses the actual physical resolution of the screen.
//	 * Use {@link #captureScreen(Rectangle)} and {@link #saveImage(BufferedImage, String, String)}
//	 * instead to be certain when taking screenshots and saving them!
//	 */
//	public static Point locateImage (BufferedImage small, BufferedImage big, int dp, double dc) {
//		if (small.getWidth() > big.getWidth() || small.getHeight() > big.getHeight()) {
//			throw new ImproperSizeException();
//		}
//
//		int x_limit = big.getWidth() -small.getWidth() +1;
//		int y_limit = big.getHeight() -small.getHeight() +1;
//
//		for (int x = 0; x < x_limit; x++) {
//			for (int y = 0; y < y_limit; y++) {
//				if (isSubImage(small, big, x, y, dp,  dc)) {
//					return new Point (x, y);
//				}
//			}
//		}
//
//		return null;
//	}
//
//	/**
//	 * @implNote Helper method for {@link #locateImage(BufferedImage, BufferedImage, double, double)}
//	 */
//	private static boolean isSubImage (BufferedImage small, BufferedImage big, int start_x, int start_y, int dp, double dc) {
//		if (dc == 0) {
//			for (int x = 0; x < small.getWidth(); x++) {
//				for (int y = 0; y < small.getHeight(); y++) {
//					if (big.getRGB(start_x +x,  start_y +y) != small.getRGB(x, y)) {
//						if ((--dp) < 0) {
//							return false;
//						}
//					}
//				}
//			}
//
//			return true;
//		} else {
//			int rs, gs, bs, rb, gb, bb, s, b;
//			for (int x = 0; x < small.getWidth(); x++) {
//				for (int y = 0; y < small.getHeight(); y++) {
//					s = small.getRGB(x, y);
//					bs = s & 0xFF;
//					gs = (s >> 8) & 0xFF;
//					rs = (s >> 16) & 0xFF;
//					b = big.getRGB(start_x +x,  start_y +y);
//					bb = b & 0xFF;
//					gb = (b >> 8) & 0xFF;
//					rb = (b >> 16) & 0xFF;
//
//					if (Math.sqrt(Math.pow(rb -rs, 2) +Math.pow(gb -gs, 2) +Math.pow(bb -bs, 2)) > dc) {
//						if ((--dp) < 0) {
//							return false;
//						}
//					}
//				}
//			}
//
//			return true;
//		}
//	}
//
//	/**
//	 * @return	an Array containing the RGB values that the int value represents
//	 */
//	public static int [] toRGB (int value) {
//		int [] rgb = new int [3];
//
//		for (int i = 0; i < 3; i++) {
//			rgb [2 -i] = (value >> (8 * i)) & 0xFF;
//		}
//
//		return rgb;
//	}
//
//	/**
//	 * @return A screenshot of the entire screen
//	 */
//	public static BufferedImage captureScreen () {
//		return captureScreen (SCREEN_RECTANGLE);
//	}
//
//	/**
//	 * @param starting_point
//	 * @return A screenshot of the screen from the starting_point to bottom-right end
//	 */
//	public static BufferedImage captureScreen (Point starting_point) {
//		return captureScreen(new Rectangle(starting_point.x, starting_point.y, SCREEN_WIDTH -starting_point.x, SCREEN_HEIGHT -starting_point.y));
//	}
//
//	/**
//	 * @param zone
//	 * @return A screenshot of the specified zone on the screen
//	 */
//	public static BufferedImage captureScreen (Rectangle zone) {
//		return robot.createScreenCapture(zone);
//	}
//
//	/**
//	 * @param image	Image to save
//	 * @param path	Path of the folder in which to store the image. For example "/Users/telos_matter/Pictures"
//	 * @param name	Name under which to store the image
//	 * @return True if the image was stored successfully, False otherwise.
//	 */
//	public static boolean saveImage (BufferedImage image, String path, String name) {
//		try {
//			return ImageIO.write(image, "png", new File(path + File.separatorChar +name +".png"));
//		} catch (IOException e) {
//			System.out.println("Unable to save image under: " +path + File.separatorChar +name +".png");
//			e.printStackTrace();
//			return false;
//		}
//	}
//
//	/**
//	 * Shorthand for {@link #captureScreen(Rectangle)} and
//	 * {@link #saveImage(BufferedImage, String, String)}
//	 */
//	public static boolean saveCapture (Rectangle zone, String path, String name) {
//		return saveImage(captureScreen(zone), path, name);
//	}
//
//	// TODO: FIX, not working with primitive types
//	/**
//	 * An easy alternative for the tedious System.out.println() with
//	 * better support for Lists, Maps and arrays.
//	 */
//	public static void out (Object any) {
//		if (any == null) {
//			System.out.println("Null");
//		} else if (any instanceof Iterable <?>) {
//
//			Iterator <?> iter = ((Iterable <?>) any).iterator();
//			int i;
//			for (i = 0; iter.hasNext(); i++) {
//				System.out.println(i +": " +iter.next());
//			}
//			if (i == 0) {
//				System.out.println("Empty");
//			}
//
//		} else if (any instanceof Map <?, ?>) {
//
//			if (((Map <?, ?>) any).isEmpty()) {
//				System.out.println("Empty map");
//			} else {
//				((Map <?, ?>) any).forEach((Object key, Object value) -> {System.out.println(key +" -> " +value);});
//			}
//
//		} else if (any.getClass().isArray()) {
//			Object [] array = (Object[]) any;
//			if (array.length == 0) {
//				System.out.println("Empty array");
//			} else {
//				for (int i = 0; i < array.length; i++) {
//					System.out.println(i +": " +array[i]);
//				}
//			}
//
//		} else {
//			System.out.println(any.toString());
//		}
//	}
//
//	/**
//	 * Utility function to keep the computer from sleeping
//	 * by periodically moving the mouse
//	 * @param initial_delay	Delay to sleep before starting is milliseconds
//	 */
//	public static void STAY_AWAKE (int initial_delay) {
//		sleep (initial_delay);
//		move(SCREEN_WIDTH/2 +50, SCREEN_HEIGHT/2 +50);
//		while (true) {
//			slide(0, -100);
//			sleep(2500);
//			slide(-100, 0);
//			sleep(2500);
//			slide(0, 100);
//			sleep(2500);
//			slide(100, 0);
//			sleep(2500);
//		}
//	}

}
