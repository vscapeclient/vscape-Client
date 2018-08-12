package com.runescape.map;

final public class TileUtils {

	public static int getRotatedXCoord(int rotation, int x, int y) {
		rotation &= 3;
		if (rotation == 0)
			return x;
		if (rotation == 1)
			return y;
		if (rotation == 2)
			return 7 - x;
		else
			return 7 - y;
	}

	public static int getRotatedYCoord(int rotation, int x, int y) {
		rotation &= 3;
		if (rotation == 0)
			return y;
		if (rotation == 1)
			return 7 - x;
		if (rotation == 2)
			return 7 - y;
		else
			return x;
	}

	public static int getNewXCoord(int rotation, int length, int x, int y, int width) {
		rotation &= 3;
		if (rotation == 0)
			return x;
		else if (rotation == 1)
			return y;
		else if (rotation == 2)
			return 7 - x - (width - 1);
		else
			return 7 - y - (length - 1);
	}

	public static int getNewYCoord(int y, int length, int rotation, int width, int x) {
		rotation &= 3;
		if (rotation == 0)
			return y;
		else if (rotation == 1)
			return 7 - x - (width - 1);
		else if (rotation == 2)
			return 7 - y - (length - 1);
		else
			return x;
	}

}
