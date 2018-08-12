package com.runescape.cache.definitions;

import com.runescape.Client;
import com.runescape.cache.Archive;
import com.runescape.cache.anim.Frame;
import com.runescape.entity.model.Model;
import com.runescape.io.Stream;
import com.runescape.link.MRUNodes;
import com.runescape.net.OnDemandFetcher;
import com.runescape.setting.VarBit;
import vscape.ClientSettings;

public final class ObjectDef
{
	public static ObjectDef forID(int i)
	{
		if (i > streamIndices.length) {
			//i = streamIndices.length - 1;
			return null;
		}
		
		for(int j = 0; j < 20; j++)
			if(cache[j].type == i)
				return cache[j];

		cacheIndex = (cacheIndex + 1) % 20;
		ObjectDef class46 = cache[cacheIndex];
		try {
			stream.currentOffset = streamIndices[i];
		} catch(Exception e) {
			e.printStackTrace();
		}
		class46.type = i;
		class46.setDefaults();
		class46.readValues(stream);
		return class46;
	}
	
	private void setDefaults()
	{
		modelIds = null;
		modelTypes = null;
		name = null;
		description = null;
		modifiedModelColors = null;
		originalModelColors = null;
		width = 1;
		length = 1;
		solid = true;
		impenetrable = true;
		hasActions = false;
		contouredGround = false;
		delayShading = false;
		occludes = false;
		animation = -1;
		decorDisplacement = 16;
		ambientLighting = 0;
		lightDiffusion = 0;
		actions = null;
		minimapFunction = -1;
		mapscene = -1;
		inverted = false;
		castsShadow = true;
		scaleX = 128;
		scaleY = 128;
		scaleZ = 128;
		surroundings = 0;
		translateX = 0;
		translateY = 0;
		translateZ = 0;
		obstructsGround = false;
		hollow = false;
		supportItems = -1;
		varbit = -1;
		varp = -1;
		childrenIDs = null;
        walkType = 2;
	}

	public void method574(OnDemandFetcher class42_sub1)
	{
		if(modelIds == null)
			return;
		for(int j = 0; j < modelIds.length; j++)
			class42_sub1.passiveRequest(modelIds[j] & 0xffff, 0);
	}

	public static void nullLoader()
	{
		baseModels = null;
		mruNodes2 = null;
		streamIndices = null;
		cache = null;
		stream = null;
	}

	public static int totalObjects;
	public static void unpackConfig(Archive streamLoader)
	{
		stream = new Stream(streamLoader.getFile("loc.dat"));
		Stream stream = new Stream(streamLoader.getFile("loc.idx"));
		totalObjects = stream.readUnsignedShort();
		streamIndices = new int[totalObjects];
		if(ClientSettings.DevMode) {
			System.out.println("Objects Loaded: " + totalObjects);
		}
		int i = 2;
		for(int j = 0; j < totalObjects; j++)
		{
			streamIndices[j] = i;
			i += stream.readUnsignedShort();
		}
		
		cache = new ObjectDef[20];
		for(int k = 0; k < 20; k++)
			cache[k] = new ObjectDef();
	}

	public boolean method577(int i)
	{
		if(modelTypes == null)
		{
			if(modelIds == null)
				return true;
			if(i != 10)
				return true;
			boolean flag1 = true;
			for(int k = 0; k < modelIds.length; k++)
				flag1 &= Model.isCached(modelIds[k] & 0xffff);

			return flag1;
		}
		for(int j = 0; j < modelTypes.length; j++)
			if(modelTypes[j] == i)
				return Model.isCached(modelIds[j] & 0xffff);

		return true;
	}

	public Model method578(int i, int j, int k, int l, int i1, int j1, int k1)
	{
		Model model = method581(i, k1, j);
		if(model == null)
			return null;
		if(contouredGround || delayShading)
			model = new Model(contouredGround, delayShading, model);
		if(contouredGround)
		{
			int l1 = (k + l + i1 + j1) / 4;
			for(int i2 = 0; i2 < model.anInt1626; i2++)
			{
				int j2 = model.anIntArray1627[i2];
				int k2 = model.anIntArray1629[i2];
				int l2 = k + ((l - k) * (j2 + 64)) / 128;
				int i3 = j1 + ((i1 - j1) * (j2 + 64)) / 128;
				int j3 = l2 + ((i3 - l2) * (k2 + 64)) / 128;
				model.anIntArray1628[i2] += j3 - l1;
			}

			model.method467();
		}
		return model;
	}

	public boolean method579()
	{
		if(modelIds == null)
			return true;
		boolean flag1 = true;
		for(int i = 0; i < modelIds.length; i++)
			flag1 &= Model.isCached(modelIds[i] & 0xffff);
			return flag1;
	}

	public ObjectDef method580()
	{
		int i = -1;
		if(varbit != -1)
		{
			VarBit varBit = VarBit.cache[varbit];
			int j = varBit.configId;
			int k = varBit.leastSignificantBit;
			int l = varBit.mostSignificantBit;
			int i1 = Client.BIT_MASKS[l - k];
			i = clientInstance.variousSettings[j] >> k & i1;
		} else
		if(varp != -1)
			i = clientInstance.variousSettings[varp];
		if(i < 0 || i >= childrenIDs.length || childrenIDs[i] == -1)
			return null;
		else
			return forID(childrenIDs[i]);
	}

	private Model method581(int j, int k, int l)
	{
		Model model = null;
		long l1;
		if(modelTypes == null)
		{
			if(j != 10)
				return null;
			l1 = (long)((type << 6) + l) + ((long)(k + 1) << 32);
			Model model_1 = (Model) mruNodes2.insertFromCache(l1);
			if(model_1 != null)
				return model_1;
			if(modelIds == null)
				return null;
			boolean flag1 = inverted ^ (l > 3);
			int k1 = modelIds.length;
			for(int i2 = 0; i2 < k1; i2++)
			{
				int l2 = modelIds[i2];
				if(flag1)
					l2 += 0x10000;
				model = (Model) baseModels.insertFromCache(l2);
				if(model == null)
				{
					model = Model.getModel(l2 & 0xffff);
					if(model == null)
						return null;
					if(flag1)
						model.method477();
					baseModels.removeFromCache(model, l2);
				}
				if(k1 > 1)
					aModelArray741s[i2] = model;
			}

			if(k1 > 1)
				model = new Model(k1, aModelArray741s);
		} else
		{
			int i1 = -1;
			for(int j1 = 0; j1 < modelTypes.length; j1++)
			{
				if(modelTypes[j1] != j)
					continue;
				i1 = j1;
				break;
			}

			if(i1 == -1)
				return null;
			l1 = (long)((type << 6) + (i1 << 3) + l) + ((long)(k + 1) << 32);
			Model model_2 = (Model) mruNodes2.insertFromCache(l1);
			if(model_2 != null)
				return model_2;
			int j2 = modelIds[i1];
			boolean flag3 = inverted ^ (l > 3);
			if(flag3)
				j2 += 0x10000;
			model = (Model) baseModels.insertFromCache(j2);
			if(model == null)
			{
				model = Model.getModel(j2 & 0xffff);
				if(model == null)
					return null;
				if(flag3)
					model.method477();
				baseModels.removeFromCache(model, j2);
			}
		}
		boolean flag;
		flag = scaleX != 128 || scaleY != 128 || scaleZ != 128;
		boolean flag2;
		flag2 = translateX != 0 || translateY != 0 || translateZ != 0;
		Model model_3 = new Model(modifiedModelColors == null, Frame.method532(k), l == 0 && k == -1 && !flag && !flag2, model);
		if(k != -1)
		{
			model_3.method469();
			model_3.method470(k);
			model_3.anIntArrayArray1658 = null;
			model_3.anIntArrayArray1657 = null;
		}
		while(l-- > 0) 
			model_3.method473();
		if(modifiedModelColors != null)
		{
			for(int k2 = 0; k2 < modifiedModelColors.length; k2++)
				model_3.method476(modifiedModelColors[k2], originalModelColors[k2]);

		}
		if(flag)
			model_3.method478(scaleX, scaleZ, scaleY);
		if(flag2)
			model_3.method475(translateX, translateY, translateZ);
		model_3.method479(64 + ambientLighting, 768 + lightDiffusion * 5, -50, -10, -50, !delayShading);
		if(supportItems == 1)
			model_3.anInt1654 = model_3.modelHeight;
		mruNodes2.removeFromCache(model_3, l1);
		return model_3;
	}

	private void readValues(Stream stream)
	{
		int i = -1;
		label0:
		do
		{
			int j;
			do
			{
				j = stream.readUnsignedByte();
				if(j == 0)
					break label0;
				if(j == 1)
				{
					int k = stream.readUnsignedByte();
					if(k > 0)
						if(modelIds == null || lowMem)
						{
							modelTypes = new int[k];
							modelIds = new int[k];
							for(int k1 = 0; k1 < k; k1++)
							{
								modelIds[k1] = stream.readUnsignedShort();
								modelTypes[k1] = stream.readUnsignedByte();
							}

						} else
						{
							stream.currentOffset += k * 3;
						}
				} else
				if(j == 2)
					name = stream.readString();
				else
				if(j == 3)
					description = stream.readBytes();
				else
				if(j == 5)
				{
					int l = stream.readUnsignedByte();
					if(l > 0)
						if(modelIds == null || lowMem)
						{
							modelTypes = null;
							modelIds = new int[l];
							for(int l1 = 0; l1 < l; l1++)
								modelIds[l1] = stream.readUnsignedShort();

						} else
						{
							stream.currentOffset += l * 2;
						}
				} else
				if(j == 14)
					width = stream.readUnsignedByte();
				else
				if(j == 15)
					length = stream.readUnsignedByte();
				else
				if(j == 17) {
					solid = false;
					walkType = 0;
				} else
				if(j == 18)
					impenetrable = false;
				else
				if(j == 19)
				{
					i = stream.readUnsignedByte();
					if(i == 1)
						hasActions = true;
				} 
				else
				if(j == 21)
					contouredGround = true;
				else
				if(j == 22)
					delayShading = true;
				else
				if(j == 23)
					occludes = true;
				else
				if(j == 24)
				{
					animation = stream.readUnsignedShort();
					if(animation == 65535)
						animation = -1;
				} else
				if(j == 27)
					walkType = 1;
				if(j == 28)
					decorDisplacement = stream.readUnsignedByte();
				else
				if(j == 29)
					ambientLighting = stream.readSignedByte();
				else
				if(j == 39)
					lightDiffusion = stream.readSignedByte();
				else
				if(j >= 30 && j < 39)
				{
					if(actions == null)
						actions = new String[5];
					actions[j - 30] = stream.readString();
					if(actions[j - 30].equalsIgnoreCase("hidden"))
						actions[j - 30] = null;
				} else
				if(j == 40)
				{
					int i1 = stream.readUnsignedByte();
					modifiedModelColors = new int[i1];
					originalModelColors = new int[i1];
					for(int i2 = 0; i2 < i1; i2++)
					{
						modifiedModelColors[i2] = stream.readUnsignedShort();
						originalModelColors[i2] = stream.readUnsignedShort();
					}

				} else
				if(j == 60)
					minimapFunction = stream.readUnsignedShort();
				else
				if(j == 62)
					inverted = true;
				else
				if(j == 64)
					castsShadow = false;
				else
				if(j == 65)
					scaleX = stream.readUnsignedShort();
				else
				if(j == 66)
					scaleY = stream.readUnsignedShort();
				else
				if(j == 67)
					scaleZ = stream.readUnsignedShort();
				else
				if(j == 68)
					mapscene = stream.readUnsignedShort();
				else
				if(j == 69)
					surroundings = stream.readUnsignedByte();
				else
				if(j == 70)
					translateX = stream.readSignedShort();
				else
				if(j == 71)
					translateY = stream.readSignedShort();
				else
				if(j == 72)
					translateZ = stream.readSignedShort();
				else
				if(j == 73)
					obstructsGround = true;
				else
				if(j == 74)
				{
					hollow = true;
				} else
				{
					if(j != 75)
						continue;
					supportItems = stream.readUnsignedByte();
				}
				continue label0;
			} while(j != 77);
			varbit = stream.readUnsignedShort();
			if(varbit == 65535)
				varbit = -1;
			varp = stream.readUnsignedShort();
			if(varp == 65535)
				varp = -1;
			int j1 = stream.readUnsignedByte();
			childrenIDs = new int[j1 + 1];
			for(int j2 = 0; j2 <= j1; j2++)
			{
				childrenIDs[j2] = stream.readUnsignedShort();
				if(childrenIDs[j2] == 65535)
					childrenIDs[j2] = -1;
			}

		} while(true);
		if(i == -1)
		{
			hasActions = modelIds != null && (modelTypes == null || modelTypes[0] == 10);
			if(actions != null)
				hasActions = true;
		}
		if(hollow)
		{
			solid = false;
			impenetrable = false;
		}
		if(supportItems == -1)
			supportItems = solid ? 1 : 0;
	}

	private ObjectDef()
	{
		type = -1;
	}

	public int walkType = 2;
	public boolean obstructsGround;
	public byte ambientLighting;
	public int translateX;
	public String name;
	public int scaleZ;
	private static final Model[] aModelArray741s = new Model[4];
	public byte lightDiffusion;
	public int width;
	public int translateY;
	public int minimapFunction;
	public int[] originalModelColors;
	public int scaleX;
	public int varp;
	public boolean inverted;
	public static boolean lowMem;
	private static Stream stream;
	public int type;
	private static int[] streamIndices;
	public boolean impenetrable;
	public int mapscene;
	public int childrenIDs[];
	public int supportItems;
	public int length;
	public boolean contouredGround;
	public boolean occludes;
	public static Client clientInstance;
	public boolean hollow;
	public boolean solid;
	public int surroundings;
	public boolean delayShading;
	private static int cacheIndex;
	public int scaleY;
	public int[] modelIds;
	public int varbit;
	public int decorDisplacement;
	public int[] modelTypes;
	public byte description[];
	public boolean hasActions;
	public boolean castsShadow;
	public static MRUNodes mruNodes2 = new MRUNodes(30);
	public int animation;
	private static ObjectDef[] cache;
	public int translateZ;
	public int[] modifiedModelColors;
	public static MRUNodes baseModels = new MRUNodes(500);
	public String actions[];
}