package com.runescape.cache.definitions;

import com.runescape.Client;
import com.runescape.cache.Archive;
import com.runescape.cache.anim.Frame;
import com.runescape.entity.model.Model;
import com.runescape.io.Stream;
import com.runescape.link.MRUNodes;
import com.runescape.setting.VarBit;

public final class EntityDef {

	public static EntityDef forID(int i) {
		for (int j = 0; j < 20; j++)
			if (cache[j].interfaceType == (long) i)
				return cache[j];

		anInt56 = (anInt56 + 1) % 20;
		EntityDef entityDef = cache[anInt56] = new EntityDef();
		stream.currentOffset = streamIndices[i];
		entityDef.npcID = i;
		entityDef.interfaceType = i;
		entityDef.readValues(stream);
		return entityDef;
	}

	public Model method160() {
		if (childrenIDs != null) {
			EntityDef entityDef = method161();
			if (entityDef == null)
				return null;
			else
				return entityDef.method160();
		}
		if (additionalModels == null)
			return null;
		boolean flag1 = false;
		for (int i = 0; i < additionalModels.length; i++)
			if (!Model.isCached(additionalModels[i]))
				flag1 = true;

		if (flag1)
			return null;
		Model aclass30_sub2_sub4_sub6s[] = new Model[additionalModels.length];
		for (int j = 0; j < additionalModels.length; j++)
			aclass30_sub2_sub4_sub6s[j] = Model.getModel(additionalModels[j]);

		Model model;
		if (aclass30_sub2_sub4_sub6s.length == 1)
			model = aclass30_sub2_sub4_sub6s[0];
		else
			model = new Model(aclass30_sub2_sub4_sub6s.length,
					aclass30_sub2_sub4_sub6s);
		if (originalModelColors != null) {
			for (int k = 0; k < originalModelColors.length; k++)
				model.method476(originalModelColors[k], modifiedModelColors[k]);

		}
		return model;
	}

	public EntityDef method161() {
		int j = -1;
		if (varBitID != -1) {
			VarBit varBit = VarBit.cache[varBitID];
			int k = varBit.configId;
			int l = varBit.leastSignificantBit;
			int i1 = varBit.mostSignificantBit;
			int j1 = Client.BIT_MASKS[i1 - l];
			j = clientInstance.variousSettings[k] >> l & j1;
		} else if (settingId != -1)
			j = clientInstance.variousSettings[settingId];
		if (j < 0 || j >= childrenIDs.length || childrenIDs[j] == -1)
			return null;
		else
			return forID(childrenIDs[j]);
	}

	public static int totalNPCs;
	public static void unpackConfig(Archive streamLoader) {
		stream = new Stream(streamLoader.getFile("npc.dat"));
		Stream stream2 = new Stream(streamLoader.getFile("npc.idx"));
		totalNPCs = stream2.readUnsignedShort();
		streamIndices = new int[totalNPCs];
		int i = 2;
		for (int j = 0; j < totalNPCs; j++) {
			streamIndices[j] = i;
			i += stream2.readUnsignedShort();
		}

		cache = new EntityDef[20];
		for (int k = 0; k < 20; k++)
			cache[k] = new EntityDef();
		for (int index = 0; index < totalNPCs; index++) {
			EntityDef ed = forID(index);
			if (ed == null)
				continue;
			if (ed.name == null)
				continue;
		}
		
		//ConfigWriter.writeNpcConfig();
	}

	public static void nullLoader() {
		mruNodes = null;
		streamIndices = null;
		cache = null;
		stream = null;
	}

	public Model method164(int j, int k, int ai[]) {
		if (childrenIDs != null) {
			EntityDef entityDef = method161();
			if (entityDef == null)
				return null;
			else
				return entityDef.method164(j, k, ai);
		}
		Model model = (Model) mruNodes.insertFromCache(interfaceType);
		if (model == null) {
			boolean flag = false;
			for (int i1 = 0; i1 < modelID.length; i1++)
				if (!Model.isCached(modelID[i1]))
					flag = true;

			if (flag)
				return null;
			Model aclass30_sub2_sub4_sub6s[] = new Model[modelID.length];
			for (int j1 = 0; j1 < modelID.length; j1++)
				aclass30_sub2_sub4_sub6s[j1] = Model
				.getModel(modelID[j1]);

			if (aclass30_sub2_sub4_sub6s.length == 1)
				model = aclass30_sub2_sub4_sub6s[0];
			else
				model = new Model(aclass30_sub2_sub4_sub6s.length,
						aclass30_sub2_sub4_sub6s);
			if (originalModelColors != null) {
				for (int k1 = 0; k1 < originalModelColors.length; k1++)
					model.method476(originalModelColors[k1], modifiedModelColors[k1]);

			}
			model.method469();
			model.method479(64 + lightModifier, 850 + shadowModifier, -30, -50, -30, true);
			mruNodes.removeFromCache(model, interfaceType);
		}
		Model model_1 = Model.aModel_1621;
		model_1.method464(model, Frame.method532(k) & Frame.method532(j));
		if (k != -1 && j != -1)
			model_1.method471(ai, j, k);
		else if (k != -1)
			model_1.method470(k);
		if (scaleXZ != 128 || scaleY != 128)
			model_1.method478(scaleXZ, scaleXZ, scaleY);
		model_1.method466();
		model_1.anIntArrayArray1658 = null;
		model_1.anIntArrayArray1657 = null;
		if (size == 1)
			model_1.aBoolean1659 = true;
		return model_1;
	}

	public void readValues(Stream stream) {
		do {
			try {
			int i = stream.readUnsignedByte();
			if (i == 0)
				return;
			if (i == 1) {
				int j = stream.readUnsignedByte();
				modelID = new int[j];
				for (int j1 = 0; j1 < j; j1++) {
					modelID[j1] = stream.readUnsignedShort();
				}
			} else if (i == 2) {
				name = stream.readNewString();
			} else if (i == 3) {
				description = stream.readBytes();
			} else if (i == 12) {
				size = stream.readSignedByte();
			} else if (i == 13) {
				standAnim = stream.readUnsignedShort();
			} else if (i == 14) {
				walkAnim = stream.readUnsignedShort();
			} else if (i == 17) {
				walkAnim = stream.readUnsignedShort();
				turn180Anim = stream.readUnsignedShort();
				turn90CWAnim = stream.readUnsignedShort();
				turn90CCWAnim = stream.readUnsignedShort();
			} else if (i >= 30 && i < 40) {
				if (actions == null)
					actions = new String[5];
				actions[i - 30] = stream.readNewString();
				if (actions[i - 30].equalsIgnoreCase("hidden"))
					actions[i - 30] = null;
			} else if (i == 40) {
				int k = stream.readUnsignedByte();
				originalModelColors = new int[k];
				modifiedModelColors = new int[k];
				for (int k1 = 0; k1 < k; k1++) {
					originalModelColors[k1] = stream.readUnsignedShort();
					modifiedModelColors[k1] = stream.readUnsignedShort();
				}

			} else if (i == 60) {
				int l = stream.readUnsignedByte();
				additionalModels = new int[l];
				for (int l1 = 0; l1 < l; l1++) {
					additionalModels[l1] = stream.readUnsignedShort();
				}
			} else if (i == 90) {
				stream.readUnsignedShort();
			} else if (i == 91) {
				stream.readUnsignedShort();
			} else if (i == 92) {
				stream.readUnsignedShort();
			} else if (i == 93) {
				drawMapDot = false;
			} else if (i == 95) {
				combatLevel = stream.readUnsignedShort();
			} else if (i == 97) {
				scaleXZ = stream.readUnsignedShort();
			} else if (i == 98) {
				scaleY = stream.readUnsignedShort();
			} else if (i == 99) {
				priorityRender = true;
			} else if (i == 100) {
				lightModifier = stream.readSignedByte();
			} else if (i == 101) {
				shadowModifier = stream.readSignedByte() * 5;
			} else if (i == 102) {
				headIcon = stream.readUnsignedShort();
			} else if (i == 103) {
				degreesToTurn = stream.readUnsignedShort();
			} else if (i == 106) {
				varBitID = stream.readUnsignedShort();
				if (varBitID == 65535)
					varBitID = -1;
				settingId = stream.readUnsignedShort();
				if (settingId == 65535)
					settingId = -1;
				int i1 = stream.readUnsignedByte();
				childrenIDs = new int[i1 + 1];
				for (int i2 = 0; i2 <= i1; i2++) {
					childrenIDs[i2] = stream.readUnsignedShort();
					if (childrenIDs[i2] == 65535)
						childrenIDs[i2] = -1;
				}

			} else if (i == 107) {
				hasActions = false;
			}
			} catch (Exception e) {
				System.out.println("Error reading values for npc#" + this.npcID);
				e.printStackTrace();
			}
		} while (true);
	}

	public EntityDef() {
		turn90CCWAnim = -1;
		varBitID = -1;
		turn180Anim = -1;
		settingId = -1;
		combatLevel = -1;
		anInt64 = 1834;
		walkAnim = -1;
		size = 1;
		headIcon = -1;
		standAnim = -1;
		interfaceType = -1L;
		degreesToTurn = 32;
		turn90CWAnim = -1;
		hasActions = true;
		scaleY = 128;
		drawMapDot = true;
		scaleXZ = 128;
		priorityRender = false;
	}

	public int npcID;
	public int turn90CCWAnim;
	public static int anInt56;
	public int varBitID;
	public int turn180Anim;
	public int settingId;
	public static Stream stream;
	public int combatLevel;
	public final int anInt64;
	public String name;
	public String actions[];
	public int walkAnim;
	public byte size;
	public int[] modifiedModelColors;
	public static int[] streamIndices;
	public int[] additionalModels;
	public int headIcon;
	public int[] originalModelColors;
	public int standAnim;
	public long interfaceType;
	public int degreesToTurn;
	public static EntityDef[] cache;
	public static Client clientInstance;
	public int turn90CWAnim;
	public boolean hasActions;
	public int lightModifier;
	public int scaleY;
	public boolean drawMapDot;
	public int childrenIDs[];
	public byte description[];
	public int scaleXZ;
	public int shadowModifier;
	public boolean priorityRender;
	public int[] modelID;
	public static MRUNodes mruNodes = new MRUNodes(30);

}
