package com.runescape;

import com.runescape.Client.ScreenMode;
import com.runescape.cache.graphics.RSInterface;
import com.runescape.drawing.RSImageProducer;

import vscape.ClientSettings;
import vscape.SettingsManager;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

public class RSApplet extends Applet implements Runnable, MouseListener, MouseMotionListener, MouseWheelListener,
		KeyListener, FocusListener, WindowListener {

	private static final long serialVersionUID = 1L;

	public static int hotKey = 508;
	private int anInt4;
	private int delayTime;
	int minDelay;
	private final long aLongArray7[] = new long[10];
	int fps;
	boolean shouldDebug;
	int myWidth;
	int myHeight;
	Graphics graphics;
	RSImageProducer fullGameScreen;
	RSFrame rsFrame;
	private boolean shouldClearScreen;
	boolean awtFocus;
	int idleTime;
	int clickMode2;
	public int mouseX;
	public int mouseY;
	private int clickMode1;
	private int clickX;
	private int clickY;
	private long clickTime;
	public int clickMode3;
	public int saveClickX;
	public int saveClickY;
	long aLong29;
	final int keyArray[] = new int[128];
	private final int charQueue[] = new int[128];
	public boolean isLoading;
	private int readIndex;
	private int writeIndex;
	public static int anInt34;
	public boolean isApplet;

	public void refreshFrameSize(boolean undecorated, int width, int height, boolean resizable, boolean full) {
		boolean createdByApplet = (isApplet && !full);
		myWidth = width;
		myHeight = height;
		if (rsFrame != null) {
			rsFrame.dispose();
		}
		if (!createdByApplet) {
			rsFrame = new RSFrame(this, width, height, resizable, undecorated);
			rsFrame.addWindowListener(this);
		}
		graphics = (createdByApplet ? this : rsFrame).getGraphics();
		if (!createdByApplet) {
			getGameComponent().addMouseWheelListener(this);
			getGameComponent().addMouseListener(this);
			getGameComponent().addMouseMotionListener(this);
			getGameComponent().addKeyListener(this);
			getGameComponent().addFocusListener(this);
		}
	}

	public boolean appletClient() {
		return rsFrame == null && isApplet == true;
	}

	final void createClientFrame(int w, int h) {
		isApplet = false;
		myWidth = w;
		myHeight = h;
		rsFrame = new RSFrame(this, myWidth, myHeight, Client.frameMode == Client.ScreenMode.RESIZABLE,
				Client.frameMode == Client.ScreenMode.FULLSCREEN);
		rsFrame.setFocusTraversalKeysEnabled(false);
		graphics = getGameComponent().getGraphics();
		fullGameScreen = new RSImageProducer(myWidth, myHeight);
		startRunnable(this, 1);
	}

	final void initClientFrame(int w, int h) {
		isApplet = true;
		myWidth = w;
		myHeight = h;
		graphics = getGameComponent().getGraphics();
		fullGameScreen = new RSImageProducer(myWidth, myHeight);
		startRunnable(this, 1);
	}

	public void run() {
		getGameComponent().addMouseListener(this);
		getGameComponent().addMouseMotionListener(this);
		getGameComponent().addKeyListener(this);
		getGameComponent().addFocusListener(this);
		getGameComponent().addMouseWheelListener(this);
		if (rsFrame != null) {
			rsFrame.addWindowListener(this);
		}
		drawLoadingText(0, "Loading...");
		startup();
		int i = 0;
		int j = 256;
		int k = 1;
		int l = 0;
		int i1 = 0;
		for (int j1 = 0; j1 < 10; j1++) {
			aLongArray7[j1] = System.currentTimeMillis();
		}
		do {
			if (anInt4 < 0) {
				break;
			}
			if (anInt4 > 0) {
				anInt4--;
				if (anInt4 == 0) {
					exit();
					return;
				}
			}
			int k1 = j;
			int i2 = k;
			j = 300;
			k = 1;
			long l2 = System.currentTimeMillis();
			if (aLongArray7[i] == 0L) {
				j = k1;
				k = i2;
			} else if (l2 > aLongArray7[i]) {
				j = (int) ((long) (2560 * delayTime) / (l2 - aLongArray7[i]));
			}
			if (j < 25) {
				j = 25;
			}
			if (j > 256) {
				j = 256;
				k = (int) ((long) delayTime - (l2 - aLongArray7[i]) / 10L);
			}
			if (k > delayTime) {
				k = delayTime;
			}
			aLongArray7[i] = l2;
			i = (i + 1) % 10;
			if (k > 1) {
				for (int j2 = 0; j2 < 10; j2++) {
					if (aLongArray7[j2] != 0L) {
						aLongArray7[j2] += k;
					}
				}

			}
			if (k < minDelay) {
				k = minDelay;
			}
			try {
				Thread.sleep(k);
			} catch (InterruptedException interruptedexception) {
				i1++;
			}
			for (; l < 256; l += j) {
				clickMode3 = clickMode1;
				if (Client.pixelScaling > 1 && Client.frameMode == ScreenMode.FIXED) {
					saveClickX = clickX / Client.pixelScaling;
					saveClickY = clickY / Client.pixelScaling;
				} else {
					saveClickX = clickX;
					saveClickY = clickY;
				}
				aLong29 = clickTime;
				clickMode1 = 0;
				doLogic();
				readIndex = writeIndex;
			}

			l &= 0xff;
			if (delayTime > 0) {
				fps = (1000 * j) / (delayTime * 256);
			}
			repaintGame();
			if (shouldDebug) {
				System.out.println((new StringBuilder()).append("ntime:").append(l2).toString());
				for (int k2 = 0; k2 < 10; k2++) {
					int i3 = ((i - k2 - 1) + 20) % 10;
					System.out.println((new StringBuilder()).append("otim").append(i3).append(":")
							.append(aLongArray7[i3]).toString());
				}

				System.out.println((new StringBuilder()).append("fps:").append(fps).append(" ratio:").append(j)
						.append(" count:").append(l).toString());
				System.out.println((new StringBuilder()).append("del:").append(k).append(" deltime:").append(delayTime)
						.append(" mindel:").append(minDelay).toString());
				System.out.println(
						(new StringBuilder()).append("intex:").append(i1).append(" opos:").append(i).toString());
				shouldDebug = false;
				i1 = 0;
			}
		} while (true);
		if (anInt4 == -1) {
			exit();
		}
	}

	private void exit() {
		anInt4 = -2;
		shutdown();
		if (rsFrame != null) {
			try {
				Thread.sleep(1000L);
			} catch (Exception exception) {
			}
			try {
				System.exit(0);
			} catch (Throwable throwable) {
			}
		}
	}

	final void method4(int i) {
		delayTime = 1000 / i;
	}

	public final void start() {
		if (anInt4 >= 0) {
			anInt4 = 0;
		}
	}

	public final void stop() {
		if (anInt4 >= 0) {
			anInt4 = 4000 / delayTime;
		}
	}

	public final void destroy() {
		SettingsManager.write();
		anInt4 = -1;
		try {
			Thread.sleep(5000L);
		} catch (Exception exception) {
		}
		if (anInt4 == -1) {
			exit();
		}
	}

	@Override
	public final void update(Graphics g) {
		if (graphics == null) {
			graphics = g;
		}
		shouldClearScreen = true;
		redraw();
	}

	@Override
	public final void paint(Graphics g) {
		if (graphics == null) {
			graphics = g;
		}
		shouldClearScreen = true;
		redraw();
	}

	// TODO does this shit even need improving
	// TODO MOVE TO THE CLIENT AND USE OVERRIDES
	public void mouseWheelMoved(MouseWheelEvent event) {
		final int rotation = event.getWheelRotation();
		if (!Client.loggedIn) {
			return;
		}
		final boolean handledInterfaceScroll = handleInterfaceScrolling(event);
		if (handledInterfaceScroll)
			return;
		if (Client.instance.backDialogID == -1 && Client.instance.isChatArea()) {
			Rectangle chatBounds = Client.gameFrame.getChatBounds();
			final int chatHeight = (int) chatBounds.getHeight();
			if (Client.instance.inputDialogState >= 3 && Client.instance.inputDialogState <= 5) {
				int scrollPos = Client.searchResultScrollPos;
				scrollPos += rotation * 30;
				if (scrollPos > Client.searchResultScrollMax)
					scrollPos = Client.searchResultScrollMax;
				if (scrollPos < 0)
					scrollPos = 0;
				if (Client.searchResultScrollPos != scrollPos) {
					Client.searchResultScrollPos = scrollPos;
					Client.redrawChatbox = true;
				}
			} else {
				int scrollPos = Client.chatScrollPos;
				scrollPos -= rotation * 30;
				if (scrollPos > Client.chatScrollMax - chatHeight)
					scrollPos = Client.chatScrollMax - chatHeight;
				if (scrollPos < 0)
					scrollPos = 0;
				if (Client.chatScrollPos != scrollPos) {
					Client.chatScrollPos = scrollPos;
					Client.redrawChatbox = true;
				}
			}
		} else {
			if (SettingsManager.zoomControl) {
				if (Client.getClient().canClick()) {
					if (Client.openInterfaceID != -1 || Client.cameraZoom <= Client.cameraZoomMin && rotation < 0
							|| Client.cameraZoom >= Client.cameraZoomMax && rotation > 0) {
						return;
					}
					Client.cameraZoom += 25 * rotation;
				}
			}
		}
	}

	// TODO does this shit even need improving
	public boolean handleInterfaceScrolling(MouseWheelEvent event) {
		final int rotation = event.getWheelRotation();
		int offsetX = 0;
		int offsetY = 0;
		/* Tab interface scrolling */
		int tabInterfaceID = Client.tabInterfaceIDs[Client.tabID];
		if (tabInterfaceID == -1 && Client.openInterfaceID == -1) {
			return false;
		}
		if (tabInterfaceID != -1) {
			RSInterface[] components = RSInterface.interfaceCache[tabInterfaceID];
			offsetX = Client.frameMode == ScreenMode.FIXED ? Client.frameWidth - 218
					: (Client.frameMode == ScreenMode.FIXED ? 28 : Client.frameWidth - 197);
			offsetY = Client.frameMode == ScreenMode.FIXED ? Client.frameHeight - 298
					: (Client.frameMode == ScreenMode.FIXED ? 37
							: Client.frameHeight - (Client.frameWidth >= 900 ? 37 : 74) - 267);
			for (int childID = 0; childID < components.length; childID++) {
				RSInterface rsInterface = components[childID];
				if (rsInterface == null)
					continue;
				if (rsInterface.type != 0)
					continue;
				if (rsInterface.scrollMax <= 0)
					continue;
				int childX = rsInterface.x + offsetX;
				int childY = rsInterface.y + offsetY;
				if (mouseX > childX && mouseY > childY && mouseX < childX + rsInterface.width
						&& mouseY < childY + rsInterface.height) {
					rsInterface.scrollPosition += rotation * 30;
					Client.redrawTab = true;
					return true;
				}
			}
		}
		/* Main interface scrolling */
		if (Client.openInterfaceID != -1) {
			RSInterface[] components = RSInterface.interfaceCache[Client.openInterfaceID];
			offsetX = Client.frameMode == ScreenMode.FIXED ? 4 : (Client.frameWidth / 2) - 356;
			offsetY = Client.frameMode == ScreenMode.FIXED ? 4 : (Client.frameHeight / 2) - 230;
			for (int childID = 0; childID < components.length; childID++) {
				RSInterface rsInterface = components[childID];
				if (rsInterface == null)
					continue;
				if (rsInterface.type != 0)
					continue;
				if (rsInterface.scrollMax <= 0)
					continue;
				int childX = rsInterface.x + offsetX;
				int childY = rsInterface.y + offsetY;
				if (mouseX > childX && mouseY > childY && mouseX < childX + rsInterface.width
						&& mouseY < childY + rsInterface.height) {
					rsInterface.scrollPosition += rotation * 30;
					return true;
				}
			}
		}
		return false;
	}

	public int clickType;
	public final int LEFT = 0;
	public final int RIGHT = 1;
	public final int DRAG = 2;
	public final int RELEASED = 3;
	public final int MOVE = 4;
	public final int CAMERADRAG = 5;
	public int releasedX;
	public int releasedY;
	public int dragX;
	public int dragY;
	public boolean altKeyDown = false;

	public final void mousePressed(MouseEvent e) {
		int type = e.getButton();
		int x = e.getX();
		int y = e.getY();
		if (rsFrame != null) {
			Insets insets = rsFrame.getInsets();
			x -= insets.left;// 4
			y -= insets.top;// 22
		}
		idleTime = 0;
		clickX = x;
		clickY = y;
		clickTime = System.currentTimeMillis();
		if (type == 2 || altKeyDown && (type == MouseEvent.BUTTON1 || type == MouseEvent.BUTTON3)) {
			clickType = CAMERADRAG;
			dragX = x;
			dragY = y;
			return;
		}
		if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
			clickType = RIGHT;
			clickMode1 = 2;
			clickMode2 = 2;
		} else {
			clickType = LEFT;
			clickMode1 = 1;
			clickMode2 = 1;
		}
	}

	public final void mouseReleased(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (rsFrame != null) {
			Insets insets = rsFrame.getInsets();
			x -= insets.left;// 4
			y -= insets.top;// 22
		}
		releasedX = x;
		releasedY = y;
		idleTime = 0;
		clickMode2 = 0;
		clickType = RELEASED;
	}

	public final void mouseClicked(MouseEvent mouseevent) {
	}

	public final void mouseEntered(MouseEvent mouseevent) {
	}

	public final void mouseExited(MouseEvent mouseevent) {
		idleTime = 0;
		mouseX = -1;
		mouseY = -1;
	}

	public final void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (Client.pixelScaling > 1 && Client.frameMode == ScreenMode.FIXED) { // TODO Odel scaling
			x /= Client.pixelScaling;
			y /= Client.pixelScaling;
		}
		if (rsFrame != null) {
			Insets insets = rsFrame.getInsets();
			x -= insets.left;// 4
			y -= insets.top;// 22
		}
		if (clickType == CAMERADRAG) {
			int tarX = dragX - e.getX();
			int tarY = dragY - e.getY();
			if (Client.getClient() != null) {
				Client.getClient().mouseCameraDrag(tarX, -tarY);
			}
			dragX = e.getX();
			dragY = e.getY();
			return;
		}
		/*
		 * if (client.isApplet) { x -= (client.appletWidth / 2) - (client.clientWidth /
		 * 2); }
		 */
		idleTime = 0;
		mouseX = x;
		mouseY = y;
		clickType = DRAG;
	}

	public final void mouseMoved(MouseEvent mouseevent) {
		int x = mouseevent.getX();
		int y = mouseevent.getY();
		if (Client.pixelScaling > 1 && Client.frameMode == ScreenMode.FIXED) { // TODO Odel scaling
			x /= Client.pixelScaling;
			y /= Client.pixelScaling;
		}
		if (rsFrame != null) {
			Insets insets = rsFrame.getInsets();
			x -= insets.left;// 4
			if (Client.pixelScaling > 1 && Client.frameMode == ScreenMode.FIXED) {
				y -= insets.top + (((Client.pixelScaling - 1) * -4) - 9);
			} else {
				y -= insets.top; // 22
			}
		}
		idleTime = 0;
		mouseX = x;
		mouseY = y;
		clickType = MOVE;
	}

	public final void keyPressed(KeyEvent keyevent) {
		idleTime = 0;
		int i = keyevent.getKeyCode();
		int j = keyevent.getKeyChar();
		/*
		 * if(j == 96) { Client.consoleOpen = !Client.consoleOpen; } if(i ==
		 * KeyEvent.VK_ESCAPE){ Client.getClient().toggleSize(0); }
		 */
		int dialogueID = Client.getClient().backDialogID;
		if (dialogueID > 0) {
			if (i == 32) {
				Client.getClient().stream.createFrame(40);
				Client.getClient().stream.writeShort(10682372);
				Client.getClient().continueDialogue = true;
			}
		}
		if (j < 30)
			j = 0;
		if (i == 37)
			j = 1;
		if (i == 39)
			j = 2;
		if (i == 38)
			j = 3;
		if (i == 40)
			j = 4;
		if (i == 17)
			j = 5;
		if (i == 8)
			j = 8;
		if (i == 127)
			j = 8;
		if (i == 9)
			j = 9;
		if (i == 10)
			j = 10;
		if (i >= 112 && i <= 123)
			j = (1008 + i) - 112;
		if (i == 36)
			j = 1000;
		if (i == 35)
			j = 1001;
		if (i == 33)
			j = 1002;
		if (i == 34)
			j = 1003;
		if (i == KeyEvent.VK_F1) {
			Client.setTab(SettingsManager.hotkeyButtons[0]);
		} else if (i == KeyEvent.VK_F2) {
			Client.setTab(SettingsManager.hotkeyButtons[1]);
		} else if (i == KeyEvent.VK_F3) {
			Client.setTab(SettingsManager.hotkeyButtons[2]);
		} else if (i == KeyEvent.VK_F4) {
			Client.setTab(SettingsManager.hotkeyButtons[3]);
		} else if (i == KeyEvent.VK_F5) {
			Client.setTab(SettingsManager.hotkeyButtons[4]);
		} else if (i == KeyEvent.VK_F6) {
			Client.setTab(SettingsManager.hotkeyButtons[5]);
		} else if (i == KeyEvent.VK_F7) {
			Client.setTab(SettingsManager.hotkeyButtons[6]);
		} else if (i == KeyEvent.VK_F8) {
			Client.setTab(SettingsManager.hotkeyButtons[7]);
		} else if (i == KeyEvent.VK_F9) {
			Client.setTab(SettingsManager.hotkeyButtons[8]);
		} else if (i == KeyEvent.VK_F10) {
			Client.setTab(SettingsManager.hotkeyButtons[9]);
		} else if (i == KeyEvent.VK_F11) {
			Client.setTab(SettingsManager.hotkeyButtons[10]);
		} else if (i == KeyEvent.VK_F12) {
			Client.setTab(SettingsManager.hotkeyButtons[11]);
		} else if (i == KeyEvent.VK_ALT) {
			altKeyDown = true;
		} else if (i == KeyEvent.VK_SHIFT)
		{
			Client.getClient().shiftDown = true;
		}
		if (j > 0 && j < 128)
			keyArray[j] = 1;
		if (j > 4) {
			charQueue[writeIndex] = j;
			writeIndex = writeIndex + 1 & 0x7f;
		}
	}

	public final void keyReleased(KeyEvent keyevent) {
		idleTime = 0;
		int i = keyevent.getKeyCode();
		char c = keyevent.getKeyChar();
		if (i == 17) {
		}
		if (c < '\036') {
			c = '\0';
		}
		if (i == 37) {
			c = '\001';
		}
		if (i == 39) {
			c = '\002';
		}
		if (i == 38) {
			c = '\003';
		}
		if (i == 40) {
			c = '\004';
		}
		if (i == 17) {
			c = '\005';
		}
		if (i == 8) {
			c = '\b';
		}
		if (i == 127) {
			c = '\b';
		}
		if (i == 9) {
			c = '\t';
		}
		if (i == 10) {
			c = '\n';
		}
		if (i == KeyEvent.VK_ALT) {
			altKeyDown = false;
		}
		if(i == KeyEvent.VK_SHIFT) {
			Client.getClient().shiftDown = false;
		}
		if (c > 0 && c < '\200') {
			keyArray[c] = 0;
		}
	}

	public final void keyTyped(KeyEvent keyevent) {
	}

	public final int readChar(int i) {
		while (i >= 0) {
			int j = 1;
			while (j > 0) {
				j++;
			}
		}
		int k = -1;
		if (writeIndex != readIndex) {
			k = charQueue[readIndex];
			readIndex = readIndex + 1 & 0x7f;
		}
		return k;
	}

	@Override
	public final void focusGained(FocusEvent focusevent) {
		awtFocus = true;
		shouldClearScreen = true;
		redraw();
	}

	@Override
	public final void focusLost(FocusEvent focusevent) {
		awtFocus = false;
		for (int i = 0; i < 128; i++) {
			keyArray[i] = 0;
		}
		altKeyDown = false;
	}

	public final void windowActivated(WindowEvent windowevent) {
	}

	public final void windowClosed(WindowEvent windowevent) {
	}

	public final void windowClosing(WindowEvent windowevent) {
		destroy();

	}

	public final void windowDeactivated(WindowEvent windowevent) {
	}

	public final void windowDeiconified(WindowEvent windowevent) {
	}

	public final void windowIconified(WindowEvent windowevent) {
	}

	public final void windowOpened(WindowEvent windowevent) {
	}

	public void startup() {
	}

	public void shutdown() {
	}

	public void doLogic() {
	}

	public void repaintGame() {
	}

	public void redraw() {
	}

	Component getGameComponent() {
		if (rsFrame != null && !isApplet) {
			return rsFrame;
		} else {
			return this;
		}
	}

	public void startRunnable(Runnable runnable, int i) {
		Thread thread = new Thread(runnable);
		thread.start();
		thread.setPriority(i);
	}

	void drawLoadingText(int percentage, String loadingText) {
		while (graphics == null) {
			graphics = (isApplet ? this : rsFrame).getGraphics();
			try {
				getGameComponent().repaint();
			} catch (Exception _ex) {
			}
			try {
				Thread.sleep(1000L);
			} catch (Exception _ex) {
			}
		}
		Font font = new Font("Helvetica", 1, 13);
		FontMetrics fontmetrics = getGameComponent().getFontMetrics(font);
		Font font1 = new Font("Helvetica", 0, 13);
		FontMetrics fontmetrics1 = getGameComponent().getFontMetrics(font1);
		if (shouldClearScreen) {
			graphics.setColor(Color.black);
			graphics.fillRect(0, 0, Client.frameWidth, Client.frameHeight);
			shouldClearScreen = false;
		}
		Color color = new Color(140, 17, 17);
		int y = (Client.frameHeight * Client.pixelScaling) / 2 - 18;
		graphics.setColor(color);
		graphics.drawRect((Client.frameWidth * Client.pixelScaling) / 2 - 152, y, 304, 34);
		graphics.fillRect((Client.frameWidth * Client.pixelScaling) / 2 - 150, y + 2, percentage * 3, 30);
		graphics.setColor(Color.black);
		graphics.fillRect(((Client.frameWidth * Client.pixelScaling) / 2 - 150) + percentage * 3, y + 2,
				300 - percentage * 3, 30);
		graphics.setFont(font);
		graphics.setColor(Color.white);
		graphics.drawString(loadingText,
				((Client.frameWidth * Client.pixelScaling) - fontmetrics.stringWidth(loadingText)) / 2, y + 22);
		graphics.drawString("", ((Client.frameWidth * Client.pixelScaling) - fontmetrics1.stringWidth("")) / 2, y - 8);
	}
	/*
	 * void drawLoadingText(int percentage, String loadingText) {
	 * Client.getClient().checkSize(); while(graphics == null) { graphics =
	 * (isApplet ? this : rsFrame).getGraphics(); try {
	 * getGameComponent().repaint(); } catch(Exception _ex) { } try {
	 * Thread.sleep(1000L); } catch(Exception _ex) { } } Font font = new
	 * Font("Helvetica", 1, 13); FontMetrics fontmetrics =
	 * getGameComponent().getFontMetrics(font); Font font1 = new Font("Helvetica",
	 * 0, 13); FontMetrics fontmetrics1 = getGameComponent().getFontMetrics(font1);
	 * if(shouldClearScreen) { graphics.setColor(Color.black); graphics.fillRect(0,
	 * 0, Client.clientWidth, Client.clientHeight); shouldClearScreen = false; }
	 * Color color = new Color(140, 17, 17); int y = Client.clientHeight / 2 - 18;
	 * graphics.setColor(color); graphics.drawRect(Client.clientWidth / 2 - 152, y,
	 * 304, 34); graphics.fillRect(Client.clientWidth / 2 - 150, y + 2, percentage *
	 * 3, 30); graphics.setColor(Color.black); graphics.fillRect((Client.clientWidth
	 * / 2 - 150) + percentage * 3, y + 2, 300 - percentage * 3, 30);
	 * graphics.setFont(font); graphics.setColor(Color.white);
	 * graphics.drawString(loadingText,(Client.clientWidth -
	 * fontmetrics.stringWidth(loadingText)) / 2, y + 22); graphics.drawString("",
	 * (Client.clientWidth - fontmetrics1.stringWidth("")) / 2, y - 8); }
	 */

	RSApplet() {
		delayTime = 20;
		minDelay = 1;
		shouldDebug = false;
		shouldClearScreen = true;
		awtFocus = true;
	}

}
