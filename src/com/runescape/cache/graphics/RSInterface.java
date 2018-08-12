package com.runescape.cache.graphics;

import com.runescape.Client;
import com.runescape.cache.Archive;
import com.runescape.cache.Index;
import com.runescape.cache.anim.Frame;
import com.runescape.cache.definitions.EntityDef;
import com.runescape.cache.definitions.ItemDef;
import com.runescape.drawing.DrawingArea;
import com.runescape.entity.model.Model;
import com.runescape.io.FileOperations;
import com.runescape.io.Stream;
import com.runescape.link.MRUNodes;
import com.runescape.util.TextUtil;
import vscape.cache.media.SpriteLoader;

public final class RSInterface {

	public static final int ACTION_OK = 1;
	public static final int ACTION_USABLE = 2;
	public static final int ACTION_CLOSE = 3;
	public static final int ACTION_TOGGLE_CONFIG = 4;
	public static final int ACTION_SET_CONFIG = 5;
	public static final int ACTION_CONTINUE = 6;

	public static final int TYPE_CONTAINER = 0;
	public static final int TYPE_MODEL_LIST = 1;
	public static final int TYPE_INVENTORY = 2;
	public static final int TYPE_RECTANGLE = 3;
	public static final int TYPE_TEXT = 4;
	public static final int TYPE_SPRITE = 5;
	public static final int TYPE_MODEL = 6;
	public static final int TYPE_ITEM_LIST = 7;
	public static final int TYPE_HOVER_BOX = 8;

	public static void unpack(Archive streamLoader, Index interfaces, Archive mediaStreamLoader, TextDrawingArea[] TDAS, RSFont[] RSFS) {
		mediaLoader = mediaStreamLoader;
		TDA = TDAS;
		RSF = RSFS;
		Stream stream = new Stream(streamLoader.getFile("interface.idx"));
		int totalInterfaces = stream.readUnsignedShort();
		interfaceCache = new RSInterface[totalInterfaces][];
		for (int index = 0; index < interfaceCache.length; index++) {
			int interfaceId = stream.readUnsignedShort();
			int childCount = stream.readUnsignedShort();
			if (childCount <= 0)
				continue;
			byte[] interfaceBytes = FileOperations.Decompress(interfaces.get(interfaceId));
			if (interfaceBytes == null)
				continue;
			int i = 0;
			int[] streamIndices = new int[childCount];
			for (int idxIndice = 0; idxIndice < childCount; idxIndice++) {
				streamIndices[idxIndice] = i;
				i += stream.readUnsignedShort();
			}
			Stream datStream = new Stream(interfaceBytes);
			interfaceCache[index] = new RSInterface[childCount];
			for (int childID = 0; childID < childCount; childID++) {
				datStream.currentOffset = streamIndices[childID];
				interfaceCache[index][childID] = new RSInterface();
				interfaceCache[index][childID].interfaceID = interfaceId;
				interfaceCache[index][childID].interfaceHash = (interfaceId << 16) + childID;
				interfaceCache[index][childID].readValues(datStream);
			}
		}
		//Anything else that needs modified goes here
	}

	public void readValues(Stream stream) {
		try {
			type = stream.readUnsignedByte();
			actionType = stream.readUnsignedByte();
			contentType = stream.readUnsignedShort();
			x = stream.readSignedShort();
			y = stream.readSignedShort();
			width = stream.readUnsignedShort();
			height = stream.readUnsignedShort();
			opacity = (byte) stream.readUnsignedByte();
			parentID = stream.readUnsignedShort();
			if (parentID == 65535) {
				parentID = -1;
			} else {
				parentID = parentID + (~0xffff & interfaceHash);
			}
			mouseOverID = stream.readUnsignedShort();
			if (mouseOverID == 65535) {
				mouseOverID = -1;
			}
			int valuecomparecount = stream.readUnsignedByte();
			if (valuecomparecount > 0) {
				valueCompareType = new int[valuecomparecount];
				requiredValues = new int[valuecomparecount];
				for (int vcIdx = 0; vcIdx < valuecomparecount; vcIdx++) {
					valueCompareType[vcIdx] = stream.readUnsignedByte();
					requiredValues[vcIdx] = stream.readUnsignedShort();
				}
			}
			int scriptLength = stream.readUnsignedByte();
			if (scriptLength > 0) {
				scripts = new int[scriptLength][];
				for (int blockIdx = 0; blockIdx < scriptLength; blockIdx++) {
					int cs1blocklen = stream.readUnsignedShort();
					scripts[blockIdx] = new int[cs1blocklen];
					for (int cs1opcIdx = 0; cs1opcIdx < cs1blocklen; cs1opcIdx++) {
						scripts[blockIdx][cs1opcIdx] = stream.readDWord();
						if (scripts[blockIdx][cs1opcIdx] == 65535)
							scripts[blockIdx][cs1opcIdx] = -1;
					}
				}
			}
			if (type == 0) {
				scrollMax = stream.readUnsignedShort();
				isHidden = stream.readUnsignedByte() == 1;
				toggledContainer = stream.readUnsignedByte() == 1;
			}
			if (type == 1) {
				stream.readUnsignedShort();
				stream.readUnsignedByte();
			}
			if (type == 2) {
				inventoryItemIds = new int[width * height];
				inventoryStackSizes = new int[width * height];
				itemsCanBeSwapped = stream.readUnsignedByte() == 1;
				hasActions = stream.readUnsignedByte() == 1;
				usableItemInterface = stream.readUnsignedByte() == 1;
				replacableItemInterface = stream.readUnsignedByte() == 1;
				canChangeInventory = stream.readUnsignedByte() == 1;
				drawInvAmount = stream.readUnsignedByte() == 1;
				spritePaddingX = stream.readUnsignedByte();
				spritePaddingY = stream.readUnsignedByte();
				spritesX = new int[20];
				spritesY = new int[20];
				sprites = new Sprite[20];
				spriteHashes = new String[20];
				for (int spriteIDX = 0; spriteIDX < 20; spriteIDX++) {
					int spriteEnabled = stream.readUnsignedByte();
					if (spriteEnabled == 1) {
						spritesX[spriteIDX] = stream.readSignedShort();
						spritesY[spriteIDX] = stream.readSignedShort();
						String s1 = stream.readString();
						spriteHashes[spriteIDX] = s1;
						if (mediaLoader != null && s1.length() > 0) {
							int i5 = s1.lastIndexOf(",");
							sprites[spriteIDX] = getCacheSprite(Integer.parseInt(s1.substring(i5 + 1)), mediaLoader, s1.substring(0, i5));
						}
					}
				}
				actions = new String[5];
				for (int actionid = 0; actionid < 5; actionid++) {
					String action = stream.readString();
					if (action.length() > 0) {
						actions[actionid] = action;
					} else {
						actions[actionid] = null;
					}
				}
			}
			if (type == 3) {
				filled = stream.readUnsignedByte() == 1;
			}
			if (type == 4 || type == 1) {
				fontSystem = stream.readUnsignedByte();
				font = stream.readUnsignedShort();
				if (fontSystem == 0) {
					centerText = stream.readUnsignedByte() == 1; // Backwards compat alignment
				} else if (fontSystem == 1) {
					horizontalAlignment = stream.readUnsignedByte();
					verticalAlignment = stream.readUnsignedByte();
					lineSpacing = stream.readUnsignedByte();
				}
				textShadow = stream.readUnsignedByte() == 1;
			}
			if (type == 4) {
				disabledText = stream.readString();
				enabledText = stream.readString();
			}
			if (type == 1 || type == 3 || type == 4)
				disabledColor = stream.readDWord();
			if (type == 3 || type == 4) {
				enabledColor = stream.readDWord();
				disabledMouseOverColor = stream.readDWord();
				enabledMouseOverColor = stream.readDWord();
			}
			if (type == 5) {
				spriteSystem = stream.readUnsignedByte();
				if (spriteSystem == 0) { //default cache media
					String s = stream.readString();
					disabledSpriteHash = s;
					if (mediaLoader != null && s.length() > 0) {
						int i4 = s.lastIndexOf(",");
						disabledSprite = getCacheSprite(Integer.parseInt(s.substring(i4 + 1)), mediaLoader, s.substring(0, i4));
					}
					s = stream.readString();
					enabledSpriteHash = s;
					if (mediaLoader != null && s.length() > 0) {
						int j4 = s.lastIndexOf(",");
						enabledSprite = getCacheSprite(Integer.parseInt(s.substring(j4 + 1)), mediaLoader, s.substring(0, j4));
					}
				} else {
					//TODO MAKE SUPPORT FOR SUB ARCHIVE HASHES
					String s = stream.readString();
					disabledSpriteHash = s;
					if (s.length() > 0) {
						int i4 = s.lastIndexOf(",");
						disabledSprite = getSpriteLoader(s.substring(0, i4), Integer.parseInt(s.substring(i4 + 1)));
					}
					s = stream.readString();
					enabledSpriteHash = s;
					if (mediaLoader != null && s.length() > 0) {
						int j4 = s.lastIndexOf(",");
						enabledSprite = getSpriteLoader(s.substring(0, j4), Integer.parseInt(s.substring(j4 + 1)));
					}
				}
				flipHorizontal = stream.readUnsignedByte() == 1;
				flipVertical = stream.readUnsignedByte() == 1;
			}
			if (type == 6) {
				defaultMediaType = 1;
				defaultMedia = stream.readUnsignedShort();
				if (defaultMedia == 65535)
					defaultMedia = -1;
				secondaryMediaType = 1;
				secondaryMedia = stream.readUnsignedShort();
				if (secondaryMedia == 65535)
					secondaryMedia = -1;
				defaultAnimationId = stream.readUnsignedShort();
				if (defaultAnimationId == 65535)
					defaultAnimationId = -1;
				secondaryAnimationId = stream.readUnsignedShort();
				if (secondaryAnimationId == 65535)
					secondaryAnimationId = -1;
				modelZoom = stream.readUnsignedShort();
				modelRotationX = stream.readUnsignedShort();
				modelRotationY = stream.readUnsignedShort();
			}
			if (type == 7) {
				inventoryItemIds = new int[height * width];
				inventoryStackSizes = new int[height * width];
				fontSystem = stream.readUnsignedByte();
				font = stream.readUnsignedShort();
				if (fontSystem == 0) {
					centerText = stream.readUnsignedByte() == 1; // Backwards compat alignment
				} else if (fontSystem == 1) {
					horizontalAlignment = stream.readUnsignedByte();
					verticalAlignment = stream.readUnsignedByte();
					lineSpacing = stream.readUnsignedByte();
				}
				textShadow = stream.readUnsignedByte() == 1;
				disabledColor = stream.readDWord();
				spritePaddingX = stream.readUnsignedShort();
				spritePaddingY = stream.readUnsignedShort();
				hasActions = stream.readUnsignedByte() == 1;
				actions = new String[5];
				for (int actionid = 0; actionid < 5; actionid++) {
					String action = stream.readString();
					if (action.length() > 0) {
						actions[actionid] = action;
					} else {
						actions[actionid] = null;
					}
				}
			}
			if (type == 8) {
				disabledText = stream.readString();
				enabledText = stream.readString();
			}
			/*
			if (type == 12) {
				  render a box similar to type 3
				  but it fills the whole game screen
				  
				  ? maybe 
			}
			*/
			if (actionType == 2 || type == 2) {
				selectedActionName = stream.readString();
				spellName = stream.readString();
				spellUsableOn = stream.readUnsignedShort();
			}
			if (actionType == 1 || actionType == 4 || actionType == 5 || actionType == 6) {
				tooltip = stream.readString();
				if (tooltip.length() == 0) {
					if (actionType == 1)
						tooltip = "Ok";
					if (actionType == 4)
						tooltip = "Select";
					if (actionType == 5)
						tooltip = "Select";
					if (actionType == 6)
						tooltip = "Continue";
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private Model getModel(int i, int j) {
		if (j == -1) {
			return null;
		}
		Model model = (Model) modelCache.insertFromCache((i << 16) + j);
		if (model != null)
			return model;
		if (i == 1)
			model = Model.getModel(j);
		if (i == 2)
			model = EntityDef.forID(j).method160();
		if (i == 3)
			model = Client.myPlayer.method453();
		if (i == 4)
			model = ItemDef.forID(j).method202(50);
		if (i == 5)
			model = null;
		if (model != null)
			modelCache.removeFromCache(model, (i << 16) + j);
		return model;
	}

	public static Sprite getCacheSprite(int i, Archive streamLoader, String s) {
		long l = (TextUtil.method585(s) << 8) + (long) i;
		Sprite sprite = (Sprite) spriteCache.insertFromCache(l);
		if (sprite != null)
			return sprite;
		try {
			sprite = new Sprite(streamLoader, s, i);
			spriteCache.removeFromCache(sprite, l);
		} catch (Exception _ex) {
			return null;
		}
		return sprite;
	}

	private static Sprite getSpriteLoader(String archive, int index) {
		return SpriteLoader.getSprite(archive, index);
	}

	public Sprite getSprite(boolean selected) {
		Sprite sprite;
		if (selected) {
			sprite = enabledSprite;
		} else {
			sprite = disabledSprite;
		}
		if (sprite == null) {
			return null;
		}
		return sprite;
	}

	public static void method208(boolean flag, Model model) {
		int i = 0;// was parameter
		int j = 5;// was parameter
		if (flag)
			return;
		modelCache.unlinkAll();
		if (model != null && j != 4)
			modelCache.removeFromCache(model, (j << 16) + i);
	}

	public Model getAnimatedMedia(int j, int k, boolean flag) {
		int mediaType;
		int modelID;
		if (flag) {
			mediaType = secondaryMediaType;
			modelID = secondaryMedia;
		} else {
			mediaType = defaultMediaType;
			modelID = defaultMedia;
		}
		if (mediaType == 0) {
			return null;
		} else if (mediaType == 1 && modelID == -1) {
			return null;
		}
		Model model = getModel(mediaType, modelID);
		if (model == null)
			return null;
		if (k == -1 && j == -1 && model.triangleColourOrTexture == null)
			return model;
		Model model_1 = new Model(true, Frame.method532(k) & Frame.method532(j), false, model);
		if (k != -1 || j != -1)
			model_1.method469();
		if (k != -1)
			model_1.method470(k);
		if (j != -1)
			model_1.method470(j);
		model_1.method479(64, 768, -50, -10, -50, true);
		return model_1;
	}

	public DrawingArea getFont() {
		if (this.fontSystem == 1) {
			return RSF[font];
		}
		return TDA[font];
	}
	
	public RSInterface setRxRyMz(int rx, int ry, int zoom) {
        this.modelRotationX = rx;
        this.modelRotationY = ry;
        this.modelZoom = zoom;
        return this;
    }
   
    public RSInterface setFont(int fontSystem, int font) {
        this.fontSystem = fontSystem;
        this.font = font;
        return this;
    }
   
    public RSInterface setPos(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }
   
    public RSInterface setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

	public static RSInterface getInterface(int hash) {
		int interfaceID = hash >> 16;
		int componentID = hash & '\uffff';
		if (interfaceID < 0 || interfaceID >= interfaceCache.length || interfaceCache[interfaceID] == null || componentID >= interfaceCache[interfaceID].length || interfaceCache[interfaceID][componentID] == null) {
			return null;
		}
		return interfaceCache[interfaceID][componentID];
	}

	public static RSInterface getInterface(int id, int childID) {
		if (id < 0 || id >= interfaceCache.length || interfaceCache[id] == null || childID >= interfaceCache[id].length || interfaceCache[id][childID] == null) {
			return null;
		}
		return interfaceCache[id][childID];
	}

	public void swapInventoryItems(int i, int j) {
		int k = inventoryItemIds[i];
		inventoryItemIds[i] = inventoryItemIds[j];
		inventoryItemIds[j] = k;
		k = inventoryStackSizes[i];
		inventoryStackSizes[i] = inventoryStackSizes[j];
		inventoryStackSizes[j] = k;
	}

	public RSInterface() {
		parentID = -1;
		mouseOverID = -1;
		defaultMedia = -1;
		secondaryMedia = -1;
		defaultAnimationId = -1;
		secondaryAnimationId = -1;
		drawInvAmount = true;
	}

	public static int totalInterfaces;
	public static RSInterface[][] interfaceCache;
	public static Archive mediaLoader;
	public static TextDrawingArea[] TDA;
	private static RSFont[] RSF;
	private static final MRUNodes spriteCache = new MRUNodes(200);
	private static final MRUNodes modelCache = new MRUNodes(50);

	public int type;
	public int actionType, contentType;
	public int x, y, width, height, xOffset, yOffset;
	public int opacity;
	public int interfaceID;
	public int parentID = -1, mouseOverID = -1, interfaceHash;
	public int[] valueCompareType, requiredValues;
	public int[][] scripts;
	public int scrollMax, scrollPosition;
	public boolean isHidden, toggledContainer;
	public int[] inventoryItemIds, inventoryStackSizes;
	public boolean itemsCanBeSwapped, hasActions, usableItemInterface, replacableItemInterface, canChangeInventory, drawInvAmount;
	public int spritePaddingX, spritePaddingY;
	public int[] spritesX, spritesY;
	public Sprite[] sprites;
	public String[] actions;
	public boolean filled;
	public int fontSystem, font, textAlignment;
	public boolean centerText, textShadow;
	public int horizontalAlignment, verticalAlignment, lineSpacing;
	public String disabledText, enabledText;
	public int disabledColor, enabledColor, disabledMouseOverColor, enabledMouseOverColor;
	public int spriteSystem;
	public Sprite disabledSprite, enabledSprite;
	public boolean flipHorizontal, flipVertical;
	public int defaultMediaType, defaultMedia, secondaryMediaType, secondaryMedia, defaultAnimationId, secondaryAnimationId;
	public int modelZoom, modelRotationX, modelRotationY;
	public String selectedActionName, spellName;
	public int spellUsableOn;
	public String tooltip;
	public int anInt208, anInt246;
	public String[] spriteHashes;
	public String disabledSpriteHash, enabledSpriteHash;
	public String interfaceURL, interfaceURLDisplay;

}
