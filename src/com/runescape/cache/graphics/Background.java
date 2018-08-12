package com.runescape.cache.graphics;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import com.runescape.cache.Archive;
import com.runescape.drawing.DrawingArea;
import com.runescape.io.Stream;

public final class Background extends DrawingArea {

	public Background(Archive streamLoader, String s, int i)
	{
		Stream stream = new Stream(streamLoader.getFile(s + ".dat"));
		Stream stream_1 = new Stream(streamLoader.getFile("index.dat"));
		stream_1.currentOffset = stream.readUnsignedShort();
		anInt1456 = stream_1.readUnsignedShort();
		anInt1457 = stream_1.readUnsignedShort();
		int j = stream_1.readUnsignedByte();
		anIntArray1451 = new int[j];
		for(int k = 0; k < j - 1; k++)
			anIntArray1451[k + 1] = stream_1.read3Bytes();

		for(int l = 0; l < i; l++)
		{
			stream_1.currentOffset += 2;
			stream.currentOffset += stream_1.readUnsignedShort() * stream_1.readUnsignedShort();
			stream_1.currentOffset++;
		}

		drawOffsetX = stream_1.readUnsignedByte();
		drawOffsetY = stream_1.readUnsignedByte();
		myWidth = stream_1.readUnsignedShort();
		myHeight = stream_1.readUnsignedShort();
		int i1 = stream_1.readUnsignedByte();
		int j1 = myWidth * myHeight;
		myPixels = new byte[j1];
		if(i1 == 0)
		{
			for(int k1 = 0; k1 < j1; k1++)
				myPixels[k1] = stream.readSignedByte();

			return;
		}
		if(i1 == 1)
		{
			for(int l1 = 0; l1 < myWidth; l1++)
			{
				for(int i2 = 0; i2 < myHeight; i2++)
					myPixels[l1 + i2 * myWidth] = stream.readSignedByte();

			}

		}
	}

	public void method356()
	{
		anInt1456 /= 2;
		anInt1457 /= 2;
		byte abyte0[] = new byte[anInt1456 * anInt1457];
		int i = 0;
		for(int j = 0; j < myHeight; j++)
		{
			for(int k = 0; k < myWidth; k++)
				abyte0[(k + drawOffsetX >> 1) + (j + drawOffsetY >> 1) * anInt1456] = myPixels[i++];

		}

		myPixels = abyte0;
		myWidth = anInt1456;
		myHeight = anInt1457;
		drawOffsetX = 0;
			drawOffsetY = 0;
	}

	public void method357()
	{
		if(myWidth == anInt1456 && myHeight == anInt1457)
			return;
		byte abyte0[] = new byte[anInt1456 * anInt1457];
		int i = 0;
		for(int j = 0; j < myHeight; j++)
		{
			for(int k = 0; k < myWidth; k++)
				abyte0[k + drawOffsetX + (j + drawOffsetY) * anInt1456] = myPixels[i++];

		}

		myPixels = abyte0;
		myWidth = anInt1456;
		myHeight = anInt1457;
		drawOffsetX = 0;
		drawOffsetY = 0;
	}

	public void flipHorizontal() //method358
	{
		byte abyte0[] = new byte[myWidth * myHeight];
		int j = 0;
		for(int k = 0; k < myHeight; k++)
		{
			for(int l = myWidth - 1; l >= 0; l--)
				abyte0[j++] = myPixels[l + k * myWidth];

		}

		myPixels = abyte0;
		drawOffsetX = anInt1456 - myWidth - drawOffsetX;
	}

	public void flipVertical() //method359
	{
		byte abyte0[] = new byte[myWidth * myHeight];
		int i = 0;
		for(int j = myHeight - 1; j >= 0; j--)
		{
			for(int k = 0; k < myWidth; k++)
				abyte0[i++] = myPixels[k + j * myWidth];

		}

		myPixels = abyte0;
		drawOffsetY = anInt1457 - myHeight - drawOffsetY;
	}

	public void method360(int i, int j, int k)
	{
		for(int i1 = 0; i1 < anIntArray1451.length; i1++)
		{
			int j1 = anIntArray1451[i1] >> 16 & 0xff;
			j1 += i;
			if(j1 < 0)
				j1 = 0;
			else
			if(j1 > 255)
				j1 = 255;
			int k1 = anIntArray1451[i1] >> 8 & 0xff;
			k1 += j;
			if(k1 < 0)
				k1 = 0;
			else
			if(k1 > 255)
				k1 = 255;
			int l1 = anIntArray1451[i1] & 0xff;
			l1 += k;
			if(l1 < 0)
				l1 = 0;
			else
			if(l1 > 255)
				l1 = 255;
			anIntArray1451[i1] = (j1 << 16) + (k1 << 8) + l1;
		}
	}

	public void drawBackground(int i, int k)
	{
		i += drawOffsetX;
		k += drawOffsetY;
		int l = i + k * DrawingArea.width;
		int i1 = 0;
		int j1 = myHeight;
		int k1 = myWidth;
		int l1 = DrawingArea.width - k1;
		int i2 = 0;
		if(k < DrawingArea.topY)
		{
			int j2 = DrawingArea.topY - k;
			j1 -= j2;
			k = DrawingArea.topY;
			i1 += j2 * k1;
			l += j2 * DrawingArea.width;
		}
		if(k + j1 > DrawingArea.bottomY)
			j1 -= (k + j1) - DrawingArea.bottomY;
		if(i < DrawingArea.topX)
		{
			int k2 = DrawingArea.topX - i;
			k1 -= k2;
			i = DrawingArea.topX;
			i1 += k2;
			l += k2;
			i2 += k2;
			l1 += k2;
		}
		if(i + k1 > DrawingArea.bottomX)
		{
			int l2 = (i + k1) - DrawingArea.bottomX;
			k1 -= l2;
			i2 += l2;
			l1 += l2;
		}
		if(!(k1 <= 0 || j1 <= 0))
		{
			method362(j1, DrawingArea.pixels, myPixels, l1, l, k1, i1, anIntArray1451, i2);
		}
	}

	private void method362(int i, int ai[], byte abyte0[], int j, int k, int l,
						   int i1, int ai1[], int j1)
	{
		int k1 = -(l >> 2);
		l = -(l & 3);
		for(int l1 = -i; l1 < 0; l1++)
		{
			for(int i2 = k1; i2 < 0; i2++)
			{
				byte byte1 = abyte0[i1++];
				if(byte1 != 0)
					ai[k++] = ai1[byte1 & 0xff];
				else
					k++;
				byte1 = abyte0[i1++];
				if(byte1 != 0)
					ai[k++] = ai1[byte1 & 0xff];
				else
					k++;
				byte1 = abyte0[i1++];
				if(byte1 != 0)
					ai[k++] = ai1[byte1 & 0xff];
				else
					k++;
				byte1 = abyte0[i1++];
				if(byte1 != 0)
					ai[k++] = ai1[byte1 & 0xff];
				else
					k++;
			}

			for(int j2 = l; j2 < 0; j2++)
			{
				byte byte2 = abyte0[i1++];
				if(byte2 != 0)
					ai[k++] = ai1[byte2 & 0xff];
				else
					k++;
			}

			k += j;
			i1 += j1;
		}

	}

	public byte myPixels[];
	public final int[] anIntArray1451;
	public int myWidth;
	public int myHeight;
	public int drawOffsetX;
	public int drawOffsetY;
	public int anInt1456;
	private int anInt1457;
}
