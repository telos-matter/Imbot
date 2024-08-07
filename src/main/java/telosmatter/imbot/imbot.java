package telosmatter.imbot;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * <p>A class that facilitates and adds features
 * to the already existing {@link java.awt.Robot}.</p>
 * <p>Methods are regrouped in static classes
 * each dedicated to a "category" of
 * functionalities.</p>
 * <p>The class <code>imbot</code>, and the "categories" inside,
 * are intentionally not capitalized (as opposed
 * to the class naming convention) to facilitate their use.</p>
 * <br>
 * <a href="https://github.com/telos-matter/Imbot"> Github link.</a>
 * @author telos_matter
 */
public class imbot {

	/**
	 * Whenever randomness is needed, we get it from here
	 */
	private static Random RAND = new Random();

	/**
	 * The singleton robot instance that does all the work.
	 */
	private static final Robot BOT;

	static {
		try {
			BOT = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
		} catch (AWTException e) {
			System.err.println("Unable to initialize Imbot.");
			throw new RuntimeException(e);
		}

		BOT.setAutoDelay(0);
		BOT.setAutoWaitForIdle(true);
	}

	/**
	 * All utilities related to the mouse
	 */
	public static class mse {

		private static final int LEFT_BUTTON = InputEvent.getMaskForButton(1);
		private static final int RIGHT_BUTTON = InputEvent.getMaskForButton(3);

		/**
		 * Cooldown duration for {@link #robotMove(int, int)}
		 * in nanoseconds. This is to avoid false
		 * triggers of exitOnInterruption when there
		 * is continuous consecutive rapid movements.
		 */
		private static final long COOLDOWN_DURATION = 2_500_000;
		/**
		 * When was {@link #robotMove(int, int)} last called
		 * according to {@link System#nanoTime()}
		 */
		private static long previous = System.nanoTime();

		/**
		 * Should the movement be realistic (human like)
		 * or robotic (linear / instant movements).
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
		 * robot.mouseMove is actually
		 * called
		 */
		private static void robotMove (int x, int y) {
			// Check if the cooldown has elapsed
			long elapsedTime = System.nanoTime() - previous;
			long cooldownRemainedMs = (COOLDOWN_DURATION - elapsedTime) / 1_000_000;
			if (cooldownRemainedMs > 0) {
				// If not, sleep what's left
				util.sleep(cooldownRemainedMs);
			}

			// Then, move and register last location. In this order
			BOT.mouseMove(x, y);
			InterruptionHandler.setLastLocation(x, y);

			// And mark for next cooldown
			previous = System.nanoTime();
		}

		/**
		 * <p>Simulate user-like mouse movement to the passed x and y
		 * trough <a href= "https://github.com/BenLand100">Benjamin
		 * J. Land</a>s'
		 * <a href= "https://ben.land/post/2021/04/25/windmouse-human-mouse-movement/">
		 *     WindMouse algorithm
		 * </a>.
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
			final double speed = (RAND.nextDouble()*15 +15)/5;
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

			while ((dist = Math.hypot(moving_x - x,moving_y - y)) >= 2) {
				wind = Math.min(wind, dist);

				if (dist >= targetArea) {
					wind_x = wind_x/sqrt_3 + (2*RAND.nextDouble() -1)*wind/sqrt_5;
					wind_y = wind_y/sqrt_3 + (2*RAND.nextDouble() -1)*wind/sqrt_5;
				} else {
					wind_x /= sqrt_3;
					wind_y /= sqrt_3;

					if (max_step < 3) {
						max_step = RAND.nextDouble()*3 +3;
					} else {
						max_step /= sqrt_5;
					}
				}

				velocity_x += wind_x +gravity*(x -moving_x)/dist;
				velocity_y += wind_y +gravity*(y -moving_y)/dist;

				double velocity_m = Math.hypot(velocity_x, velocity_y);
				if (velocity_m > max_step) {
					double random_dist = max_step/2 +RAND.nextDouble()*max_step/2D;
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
			BOT.mousePress(LEFT_BUTTON);
			BOT.mouseRelease(LEFT_BUTTON);
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
			BOT.mousePress(RIGHT_BUTTON);
			BOT.mouseRelease(RIGHT_BUTTON);
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
			BOT.mousePress(LEFT_BUTTON);
			move(to_x, to_y);
			BOT.mouseRelease(LEFT_BUTTON);
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
			BOT.mousePress(LEFT_BUTTON);
			move(to_x,to_y);
			BOT.mouseRelease(LEFT_BUTTON);
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
			duration = (RAND.nextDouble()*2*delta + (duration - delta)) * 1_000_000_000 ;

			// Hover until duration has elapsed
			while (duration >= 0) {
				long start = System.nanoTime();
				// Get random x and y within square
				int x = (int)(RAND.nextDouble()*side + minX);
				int y = (int)(RAND.nextDouble()*side + minY);
				// Move to x and y
				move(x, y);
				// Subtract however long this took
				duration -= System.nanoTime() - start; // duration -= (end - start)
			}
		}

		/**
		 * Goes to the center of the screen
		 */
		public static void goToCenter () {
			move(scr.CENTER);
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
			BOT.keyPress(keyCode);
		}

		/**
		 * Releases a key.
		 * @param keyCode KeyCode of the key to release. (e.g. KeyEvent.VK_A)
		 */
		public static void release (int keyCode) {
			BOT.keyRelease(keyCode);
		}

		/**
		 * @param c	char to write
		 */
		public static void type (char c) {
			int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
			BOT.keyPress(keyCode);
			BOT.keyRelease(keyCode);
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
		 * The center of your screen.
		 * READ-ONLY, do not edit.
		 */
		public static final Point CENTER = new Point(WIDTH/2, HEIGHT/2);
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

		/**
		 * Get the color at that location
		 * @return the {@link Color} of the pixel at the specified
		 * location on the screen
		 */
		public static Color color(int x, int y) {
			return BOT.getPixelColor(x, y);
		}

		/**
		 * @see #color(int, int)
		 */
		public static Color color(Point point) {
			return color(point.x, point.y);
		}

		/**
		 * @return the color at the current location of the
		 * cursor
		 * @see #color(int, int)
		 * @see mse#location()
		 */
		public static Color color() {
			return color(mse.location());
		}

		/**
		 * Is the color of the pixel at <code>x</code>
		 * and <code>y</code>
		 * similar to this one?
		 * @param color to check against
		 * @param tolerance	a percentage of how dissimilar can it be
		 *                  0 -> must be exact
		 *                  100 -> any color matches
		 */
		public static boolean isColor (int x, int y, Color color, float tolerance) {
			return img.isColorSimilar(color(x, y), color, tolerance);
		}

		/**
		 * @see #isColor(int, int, Color, float)
		 */
		public static boolean isColor(Point point, Color color, float tolerance) {
			return isColor(point.x, point.y, color, tolerance);
		}

		/**
		 * @return a screenshot of the specified
		 * zone on the screen. Does not include the cursor
		 */
		public static BufferedImage captureScreen (Rectangle zone) {
			return BOT.createScreenCapture(zone);
		}

		/**
		 * @return a screenshot of the entire screen.
		 * Does not include the cursor
		 */
		public static BufferedImage captureScreen () {
			return captureScreen (SCREEN_RECTANGLE);
		}

		/**
		 * Continuously search for an <code>image</code>
		 * on the specified <code>zone</code> of the screen
		 * for the specified amount of time.
		 * @param image the image to look for
		 * @param zone the zone on the screen to look in. Must be of
		 *                   equal or greater size compared to <code>image</code>
		 * @param screenRelative whether the returned point is relative
		 *                       to the screen, or relative to the
		 *                       given <code>zone</code>
		 * @param duration of search in seconds. 0 will search only once,
		 *                meanwhile negative values mean search indefinitely
		 * @param differenceThreshold see {@link img#locateSubImage(BufferedImage, BufferedImage, float, float)}
		 * @param colorTolerance see {@link img#locateSubImage(BufferedImage, BufferedImage, float, float)}
		 * @return a {@link Point} representing the upper-left corner of where
		 * <code>image</code> has been found, relative to the screen
		 * or relative to the given <code>zone</code>, depending
		 * on <code>screenRelative</code>. Or <code>null</code> if
		 * <code>image</code> was not found.
		 * @see img#locateSubImage(BufferedImage, BufferedImage, float, float)
		 * @note Know that some screens have a different number of physical pixels
		 * than the number of "displayed pixels". Taking a normal screenshot
		 * would usually use the "displayed pixels". This taken screenshot would not
		 * work with this method as that the screen captures taken
		 * by {@link #captureScreen(Rectangle)} use the physical pixels. To never
		 * encounter this problem; save your screenshots using
		 * {@link #captureScreen(Rectangle)} and {@link file#saveImage(BufferedImage, String, String)}
		 * and then use that image if you want to locate something. Searching
		 * could also sometimes fail because the format under which you saved the image
		 * is lossy.
		 */
		public static Point search (BufferedImage image, Rectangle zone, boolean screenRelative, double duration, float differenceThreshold, float colorTolerance) {
			// Convert to nanoseconds or infinity
			duration = (duration >= 0)? duration * 1_000_000_000 : Double.POSITIVE_INFINITY;

			// Start looking for duration amount
			Point location;
			do {
				long start = System.nanoTime();
				location = img.locateSubImage(image, captureScreen(zone), differenceThreshold, colorTolerance);
				duration -= (System.nanoTime() - start);

			} while (location == null && duration >= 0);

			// If the location should be screen relative, then adjust it
			if (screenRelative && location != null) {
				location.translate(zone.x, zone.y);
			}

			return location;
		}

		/**
		 * Continuously search for an image
		 * on the entire screen for
		 * a given duration.
		 * @see #search(BufferedImage, Rectangle, boolean, double, float, float)
		 */
		public static Point search (BufferedImage image, double duration, float differenceThreshold, float colorTolerance) {
			return search(image, SCREEN_RECTANGLE, false, duration, differenceThreshold, colorTolerance); // screenRelative does not matter
		}
	}

	/**
	 * All utilities related
	 * to images and colors
	 */
	public static class img {

		/**
		 * A constant needed in {@link #isColorSimilar(int, int, float)}
		 */
		private static final float MAX_RGB_DISTANCE = (float)(255 * Math.sqrt(3));

		/**
		 * What {@link #isColorSimilar(Color, Color, float)} actually
		 * uses. And other methods.
		 * Uses 3D Euclidean distance.
		 * @param tolerance a percentage between 0 and 100
		 * @implNote this method is called millions
		 * of times, so it's optimized for speed
		 * and not ease of reading.
		 */
		private static boolean isColorSimilar (int rgbA, int rgbB, float tolerance) {
			// If there is no tolerance, then the colors must be exact
			if (tolerance == 0) {
				return rgbA == rgbB;
			}

			// Define needed variables
			int rA, gA, bA, rB, gB, bB;

			// Now calculate the distance, to do so:
			// - First unpack the components:
			rA = rgbA >> 16 & 0xFF; // Red 	  in A
			gA = rgbA >> 8  & 0xFF; // Green  in A
			bA = rgbA       & 0xFF; // Blue   in A
			rB = rgbB >> 16 & 0xFF; // Red   in B
			gB = rgbB >> 8  & 0xFF; // Green in B
			bB = rgbB       & 0xFF; // Blue  in B

			// - Then calculate the 3D distance between the components
			// We will not need the variables anymore so we can use them
			// to store the result
			rA -= rB; rA *= rA; // (rA - rB) ^ 2
			gA -= gB; gA *= gA; // (gA - gB) ^ 2
			bA -= bB; bA *= bA; // (bA - bB) ^ 2

			// And while calculating the distance, might just as well
			// normalize the value and do one write
			final float normalizedDist = ((float) Math.sqrt(rA + gA + bA)) / MAX_RGB_DISTANCE;

			// Finally compare
			return (normalizedDist * 100) <= tolerance; // It's better to multiply than to divide
		}

		/**
		 * Are these two colors similar?
		 * @param tolerance a percentage of how dissimilar they can be
		 *                  0 -> must be the exact same color
		 *                  100 -> any two colors are the same
		 */
		public static boolean isColorSimilar (Color a, Color b, float tolerance) {
			Exceptions.requirePercentage(tolerance);
			return isColorSimilar(a.getRGB(), b.getRGB(), tolerance);
		}

		/**
		 * Locates an image within another
		 * @param subImage the image to look for
		 * @param bigImage	the image to look in
		 * @param differenceThreshold a percentage how many pixels can
		 *                            be different and still
		 *                            count as the same image.
		 *                            - 0 -> all pixels must be the same
		 *                            - 100 -> any subImage would match
		 * @param colorTolerance a percentage of how different can two colors be and
		 *                       still count as the same color, and not
		 *                       count towards the difference threshold.
		 *                       - 0 -> must be the exact color.
		 *                       - 100 -> any color would match.
		 * @return a {@link Point} representing the upper-left corner of where
		 * the <code>subImage</code> has been found in the
		 * <code>bigImage</code>, or <code>null</code> if
		 * the <code>subImage</code> is not inside the <code>bigImage</code>
		 *
		 * @throws IllegalArgumentException if <code>subImage</code> is
		 * bigger than <code>bigImage</code>
		 */
		public static Point locateSubImage (BufferedImage subImage, BufferedImage bigImage, float differenceThreshold, float colorTolerance) {
			if (subImage.getWidth() > bigImage.getWidth() || subImage.getHeight() > bigImage.getHeight()) {
				throw new IllegalArgumentException("The subImage is bigger than the bigImage");
			}
			Exceptions.requirePercentage(differenceThreshold);
			Exceptions.requirePercentage(colorTolerance);

			// Get how far should we check
			int maxX = bigImage.getWidth() - subImage.getWidth();
			int maxY = bigImage.getHeight() - subImage.getHeight();

			// Try and match subImage to bigImage at x and y
			for (int x = 0; x <= maxX; x++) {
				for (int y = 0; y <= maxY; y++) {
					if (isSubImage(subImage, bigImage, x, y, differenceThreshold,  colorTolerance)) {
						return new Point (x, y);
					}
				}
			}

			return null;
		}

		/**
		 * Helper method for
		 * {@link #locateSubImage(BufferedImage, BufferedImage, float, float)}.
		 * Checks if <code>small</code> matches <code>big</code> at
		 * <code>x</code> and <code>y</code>.
		 */
		private static boolean isSubImage (BufferedImage small, BufferedImage big, int startX, int startY, float differenceThreshold, float colorTolerance) {
			// Convert differenceThreshold percentage to actual value
			//					= (how many pixels are there)		   * (percentage)
			differenceThreshold = (small.getWidth()*small.getHeight()) * (differenceThreshold/100);

			// Needed variables
			int smallRGB, bigRGB;

			// If there is no colorTolerance, then simply compare
			// the pixels. No need to check a million time
			// if there is no colorTolerance in isColorSimilar
			if (colorTolerance == 0) {
				for (int x = 0; x < small.getWidth(); x++) {
					for (int y = 0; y < small.getHeight(); y++) {
						smallRGB = small.getRGB(x, y);
						bigRGB = big.getRGB(startX + x, startY + y);
						if (smallRGB != bigRGB) {
							if ((--differenceThreshold) < 0) {
								return false;
							}
						}
					}
				}

				return true;
			}

			// Otherwise, go through isColorSimilar, it's fine
			for (int x = 0; x < small.getWidth(); x++) {
				for (int y = 0; y < small.getHeight(); y++) {
					smallRGB = small.getRGB(x, y);
					bigRGB = big.getRGB(startX + x, startY + y);
					if (!isColorSimilar(smallRGB, bigRGB, colorTolerance)) {
						if ((--differenceThreshold) < 0) {
							return false;
						}
					}
				}
			}

			return true;
		}

		/**
		 * @return an array containing the RGB values that the int value represents
		 */
		public static int [] toRGBArray (int value) {
			int [] rgb = new int [3];
			rgb[0] = (value >> (16)) & 0xFF; // Red
			rgb[1] = (value >>  (8)) & 0xFF; // Green
			rgb[2] = (value >>  (0)) & 0xFF; // Blue
			return rgb;
		}

		/**
		 * Iterates over the pixels of the two images
		 * and returns the absolute difference between
		 * the colors. Each color component is subtracted
		 * individually, and the returned array contains those components
		 * back together. For example, if <code>imageA</code>'s first
		 * pixel is <code>(100, 20, 255)</code> and <code>imageB</code>'s
		 * first pixel is <code>(50, 100, 200)</code>, then
		 * the first element of the returned array would be
		 * the RGB value of this color <code>(50, 80, 55)</code>.
		 * @return an array representing the absolute difference
		 * between each pixel in the two images.
		 * @throws IllegalArgumentException if the two images are not the same size
		 */
		public static int [][] imageDifference (BufferedImage imageA, BufferedImage imageB) {
			if (imageA.getWidth() != imageB.getWidth() ||
					imageA.getHeight() != imageB.getHeight()) {
				throw new IllegalArgumentException("Images must be of the same size!");
			}

			// Needed variables
			int rgbA, rA, gA, bA, rgbB, rB, gB, bB;
			// Where to put the result
			int [][] diff = new int[imageA.getHeight()][imageA.getWidth()];
			for (int j = 0; j < imageA.getHeight(); j++) {
				for (int i = 0; i < imageA.getWidth(); i++) {
					// Get the colors
					rgbA = imageA.getRGB(i, j);
					rgbB = imageB.getRGB(i, j);
					// Get the components
					rA = rgbA >> 16 & 0xFF; // Red 	  in A
					gA = rgbA >> 8  & 0xFF; // Green  in A
					bA = rgbA       & 0xFF; // Blue   in A
					rB = rgbB >> 16 & 0xFF; // Red   in B
					gB = rgbB >> 8  & 0xFF; // Green in B
					bB = rgbB       & 0xFF; // Blue  in B
					// Get the difference
					rA = Math.abs(rA - rB);
					gA = Math.abs(gA - gB);
					bA = Math.abs(bA - bB);
					// Reconstruct the color
					rgbA = 0;
					rgbA |= rA << 16 | gA << 8 | bA;
					diff[j][i] = rgbA;
				}
			}

			return diff;
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
	 * All utilities related
	 * to reading from and saving
	 * to files
	 */
	public static class file {

		/**
		 * @return the image at <code>path</code> or <code>null</code> if unable
		 * to read it.
		 */
		public static BufferedImage readImage (String path) {
			try {
				return ImageIO.read(new File(path));
			} catch (IOException e) {
				return null;
			}
		}

		/**
		 * Saves the given image as a PNG under the given path + name
		 * @param image	image to save
		 * @param folderPath path of the folder in which to store the image. For example `/Users/telos_matter/Pictures`
		 * @param name name under which to store the image. For example `dog_picture`
		 * @return <code>true</code> if the image was saved successfully,
		 * <code>false</code> otherwise.
		 */
		public static boolean saveImage (BufferedImage image, String folderPath, String name) {
			try {
				return ImageIO.write(image, "png", new File(folderPath +File.separatorChar +name +".png"));
			} catch (IOException e) {
				return false;
			}
		}

		/**
		 * Read the content the entire content of
		 * the file at <code>path</code>.
		 * @return the content or <code>null</code>
		 * if it was unable to read it.
		 */
		public static String read (String path) {
			try {
				FileReader fileReader = new FileReader(new File(path));
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String content = bufferedReader.
						lines().
						collect(Collectors.joining());

				bufferedReader.close();
				fileReader.close();

				return content;
			} catch (IOException e) {
				return null;
			}
		}

		/**
		 * Either appends the given <code>content</code>
		 * to the given file at <code>path</code>, or
		 * override it.
		 * @return <code>true</code> if all went well,
		 * <code>false</code> otherwise.
		 */
		public static boolean write (String path, String content, boolean append) {
			try {
				FileWriter fileWriter = new FileWriter(new File(path), append);
				fileWriter.write(content);
				fileWriter.flush();
				fileWriter.close();
				return true;

			} catch (IOException e) {
				return false;
			}
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
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * @param secs seconds to sleep
		 */
		public static void sleeps (double secs) {
			sleep ((long)(secs *1000));
		}

		/**
		 * Sleep a random amount of seconds between
		 * secs -delta and secs +delta
		 */
		public static void sleepsRandom(double secs, double delta) {
			double amount = RAND.nextDouble()*2*delta + (secs - delta);
			sleeps(amount);
		}

		/**
		 * @return whether the user is trying
		 * to interrupt the program trough
		 * mouse movements or not.
		 */
		public static boolean isUserInterrupting () {
			return InterruptionHandler.isUserInterrupting();
		}
	}

	/**
	 * A utility class that
	 * is used internally to
	 * validate arguments.
	 */
	private static class Exceptions {

		/**
		 * Must be between 0 and 100
		 */
		public static void requirePercentage (double percentage) {
			if (percentage < 0 || percentage > 100) {
				throw new IllegalArgumentException("Percentages must be between 0 and 100, not %f".formatted(percentage));
			}
		}
	}

	/**
	 * Where the exit-on-interruption mechanism
	 * and its components reside.
	 */
	private static class InterruptionHandler {

		/**
		 * If the main thread is dead, then the
		 * thread used here should also end.
		 */
		private static final Thread MAIN_THREAD = Thread.currentThread();


		/**
		 * Whether we should actually exit if
		 * the user is trying to interrupt or not
		 */
		private static volatile boolean exitOnInt = true;

		/**
		 * A volatile double that will hold
		 * two ints representing the x and y
		 * of the last location moved to
		 * by the program.
		 * It's like so, so that the reads
		 * and writes are atomic.
		 */
		private static volatile long lastLocation;

		/**
		 * Used when assigning the x and y in
		 * lastLocation
		 */
		private static final long MAX_INT = 0x0000_0000_FFFF_FFFFL;

		/**
		 * User defined runnable to be called
		 * when an exit occurs.
		 */
		private static Runnable exitRunnable;

		// Initialize
		static {
			// Initialize lastLocation with the current location
			Point location = mse.location();
			setLastLocation(location.x, location.y);

			// Start the thread that will continuously check
			// if the user is trying to interrupt
			Thread thread = new Thread(() -> {
				while (true) {
					if (exitOnInt && isUserInterrupting()) {
						exit();
						break;
					}

					if (!MAIN_THREAD.isAlive()) {
						break;
					}
				}
			});

			thread.start();
		}

		/**
		 * Called by the mouse whenever it moves
		 * to store its last location
		 */
		private static void setLastLocation (int x, int y) {
			long l = x & MAX_INT;
			l <<= 32;
			l |= y & MAX_INT;

			// x is stored in first 32 bits and y in the second 32 bits
			lastLocation = l;
		}

		/**
		 * What checks if the user is trying
		 * to interrupt.
		 * Please don't false trigger. (I'm begging the
		 * method, not asking you to do something.)
		 */
		private static boolean isUserInterrupting() {
			// Wait until it's no longer moving
			BOT.waitForIdle();

			// Get the current location
			Point currentLocation = mse.location();

			// Unpack the last location
			long l = lastLocation;
			int x = (int) ((l >> 32) & MAX_INT);
			int y = (int) (l & MAX_INT);

			// Compare
			return x != currentLocation.x || y != currentLocation.y;
		}

		/**
		 * Called when the user has been
		 * trying to interrupt and exitOnInt
		 * is set to <code>true</code>.
		 * Only called on the checking thread.
		 */
		private static void exit () {
			System.out.println("Imbot was interrupted.");

			// Check if we have an exitRunnable to run
			if (exitRunnable != null) {
				exitRunnable.run();
			}

			// Finally, do what Itachi did
			System.exit(0);
		}
	}

	/**
	 * Set the randomness seed again.
	 * The initial by-default seed value
	 * is random.
	 * @param seed the new seed or <code>null</code>
	 *             for a new random seed
	 */
	public static void setRandomnessSeed (Long seed) {
		RAND = (seed == null)? new Random() : new Random(seed);
	}

	/**
	 * Whether <code>imbot</code> should exit, trough
	 * {@link System#exit(int)},
	 * if the user is trying to interrupt it trough
	 * mouse movements.
	 * If this false triggers when there is rapid
	 * movements please
	 * increase {@link mse#COOLDOWN_DURATION} by
	 * a couple ms.
	 */
	public static void setExitOnInterruption (boolean value) {
		InterruptionHandler.exitOnInt = value;
	}

	/**
	 * Is ExitOnInterruption on or off?
	 * @see #setExitOnInterruption(boolean)
	 */
	public static boolean isExitOnInterruption () {
		return InterruptionHandler.exitOnInt;
	}

	/**
	 * Set the runnable that will get called
	 * when the program ends because of
	 * an interruption. Useful
	 * if you want to save the state
	 * or the program to resume it
	 * or something...
	 * Can be <code>null</code> if
	 * you want nothing to run.
	 */
	public static void setExitRunnable (Runnable runnable) {
		InterruptionHandler.exitRunnable = runnable;
	}

	/**
	 * @return the exitRunnable
	 * @see #setExitRunnable(Runnable)
	 */
	public static Runnable getExitRunnable () {
		return InterruptionHandler.exitRunnable;
	}

	/**
	 * Groups pre-defined
	 * utility programs that use imbot.
	 * These are like samples
	 * of how one may use imbot, but
	 * they are also useful.
	 */
	public static class progs {

		/**
		 * Continuously report the current location
		 * of the mouse and the color of the pixel
		 * it's on in hex.
		 * Useful when trying to hard code location
		 * of certain elements into a script.
		 * @param secs period in seconds between each report
		 * @implNote The method never exits,
		 * as it contains an infinite loop. It's not meant
		 * to be used within a script.
		 */
		public static void reportLocation (double secs) {
			setExitOnInterruption(false);

			while (true) {
				util.sleeps(secs);
				Point location = mse.location();
				Color color = scr.color(location);
				System.out.printf("(%d, %d) -> 0x%02X%02X%02X%n",
						location.x, location.y,
						color.getRed(), color.getGreen(), color.getBlue());
			}
		}

		/**
		 * <p>Continuously report the location of the mouse
		 * and the color of the pixel at that location
		 * trough a GUI.</p>
		 * <p>Useful when trying to hard code locations
		 * of certain elements into a script.</p>
		 * <ul>Type (while the window is in focus):
		 * <li><strong>&lt;space&gt;</strong>: to copy the color into the clipboard</li>
		 * <li><strong>l</strong>: to copy the location into the clipboard</li>
		 * <li><strong>&lt;enter&gt;</strong>: to copy the location and the color into the clipboard</li>
		 * <li><strong>q</strong>: to quit</li>
		 * </ul>
		 */
		public static void reportLocation () {
			setExitOnInterruption(false);

			// Construct the JFrame
			JFrame frame = new JFrame ("Imbot");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLocationRelativeTo(null);
			frame.setResizable(true);
			// Add a keyListener for the keystrokes
			frame.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					Point location = mse.location();
					Color color = scr.color(location);

					switch (e.getKeyChar()) {
						case ' ' -> cb.copy(String.format("0x%02X%02X%02X",
								color.getRed(), color.getGreen(), color.getBlue()));

						case 'l', 'L' -> cb.copy(String.format("(%d, %d)",
								location.x, location.y));

						case '\n' -> cb.copy(String.format("(%d, %d) -> 0x%02X%02X%02X",
								location.x, location.y,
								color.getRed(), color.getGreen(), color.getBlue()));

						case 'q', 'Q' -> {
							frame.dispose();
							System.exit(0);
						}
					}
				}
			});

			// Populate the JFrame
			JPanel panel = new JPanel ();
			panel.setLayout(new GridLayout (3, 1));

			JLabel xLabel = new JLabel("x: 0000", SwingConstants.CENTER);
			JLabel yLabel = new JLabel("y: 0000", SwingConstants.CENTER);
			JLabel colorLabel = new JLabel("0x000000", SwingConstants.CENTER);

			panel.add(xLabel);
			panel.add(yLabel);
			panel.add(colorLabel);

			frame.setContentPane(panel);
			frame.setSize(150, 100);
			frame.setVisible(true);

			// Update the GUI
			while (true) {
				util.sleep(50);

				EventQueue.invokeLater(() -> {
					Point location = mse.location();
					Color color = scr.color(location);

					// Set the background to the current color
					panel.setBackground(color);

					// Check if the color is light or dark for text visibility
					double luma = 0.2126*color.getRed() +
							0.7152*color.getGreen() +
							0.0722*color.getBlue();
					// The color is dark, make the text color white
					if (luma < 100) {
						xLabel.setForeground(Color.WHITE);
						yLabel.setForeground(Color.WHITE);
						colorLabel.setForeground(Color.WHITE);
					// The color is light, make the text color black
					} else {
						xLabel.setForeground(Color.BLACK);
						yLabel.setForeground(Color.BLACK);
						colorLabel.setForeground(Color.BLACK);
					}

					// Update the labels
					xLabel.setText("x: " +location.x);
					yLabel.setText("y: " +location.y);
					colorLabel.setText(String.format("0x%02X%02X%02X",
							color.getRed(), color.getGreen(), color.getBlue()));
				});
			}
		}

		/**
		 * Keep the computer awake, until
		 * you come back,
		 * by periodically moving the mouse
		 */
		public static void stayAwake () {
			setExitOnInterruption(true);
			mse.setRealistic(true);

			mse.move(scr.CENTER.x +50, scr.CENTER.y +50);

			final double SECS = 2.5;
			final double DELTA = 0.5;
			// Keep going around in a square
			while (true) {
				util.sleepsRandom(SECS, DELTA);
				mse.slide(0, -100);
				util.sleepsRandom(SECS, DELTA);
				mse.slide(-100, 0);
				util.sleepsRandom(SECS, DELTA);
				mse.slide(0, 100);
				util.sleepsRandom(SECS, DELTA);
				mse.slide(100, 0);
			}
		}
	}

}
