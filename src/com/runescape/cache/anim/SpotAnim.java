package com.runescape.cache.anim;

import com.runescape.cache.Archive;
import com.runescape.entity.model.Model;
import com.runescape.io.Stream;
import com.runescape.link.MRUNodes;

public final class SpotAnim {

	public static void unpackConfig(Archive streamLoader) {
		Stream stream = new Stream(streamLoader.getFile("spotanim.dat"));
		int length = stream.readUnsignedShort();
		if (cache == null)
			cache = new SpotAnim[length];
		for (int j = 0; j < length; j++) {
			if (cache[j] == null)
				cache[j] = new SpotAnim();
			cache[j].anInt404 = j;
			cache[j].readValues(stream);
		}

	}

	public void readValues(Stream stream) {
		do {
			int i = stream.readUnsignedByte();
			if (i == 0)
				return;
			if (i == 1)
				modelId = stream.readUnsignedShort();
			else if (i == 2) {
				animationId = stream.readUnsignedShort();
				if (Animation.anims != null)
					animationSequence = Animation.anims[animationId];
			} else if (i == 4)
				resizeXY = stream.readUnsignedShort();
			else if (i == 5)
				resizeZ = stream.readUnsignedShort();
			else if (i == 6)
				rotation = stream.readUnsignedShort();
			else if (i == 7)
				modelBrightness = stream.readUnsignedByte();
			else if (i == 8)
				modelShadow = stream.readUnsignedByte();
			else if (i == 40) {
				int j = stream.readUnsignedByte();
				for (int k = 0; k < j; k++) {
					originalModelColours[k] = stream.readUnsignedShort();
					modifiedModelColours[k] = stream.readUnsignedShort();
				}
			} else
				System.out.println("Error unrecognised spotanim config code: "
						+ i);
		} while (true);
	}

	public Model getModel() {
		Model model = (Model) aMRUNodes_415.insertFromCache(anInt404);
		if (model != null)
			return model;
		model = Model.getModel(modelId);
		if (model == null)
			return null;
		for (int i = 0; i < 6; i++)
			if (originalModelColours[0] != 0)
				model.method476(originalModelColours[i], modifiedModelColours[i]);

		aMRUNodes_415.removeFromCache(model, anInt404);
		return model;
	}

	private SpotAnim() {
		animationId = -1;
		originalModelColours = new int[6];
		modifiedModelColours = new int[6];
		resizeXY = 128;
		resizeZ = 128;
	}

	public static SpotAnim cache[];
	private int anInt404;
	private int modelId;
	private int animationId;
	public Animation animationSequence;
	private final int[] originalModelColours;
	private final int[] modifiedModelColours;
	public int resizeXY;
	public int resizeZ;
	public int rotation;
	public int modelBrightness;
	public int modelShadow;
	public static MRUNodes aMRUNodes_415 = new MRUNodes(30);

}
