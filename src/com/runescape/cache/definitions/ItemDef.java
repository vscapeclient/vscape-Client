package com.runescape.cache.definitions;

import com.runescape.cache.Archive;
import com.runescape.cache.graphics.Sprite;
import com.runescape.drawing.DrawingArea;
import com.runescape.drawing.Texture;
import com.runescape.entity.model.Model;
import com.runescape.io.Stream;
import com.runescape.link.MRUNodes;

public final class ItemDef {

	public static void nullLoader() {
		mruNodes2 = null;
		mruNodes1 = null;
		streamIndices = null;
		cache = null;
		stream = null;
	}

	public boolean method192(int j) {
		int k = maleDialogueModel1;
		int l = maleDialogueModel2;
		if(j == 1)
		{
			k = femaleDialogueModel1;
			l = femaleDialogueModel2;
		}
		if(k == -1)
			return true;
		boolean flag = true;
		if(!Model.isCached(k))
			flag = false;
		if(l != -1 && !Model.isCached(l))
			flag = false;
		return flag;
	}

	private static void convertToUnid(ItemDef itemDef, boolean grimy) {
		if (itemDef.certID == -1) {
			if (grimy) {
				itemDef.description = "I need a closer look to identify this.".getBytes();
				itemDef.name = "Herb";
				itemDef.modifiedModelColors = null;
				itemDef.originalModelColors = null;
			} else {
				if (itemDef.id == 249) {
					itemDef.name = "Guam leaf";
				} else if (itemDef.id == 257) {
					itemDef.name = "Ranarr weed";
				} else if (itemDef.id == 259) {
					itemDef.name = "Irit leaf";
				} else {
					itemDef.name = itemDef.name.substring(6);
					itemDef.name = itemDef.name.substring(0, 1).toUpperCase() + itemDef.name.substring(1);
				}
				switch (itemDef.id) {
					case 249:
						itemDef.description = "A bitter green herb.".getBytes();
						break;
					case 251:
						itemDef.description = "A herb used in poison cures.".getBytes();
						break;
					case 253:
					case 255:
					case 257:
					case 259:
					case 261:
					case 2998:
						itemDef.description = "A useful herb.".getBytes();
						break;
					case 263:
					case 265:
					case 267:
					case 269:
					case 2481:
					case 3000:
						itemDef.description = "A powerful herb.".getBytes();
						break;
				}
			}
			itemDef.modelID = 2364;
			itemDef.modelOffsetX = 4;
			itemDef.modelOffsetY = 0;
			itemDef.modelRotationX = 376;
			
			itemDef.modelRotationY = 1588;
			itemDef.modelZoom = 700;
		}
	}
	public static void unpackConfig(Archive streamLoader)	 {
		stream = new Stream(streamLoader.getFile("obj.dat"));
		Stream stream = new Stream(streamLoader.getFile("obj.idx"));
		totalItems = stream.readUnsignedShort();
		streamIndices = new int[totalItems + 100];
		int i = 2;
		for(int j = 0; j < totalItems; j++) {
			streamIndices[j] = i;
			i += stream.readUnsignedShort();
		}
		cache = new ItemDef[10];
		for(int k = 0; k < 10; k++)
			cache[k] = new ItemDef();
		
		//ConfigWriter.writeItemConfig();
	}


	public Model method194(int j) {
		int k = maleDialogueModel1;
		int l = maleDialogueModel2;
		if(j == 1) {
			k = femaleDialogueModel1;
			l = femaleDialogueModel2;
		}
		if(k == -1)
			return null;
		Model model = Model.getModel(k);
		if(l != -1) {
			Model model_1 = Model.getModel(l);
			Model aclass30_sub2_sub4_sub6s[] = {
					model, model_1
			};
			model = new Model(2, aclass30_sub2_sub4_sub6s);
		}
	   if (originalModelColors != null) {
			for (int i1 = 0; i1 < originalModelColors.length; i1++)
				model.method476(originalModelColors[i1], modifiedModelColors[i1]);

		}
		return model;
	}

	public boolean method195(int j) {
		int k = maleEquip1;
		int l = maleEquip2;
		int i1 = anInt185;
		if(j == 1) {
			k = femaleEquip1;
			l = femaleEquip2;
			i1 = anInt162;
		}
		if(k == -1)
			return true;
		boolean flag = true;
		if(!Model.isCached(k))
			flag = false;
		if(l != -1 && !Model.isCached(l))
			flag = false;
		if(i1 != -1 && !Model.isCached(i1))
			flag = false;
		return flag;
	}

	public Model method196(int i) {
		int j = maleEquip1;
		int k = maleEquip2;
		int l = anInt185;
		if(i == 1) {
			j = femaleEquip1;
			k = femaleEquip2;
			l = anInt162;
		}
		if(j == -1)
			return null;
		Model model = Model.getModel(j);
		if(k != -1)
			if(l != -1) {
				Model model_1 = Model.getModel(k);
				Model model_3 = Model.getModel(l);
				Model aclass30_sub2_sub4_sub6_1s[] = {
						model, model_1, model_3
				};
				model = new Model(3, aclass30_sub2_sub4_sub6_1s);
			} else {
				Model model_2 = Model.getModel(k);
				Model aclass30_sub2_sub4_sub6s[] = {
						model, model_2
				};
				model = new Model(2, aclass30_sub2_sub4_sub6s);
			}
		if(i == 0 && aByte205 != 0)
			model.method475(0, aByte205, 0);
		if(i == 1 && aByte154 != 0)
			model.method475(0, aByte154, 0);
		if (originalModelColors != null) {
			for (int i1 = 0; i1 < originalModelColors.length; i1++)
				model.method476(originalModelColors[i1], modifiedModelColors[i1]);

		}
		return model;
	}

	
	public void setDefaults() {
		modelID = 0;
		name = null;
		description = null;
		originalModelColors = null;
		modifiedModelColors = null;
		modelZoom = 2000;
		modelRotationX = 0;
		modelRotationY = 0;
		anInt204 = 0;
		modelOffsetX = 0;
		modelOffsetY = 0;
		stackable = false;
		value = 1;
		membersObject = false;
		groundActions = null;
		itemActions = null;
		equipmentActions = null;
		maleEquip1 = -1;
		maleEquip2 = -1;
		aByte205 = 0;
		femaleEquip1 = -1;
		femaleEquip2 = -1;
		aByte154 = 0;
		anInt185 = -1;
		anInt162 = -1;
		maleDialogueModel1 = -1;
		maleDialogueModel2 = -1;
		femaleDialogueModel1 = -1;
		femaleDialogueModel2 = -1;
		stackIDs = null;
		stackAmounts = null;
		certID = -1;
		certTemplateID = -1;
		anInt167 = 128;
		anInt192 = 128;
		anInt191 = 128;
		anInt196 = 0;
		anInt184 = 0;
		team = 0;
	}

	public static ItemDef forID(int i) {
		for(int j = 0; j < 10; j++)
			if(cache[j].id == i)
				return cache[j];
		cacheIndex = (cacheIndex + 1) % 10;
		ItemDef itemDef = cache[cacheIndex];
		stream.currentOffset = streamIndices[i];
		itemDef.id = i;
		itemDef.setDefaults();
		itemDef.readValues(stream);
		
//		if (i >= 199 && i <= 220) {
//			convertToUnid(itemDef, true);
//		}
//		if (i == 2485 || i == 3049 || i == 3051) {
//			convertToUnid(itemDef, true);
//		}
//		if (i >= 249 && i <= 270) {
//			convertToUnid(itemDef, false);
//		}
//		if (i == 2481 || i == 2998 || i == 3000) {
//			convertToUnid(itemDef, false);
//		}
		
		if(itemDef.certTemplateID != -1)
			itemDef.toNote();
		if(!isMembers && itemDef.membersObject) {
			itemDef.name = "Members Object";
			itemDef.description = "Login to a members' server to use this object.".getBytes();
			itemDef.groundActions = null;
			itemDef.itemActions = null;
			itemDef.team = 0;
		}
		
		return itemDef;
	}


	public void setItemActions(String[] a) {
		itemActions = a;
	}
	public void totalColors(int total) {
	   originalModelColors = new int[total];	   
	   modifiedModelColors = new int[total];
	}
	public void colors(int id, int original, int modified) {
		originalModelColors[id] = original;
		modifiedModelColors[id] = modified;
	}
	public void setIdentity(String n, String d) {
		name = n;
		description = d.getBytes();
	}
	public void setModels(int groundModel, int mE, int fE, int mE2, int fE2) {
		modelID = groundModel;
		maleEquip1 = mE;
		femaleEquip1 = fE;
		maleEquip2 = mE2;
		femaleEquip2 = fE2;
	}
	public void setChatModels(int m1, int m2, int f1, int f2) {
		maleDialogueModel1 = m1;
		maleDialogueModel2 = m2;
		femaleDialogueModel1 = f1;
		femaleDialogueModel2 = f2;
	}
	public void setModelConfig(int mZ, int mR1, int mR2, int mO1, int mO2) {
		modelZoom = mZ;
		modelRotationX = mR1;
		modelRotationY = mR2;
		modelOffsetX = mO1;
		modelOffsetY = mO2;
	}

	public void toNote() {
		ItemDef itemDef = forID(certTemplateID);
		modelID = itemDef.modelID;
		modelZoom = itemDef.modelZoom;
		modelRotationX = itemDef.modelRotationX;
		modelRotationY = itemDef.modelRotationY;
		anInt204 = itemDef.anInt204;
		modelOffsetX = itemDef.modelOffsetX;
		modelOffsetY = itemDef.modelOffsetY;
		originalModelColors = itemDef.originalModelColors;
		modifiedModelColors = itemDef.modifiedModelColors;
		if (certID != -1) {
			ItemDef itemDef_1 = forID(certID);
			name = itemDef_1.name;
			membersObject = itemDef_1.membersObject;
			value = itemDef_1.value;
			if (itemDef_1.name != null) {
				String s = "a";
				char c = itemDef_1.name.charAt(0);
				if (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U')
					s = "an";
				description = ("Swap this note at any bank for " + s + " " + itemDef_1.name + ".").getBytes();
			}
		}
		stackable = true;
	}

	public static Sprite getSprite(int i, int j, int k) {
		if(k == 0) {
			Sprite sprite = (Sprite) mruNodes1.insertFromCache(i);
			if(sprite != null && sprite.maxHeight != j && sprite.maxHeight != -1) {
				sprite.unlink();
				sprite = null;
			}
			if(sprite != null)
				return sprite;
		}
		ItemDef itemDef = forID(i);
		if(itemDef.stackIDs == null)
			j = -1;
		if(j > 1) {
			int i1 = -1;
			for(int j1 = 0; j1 < 10; j1++)
				if(j >= itemDef.stackAmounts[j1] && itemDef.stackAmounts[j1] != 0)
					i1 = itemDef.stackIDs[j1];
			if(i1 != -1)
				itemDef = forID(i1);
		}
		Model model = itemDef.method201(1);
		if(model == null)
			return null;
		Sprite sprite = null;
		if(itemDef.certTemplateID != -1) {
			sprite = getSprite(itemDef.certID, 10, -1);
			if(sprite == null)
				return null;
		}
		Sprite sprite2 = new Sprite(32, 32);
		int k1 = Texture.textureInt1;
		int l1 = Texture.textureInt2;
		int ai[] = Texture.anIntArray1472;
		int ai1[] = DrawingArea.pixels;
		int i2 = DrawingArea.width;
		int j2 = DrawingArea.height;
		int k2 = DrawingArea.topX;
		int l2 = DrawingArea.bottomX;
		int i3 = DrawingArea.topY;
		int j3 = DrawingArea.bottomY;
		Texture.aBoolean1464 = false;
		DrawingArea.initDrawingArea(32, 32, sprite2.myPixels);
		DrawingArea.drawPixels(32, 0, 0, 0, 32);
		Texture.method364();
		int k3 = itemDef.modelZoom;
		if(k == -1)
			k3 = (int)((double)k3 * 1.5D);
		if(k > 0)
			k3 = (int)((double)k3 * 1.04D);
		int l3 = Texture.anIntArray1470[itemDef.modelRotationX] * k3 >> 16;
		int i4 = Texture.anIntArray1471[itemDef.modelRotationX] * k3 >> 16;
		model.method482(itemDef.modelRotationY, itemDef.anInt204, itemDef.modelRotationX, itemDef.modelOffsetX, l3 + model.modelHeight / 2 + itemDef.modelOffsetY, i4 + itemDef.modelOffsetY);
		for(int i5 = 31; i5 >= 0; i5--) {
			for(int j4 = 31; j4 >= 0; j4--)
				if(sprite2.myPixels[i5 + j4 * 32] == 0)
					if(i5 > 0 && sprite2.myPixels[(i5 - 1) + j4 * 32] > 1)
						sprite2.myPixels[i5 + j4 * 32] = 1;
					else if(j4 > 0 && sprite2.myPixels[i5 + (j4 - 1) * 32] > 1)
						sprite2.myPixels[i5 + j4 * 32] = 1;
					else if(i5 < 31 && sprite2.myPixels[i5 + 1 + j4 * 32] > 1)
						sprite2.myPixels[i5 + j4 * 32] = 1;
					else if(j4 < 31 && sprite2.myPixels[i5 + (j4 + 1) * 32] > 1)
						sprite2.myPixels[i5 + j4 * 32] = 1;
		}
		if(k > 0) {
			for(int j5 = 31; j5 >= 0; j5--) {
				for(int k4 = 31; k4 >= 0; k4--)
					if(sprite2.myPixels[j5 + k4 * 32] == 0)
						if(j5 > 0 && sprite2.myPixels[(j5 - 1) + k4 * 32] == 1)
							sprite2.myPixels[j5 + k4 * 32] = k;
						else if(k4 > 0 && sprite2.myPixels[j5 + (k4 - 1) * 32] == 1)
							sprite2.myPixels[j5 + k4 * 32] = k;
						else if(j5 < 31 && sprite2.myPixels[j5 + 1 + k4 * 32] == 1)
							sprite2.myPixels[j5 + k4 * 32] = k;
						else if(k4 < 31 && sprite2.myPixels[j5 + (k4 + 1) * 32] == 1)
							sprite2.myPixels[j5 + k4 * 32] = k;
			}
		} else if(k == 0) {
			for(int k5 = 31; k5 >= 0; k5--) {
				for(int l4 = 31; l4 >= 0; l4--)
					if(sprite2.myPixels[k5 + l4 * 32] == 0 && k5 > 0 && l4 > 0 && sprite2.myPixels[(k5 - 1) + (l4 - 1) * 32] > 0)
						sprite2.myPixels[k5 + l4 * 32] = 0x302020;
			}
		}
		if(itemDef.certTemplateID != -1) {
			int l5 = sprite.maxWidth;
			int j6 = sprite.maxHeight;
			sprite.maxWidth = 32;
			sprite.maxHeight = 32;
			sprite.drawSprite(0, 0);
			sprite.maxWidth = l5;
			sprite.maxHeight = j6;
		}
		if(k == 0)
			mruNodes1.removeFromCache(sprite2, i);
		DrawingArea.initDrawingArea(j2, i2, ai1);
		DrawingArea.setDrawingArea(j3, k2, l2, i3);
		Texture.textureInt1 = k1;
		Texture.textureInt2 = l1;
		Texture.anIntArray1472 = ai;
		Texture.aBoolean1464 = true;
		if(itemDef.stackable)
			sprite2.maxWidth = 33;
		else
			sprite2.maxWidth = 32;
		sprite2.maxHeight = j;
		return sprite2;
	}

	public Model method201(int i) {
		if(stackIDs != null && i > 1) {
			int j = -1;
			for(int k = 0; k < 10; k++)
				if(i >= stackAmounts[k] && stackAmounts[k] != 0)
					j = stackIDs[k];
			if(j != -1)
				return forID(j).method201(1);
		}
		Model model = (Model) mruNodes2.insertFromCache(id);
		if(model != null)
			return model;
		model = Model.getModel(modelID);
		if(model == null)
			return null;
		if(anInt167 != 128 || anInt192 != 128 || anInt191 != 128)
			model.method478(anInt167, anInt191, anInt192);
		if (originalModelColors != null) {
			for (int l = 0; l < originalModelColors.length; l++)
				model.method476(originalModelColors[l], modifiedModelColors[l]);

		}
		model.method479(64 + anInt196, 768 + anInt184, -50, -10, -50, true);
		model.aBoolean1659 = true;
		mruNodes2.removeFromCache(model, id);
		return model;
	}

	public Model method202(int i) {
		if(stackIDs != null && i > 1) {
			int j = -1;
			for(int k = 0; k < 10; k++)
				if(i >= stackAmounts[k] && stackAmounts[k] != 0)
					j = stackIDs[k];
			if(j != -1)
				return forID(j).method202(1);
		}
		Model model = Model.getModel(modelID);
		if(model == null)
			return null;
		if (originalModelColors != null) {
			for (int l = 0; l < originalModelColors.length; l++)
				model.method476(originalModelColors[l], modifiedModelColors[l]);

		}
		return model;
	}
	
	public void readValues(Stream stream) {
		do {
			int i = stream.readUnsignedByte();
			if(i == 0) {
				return;
			}
			if(i == 1) {
				modelID = stream.readUnsignedShort();
			} else if(i == 2) {
				name = stream.readString();
			} else if(i == 3) {
				description = stream.readBytes();
			} else if(i == 4) {
				modelZoom = stream.readUnsignedShort();
			} else if(i == 5) {
				modelRotationX = stream.readUnsignedShort();
			} else if(i == 6) {
				modelRotationY = stream.readUnsignedShort();
			} else if(i == 7) {
				modelOffsetX = stream.readUnsignedShort();
				if(modelOffsetX > 32767)
					modelOffsetX -= 0x10000;
			} else if(i == 8) {
				modelOffsetY = stream.readUnsignedShort();
				if(modelOffsetY > 32767)
					modelOffsetY -= 0x10000;
			} else if(i == 10) {
				stream.readUnsignedShort();
			} else if(i == 11) {
				stackable = true;
			} else if(i == 12) {
				value = stream.readDWord();
			} else if(i == 16) {
				membersObject = true;
			} else if(i == 23) {
				maleEquip1 = stream.readUnsignedShort();
				aByte205 = stream.readSignedByte();
			} else if(i == 24) {
				maleEquip2 = stream.readUnsignedShort();
			} else if(i == 25) {
				femaleEquip1 = stream.readUnsignedShort();
				aByte154 = stream.readSignedByte();
			} else if(i == 26) {
				femaleEquip2 = stream.readUnsignedShort();
			} else if(i >= 30 && i < 35) {
				if(groundActions == null)
					groundActions = new String[5];
				groundActions[i - 30] = stream.readString();
				if(groundActions[i - 30].equalsIgnoreCase("hidden"))
					groundActions[i - 30] = null;
			} else if(i >= 35 && i < 40) {
				if(itemActions == null)
					itemActions = new String[5];
				itemActions[i - 35] = stream.readString();
			} else if(i == 40) {
				int j = stream.readUnsignedByte();
				originalModelColors = new int[j];
				modifiedModelColors = new int[j];
				for(int k = 0; k < j; k++) {
					originalModelColors[k] = stream.readUnsignedShort();
					modifiedModelColors[k] = stream.readUnsignedShort();
				}
			} else if(i >= 45 && i < 50) {
				if(equipmentActions == null)
					equipmentActions = new String[5];
				equipmentActions[i - 45] = stream.readString();
			} else if(i == 78) {
				anInt185 = stream.readUnsignedShort();
			} else if(i == 79) {
				anInt162 = stream.readUnsignedShort();
			} else if(i == 90) {
				maleDialogueModel1 = stream.readUnsignedShort();
			} else if(i == 91) {
				femaleDialogueModel1 = stream.readUnsignedShort();
			} else if(i == 92) {
				maleDialogueModel2 = stream.readUnsignedShort();
			} else if(i == 93) {
				femaleDialogueModel2 = stream.readUnsignedShort();
			} else if(i == 95) {
				anInt204 = stream.readUnsignedShort();
			} else if(i == 97) {
				certID = stream.readUnsignedShort();
			} else if(i == 98) {
				certTemplateID = stream.readUnsignedShort();
			} else if(i >= 100 && i < 110) {
				if(stackIDs == null) {
					stackIDs = new int[10];
					stackAmounts = new int[10];
				}
				stackIDs[i - 100] = stream.readUnsignedShort();
				stackAmounts[i - 100] = stream.readUnsignedShort();
			} else if(i == 110) {
				anInt167 = stream.readUnsignedShort();
			} else if(i == 111) {
				anInt192 = stream.readUnsignedShort();
			} else if(i == 112) {
				anInt191 = stream.readUnsignedShort();
			} else if(i == 113) {
				anInt196 = stream.readSignedByte();
			} else if(i == 114) {
				anInt184 = stream.readSignedByte() * 5;
			} else if(i == 115) {
				team = stream.readUnsignedByte();
			}
		} while(true);
	}

	public ItemDef() {
		id = -1;
	}

	public byte aByte154;
	public int value;
	public int[] modifiedModelColors;
	public int id;
	public static MRUNodes mruNodes1 = new MRUNodes(100);
	public static MRUNodes mruNodes2 = new MRUNodes(50);
	public int[] originalModelColors;
	public boolean membersObject;
	public int anInt162;
	public int certTemplateID;
	public int femaleEquip2;
	public int maleEquip1;
	public int maleDialogueModel2;
	public int anInt167;
	public String groundActions[];
	public int modelOffsetX;
	public String name;
	public static ItemDef[] cache;
	public int femaleDialogueModel2;
	public int modelID;
	public int maleDialogueModel1;
	public boolean stackable;
	public byte description[];
	public int certID;
	public static int cacheIndex;
	public int modelZoom;
	public static boolean isMembers = true;
	public static Stream stream;
	public int anInt184;
	public int anInt185;
	public int maleEquip2;
	public String itemActions[];
	public String equipmentActions[];
	public int modelRotationX;
	public int anInt191;
	public int anInt192;
	public int[] stackIDs;
	public int modelOffsetY;
	public static int[] streamIndices;
	public int anInt196;
	public int femaleDialogueModel1;
	public int modelRotationY;
	public int femaleEquip1;
	public int[] stackAmounts;
	public int team;
	public static int totalItems;
	public int anInt204;
	public byte aByte205;
	public int anInt164;
	public int anInt199;
	public int anInt188;
}