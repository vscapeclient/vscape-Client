package com.runescape.cache.anim;

import com.runescape.io.Stream;

public final class FrameBase {

	public FrameBase(Stream stream) {
		int count = stream.readUnsignedByte();
		transformationType = new int[count];
		labels = new int[count][];
		for (int j = 0; j < count; j++)
			transformationType[j] = stream.readUnsignedByte();

		for (int j = 0; j < count; j++)
			labels[j] = new int[stream.readUnsignedByte()];

		for (int j = 0; j < count; j++)
			for (int l = 0; l < labels[j].length; l++)
				labels[j][l] = stream.readUnsignedByte();

	}

	public final int[] transformationType;
	public final int[][] labels;
}
