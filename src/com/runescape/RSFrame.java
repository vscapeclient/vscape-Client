package com.runescape;

import vscape.ClientSettings;

import javax.swing.*;
import java.awt.*;

final class RSFrame extends Frame {
	private final RSApplet applet;
	public Toolkit toolkit = Toolkit.getDefaultToolkit();
	public Dimension screenSize = toolkit.getScreenSize();
	public int screenWidth = (int) screenSize.getWidth();
	public int screenHeight = (int) screenSize.getHeight();
	protected Insets insets = getInsets();
	private static final long serialVersionUID = 1L;

	public RSFrame(RSApplet applet, int width, int height, boolean resizable, boolean fullscreen) {
		this.applet = applet;
		screenSize = this.toolkit.getScreenSize();
		screenWidth = (int) screenSize.getWidth();
		screenHeight = (int) screenSize.getHeight();
		ImageIcon icon = new ImageIcon(RSFrame.class.getResource("icon.png"));
		setIconImage(icon.getImage());
		setTitle("/v/scape " + ClientSettings.CLIENT_VERSION+(ClientSettings.DevMode?" [ Dev Client ]":""));
		setResizable(resizable);
		setUndecorated(fullscreen);
		setBackground(Color.BLACK);
		setLocationByPlatform(true);
		setVisible(true);
		insets = getInsets();
		if (resizable) {
			setMinimumSize(new Dimension(766 + insets.left + insets.right, 536 + insets.top + insets.bottom));
		}
		String OS = System.getProperty("os.name").toLowerCase();
		if(resizable && (OS.indexOf("mac") >= 0)){
			setSize(screenWidth, screenHeight);
		}
		else
		{
			setSize(width + insets.left + insets.right, height + insets.top + insets.bottom);
		}
		setVisible(true);
		setFocusTraversalKeysEnabled(false);
		requestFocus();
		toFront();
	}

	public Graphics getGraphics() {
		final Graphics graphics = super.getGraphics();
		Insets insets = this.getInsets();
		graphics.fillRect(0, 0, getWidth(), getHeight());
		graphics.translate(insets != null ? insets.left : 0, insets != null ? insets.top : 0);
		return graphics;
	}

	public int getFrameWidth() {
		Insets insets = this.getInsets();
		return getWidth() - (insets.left + insets.right);
	}

	public int getFrameHeight() {
		Insets insets = this.getInsets();
		return getHeight() - (insets.top + insets.bottom);
	}

	public void update(Graphics graphics) {
		applet.update(graphics);
	}

	public void paint(Graphics graphics) {
		applet.paint(graphics);
	}
}
