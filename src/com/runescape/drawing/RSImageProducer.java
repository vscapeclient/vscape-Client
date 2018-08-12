package com.runescape.drawing;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import com.runescape.Client;
import com.runescape.Client.ScreenMode;

import vscape.ClientSettings;

final public class RSImageProducer //implements ImageProducer, ImageObserver
{
	public final int[] canvasRaster;
	public final int canvasWidth;
	public final int canvasHeight;
	private final BufferedImage bufferedImage;;

	public RSImageProducer(int canvasWidth, int canvasHeight) {
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		bufferedImage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
		canvasRaster = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
		initDrawingArea();
	}

	public void drawGraphics(int y, Graphics graphics, int x) {
		if(Client.pixelScaling > 1 && Client.getFrameMode() == ScreenMode.FIXED) {
			graphics.drawImage(bufferedImage, x * Client.pixelScaling, y * Client.pixelScaling, canvasWidth * Client.pixelScaling, canvasHeight * Client.pixelScaling, null);
		} else {
			graphics.drawImage(bufferedImage, x, y, null);
		}
	}

	public void initDrawingArea() {
		DrawingArea.initDrawingArea(canvasHeight, canvasWidth, canvasRaster);
	}
}
