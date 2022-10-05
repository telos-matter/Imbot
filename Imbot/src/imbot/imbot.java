package imbot;

import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import exceptions.AlreadyInitException;
import exceptions.OutOfScreenBoundsException;

public class imbot {

	private static final int LEFT_BUTTON = InputEvent.getMaskForButton(1);
	private static final int RIGHT_BUTTON = InputEvent.getMaskForButton(3);
	
	public static final int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
	public static final int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
	public static final Rectangle SCREEN_RECTANGLE = new Rectangle (0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
	
	private static Robot robot = null;
	private static Point last_location = new Point (-1, -1);
	private static boolean exit_int = false;
	
	public static void init () {
		init (0, true);
	}
	
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
	
	public static void sleepS (double s) {
		sleep ((long)(s *1000));
	}
	
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
	
	public static void write (String s) {
		for (char c : s.toCharArray()) {
			write (c);
		}
	}
	
	public static void write (char c) {
		safeExitIfInt();
		int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
		robot.keyPress(keyCode);
		robot.keyRelease(keyCode);
		safeExitIfInt();
	}
	
	public static Point getLocation () {
		return MouseInfo.getPointerInfo().getLocation();
	}
	
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
	
	public static void slide (int dx, int dy) {
		Point p = getLocation();
		move(p.x +dx, p.y +dy);
	}
	
	public static void click (int x, int y) {
		move(x, y);
		click();
	}
	
	public static void click () {
		safeExitIfInt();
		robot.mousePress(LEFT_BUTTON);
		robot.mouseRelease(LEFT_BUTTON);
		safeExitIfInt();
	}
	
	public static void clickRight (int x, int y) {
		move(x, y);
		clickRight();
	}
	
	public static void clickRight () {
		safeExitIfInt();
		robot.mousePress(RIGHT_BUTTON);
		robot.mouseRelease(RIGHT_BUTTON);
		safeExitIfInt();
	}
	
	public static void drag (int x, int y) {
		safeExitIfInt();
		robot.mousePress(LEFT_BUTTON);		
		move(x,y);
		robot.mouseRelease(LEFT_BUTTON);
		safeExitIfInt();
	}
	
	public static void drag (int from_x, int from_y, int to_x, int to_y) {
		move(from_x, from_y);
		robot.mousePress(LEFT_BUTTON);	
		move(to_x, to_y);
		robot.mouseRelease(LEFT_BUTTON);
		safeExitIfInt();
	}
	
	public static void safeExitIfInt () {
		if (exit_int) {
			exitIfInt();
		}
	}
	
	public static void exitIfInt () {
		if (isUserControlling()) {
			System.exit(0);
		}
	}
	
	public static boolean isUserControlling () {
		return !last_location.equals(getLocation());
	}
	
	public static void reportLocation (int ms) {
		while(true) {
			sleep(ms);
			System.out.println(getLocation());
		}
	}
	
	public static void setExitIfInt (boolean exit_on_interruption) {
		exit_int = exit_on_interruption;
	}
	
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
