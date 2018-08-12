package com.runescape;

import com.runescape.ProxySocket.ProxyType;
import com.runescape.cache.Archive;
import com.runescape.cache.Index;
import com.runescape.cache.anim.Animation;
import com.runescape.cache.anim.Frame;
import com.runescape.cache.anim.SpotAnim;
import com.runescape.cache.definitions.EntityDef;
import com.runescape.cache.definitions.ItemDef;
import com.runescape.cache.definitions.ObjectDef;
import com.runescape.cache.graphics.*;
import com.runescape.drawing.DrawingArea;
import com.runescape.drawing.RSImageProducer;
import com.runescape.drawing.Texture;
import com.runescape.entity.*;
import com.runescape.entity.model.IdentityKit;
import com.runescape.entity.model.Model;
import com.runescape.io.FileOperations;
import com.runescape.io.Stream;
import com.runescape.link.Node;
import com.runescape.link.NodeList;
import com.runescape.map.CollisionMap;
import com.runescape.map.MapRegion;
import com.runescape.map.WorldController;
import com.runescape.map.floor.Flo;
import com.runescape.map.floor.FloOverlay;
import com.runescape.map.object.GroundDecoration;
import com.runescape.map.object.SpawnedObject;
import com.runescape.map.object.Wall;
import com.runescape.map.object.WallDecoration;
import com.runescape.net.*;
import com.runescape.setting.VarBit;
import com.runescape.setting.Varp;
import com.runescape.sign.Signlink;
import com.runescape.sound.MidiPlayer;
import com.runescape.sound.SoundPlayer;
import com.runescape.sound.Sounds;
import com.runescape.util.*;
import vscape.ClientSettings;
import vscape.SettingsManager;
import vscape.cache.CacheDownloader;
import vscape.cache.media.SpriteLoader;
import vscape.gameframe.GameFrame;
import vscape.gameframe.GameFrameManager;
import vscape.gameframe.GameFrameManager.GameFrameUI;
import vscape.widgets.HPView;
import vscape.widgets.NpcCombatDefinition;
import vscape.widgets.NpcDefinition;
import vscape.widgets.XPDrop;

import java.applet.AppletContext;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class Client extends RSApplet {

	public enum ScreenMode {
		FIXED, RESIZABLE, FULLSCREEN;
	}

	public static ScreenMode frameMode = ScreenMode.FIXED;
	public static int pixelScaling = 1;
	public static int frameWidth = 765;
	public static int frameHeight = 503;
	public static int screenAreaWidth = 512;
	public static int screenAreaHeight = 334;
	public static boolean showChatComponents = true;
	public static boolean showTabComponents = true;
	public static boolean resizableTabArea = false;
	public static boolean resizableChatArea = false;
	public static int smallTabThreshold = 1000;

	public static ScreenMode getFrameMode() {
		return frameMode;
	}

	public void setFrameMode(ScreenMode screenMode) {
		if (frameMode != screenMode) {
			frameMode = screenMode;
			if (screenMode == ScreenMode.FIXED) {
				frameWidth = 765;
				frameHeight = 503;
				cameraZoom = SettingsManager.cameraZoom;
				log_view_dist = 9;
				resizableTabArea = false;
				resizableChatArea = false;
			} else if (screenMode == ScreenMode.RESIZABLE) {
				frameWidth = SettingsManager.resizableW >= 766 ? SettingsManager.resizableW : 766;
				frameHeight = SettingsManager.resizableH >= 529 ? SettingsManager.resizableH : 529;
				cameraZoom = SettingsManager.cameraZoom;
				log_view_dist = 10;
			} else if (screenMode == ScreenMode.FULLSCREEN) {
				cameraZoom = SettingsManager.cameraZoom;
				log_view_dist = 10;
				frameWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
				frameHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
			}
			if (screenMode != ScreenMode.FIXED) {
				resizableTabArea = true;
				resizableChatArea = true;
			}
			rebuildFrameSize(screenMode, frameWidth, frameHeight);
			setBounds();
			changeConfig(161, frameMode == ScreenMode.FIXED ? 0 : 1);
			// System.out.println("ScreenMode: " + screenMode.toString());
		}
		showChatComponents = screenMode == ScreenMode.FIXED ? true : showChatComponents;
		showTabComponents = screenMode == ScreenMode.FIXED ? true : showTabComponents;
	}

	public static void rebuildFrameSize(ScreenMode screenMode, int screenWidth, int screenHeight) {
		try {
			screenAreaWidth = (screenMode == ScreenMode.FIXED) ? 512 : screenWidth;
			screenAreaHeight = (screenMode == ScreenMode.FIXED) ? 334 : screenHeight;
			frameWidth = screenWidth;
			frameHeight = screenHeight;
			if (screenMode == ScreenMode.FIXED) {
				instance.refreshFrameSize(screenMode == ScreenMode.FULLSCREEN, screenWidth * pixelScaling,
						screenHeight * pixelScaling, screenMode == ScreenMode.RESIZABLE,
						screenMode != ScreenMode.FIXED);
			} else {
				instance.refreshFrameSize(screenMode == ScreenMode.FULLSCREEN, screenWidth, screenHeight,
						screenMode == ScreenMode.RESIZABLE, screenMode != ScreenMode.FIXED);
			}
			setBounds();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void refreshFrameSize() {
		if (frameMode == ScreenMode.RESIZABLE) {
			if (frameWidth != (appletClient() ? getGameComponent().getWidth() : rsFrame.getFrameWidth())) {
				frameWidth = (appletClient() ? getGameComponent().getWidth() : rsFrame.getFrameWidth());
				screenAreaWidth = frameWidth;
				setBounds();
			}
			if (frameHeight != (appletClient() ? getGameComponent().getHeight() : rsFrame.getFrameHeight())) {
				frameHeight = (appletClient() ? getGameComponent().getHeight() : rsFrame.getFrameHeight());
				screenAreaHeight = frameHeight;
				setBounds();
			}
			if (loggedIn) {
				if (frameWidth != SettingsManager.resizableW || frameHeight != SettingsManager.resizableH) {
					SettingsManager.write();
				}
			}
		}
	}

	private static void setBounds() {
		Texture.method365(frameWidth, frameHeight);
		fullScreenTextureArray = Texture.anIntArray1472;
		Texture.method365(
				frameMode == ScreenMode.FIXED
						? (aRSImageProducer_1166 != null ? aRSImageProducer_1166.canvasWidth : 519)
						: frameWidth,
				frameMode == ScreenMode.FIXED
						? (aRSImageProducer_1166 != null ? aRSImageProducer_1166.canvasHeight : 165)
						: frameHeight);
		anIntArray1180 = Texture.anIntArray1472;
		Texture.method365(
				frameMode == ScreenMode.FIXED ? (tabImageProducer != null ? tabImageProducer.canvasWidth : 249)
						: frameWidth,
				frameMode == ScreenMode.FIXED ? (tabImageProducer != null ? tabImageProducer.canvasHeight : 335)
						: frameHeight);
		anIntArray1181 = Texture.anIntArray1472;
		Texture.method365(screenAreaWidth, screenAreaHeight);
		anIntArray1182 = Texture.anIntArray1472;
		int ai[] = new int[9];
		for (int i8 = 0; i8 < 9; i8++) {
			int k8 = 128 + i8 * 32 + 15;
			int l8 = 600 + k8 * 3;
			int i9 = Texture.anIntArray1470[k8];
			ai[i8] = l8 * i9 >> 16;
		}
		/*
		 * if (frameMode == ScreenMode.RESIZABLE && (frameWidth >= 766) && (frameWidth
		 * <= 1025) && (frameHeight >= 504) && (frameHeight <= 850)) { log_view_dist =
		 * 10; cameraZoom = 600; } else if (frameMode == ScreenMode.FIXED) { cameraZoom
		 * = 600; } else if (frameMode == ScreenMode.RESIZABLE || frameMode ==
		 * ScreenMode.FULLSCREEN) { log_view_dist = 10; cameraZoom = 600; }
		 */
		WorldController.method310(500, 800, screenAreaWidth, screenAreaHeight, ai);
		if (loggedIn) {
			gameScreenImageProducer = new RSImageProducer(screenAreaWidth, screenAreaHeight);
		}
	}

	public boolean isChatArea() {
		if (showChatComponents) {
			if (super.mouseX > 0 && super.mouseX < 519 && super.mouseY > frameHeight - 165 && super.mouseY < frameHeight
					|| super.mouseX > frameWidth - 220 && super.mouseX < frameWidth && super.mouseY > 0
							&& super.mouseY < 165) {
				return true;
			}
		}
		return false;
	}

	public boolean isTabArea() {
		if (showTabComponents) {
			if (frameWidth > smallTabThreshold) {
				if (super.mouseX >= frameWidth - 420 && super.mouseX <= frameWidth && super.mouseY >= frameHeight - 37
						&& super.mouseY <= frameHeight
						|| super.mouseX > frameWidth - 204 && super.mouseX < frameWidth
								&& super.mouseY > frameHeight - 37 - 274 && super.mouseY < frameHeight)
					return true;
			} else {
				if (super.mouseX >= frameWidth - 210 && super.mouseX <= frameWidth && super.mouseY >= frameHeight - 74
						&& super.mouseY <= frameHeight
						|| super.mouseX > frameWidth - 225 && super.mouseX < frameWidth
								&& super.mouseY > frameHeight - 74 - 274 && super.mouseY < frameHeight)
					return true;
			}
		}
		return false;
	}

	public boolean canClick() {
		if (frameMode != ScreenMode.FIXED) {
			if (mouseInRegion(frameWidth - (frameWidth <= smallTabThreshold ? 240 : 480),
					frameHeight - (frameWidth <= smallTabThreshold ? 90 : 37), frameWidth, frameHeight)) {
				return false;
			}
		}
		if (canClickMap()) {
			return false;
		}
		if (isTabArea() || isChatArea()) {
			return false;
		}
		return true;
	}

	public boolean mouseInRegion(int x1, int y1, int x2, int y2) {
		if (super.mouseX >= x1 && super.mouseX <= x2 && super.mouseY >= y1 && super.mouseY <= y2)
			return true;
		return false;
	}

	public boolean mouseInRegion2(int x, int y, int width, int height) {
		if (super.mouseX >= x && super.mouseX < x + width && super.mouseY >= y && super.mouseY < y + height) {
			return true;
		}
		return false;
	}

	public boolean clickInRegion(int x, int y, int width, int height) {
		if (super.saveClickX >= x && super.saveClickX < x + width && super.saveClickY >= y
				&& super.saveClickY < y + height)
			return true;
		return false;
	}

	public boolean canClickMap() {
		Point mapBase = gameFrame.getMapOffset(true);
		final int xOffset = mapBase.x + gameFrame.getMapImageOffset().x;
		final int yOffset = mapBase.y + gameFrame.getMapImageOffset().y;
		if (super.mouseX >= xOffset && super.mouseX <= xOffset + 146 && super.mouseY >= yOffset
				&& super.mouseY <= yOffset + 151) {
			return true;
		}
		return false;
	}

	public void drawGameframe() {
		try {
			if (frameMode == ScreenMode.FIXED) {
				if (redrawTab || redrawTabIcons) {
					drawTabArea();
					if (redrawTab)
						redrawTab = false;
					if (redrawTabIcons)
						redrawTabIcons = false;
				}
				if (redrawChatbox) {
					drawChatArea();
					redrawChatbox = false;
				}
			} else {
				drawTabArea();
				drawChatArea();
			}
			drawMinimapToScreen();
		} catch (Exception ex) {
			Signlink.reportError("Game Frame Drawing, " + ex.toString());
			throw new RuntimeException();
		}
	}

	public String getMac() {
		String sOsName = System.getProperty("os.name");
		if ((sOsName.startsWith("Linux")) || (sOsName.startsWith("HP-UX"))) {
			Process pa = null;
			Pattern p = Pattern.compile("([a-fA-F0-9]{1,2}(-|:)){5}[a-fA-F0-9]{1,2}");

			try {
				pa = Runtime.getRuntime().exec("ip addr show");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				pa.waitFor();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			BufferedReader buf = new BufferedReader(new InputStreamReader(pa.getInputStream()));
			String line = "";
			String output = "";

			try {
				while ((line = buf.readLine()) != null) {
					output += line + "\n";
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			output = output.replace("00:00:00:00:00:00", "");
			output = output.replace("ff:ff:ff:ff:ff:ff", "");

			Matcher matcher = p.matcher(output);
			if (matcher.find()) {
				line = matcher.group(0);
			} else {
				System.out.println("Error parsing output");
			}

			return line;
		} else if ((sOsName.startsWith("Mac"))) {
			Process pa = null;
			Pattern p = Pattern.compile("([a-fA-F0-9]{1,2}(-|:)){5}[a-fA-F0-9]{1,2}");

			try {
				pa = Runtime.getRuntime().exec("ifconfig");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				pa.waitFor();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			BufferedReader buf = new BufferedReader(new InputStreamReader(pa.getInputStream()));
			String line = "";
			String output = "";

			try {
				while ((line = buf.readLine()) != null) {
					output += line + "\n";
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Matcher matcher = p.matcher(output);
			if (matcher.find()) {
				line = matcher.group(0);
			} else {
				System.out.println("Error parsing output");
			}

			return line;
		} else {
			String firstInterface = null;
			Map<String, String> addressByNetwork = new HashMap<>();
			Enumeration<NetworkInterface> networkInterfaces = null;
			try {
				networkInterfaces = NetworkInterface.getNetworkInterfaces();
			} catch (SocketException e) {
				e.printStackTrace();
			}

			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface network = networkInterfaces.nextElement();

				byte[] bmac = null;
				try {
					bmac = network.getHardwareAddress();
				} catch (SocketException e) {
					e.printStackTrace();
				}
				if (bmac != null) {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < bmac.length; i++) {
						sb.append(String.format("%02X%s", bmac[i], (i < bmac.length - 1) ? "-" : ""));
					}

					if (sb.toString().isEmpty() == false) {
						addressByNetwork.put(network.getName(), sb.toString());
						// System.out.println("MAC = "+sb.toString()+" @
						// ["+network.getName()+"] "+network.getDisplayName());
					}

					if (sb.toString().isEmpty() == false && firstInterface == null) {
						firstInterface = network.getName();
					}
				}
			}

			if (firstInterface != null) {
				return addressByNetwork.get(firstInterface);
			}

			return null;
		}
	}

	private static String intToKOrMilLongName(int i) {
		String s = String.valueOf(i);
		for (int k = s.length() - 3; k > 0; k -= 3)
			s = s.substring(0, k) + "," + s.substring(k);
		if (s.length() > 8)
			s = "@gre@" + s.substring(0, s.length() - 8) + "M @whi@(" + s + ")";
		else if (s.length() > 4)
			s = "@cya@" + s.substring(0, s.length() - 4) + "K @whi@(" + s + ")";
		return " " + s;
	}

	public final String methodR(int j) {
		if (j >= 0 && j < 10000)
			return String.valueOf(j);
		if (j >= 10000 && j < 10000000)
			return j / 1000 + "K";
		if (j >= 10000000 && j < 999999999)
			return j / 1000000 + "M";
		if (j >= 999999999)
			return "*";
		else
			return "?";
	}

	public void pmQuickReply() {
		String name = null;
		for (int k = 0; k < 100; k++) {
			if (chatMessages[k] == null) {
				continue;
			}
			int l = chatTypes[k];
			if (l == 3 || l == 7) {
				name = chatNames[k];
				break;
			}
		}

		if (name == null) {
			pushMessage("You haven't received any messages to which you can reply.", 0, "");
			return;
		}

		if (name.startsWith("@cr")) {
			name = name.substring(5);
		}
		if (name.startsWith("@irn")) {
			name = name.substring(5);
		}

		long nameAsLong = TextUtil.longForName(name.trim());
		int k3 = -1;
		for (int i4 = 0; i4 < friendsCount; i4++) {
			if (friendsListAsLongs[i4] != nameAsLong)
				continue;
			k3 = i4;
			break;
		}

		if (k3 != -1) {
			if (friendsNodeIDs[k3] > 0) {
				redrawChatbox = true;
				inputDialogState = 0;
				messagePromptRaised = true;
				promptInput = "";
				friendsListAction = 3;
				aLong953 = friendsListAsLongs[k3];
				aString1121 = "Enter message to send to " + friendsList[k3];
			} else {
				pushMessage("That player is currently offline.", 0, "");
			}
		}
	}

	/*
	 * private void stopMidi() { Signlink.midifade = 0; Signlink.midi = "stop"; }
	 */

	private boolean menuHasAddFriend(int j) {
		if (j < 0)
			return false;
		int k = menuActionID[j];
		if (k >= 2000)
			k -= 2000;
		return k == 337;
	}

	private String iconSymbol(int index) {
		switch (index) {
		case 1:
			return "@cr1@";
		case 2:
			return "@cr2@";
		case 3:
			return "@cr2@";
		case 4:
			return "@irn@";
		case 5:
			return "@hci@";
		case 6:
			return "@ult@";
		}
		return "";
	}

	private int drawUserIcon(int type, int xPos, int yPos) {
		switch (type) {
		case 1:
			modIcons[0].drawBackground(xPos, yPos);
			return 15;
		case 2:
			modIcons[1].drawBackground(xPos, yPos);
			return 15;
		case 3:
			modIcons[1].drawBackground(xPos, yPos);
			return 15;
		case 4:
		case 5:
		case 6:
			Sprite ironmanIcon = SpriteLoader.getSprite("chaticons", type - 4);
			ironmanIcon.drawSprite(xPos, yPos);
			return 13;
		}
		return 0;
	}

	private int drawUserIcons(int chatRight, int userIconType, int xPos, int yPos) {
		int xOffset = 0;
		if (chatRight > 0) {
			xOffset += drawUserIcon(chatRight, xOffset + xPos, yPos);
		}
		if (userIconType > 0) {
			xOffset += drawUserIcon(userIconType, xOffset + xPos, yPos);
		}
		return xOffset;
	}

	private boolean chatStateCheck() {
		return messagePromptRaised || inputDialogState != 0 || aString844 != null || backDialogID != -1
				|| dialogID != -1;
	}

	private int totalSearchResults;
	final private String searchResultStrings[] = new String[100];
	final private int searchResultIDs[] = new int[100];
	public static int searchResultScrollPos, searchResultScrollMax = 100;

	private void definitionSearch(String searchName) {
		if (searchName == null || searchName.isEmpty()) {
			totalSearchResults = 0;
			return;
		}
		try {
			searchName = searchName.toLowerCase().trim();
			String[] searchParts = new String[] { searchName };
			final int shouldSplit = searchName.indexOf(" ");
			if (shouldSplit != -1) {
				searchParts = searchName.split(" ");
			}
			totalSearchResults = 0;
			final int limit = inputDialogState == 3 ? ItemDef.totalItems
					: inputDialogState == 4 ? EntityDef.totalNPCs : ObjectDef.totalObjects;
			mainLoop: for (int id = 0; id < limit; id++) {
				if (totalSearchResults >= searchResultStrings.length) {
					break;
				}
				String itemName = null;
				if (inputDialogState == 3) {
					ItemDef item = ItemDef.forID(id);
					if (item == null || item.certTemplateID != -1 || item.name == null || item.name.isEmpty()) {
						continue;
					}
					itemName = item.name.toLowerCase();
				} else if (inputDialogState == 4) {
					EntityDef npc = EntityDef.forID(id);
					if (npc == null || npc.name == null || npc.name.isEmpty()) {
						continue;
					}
					itemName = npc.name.toLowerCase();
				} else if (inputDialogState == 5) {
					ObjectDef obj = ObjectDef.forID(id);
					if (obj == null || obj.name == null || obj.name.isEmpty()) {
						continue;
					}
					itemName = obj.name.toLowerCase();
				}
				if (itemName == null || itemName.isEmpty()) {
					continue;
				}
				for (int index = 0; index < searchParts.length; index++) {
					if (itemName.indexOf(searchParts[index]) == -1) {
						continue mainLoop;
					}
				}
				searchResultStrings[totalSearchResults] = itemName;
				searchResultIDs[totalSearchResults] = id;
				totalSearchResults++;
			}
		} catch (Exception e) {
			System.out.println("Error searching defs:");
			e.printStackTrace();
		}
	}

	private void drawDefSearchResults(Rectangle chatBounds, int drawX, int drawY, int yOffset) {
		// Search Function Drawing
		TextDrawingArea chatFont = regularFont;
		final int charHeight = chatFont.getBaseCharHeight();
		final int spriteSpacingY = 34;
		final int lineSpacing = gameFrame.chatLineSpacing();
		DrawingArea.setDrawingArea(drawY + 20 + lineSpacing, drawX, (int) chatBounds.getWidth(), drawY);
		drawY += charHeight - 1;
		newBoldFont.drawCenteredString("SEARCH NAME: <col=255>" + amountOrNameInput + "*</col>",
				(int) chatBounds.getCenterX(), drawY, 0, -1);
		drawY += lineSpacing + 1;
		DrawingArea.method339(drawY, resizableChatArea ? 0x575757 : 0x807660, (int) chatBounds.getWidth() - 12, drawX);
		DrawingArea.setDrawingArea((int) chatBounds.getHeight() + drawY, drawX, (int) chatBounds.getWidth(), drawY);
		if (amountOrNameInput.length() == 0) {
			newBoldFont.drawCenteredString("ENTER SEARCH NAME", (int) chatBounds.getCenterX(),
					drawY + (int) chatBounds.getCenterY() - 20, 255, -1);
		} else {
			if (totalSearchResults <= 0) {
				newBoldFont.drawCenteredString("NO SEARCH RESULTS FOR: " + amountOrNameInput,
						(int) chatBounds.getCenterX(), drawY + (int) chatBounds.getCenterY() - 20, 255, -1);
			} else {
				try {
					final int itemsPerRow = 3;
					final int spriteSpacingX = chatBounds.width / itemsPerRow;
					final int yOffsetMouse = frameMode == ScreenMode.FIXED ? gameFrame.getChatOffsetY(true) : 0;
					int row = 0;
					for (int j = 0; j < totalSearchResults; j++) {
						if (searchResultStrings[j] == null || searchResultStrings[j].isEmpty()) {
							continue;
						}
						if (inputDialogState == 3) { // item search special drawing
							int itemY = (drawY + (row * spriteSpacingY)) - searchResultScrollPos;
							final int xMod = (j % itemsPerRow);
							final int itemX = drawX + (spriteSpacingX * xMod);
							if (mouseInRegion2(itemX, yOffsetMouse + itemY, spriteSpacingX, spriteSpacingY)) {
								DrawingArea.method335(0xffffff, itemY, spriteSpacingX, spriteSpacingY, 64, itemX);
							}
							Sprite itemSprite = ItemDef.getSprite(searchResultIDs[j], 64, 0);
							if (itemSprite != null) {
								itemSprite.drawSprite(itemX, itemY);
							} else {
								DrawingArea.method335(0xff0000, itemY + 2, 32, 32, 128, itemX + 2);
							}
							itemY += lineSpacing - 1;
							chatFont.method389(false, itemX + spriteSpacingY + lineSpacing, 0, searchResultStrings[j],
									itemY + charHeight);
							chatFont.method389(false, itemX + spriteSpacingY + lineSpacing, 0,
									"ID #" + searchResultIDs[j], itemY + (charHeight * 2) + lineSpacing);
							if (j < (totalSearchResults - 1) && xMod >= (itemsPerRow - 1)) {
								row++;
							}
						} else {
							final int msgY = (drawY + (j * (charHeight + (lineSpacing * 2)))
									+ (charHeight + lineSpacing)) - searchResultScrollPos;
							final int msgX = drawX;
							chatFont.method389(false, msgX, 0, searchResultStrings[j] + " - " + searchResultIDs[j],
									msgY);
						}
					}
					DrawingArea.defaultDrawingAreaSize();
					final int scrollBarHeight = (int) chatBounds.getHeight() + 1;
					if (inputDialogState == 3) {
						searchResultScrollMax = ((row + 1) * spriteSpacingY);
						if (searchResultScrollMax < (int) chatBounds.getHeight() - 1) {
							searchResultScrollMax = (int) chatBounds.getHeight() - 1;
						}
						drawScrollbar(scrollBarHeight, searchResultScrollPos, drawY, (int) chatBounds.getWidth(),
								searchResultScrollMax);
					} else {
						searchResultScrollMax = (totalSearchResults * (charHeight + (lineSpacing * 2))) + lineSpacing;
						if (searchResultScrollMax < (int) chatBounds.getHeight() - 1) {
							searchResultScrollMax = (int) chatBounds.getHeight() - 1;
						}
						drawScrollbar(scrollBarHeight, searchResultScrollPos, drawY, (int) chatBounds.getWidth(),
								searchResultScrollMax);
					}
				} catch (Exception e) {
					System.out.println("Error drawing def search:");
					e.printStackTrace();
				}
			}
		}
		DrawingArea.defaultDrawingAreaSize();
	}

	private void drawInputDialog(String title, String value, int xPos, int yPos) {
		boldFontS.drawText(0, title, 40 + yPos, xPos);
		boldFontS.drawText(128, value, 60 + yPos, xPos);
	}

	private void drawChatArea() {
		if (frameMode == ScreenMode.FIXED) {
			aRSImageProducer_1166.initDrawingArea();
		}
		Texture.anIntArray1472 = anIntArray1180;

		if (chatStateCheck()) {
			showChatComponents = true;
		}

		gameFrame.drawChatArea(this);

		Rectangle chatBounds = gameFrame.getChatBounds();
		final int yOffset = gameFrame.getChatOffsetY(false);
		int drawX = (int) chatBounds.getX();
		int drawY = yOffset + (int) chatBounds.getY();
		if (messagePromptRaised) {
			drawInputDialog(aString1121, promptInput + "*", 259, drawY);
		} else if (inputDialogState == 1) {
			drawInputDialog(amountOrNameTitle + ":", amountOrNameInput + "*", 259, drawY);
		} else if (inputDialogState == 2) {
			drawInputDialog(amountOrNameTitle + ":", amountOrNameInput + "*", 259, drawY);
		} else if (inputDialogState >= 3 && inputDialogState <= 5) {
			// Search Function Drawing
			drawDefSearchResults(chatBounds, drawX, drawY, yOffset);
		} else if (aString844 != null) {
			drawInputDialog(aString844, "Click to continue", 259, drawY);
		} else if (backDialogID != -1) {
			drawInterface(drawX + 6, drawY + 10, 520, 143, RSInterface.interfaceCache[backDialogID], -1, 0);
		} else if (dialogID != -1) {
			drawInterface(drawX + 6, drawY + 10, 520, 143, RSInterface.interfaceCache[dialogID], -1, 0);
		} else if (showChatComponents) {
			TextDrawingArea chatFont = regularFont;
			final int charHeight = chatFont.getBaseCharHeight();
			final int lineSpacing = gameFrame.chatLineSpacing();
			int messagesDisplayed = 0;
			DrawingArea.setDrawingArea((int) chatBounds.getMaxY() + yOffset, drawX, (int) chatBounds.getWidth(), drawY);
			for (int mI = 0; mI < messageDisplayLimit; mI++) {
				if (chatMessages[mI] == null)
					continue;
				String chatName = chatNames[mI];
				String chatMsg = chatMessages[mI];
				final int chatType = chatTypes[mI];
				final int chatRight = chatRights[mI];
				final boolean isFiltered = chatFiltered[mI];
				final int msgY = (((drawY + (int) chatBounds.getHeight())
						- (messagesDisplayed * (charHeight + lineSpacing))) - (lineSpacing * 2) + 1) + chatScrollPos;
				int msgX = drawX;
				int userIconType = 0;
				if (chatName != null && chatName.startsWith("@irn@")) {
					chatName = chatName.substring(5);
					userIconType = 4;
				}
				else if (chatName != null && chatName.startsWith("@hci@"))
				{
					chatName = chatName.substring(5);
					userIconType = 5;
				}
				else if (chatName != null && chatName.startsWith("@ult@"))
				{
					chatName = chatName.substring(5);
					userIconType = 6;
				}
				if (chatName == null) {
					chatName = TextUtil.fixName(myUsername);
				}
				/**
				 * 0 - Game Messages 1 - ?? Forced public? 2 - Public 3 - Private Message
				 * Receive Normal 4 - Trade Request 5 - Private Message Login | Logout 6 -
				 * Private Message Sent 7 - Private Message Receive from Admin or Mod 8 - Duel &
				 * Challenge Requests 9 - Global 12 - Link process 16 - Clan messages
				 */
				switch (chatType) {
				case 0: // Game Messages
					if ((chatTypeView == 5 || chatTypeView == 0) && (gameMode == 0 || (gameMode == 1 && isFiltered))) {
						chatFont.method389(false, msgX, 0, chatMsg, msgY);
						messagesDisplayed++;
					}
					break;
				case 1: // ?? Forced public?
				case 2: // Public
					if ((chatTypeView == 1 || chatTypeView == 0) && (chatType == 1 || publicChatMode == 0
							|| (publicChatMode == 1 && isFriendOrSelf(chatName)))) {
						msgX += drawUserIcons(chatRight, userIconType, msgX, msgY - 12);
						chatFont.method389(false, msgX, 0, chatName + ":", msgY);
						msgX += chatFont.getTextWidth(chatName + ":") + 2;
						chatFont.method389(false, msgX, 255, chatMsg, msgY);
						messagesDisplayed++;
					}
					break;
				case 3: // Private Message Receive Normal
				case 7: // Private Message Receive from Admin or Mod
					// UN SPLIT CHAT
					if (((splitPrivateChat == 0 && chatTypeView == 0) || chatTypeView == 2) && (chatType == 7
							|| privateChatMode == 0 || (privateChatMode == 1 && isFriendOrSelf(chatName)))) {
						chatFont.method389(false, msgX, 0, "From", msgY);
						msgX += chatFont.getTextWidth("From ");
						msgX += drawUserIcons(chatRight, userIconType, msgX, msgY - 12);
						chatFont.method389(false, msgX, 0, chatName + ":", msgY);
						msgX += chatFont.getTextWidth(chatName + ":") + 2;
						chatFont.method389(false, msgX, 0x800000, chatMsg, msgY);
						messagesDisplayed++;
					}
					break;
				case 4:// Trade Request
					if ((chatTypeView == 3 || chatTypeView == 0)
							&& (chatType == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(chatName)))) {
						chatFont.method389(false, msgX, 0x800080, chatName + " " + chatMsg, msgY);
						messagesDisplayed++;
					}
					break;
				case 5:// Private Message Login | Logout
					if (((splitPrivateChat == 0 && chatTypeView == 0) || chatTypeView == 2) && privateChatMode < 2) {
						chatFont.method389(false, msgX, 0x800000, chatName + " " + chatMsg, msgY);
						messagesDisplayed++;
					}
					break;
				case 6:// Private Message Sent
					if (((splitPrivateChat == 0 && chatTypeView == 0) || chatTypeView == 2) && privateChatMode < 2) {
						chatFont.method389(false, msgX, 0, "To " + chatName + ":", msgY);
						chatFont.method389(false, msgX + chatFont.getTextWidth("To :" + chatName) + 2, 0x800000,
								chatMsg, msgY);
						messagesDisplayed++;
					}
					break;
				case 8:// Duel & Challenge Requests
					if ((chatTypeView == 3 || chatTypeView == 0)
							&& (duelMode == 0 || (duelMode == 1 && isFriendOrSelf(chatName)))) {
						chatFont.method389(false, msgX, 0x7e3200, chatName + " " + chatMsg, msgY);
						messagesDisplayed++;
					}
					break;
				case 9:// Global
					if (((chatTypeView == 0 && globalMode == 0) || chatTypeView == 9)
							&& (globalMode == 0 || globalMode == 1)) {
						String prefix = chatPrefix[mI];
						chatFont.method389(false, msgX, 255, "[", msgY);
						msgX += 6;
						chatFont.method389(false, msgX, 255, prefix, msgY);
						msgX += chatFont.getTextWidth(prefix);
						chatFont.method389(false, msgX, 255, "]", msgY);
						msgX += 6;
						msgX += drawUserIcons(chatRight, userIconType, msgX, msgY - 12);
						chatFont.method389(false, msgX, 0, chatName + ":", msgY);
						msgX += chatFont.getTextWidth(chatName + ":") + 2;
						chatFont.method389(false, msgX, 0x7e3200, chatMsg, msgY);
						messagesDisplayed++;
					}
					break;
				case 12:// Link process
					if ((chatTypeView == 5 || chatTypeView == 0) && (gameMode == 0 || (gameMode == 1 && isFiltered))) {
						String prefix = chatPrefix[mI];
						chatFont.method389(false, msgX, 0, chatMsg + " @red@" + prefix, msgY);
						messagesDisplayed++;
					}
					break;
				case 16:// Clan chat
					if ((chatTypeView == 16 || (chatTypeView == 0 && clanChatMode == 0))
							&& (clanChatMode == 0 || clanChatMode == 1)) {
						String clanName = chatPrefix[mI];
						chatFont.method389(false, msgX, 0, "[", msgY);
						msgX += 6;
						chatFont.method389(false, msgX, 255, clanName, msgY);
						msgX += chatFont.getTextWidth(clanName);
						chatFont.method389(false, msgX, 0, "]", msgY);
						msgX += 6;
						msgX += drawUserIcons(chatRight, userIconType, msgX, msgY - 12);
						chatFont.method389(false, msgX, 0, chatName + ":", msgY);
						msgX += chatFont.getTextWidth(chatName + ":") + 2;
						chatFont.method389(false, msgX, 0x800000, chatMsg, msgY);
						messagesDisplayed++;
					}
					break;
				}
			}
			DrawingArea.defaultDrawingAreaSize();
			final int scrollBarHeight = (int) chatBounds.getHeight();
			chatScrollMax = (messagesDisplayed * (charHeight + lineSpacing)) + lineSpacing;// ((messagesDisplayed+1) *
																							// (charHeight+lineSpacing));
			if (chatScrollMax < (int) chatBounds.getHeight() - 1) {
				chatScrollMax = (int) chatBounds.getHeight() - 1;
			}
			drawScrollbar(scrollBarHeight, (chatScrollMax - chatScrollPos - scrollBarHeight), drawY,
					(int) chatBounds.getWidth(), chatScrollMax);
			String myName;
			if (myPlayer != null && myPlayer.name != null) {
				myName = myPlayer.name;
			} else {
				myName = TextUtil.fixName(myUsername);
			}
			drawX = (int) chatBounds.getX();
			drawY = yOffset + (int) chatBounds.getMaxY();
			DrawingArea.setDrawingArea(drawY + 20, drawX, (int) chatBounds.getWidth(), drawY);
			DrawingArea.method339(drawY, resizableChatArea ? 0x575757 : 0x807660, (int) chatBounds.getWidth() - 12,
					drawX);
			drawY += charHeight;
			drawX += drawUserIcons(myPrivilege, (myPlayer.ironMan > 0 ? myPlayer.ironMan + 3 : 0), drawX, drawY - 12);
			chatFont.method385(0, myName + ":", drawY, drawX);
			chatFont.drawChatInput(255, drawX + chatFont.getTextWidth(myName + ":") + 2, inputString + "*", drawY,
					false);
			DrawingArea.defaultDrawingAreaSize();
		}
		if (menuOpen) {
			drawMenu(0, frameMode == ScreenMode.FIXED ? 338 : 0);
		}
		if (frameMode == ScreenMode.FIXED) {
			aRSImageProducer_1166.drawGraphics(338, super.graphics, 0);
		}
		gameScreenImageProducer.initDrawingArea();
		Texture.anIntArray1472 = anIntArray1182;
	}

	public void startRunnable(Runnable runnable, int i) {
		if (i > 10)
			i = 10;
		if (Signlink.mainapp != null) {
			Signlink.startthread(runnable, i);
		} else {
			super.startRunnable(runnable, i);
		}
	}

	public ProxySocket openSocket(int port) throws IOException {
		return new ProxySocket(InetAddress.getByName(server), port);
	}

	private void processMenuClick() {
		if (activeInterfaceType != 0)
			return;
		int j = super.clickMode3;
		if (spellSelected == 1 && super.saveClickX >= 516 && super.saveClickY >= 160 && super.saveClickX <= 765
				&& super.saveClickY <= 205)
			j = 0;
		if (menuOpen) {
			if (j != 1) {
				int k = super.mouseX - 4;
				int j1 = super.mouseY - 4;
				if (k < menuOffsetX - 10 || k > menuOffsetX + menuWidth + 10 || j1 < menuOffsetY - 10
						|| j1 > menuOffsetY + menuHeight + 10) {
					menuOpen = false;
					if (menuScreenArea == 1) {
						redrawTab = true;
					}
					if (menuScreenArea == 2) {
						redrawChatbox = true;
					}
				}
			}
			if (j == 1) {
				int l = menuOffsetX;
				int k1 = menuOffsetY;
				int i2 = menuWidth;
				int k2 = super.saveClickX - 4;
				int l2 = super.saveClickY - 4;
				int i3 = -1;
				for (int j3 = 0; j3 < menuActionRow; j3++) {
					int k3 = k1 + 31 + (menuActionRow - 1 - j3) * 15;
					if (k2 > l && k2 < l + i2 && l2 > k3 - 13 && l2 < k3 + 3)
						i3 = j3;
				}
				if (i3 != -1)
					doAction(i3);
				menuOpen = false;
				if (menuScreenArea == 1) {
					redrawTab = true;
				}
				if (menuScreenArea == 2) {
					redrawChatbox = true;
				}
			}
		} else {
			if (j == 1 && menuActionRow > 0) {
				int i1 = menuActionID[menuActionRow - 1];
				if ((i1 >= 700 && i1 <= 705) || i1 == 632 || i1 == 78 || i1 == 867 || i1 == 431 || i1 == 53 || i1 == 74
						|| i1 == 454 || i1 == 539 || i1 == 493 || i1 == 847 || i1 == 447 || i1 == 1125) {
					int l1 = menuActionCmd2[menuActionRow - 1];
					int j2 = menuActionCmd3[menuActionRow - 1];
					RSInterface rsInterface = RSInterface.getInterface(j2);
					if (rsInterface.itemsCanBeSwapped || rsInterface.replacableItemInterface) {
						aBoolean1242 = false;
						anInt989 = 0;
						anInt1084 = j2;
						lastMouseInvInterfaceIndex = l1;
						activeInterfaceType = 2;
						anInt1087 = super.saveClickX;
						anInt1088 = super.saveClickY;
						if (rsInterface.interfaceID == openInterfaceID)
							activeInterfaceType = 1;
						if (rsInterface.interfaceID == backDialogID)
							activeInterfaceType = 3;
						return;
					}
				}
			}
			if (j == 1 && (anInt1253 == 1 || menuHasAddFriend(menuActionRow - 1)) && menuActionRow > 2)
				j = 2;
			if (j == 1 && menuActionRow > 0)
				doAction(menuActionRow - 1);
			if (j == 2 && menuActionRow > 0)
				determineMenuSize();

			processMainScreenClick();
			gameFrame.processTabClick(this);
			gameFrame.processChatModeClick(this);
			processMinimapHover();
		}
	}

	public static String getFileNameWithoutExtension(String fileName) {
		File tmpFile = new File(fileName);
		tmpFile.getName();
		int whereDot = tmpFile.getName().lastIndexOf('.');
		if (0 < whereDot && whereDot <= tmpFile.getName().length() - 2) {
			return tmpFile.getName().substring(0, whereDot);
		}
		return "";
	}

	private void updateWorldObjects() {
		try {
			lastPlane = -1;
			refreshMinimap = false;
			incompleteAnimables.removeAll();
			projectiles.removeAll();
			Texture.method366();
			unlinkMRUNodes();
			worldController.initToNull();
			System.gc();
			for (int i = 0; i < 4; i++)
				collisionMaps[i].method210();
			for (int l = 0; l < 4; l++) {
				for (int k1 = 0; k1 < 104; k1++) {
					for (int j2 = 0; j2 < 104; j2++)
						tileFlags[l][k1][j2] = 0;
				}
			}

			MapRegion objectManager = new MapRegion(tileFlags, tileHeights);
			int k2 = floorMapBytes.length;
			stream.createFrame(0);
			if (!constructedViewport) {
				for (int i3 = 0; i3 < k2; i3++) {
					int i4 = (regionIdArray[i3] >> 8) * 64 - baseX;
					int k5 = (regionIdArray[i3] & 0xff) * 64 - baseY;
					byte abyte0[] = floorMapBytes[i3];
					if (abyte0 != null)
						objectManager.loadMapChunk(abyte0, k5, i4, (anInt1069 - 6) * 8, (anInt1070 - 6) * 8,
								collisionMaps);
				}
				for (int j4 = 0; j4 < k2; j4++) {
					int l5 = (regionIdArray[j4] >> 8) * 64 - baseX;
					int k7 = (regionIdArray[j4] & 0xff) * 64 - baseY;
					byte abyte2[] = floorMapBytes[j4];
					if (abyte2 == null && anInt1070 < 800)
						objectManager.method174(k7, 64, 64, l5);
				}
				anInt1097++;
				if (anInt1097 > 160) {
					anInt1097 = 0;
					stream.createFrame(238);
					stream.writeWordBigEndian(96);
				}
				stream.createFrame(0);
				for (int i6 = 0; i6 < k2; i6++) {
					byte abyte1[] = objectMapBytes[i6];
					if (abyte1 != null) {
						int l8 = (regionIdArray[i6] >> 8) * 64 - baseX;
						int k9 = (regionIdArray[i6] & 0xff) * 64 - baseY;
						objectManager.method190(l8, collisionMaps, k9, worldController, abyte1);
					}
				}
			}

			if (constructedViewport) {
				for (int z = 0; z < 4; z++) {
					for (int x = 0; x < 13; x++) {
						for (int y = 0; y < 13; y++) {
							int paletteData = copyMapPalette[z][x][y];
							if (paletteData != -1) {
								int tileX = paletteData >> 14 & 0x3ff;
								int tileY = paletteData >> 3 & 0x7ff;
								int tileZ = paletteData >> 24 & 3;
								int tileRot = paletteData >> 1 & 3;
								int regionId = (tileX / 8 << 8) + tileY / 8;
								for (int l11 = 0; l11 < regionIdArray.length; l11++) {
									if (regionIdArray[l11] != regionId || floorMapBytes[l11] == null)
										continue;
									objectManager.loadCopyMapChunk(tileZ, tileRot, collisionMaps, x * 8,
											(tileX & 7) * 8, floorMapBytes[l11], (tileY & 7) * 8, z, y * 8);
									break;
								}

							}
						}
					}
				}
				for (int l4 = 0; l4 < 13; l4++) {
					for (int k6 = 0; k6 < 13; k6++) {
						int i8 = copyMapPalette[0][l4][k6];
						if (i8 == -1) {
							objectManager.method174(k6 * 8, 8, 8, l4 * 8);
						}
					}
				}

				stream.createFrame(0);
				for (int l6 = 0; l6 < 4; l6++) {
					for (int j8 = 0; j8 < 13; j8++) {
						for (int j9 = 0; j9 < 13; j9++) {
							int i10 = copyMapPalette[l6][j8][j9];
							if (i10 != -1) {
								int z = i10 >> 24 & 3;
								int rotation = i10 >> 1 & 3;
								int x = i10 >> 14 & 0x3ff;
								int y = i10 >> 3 & 0x7ff;
								int j12 = (x / 8 << 8) + y / 8;
								for (int k12 = 0; k12 < regionIdArray.length; k12++) {
									if (regionIdArray[k12] != j12 || objectMapBytes[k12] == null)
										continue;
									objectManager.method183(collisionMaps, worldController, z, j8 * 8, (y & 7) * 8, l6,
											objectMapBytes[k12], (x & 7) * 8, rotation, j9 * 8);
									break;
								}

							}
						}
					}
				}
			}
			stream.createFrame(0);
			objectManager.method171(collisionMaps, worldController);
			gameScreenImageProducer.initDrawingArea();
			stream.createFrame(0);
			int k3 = MapRegion.maximumPlane;
			if (k3 > plane)
				k3 = plane;
			if (k3 < plane - 1)
				k3 = plane - 1;
			if (lowMem)
				worldController.method275(MapRegion.maximumPlane);
			else
				worldController.method275(0);
			for (int i5 = 0; i5 < 104; i5++) {
				for (int i7 = 0; i7 < 104; i7++)
					updateGroundItem(i5, i7);

			}

			anInt1051++;
			if (anInt1051 > 98) {
				anInt1051 = 0;
				stream.createFrame(150);
			}
			method63();
		} catch (Exception exception) {
		}
		ObjectDef.baseModels.unlinkAll();
		if (super.rsFrame != null) {
			stream.createFrame(210);
			stream.writeDWord(0x3f008edd);
		}
		if (lowMem && Signlink.cache_dat != null) {
			int j = onDemandFetcher.getVersionCount(0);
			for (int i1 = 0; i1 < j; i1++) {
				int l1 = onDemandFetcher.getModelIndex(i1);
				if ((l1 & 0x79) == 0)
					Model.resetModel(i1);
			}

		}
		System.gc();
		Texture.method367();
		onDemandFetcher.method566();
		int k = (anInt1069 - 6) / 8 - 1;
		int j1 = (anInt1069 + 6) / 8 + 1;
		int i2 = (anInt1070 - 6) / 8 - 1;
		int l2 = (anInt1070 + 6) / 8 + 1;
		if (aBoolean1141) {
			k = 49;
			j1 = 50;
			i2 = 49;
			l2 = 50;
		}
		for (int l3 = k; l3 <= j1; l3++) {
			for (int j5 = i2; j5 <= l2; j5++) {
				if (l3 == k || l3 == j1 || j5 == i2 || j5 == l2) {
					int j7 = onDemandFetcher.getMapId(0, j5, l3);
					if (j7 != -1)
						onDemandFetcher.passiveRequest(j7, 3);
					int k8 = onDemandFetcher.getMapId(1, j5, l3);
					if (k8 != -1)
						onDemandFetcher.passiveRequest(k8, 3);
				}
			}
		}
	}

	private void unlinkMRUNodes() {
		ObjectDef.baseModels.unlinkAll();
		ObjectDef.mruNodes2.unlinkAll();
		EntityDef.mruNodes.unlinkAll();
		ItemDef.mruNodes2.unlinkAll();
		ItemDef.mruNodes1.unlinkAll();
		Player.mruNodes.unlinkAll();
		SpotAnim.aMRUNodes_415.unlinkAll();
	}

	/*
	 * private void method24(int i) { int ai[] = minimapImage.myPixels; int j =
	 * ai.length; for (int k = 0; k < j; k++) ai[k] = 0;
	 *
	 * for (int l = 1; l < 103; l++) { int i1 = 24628 + (103 - l) * 512 * 4; for
	 * (int k1 = 1; k1 < 103; k1++) { if ((byteGroundArray[i][k1][l] & 0x18) == 0)
	 * worldController.method309(ai, i1, i, k1, l); if (i < 3 && (byteGroundArray[i
	 * + 1][k1][l] & 8) != 0) worldController.method309(ai, i1, i + 1, k1, l); i1 +=
	 * 4; }
	 *
	 * }
	 *
	 * int j1 = ((238 + (int) (Math.random() * 20D)) - 10 << 16) + ((238 + (int)
	 * (Math.random() * 20D)) - 10 << 8) + ((238 + (int) (Math.random() * 20D)) -
	 * 10); int l1 = (238 + (int) (Math.random() * 20D)) - 10 << 16;
	 * minimapImage.method343(); for (int i2 = 1; i2 < 103; i2++) { for (int j2 = 1;
	 * j2 < 103; j2++) { if ((byteGroundArray[i][j2][i2] & 0x18) == 0) method50(i2,
	 * j1, j2, l1, i); if (i < 3 && (byteGroundArray[i + 1][j2][i2] & 8) != 0)
	 * method50(i2, j1, j2, l1, i + 1); }
	 *
	 * }
	 *
	 * aRSImageProducer_1165.initDrawingArea(); anInt1071 = 0; for (int k2 = 0; k2 <
	 * 104; k2++) { for (int l2 = 0; l2 < 104; l2++) { int i3 =
	 * worldController.method303(plane, k2, l2); if (i3 != 0) { i3 = i3 >> 14 &
	 * 0x7fff; int j3 = ObjectDef.forID(i3).anInt746; if (j3 >= 0) { int k3 = k2;
	 * int l3 = l2; if (j3 != 22 && j3 != 29 && j3 != 34 && j3 != 36 && j3 != 46 &&
	 * j3 != 47 && j3 != 48) { byte byte0 = 104; byte byte1 = 104; int ai1[][] =
	 * aClass11Array1230[plane].anIntArrayArray294; for (int i4 = 0; i4 < 10; i4++)
	 * { int j4 = (int) (Math.random() * 4D); if (j4 == 0 && k3 > 0 && k3 > k2 - 3
	 * && (ai1[k3 - 1][l3] & 0x1280108) == 0) k3--; if (j4 == 1 && k3 < byte0 - 1 &&
	 * k3 < k2 + 3 && (ai1[k3 + 1][l3] & 0x1280180) == 0) k3++; if (j4 == 2 && l3 >
	 * 0 && l3 > l2 - 3 && (ai1[k3][l3 - 1] & 0x1280102) == 0) l3--; if (j4 == 3 &&
	 * l3 < byte1 - 1 && l3 < l2 + 3 && (ai1[k3][l3 + 1] & 0x1280120) == 0) l3++; }
	 *
	 * } aClass30_Sub2_Sub1_Sub1Array1140[anInt1071] = mapFunctions[j3];
	 * anIntArray1072[anInt1071] = k3; anIntArray1073[anInt1071] = l3; anInt1071++;
	 * } } }
	 *
	 * }
	 *
	 * }
	 */

	// redraws a new minimap for current region
	private void refreshMinimap(int height) {
		boolean showIconsOnAllHeights = false;
		// System.out.println("New region, refreshMinimap called, plane " + height);
		int minimapBackgroundArray[] = minimapImage.myPixels;
		int j = minimapBackgroundArray.length;
		for (int k = 0; k < j; k++)
			minimapBackgroundArray[k] = 0;

		// does minimap terrain drawing
		for (int l = 1; l < 103; l++) {
			int i1 = 24628 + (103 - l) * 512 * 4;
			for (int k1 = 1; k1 < 103; k1++) {
				if ((tileFlags[height][k1][l] & 0x18) == 0)
					worldController.method309(minimapBackgroundArray, i1, height, k1, l);
				if (height < 3 && (tileFlags[height + 1][k1][l] & 8) != 0)
					worldController.method309(minimapBackgroundArray, i1, height + 1, k1, l);
				i1 += 4;
			}

		}

		/*
		 * for (int k = 0; k < j; k+=10000)
		 * System.out.println(Integer.toHexString(minimapBackgroundArray[k]));
		 */ // example of converting background colors to hex colors

		int j1 = 0xFFFFFF;
		int l1 = 0xEE0000;
		minimapImage.cleanMinimapDrawingArea(); // this is what this *should do* but it doesn't seem to be needed?
		for (int i2 = 1; i2 < 103; i2++) {
			for (int j2 = 1; j2 < 103; j2++) {
				if ((tileFlags[height][j2][i2] & 0x18) == 0)
					drawMinimapLines(i2, j1, j2, l1, height);
				if (height < 3 && (tileFlags[height + 1][j2][i2] & 8) != 0)
					drawMinimapLines(i2, j1, j2, l1, height + 1);
			}

		}

		gameScreenImageProducer.initDrawingArea();
		anInt1071 = 0;

		if (showIconsOnAllHeights) {
			for (int h = 0; h < 3; h++) {
				addIconsToMinimap(h);
			}
		} else {
			addIconsToMinimap(height);
		}
	}

	// adds icons to minimap
	private void addIconsToMinimap(int height) {
		for (int k2 = 0; k2 < 104; k2++) {
			for (int l2 = 0; l2 < 104; l2++) {
				int i3 = worldController.method303(height, k2, l2);
				if (i3 != 0) {
					i3 = i3 >> 14 & 0x7fff;
					ObjectDef objectDef = ObjectDef.forID(i3);
					if (objectDef == null)
						continue;
					int iconID = objectDef.minimapFunction;
					if (iconID >= 0) {
						int k3 = k2;
						int l3 = l2;
						// System.out.println("Adding icon #" + iconID + " at " + k3 + "," + l3);
						aClass30_Sub2_Sub1_Sub1Array1140[anInt1071] = mapFunctions[iconID];
						anIntArray1072[anInt1071] = k3;
						anIntArray1073[anInt1071] = l3;
						anInt1071++;
					}
				}
			}
		}
	}

	private void updateGroundItem(int i, int j) {
		NodeList class19 = groundArray[plane][i][j];
		if (class19 == null) {
			worldController.method295(plane, i, j);
			return;
		}
		int k = 0xfa0a1f01;
		Object obj = null;
		for (Item item = (Item) class19.reverseGetFirst(); item != null; item = (Item) class19.reverseGetNext()) {
			ItemDef itemDef = ItemDef.forID(item.ID);
			int l = itemDef.value;
			if (itemDef.stackable)
				l *= item.itemCount + 1;
			// notifyItemSpawn(item, i + baseX, j + baseY);

			if (l > k) {
				k = l;
				obj = item;
			}
		}

		class19.insertTail(((Node) (obj)));
		Object obj1 = null;
		Object obj2 = null;
		for (Item class30_sub2_sub4_sub2_1 = (Item) class19
				.reverseGetFirst(); class30_sub2_sub4_sub2_1 != null; class30_sub2_sub4_sub2_1 = (Item) class19
						.reverseGetNext()) {
			if (class30_sub2_sub4_sub2_1.ID != ((Item) (obj)).ID && obj1 == null)
				obj1 = class30_sub2_sub4_sub2_1;
			if (class30_sub2_sub4_sub2_1.ID != ((Item) (obj)).ID && class30_sub2_sub4_sub2_1.ID != ((Item) (obj1)).ID
					&& obj2 == null)
				obj2 = class30_sub2_sub4_sub2_1;
		}

		int i1 = i + (j << 7) + 0x60000000;
		worldController.method281(i, i1, ((Renderable) (obj1)), method42(plane, j * 128 + 64, i * 128 + 64),
				((Renderable) (obj2)), ((Renderable) (obj)), plane, j);
	}

	private void showNPCs(boolean flag) { // method 26
		for (int j = 0; j < npcCount; j++) {
			NPC npc = npcArray[npcIndices[j]];
			int k = 0x20000000 + (npcIndices[j] << 14);
			if (npc == null || !npc.isVisible() || npc.desc.priorityRender != flag)
				continue;
			int l = npc.x >> 7;
			int i1 = npc.y >> 7;
			if (l < 0 || l >= 104 || i1 < 0 || i1 >= 104)
				continue;
			if (npc.size == 1 && (npc.x & 0x7f) == 64 && (npc.y & 0x7f) == 64) {
				if (anIntArrayArray929[l][i1] == anInt1265)
					continue;
				anIntArrayArray929[l][i1] = anInt1265;
			}
			if (!npc.desc.hasActions)
				k += 0x80000000;
			worldController.method285(plane, npc.face, method42(plane, npc.y, npc.x), k, npc.y,
					(npc.size - 1) * 64 + 60, npc.x, npc, npc.aBoolean1541);
		}
	}

	public void drawHoverBox(int xPos, int yPos, String text) {
		String[] results = text.split("\n");
		int height = (results.length * 16) + 6;
		int width;
		width = smallFont.getTextWidth(results[0]) + 6;
		for (int i = 1; i < results.length; i++)
			if (width <= smallFont.getTextWidth(results[i]) + 6)
				width = smallFont.getTextWidth(results[i]) + 6;
		DrawingArea.drawPixels(height, yPos, xPos, 0xFFFFA0, width);
		DrawingArea.fillPixels(xPos, width, height, 0, yPos);
		yPos += 14;
		for (int i = 0; i < results.length; i++) {
			smallFont.method389(false, xPos + 3, 0, results[i], yPos);
			yPos += 16;
		}
	}

	private void buildInterfaceMenu(int baseX, RSInterface[] components, int mouseX, int baseY, int mouseY,
			int scrollPos, int parentID) {
		if (components == null)
			return;
		for (int childID = 0; childID < components.length; childID++) {
			RSInterface rsInterface = components[childID];
			if (rsInterface == null)
				continue;
			if (rsInterface.parentID == parentID) {
				if (rsInterface.toggledContainer) {
					if (rsInterface.type == 0 && !interfaceIsSelected(rsInterface)) {
						continue;
					}
				} else {
					if (rsInterface.isHidden) {
						continue;
					}
				}
				int childX = rsInterface.x + baseX;
				int childY = (rsInterface.y + baseY) - scrollPos;
				if ((rsInterface.mouseOverID >= 0 || rsInterface.disabledMouseOverColor != 0) && mouseX >= childX
						&& mouseY >= childY && mouseX < childX + rsInterface.width
						&& mouseY < childY + rsInterface.height) {
					if (rsInterface.mouseOverID >= 0) {
						hoverInterface = components[rsInterface.mouseOverID].interfaceHash;
					} else {
						hoverInterface = rsInterface.interfaceHash;
					}
				}
				if (rsInterface.type == 0) {
					if (mouseX >= childX && mouseY >= childY && mouseX < childX + rsInterface.width + 32
							&& mouseY < childY + rsInterface.height) {
						buildInterfaceMenu(childX, components, mouseX, childY, mouseY, rsInterface.scrollPosition,
								rsInterface.interfaceHash);
						if (rsInterface.scrollMax > rsInterface.height)
							moveScroller(childX + rsInterface.width, rsInterface.height, mouseX, mouseY, rsInterface,
									childY, true, rsInterface.scrollMax);
					} else {
						continue;
					}
				} else {
					if (rsInterface.type == 8 && mouseX >= childX && mouseY >= childY
							&& mouseX < childX + rsInterface.width && mouseY < childY + rsInterface.height) {
						anInt1315 = rsInterface.interfaceHash;
					}
					if (rsInterface.type == 4 && rsInterface.interfaceURL != null) {
						if (mouseX >= childX && mouseY >= childY && mouseX < childX + rsInterface.width
								&& mouseY < childY + rsInterface.height) {
							menuActionName[menuActionRow] = "Go-to @lre@" + rsInterface.interfaceURLDisplay
									+ (myPrivilege > 1 ? " @gre@(@whi@" + rsInterface.interfaceHash + "@gre@)" : "");
							menuActionID[menuActionRow] = 928;
							menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
							menuActionRow++;
						}
					}
					if (rsInterface.actionType == 1 && mouseX >= childX && mouseY >= childY
							&& mouseX < childX + rsInterface.width && mouseY < childY + rsInterface.height) {
						boolean flag = false;
						if (rsInterface.contentType != 0) {
							flag = buildFriendsListMenu(rsInterface) || buildClanChatMenu(rsInterface);
						}
						if (!flag) {
							menuActionName[menuActionRow] = rsInterface.tooltip
									+ (myPrivilege > 1 ? " @gre@(@whi@" + rsInterface.interfaceHash + "@gre@)" : "");
							menuActionID[menuActionRow] = 315;
							menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
							menuActionRow++;
						}
					}
					if (rsInterface.actionType == 2 && spellSelected == 0 && mouseX >= childX && mouseY >= childY
							&& mouseX < childX + rsInterface.width && mouseY < childY + rsInterface.height) {
						String s = rsInterface.selectedActionName;
						if (s.indexOf(" ") != -1)
							s = s.substring(0, s.indexOf(" "));
						menuActionName[menuActionRow] = s + " @gre@" + rsInterface.spellName
								+ (myPrivilege > 1 ? " @gre@(@whi@" + rsInterface.interfaceHash + "@gre@)" : "");
						menuActionID[menuActionRow] = 626;
						menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
						menuActionRow++;
					}
					if (rsInterface.actionType == 3 && mouseX >= childX && mouseY >= childY
							&& mouseX < childX + rsInterface.width && mouseY < childY + rsInterface.height) {
						menuActionName[menuActionRow] = "Close"
								+ (myPrivilege > 1 ? " @gre@(@whi@" + rsInterface.interfaceHash + "@gre@)" : "");
						menuActionID[menuActionRow] = 200;
						menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
						menuActionRow++;
					}
					if (rsInterface.actionType == 4 && mouseX >= childX && mouseY >= childY
							&& mouseX < childX + rsInterface.width && mouseY < childY + rsInterface.height) {
						menuActionName[menuActionRow] = rsInterface.tooltip
								+ (myPrivilege > 1 ? " @gre@(@whi@" + rsInterface.interfaceHash + "@gre@)" : "");
						menuActionID[menuActionRow] = 169;
						menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
						menuActionRow++;
					}
					if (rsInterface.actionType == 5 && mouseX >= childX && mouseY >= childY
							&& mouseX < childX + rsInterface.width && mouseY < childY + rsInterface.height) {
						menuActionName[menuActionRow] = rsInterface.tooltip
								+ (myPrivilege > 1 ? " @gre@(@whi@" + rsInterface.interfaceHash + "@gre@)" : "");
						menuActionID[menuActionRow] = 646;
						menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
						menuActionRow++;
					}
					if (rsInterface.actionType == 6 && !continueDialogue && mouseX >= childX && mouseY >= childY
							&& mouseX < childX + rsInterface.width && mouseY < childY + rsInterface.height) {
						menuActionName[menuActionRow] = rsInterface.tooltip
								+ (myPrivilege > 1 ? " @gre@(@whi@" + rsInterface.interfaceHash + "@gre@)" : "");
						menuActionID[menuActionRow] = 679;
						menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
						menuActionRow++;
					}
					if (rsInterface.type == 2) {
						int k2 = 0;
						for (int l2 = 0; l2 < rsInterface.height; l2++) {
							for (int i3 = 0; i3 < rsInterface.width; i3++) {
								int j3 = childX + i3 * (32 + rsInterface.spritePaddingX);
								int k3 = childY + l2 * (32 + rsInterface.spritePaddingY);
								if (k2 < 20) {
									j3 += rsInterface.spritesX[k2];
									k3 += rsInterface.spritesY[k2];
								}
								if (mouseX >= j3 && mouseY >= k3 && mouseX < j3 + 32 && mouseY < k3 + 32) {
									mouseInvInterfaceIndex = k2;
									lastActiveInvInterface = rsInterface.interfaceHash;
									if (rsInterface.inventoryItemIds[k2] > 0) {
										ItemDef itemDef = ItemDef.forID(rsInterface.inventoryItemIds[k2] - 1);
										if (itemSelected == 1 && rsInterface.hasActions) {
											if (rsInterface.interfaceHash != anInt1284 || k2 != anInt1283) {
												menuActionName[menuActionRow] = "Use " + selectedItemName
														+ " with @lre@" + itemDef.name;
												menuActionID[menuActionRow] = 870;
												menuActionCmd1[menuActionRow] = itemDef.id;
												menuActionCmd2[menuActionRow] = k2;
												menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
												menuActionRow++;
											}
										} else if (spellSelected == 1 && rsInterface.hasActions) {
											if ((spellUsableOn & 0x10) == 16) {
												menuActionName[menuActionRow] = spellTooltip + " @lre@" + itemDef.name;
												menuActionID[menuActionRow] = 543;
												menuActionCmd1[menuActionRow] = itemDef.id;
												menuActionCmd2[menuActionRow] = k2;
												menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
												menuActionRow++;
											}
										} else {
											if (shiftDown) {
												menuActionName[menuActionRow] = "Drop @lre@" + itemDef.name;
												menuActionID[menuActionRow] = 847;
												menuActionCmd1[menuActionRow] = itemDef.id;
												menuActionCmd2[menuActionRow] = k2;
												menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
												menuActionRow++;
												return;
											}
											if (rsInterface.hasActions) {
												for (int l3 = 4; l3 >= 3; l3--)
													if (itemDef.itemActions != null
															&& itemDef.itemActions[l3] != null) {
														menuActionName[menuActionRow] = itemDef.itemActions[l3]
																+ " @lre@" + itemDef.name;
														if (l3 == 3)
															menuActionID[menuActionRow] = 493;
														if (l3 == 4)
															menuActionID[menuActionRow] = 847;
														menuActionCmd1[menuActionRow] = itemDef.id;
														menuActionCmd2[menuActionRow] = k2;
														menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
														menuActionRow++;
													} else if (l3 == 4) {
														menuActionName[menuActionRow] = "Drop @lre@" + itemDef.name;
														menuActionID[menuActionRow] = 847;
														menuActionCmd1[menuActionRow] = itemDef.id;
														if (itemDef.id == 1971) {
															menuActionName[menuActionRow] = "Remove @lre@"
																	+ itemDef.name;
														}
														menuActionCmd2[menuActionRow] = k2;
														menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
														menuActionRow++;
													}
												if (shiftDown) {
													menuActionName[menuActionRow] = "Drop @lre@" + itemDef.name;
													menuActionID[menuActionRow] = 847;
													menuActionCmd1[menuActionRow] = itemDef.id;
													menuActionCmd2[menuActionRow] = k2;
													menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
													menuActionRow++;
													//return;
												}
											}
											if (rsInterface.usableItemInterface) {
												menuActionName[menuActionRow] = "Use @lre@" + itemDef.name;
												menuActionID[menuActionRow] = 447;
												menuActionCmd1[menuActionRow] = itemDef.id;
												menuActionCmd2[menuActionRow] = k2;
												menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
												menuActionRow++;
											}
											if (rsInterface.hasActions && itemDef.itemActions != null) {
												for (int i4 = 2; i4 >= 0; i4--)
													if (itemDef.itemActions[i4] != null) {
														menuActionName[menuActionRow] = itemDef.itemActions[i4]
																+ " @lre@" + itemDef.name;
														if (i4 == 0)
															menuActionID[menuActionRow] = 74;
														if (i4 == 1)
															menuActionID[menuActionRow] = 454;
														if (i4 == 2)
															menuActionID[menuActionRow] = 539;
														menuActionCmd1[menuActionRow] = itemDef.id;
														menuActionCmd2[menuActionRow] = k2;
														menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
														menuActionRow++;
													}

											}
											if (itemDef.equipmentActions != null) {
												if (rsInterface.interfaceID == 94 || rsInterface.interfaceID == 483) {
													for (int j4 = 4; j4 >= 0; j4--) {
														if (itemDef.equipmentActions[j4] != null) {
															menuActionName[menuActionRow] = itemDef.equipmentActions[j4]
																	+ " @lre@" + itemDef.name;
															if (j4 == 0)
																menuActionID[menuActionRow] = 700;
															if (j4 == 1)
																menuActionID[menuActionRow] = 701;
															if (j4 == 2)
																menuActionID[menuActionRow] = 702;
															if (j4 == 3)
																menuActionID[menuActionRow] = 703;
															if (j4 == 4)
																menuActionID[menuActionRow] = 704;
															menuActionCmd1[menuActionRow] = itemDef.id;
															menuActionCmd2[menuActionRow] = k2;
															menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
															menuActionRow++;
														}
													}
												}
											}
											if (rsInterface.actions != null) {
												for (int j4 = 4; j4 >= 0; j4--)
													if (rsInterface.actions[j4] != null) {
														menuActionName[menuActionRow] = rsInterface.actions[j4]
																+ " @lre@" + itemDef.name;
														if (j4 == 0)
															menuActionID[menuActionRow] = 632;
														if (j4 == 1)
															menuActionID[menuActionRow] = 78;
														if (j4 == 2)
															menuActionID[menuActionRow] = 867;
														if (j4 == 3)
															menuActionID[menuActionRow] = 431;
														if (j4 == 4)
															menuActionID[menuActionRow] = 53;
														menuActionCmd1[menuActionRow] = itemDef.id;
														menuActionCmd2[menuActionRow] = k2;
														menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
														menuActionRow++;
													}

											}
											menuActionName[menuActionRow] = (myPrivilege < 2
													? "Examine @lre@" + itemDef.name
													: "Examine @lre@" + itemDef.name + " @gre@(@whi@"
															+ (rsInterface.inventoryItemIds[k2] - 1) + "@gre@)");
											menuActionID[menuActionRow] = 1125;
											menuActionCmd1[menuActionRow] = itemDef.id;
											menuActionCmd2[menuActionRow] = k2;
											menuActionCmd3[menuActionRow] = rsInterface.interfaceHash;
											menuActionRow++;
										}
									}
								}
								k2++;
							}
						}
					}
				}
			}
		}
	}

	private int moveSlider(int x, int y, int mX, int mY, int steps, int curSlider, int minSlide, int maxSlide,
			boolean vertical) {
		final int blockSize = 16;
		final int sliderSize = 32 + (32 * steps);
		final int xMin = vertical ? x : x - blockSize;
		final int xMax = vertical ? x + blockSize : (x + sliderSize + blockSize);
		final int yMin = vertical ? y - blockSize : y;
		final int yMax = vertical ? (y + sliderSize + blockSize) : y + blockSize;
		if (mX >= xMin && mX < xMax && mY >= yMin && mY < yMax && anInt1213 > 0) {
			final double range = Math.abs(maxSlide - minSlide);
			final double stepIncrement = Math.abs(sliderSize / range);
			final int mouseOffset = (vertical ? (mY - y) : (mX - x)) + (int) (stepIncrement / 2.0D);
			curSlider = (int) (mouseOffset / stepIncrement) + minSlide;
		}
		if (curSlider < minSlide) {
			curSlider = minSlide;
		}
		if (curSlider > maxSlide) {
			curSlider = maxSlide;
		}
		return curSlider;
	}

	private void drawSlider(int x, int y, int steps, int curSlider, int minSlide, int maxSlide, boolean vertical) {
		Sprite sliderSprite = SpriteLoader.getSprite("sliders", 18);
		Sprite bgStart = SpriteLoader.getSprite("sliders", vertical ? 19 : 0);
		Sprite bgMid = SpriteLoader.getSprite("sliders", vertical ? 20 : 1);
		Sprite bgEnd = SpriteLoader.getSprite("sliders", vertical ? 22 : 3);
		int bgX = vertical ? x : x - 16;
		int bgY = vertical ? y - 16 : y;
		bgStart.drawSprite(bgX, bgY);
		if (vertical) {
			bgY += 32;
		} else {
			bgX += 32;
		}
		for (int i = 0; i < steps; i++) {
			bgMid.drawSprite(bgX, bgY);
			if (vertical) {
				bgY += 32;
			} else {
				bgX += 32;
			}
		}
		bgEnd.drawSprite(bgX, bgY);
		final int sliderSize = 32 + (32 * steps);
		final double range = Math.abs(maxSlide - minSlide);
		final double stepIncrement = Math.abs(sliderSize / range);
		int sliderPos = (int) ((curSlider - minSlide) * stepIncrement);
		if (sliderPos < 0) {
			sliderPos = 0;
		}
		if (sliderPos > sliderSize) {
			sliderPos = sliderSize;
		}
		sliderPos -= (vertical ? sliderSprite.myHeight : sliderSprite.myWidth) / 2;
		int drawX = vertical ? x : x + sliderPos;
		int drawY = vertical ? y + sliderPos : y;
		sliderSprite.drawSprite(drawX, drawY);
	}

	public void drawScrollbar(int j, int k, int l, int i1, int j1) {
		if (gameFrame.useOldScrollBar()) {
			final int anInt902 = 0x766654;
			final int anInt927 = 0x332d25;
			scrollBar1_classic.drawBackground(i1, l);
			scrollBar2_classic.drawBackground(i1, (l + j) - 16);
			DrawingArea.method336(j - 32, l + 16, i1, 0x23201b, 16);
			int k1 = ((j - 32) * j) / j1;
			if (k1 < 8)
				k1 = 8;
			int l1 = ((j - 32 - k1) * k) / (j1 - j);
			DrawingArea.method336(k1, l + 16 + l1, i1, 0x4d4233, 16);
			DrawingArea.method341(l + 16 + l1, anInt902, k1, i1);
			DrawingArea.method341(l + 16 + l1, anInt902, k1, i1 + 1);
			DrawingArea.method339(l + 16 + l1, anInt902, 16, i1);
			DrawingArea.method339(l + 17 + l1, anInt902, 16, i1);
			DrawingArea.method341(l + 16 + l1, anInt927, k1, i1 + 15);
			DrawingArea.method341(l + 17 + l1, anInt927, k1 - 1, i1 + 14);
			DrawingArea.method339(l + 15 + l1 + k1, anInt927, 16, i1);
			DrawingArea.method339(l + 14 + l1 + k1, anInt927, 15, i1 + 1);
		} else {
			scrollBar1.drawSprite(i1, l);
			scrollBar2.drawSprite(i1, (l + j) - 16);
			DrawingArea.drawPixels(j - 32, l + 16, i1, 0x000001, 16);
			DrawingArea.drawPixels(j - 32, l + 16, i1, 0x3d3426, 15);
			DrawingArea.drawPixels(j - 32, l + 16, i1, 0x342d21, 13);
			DrawingArea.drawPixels(j - 32, l + 16, i1, 0x2e281d, 11);
			DrawingArea.drawPixels(j - 32, l + 16, i1, 0x29241b, 10);
			DrawingArea.drawPixels(j - 32, l + 16, i1, 0x252019, 9);
			DrawingArea.drawPixels(j - 32, l + 16, i1, 0x000001, 1);
			int k1 = ((j - 32) * j) / j1;
			if (k1 < 8)
				k1 = 8;
			int l1 = ((j - 32 - k1) * k) / (j1 - j);
			DrawingArea.drawPixels(k1, l + 16 + l1, i1, barFillColor, 16);
			DrawingArea.method341(l + 16 + l1, 0x000001, k1, i1);
			DrawingArea.method341(l + 16 + l1, 0x817051, k1, i1 + 1);
			DrawingArea.method341(l + 16 + l1, 0x73654a, k1, i1 + 2);
			DrawingArea.method341(l + 16 + l1, 0x6a5c43, k1, i1 + 3);
			DrawingArea.method341(l + 16 + l1, 0x6a5c43, k1, i1 + 4);
			DrawingArea.method341(l + 16 + l1, 0x655841, k1, i1 + 5);
			DrawingArea.method341(l + 16 + l1, 0x655841, k1, i1 + 6);
			DrawingArea.method341(l + 16 + l1, 0x61553e, k1, i1 + 7);
			DrawingArea.method341(l + 16 + l1, 0x61553e, k1, i1 + 8);
			DrawingArea.method341(l + 16 + l1, 0x5d513c, k1, i1 + 9);
			DrawingArea.method341(l + 16 + l1, 0x5d513c, k1, i1 + 10);
			DrawingArea.method341(l + 16 + l1, 0x594e3a, k1, i1 + 11);
			DrawingArea.method341(l + 16 + l1, 0x594e3a, k1, i1 + 12);
			DrawingArea.method341(l + 16 + l1, 0x514635, k1, i1 + 13);
			DrawingArea.method341(l + 16 + l1, 0x4b4131, k1, i1 + 14);
			DrawingArea.method339(l + 16 + l1, 0x000001, 15, i1);
			DrawingArea.method339(l + 17 + l1, 0x000001, 15, i1);
			DrawingArea.method339(l + 17 + l1, 0x655841, 14, i1);
			DrawingArea.method339(l + 17 + l1, 0x6a5c43, 13, i1);
			DrawingArea.method339(l + 17 + l1, 0x6d5f48, 11, i1);
			DrawingArea.method339(l + 17 + l1, 0x73654a, 10, i1);
			DrawingArea.method339(l + 17 + l1, 0x76684b, 7, i1);
			DrawingArea.method339(l + 17 + l1, 0x7b6a4d, 5, i1);
			DrawingArea.method339(l + 17 + l1, 0x7e6e50, 4, i1);
			DrawingArea.method339(l + 17 + l1, 0x817051, 3, i1);
			DrawingArea.method339(l + 17 + l1, 0x000001, 2, i1);
			DrawingArea.method339(l + 18 + l1, 0x000001, 16, i1);
			DrawingArea.method339(l + 18 + l1, 0x564b38, 15, i1);
			DrawingArea.method339(l + 18 + l1, 0x5d513c, 14, i1);
			DrawingArea.method339(l + 18 + l1, 0x625640, 11, i1);
			DrawingArea.method339(l + 18 + l1, 0x655841, 10, i1);
			DrawingArea.method339(l + 18 + l1, 0x6a5c43, 7, i1);
			DrawingArea.method339(l + 18 + l1, 0x6e6046, 5, i1);
			DrawingArea.method339(l + 18 + l1, 0x716247, 4, i1);
			DrawingArea.method339(l + 18 + l1, 0x7b6a4d, 3, i1);
			DrawingArea.method339(l + 18 + l1, 0x817051, 2, i1);
			DrawingArea.method339(l + 18 + l1, 0x000001, 1, i1);
			DrawingArea.method339(l + 19 + l1, 0x000001, 16, i1);
			DrawingArea.method339(l + 19 + l1, 0x514635, 15, i1);
			DrawingArea.method339(l + 19 + l1, 0x564b38, 14, i1);
			DrawingArea.method339(l + 19 + l1, 0x5d513c, 11, i1);
			DrawingArea.method339(l + 19 + l1, 0x61553e, 9, i1);
			DrawingArea.method339(l + 19 + l1, 0x655841, 7, i1);
			DrawingArea.method339(l + 19 + l1, 0x6a5c43, 5, i1);
			DrawingArea.method339(l + 19 + l1, 0x6e6046, 4, i1);
			DrawingArea.method339(l + 19 + l1, 0x73654a, 3, i1);
			DrawingArea.method339(l + 19 + l1, 0x817051, 2, i1);
			DrawingArea.method339(l + 19 + l1, 0x000001, 1, i1);
			DrawingArea.method339(l + 20 + l1, 0x000001, 16, i1);
			DrawingArea.method339(l + 20 + l1, 0x4b4131, 15, i1);
			DrawingArea.method339(l + 20 + l1, 0x544936, 14, i1);
			DrawingArea.method339(l + 20 + l1, 0x594e3a, 13, i1);
			DrawingArea.method339(l + 20 + l1, 0x5d513c, 10, i1);
			DrawingArea.method339(l + 20 + l1, 0x61553e, 8, i1);
			DrawingArea.method339(l + 20 + l1, 0x655841, 6, i1);
			DrawingArea.method339(l + 20 + l1, 0x6a5c43, 4, i1);
			DrawingArea.method339(l + 20 + l1, 0x73654a, 3, i1);
			DrawingArea.method339(l + 20 + l1, 0x817051, 2, i1);
			DrawingArea.method339(l + 20 + l1, 0x000001, 1, i1);
			DrawingArea.method341(l + 16 + l1, 0x000001, k1, i1 + 15);
			DrawingArea.method339(l + 15 + l1 + k1, 0x000001, 16, i1);
			DrawingArea.method339(l + 14 + l1 + k1, 0x000001, 15, i1);
			DrawingArea.method339(l + 14 + l1 + k1, 0x3f372a, 14, i1);
			DrawingArea.method339(l + 14 + l1 + k1, 0x443c2d, 10, i1);
			DrawingArea.method339(l + 14 + l1 + k1, 0x483e2f, 9, i1);
			DrawingArea.method339(l + 14 + l1 + k1, 0x4a402f, 7, i1);
			DrawingArea.method339(l + 14 + l1 + k1, 0x4b4131, 4, i1);
			DrawingArea.method339(l + 14 + l1 + k1, 0x564b38, 3, i1);
			DrawingArea.method339(l + 14 + l1 + k1, 0x000001, 2, i1);
			DrawingArea.method339(l + 13 + l1 + k1, 0x000001, 16, i1);
			DrawingArea.method339(l + 13 + l1 + k1, 0x443c2d, 15, i1);
			DrawingArea.method339(l + 13 + l1 + k1, 0x4b4131, 11, i1);
			DrawingArea.method339(l + 13 + l1 + k1, 0x514635, 9, i1);
			DrawingArea.method339(l + 13 + l1 + k1, 0x544936, 7, i1);
			DrawingArea.method339(l + 13 + l1 + k1, 0x564b38, 6, i1);
			DrawingArea.method339(l + 13 + l1 + k1, 0x594e3a, 4, i1);
			DrawingArea.method339(l + 13 + l1 + k1, 0x625640, 3, i1);
			DrawingArea.method339(l + 13 + l1 + k1, 0x6a5c43, 2, i1);
			DrawingArea.method339(l + 13 + l1 + k1, 0x000001, 1, i1);
			DrawingArea.method339(l + 12 + l1 + k1, 0x000001, 16, i1);
			DrawingArea.method339(l + 12 + l1 + k1, 0x443c2d, 15, i1);
			DrawingArea.method339(l + 12 + l1 + k1, 0x4b4131, 14, i1);
			DrawingArea.method339(l + 12 + l1 + k1, 0x544936, 12, i1);
			DrawingArea.method339(l + 12 + l1 + k1, 0x564b38, 11, i1);
			DrawingArea.method339(l + 12 + l1 + k1, 0x594e3a, 10, i1);
			DrawingArea.method339(l + 12 + l1 + k1, 0x5d513c, 7, i1);
			DrawingArea.method339(l + 12 + l1 + k1, 0x61553e, 4, i1);
			DrawingArea.method339(l + 12 + l1 + k1, 0x6e6046, 3, i1);
			DrawingArea.method339(l + 12 + l1 + k1, 0x7b6a4d, 2, i1);
			DrawingArea.method339(l + 12 + l1 + k1, 0x000001, 1, i1);
			DrawingArea.method339(l + 11 + l1 + k1, 0x000001, 16, i1);
			DrawingArea.method339(l + 11 + l1 + k1, 0x4b4131, 15, i1);
			DrawingArea.method339(l + 11 + l1 + k1, 0x514635, 14, i1);
			DrawingArea.method339(l + 11 + l1 + k1, 0x564b38, 13, i1);
			DrawingArea.method339(l + 11 + l1 + k1, 0x594e3a, 11, i1);
			DrawingArea.method339(l + 11 + l1 + k1, 0x5d513c, 9, i1);
			DrawingArea.method339(l + 11 + l1 + k1, 0x61553e, 7, i1);
			DrawingArea.method339(l + 11 + l1 + k1, 0x655841, 5, i1);
			DrawingArea.method339(l + 11 + l1 + k1, 0x6a5c43, 4, i1);
			DrawingArea.method339(l + 11 + l1 + k1, 0x73654a, 3, i1);
			DrawingArea.method339(l + 11 + l1 + k1, 0x7b6a4d, 2, i1);
			DrawingArea.method339(l + 11 + l1 + k1, 0x000001, 1, i1);
		}
	}

	private void updateNPCs(Stream stream, int i) {
		anInt839 = 0;
		npcsAwaitingUpdateCount = 0;
		moveNpcs(stream);
		addNewNpcs(i, stream);
		method86(stream);
		for (int k = 0; k < anInt839; k++) {
			int l = anIntArray840[k];
			if (npcArray[l].anInt1537 != loopCycle) {
				npcArray[l].desc = null;
				npcArray[l] = null;
			}
		}

		if (stream.currentOffset != i) {
			Signlink.reportError(
					myUsername + " size mismatch in getnpcpos - pos:" + stream.currentOffset + " psize:" + i);
			throw new RuntimeException("eek");
		}
		for (int i1 = 0; i1 < npcCount; i1++)
			if (npcArray[npcIndices[i1]] == null) {
				Signlink.reportError(myUsername + " null entry in npc list - pos:" + i1 + " size:" + npcCount);
				throw new RuntimeException("eek");
			}

	}

	public void setChatChannel(final int chatIndex, final int chatView) {
		if (chatTypeIndex == chatIndex) {
			showChatComponents = frameMode != ScreenMode.FIXED ? !showChatComponents : true;
		} else {
			chatTypeIndex = chatIndex;
			chatTypeView = chatView;
			redrawChatbox = true;
			showChatComponents = true;
		}
	}

	private void doConfigAction(int configId) {
		try {
			int action = Varp.cache[configId].anInt709;
			int configValue = variousSettings[configId];
			if (action == 0) {
				switch (configId) {
				case 19: // loop music
					loopMusic = configValue > 0;
					if (midiPlayer != null && songChanging && !(temporarySongDelay > 0)) {
						midiPlayer.setLooping(loopMusic);
						if (!midiPlayer.playing()) {
							nextSong = currentSong;
							songChanging = true;
							onDemandFetcher.request(2, nextSong);
						}
					}
					break;
				case 161:
					switch (configValue) {
					default:
						setFrameMode(ScreenMode.FIXED);
						SettingsManager.write();
						break;
					case 1:
						setFrameMode(ScreenMode.RESIZABLE);
						SettingsManager.write();
						break;
					}
					break;
				case 162: // zoom
					SettingsManager.zoomControl = (configValue == 1);
					SettingsManager.write();
					break;
				case 163: // toggle roofs
					SettingsManager.showRoofs = (configValue == 0);
					SettingsManager.write();
					break;
				case 164: // orbs
					SettingsManager.orbsEnabled = (configValue == 0);
					SettingsManager.write();
					break;
				case 165: // mouse drag camera
					SettingsManager.middleMouseCamera = (configValue == 0);
					SettingsManager.write();
					break;
				}
				return;
			}
			if (action == 1) {
				if (configValue == 1)
					chosenBrightness = 0.90000000000000002D;
				if (configValue == 2)
					chosenBrightness = 0.80000000000000004D;
				if (configValue == 3)
					chosenBrightness = 0.69999999999999996D;
				if (configValue == 4)
					chosenBrightness = 0.59999999999999998D;

				Texture.method372(chosenBrightness);
				ItemDef.mruNodes1.unlinkAll();
				redrawGame = true;
			}
			if (action == 3) {
				if (!audioMuted) {
					boolean flag1 = musicEnabled;
					if (configValue == 0) {
						if (!musicEnabled) {
							temporarySongDelay = 0;
						}
						setMidiVolume(256);
						musicEnabled = true;
					}
					if (configValue == 1) {
						setMidiVolume(192);
						musicEnabled = true;
					}
					if (configValue == 2) {
						setMidiVolume(128);
						musicEnabled = true;
					}
					if (configValue == 3) {
						setMidiVolume(64);
						musicEnabled = true;
					}
					if (configValue == 4) {
						musicEnabled = false;
					}
					if (musicEnabled != flag1 && !lowMem) {
						if (musicEnabled) {
							nextSong = currentSong;
							songChanging = true;
							onDemandFetcher.request(2, nextSong);
						} else {
							stopMidi();
						}
						temporarySongDelay = 0;
					}
				}
			}

			if (action == 4) {
				if (!audioMuted) {
					SoundPlayer.setVolume(configValue);
					if (configValue == 0) {
						soundEnabled = true;
						setWaveVolume(0);
					}
					if (configValue == 1) {
						soundEnabled = true;
						setWaveVolume(-400);
					}
					if (configValue == 2) {
						soundEnabled = true;
						setWaveVolume(-800);
					}
					if (configValue == 3) {
						soundEnabled = true;
						setWaveVolume(-1200);
					}
					if (configValue == 4) {
						soundEnabled = false;
					}
				}
			}
			if (action == 5)
				anInt1253 = configValue;
			if (action == 6)
				anInt1249 = configValue;
			if (action == 7) {
				runClicked = (configValue == 1);
			}
			if (action == 8) {
				splitPrivateChat = configValue;
				redrawChatbox = true;
			}
			if (action == 9)
				anInt913 = configValue;
		} catch (Exception e) {
		}
	}

	public void updateEntities() {
		try {
			int anInt974 = 0;
			for (int j = -1; j < playerCount + npcCount; j++) {
				Object obj;
				if (j == -1)
					obj = myPlayer;
				else if (j < playerCount)
					obj = playerArray[playerIndices[j]];
				else
					obj = npcArray[npcIndices[j - playerCount]];
				if (obj == null || !((Entity) (obj)).isVisible())
					continue;
				if (obj instanceof NPC) {
					EntityDef entityDef = ((NPC) obj).desc;
					if (entityDef.childrenIDs != null)
						entityDef = entityDef.method161();
					if (entityDef == null)
						continue;
				}
				if (j < playerCount) {
					int l = 30;
					Player player = (Player) obj;
					if (player.headIcon >= 0) {
						npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
						if (spriteDrawX > -1) {
							if (player.skullIcon < 2) {
								skullIcons[player.skullIcon].drawSprite(spriteDrawX - 12, spriteDrawY - l);
								l += 25;
							}
							if (player.headIcon < 7) {
								headIcons[player.headIcon].drawSprite(spriteDrawX - 12, spriteDrawY - l);
								l += 18;
							}
						}
					}
					if (j >= 0 && anInt855 == 10 && anInt933 == playerIndices[j]) {
						npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
						if (spriteDrawX > -1)
							headIconsHint[player.hintIcon].drawSprite(spriteDrawX - 12, spriteDrawY - l);
					}
				} else {
					EntityDef entityDef_1 = ((NPC) obj).desc;
					if (entityDef_1.headIcon >= 0 && entityDef_1.headIcon < headIcons.length) {
						npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
						if (spriteDrawX > -1)
							headIcons[entityDef_1.headIcon].drawSprite(spriteDrawX - 12, spriteDrawY - 30);
					}
					if (anInt855 == 1 && anInt1222 == npcIndices[j - playerCount] && loopCycle % 20 < 10) {
						npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
						if (spriteDrawX > -1)
							headIconsHint[0].drawSprite(spriteDrawX - 12, spriteDrawY - 28);
					}
				}
				if (((Entity) (obj)).textSpoken != null && (j >= playerCount || publicChatMode == 0
						|| publicChatMode == 3 || publicChatMode == 1 && isFriendOrSelf(((Player) obj).name))) {
					npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height);
					if (spriteDrawX > -1 && anInt974 < anInt975) {
						anIntArray979[anInt974] = boldFontS.method384(((Entity) (obj)).textSpoken) / 2;
						anIntArray978[anInt974] = boldFontS.anInt1497;
						anIntArray976[anInt974] = spriteDrawX;
						anIntArray977[anInt974] = spriteDrawY;
						anIntArray980[anInt974] = ((Entity) (obj)).textColour;
						anIntArray981[anInt974] = ((Entity) (obj)).textEffect;
						anIntArray982[anInt974] = ((Entity) (obj)).textCycle;
						aStringArray983[anInt974++] = ((Entity) (obj)).textSpoken;
						if (anInt1249 == 0 && ((Entity) (obj)).textEffect >= 1 && ((Entity) (obj)).textEffect <= 3) {
							anIntArray978[anInt974] += 10;
							anIntArray977[anInt974] += 5;
						}
						if (anInt1249 == 0 && ((Entity) (obj)).textEffect == 4)
							anIntArray979[anInt974] = 60;
						if (anInt1249 == 0 && ((Entity) (obj)).textEffect == 5)
							anIntArray978[anInt974] += 5;
					}
				}
				if (myPlayer.interactingEntity != -1 && obj instanceof NPC) {
					NPC npc = npcArray[myPlayer.interactingEntity];
					if ((npc.interactingEntity - 32768) == localPlayerIndex) {
						HPView.draw(npc);
					}
				}
				if (((Entity) (obj)).loopCycleStatus > loopCycle) {
					try {
						npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
						if (spriteDrawX > -1) {
							int i1 = (((Entity) (obj)).currentHealth * 30) / ((Entity) (obj)).maxHealth;
							if (i1 > 30)
								i1 = 30;
							DrawingArea.drawPixels(5, spriteDrawY - 3, spriteDrawX - 15, 65280, i1);
							DrawingArea.drawPixels(5, spriteDrawY - 3, (spriteDrawX - 15) + i1, 0xff0000, 30 - i1);
						}
					} catch (Exception e) {
					}
				}
				for (int j1 = 0; j1 < 4; j1++)
					if (((Entity) (obj)).hitsLoopCycle[j1] > loopCycle) {
						npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height / 2);
						if (spriteDrawX > -1) {
							if (j1 == 1)
								spriteDrawY -= 20;
							if (j1 == 2) {
								spriteDrawX -= 15;
								spriteDrawY -= 10;
							}
							if (j1 == 3) {
								spriteDrawX += 15;
								spriteDrawY -= 10;
							}
							hitMarks[((Entity) (obj)).hitMarkTypes[j1]].drawSprite(spriteDrawX - 12, spriteDrawY - 12);
							smallFont.drawText(0, String.valueOf(((Entity) (obj)).hitArray[j1]), spriteDrawY + 4,
									spriteDrawX);
							smallFont.drawText(0xffffff, String.valueOf(((Entity) (obj)).hitArray[j1]), spriteDrawY + 3,
									spriteDrawX - 1);
						}
					}
			}
			for (int k = 0; k < anInt974; k++) {
				int k1 = anIntArray976[k];
				int l1 = anIntArray977[k];
				int j2 = anIntArray979[k];
				int k2 = anIntArray978[k];
				boolean flag = true;
				while (flag) {
					flag = false;
					for (int l2 = 0; l2 < k; l2++)
						if (l1 + 2 > anIntArray977[l2] - anIntArray978[l2] && l1 - k2 < anIntArray977[l2] + 2
								&& k1 - j2 < anIntArray976[l2] + anIntArray979[l2]
								&& k1 + j2 > anIntArray976[l2] - anIntArray979[l2]
								&& anIntArray977[l2] - anIntArray978[l2] < l1) {
							l1 = anIntArray977[l2] - anIntArray978[l2];
							flag = true;
						}

				}
				spriteDrawX = anIntArray976[k];
				spriteDrawY = anIntArray977[k] = l1;
				String s = aStringArray983[k];
				if (anInt1249 == 0) {
					int i3 = 0xffff00;
					if (anIntArray980[k] < 6)
						i3 = anIntArray965[anIntArray980[k]];
					if (anIntArray980[k] == 6)
						i3 = anInt1265 % 20 >= 10 ? 0xffff00 : 0xff0000;
					if (anIntArray980[k] == 7)
						i3 = anInt1265 % 20 >= 10 ? 65535 : 255;
					if (anIntArray980[k] == 8)
						i3 = anInt1265 % 20 >= 10 ? 0x80ff80 : 45056;
					if (anIntArray980[k] == 9) {
						int j3 = 150 - anIntArray982[k];
						if (j3 < 50)
							i3 = 0xff0000 + 1280 * j3;
						else if (j3 < 100)
							i3 = 0xffff00 - 0x50000 * (j3 - 50);
						else if (j3 < 150)
							i3 = 65280 + 5 * (j3 - 100);
					}
					if (anIntArray980[k] == 10) {
						int k3 = 150 - anIntArray982[k];
						if (k3 < 50)
							i3 = 0xff0000 + 5 * k3;
						else if (k3 < 100)
							i3 = 0xff00ff - 0x50000 * (k3 - 50);
						else if (k3 < 150)
							i3 = (255 + 0x50000 * (k3 - 100)) - 5 * (k3 - 100);
					}
					if (anIntArray980[k] == 11) {
						int l3 = 150 - anIntArray982[k];
						if (l3 < 50)
							i3 = 0xffffff - 0x50005 * l3;
						else if (l3 < 100)
							i3 = 65280 + 0x50005 * (l3 - 50);
						else if (l3 < 150)
							i3 = 0xffffff - 0x50000 * (l3 - 100);
					}
					if (anIntArray981[k] == 0) {
						boldFontS.drawText(0, s, spriteDrawY + 1, spriteDrawX);
						boldFontS.drawText(i3, s, spriteDrawY, spriteDrawX);
					}
					if (anIntArray981[k] == 1) {
						boldFontS.method386(0, s, spriteDrawX, anInt1265, spriteDrawY + 1);
						boldFontS.method386(i3, s, spriteDrawX, anInt1265, spriteDrawY);
					}
					if (anIntArray981[k] == 2) {
						boldFontS.method387(spriteDrawX, s, anInt1265, spriteDrawY + 1, 0);
						boldFontS.method387(spriteDrawX, s, anInt1265, spriteDrawY, i3);
					}
					if (anIntArray981[k] == 3) {
						boldFontS.method388(150 - anIntArray982[k], s, anInt1265, spriteDrawY + 1, spriteDrawX, 0);
						boldFontS.method388(150 - anIntArray982[k], s, anInt1265, spriteDrawY, spriteDrawX, i3);
					}
					if (anIntArray981[k] == 4) {
						int i4 = boldFontS.method384(s);
						int k4 = ((150 - anIntArray982[k]) * (i4 + 100)) / 150;
						DrawingArea.setDrawingArea(334, spriteDrawX - 50, spriteDrawX + 50, 0);
						boldFontS.method385(0, s, spriteDrawY + 1, (spriteDrawX + 50) - k4);
						boldFontS.method385(i3, s, spriteDrawY, (spriteDrawX + 50) - k4);
						DrawingArea.defaultDrawingAreaSize();
					}
					if (anIntArray981[k] == 5) {
						int j4 = 150 - anIntArray982[k];
						int l4 = 0;
						if (j4 < 25)
							l4 = j4 - 25;
						else if (j4 > 125)
							l4 = j4 - 125;
						DrawingArea.setDrawingArea(spriteDrawY + 5, 0, 512, spriteDrawY - boldFontS.anInt1497 - 1);
						boldFontS.drawText(0, s, spriteDrawY + 1 + l4, spriteDrawX);
						boldFontS.drawText(i3, s, spriteDrawY + l4, spriteDrawX);
						DrawingArea.defaultDrawingAreaSize();
					}
				} else {
					boldFontS.drawText(0, s, spriteDrawY + 1, spriteDrawX);
					boldFontS.drawText(0xffff00, s, spriteDrawY, spriteDrawX);
				}
			}
		} catch (Exception e) {
		}
	}

	private void delFriend(long l) {
		try {
			if (l == 0L)
				return;
			for (int i = 0; i < friendsCount; i++) {
				if (friendsListAsLongs[i] != l)
					continue;
				friendsCount--;
				redrawTab = true;
				for (int j = i; j < friendsCount; j++) {
					friendsList[j] = friendsList[j + 1];
					friendsNodeIDs[j] = friendsNodeIDs[j + 1];
					friendsListAsLongs[j] = friendsListAsLongs[j + 1];
				}

				stream.createFrame(215);
				stream.writeQWord(l);
				break;
			}
		} catch (RuntimeException runtimeexception) {
			Signlink.reportError("18622, " + false + ", " + l + ", " + runtimeexception.toString());
			throw new RuntimeException();
		}
	}

	public boolean tabStateCheck() {
		return invOverlayInterfaceID != -1;
	}

	private void drawTabArea() {
		if (frameMode == ScreenMode.FIXED) {
			tabImageProducer.initDrawingArea();
		}
		Texture.anIntArray1472 = anIntArray1181;

		if (tabStateCheck()) {
			showTabComponents = true;
		}

		gameFrame.drawTabArea(this);

		if (menuOpen) {
			drawMenu(frameMode == ScreenMode.FIXED ? 516 : 0, frameMode == ScreenMode.FIXED ? 168 : 0);
		}
		if (frameMode == ScreenMode.FIXED) {
			tabImageProducer.drawGraphics(168, super.graphics, 516);
		}
		gameScreenImageProducer.initDrawingArea();
		Texture.anIntArray1472 = anIntArray1182;
	}

	private void writeBackgroundTexture(int j) { // method37
		if (!lowMem) {
			if (Texture.anIntArray1480[17] >= j) {
				Background background = Texture.aBackgroundArray1474s[17];
				int k = background.myWidth * background.myHeight - 1;
				int j1 = background.myWidth * anInt945 * 2;
				byte abyte0[] = background.myPixels;
				byte abyte3[] = aByteArray912;
				for (int i2 = 0; i2 <= k; i2++)
					abyte3[i2] = abyte0[i2 - j1 & k];

				background.myPixels = abyte3;
				aByteArray912 = abyte0;
				Texture.method370(17);
				anInt854++;
				if (anInt854 > 1235) {
					anInt854 = 0;
					stream.createFrame(226);
					stream.writeWordBigEndian(0);
					int l2 = stream.currentOffset;
					stream.writeShort(58722);
					stream.writeWordBigEndian(240);
					stream.writeShort((int) (Math.random() * 65536D));
					stream.writeWordBigEndian((int) (Math.random() * 256D));
					if ((int) (Math.random() * 2D) == 0)
						stream.writeShort(51825);
					stream.writeWordBigEndian((int) (Math.random() * 256D));
					stream.writeShort((int) (Math.random() * 65536D));
					stream.writeShort(7130);
					stream.writeShort((int) (Math.random() * 65536D));
					stream.writeShort(61657);
					stream.writeBytes(stream.currentOffset - l2);
				}
			}
			if (Texture.anIntArray1480[24] >= j) {
				Background background_1 = Texture.aBackgroundArray1474s[24];
				int l = background_1.myWidth * background_1.myHeight - 1;
				int k1 = background_1.myWidth * anInt945 * 2;
				byte abyte1[] = background_1.myPixels;
				byte abyte4[] = aByteArray912;
				for (int j2 = 0; j2 <= l; j2++)
					abyte4[j2] = abyte1[j2 - k1 & l];

				background_1.myPixels = abyte4;
				aByteArray912 = abyte1;
				Texture.method370(24);
			}
			if (Texture.anIntArray1480[34] >= j) {
				Background background_2 = Texture.aBackgroundArray1474s[34];
				int i1 = background_2.myWidth * background_2.myHeight - 1;
				int l1 = background_2.myWidth * anInt945 * 2;
				byte abyte2[] = background_2.myPixels;
				byte abyte5[] = aByteArray912;
				for (int k2 = 0; k2 <= i1; k2++)
					abyte5[k2] = abyte2[k2 - l1 & i1];

				background_2.myPixels = abyte5;
				aByteArray912 = abyte2;
				Texture.method370(34);
			}
			if (Texture.anIntArray1480[40] >= j)
				;
			{
				Background background_2 = Texture.aBackgroundArray1474s[40];
				int i1 = background_2.myWidth * background_2.myHeight - 1;
				int l1 = background_2.myWidth * anInt945 * 2;
				byte abyte2[] = background_2.myPixels;
				byte abyte5[] = aByteArray912;
				for (int k2 = 0; k2 <= i1; k2++)
					abyte5[k2] = abyte2[k2 - l1 & i1];

				background_2.myPixels = abyte5;
				aByteArray912 = abyte2;
				Texture.method370(40);
			}
		}
	}

	private void method38() {
		for (int i = -1; i < playerCount; i++) {
			int j;
			if (i == -1)
				j = myPlayerIndex;
			else
				j = playerIndices[i];
			Player player = playerArray[j];
			if (player != null && player.textCycle > 0) {
				player.textCycle--;
				if (player.textCycle == 0)
					player.textSpoken = null;
			}
		}
		for (int k = 0; k < npcCount; k++) {
			int l = npcIndices[k];
			NPC npc = npcArray[l];
			if (npc != null && npc.textCycle > 0) {
				npc.textCycle--;
				if (npc.textCycle == 0)
					npc.textSpoken = null;
			}
		}
	}

	private void calcCameraPos() {
		int i = anInt1098 * 128 + 64;
		int j = anInt1099 * 128 + 64;
		int k = method42(plane, j, i) - anInt1100;
		if (xCameraPos < i) {
			xCameraPos += anInt1101 + ((i - xCameraPos) * anInt1102) / 1000;
			if (xCameraPos > i)
				xCameraPos = i;
		}
		if (xCameraPos > i) {
			xCameraPos -= anInt1101 + ((xCameraPos - i) * anInt1102) / 1000;
			if (xCameraPos < i)
				xCameraPos = i;
		}
		if (zCameraPos < k) {
			zCameraPos += anInt1101 + ((k - zCameraPos) * anInt1102) / 1000;
			if (zCameraPos > k)
				zCameraPos = k;
		}
		if (zCameraPos > k) {
			zCameraPos -= anInt1101 + ((zCameraPos - k) * anInt1102) / 1000;
			if (zCameraPos < k)
				zCameraPos = k;
		}
		if (yCameraPos < j) {
			yCameraPos += anInt1101 + ((j - yCameraPos) * anInt1102) / 1000;
			if (yCameraPos > j)
				yCameraPos = j;
		}
		if (yCameraPos > j) {
			yCameraPos -= anInt1101 + ((yCameraPos - j) * anInt1102) / 1000;
			if (yCameraPos < j)
				yCameraPos = j;
		}
		i = anInt995 * 128 + 64;
		j = anInt996 * 128 + 64;
		k = method42(plane, j, i) - anInt997;
		int l = i - xCameraPos;
		int i1 = k - zCameraPos;
		int j1 = j - yCameraPos;
		int k1 = (int) Math.sqrt(l * l + j1 * j1);
		int l1 = (int) (Math.atan2(i1, k1) * 325.94900000000001D) & 0x7ff;
		int i2 = (int) (Math.atan2(l, j1) * -325.94900000000001D) & 0x7ff;
		if (l1 < 128)
			l1 = 128;
		if (l1 > 383)
			l1 = 383;
		if (yCameraCurve < l1) {
			yCameraCurve += anInt998 + ((l1 - yCameraCurve) * anInt999) / 1000;
			if (yCameraCurve > l1)
				yCameraCurve = l1;
		}
		if (yCameraCurve > l1) {
			yCameraCurve -= anInt998 + ((yCameraCurve - l1) * anInt999) / 1000;
			if (yCameraCurve < l1)
				yCameraCurve = l1;
		}
		int j2 = i2 - xCameraCurve;
		if (j2 > 1024)
			j2 -= 2048;
		if (j2 < -1024)
			j2 += 2048;
		if (j2 > 0) {
			xCameraCurve += anInt998 + (j2 * anInt999) / 1000;
			xCameraCurve &= 0x7ff;
		}
		if (j2 < 0) {
			xCameraCurve -= anInt998 + (-j2 * anInt999) / 1000;
			xCameraCurve &= 0x7ff;
		}
		int k2 = i2 - xCameraCurve;
		if (k2 > 1024)
			k2 -= 2048;
		if (k2 < -1024)
			k2 += 2048;
		if (k2 < 0 && j2 > 0 || k2 > 0 && j2 < 0)
			xCameraCurve = i2;
	}

	public void drawMenu(int x, int y) {
		int xPos = menuOffsetX - (x - 4);
		int yPos = (-y + 4) + menuOffsetY;
		int w = menuWidth;
		int h = menuHeight + 1;
		redrawChatbox = true;
		redrawTabIcons = true;
		redrawTab = true;
		int menuColor = 0x5d5447;
		DrawingArea.drawPixels(h, yPos, xPos, menuColor, w);
		DrawingArea.drawPixels(16, yPos + 1, xPos + 1, 0, w - 2);
		DrawingArea.fillPixels(xPos + 1, w - 2, h - 19, 0, yPos + 18);
		boldFontS.method385(menuColor, "Choose Option", yPos + 14, xPos + 3);
		int mouseX = super.mouseX - (x);
		int mouseY = (-y) + super.mouseY;
		for (int i = 0; i < menuActionRow; i++) {
			int textY = yPos + 31 + (menuActionRow - 1 - i) * 15;
			int textColor = 0xffffff;
			if (mouseX > xPos && mouseX < xPos + w && mouseY > textY - 13 && mouseY < textY + 3) {
				DrawingArea.drawPixels(15, textY - 11, xPos + 3, 0x6f695d, menuWidth - 6);
				textColor = 0xffff00;
			}
			// chatTextDrawingArea.method389(true, xPos + 3, textColor, menuActionName[i],
			// textY);
			newBoldFont.drawBasicString(legacyColorConvert(menuActionName[i]), xPos + 3, textY, textColor, 0);
		}
	}

	// TODO: MOVE TO RSFONT AND HANDLE MULTI COLOR CONVERSION
	public String legacyColorConvert(String string) {
		if (!string.contains("@")) {
			return string;
		}
		String newString = string;
		for (int k1 = 0; k1 < string.length(); k1++) {
			if (string.charAt(k1) == '@' && k1 + 4 < string.length() && string.charAt(k1 + 4) == '@') {
				String subString = string.substring(k1 + 1, k1 + 4);
				String color = RSFont.getColorByName(subString); // just going to do this for now
				if (!color.equals("")) {
					newString = newString.replaceAll(string.substring(k1, k1 + 5), "<col=" + color + ">");
				} else {
					if (subString.equals("str")) {
						newString = newString.replaceAll(string.substring(k1, k1 + 5), "<str>");
					}
				}
			}
		}
		return newString;
	}

	private void addFriend(long l) {
		try {
			if (l == 0L)
				return;
//			if (friendsCount >= 100 && anInt1046 != 1) {
//				pushMessage("Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "", true);
//				return;
//			}
			if (friendsCount >= 200) {
				pushMessage("Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "", true);
				return;
			}
			String s = TextUtil.fixName(TextUtil.nameForLong(l));
			for (int i = 0; i < friendsCount; i++)
				if (friendsListAsLongs[i] == l) {
					pushMessage(s + " is already on your friend list", 0, "", true);
					return;
				}
			for (int j = 0; j < ignoreCount; j++)
				if (ignoreListAsLongs[j] == l) {
					pushMessage("Please remove " + s + " from your ignore list first", 0, "", true);
					return;
				}

			if (s.equals(myPlayer.name)) {
				return;
			} else {
				friendsList[friendsCount] = s;
				friendsListAsLongs[friendsCount] = l;
				friendsNodeIDs[friendsCount] = 0;
				friendsCount++;
				redrawTab = true;
				stream.createFrame(188);
				stream.writeQWord(l);
				return;
			}
		} catch (RuntimeException runtimeexception) {
			Signlink.reportError("15283, " + (byte) 68 + ", " + l + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	private int method42(int i, int j, int k) {
		int l = k >> 7;
		int i1 = j >> 7;
		if (l < 0 || i1 < 0 || l > 103 || i1 > 103)
			return 0;
		int j1 = i;
		if (j1 < 3 && (tileFlags[1][l][i1] & 2) == 2)
			j1++;
		int k1 = k & 0x7f;
		int l1 = j & 0x7f;
		int i2 = tileHeights[j1][l][i1] * (128 - k1) + tileHeights[j1][l + 1][i1] * k1 >> 7;
		int j2 = tileHeights[j1][l][i1 + 1] * (128 - k1) + tileHeights[j1][l + 1][i1 + 1] * k1 >> 7;
		return i2 * (128 - l1) + j2 * l1 >> 7;
	}

	private static String intToKOrMil(int j) {
		if (j < 0x186a0)
			return String.valueOf(j);
		if (j < 0x989680)
			return j / 1000 + "K";
		else
			return j / 0xf4240 + "M";
	}

	private void resetLogout() {
		try {
			if (socketStream != null)
				socketStream.close();
		} catch (Exception _ex) {
		}
		SettingsManager.write();
		XPDrop.reset();
		resetFade();
		setTint(0, 0);
		socketStream = null;
		loggedIn = false;
		loginScreenState = 0;
		loginMessage1 = "";
		loginMessage2 = "";
		serverMessage = "";
		// myUsername = "";
		// myPassword = "";
		ignoreCount = 0;
		clanCount = 0;
		selectedBankTab = 0;
		bankTabsUsed = 1;
		unlinkMRUNodes();
		worldController.initToNull();
		for (int i = 0; i < 4; i++)
			collisionMaps[i].method210();
		System.gc();
		stopMidi();
		currentSong = -1;
		nextSong = 0;
		temporarySongDelay = 0;
		if (loginMusicEnabled && !audioMuted) {
			musicVolume = 256;
			songChanging = true;
			onDemandFetcher.request(2, nextSong);
		}
		setFrameMode(ScreenMode.FIXED);
		verificationCode = -1;
		verificationCodeS = "";
	}

	private void changeCharacterGender() {
		aBoolean1031 = true;
		for (int j = 0; j < 7; j++) {
			anIntArray1065[j] = -1;
			for (int k = 0; k < IdentityKit.length; k++) {
				if (IdentityKit.cache[k].aBoolean662 || IdentityKit.cache[k].anInt657 != j + (aBoolean1047 ? 0 : 7))
					continue;
				anIntArray1065[j] = k;
				break;
			}
		}
	}

	private void addNewNpcs(int i, Stream stream) {
		while (stream.bitPosition + 21 < i * 8) {
			int k = stream.readBits(14);
			if (k == 16383)
				break;
			boolean newNpc = false;
			if (npcArray[k] == null) {
				npcArray[k] = new NPC();
				newNpc = true;
			}
			NPC npc = npcArray[k];
			npcIndices[npcCount++] = k;
			npc.anInt1537 = loopCycle;
			int l = stream.readBits(5);
			if (l > 15)
				l -= 32;
			int i1 = stream.readBits(5);
			if (i1 > 15)
				i1 -= 32;
			int j1 = stream.readBits(1);
			npc.desc = EntityDef.forID(stream.readBits(14));
			int faceBits = stream.readBits(2);
			if (faceBits != -1 && newNpc) {
				int[] turnDirections = { 1024, 1536, 0, 512 };
				npc.face = npc.turnDirection = turnDirections[faceBits];
			}
			int k1 = stream.readBits(1);
			if (k1 == 1)
				npcsAwaitingUpdate[npcsAwaitingUpdateCount++] = k;
			npc.size = npc.desc.size;
			npc.degreesToTurn = npc.desc.degreesToTurn;
			npc.walkAnim = npc.desc.walkAnim;
			npc.turn180Anim = npc.desc.turn180Anim;
			npc.turn90CWAnim = npc.desc.turn90CWAnim;
			npc.turn90CCWAnim = npc.desc.turn90CCWAnim;
			npc.standAnim = npc.desc.standAnim;
			npc.setPos(myPlayer.smallX[0] + i1, myPlayer.smallY[0] + l, j1 == 1);
		}
		stream.finishBitAccess();
	}

	@Override
	public void doLogic() {
		if (rsAlreadyLoaded || loadingError || genericLoadingError)
			return;
		loopCycle++;
		if (!loggedIn)
			processLoginScreenInput();
		else
			mainGameProcessor();
		processOnDemandQueue();
	}

	/*
	 * private void method47(boolean flag) { if (myPlayer.x >> 7 == destX &&
	 * myPlayer.y >> 7 == destY) destX = 0; int j = playerCount; if (flag) j = 1;
	 * for (int l = 0; l < j; l++) { Player player; int i1; if (flag) { player =
	 * myPlayer; i1 = myPlayerIndex << 14; } else { player =
	 * playerArray[playerIndices[l]]; i1 = playerIndices[l] << 14; } if (player ==
	 * null || !player.isVisible()) continue; player.aBoolean1699 = (lowMem &&
	 * playerCount > 50 || playerCount > 200) && !flag && player.anInt1517 ==
	 * player.anInt1511; int j1 = player.x >> 7; int k1 = player.y >> 7; if (j1 < 0
	 * || j1 >= 104 || k1 < 0 || k1 >= 104) continue; if (player.aModel_1714 != null
	 * && loopCycle >= player.anInt1707 && loopCycle < player.anInt1708) {
	 * player.aBoolean1699 = false; player.anInt1709 = method42(plane, player.y,
	 * player.x); worldController.method286(plane, player.y, player, player.face,
	 * player.anInt1722, player.x, player.anInt1709, player.anInt1719,
	 * player.anInt1721, i1, player.anInt1720); continue; } if ((player.x & 0x7f) ==
	 * 64 && (player.y & 0x7f) == 64) { if (anIntArrayArray929[j1][k1] == anInt1265)
	 * continue; anIntArrayArray929[j1][k1] = anInt1265; } player.anInt1709 =
	 * method42(plane, player.y, player.x); worldController.method285(plane,
	 * player.face, player.anInt1709, i1, player.y, 60, player.x, player,
	 * player.aBoolean1541); } }
	 */
	private void showOtherPlayers(boolean flag) // method 47
	{
		if (myPlayer.x >> 7 == destX && myPlayer.y >> 7 == destY)
			destX = 0;
		int j = playerCount;
		if (flag)
			j = 1;
		for (int l = 0; l < j; l++) {
			Player player;
			int i1;
			if (flag) {
				player = myPlayer;
				i1 = myPlayerIndex << 14;
			} else {
				player = playerArray[playerIndices[l]];
				i1 = playerIndices[l] << 14;
			}
			if (player == null || !player.isVisible())
				continue;
			player.aBoolean1699 = (lowMem && playerCount > 50 || playerCount > 200) && !flag
					&& player.anInt1517 == player.standAnim;
			int j1 = player.x >> 7;
			int k1 = player.y >> 7;
			if (j1 < 0 || j1 >= 104 || k1 < 0 || k1 >= 104)
				continue;
			if (player.aModel_1714 != null && loopCycle >= player.anInt1707 && loopCycle < player.anInt1708) {
				player.aBoolean1699 = false;
				player.anInt1709 = method42(plane, player.y, player.x);
				worldController.method286(plane, player.y, player, player.face, player.anInt1722, player.x,
						player.anInt1709, player.anInt1719, player.anInt1721, i1, player.anInt1720);
				continue;
			}
			if ((player.x & 0x7f) == 64 && (player.y & 0x7f) == 64) {
				if (anIntArrayArray929[j1][k1] == anInt1265)
					continue;
				anIntArrayArray929[j1][k1] = anInt1265;
			}
			player.anInt1709 = method42(plane, player.y, player.x);
			worldController.method285(plane, player.face, player.anInt1709, i1, player.y, 60, player.x, player,
					player.aBoolean1541);
		}

	}

	private boolean promptUserForInput(RSInterface class9) {
		int j = class9.contentType;
		if (anInt900 == 2) {
			if (j == 201) {
				redrawChatbox = true;
				inputDialogState = 0;
				messagePromptRaised = true;
				promptInput = "";
				friendsListAction = 1;
				aString1121 = "Enter name of friend to add to list";
			}
			if (j == 202) {
				redrawChatbox = true;
				inputDialogState = 0;
				messagePromptRaised = true;
				promptInput = "";
				friendsListAction = 2;
				aString1121 = "Enter name of friend to delete from list";
			}
		}
		if (j == 205) {
			anInt1011 = 250;
			return true;
		}
		if (j == 501) {
			redrawChatbox = true;
			inputDialogState = 0;
			messagePromptRaised = true;
			promptInput = "";
			friendsListAction = 4;
			aString1121 = "Enter name of player to add to list";
		}
		if (j == 502) {
			redrawChatbox = true;
			inputDialogState = 0;
			messagePromptRaised = true;
			promptInput = "";
			friendsListAction = 5;
			aString1121 = "Enter name of player to delete from list";
		}
		if (j == 550) {
			redrawChatbox = true;
			inputDialogState = 0;
			messagePromptRaised = true;
			promptInput = "";
			friendsListAction = 6;
			aString1121 = "Enter the name of the chat you wish to join";
		}
		if (j >= 300 && j <= 313) {
			int k = (j - 300) / 2;
			int j1 = j & 1;
			int i2 = anIntArray1065[k];
			if (i2 != -1) {
				do {
					if (j1 == 0 && --i2 < 0)
						i2 = IdentityKit.length - 1;
					if (j1 == 1 && ++i2 >= IdentityKit.length)
						i2 = 0;
				} while (IdentityKit.cache[i2].aBoolean662
						|| IdentityKit.cache[i2].anInt657 != k + (aBoolean1047 ? 0 : 7));
				anIntArray1065[k] = i2;
				aBoolean1031 = true;
			}
		}
		if (j >= 314 && j <= 323) {
			int l = (j - 314) / 2;
			int k1 = j & 1;
			int j2 = anIntArray990[l];
			if (k1 == 0 && --j2 < 0)
				j2 = anIntArrayArray1003[l].length - 1;
			if (k1 == 1 && ++j2 >= anIntArrayArray1003[l].length)
				j2 = 0;
			anIntArray990[l] = j2;
			aBoolean1031 = true;
		}
		if (j == 324 && !aBoolean1047) {
			aBoolean1047 = true;
			changeCharacterGender();
		}
		if (j == 325 && aBoolean1047) {
			aBoolean1047 = false;
			changeCharacterGender();
		}
		if (j == 326) {
			stream.createFrame(101);
			stream.writeWordBigEndian(aBoolean1047 ? 0 : 1);
			for (int i1 = 0; i1 < 7; i1++)
				stream.writeWordBigEndian(anIntArray1065[i1]);

			for (int l1 = 0; l1 < 5; l1++)
				stream.writeWordBigEndian(anIntArray990[l1]);

			return true;
		}
		if (j == 620) {
			canMute = !canMute;
		}
		if (j >= 601 && j <= 613) {
			clearTopInterfaces();
			if (reportAbuseInput.length() > 0) {
				stream.createFrame(218);
				stream.writeQWord(TextUtil.longForName(reportAbuseInput));
				stream.writeWordBigEndian(j - 601);
				stream.writeWordBigEndian(canMute ? 1 : 0);
			}
			reportAbuseInput = "";
			canMute = false;
		}
		if (j >= 10000 && j <= 10022) {
			return true;
		}
		return false;
	}

	private void method49(Stream stream) {
		for (int j = 0; j < npcsAwaitingUpdateCount; j++) {
			int k = npcsAwaitingUpdate[j];
			Player player = playerArray[k];
			int l = stream.readUnsignedByte();
			if ((l & 0x40) != 0)
				l += stream.readUnsignedByte() << 8;
			method107(l, k, stream, player);
		}
	}

	// draws walls/fences/lines
	private void drawMinimapLines(int i, int k, int l, int i1, int j1) {
		int k1 = worldController.method300(j1, l, i);
		if (k1 != 0) {
			int l1 = worldController.method304(j1, l, i, k1);
			int k2 = l1 >> 6 & 3;
			int i3 = l1 & 0x1f;
			int k3 = k;
			if (k1 > 0)
				k3 = i1;
			int ai[] = minimapImage.myPixels;
			int k4 = 24624 + l * 4 + (103 - i) * 512 * 4;
			int i5 = k1 >> 14 & 0x7fff;
			ObjectDef class46_2 = ObjectDef.forID(i5);
			if (class46_2 != null && class46_2.mapscene != -1) {
				Background background_2 = mapScenes[class46_2.mapscene];
				if (background_2 != null) {
					int i6 = (class46_2.width * 4 - background_2.myWidth) / 2;
					int j6 = (class46_2.length * 4 - background_2.myHeight) / 2;
					background_2.drawBackground(48 + l * 4 + i6, 48 + (104 - i - class46_2.length) * 4 + j6);
				}
			} else {
				if (i3 == 0 || i3 == 2)
					if (k2 == 0) {
						ai[k4] = k3;
						ai[k4 + 512] = k3;
						ai[k4 + 1024] = k3;
						ai[k4 + 1536] = k3;
					} else if (k2 == 1) {
						ai[k4] = k3;
						ai[k4 + 1] = k3;
						ai[k4 + 2] = k3;
						ai[k4 + 3] = k3;
					} else if (k2 == 2) {
						ai[k4 + 3] = k3;
						ai[k4 + 3 + 512] = k3;
						ai[k4 + 3 + 1024] = k3;
						ai[k4 + 3 + 1536] = k3;
					} else if (k2 == 3) {
						ai[k4 + 1536] = k3;
						ai[k4 + 1536 + 1] = k3;
						ai[k4 + 1536 + 2] = k3;
						ai[k4 + 1536 + 3] = k3;
					}
				if (i3 == 3)
					if (k2 == 0)
						ai[k4] = k3;
					else if (k2 == 1)
						ai[k4 + 3] = k3;
					else if (k2 == 2)
						ai[k4 + 3 + 1536] = k3;
					else if (k2 == 3)
						ai[k4 + 1536] = k3;
				if (i3 == 2)
					if (k2 == 3) {
						ai[k4] = k3;
						ai[k4 + 512] = k3;
						ai[k4 + 1024] = k3;
						ai[k4 + 1536] = k3;
					} else if (k2 == 0) {
						ai[k4] = k3;
						ai[k4 + 1] = k3;
						ai[k4 + 2] = k3;
						ai[k4 + 3] = k3;
					} else if (k2 == 1) {
						ai[k4 + 3] = k3;
						ai[k4 + 3 + 512] = k3;
						ai[k4 + 3 + 1024] = k3;
						ai[k4 + 3 + 1536] = k3;
					} else if (k2 == 2) {
						ai[k4 + 1536] = k3;
						ai[k4 + 1536 + 1] = k3;
						ai[k4 + 1536 + 2] = k3;
						ai[k4 + 1536 + 3] = k3;
					}
			}
		}
		k1 = worldController.method302(j1, l, i);
		if (k1 != 0) {
			int i2 = worldController.method304(j1, l, i, k1);
			int l2 = i2 >> 6 & 3;
			int j3 = i2 & 0x1f;
			int l3 = k1 >> 14 & 0x7fff;
			ObjectDef class46_1 = ObjectDef.forID(l3);
			if (class46_1 != null && class46_1.mapscene != -1) {
				Background background_1 = mapScenes[class46_1.mapscene];
				if (background_1 != null) {
					int j5 = (class46_1.width * 4 - background_1.myWidth) / 2;
					int k5 = (class46_1.length * 4 - background_1.myHeight) / 2;
					background_1.drawBackground(48 + l * 4 + j5, 48 + (104 - i - class46_1.length) * 4 + k5);
				}
			} else if (j3 == 9) {
				int l4 = 0xeeeeee;
				if (k1 > 0)
					l4 = 0xee0000;
				int ai1[] = minimapImage.myPixels;
				int l5 = 24624 + l * 4 + (103 - i) * 512 * 4;
				if (l2 == 0 || l2 == 2) {
					ai1[l5 + 1536] = l4;
					ai1[l5 + 1024 + 1] = l4;
					ai1[l5 + 512 + 2] = l4;
					ai1[l5 + 3] = l4;
				} else {
					ai1[l5] = l4;
					ai1[l5 + 512 + 1] = l4;
					ai1[l5 + 1024 + 2] = l4;
					ai1[l5 + 1536 + 3] = l4;
				}
			}
		}
		k1 = worldController.method303(j1, l, i);
		if (k1 != 0) {
			int j2 = k1 >> 14 & 0x7fff;
			ObjectDef class46 = ObjectDef.forID(j2);
			if (class46 != null && class46.mapscene != -1) {
				Background background = mapScenes[class46.mapscene];
				if (background != null) {
					int i4 = (class46.width * 4 - background.myWidth) / 2;
					int j4 = (class46.length * 4 - background.myHeight) / 2;
					background.drawBackground(48 + l * 4 + i4, 48 + (104 - i - class46.length) * 4 + j4);
				}
			}
		}
	}

	private static void setHighMem() {
		WorldController.lowMem = false;
		Texture.lowMem = false;
		lowMem = false;
		MapRegion.lowMem = false;
		ObjectDef.lowMem = false;
	}

	public static void main(String args[]) {
		try {
			nodeID = 10;
			portOff = 0;
			setHighMem();
			isMembers = true;
			Signlink.storeid = 32;
			Signlink.startpriv(InetAddress.getLocalHost());
			if (args.length == 1 && args[0].equals("mute")) {
				audioMuted = true;
			}
			instance = new Client();
			instance.createClientFrame(frameWidth, frameHeight);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Client instance;

	public static Client getClient() {
		return instance;
	}

	private void loadingStages() {
		if (lowMem && loadingStage == 2 && MapRegion.currentPlane != plane) {
			gameScreenImageProducer.initDrawingArea();
			regularFont.drawText(0, "Loading - please wait.", 151, 257);
			regularFont.drawText(0xffffff, "Loading - please wait.", 150, 256);
			gameScreenImageProducer.drawGraphics(frameMode == ScreenMode.FIXED ? 4 : 0, super.graphics,
					frameMode == ScreenMode.FIXED ? 4 : 0);
			loadingStage = 1;
			aLong824 = System.currentTimeMillis();
		}
		if (loadingStage == 1) {
			int j = method54();
			if (j != 0 && System.currentTimeMillis() - aLong824 > 0x57e40L) {
				Signlink.reportError(
						myUsername + " glcfb " + aLong1215 + "," + j + "," + lowMem + "," + decompressors[0] + ","
								+ onDemandFetcher.getNodeCount() + "," + plane + "," + anInt1069 + "," + anInt1070);
				aLong824 = System.currentTimeMillis();
			}
		}
		if (loadingStage == 2 && plane != lastPlane) {
			lastPlane = plane;
			refreshMinimap(plane);
		}
	}

	private int method54() {
		for (int i = 0; i < floorMapBytes.length; i++) {
			if (floorMapBytes[i] == null && floorMapArray[i] != -1)
				return -1;
			if (objectMapBytes[i] == null && objectMapArray[i] != -1)
				return -2;
		}
		boolean flag = true;
		for (int j = 0; j < floorMapBytes.length; j++) {
			byte abyte0[] = objectMapBytes[j];
			if (abyte0 != null) {
				int k = (regionIdArray[j] >> 8) * 64 - baseX;
				int l = (regionIdArray[j] & 0xff) * 64 - baseY;
				if (constructedViewport) {
					k = 10;
					l = 10;
				}
				flag &= MapRegion.method189(k, abyte0, l);
			}
		}
		if (!flag)
			return -3;
		if (aBoolean1080) {
			return -4;
		} else {
			loadingStage = 2;
			MapRegion.currentPlane = plane;
			updateWorldObjects();
			stream.createFrame(121);
			return 0;
		}
	}

	private void createProjectiles() { // method55
		for (Projectile class30_sub2_sub4_sub4 = (Projectile) projectiles
				.reverseGetFirst(); class30_sub2_sub4_sub4 != null; class30_sub2_sub4_sub4 = (Projectile) projectiles
						.reverseGetNext())
			if (class30_sub2_sub4_sub4.anInt1597 != plane || loopCycle > class30_sub2_sub4_sub4.anInt1572)
				class30_sub2_sub4_sub4.unlink();
			else if (loopCycle >= class30_sub2_sub4_sub4.anInt1571) {
				if (class30_sub2_sub4_sub4.anInt1590 > 0) {
					NPC npc = npcArray[class30_sub2_sub4_sub4.anInt1590 - 1];
					if (npc != null && npc.x >= 0 && npc.x < 13312 && npc.y >= 0 && npc.y < 13312)
						class30_sub2_sub4_sub4.method455(loopCycle, npc.y,
								method42(class30_sub2_sub4_sub4.anInt1597, npc.y, npc.x)
										- class30_sub2_sub4_sub4.anInt1583,
								npc.x);
				}
				if (class30_sub2_sub4_sub4.anInt1590 < 0) {
					int j = -class30_sub2_sub4_sub4.anInt1590 - 1;
					Player player;
					if (j == localPlayerIndex)
						player = myPlayer;
					else
						player = playerArray[j];
					if (player != null && player.x >= 0 && player.x < 13312 && player.y >= 0 && player.y < 13312)
						class30_sub2_sub4_sub4.method455(loopCycle, player.y,
								method42(class30_sub2_sub4_sub4.anInt1597, player.y, player.x)
										- class30_sub2_sub4_sub4.anInt1583,
								player.x);
				}
				class30_sub2_sub4_sub4.method456(anInt945);
				worldController.method285(plane, class30_sub2_sub4_sub4.anInt1595,
						(int) class30_sub2_sub4_sub4.aDouble1587, -1, (int) class30_sub2_sub4_sub4.aDouble1586, 60,
						(int) class30_sub2_sub4_sub4.aDouble1585, class30_sub2_sub4_sub4, false);
			}

	}

	public AppletContext getAppletContext() {
		if (Signlink.mainapp != null)
			return Signlink.mainapp.getAppletContext();
		else
			return super.getAppletContext();
	}

	@SuppressWarnings("unused")
	private void saveMidi(boolean flag, byte abyte0[]) {
		Signlink.midifade = flag ? 1 : 0;
		Signlink.saveMidi(abyte0, abyte0.length);
	}

	public void setMidiVolume(int vol) {
		if (!audioMuted) {
			musicVolume = vol;
			if (midiPlayer == null) {
				return;
			}
			midiPlayer.setVolume(0, musicVolume);
		}
	}

	public void playMidi(boolean changing, byte abyte0[]) {
		if (!audioMuted) {
			if (midiPlayer == null) {
				return;
			}
			boolean quickSong = (temporarySongDelay > 0);
			if (changing && !quickSong) {
				midiPlayer.play(abyte0, loopMusic, musicVolume);
			} else {
				midiPlayer.play(abyte0, false, musicVolume);
			}
		}
	}

	private void stopMidi() {
		if (!audioMuted) {
			if (midiPlayer != null) {
				midiPlayer.stop();
			}
			// currentSong = -1;
		}
	}

	private void playSound(int soundId, int type, int delay) {
		if (!audioMuted && soundEnabled && !lowMem && currentSound < 50) {
			sound[currentSound] = soundId;
			soundType[currentSound] = type;
			soundDelay[currentSound] = delay + Sounds.anIntArray326[soundId];
			currentSound++;
		}
	}

	private void playSound(String audioName) {
		if (!audioMuted && soundEnabled && !lowMem) {
			try {
				File audioFile = new File(Signlink.findcachedir() + "/audio/" + audioName + ".wav");
				if (!audioFile.exists()) {
					return;
				}
				byte[] audioBytes = FileOperations.ReadFile(audioFile.getPath());
				new SoundPlayer((InputStream) new ByteArrayInputStream(audioBytes), 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void processOnDemandQueue() {
		do {
			OnDemandData onDemandData;
			do {
				onDemandData = onDemandFetcher.getNextNode();
				if (onDemandData == null)
					return;
				if (onDemandData.dataType == 0) {
					Model.loadModelHeader(onDemandData.buffer, onDemandData.ID);
					redrawTab = true;
					if (backDialogID != -1)
						redrawChatbox = true;
				}
				if (onDemandData.dataType == 1 && onDemandData.buffer != null)
					Frame.load(onDemandData.buffer, onDemandData.ID);
				if (onDemandData.dataType == 2 && onDemandData.ID == nextSong && onDemandData.buffer != null) {
					playMidi(songChanging, onDemandData.buffer);
					// saveMidi(songChanging, onDemandData.buffer);
				}
				if (onDemandData.dataType == 3 && loadingStage == 1) {
					for (int i = 0; i < floorMapBytes.length; i++) {
						if (floorMapArray[i] == onDemandData.ID) {
							floorMapBytes[i] = onDemandData.buffer;
							if (onDemandData.buffer == null)
								floorMapArray[i] = -1;
							break;
						}
						if (objectMapArray[i] != onDemandData.ID)
							continue;
						objectMapBytes[i] = onDemandData.buffer;
						if (onDemandData.buffer == null)
							objectMapArray[i] = -1;
						break;
					}

				}
			} while (onDemandData.dataType != 93 || !onDemandFetcher.method564(onDemandData.ID));
			MapRegion.method173(new Stream(onDemandData.buffer), onDemandFetcher);
		} while (true);
	}

	private void resetInterfaceAnimation(int i) {
		RSInterface[] components = RSInterface.interfaceCache[i];
		for (int j = 0; j < components.length; j++) {
			RSInterface child = components[j];
			if (child == null)
				continue;
			child.anInt246 = 0;
			child.anInt208 = 0;
		}
	}

	private void drawHeadIcon() {
		if (anInt855 != 2)
			return;
		calcEntityScreenPos((anInt934 - baseX << 7) + anInt937, anInt936 * 2, (anInt935 - baseY << 7) + anInt938);
		if (spriteDrawX > -1 && loopCycle % 20 < 10)
			headIconsHint[0].drawSprite(spriteDrawX - 12, spriteDrawY - 28);
	}

	public void sendString(String s, int interfaceID, int child) {
		RSInterface component = RSInterface.getInterface(interfaceID, child);
		if (component != null) {
			component.disabledText = s;
			if (s.contains("<url=") && s.endsWith("</url>")) {
				String text = s.substring(0, s.indexOf("<url="));
				s = s.substring(text.length() + 5).trim();
				component.interfaceURL = s.substring(0, s.indexOf(">"));
				s = s.substring(component.interfaceURL.length() + 1).trim();
				component.interfaceURLDisplay = s.substring(0, s.indexOf("</url>"));
			} else {
				component.interfaceURL = null;
				component.interfaceURLDisplay = null;
			}
			redrawTab = true;
			redrawChatbox = true;
		}
	}

	private void mainGameProcessor() {
		refreshFrameSize();
		if (systemUpdateTime > 1)
			systemUpdateTime--;
		if (anInt1011 > 0)
			anInt1011--;
		for (int j = 0; j < 5; j++) {
			if (!parsePacket())
				break;
		}

		if (!loggedIn)
			return;

		if (anInt1016 > 0)
			anInt1016--;
		if (super.keyArray[1] == 1 || super.keyArray[2] == 1 || super.keyArray[3] == 1 || super.keyArray[4] == 1)
			aBoolean1017 = true;
		if (aBoolean1017 && anInt1016 <= 0) {
			anInt1016 = 20;
			aBoolean1017 = false;
			stream.createFrame(86);
			stream.writeShort(anInt1184);
			stream.writeShortA(cameraHorizontal);
		}
		if (super.awtFocus && !gameHasFocus) {
			gameHasFocus = true;
			stream.createFrame(3);
			stream.writeWordBigEndian(1);
		}
		if (!super.awtFocus && gameHasFocus) {
			gameHasFocus = false;
			stream.createFrame(3);
			stream.writeWordBigEndian(0);
		}
		loadingStages();
		method115();
		processAudioQueue();
		timeoutCounter++;
		if (timeoutCounter > 750)
			dropClient();
		method114();
		forceNPCUpdateBlock();
		method38();
		anInt945++;
		if (crossType != 0) {
			crossIndex += 20;
			if (crossIndex >= 400)
				crossType = 0;
		}
		if (atInventoryInterfaceType != 0) {
			atInventoryLoopCycle++;
			if (atInventoryLoopCycle >= 15) {
				if (atInventoryInterfaceType == 2) {
					redrawTab = true;
				}
				if (atInventoryInterfaceType == 3)
					redrawChatbox = true;
				atInventoryInterfaceType = 0;
			}
		}
		if (activeInterfaceType != 0) {
			anInt989++;
			if (super.mouseX > anInt1087 + 5 || super.mouseX < anInt1087 - 5 || super.mouseY > anInt1088 + 5
					|| super.mouseY < anInt1088 - 5)
				aBoolean1242 = true;
			if (super.clickMode2 == 0) {
				if (activeInterfaceType == 2) {
					redrawTab = true;
				}
				if (activeInterfaceType == 3)
					redrawChatbox = true;
				activeInterfaceType = 0;
				if (aBoolean1242 && anInt989 >= 15) {
					lastActiveInvInterface = -1;
					processRightClick();
					processBankClick();
					if (lastActiveInvInterface == anInt1084) {
						if (mouseInvInterfaceIndex != lastMouseInvInterfaceIndex) {
							RSInterface class9 = RSInterface.getInterface(anInt1084);
							int j1 = 0;
							if (anInt913 == 1 && class9.contentType == 206)
								j1 = 1;
							if (class9.inventoryItemIds[mouseInvInterfaceIndex] <= 0)
								j1 = 0;
							if (class9.replacableItemInterface) {
								int l2 = lastMouseInvInterfaceIndex;
								int l3 = mouseInvInterfaceIndex;
								class9.inventoryItemIds[l3] = class9.inventoryItemIds[l2];
								class9.inventoryStackSizes[l3] = class9.inventoryStackSizes[l2];
								class9.inventoryItemIds[l2] = -1;
								class9.inventoryStackSizes[l2] = 0;
							} else {
								if (j1 == 1) {
									int i3 = lastMouseInvInterfaceIndex;
									for (int i4 = mouseInvInterfaceIndex; i3 != i4;) {
										if (i3 > i4) {
											class9.swapInventoryItems(i3, i3 - 1);
											i3--;
										} else {
											if (i3 < i4) {
												class9.swapInventoryItems(i3, i3 + 1);
												i3++;
											}
										}
									}
								} else {
									class9.swapInventoryItems(lastMouseInvInterfaceIndex, mouseInvInterfaceIndex);
								}
							}
							stream.createFrame(214); // SWAP ITEM
							stream.writeDWord(anInt1084);
							stream.method424(j1);
							stream.method433(lastMouseInvInterfaceIndex);
							stream.method431(mouseInvInterfaceIndex);
						}
					} else {
						RSInterface oldInv = RSInterface.getInterface(anInt1084);
						RSInterface newInv = RSInterface.getInterface(lastActiveInvInterface);
						if (newInv != null && oldInv != null) {
							if (oldInv.canChangeInventory && newInv.canChangeInventory) {
								int fromSlot = lastMouseInvInterfaceIndex;
								int toSlot = mouseInvInterfaceIndex;
								// if(newInv.inventoryItemIds[toSlot] > 0)
								int temp = oldInv.inventoryItemIds[fromSlot];
								oldInv.inventoryItemIds[fromSlot] = newInv.inventoryItemIds[toSlot];
								newInv.inventoryItemIds[toSlot] = temp;
								temp = oldInv.inventoryStackSizes[fromSlot];
								oldInv.inventoryStackSizes[fromSlot] = newInv.inventoryStackSizes[toSlot];
								newInv.inventoryStackSizes[toSlot] = temp;
								stream.createFrame(213); // SWAP item inventory
								stream.writeDWord(anInt1084);
								stream.writeDWord(lastActiveInvInterface);
								stream.method433(lastMouseInvInterfaceIndex);
								stream.method431(mouseInvInterfaceIndex);
							}
						}
					}
				} else if ((anInt1253 == 1 || menuHasAddFriend(menuActionRow - 1)) && menuActionRow > 2)
					determineMenuSize();
				else if (menuActionRow > 0)
					doAction(menuActionRow - 1);
				atInventoryLoopCycle = 10;
				super.clickMode3 = 0;
			}
		}
		if (WorldController.anInt470 != -1) {
			int k = WorldController.anInt470;
			int k1 = WorldController.anInt471;
			boolean flag = doWalkTo(0, 0, 0, 0, myPlayer.smallY[0], 0, 0, k1, myPlayer.smallX[0], true, k);
			WorldController.anInt470 = -1;
			if (flag) {
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 1;
				crossIndex = 0;
			}
		}
		if (super.clickMode3 == 1 && aString844 != null) {
			aString844 = null;
			redrawChatbox = true;
			super.clickMode3 = 0;
		}
		processMenuClick();
		if (super.clickMode2 == 1 || super.clickMode3 == 1)
			anInt1213++;
		if (anInt1500 != 0 || anInt1044 != 0 || anInt1129 != 0) {
			if (anInt1501 < 0 && !menuOpen) {
				anInt1501++;
				if (anInt1501 == 0) {
					if (anInt1500 != 0) {
						redrawChatbox = true;
					}
					if (anInt1044 != 0) {
					}
				}
			}
		} else if (anInt1501 > 0) {
			anInt1501--;
		}
		if (loadingStage == 2)
			method108();
		if (loadingStage == 2 && cutSceneCamera)
			calcCameraPos();
		for (int i1 = 0; i1 < 5; i1++)
			anIntArray1030[i1]++;

		method73();
		super.idleTime++;
		if (super.idleTime > 20000) {
			anInt1011 = 250;
			super.idleTime -= 2000;
			stream.createFrame(202);
		}
		anInt1010++;
		if (anInt1010 > 50)
			stream.createFrame(0);
		try {
			if (socketStream != null && stream.currentOffset > 0) {
				socketStream.queueBytes(stream.currentOffset, stream.buffer);
				stream.currentOffset = 0;
				anInt1010 = 0;
			}
		} catch (IOException _ex) {
			dropClient();
		} catch (Exception exception) {
			resetLogout();
		}
	}

	private void method63() {
		SpawnedObject class30_sub1 = (SpawnedObject) aClass19_1179.reverseGetFirst();
		for (; class30_sub1 != null; class30_sub1 = (SpawnedObject) aClass19_1179.reverseGetNext())
			if (class30_sub1.anInt1294 == -1) {
				class30_sub1.anInt1302 = 0;
				method89(class30_sub1);
			} else {
				class30_sub1.unlink();
			}

	}

	private void resetImageProducers() {
		if (aRSImageProducer_1107 != null)
			return;
		super.fullGameScreen = null;
		aRSImageProducer_1166 = null;
		aRSImageProducer_1164 = null;
		tabImageProducer = null;
		gameScreenImageProducer = null;
		aRSImageProducer_1125 = null;
		aRSImageProducer_1110 = new RSImageProducer(128, 265);
		DrawingArea.setAllPixelsToZero();
		aRSImageProducer_1111 = new RSImageProducer(128, 265);
		DrawingArea.setAllPixelsToZero();
		aRSImageProducer_1107 = new RSImageProducer(509, 171);
		DrawingArea.setAllPixelsToZero();
		aRSImageProducer_1108 = new RSImageProducer(360, 132);
		DrawingArea.setAllPixelsToZero();
		aRSImageProducer_1109 = new RSImageProducer(frameWidth, frameHeight);
		DrawingArea.setAllPixelsToZero();
		aRSImageProducer_1112 = new RSImageProducer(202, 238);
		DrawingArea.setAllPixelsToZero();
		aRSImageProducer_1113 = new RSImageProducer(203, 238);
		DrawingArea.setAllPixelsToZero();
		aRSImageProducer_1114 = new RSImageProducer(74, 94);
		DrawingArea.setAllPixelsToZero();
		aRSImageProducer_1115 = new RSImageProducer(75, 94);
		DrawingArea.setAllPixelsToZero();
		if (titleArchive != null) {
			// drawLogo();
			// loadTitleScreen();
		}
		redrawGame = true;
	}

	private void resetImageProducers2() {
		if (aRSImageProducer_1166 != null)
			return;
		nullLoader();
		super.fullGameScreen = null;
		aRSImageProducer_1107 = null;
		aRSImageProducer_1108 = null;
		aRSImageProducer_1109 = null;
		aRSImageProducer_1110 = null;
		aRSImageProducer_1111 = null;
		aRSImageProducer_1112 = null;
		aRSImageProducer_1113 = null;
		aRSImageProducer_1114 = null;
		aRSImageProducer_1115 = null;
		aRSImageProducer_1166 = new RSImageProducer(519, 165);// chatback
		aRSImageProducer_1164 = new RSImageProducer(249, 168);// mapback
		DrawingArea.setAllPixelsToZero();
		tabImageProducer = new RSImageProducer(249, 335);// inventory
		gameScreenImageProducer = new RSImageProducer(512, 334);// gamescreen
		DrawingArea.setAllPixelsToZero();
		aRSImageProducer_1125 = new RSImageProducer(249, 45);
		redrawGame = true;
	}

	@Override
	public void drawLoadingText(int i, String s) {
		anInt1079 = i;
		aString1049 = s;
		resetImageProducers();
		if (titleArchive == null) {
			super.drawLoadingText(i, s);
			return;
		}
		aRSImageProducer_1109.initDrawingArea();
		char c = '\u0168';
		char c1 = '\310';
		byte byte1 = 20;
		boldFontS.drawText(0xffffff, "vscape is loading - please wait...", c1 / 2 - 26 - byte1, c / 2);
		int j = c1 / 2 - 18 - byte1;
		DrawingArea.fillPixels(c / 2 - 152, 304, 34, 0x8c1111, j);
		DrawingArea.fillPixels(c / 2 - 151, 302, 32, 0, j + 1);
		DrawingArea.drawPixels(30, j + 2, c / 2 - 150, 0x8c1111, i * 3);
		DrawingArea.drawPixels(30, j + 2, (c / 2 - 150) + i * 3, 0, 300 - i * 3);
		boldFontS.drawText(0xffffff, s, (c1 / 2 + 5) - byte1, c / 2);
		aRSImageProducer_1109.drawGraphics(171, super.graphics, 202);
		/*
		 * if (redrawGame) { redrawGame = false; if (!aBoolean831) {
		 * aRSImageProducer_1110.drawGraphics(0, super.graphics, 0);
		 * aRSImageProducer_1111.drawGraphics(0, super.graphics, 637); }
		 * aRSImageProducer_1107.drawGraphics(0, super.graphics, 128);
		 * aRSImageProducer_1108.drawGraphics(371, super.graphics, 202);
		 * aRSImageProducer_1112.drawGraphics(265, super.graphics, 0);
		 * aRSImageProducer_1113.drawGraphics(265, super.graphics, 562);
		 * aRSImageProducer_1114.drawGraphics(171, super.graphics, 128);
		 * aRSImageProducer_1115.drawGraphics(171, super.graphics, 562); }
		 */
	}

	public void moveScroller(int x, int barSize, int mX, int mY, RSInterface class9, int y, boolean flag,
			int scrollMax) {
		int anInt992;
		if (aBoolean972)
			anInt992 = 32;
		else
			anInt992 = 0;
		aBoolean972 = false;
		if (mX >= x && mX < x + 16 && mY >= y && mY < y + 16) {
			class9.scrollPosition -= anInt1213 * 4;
			if (flag) {
				redrawTab = true;
			}
		} else if (mX >= x && mX < x + 16 && mY >= (y + barSize) - 16 && mY < y + barSize) {
			class9.scrollPosition += anInt1213 * 4;
			if (flag) {
				redrawTab = true;
			}
			// free scrolling
		} else if (mX >= x - anInt992 && mX < x + 16 + anInt992 && mY >= y + 16 && mY < (y + barSize) - 16
				&& anInt1213 > 0) {
			int l1 = ((barSize - 32) * barSize) / scrollMax;
			if (l1 < 8)
				l1 = 8;
			int i2 = mY - y - 16 - l1 / 2;
			int j2 = barSize - 32 - l1;
			class9.scrollPosition = (short) (((scrollMax - barSize) * i2) / j2);
			if (flag)
				redrawTab = true;
			aBoolean972 = true;
		}
	}

	private boolean method66(int i, int j, int k) {
		int i1 = i >> 14 & 0x7fff;
		int j1 = worldController.method304(plane, k, j, i);
		if (j1 == -1)
			return false;
		int k1 = j1 & 0x1f;
		int l1 = j1 >> 6 & 3;
		if (k1 == 10 || k1 == 11 || k1 == 22) {
			ObjectDef class46 = ObjectDef.forID(i1);
			if (class46 != null) {
				int i2;
				int j2;
				if (l1 == 0 || l1 == 2) {
					i2 = class46.width;
					j2 = class46.length;
				} else {
					i2 = class46.length;
					j2 = class46.width;
				}
				int k2 = class46.surroundings;
				if (l1 != 0)
					k2 = (k2 << l1 & 0xf) + (k2 >> 4 - l1);
				doWalkTo(2, 0, j2, 0, myPlayer.smallY[0], i2, k2, j, myPlayer.smallX[0], false, k);
			}
		} else {
			doWalkTo(2, l1, 0, k1 + 1, myPlayer.smallY[0], 0, 0, j, myPlayer.smallX[0], false, k);
		}
		crossX = super.saveClickX;
		crossY = super.saveClickY;
		crossType = 2;
		crossIndex = 0;
		return true;
	}

	private Archive requestArchive(int i, String s, String s1, int j, int k) {
		byte abyte0[] = null;
		int l = 5;
		try {
			if (decompressors[0] != null)
				abyte0 = decompressors[0].get(i);
		} catch (Exception _ex) {
		}
		if (abyte0 != null) {
			// aCRC32_930.reset();
			// aCRC32_930.update(abyte0);
			// int i1 = (int)aCRC32_930.getValue();
			// if(i1 != j)
		}
		if (abyte0 != null) {
			Archive archive = new Archive(abyte0);
			return archive;
		}
		int j1 = 0;
		while (abyte0 == null) {
			String s2 = "Unknown error";
			drawLoadingText(k, "Requesting " + s);
			try {
				int k1 = 0;
				DataInputStream datainputstream = openJagGrabInputStream(s1 + j);
				byte abyte1[] = new byte[6];
				datainputstream.readFully(abyte1, 0, 6);
				Stream stream = new Stream(abyte1);
				stream.currentOffset = 3;
				int i2 = stream.read3Bytes() + 6;
				int j2 = 6;
				abyte0 = new byte[i2];
				System.arraycopy(abyte1, 0, abyte0, 0, 6);

				while (j2 < i2) {
					int l2 = i2 - j2;
					if (l2 > 1000)
						l2 = 1000;
					int j3 = datainputstream.read(abyte0, j2, l2);
					if (j3 < 0) {
						s2 = "Length error: " + j2 + "/" + i2;
						throw new IOException("EOF");
					}
					j2 += j3;
					int k3 = (j2 * 100) / i2;
					if (k3 != k1)
						drawLoadingText(k, "Loading " + s + " - " + k3 + "%");
					k1 = k3;
				}
				datainputstream.close();
				try {
					if (decompressors[0] != null)
						decompressors[0].put(abyte0.length, abyte0, i);
				} catch (Exception _ex) {
					decompressors[0] = null;
				}
				/*
				 * if(abyte0 != null) { aCRC32_930.reset(); aCRC32_930.update(abyte0); int i3 =
				 * (int)aCRC32_930.getValue(); if(i3 != j) { abyte0 = null; j1++; s2 =
				 * "Checksum error: " + i3; } }
				 */
			} catch (IOException ioexception) {
				if (s2.equals("Unknown error"))
					s2 = "Connection error";
				abyte0 = null;
			} catch (NullPointerException _ex) {
				s2 = "Null error";
				abyte0 = null;
				if (!Signlink.reporterror)
					return null;
			} catch (ArrayIndexOutOfBoundsException _ex) {
				s2 = "Bounds error";
				abyte0 = null;
				if (!Signlink.reporterror)
					return null;
			} catch (Exception _ex) {
				s2 = "Unexpected error";
				abyte0 = null;
				if (!Signlink.reporterror)
					return null;
			}
			if (abyte0 == null) {
				for (int l1 = l; l1 > 0; l1--) {
					if (j1 >= 3) {
						drawLoadingText(k, "Game updated - please reload page");
						l1 = 10;
					} else {
						drawLoadingText(k, s2 + " - Retrying in " + l1);
					}
					try {
						Thread.sleep(1000L);
					} catch (Exception _ex) {
					}
				}

				l *= 2;
				if (l > 60)
					l = 60;
				aBoolean872 = !aBoolean872;
			}

		}

		Archive archive_1 = new Archive(abyte0);
		return archive_1;
	}

	private void dropClient() {
		if (anInt1011 > 0) {
			resetLogout();
			return;
		}
		gameScreenImageProducer.initDrawingArea();
		regularFont.drawText(0, "Connection lost", 144, 257);
		regularFont.drawText(0xffffff, "Connection lost", 143, 256);
		regularFont.drawText(0, "Please wait - attempting to reestablish", 159, 257);
		regularFont.drawText(0xffffff, "Please wait - attempting to reestablish", 158, 256);
		gameScreenImageProducer.drawGraphics(4, super.graphics, 4);
		anInt1021 = 0;
		destX = 0;
		RSSocket rsSocket = socketStream;
		loggedIn = false;
		loginFailures = 0;
		login(myUsername, myPassword, true);
		if (!loggedIn)
			resetLogout();
		try {
			rsSocket.close();
		} catch (Exception _ex) {
		}
	}

	public void setNorth() {
		cameraOffsetX = 0;
		cameraOffsetY = 0;
		viewRotationOffset = 0;
		cameraHorizontal = 0;
		minimapRotation = 0;
		minimapZoom = 0;
	}

	private void doAction(int i) {
		if (i < 0)
			return;
		if (i > 0 && inputDialogState != 3 && inputDialogState != 4 && inputDialogState != 5) {
			if (inputDialogState != 0) {
				inputDialogState = 0;
				redrawChatbox = true;
			}
		}
		int first = menuActionCmd2[i];
		int second = menuActionCmd3[i];
		int action = menuActionID[i];
		int clicked = menuActionCmd1[i];
		if (action >= 2000)
			action -= 2000;
		if (action == 696) {
			setNorth();
		}
		if (action == 582) {
			NPC npc = npcArray[clicked];
			if (npc != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, npc.smallY[0], myPlayer.smallX[0], false, npc.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(57); // ITEM ON NPC
				stream.writeShortA(anInt1285);
				stream.writeShortA(clicked);
				stream.method431(anInt1283);
				stream.writeDWord(anInt1284);// stream.writeShortA(anInt1284);
			}
		}
		if (action == 234) {
			boolean flag1 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, second, myPlayer.smallX[0], false, first);
			if (!flag1)
				flag1 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, second, myPlayer.smallX[0], false, first);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(236);
			stream.method431(second + baseY);
			stream.writeShort(clicked);
			stream.method431(first + baseX);
		}
		if (action == 62 && method66(clicked, second, first)) {
			stream.createFrame(192); // ITEM ON OBJECT
			stream.writeDWord(anInt1284);// stream.writeShort(anInt1284);
			stream.method431(clicked >> 14 & 0x7fff);
			stream.method433(second + baseY);
			stream.method431(anInt1283);
			stream.method433(first + baseX);
			stream.writeShort(anInt1285);
		}
		if (action == 511) {
			boolean flag2 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, second, myPlayer.smallX[0], false, first);
			if (!flag2)
				flag2 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, second, myPlayer.smallX[0], false, first);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(25); // ITEM ON GROUND ITEM
			stream.writeDWord(anInt1284);// stream.method431(anInt1284);
			stream.writeShortA(anInt1285);
			stream.writeShort(clicked);
			stream.writeShortA(second + baseY);
			stream.method433(anInt1283);
			stream.writeShort(first + baseX);
		}
		if (action == 74) {
			stream.createFrame(122); // FIRST_CLICK_ITEM
			stream.writeDWord(second);
			stream.writeShortA(first);
			stream.method431(clicked);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		if (action == 315) {
			RSInterface rsInterface = RSInterface.getInterface(second);
			boolean flag8 = true;
			if (rsInterface.contentType > 0) {
				flag8 = promptUserForInput(rsInterface);
			}
			if (flag8) {
				switch (second) {
				case 2097179:
					cameraZoom = 600;
					break;
				default:
					stream.createFrame(185);
					stream.writeDWord(second);
					System.out.println(second);
					break;
				}
			}
		}
		if (action == 561) {
			Player player = playerArray[clicked];
			if (player != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, player.smallY[0], myPlayer.smallX[0], false,
						player.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt1188 += clicked;
				if (anInt1188 >= 90) {
					stream.createFrame(136);
					anInt1188 = 0;
				}
				stream.createFrame(128);
				stream.writeShort(clicked);
			}
		}
		if (action == 20) {
			NPC class30_sub2_sub4_sub1_sub1_1 = npcArray[clicked];
			if (class30_sub2_sub4_sub1_sub1_1 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_1.smallY[0],
						myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_1.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(155);
				stream.method431(clicked);
			}
		}
		if (action == 779) {
			Player class30_sub2_sub4_sub1_sub2_1 = playerArray[clicked];
			if (class30_sub2_sub4_sub1_sub2_1 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_1.smallY[0],
						myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_1.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(153);
				stream.method431(clicked);
			}
		}
		if (action == 519)
			if (!menuOpen)
				worldController.method312(super.saveClickY - 4, super.saveClickX - 4);
			else
				worldController.method312(second - 4, first - 4);
		if (action == 1062) {
			anInt924 += baseX;
			if (anInt924 >= 113) {
				stream.createFrame(183);
				stream.writeDWordBigEndian(0xe63271);
				anInt924 = 0;
			}
			method66(clicked, second, first);
			stream.createFrame(228);
			stream.writeShortA(clicked >> 14 & 0x7fff);
			stream.writeShortA(second + baseY);
			stream.writeShort(first + baseX);
		}
		if (action == 679 && !continueDialogue) {
			stream.createFrame(40);
			stream.writeShort(second);
			continueDialogue = true;
		}
		if (action == 431) {
			stream.createFrame(129); // CLICK_ALL
			stream.writeShortA(first);
			stream.writeDWord(second);
			stream.writeShortA(clicked);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		if (action == 337 || action == 42 || action == 792 || action == 322) {
			String s = menuActionName[i];
			int k1 = s.indexOf("@whi@");
			if (k1 != -1) {
				long l3 = TextUtil.longForName(s.substring(k1 + 5).trim());
				if (action == 337)
					addFriend(l3);
				if (action == 42)
					addIgnore(l3);
				if (action == 792)
					delFriend(l3);
				if (action == 322)
					delIgnore(l3);
			}
		}
		if (action == 1500 || action == 1501 || action == 1502) {
			String s = menuActionName[i];
			int k1 = s.indexOf("@whi@");
			if (k1 != -1) {
				long l3 = TextUtil.longForName(s.substring(k1 + 5).trim());
				if (action == 1500)
					clanChatUserAction(l3, 0);
				if (action == 1501)
					clanChatUserAction(l3, 1);
				if (action == 1502)
					clanChatUserAction(l3, 2);
			}
		}
		if (action == 53) {
			stream.createFrame(135); // SHOW_ENTER_X
			stream.method431(first);
			stream.writeDWord(second);
			stream.method431(clicked);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		if (action == 539) {
			stream.createFrame(16); // SECOND_CLICK_ITEM
			stream.writeShortA(clicked);
			stream.method433(first);
			stream.writeDWord(second);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		if (action == 927) {
			String url = chatNames[second];
			if (url != null) {
				launchURL(url);
			}
		}
		if (action == 928) {
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface.interfaceURL != null) {
				launchURL(rsInterface.interfaceURL);
			}
		}
		if (action == 484 || action == 6) {
			String s1 = menuActionName[i];
			int l1 = s1.indexOf("@whi@");
			if (l1 != -1) {
				s1 = s1.substring(l1 + 5).trim();
				String s7 = TextUtil.fixName(TextUtil.nameForLong(TextUtil.longForName(s1)));
				boolean flag9 = false;
				for (int j3 = 0; j3 < playerCount; j3++) {
					Player class30_sub2_sub4_sub1_sub2_7 = playerArray[playerIndices[j3]];
					if (class30_sub2_sub4_sub1_sub2_7 == null || class30_sub2_sub4_sub1_sub2_7.name == null
							|| !class30_sub2_sub4_sub1_sub2_7.name.equalsIgnoreCase(s7))
						continue;
					doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_7.smallY[0],
							myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_7.smallX[0]);
					if (action == 484) {
						stream.createFrame(139);
						stream.method431(playerIndices[j3]);
					}
					if (action == 6) {
						anInt1188 += clicked;
						if (anInt1188 >= 90) {
							stream.createFrame(136);
							anInt1188 = 0;
						}
						stream.createFrame(128);
						stream.writeShort(playerIndices[j3]);
					}
					flag9 = true;
					break;
				}

				if (!flag9)
					pushMessage("Unable to find " + s7, 0, "", true);
			}
		}
		if (action == 870) {
			stream.createFrame(53); // USE_ITEM_ON_ITEM
			stream.writeShort(first);
			stream.writeShortA(anInt1283);
			stream.method433(clicked);
			stream.writeShort(anInt1284);
			stream.method431(anInt1285);
			stream.writeDWord(second);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		if (action == 847) {
			stream.createFrame(87); // DROP_ITEM
			stream.writeShortA(clicked);
			stream.writeDWord(second);
			stream.writeShortA(first);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		if (action == 626) {
			RSInterface rsInterface = RSInterface.getInterface(second);
			spellSelected = 1;
			spellID = rsInterface.interfaceHash;
			anInt1137 = second;
			spellUsableOn = rsInterface.spellUsableOn;
			itemSelected = 0;
			redrawTab = true;
			String s4 = rsInterface.selectedActionName;
			if (s4.indexOf(" ") != -1)
				s4 = s4.substring(0, s4.indexOf(" "));
			String s8 = rsInterface.selectedActionName;
			if (s8.indexOf(" ") != -1)
				s8 = s8.substring(s8.indexOf(" ") + 1);
			spellTooltip = s4 + " " + rsInterface.spellName + " " + s8;
			if (spellUsableOn == 16) {
				redrawTab = true;
				tabID = 3;
				redrawTabIcons = true;
			}
			return;
		}
		if (action == 78) {
			stream.createFrame(117); // CLICK_5
			stream.writeDWord(second);
			stream.method433(clicked);
			stream.method431(first);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		if (action == 27) {
			Player class30_sub2_sub4_sub1_sub2_2 = playerArray[clicked];
			if (class30_sub2_sub4_sub1_sub2_2 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_2.smallY[0],
						myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_2.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt986 += clicked;
				if (anInt986 >= 54) {
					stream.createFrame(189);
					stream.writeWordBigEndian(234);
					anInt986 = 0;
				}
				stream.createFrame(73);
				stream.method431(clicked);
			}
		}
		if (action == 213) {
			boolean flag3 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, second, myPlayer.smallX[0], false, first);
			if (!flag3)
				flag3 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, second, myPlayer.smallX[0], false, first);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(79);
			stream.method431(second + baseY);
			stream.writeShort(clicked);
			stream.writeShortA(first + baseX);
		}
		if (action == 632) {
			stream.createFrame(145); // CLICK_1
			stream.writeDWord(second);
			if ((second == 11403314 || second == 11403315 || second == 11403316 || second == 11403317 || second == 11403318 || second == 11403319 || second == 11403320 || second == 11403321 || second == 11403322 || second == 11403323) && searchString != "" && searchString != null && searchString.length() > 0)
			{
				try
				{
//					first = -1;
//					for (int y = 0; y < tempInventoryItemIds[0].length; y++)
//					{
//						if ((tempInventoryItemIds[0][y] - 1) == clicked)
//						{
//							tempInventoryItemIds[0] = removeElement(tempInventoryItemIds[0], y);
//							tempInventoryStackSizes[0] = removeElement(tempInventoryStackSizes[0], y);
//							first = y;
//							break;
//						}
//					}
//					if (first == -1)
//					{
//						for (int x = 1; x < 9; x++)
//						{
//							if (first != -1)
//							{
//								break;
//							}
//							for (int y = 0; y < tempInventoryItemIds[x].length; y++)
//							{
//								if ((tempInventoryItemIds[x][y] - 1) == clicked)
//								{
//									if (tempInventoryStackSizes[x][y] == 1)
//									{
//										tempInventoryItemIds[x] = removeElement(tempInventoryItemIds[x], y);
//										tempInventoryStackSizes[x] = removeElement(tempInventoryStackSizes[x], y);
//									}
//									first = y;
//									break;
//								}
//							}
//						}
//					}

					stream.writeShortA(first);
					stream.writeShortA(clicked);
					System.out.println("We didnt error here...");
				}
				catch (Exception ex)
				{
					System.out.println(ex);
				}

			}
			else
			{
				stream.writeShortA(first);
				stream.writeShortA(clicked);
			}

			System.out.println("Second: " + second + " First: " + first + " Clicked: " + clicked);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		// equipment action packets
		if (action >= 700 && action <= 705) { // ACTION 1 - 5
			int packetId = 110 + (action - 700);
			stream.createFrame(packetId);
			stream.writeDWord(second);
			stream.method433(clicked);
			stream.method431(first);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		if (action == 1010) {
			cameraHorizontal = 0;
		}
		if (action >= 1011 && action <= 1014) {
			switch (action) {
			case 1011:
				XPDrop.toggle();
				break;
			case 1012:
			case 1013:
			case 1014:
				XPDrop.setDropPosX((action - 1012));
				break;
			}
			SettingsManager.write();
		}
		if (action == 1050) {
			int currentHP = currentStats[3];
			if (currentHP > 0) {
				runClicked = !runClicked;
				sendFrame36(429, runClicked ? 1 : 0);
				stream.createFrame(185);
				stream.writeDWord(2097164); // (run button interface hash)
			}
		}
		if (action == 1051) {
			SettingsManager.orbsOnRight = !SettingsManager.orbsOnRight;
		}
		if (action == 1008) {
			globalMode = 2;
			redrawChatbox = true;
			SettingsManager.write();
		}
		if (action == 1007) {
			globalMode = 1;
			redrawChatbox = true;
			SettingsManager.write();
		}
		if (action == 1006) {
			globalMode = 0;
			redrawChatbox = true;
			SettingsManager.write();
		}
		if (action == 1005) {
			setChatChannel(6, 9);
		}
		if (action == 1004) {
			if (tabInterfaceIDs[10] != -1) {
				redrawTab = true;
				tabID = 10;
				redrawTabIcons = true;
			}
		}
		if (action == 1002) {
			gameMode = 2;
			redrawChatbox = true;
			SettingsManager.write();
		}
		if (action == 1001) {
			gameMode = 1;
			redrawChatbox = true;
			SettingsManager.write();
		}
		if (action == 1000) {
			gameMode = 0;
			redrawChatbox = true;
			SettingsManager.write();
		}
		if (action == 999) {
			setChatChannel(0, 0);
		}
		if (action == 998) {
			setChatChannel(1, 5);
		}
		if (action >= 994 && action <= 997) {
			if (action == 997) {
				publicChatMode = 3;
			}
			if (action == 996) {
				publicChatMode = 2;
			}
			if (action == 995) {
				publicChatMode = 1;
			}
			if (action == 994) {
				publicChatMode = 0;
			}
			redrawChatbox = true;
			stream.createFrame(95);
			stream.writeWordBigEndian(publicChatMode);
			stream.writeWordBigEndian(privateChatMode);
			stream.writeWordBigEndian(tradeMode);
		}
		if (action == 993) {
			setChatChannel(2, 1);
		}
		if (action >= 990 && action <= 992) {
			if (action == 992) {
				privateChatMode = 2;
			}
			if (action == 991) {
				privateChatMode = 1;
			}
			if (action == 990) {
				privateChatMode = 0;
			}
			redrawChatbox = true;
			stream.createFrame(95);
			stream.writeWordBigEndian(publicChatMode);
			stream.writeWordBigEndian(privateChatMode);
			stream.writeWordBigEndian(tradeMode);
		}
		if (action == 989) {
			setChatChannel(3, 2);
		}
		if (action >= 985 && action <= 987) {
			if (action == 987) {
				tradeMode = 2;
			}
			if (action == 986) {
				tradeMode = 1;
			}
			if (action == 985) {
				tradeMode = 0;
			}
			redrawChatbox = true;
			stream.createFrame(95);
			stream.writeWordBigEndian(publicChatMode);
			stream.writeWordBigEndian(privateChatMode);
			stream.writeWordBigEndian(tradeMode);
		}
		if (action == 984) {
			setChatChannel(5, 3);
		}
		if (action == 983) {
			clanChatMode = 2;
			redrawChatbox = true;
			SettingsManager.write();
		}
		if (action == 982) {
			clanChatMode = 1;
			redrawChatbox = true;
			SettingsManager.write();
		}
		if (action == 981) {
			clanChatMode = 0;
			redrawChatbox = true;
			SettingsManager.write();
		}
		if (action == 980) {
			setChatChannel(4, 16);
		}
		if (action == 493) {
			stream.createFrame(75); // THIRD_CLICK
			stream.writeDWord(second);
			stream.method431(first);
			stream.writeShortA(clicked);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		if (action == 652) {
			boolean flag4 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, second, myPlayer.smallX[0], false, first);
			if (!flag4)
				flag4 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, second, myPlayer.smallX[0], false, first);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(156);
			stream.writeShortA(first + baseX);
			stream.method431(second + baseY);
			stream.method433(clicked);
		}
		if (action == 94) {
			boolean flag5 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, second, myPlayer.smallX[0], false, first);
			if (!flag5)
				flag5 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, second, myPlayer.smallX[0], false, first);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(181); // Cast spell Ground item
			stream.method431(second + baseY);
			stream.writeShort(clicked);
			stream.method431(first + baseX);
			stream.writeDWord(anInt1137);
		}
		if (action == 646) {
			stream.createFrame(185);
			stream.writeDWord(second);
			System.out.println(second);
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.scripts != null && rsInterface.scripts[0][0] == 5) {
					int configId = rsInterface.scripts[0][1];
					if (variousSettings[configId] != rsInterface.requiredValues[0]) {
						variousSettings[configId] = rsInterface.requiredValues[0];
						doConfigAction(configId);
						redrawTab = true;
					}
				}
			}
		}
		if (action == 225) {
			NPC class30_sub2_sub4_sub1_sub1_2 = npcArray[clicked];
			if (class30_sub2_sub4_sub1_sub1_2 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_2.smallY[0],
						myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_2.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt1226 += clicked;
				if (anInt1226 >= 85) {
					stream.createFrame(230);
					stream.writeWordBigEndian(239);
					anInt1226 = 0;
				}
				stream.createFrame(17);
				stream.method433(clicked);
			}
		}
		if (action == 965) {
			NPC class30_sub2_sub4_sub1_sub1_3 = npcArray[clicked];
			if (class30_sub2_sub4_sub1_sub1_3 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_3.smallY[0],
						myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_3.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt1134++;
				if (anInt1134 >= 96) {
					stream.createFrame(152);
					stream.writeWordBigEndian(88);
					anInt1134 = 0;
				}
				stream.createFrame(21);
				stream.writeShort(clicked);
			}
		}
		if (action == 413) {
			NPC class30_sub2_sub4_sub1_sub1_4 = npcArray[clicked];
			if (class30_sub2_sub4_sub1_sub1_4 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_4.smallY[0],
						myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_4.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(131); // Cast spell On Npc
				stream.method433(clicked);
				stream.writeDWord(anInt1137);
			}
		}
		if (action == 200) //Close bank interface
		{
			clearTopInterfaces();
//			RSInterface[] bankComponents = RSInterface.interfaceCache[174];
//			RSInterface mainInventory = bankComponents[50];
//			mainInventory.inventoryItemIds = tempInventoryItemIds[0];
//			mainInventory.inventoryStackSizes = tempInventoryStackSizes[0];
//			for (int x = 1; x < 9; x++)
//			{
//				RSInterface tabInventory = bankComponents[51 + (x - 1)];
//				tabInventory.inventoryItemIds = tempInventoryItemIds[x];
//				tabInventory.inventoryStackSizes = tempInventoryStackSizes[x];
//			}
			searchString = "";
		}
		if (action == 1025) {
			/*
			 * NPC class30_sub2_sub4_sub1_sub1_5 = npcArray[i1]; if
			 * (class30_sub2_sub4_sub1_sub1_5 != null) { EntityDef entityDef =
			 * class30_sub2_sub4_sub1_sub1_5.desc; if (entityDef.childrenIDs != null)
			 * entityDef = entityDef.method161(); if (entityDef != null) { String s9; if
			 * (entityDef.description != null) s9 = new String(entityDef.description); else
			 * s9 = "It's a " + entityDef.name + "."; pushMessage(s9, 0, ""); } }
			 */
			// EXAMINE NPC
			NPC class30_sub2_sub4_sub1_sub1_5 = npcArray[clicked];
			if (class30_sub2_sub4_sub1_sub1_5 != null) {
				EntityDef entityDef = class30_sub2_sub4_sub1_sub1_5.desc;
				if (entityDef.childrenIDs != null)
					entityDef = entityDef.method161();
				if (entityDef != null) {
					stream.createFrame(222);
					stream.writeShort(entityDef.npcID);
				}
			}
		}
		if (action == 900) {
			method66(clicked, second, first);
			stream.createFrame(252);
			stream.method433(clicked >> 14 & 0x7fff);
			stream.method431(second + baseY);
			stream.writeShortA(first + baseX);
		}
		if (action == 412) {
			NPC class30_sub2_sub4_sub1_sub1_6 = npcArray[clicked];
			if (class30_sub2_sub4_sub1_sub1_6 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_6.smallY[0],
						myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_6.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(72);
				stream.writeShortA(clicked);
			}
		}
		if (action == 365) {
			Player class30_sub2_sub4_sub1_sub2_3 = playerArray[clicked];
			if (class30_sub2_sub4_sub1_sub2_3 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_3.smallY[0],
						myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_3.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(249); // Cast Spell on Player
				stream.writeShortA(clicked);
				stream.writeDWord(anInt1137);
			}
		}
		if (action == 729) {
			Player class30_sub2_sub4_sub1_sub2_4 = playerArray[clicked];
			if (class30_sub2_sub4_sub1_sub2_4 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_4.smallY[0],
						myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_4.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(39);
				stream.method431(clicked);
			}
		}
		if (action == 577) {
			Player class30_sub2_sub4_sub1_sub2_5 = playerArray[clicked];
			if (class30_sub2_sub4_sub1_sub2_5 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_5.smallY[0],
						myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_5.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(139);
				stream.method431(clicked);
			}
		}
		if (action == 956 && method66(clicked, second, first)) {
			stream.createFrame(35); // Cast spell Object
			stream.method431(first + baseX);
			stream.writeDWord(anInt1137);
			stream.writeShortA(second + baseY);
			stream.method431(clicked >> 14 & 0x7fff);
		}
		if (action == 567) {
			boolean flag6 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, second, myPlayer.smallX[0], false, first);
			if (!flag6)
				flag6 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, second, myPlayer.smallX[0], false, first);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(23);
			stream.method431(second + baseY);
			stream.method431(clicked);
			stream.method431(first + baseX);
		}
		if (action == 867) {
			if ((clicked & 3) == 0)
				anInt1175++;
			if (anInt1175 >= 59) {
				stream.createFrame(200);
				stream.writeShort(25501); // TODO Whats this? hm
				anInt1175 = 0;
			}
			stream.createFrame(43); // CLICK_10
			stream.writeDWord(second);
			stream.writeShortA(clicked);
			stream.writeShortA(first);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		if (action == 543) {
			stream.createFrame(237); // MAGIC
			stream.writeShort(first);
			stream.writeShortA(clicked);
			stream.writeDWord(anInt1137);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		if (action == 606) {
			String s2 = menuActionName[i];
			int j2 = s2.indexOf("@whi@");
			if (j2 != -1)
				if (openInterfaceID == -1) {
					clearTopInterfaces();
					reportAbuseInput = s2.substring(j2 + 5).trim();
					canMute = false;
					reportAbuseInterfaceID = openInterfaceID = 201;
				} else {
					pushMessage("Please close the interface you have open before using 'report abuse'", 0, "");
				}
		}
		if (action == 491) {
			Player class30_sub2_sub4_sub1_sub2_6 = playerArray[clicked];
			if (class30_sub2_sub4_sub1_sub2_6 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_6.smallY[0],
						myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_6.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(14);
				// stream.method432(anInt1284);
				stream.writeShort(clicked); // Player id
				// stream.writeWord(anInt1285);
				stream.method431(anInt1283); // Item slot
			}
		}
		if (action == 639) {
			String s3 = menuActionName[i];
			int k2 = s3.indexOf("@whi@");
			if (k2 != -1) {
				long l4 = TextUtil.longForName(s3.substring(k2 + 5).trim());
				int k3 = -1;
				for (int i4 = 0; i4 < friendsCount; i4++) {
					if (friendsListAsLongs[i4] != l4)
						continue;
					k3 = i4;
					break;
				}

				if (k3 != -1 && friendsNodeIDs[k3] > 0) {
					redrawChatbox = true;
					inputDialogState = 0;
					messagePromptRaised = true;
					promptInput = "";
					friendsListAction = 3;
					aLong953 = friendsListAsLongs[k3];
					aString1121 = "Enter message to send to " + friendsList[k3];
				}
			}
		}
		if (action == 454) {
			stream.createFrame(41);
			stream.writeShort(clicked);
			stream.writeShortA(first);
			stream.writeDWord(second);
			atInventoryLoopCycle = 0;
			atInventoryInterface = second;
			atInventoryIndex = first;
			atInventoryInterfaceType = 2;
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface != null) {
				if (rsInterface.interfaceID == openInterfaceID)
					atInventoryInterfaceType = 1;
				if (rsInterface.interfaceID == backDialogID)
					atInventoryInterfaceType = 3;
			}
		}
		if (action == 478) {
			NPC class30_sub2_sub4_sub1_sub1_7 = npcArray[clicked];
			if (class30_sub2_sub4_sub1_sub1_7 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_7.smallY[0],
						myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_7.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				if ((clicked & 3) == 0)
					anInt1155++;
				if (anInt1155 >= 53) {
					stream.createFrame(85);
					stream.writeWordBigEndian(66);
					anInt1155 = 0;
				}
				stream.createFrame(18);
				stream.method431(clicked);
			}
		}
		if (action == 113) {
			method66(clicked, second, first);
			stream.createFrame(70);
			stream.method431(first + baseX);
			stream.writeShort(second + baseY);
			stream.method433(clicked >> 14 & 0x7fff);
		}
		if (action == 872) {
			method66(clicked, second, first);
			stream.createFrame(234);
			stream.method433(first + baseX);
			stream.writeShortA(clicked >> 14 & 0x7fff);
			stream.method433(second + baseY);
		}
		if (action == 502) {
			method66(clicked, second, first);
			stream.createFrame(132);
			stream.method433(first + baseX);
			stream.writeShort(clicked >> 14 & 0x7fff);
			stream.writeShortA(second + baseY);
		}
		if (action == 1125) {
			// EXAMINE ITEM PACKET
			/*
			 * ItemDef itemDef = ItemDef.forID(i1); RSInterface class9_4 =
			 * RSInterface.interfaceCache[k]; String s5; if(itemDef != null){ if (class9_4
			 * != null && class9_4.invStackSizes[j] >= 0x186a0){ s5 =
			 * class9_4.invStackSizes[j] + " x " + itemDef.name; pushMessage(s5, 0, "");
			 * }else{ stream.createFrame(220); stream.writeWord(i1); // ID } }
			 */

			ItemDef itemDef = ItemDef.forID(clicked);
			RSInterface rsInterface = RSInterface.getInterface(second);
			String s5;
			if (rsInterface != null && rsInterface.inventoryStackSizes[first] >= 0x186a0)
				s5 = rsInterface.inventoryStackSizes[first] + " x " + itemDef.name;
			else if (itemDef.description != null)
				s5 = new String(itemDef.description);
			else
				s5 = "It's a " + itemDef.name + ".";
			pushMessage(s5, 0, "", true);
		}
		if (action == 169) {
			stream.createFrame(185);
			stream.writeDWord(second);
			System.out.println(second);
			RSInterface rsInterface = RSInterface.getInterface(second);
			if (rsInterface.scripts != null && rsInterface.scripts[0][0] == 5) {
				int l2 = rsInterface.scripts[0][1];
				variousSettings[l2] = 1 - variousSettings[l2];
				doConfigAction(l2);
				redrawTab = true;
			}
		}
		if (action == 447) {
			itemSelected = 1;
			anInt1283 = first;
			anInt1284 = second;
			anInt1285 = clicked;
			selectedItemName = ItemDef.forID(clicked).name;
			spellSelected = 0;
			redrawTab = true;
			return;
		}
		if (action == 1226) {
			int j1 = clicked >> 14 & 0x7fff;
			ObjectDef class46 = ObjectDef.forID(j1);
			String s10;
			if (class46 != null) {
				if (class46.description != null)
					s10 = new String(class46.description);
				else
					s10 = "It's a " + class46.name + ".";
			} else {
				s10 = "Something went horribly wrong with this object! ID: " + j1 + " Contact an admin.";
			}
			if (s10.contains("RuneScape"))
				s10 = s10.replace("RuneScape", "/v/scape");
			pushMessage(s10, 0, "", true);
		}
		if (action == 244) {
			boolean flag7 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, second, myPlayer.smallX[0], false, first);
			if (!flag7)
				flag7 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, second, myPlayer.smallX[0], false, first);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(253);
			stream.method431(first + baseX);
			stream.method433(second + baseY);
			stream.writeShortA(clicked);
		}
		if (action == 1448) {
			ItemDef itemDef_1 = ItemDef.forID(clicked);
			String s6;
			if (itemDef_1.description != null)
				s6 = new String(itemDef_1.description);
			else
				s6 = "It's a " + itemDef_1.name + ".";
			pushMessage(s6, 0, "", true);
			// EXAMINE ITEM PACKET
			/*
			 * ItemDef itemDef_1 = ItemDef.forID(i1); if(itemDef_1 != null) {
			 * stream.createFrame(220); stream.writeWord(i1); // ID }
			 */
		}
		itemSelected = 0;
		spellSelected = 0;
		redrawTab = true;

	}

	private void checkTutorialIsland() {
		onTutorialIsland = 0;
		int j = (myPlayer.x >> 7) + baseX;
		int k = (myPlayer.y >> 7) + baseY;
		if (j >= 3053 && j <= 3156 && k >= 3056 && k <= 3136)
			onTutorialIsland = 1;
		if (j >= 3072 && j <= 3118 && k >= 9492 && k <= 9535)
			onTutorialIsland = 1;
		if (onTutorialIsland == 1 && j >= 3135 && j <= 3199 && k >= 3008 && k <= 3062)
			onTutorialIsland = 0;
	}

	public void run() {
		super.run();
	}

	private void build3dScreenMenu() {
		if (itemSelected == 0 && spellSelected == 0) {
			menuActionName[menuActionRow] = "Walk here";
			menuActionID[menuActionRow] = 519;
			menuActionCmd2[menuActionRow] = super.mouseX;
			menuActionCmd3[menuActionRow] = super.mouseY;
			menuActionRow++;
		}
		int j = -1;
		for (int k = 0; k < Model.anInt1687; k++) {
			int l = Model.anIntArray1688[k];
			int i1 = l & 0x7f;
			int j1 = l >> 7 & 0x7f;
			int k1 = l >> 29 & 3;
			int l1 = l >> 14 & 0x7fff;
			if (l == j)
				continue;
			j = l;
			if (k1 == 2 && worldController.method304(plane, i1, j1, l) >= 0) {
				ObjectDef class46 = ObjectDef.forID(l1);
				if (class46 == null)
					continue;
				if (class46.childrenIDs != null)
					class46 = class46.method580();
				if (itemSelected == 1) {
					menuActionName[menuActionRow] = "Use " + selectedItemName + " with @cya@" + class46.name;
					menuActionID[menuActionRow] = 62;
					menuActionCmd1[menuActionRow] = l;
					menuActionCmd2[menuActionRow] = i1;
					menuActionCmd3[menuActionRow] = j1;
					menuActionRow++;
				} else if (spellSelected == 1) {
					if ((spellUsableOn & 4) == 4) {
						menuActionName[menuActionRow] = spellTooltip + " @cya@" + class46.name;
						menuActionID[menuActionRow] = 956;
						menuActionCmd1[menuActionRow] = l;
						menuActionCmd2[menuActionRow] = i1;
						menuActionCmd3[menuActionRow] = j1;
						menuActionRow++;
					}
				} else {
					if (class46.actions != null) {
						for (int i2 = 4; i2 >= 0; i2--)
							if (class46.actions[i2] != null) {
								menuActionName[menuActionRow] = class46.actions[i2] + " @cya@" + class46.name;
								if (i2 == 0)
									menuActionID[menuActionRow] = 502;
								if (i2 == 1)
									menuActionID[menuActionRow] = 900;
								if (i2 == 2)
									menuActionID[menuActionRow] = 113;
								if (i2 == 3)
									menuActionID[menuActionRow] = 872;
								if (i2 == 4)
									menuActionID[menuActionRow] = 1062;
								menuActionCmd1[menuActionRow] = l;
								menuActionCmd2[menuActionRow] = i1;
								menuActionCmd3[menuActionRow] = j1;
								menuActionRow++;
							}

					}
					menuActionName[menuActionRow] = (myPrivilege < 2 ? "Examine @cya@" + class46.name
							: "Examine @cya@" + class46.name + " @gre@(@whi@" + l1 + "@gre@) (@whi@" + (i1 + baseX)
									+ "," + (j1 + baseY) + "@gre@)");
					menuActionID[menuActionRow] = 1226;
					menuActionCmd1[menuActionRow] = class46.type << 14;
					menuActionCmd2[menuActionRow] = i1;
					menuActionCmd3[menuActionRow] = j1;
					menuActionRow++;
				}
			}
			if (k1 == 1) {
				NPC npc = npcArray[l1];
				try {
					if (npc.desc.size == 1 && (npc.x & 0x7f) == 64 && (npc.y & 0x7f) == 64) {
						for (int j2 = 0; j2 < npcCount; j2++) {
							NPC npc2 = npcArray[npcIndices[j2]];
							if (npc2 != null && npc2 != npc && npc2.desc.size == 1 && npc2.x == npc.x
									&& npc2.y == npc.y)
								buildAtNPCMenu(npc2.desc, npcIndices[j2], j1, i1);
						}
						for (int l2 = 0; l2 < playerCount; l2++) {
							Player player = playerArray[playerIndices[l2]];
							if (player != null && player.x == npc.x && player.y == npc.y)
								buildAtPlayerMenu(i1, playerIndices[l2], player, j1);
						}
					}
					buildAtNPCMenu(npc.desc, l1, j1, i1);
				} catch (Exception e) {
				}
			}
			if (k1 == 0) {
				Player player = playerArray[l1];
				try {
					if ((player.x & 0x7f) == 64 && (player.y & 0x7f) == 64) {
						for (int k2 = 0; k2 < npcCount; k2++) {
							NPC npc2 = npcArray[npcIndices[k2]];
							if (npc2 != null && npc2.desc.size == 1 && npc2.x == player.x && npc2.y == player.y)
								buildAtNPCMenu(npc2.desc, npcIndices[k2], j1, i1);
						}

						for (int i3 = 0; i3 < playerCount; i3++) {
							Player player2 = playerArray[playerIndices[i3]];
							if (player2 != null && player2 != player && player2.x == player.x && player2.y == player.y)
								buildAtPlayerMenu(i1, playerIndices[i3], player2, j1);
						}

					}
					buildAtPlayerMenu(i1, l1, player, j1);
				} catch (Exception e) {

				}
			}
			if (k1 == 3) {
				NodeList class19 = groundArray[plane][i1][j1];
				if (class19 != null) {
					for (Item item = (Item) class19.getFirst(); item != null; item = (Item) class19.getNext()) {
						ItemDef itemDef = ItemDef.forID(item.ID);
						if (itemSelected == 1) {
							menuActionName[menuActionRow] = "Use " + selectedItemName + " with @lre@" + itemDef.name;
							menuActionID[menuActionRow] = 511;
							menuActionCmd1[menuActionRow] = item.ID;
							menuActionCmd2[menuActionRow] = i1;
							menuActionCmd3[menuActionRow] = j1;
							menuActionRow++;
						} else if (spellSelected == 1) {
							if ((spellUsableOn & 1) == 1) {
								menuActionName[menuActionRow] = spellTooltip + " @lre@" + itemDef.name;
								menuActionID[menuActionRow] = 94;
								menuActionCmd1[menuActionRow] = item.ID;
								menuActionCmd2[menuActionRow] = i1;
								menuActionCmd3[menuActionRow] = j1;
								menuActionRow++;
							}
						} else {
							for (int j3 = 4; j3 >= 0; j3--)
								if (itemDef.groundActions != null && itemDef.groundActions[j3] != null) {
									menuActionName[menuActionRow] = itemDef.groundActions[j3] + " @lre@" + itemDef.name;
									if (j3 == 0)
										menuActionID[menuActionRow] = 652;
									if (j3 == 1)
										menuActionID[menuActionRow] = 567;
									if (j3 == 2)
										menuActionID[menuActionRow] = 234;
									if (j3 == 3)
										menuActionID[menuActionRow] = 244;
									if (j3 == 4)
										menuActionID[menuActionRow] = 213;
									menuActionCmd1[menuActionRow] = item.ID;
									menuActionCmd2[menuActionRow] = i1;
									menuActionCmd3[menuActionRow] = j1;
									menuActionRow++;
								} else if (j3 == 2) {
									menuActionName[menuActionRow] = "Take @lre@" + itemDef.name;
									menuActionID[menuActionRow] = 234;
									menuActionCmd1[menuActionRow] = item.ID;
									if (item.ID == 1971) {
										menuActionName[menuActionRow] = "Remove @lre@" + itemDef.name;
									}
									menuActionCmd2[menuActionRow] = i1;
									menuActionCmd3[menuActionRow] = j1;
									menuActionRow++;
								}
							menuActionName[menuActionRow] = (myPrivilege < 2 ? "Examine @lre@" + itemDef.name
									: "Examine @lre@" + itemDef.name + " @gre@(@whi@" + item.ID + "@gre@)");
							menuActionID[menuActionRow] = 1448;
							menuActionCmd1[menuActionRow] = item.ID;
							menuActionCmd2[menuActionRow] = i1;
							menuActionCmd3[menuActionRow] = j1;
							menuActionRow++;
						}
					}

				}
			}
		}
	}

	@Override
	public void shutdown() {
		Signlink.reporterror = false;
		try {
			if (socketStream != null) {
				socketStream.close();
			}
		} catch (Exception _ex) {
		}
		socketStream = null;
		stopMidi();
		resetFade();
		setTint(0, 0);
		if (mouseDetection != null)
			mouseDetection.running = false;
		mouseDetection = null;
		if (midiPlayer != null) {
			midiPlayer = null;
		}
		onDemandFetcher.disable();
		onDemandFetcher = null;
		aStream_834 = null;
		stream = null;
		aStream_847 = null;
		inStream = null;
		regionIdArray = null;
		floorMapBytes = null;
		objectMapBytes = null;
		floorMapArray = null;
		objectMapArray = null;
		tileHeights = null;
		tileFlags = null;
		worldController = null;
		collisionMaps = null;
		anIntArrayArray901 = null;
		anIntArrayArray825 = null;
		bigX = null;
		bigY = null;
		aByteArray912 = null;
		tabImageProducer = null;
		leftFrame = null;
		topFrame = null;
		aRSImageProducer_1164 = null;
		gameScreenImageProducer = null;
		aRSImageProducer_1166 = null;
		aRSImageProducer_1123 = null;
		aRSImageProducer_1124 = null;
		aRSImageProducer_1125 = null;
		/* Null pointers for custom sprites */
		gameFrame = null;
		background = null;
		mascotInv = null;
		mascotChat = null;
		mapBack = null;
		statIcons = null;
		compass = null;
		hitMarks = null;
		headIcons = null;
		skullIcons = null;
		headIconsHint = null;
		crosses = null;
		mapDotItem = null;
		mapDotNPC = null;
		mapDotPlayer = null;
		mapDotFriend = null;
		mapDotTeam = null;
		mapScenes = null;
		mapFunctions = null;
		anIntArrayArray929 = null;
		playerArray = null;
		playerIndices = null;
		npcsAwaitingUpdate = null;
		aStreamArray895s = null;
		anIntArray840 = null;
		npcArray = null;
		npcIndices = null;
		groundArray = null;
		aClass19_1179 = null;
		projectiles = null;
		incompleteAnimables = null;
		menuActionCmd2 = null;
		menuActionCmd3 = null;
		menuActionID = null;
		menuActionCmd1 = null;
		menuActionName = null;
		variousSettings = null;
		anIntArray1072 = null;
		anIntArray1073 = null;
		aClass30_Sub2_Sub1_Sub1Array1140 = null;
		minimapImage = null;
		friendsList = null;
		friendsListAsLongs = null;
		friendsNodeIDs = null;
		ignoreListAsLongs = null;
		clanListAsLongs = null;
		clanListRights = null;
		aRSImageProducer_1110 = null;
		aRSImageProducer_1111 = null;
		aRSImageProducer_1107 = null;
		aRSImageProducer_1108 = null;
		aRSImageProducer_1109 = null;
		aRSImageProducer_1112 = null;
		aRSImageProducer_1113 = null;
		aRSImageProducer_1114 = null;
		aRSImageProducer_1115 = null;
		multiOverlay = null;
		nullLoader();
		ObjectDef.nullLoader();
		EntityDef.nullLoader();
		ItemDef.nullLoader();
		Flo.cache = null;
		IdentityKit.cache = null;
		RSInterface.interfaceCache = null;
		Animation.anims = null;
		SpotAnim.cache = null;
		SpotAnim.aMRUNodes_415 = null;
		Varp.cache = null;
		super.fullGameScreen = null;
		Player.mruNodes = null;
		Texture.nullLoader();
		WorldController.nullLoader();
		Model.nullLoader();
		Frame.nullLoader();
		System.gc();
	}

	Component getGameComponent() {
		if (Signlink.mainapp != null)
			return Signlink.mainapp;
		if (super.rsFrame != null)
			return super.rsFrame;
		else
			return this;
	}

	private void method73() {
		do {
			int j = readChar(-796);
			if (j == -1)
				break;
			if (openInterfaceID != -1 && openInterfaceID == reportAbuseInterfaceID) {
				if (j == 8 && reportAbuseInput.length() > 0)
					reportAbuseInput = reportAbuseInput.substring(0, reportAbuseInput.length() - 1);
				if ((j >= 97 && j <= 126 || j >= 65 && j <= 90 || j >= 48 && j <= 57 || j == 32 || j == 95)
						&& reportAbuseInput.length() < 12)
					reportAbuseInput += (char) j;
			} else if (messagePromptRaised) {
				if (j >= 32 && j <= 126 && promptInput.length() < 80) {
					promptInput += (char) j;
					redrawChatbox = true;
				}
				if (j == 8 && promptInput.length() > 0) {
					promptInput = promptInput.substring(0, promptInput.length() - 1);
					redrawChatbox = true;
				}
				if (j == 13 || j == 10) {
					messagePromptRaised = false;
					redrawChatbox = true;
					if (friendsListAction == 1) {
						long l = TextUtil.longForName(promptInput);
						addFriend(l);
					}
					if (friendsListAction == 2 && friendsCount > 0) {
						long l1 = TextUtil.longForName(promptInput);
						delFriend(l1);
					}
					if (friendsListAction == 3 && promptInput.length() > 0) {
						stream.createFrame(126);
						stream.writeWordBigEndian(0);
						int k = stream.currentOffset;
						stream.writeQWord(aLong953);
						TextInput.method526(promptInput, stream);
						stream.writeBytes(stream.currentOffset - k);
						promptInput = TextInput.processText(promptInput);
						// promptInput = Censor.doCensor(promptInput);
						pushMessage(promptInput, 6, TextUtil.fixName(TextUtil.nameForLong(aLong953)));
						if (privateChatMode == 2) {
							privateChatMode = 1;
							stream.createFrame(95);
							stream.writeWordBigEndian(publicChatMode);
							stream.writeWordBigEndian(privateChatMode);
							stream.writeWordBigEndian(tradeMode);
						}
					}
					if (friendsListAction == 4 && ignoreCount < 100) {
						long l2 = TextUtil.longForName(promptInput);
						addIgnore(l2);
					}
					if (friendsListAction == 5 && ignoreCount > 0) {
						long l3 = TextUtil.longForName(promptInput);
						delIgnore(l3);
					}
					if (friendsListAction == 6) {
						long l3 = TextUtil.longForName(promptInput);
						clanChatJoin(l3);
					}
				}
			} else if (inputDialogState == 1) {
				if (j >= 48 && j <= 57 && amountOrNameInput.length() < 10) {
					amountOrNameInput += (char) j;
					redrawChatbox = true;
				}
				if ((!amountOrNameInput.toLowerCase().contains("k") && !amountOrNameInput.toLowerCase().contains("m")
						&& !amountOrNameInput.toLowerCase().contains("b")) && (j == 107 || j == 109) || j == 98) {
					amountOrNameInput += (char) j;
					redrawChatbox = true;
				}
				if (j == 8 && amountOrNameInput.length() > 0) {
					amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
					redrawChatbox = true;
				}
				if (j == 13 || j == 10) {
					if (amountOrNameInput.length() > 0) {
						if (amountOrNameInput.toLowerCase().contains("k")) {
							amountOrNameInput = amountOrNameInput.replaceAll("k", "000");
						} else if (amountOrNameInput.toLowerCase().contains("m")) {
							amountOrNameInput = amountOrNameInput.replaceAll("m", "000000");
						} else if (amountOrNameInput.toLowerCase().contains("b")) {
							amountOrNameInput = amountOrNameInput.replaceAll("b", "000000000");
						}
						int amount = 0;
						try {
							amount = Integer.parseInt(amountOrNameInput);
						} catch (NumberFormatException ex) {
							amount = Integer.MAX_VALUE;
						}
						stream.createFrame(208);
						stream.writeDWord(amount);
					}
					inputDialogState = 0;
					redrawChatbox = true;
				}
			} else if (inputDialogState == 2) {
				if (TextInput.validChar((char) j) && amountOrNameInput.length() < 12) {
					amountOrNameInput += (char) j;
					redrawChatbox = true;
				}
				if (j == 8 && amountOrNameInput.length() > 0) {
					amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
					redrawChatbox = true;
				}
				if (j == 13 || j == 10) {
					if (amountOrNameInput.length() > 0) {
						stream.createFrame(60);
						stream.writeQWord(TextUtil.longForName(amountOrNameInput));
					}
					inputDialogState = 0;
					redrawChatbox = true;
				}
			} else if (inputDialogState >= 3 && inputDialogState <= 5) {
				final int limit = 40;
				if (TextInput.validChar((char) j) && amountOrNameInput.length() < limit) {
					amountOrNameInput += (char) j;
					redrawChatbox = true;
				}
				if (j == 8 && amountOrNameInput.length() > 0) {
					amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
					redrawChatbox = true;
				}
				if (j == 13 || j == 10) {
					inputDialogState = 0;
					redrawChatbox = true;
				}
				if ((TextInput.validChar((char) j) && amountOrNameInput.length() < limit)
						|| (j == 8 && amountOrNameInput.length() > 0)) {
					definitionSearch(amountOrNameInput);
					redrawChatbox = true;
				}
			} else if (backDialogID == -1) {
				if (TextInput.validChar((char) j) && inputString.length() < 80) {
					inputString += (char) j;
					redrawChatbox = true;
				}
				if (j == 8 && inputString.length() > 0) {
					inputString = inputString.substring(0, inputString.length() - 1);
					redrawChatbox = true;
				}
				if (j == 9) {
					pmQuickReply();
				}
				if ((j == 13 || j == 10) && inputString.length() > 0) {
					if (inputString.equalsIgnoreCase("::fps")) {
						fpsOn = !fpsOn;
					}
					if (inputString.startsWith("::search"))
					{
                        RSInterface bank = RSInterface.interfaceCache[174][50];
                        Arrays.fill(bankInvTemp, 0);
                        Arrays.fill(bankStackTemp, 0);
                        int bankSlot = 0;
                        for (int slot = 0; slot < bank.inventoryItemIds.length; slot++) {
                            if (bank.inventoryItemIds[slot] - 1 > 0) {
                                if (ItemDef.forID(bank.inventoryItemIds[slot] - 1).name.toLowerCase().contains("torag")) {
                                    bankInvTemp[bankSlot] = bank.inventoryItemIds[slot];
                                    bankStackTemp[bankSlot++] = bank.inventoryStackSizes[slot];
                                }
                            }
                        }
                        inputString = "";
						return;
					}
					if (myPrivilege >= 2) {
						/*
						 * if (inputString.equalsIgnoreCase("::noscale")) { pixelScaling = 1;
						 * rebuildFrameSize(SettingsManager.screenMode, frameWidth, frameHeight);} if
						 * (inputString.equalsIgnoreCase("::scale")) { pixelScaling = 2;
						 * rebuildFrameSize(SettingsManager.screenMode, frameWidth, frameHeight);}
						 */
						if (inputString.equalsIgnoreCase("::dumpclip"))
							onDemandFetcher.DumpMapClipping();
						if (inputString.equalsIgnoreCase("::data"))
							clientData = !clientData;
						if (inputString.equalsIgnoreCase("::itemsearch")) {
							inputDialogState = 3;
							definitionSearch(amountOrNameInput);
							redrawChatbox = true;
						} else if (inputString.equalsIgnoreCase("::npcsearch")) {
							inputDialogState = 4;
							definitionSearch(amountOrNameInput);
							redrawChatbox = true;
						} else if (inputString.equalsIgnoreCase("::objectsearch")) {
							inputDialogState = 5;
							definitionSearch(amountOrNameInput);
							redrawChatbox = true;
						}
					}
					if (inputString.startsWith("/"))
						inputString = "::" + inputString;
					if (inputString.startsWith("::")) {
						stream.createFrame(103);
						stream.writeWordBigEndian(inputString.length() - 1);
						stream.writeString(inputString.substring(2));
					} else {
						String s = inputString.toLowerCase();
						int j2 = 0;
						if (s.startsWith("yellow:")) {
							j2 = 0;
							inputString = inputString.substring(7);
						} else if (s.startsWith("red:")) {
							j2 = 1;
							inputString = inputString.substring(4);
						} else if (s.startsWith("green:")) {
							j2 = 2;
							inputString = inputString.substring(6);
						} else if (s.startsWith("cyan:")) {
							j2 = 3;
							inputString = inputString.substring(5);
						} else if (s.startsWith("purple:")) {
							j2 = 4;
							inputString = inputString.substring(7);
						} else if (s.startsWith("white:")) {
							j2 = 5;
							inputString = inputString.substring(6);
						} else if (s.startsWith("flash1:")) {
							j2 = 6;
							inputString = inputString.substring(7);
						} else if (s.startsWith("flash2:")) {
							j2 = 7;
							inputString = inputString.substring(7);
						} else if (s.startsWith("flash3:")) {
							j2 = 8;
							inputString = inputString.substring(7);
						} else if (s.startsWith("glow1:")) {
							j2 = 9;
							inputString = inputString.substring(6);
						} else if (s.startsWith("glow2:")) {
							j2 = 10;
							inputString = inputString.substring(6);
						} else if (s.startsWith("glow3:")) {
							j2 = 11;
							inputString = inputString.substring(6);
						} else if (s.startsWith(">")) {
							j2 = 2;
						}
						s = inputString.toLowerCase();
						int i3 = 0;
						if (s.startsWith("wave:")) {
							i3 = 1;
							inputString = inputString.substring(5);
						} else if (s.startsWith("wave2:")) {
							i3 = 2;
							inputString = inputString.substring(6);
						} else if (s.startsWith("shake:")) {
							i3 = 3;
							inputString = inputString.substring(6);
						} else if (s.startsWith("scroll:")) {
							i3 = 4;
							inputString = inputString.substring(7);
						} else if (s.startsWith("slide:")) {
							i3 = 5;
							inputString = inputString.substring(6);
						}
						stream.createFrame(4);
						stream.writeWordBigEndian(0);
						int j3 = stream.currentOffset;
						stream.method425(i3);
						stream.method425(j2);
						aStream_834.currentOffset = 0;
						TextInput.method526(inputString, aStream_834);
						stream.method441(0, aStream_834.buffer, aStream_834.currentOffset);
						stream.writeBytes(stream.currentOffset - j3);
						inputString = TextInput.processText(inputString);
						// inputString = Censor.doCensor(inputString);
						myPlayer.textSpoken = inputString;
						myPlayer.textColour = j2;
						myPlayer.textEffect = i3;
						myPlayer.textCycle = 150;
						if (myPlayer.ironMan == 1) {
							pushMessage(myPlayer.textSpoken, 2, "@irn@" + myPlayer.name, myPrivilege);
						} else if (myPlayer.ironMan == 2) {
							pushMessage(myPlayer.textSpoken, 2, "@hci@" + myPlayer.name, myPrivilege);
						} else if (myPlayer.ironMan == 3) {
							pushMessage(myPlayer.textSpoken, 2, "@ult@" + myPlayer.name, myPrivilege);
						} else {
							pushMessage(myPlayer.textSpoken, 2, myPlayer.name, myPrivilege);
						}
						/*
						 * if (myPrivilege == 2 || myPrivilege == 3) { pushMessage(myPlayer.textSpoken,
						 * 2, myPlayer.name, myPrivilege); } else if (myPrivilege == 1) {
						 * pushMessage(myPlayer.textSpoken, 2, myPlayer.name, myPrivilege); } else if
						 * (myPlayer.ironMan) { pushMessage(myPlayer.textSpoken, 2, "@irn@" +
						 * myPlayer.name, myPrivilege); } else { pushMessage(myPlayer.textSpoken, 2,
						 * myPlayer.name); }
						 */
						if (publicChatMode == 2) {
							publicChatMode = 3;
							stream.createFrame(95);
							stream.writeWordBigEndian(publicChatMode);
							stream.writeWordBigEndian(privateChatMode);
							stream.writeWordBigEndian(tradeMode);
						}
					}
					inputString = "";
					redrawChatbox = true;
				}
			}
		} while (true);
	}

	private String getClanPrefix() {
		RSInterface rsi = RSInterface.getInterface(489, 2);
		if (rsi != null && rsi.disabledText != null) {
			int index = rsi.disabledText.indexOf(':');
			if (index != -1) {
				return rsi.disabledText.substring(index + 2);
			}
		}
		return "";
	}

	private void buildChatMenu(final String chatName) {
		if (myPrivilege >= 0) {
			menuActionName[menuActionRow] = "Report abuse @whi@" + chatName;
			menuActionID[menuActionRow] = 606;
			menuActionRow++;
		}
		menuActionName[menuActionRow] = "Add ignore @whi@" + chatName;
		menuActionID[menuActionRow] = 42;
		menuActionRow++;
		menuActionName[menuActionRow] = "Add friend @whi@" + chatName;
		menuActionID[menuActionRow] = 337;
		menuActionRow++;
	}

	private void buildChatAreaMenu(int mouseYOffset) {
		if (messagePromptRaised || inputDialogState != 0 || aString844 != null || backDialogID != -1
				|| dialogID != -1) {
			return;
		}
		final Rectangle chatBounds = gameFrame.getChatBounds();
		final int drawX = (int) chatBounds.getX();
		final int drawY = (int) chatBounds.getY();
		final TextDrawingArea chatFont = regularFont;
		final int charHeight = chatFont.getBaseCharHeight();
		final int lineSpacing = gameFrame.chatLineSpacing();
		int messagesDisplayed = 0;
		for (int mI = 0; mI < messageDisplayLimit; mI++) {
			if (chatMessages[mI] == null)
				continue;
			String chatName = chatNames[mI];
			String chatMsg = chatMessages[mI];
			final int chatType = chatTypes[mI];
			final int chatRight = chatRights[mI];
			final boolean isFiltered = chatFiltered[mI];
			final int msgY = (((drawY + (int) chatBounds.getHeight())
					- (messagesDisplayed * (charHeight + lineSpacing))) - (lineSpacing * 2)) + chatScrollPos;
			if (chatName != null && (chatName.startsWith("@irn@") || chatName.startsWith("@hci@") || chatName.startsWith("@ult@"))) {
				chatName = chatName.substring(5);
			}
			if (chatName == null) {
				chatName = TextUtil.fixName(myUsername);
			}
			/**
			 * 0 - Game Messages 1 - ?? Forced public? 2 - Public 3 - Private Message
			 * Receive Normal 4 - Trade Request 5 - Private Message Login | Logout 6 -
			 * Private Message Sent 7 - Private Message Receive from Admin or Mod 8 - Duel &
			 * Challenge Requests 9 - Global 12 - Link process 16 - Clan messages
			 */
			switch (chatType) {
			case 0: // Game Messages
				if ((chatTypeView == 5 || chatTypeView == 0) && (gameMode == 0 || (gameMode == 1 && isFiltered))) {
					messagesDisplayed++;
				}
				break;
			case 1: // ?? Forced public?
			case 2: // Public
				if ((chatTypeView == 1 || chatTypeView == 0) && (chatType == 1 || publicChatMode == 0
						|| (publicChatMode == 1 && isFriendOrSelf(chatName)))) {
					messagesDisplayed++;
					if (chatName.equalsIgnoreCase(myPlayer.name)
							|| (mouseYOffset <= msgY - charHeight || mouseYOffset > msgY)) {
						continue;
					}
					buildChatMenu(chatName);
				}
				break;
			case 3: // Private Message Receive Normal
			case 7: // Private Message Receive from Admin or Mod
				// UN SPLIT CHAT
				if (((splitPrivateChat == 0 && chatTypeView == 0) || chatTypeView == 2) && (chatType == 7
						|| privateChatMode == 0 || (privateChatMode == 1 && isFriendOrSelf(chatName)))) {
					messagesDisplayed++;
					if (chatName.equalsIgnoreCase(myPlayer.name)
							|| (mouseYOffset <= msgY - charHeight || mouseYOffset > msgY)) {
						continue;
					}
					buildChatMenu(chatName);
				}
				break;
			case 4:// Trade Request
				if ((chatTypeView == 3 || chatTypeView == 0)
						&& (chatType == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(chatName)))) {
					messagesDisplayed++;
					if (chatName.equalsIgnoreCase(myPlayer.name)
							|| (mouseYOffset <= msgY - charHeight || mouseYOffset > msgY)) {
						continue;
					}
					menuActionName[menuActionRow] = "Accept trade @whi@" + chatName;
					menuActionID[menuActionRow] = 484;
					menuActionRow++;
				}
				break;
			case 5:// Private Message Login | Logout
				if (((splitPrivateChat == 0 && chatTypeView == 0) || chatTypeView == 2) && privateChatMode < 2) {
					messagesDisplayed++;
				}
				break;
			case 6:// Private Message Sent
				if (((splitPrivateChat == 0 && chatTypeView == 0) || chatTypeView == 2) && privateChatMode < 2) {
					messagesDisplayed++;
				}
				break;
			case 8:// Duel & Challenge Requests
				if ((chatTypeView == 3 || chatTypeView == 0)
						&& (duelMode == 0 || (duelMode == 1 && isFriendOrSelf(chatName)))) {
					messagesDisplayed++;
					if (chatName.equalsIgnoreCase(myPlayer.name)
							|| (mouseYOffset <= msgY - charHeight || mouseYOffset > msgY)) {
						continue;
					}
					menuActionName[menuActionRow] = "Accept challenge @whi@" + chatName;
					menuActionID[menuActionRow] = 6;
					menuActionRow++;
				}
				break;
			case 9:// Global
				if (((chatTypeView == 0 && globalMode == 0) || chatTypeView == 9)
						&& (globalMode == 0 || globalMode == 1)) {
					messagesDisplayed++;
					if (chatName.equalsIgnoreCase(myPlayer.name)
							|| (mouseYOffset <= msgY - charHeight || mouseYOffset > msgY)) {
						continue;
					}
					buildChatMenu(chatName);
				}
				break;
			case 12:// Link process
				if ((chatTypeView == 5 || chatTypeView == 0) && (gameMode == 0 || (gameMode == 1 && isFiltered))) {
					messagesDisplayed++;
					if (chatName.equalsIgnoreCase(myPlayer.name)
							|| (mouseYOffset <= msgY - charHeight || mouseYOffset > msgY)) {
						continue;
					}
					String prefix = chatPrefix[mI];
					menuActionName[menuActionRow] = "Go-to @lre@" + prefix;
					menuActionID[menuActionRow] = 927;
					menuActionCmd3[menuActionRow] = mI;
					menuActionRow++;
				}
				break;
			case 16:// Clan chat
				if ((chatTypeView == 16 || (chatTypeView == 0 && clanChatMode == 0))
						&& (clanChatMode == 0 || clanChatMode == 1)) {
					messagesDisplayed++;
					if (chatName.equalsIgnoreCase(myPlayer.name)
							|| (mouseYOffset <= msgY - charHeight || mouseYOffset > msgY)) {
						continue;
					}
					buildChatMenu(chatName);
				}
				break;
			}
		}
	}

	private void drawFriendsListOrWelcomeScreen(RSInterface class9) {
		int j = class9.contentType;
		if (j >= 1 && j <= 100 || j >= 701 && j <= 800) {
			if (j == 1 && anInt900 == 0) {
				class9.disabledText = "Loading friend list";
				class9.actionType = 0;
				return;
			}
			if (j == 1 && anInt900 == 1) {
				class9.disabledText = "Connecting to friendserver";
				class9.actionType = 0;
				return;
			}
			if (j == 2 && anInt900 != 2) {
				class9.disabledText = "Please wait...";
				class9.actionType = 0;
				return;
			}
			int k = friendsCount;
			if (anInt900 != 2)
				k = 0;
			if (j > 700)
				j -= 601;
			else
				j--;
			if (j >= k) {
				class9.disabledText = "";
				class9.actionType = 0;
				return;
			} else {
				class9.disabledText = friendsList[j];
				class9.actionType = 1;
				return;
			}
		}
		if (j >= 101 && j <= 200 || j >= 801 && j <= 900) {
			int l = friendsCount;
			if (anInt900 != 2)
				l = 0;
			if (j > 800)
				j -= 701;
			else
				j -= 101;
			if (j >= l) {
				class9.disabledText = "";
				class9.actionType = 0;
				return;
			}
			if (friendsNodeIDs[j] == 0)
				class9.disabledText = "@red@Offline";
			else if (friendsNodeIDs[j] == nodeID)
				class9.disabledText = "@gre@Online"/* + (friendsNodeIDs[j] - 9) */;
			else
				class9.disabledText = "@red@Offline"/* + (friendsNodeIDs[j] - 9) */;
			class9.actionType = 1;
			return;
		}
		if (j == 203) {
			int i1 = friendsCount;
			if (anInt900 != 2)
				i1 = 0;
			class9.scrollMax = i1 * 15 + 20;
			if (class9.scrollMax <= class9.height)
				class9.scrollMax = class9.height + 1;
			return;
		}
		if (j >= 401 && j <= 500) {
			if ((j -= 401) == 0 && anInt900 == 0) {
				class9.disabledText = "Loading ignore list";
				class9.actionType = 0;
				return;
			}
			if (j == 1 && anInt900 == 0) {
				class9.disabledText = "Please wait...";
				class9.actionType = 0;
				return;
			}
			int j1 = ignoreCount;
			if (anInt900 == 0)
				j1 = 0;
			if (j >= j1) {
				class9.disabledText = "";
				class9.actionType = 0;
				return;
			} else {
				class9.disabledText = TextUtil.fixName(TextUtil.nameForLong(ignoreListAsLongs[j]));
				class9.actionType = 1;
				return;
			}
		}
		if (j == 503) {
			class9.scrollMax = ignoreCount * 15 + 20;
			if (class9.scrollMax <= class9.height)
				class9.scrollMax = class9.height + 1;
			return;
		}
		if (j == 327) {
			class9.modelRotationX = 150;
			class9.modelRotationY = (int) (Math.sin((double) loopCycle / 40D) * 256D) & 0x7ff;
			if (aBoolean1031) {
				for (int k1 = 0; k1 < 7; k1++) {
					int l1 = anIntArray1065[k1];
					if (l1 >= 0 && !IdentityKit.cache[l1].method537())
						return;
				}

				aBoolean1031 = false;
				Model aclass30_sub2_sub4_sub6s[] = new Model[7];
				int i2 = 0;
				for (int j2 = 0; j2 < 7; j2++) {
					int k2 = anIntArray1065[j2];
					if (k2 >= 0)
						aclass30_sub2_sub4_sub6s[i2++] = IdentityKit.cache[k2].method538();
				}

				Model model = new Model(i2, aclass30_sub2_sub4_sub6s);
				for (int l2 = 0; l2 < 5; l2++)
					if (anIntArray990[l2] != 0) {
						model.method476(anIntArrayArray1003[l2][0], anIntArrayArray1003[l2][anIntArray990[l2]]);
						if (l2 == 1)
							model.method476(anIntArray1204[0], anIntArray1204[anIntArray990[l2]]);
					}

				model.method469();
				model.method470(Animation.anims[myPlayer.standAnim].anIntArray353[0]);
				model.method479(64, 850, -30, -50, -30, true);
				class9.defaultMediaType = 5;
				class9.defaultMedia = 0;
				RSInterface.method208(aBoolean994, model);
			}
			return;
		}
		if (j == 328) {
			RSInterface rsInterface = class9;
			int verticleTilt = 150;
			int animationSpeed = (int) (Math.sin((double) loopCycle / 40D) * 256D) & 0x7ff;
			rsInterface.modelRotationX = verticleTilt;
			rsInterface.modelRotationY = animationSpeed;
			if (aBoolean1031) {
				Model characterDisplay = myPlayer.method452();
				for (int l2 = 0; l2 < 5; l2++)
					if (anIntArray990[l2] != 0) {
						characterDisplay.method476(anIntArrayArray1003[l2][0],
								anIntArrayArray1003[l2][anIntArray990[l2]]);
						if (l2 == 1)
							characterDisplay.method476(anIntArray1204[0], anIntArray1204[anIntArray990[l2]]);
					}
				int staticFrame = myPlayer.standAnim;
				characterDisplay.method469();
				characterDisplay.method470(Animation.anims[staticFrame].anIntArray353[0]);
				// characterDisplay.method479(64, 850, -30, -50, -30, true);
				rsInterface.defaultMediaType = 5;
				rsInterface.defaultMedia = 0;
				RSInterface.method208(aBoolean994, characterDisplay);
			}
			return;
		}
		if (j == 324) {
			if (aClass30_Sub2_Sub1_Sub1_931 == null) {
				aClass30_Sub2_Sub1_Sub1_931 = class9.disabledSprite;
				aClass30_Sub2_Sub1_Sub1_932 = class9.enabledSprite;
			}
			if (aBoolean1047) {
				class9.disabledSprite = aClass30_Sub2_Sub1_Sub1_932;
				return;
			} else {
				class9.disabledSprite = aClass30_Sub2_Sub1_Sub1_931;
				return;
			}
		}
		if (j == 325) {
			if (aClass30_Sub2_Sub1_Sub1_931 == null) {
				aClass30_Sub2_Sub1_Sub1_931 = class9.disabledSprite;
				aClass30_Sub2_Sub1_Sub1_932 = class9.enabledSprite;
			}
			if (aBoolean1047) {
				class9.disabledSprite = aClass30_Sub2_Sub1_Sub1_931;
				return;
			} else {
				class9.disabledSprite = aClass30_Sub2_Sub1_Sub1_932;
				return;
			}
		}
		if (j == 600) {
			class9.disabledText = reportAbuseInput;
			if (loopCycle % 20 < 10) {
				class9.disabledText += "|";
				return;
			} else {
				class9.disabledText += " ";
				return;
			}
		}
		if (j == 620)
			if (myPrivilege >= 1) {
				if (canMute) {
					class9.disabledColor = 0xff0000;
					class9.disabledText = "Moderator option: Mute player for 48 hours: <ON>";
				} else {
					class9.disabledColor = 0xffffff;
					class9.disabledText = "Moderator option: Mute player for 48 hours: <OFF>";
				}
			} else {
				class9.disabledText = "";
			}
		if (j == 650 || j == 655)
			if (anInt1193 != 0) {
				String s;
				if (daysSinceLastLogin == 0)
					s = "earlier today";
				else if (daysSinceLastLogin == 1)
					s = "yesterday";
				else
					s = daysSinceLastLogin + " days ago";
				class9.disabledText = "You last logged in " + s + " from: " + Signlink.dns;
			} else {
				class9.disabledText = "";
			}
		if (j == 651) {
			if (unreadMessages == 0) {
				class9.disabledText = "0 unread messages";
				class9.disabledColor = 0xffff00;
			}
			if (unreadMessages == 1) {
				class9.disabledText = "1 unread message";
				class9.disabledColor = 65280;
			}
			if (unreadMessages > 1) {
				class9.disabledText = unreadMessages + " unread messages";
				class9.disabledColor = 65280;
			}
		}
		if (j == 652)
			if (daysSinceRecovChange == 201) {
				if (membersInt == 1)
					class9.disabledText = "@yel@This is a non-members world: @whi@Since you are a member we";
				else
					class9.disabledText = "";
			} else if (daysSinceRecovChange == 200) {
				class9.disabledText = "You have not yet set any password recovery questions.";
			} else {
				String s1;
				if (daysSinceRecovChange == 0)
					s1 = "Earlier today";
				else if (daysSinceRecovChange == 1)
					s1 = "Yesterday";
				else
					s1 = daysSinceRecovChange + " days ago";
				class9.disabledText = s1 + " you changed your recovery questions";
			}
		if (j == 653)
			if (daysSinceRecovChange == 201) {
				if (membersInt == 1)
					class9.disabledText = "@whi@recommend you use a members world instead. You may use";
				else
					class9.disabledText = "";
			} else if (daysSinceRecovChange == 200)
				class9.disabledText = "We strongly recommend you do so now to secure your account.";
			else
				class9.disabledText = "If you do not remember making this change then cancel it immediately";
		if (j == 654) {
			if (daysSinceRecovChange == 201)
				if (membersInt == 1) {
					class9.disabledText = "@whi@this world but member benefits are unavailable whilst here.";
					return;
				} else {
					class9.disabledText = "";
					return;
				}
			if (daysSinceRecovChange == 200) {
				class9.disabledText = "Do this from the 'account management' area on our front webpage";
				return;
			}
			class9.disabledText = "Do this from the 'account management' area on our front webpage";
		}
		if (j >= 10000 && j <= 10022) {
			j -= 10000;
			class9.disabledText = setSkillHover(j);
			return;
		}
		if (j == 20000) {
			class9.scrollMax = clanCount * 15 + 20;
			if (class9.scrollMax <= class9.height)
				class9.scrollMax = class9.height + 1;
			return;
		}
		if (j >= 20001 && j <= 20101) {
			int k = clanCount;
			j -= 20001;
			RSInterface chatIcon = RSInterface.getInterface(class9.interfaceID, 21 + j);// RSInterface.interfaceCache[class9.interfaceHash-101];
			if (j >= k) {
				chatIcon.disabledSprite = chatIcon.enabledSprite = null;
				class9.disabledText = "";
				class9.width = 0;
				class9.actionType = 0;
				return;
			} else {
				int clanMemberRights = clanListRights[j];
				String chatMember = TextUtil.nameForLong(clanListAsLongs[j]);
				switch (clanMemberRights) {
				case 0:
					if (isFriendOrSelf(chatMember) && !chatMember.equalsIgnoreCase(myPlayer.name)) {
						chatIcon.disabledSprite = chatIcon.enabledSprite = SpriteLoader.getSprite("clanchat", 0);
					} else {
						chatIcon.disabledSprite = chatIcon.enabledSprite = null;
					}
					break;
				case 1:
					chatIcon.disabledSprite = chatIcon.enabledSprite = SpriteLoader.getSprite("clanchat", 1);
					break;
				case 2:
					chatIcon.disabledSprite = chatIcon.enabledSprite = SpriteLoader.getSprite("clanchat", 3);
					break;
				case 3:
					chatIcon.disabledSprite = chatIcon.enabledSprite = SpriteLoader.getSprite("clanchat", 2);
					break;
				}
				class9.disabledText = TextUtil.fixName(chatMember);
				int nameWidth = 0;
				if (class9.getFont() instanceof TextDrawingArea) {
					nameWidth = ((TextDrawingArea) class9.getFont()).getTextWidth(class9.disabledText);
				} else if (class9.getFont() instanceof RSFont) {
					nameWidth = ((RSFont) class9.getFont()).getTextWidth(class9.disabledText);
				}
				class9.width = nameWidth;
				class9.actionType = 1;
				return;
			}
		}
	}

	public int getXPForLevel(int level) {
		int points = 0;
		int output = 0;
		for (int lvl = 1; lvl <= level; lvl++) {
			points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0));
			if (lvl >= level) {
				return output;
			}
			output = (int) Math.floor(points / 4);
		}
		return 0;
	}

	private final static int[] skillID = { 0, 3, 14, 2, 16, 13, 1, 15, 10, 4, 17, 7, 5, 12, 11, 6, 9, 8, 20, 18, 19, 22,
			21 };

	public String setSkillHover(int skill) {
		String message = "";
		message += TextUtil.fixName(Skills.skillNames[skillID[skill]]) + ": " + currentStats[skillID[skill]] + "/"
				+ maxStats[skillID[skill]] + "\\n";
		message += "Current XP: " + NumberFormat.getIntegerInstance().format(currentExp[skillID[skill]]) + "\\n";
		if (maxStats[skillID[skill]] < 99) {
			message += "Next level: "
					+ NumberFormat.getIntegerInstance().format(getXPForLevel(maxStats[skillID[skill]] + 1)) + "\\n";
			message += "Remainder: " + NumberFormat.getIntegerInstance()
					.format((getXPForLevel(maxStats[skillID[skill]] + 1) - currentExp[skillID[skill]]));
		} else {
			if (currentExp[skillID[skill]] < 200000000) {
				message += "Remainder: "
						+ NumberFormat.getIntegerInstance().format((200000000 - currentExp[skillID[skill]])) + "\\n";
			} else {
				message += "Max EXP Reached\\n";
			}
			message += "Max Level Reached";
		}
		return message;
	}

	private void drawSplitPrivateChat() {
		if (splitPrivateChat == 0)
			return;
		TextDrawingArea textDrawingArea = regularFont;
		int i = 0;
		/*
		 * if(anInt1104 != 0) i = 1;
		 */
		if (!serverMessage.isEmpty()) {
			i++;
		}
		if (systemUpdateTime != 0) {
			i++;
		}
		if (i > 2) {
			i = 2;
		}
		for (int j = 0; j < 100; j++)
			if (chatMessages[j] != null) {
				int k = chatTypes[j];
				String s = chatNames[j];
				int chatRight = chatRights[j];
				int userIconType = 0;
				/*
				 * if (s1 != null && s1.startsWith("@cr1@")) { s1 = s1.substring(5);
				 * userIconType = 1; } else if (s1 != null && s1.startsWith("@cr2@")) { s1 =
				 * s1.substring(5); userIconType = 2; } else if (s1 != null &&
				 * s1.startsWith("@cr3@")) { s1 = s1.substring(5); userIconType = 3; } else
				 */
				if (s != null && (s.startsWith("@irn@") || s.startsWith("@hci@") || s.startsWith("@ult@"))) {
					s = s.substring(5);
					userIconType = 4;
				}
				/*
				 * byte byte1 = 0; if(s != null && s.startsWith("@cr1@")) { s = s.substring(5);
				 * byte1 = 1; } if(s != null && s.startsWith("@cr2@")) { s = s.substring(5);
				 * byte1 = 2; } if(s != null && s.startsWith("@cr3@")) { s = s.substring(5);
				 * byte1 = 3; } if (s != null && s.startsWith("@irn@")) { s = s.substring(5);
				 * byte1 = 4; }
				 */
				if ((k == 3 || k == 7)
						&& (k == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s))) {
					int l = 329 - i * 13;
					if (frameMode != ScreenMode.FIXED) {
						l = frameHeight - 170 - i * 13;
					}
					int k1 = 4;
					textDrawingArea.method385(0, "From", l, k1);
					textDrawingArea.method385(65535, "From", l - 1, k1);
					k1 += textDrawingArea.getTextWidth("From ");
					// k1 += drawUserIcon(byte1, k1, l - 12);
					if (chatRight > 0) {
						k1 += drawUserIcon(chatRight, k1 + 1, l - 12);
					}
					if (userIconType > 0) {
						k1 += drawUserIcon(userIconType, k1 + 1, l - 12);
					}
					textDrawingArea.method385(0, s + ": " + chatMessages[j], l, k1);
					textDrawingArea.method385(65535, s + ": " + chatMessages[j], l - 1, k1);
					if (++i >= 5)
						return;
				}
				if (k == 5 && privateChatMode < 2) {
					int i1 = 329 - i * 13;
					if (frameMode != ScreenMode.FIXED) {
						i1 = frameHeight - 170 - i * 13;
					}
					textDrawingArea.method385(0, chatMessages[j], i1, 4);
					textDrawingArea.method385(65535, chatMessages[j], i1 - 1, 4);
					if (++i >= 5)
						return;
				}
				if (k == 6 && privateChatMode < 2) {
					int j1 = 329 - i * 13;
					if (frameMode != ScreenMode.FIXED) {
						j1 = frameHeight - 170 - i * 13;
					}
					textDrawingArea.method385(0, "To " + s + ": " + chatMessages[j], j1, 4);
					textDrawingArea.method385(65535, "To " + s + ": " + chatMessages[j], j1 - 1, 4);
					if (++i >= 5)
						return;
				}
			}

	}

	public void pushMessage(String message, int i, String name) {
		pushMessage(i, "", name, message, 0, false);
	}

	public void pushMessage(String message, int i, String name, boolean isFiltered) {
		pushMessage(i, "", name, message, 0, isFiltered);
	}

	public void pushMessage(String message, int i, String name, int rights) {
		pushMessage(i, "", name, message, rights, false);
	}

	public void pushMessage(int channel, String prefix, String chatName, String message, int rights) {
		pushMessage(channel, prefix, chatName, message, rights, false);
	}

	public void pushMessage(int channel, String prefix, String name, String message, int rights, boolean isFiltered) {
		if (channel == 0 && dialogID != -1) {
			aString844 = message;
			super.clickMode3 = 0;
		}
		if (backDialogID == -1)
			redrawChatbox = true;
		for (int j = 499; j > 0; j--) {
			chatTypes[j] = chatTypes[j - 1];
			chatNames[j] = chatNames[j - 1];
			chatMessages[j] = chatMessages[j - 1];
			chatRights[j] = chatRights[j - 1];
			chatFiltered[j] = chatFiltered[j - 1];
			chatPrefix[j] = chatPrefix[j - 1];
		}
		chatTypes[0] = channel;
		chatNames[0] = name;
		chatMessages[0] = message;
		chatRights[0] = rights;
		chatFiltered[0] = isFiltered;
		chatPrefix[0] = prefix;
	}

	public static void setTab(int id) {
		if (tabInterfaceIDs[id] != -1) {
			redrawTab = true;
			tabID = id;
			redrawTabIcons = true;
		}
	}

	public void rightClickMapArea() {
		final boolean fixed = frameMode == ScreenMode.FIXED;
		if (super.mouseX >= frameWidth - (fixed ? 225 : 215) && super.mouseX <= frameWidth - (fixed ? 185 : 172)
				&& super.mouseY > (fixed ? 0 : 0) && super.mouseY < (fixed ? 38 : 38)) {
			menuActionName[1] = "Face north";
			menuActionID[1] = 1010;
			menuActionRow = 2;
		}
		if (SettingsManager.orbsEnabled) {
			boolean right = SettingsManager.orbsOnRight;
			Point cXPf = new Point(248, 18);
			Point cXPr = new Point(240, 22);
			Point cRf = new Point(!right ? 230 : 75, 122);
			Point cRr = new Point(!right ? 224 : 57, 126);
			if (super.mouseX >= frameWidth - (fixed ? cXPf.x : cXPr.x)
					&& super.mouseX <= frameWidth - ((fixed ? cXPf.x : cXPr.x) - 22)
					&& super.mouseY >= (fixed ? cXPf.y : cXPr.y) && super.mouseY <= ((fixed ? cXPf.y : cXPr.y) + 28)) {
				if (XPDrop.isEnabled()) {
					menuActionName[4] = "Disable @lre@XP Drops";
					menuActionID[4] = 1011;
					menuActionName[3] = "Position: @lre@Left";
					menuActionID[3] = 1012;
					menuActionName[2] = "Position: @lre@Center";
					menuActionID[2] = 1013;
					menuActionName[1] = "Position: @lre@Right";
					menuActionID[1] = 1014;
					menuActionRow = 5;
				} else {
					menuActionName[1] = "Enable @lre@XP Drops";
					menuActionID[1] = 1011;
					menuActionRow = 2;
				}
			} else if (super.mouseX >= frameWidth - (fixed ? cRf.x : cRr.x)
					&& super.mouseX <= frameWidth - ((fixed ? cRf.x : cRr.x) - 56)
					&& super.mouseY >= (fixed ? cRf.y : cRr.y) && super.mouseY <= ((fixed ? cRf.y : cRr.y) + 28)) {
				menuActionName[2] = "Toggle @lre@Run";
				menuActionID[2] = 1050;
				menuActionName[1] = "Toggle @lre@" + (right ? "Left" : "Right");
				menuActionID[1] = 1051;
				menuActionRow = 3;
			}
		}
	}

	public void processRightClick() {
		if (activeInterfaceType != 0) {
			return;
		}
		menuActionName[0] = "Cancel";
		menuActionID[0] = 1107;
		menuActionRow = 1;
		if (fullscreenInterfaceID != -1) {
			hoverInterface = 0;
			anInt1315 = 0;
			buildInterfaceMenu(8, RSInterface.interfaceCache[fullscreenInterfaceID], super.mouseX, 8, super.mouseY, 0,
					-1);
			if (hoverInterface != anInt1026) {
				anInt1026 = hoverInterface;
			}
			if (anInt1315 != anInt1129) {
				anInt1129 = anInt1315;
			}
			return;
		}
		buildSplitPrivateChatMenu();
		hoverInterface = 0;
		anInt1315 = 0;
		if (frameMode == ScreenMode.FIXED) {
			if (super.mouseX > 4 && super.mouseY > 4 && super.mouseX < 516 && super.mouseY < 338)
				if (openInterfaceID != -1)
					buildInterfaceMenu(4, RSInterface.interfaceCache[openInterfaceID], super.mouseX, 4, super.mouseY, 0,
							-1);
				else
					build3dScreenMenu();
		} else if (frameMode != ScreenMode.FIXED) {
			if (canClick()) {
				final int x = (frameWidth / 2) - 356, y = (frameHeight / 2) - 230;
				final int width = 516, height = 338;
				boolean inInterfaceArea = mouseInRegion(x, y, x + width, y + height);
				if (inInterfaceArea && openInterfaceID != -1) {
					buildInterfaceMenu(x, RSInterface.interfaceCache[openInterfaceID], super.mouseX, y, super.mouseY, 0,
							-1);
				} else {
					build3dScreenMenu();
				}
			}
		}
		if (hoverInterface != anInt1026) {
			anInt1026 = hoverInterface;
		}
		if (anInt1315 != anInt1129) {
			anInt1129 = anInt1315;
		}
		hoverInterface = 0;
		anInt1315 = 0;
		if (frameMode == ScreenMode.FIXED) {
			if (super.mouseX > frameWidth - 218 && super.mouseY > frameHeight - 298 && super.mouseX < frameWidth - 25
					&& super.mouseY < frameHeight - 35) {
				if (invOverlayInterfaceID != -1)
					buildInterfaceMenu(frameWidth - 218, RSInterface.interfaceCache[invOverlayInterfaceID],
							super.mouseX, frameHeight - 298, super.mouseY, 0, -1);
				else if (tabInterfaceIDs[tabID] != -1)
					buildInterfaceMenu(frameWidth - 218, RSInterface.interfaceCache[tabInterfaceIDs[tabID]],
							super.mouseX, frameHeight - 298, super.mouseY, 0, -1);
			}
		} else if (frameMode != ScreenMode.FIXED) {
			int y = frameWidth > smallTabThreshold ? 37 : 74;
			if (frameWidth > smallTabThreshold) {
				if (super.mouseX > frameWidth - 197 && super.mouseY > frameHeight - y - 267
						&& super.mouseX < frameWidth - 7 && super.mouseY < frameHeight - y - 7 && showTabComponents) {
					if (invOverlayInterfaceID != -1) {
						buildInterfaceMenu(frameWidth - 197, RSInterface.interfaceCache[invOverlayInterfaceID],
								super.mouseX, frameHeight - 304, super.mouseY, 0, -1);
					} else if (tabInterfaceIDs[tabID] != -1) {
						buildInterfaceMenu(frameWidth - 197, RSInterface.interfaceCache[tabInterfaceIDs[tabID]],
								super.mouseX, frameHeight - 304, super.mouseY, 0, -1);
					}
				}
			}
			if (frameWidth <= smallTabThreshold) {
				if (super.mouseX > frameWidth - 211 && super.mouseY > frameHeight - y - 267
						&& super.mouseX < frameWidth - 7 && super.mouseY < frameHeight - y - 7 && showTabComponents) {
					if (invOverlayInterfaceID != -1) {
						buildInterfaceMenu(frameWidth - 211, RSInterface.interfaceCache[invOverlayInterfaceID],
								super.mouseX, frameHeight - y - 267, super.mouseY, 0, -1);
					} else if (tabInterfaceIDs[tabID] != -1) {
						buildInterfaceMenu(frameWidth - 211, RSInterface.interfaceCache[tabInterfaceIDs[tabID]],
								super.mouseX, frameHeight - y - 267, super.mouseY, 0, -1);
					}
				}
			}
		}
		if (hoverInterface != anInt1048) {
			redrawTab = true;
			redrawTabIcons = true;
			anInt1048 = hoverInterface;
		}
		if (anInt1315 != anInt1044) {
			redrawTab = true;
			redrawTabIcons = true;
			anInt1044 = anInt1315;
		}
		hoverInterface = 0;
		anInt1315 = 0;
		// chat menus
		if (showChatComponents) {
			final int chatY = gameFrame.getChatOffsetY(true);
			final Rectangle chatBounds = gameFrame.getChatBounds();
			final int chatInterfaceOffset = 11;
			if (super.mouseX > 0 && super.mouseX < (int) chatBounds.getWidth() && super.mouseY > chatY
					&& super.mouseY < (chatY + (int) chatBounds.getMaxY() + chatInterfaceOffset)) {
				if (backDialogID != -1) {
					int yOffset = chatY + chatBounds.y + chatInterfaceOffset;
					buildInterfaceMenu((int) chatBounds.getX(), RSInterface.interfaceCache[backDialogID], super.mouseX,
							yOffset, super.mouseY, 0, -1);
				} else {
					buildChatAreaMenu(super.mouseY - chatY);
				}
			}
		}
		if (backDialogID != -1 && hoverInterface != anInt1039) {
			redrawChatbox = true;
			anInt1039 = hoverInterface;
		}
		if (backDialogID != -1 && anInt1315 != anInt1500) {
			redrawChatbox = true;
			anInt1500 = anInt1315;
		}
		if (super.mouseX > 0 && super.mouseY > frameHeight - 165 && super.mouseX < 459 && super.mouseY < frameHeight) {
			gameFrame.rightClickChatButtons(this);
		} else if (super.mouseX > frameWidth - 249 && super.mouseY < 168) {
			rightClickMapArea();
		}
		boolean flag = false;
		while (!flag) {
			flag = true;
			for (int j = 0; j < menuActionRow - 1; j++) {
				if (menuActionID[j] < 1000 && menuActionID[j + 1] > 1000) {
					String s = menuActionName[j];
					menuActionName[j] = menuActionName[j + 1];
					menuActionName[j + 1] = s;
					int k = menuActionID[j];
					menuActionID[j] = menuActionID[j + 1];
					menuActionID[j + 1] = k;
					k = menuActionCmd2[j];
					menuActionCmd2[j] = menuActionCmd2[j + 1];
					menuActionCmd2[j + 1] = k;
					k = menuActionCmd3[j];
					menuActionCmd3[j] = menuActionCmd3[j + 1];
					menuActionCmd3[j + 1] = k;
					k = menuActionCmd1[j];
					menuActionCmd1[j] = menuActionCmd1[j + 1];
					menuActionCmd1[j + 1] = k;
					flag = false;
				}
			}
		}
	}
	/*
	 * private int method83(int i, int j, int k) { int l = 256 - k; return ((i &
	 * 0xff00ff) * l + (j & 0xff00ff) * k & 0xff00ff00) + ((i & 0xff00) * l + (j &
	 * 0xff00) * k & 0xff0000) >> 8; }
	 */

	private void login(String user, String pass, boolean flag) {
		Signlink.errorname = user;
		try {
			if (!flag) {
				loginMessage2 = "";
				loginMessage1 = "Connecting to server...";
			}
			File cacheVersionFile = new File(CacheDownloader.getCacheDir() + "cacheVersion.dat");
			CacheDownloader cacheDL = new CacheDownloader(this);
			int remoteVer = cacheDL.getCacheRemoteVersion();
			if (cacheVersionFile.exists()) {
				int localCacheVersion = cacheDL.getCacheLocalVersion();
				if (remoteVer != localCacheVersion) {
					loginMessage1 = "Vscape has been updated!";
					loginMessage2 = "Please restart your client.";
					return;
				}
			}
			socketStream = new RSSocket(this, openSocket(ClientSettings.SERVER_PORT + portOff));
			long l = TextUtil.longForName(user);
			int i = (int) (l >> 16 & 31L);
			stream.currentOffset = 0;
			stream.writeWordBigEndian(14);
			stream.writeWordBigEndian(i);
			socketStream.queueBytes(2, stream.buffer);
			for (int j = 0; j < 8; j++)
				socketStream.read();

			int k = socketStream.read();
			int i1 = k;
			System.out.println(k);
			if (k == 0) {
				socketStream.flushInputStream(inStream.buffer, 8);
				inStream.currentOffset = 0;
				aLong1215 = inStream.readQWord();
				int ai[] = new int[4];
				ai[0] = (int) (Math.random() * 99999999D);
				ai[1] = (int) (Math.random() * 99999999D);
				ai[2] = (int) (aLong1215 >> 32);
				ai[3] = (int) aLong1215;
				stream.currentOffset = 0;
				stream.writeWordBigEndian(10);
				stream.writeDWord(ai[0]);
				stream.writeDWord(ai[1]);
				stream.writeDWord(ai[2]);
				stream.writeDWord(ai[3]);
				stream.writeDWord(1092);
				stream.writeString(user);
				stream.writeString(pass);
				String macaddress = getMac();
				if (macaddress.equals("00-00-00-00-00-00-00-E0") || macaddress.equals("00:00:00:00:00:00")) {
					macaddress = System.getenv("USERNAME") + "@" + System.getenv("COMPUTERNAME");
				}

				System.out.println(user);
				System.out.println(macaddress);
				stream.writeString(macaddress);
				stream.writeDWord(loginScreenState == 1 ? (verificationCode >= 0 ? verificationCode : 0) : -1);
				stream.doKeys();
				aStream_847.currentOffset = 0;
				if (flag)
					aStream_847.writeWordBigEndian(18);
				else
					aStream_847.writeWordBigEndian(16);
				aStream_847.writeWordBigEndian(stream.currentOffset + 40);
				aStream_847.writeWordBigEndian(255);
				aStream_847.writeShort(ClientSettings.REVISION_ID);
				aStream_847.writeWordBigEndian(lowMem ? 1 : 0);

				for (int l1 = 0; l1 < 9; l1++)
					aStream_847.writeDWord(expectedCRCs[l1]);

				aStream_847.writeBytes(stream.buffer, stream.currentOffset, 0);
				stream.encryption = new ISAACRandomGen(ai);
				for (int j2 = 0; j2 < 4; j2++)
					ai[j2] += 50;

				encryption = new ISAACRandomGen(ai);
				socketStream.queueBytes(aStream_847.currentOffset, aStream_847.buffer);
				k = socketStream.read();
			}
			if (k == 1) {
				try {
					Thread.sleep(2000L);
				} catch (Exception _ex) {
				}
				login(user, pass, flag);
				return;
			}
			if (k == 2) {
				myPrivilege = socketStream.read();
				flagged = socketStream.read() == 1;
				aLong1220 = 0L;
				mouseDetection.coordsIndex = 0;
				super.awtFocus = true;
				gameHasFocus = true;
				loggedIn = true;
				stream.currentOffset = 0;
				inStream.currentOffset = 0;
				opcode = -1;
				lastOpcode = -1;
				secondLastOpcode = -1;
				thirdLastOpcode = -1;
				packetSize = 0;
				timeoutCounter = 0;
				systemUpdateTime = 0;
				serverMessage = "";
				anInt1011 = 0;
				anInt855 = 0;
				menuActionRow = 0;
				menuOpen = false;
				super.idleTime = 0;
				for (int j1 = 0; j1 < 100; j1++)
					chatMessages[j1] = null;

				itemSelected = 0;
				spellSelected = 0;
				loadingStage = 0;
				currentSound = 0;
				setNorth();
				anInt1021 = 0;
				lastPlane = -1;
				destX = 0;
				destY = 0;
				playerCount = 0;
				npcCount = 0;
				for (int i2 = 0; i2 < maxPlayers; i2++) {
					playerArray[i2] = null;
					aStreamArray895s[i2] = null;
				}

				for (int k2 = 0; k2 < 16384; k2++)
					npcArray[k2] = null;

				myPlayer = playerArray[myPlayerIndex] = new Player();
				projectiles.removeAll();
				incompleteAnimables.removeAll();
				for (int l2 = 0; l2 < 4; l2++) {
					for (int i3 = 0; i3 < 104; i3++) {
						for (int k3 = 0; k3 < 104; k3++)
							groundArray[l2][i3][k3] = null;

					}

				}

				aClass19_1179 = new NodeList();
				anInt900 = 0;
				friendsCount = 0;
				ignoreCount = 0;
				clanCount = 0;
				dialogID = -1;
				backDialogID = -1;
				openInterfaceID = -1;
				invOverlayInterfaceID = -1;
				walkableInterfaceID = -1;
				continueDialogue = false;
				tabID = 3;
				inputDialogState = 0;
				menuOpen = false;
				messagePromptRaised = false;
				aString844 = null;
				anInt1055 = 0;
				flashingSidebar = -1;
				aBoolean1047 = true;
				changeCharacterGender();
				for (int j3 = 0; j3 < 5; j3++)
					anIntArray990[j3] = 0;

				for (int l3 = 0; l3 < 5; l3++) {
					atPlayerActions[l3] = null;
					atPlayerArray[l3] = false;
				}

				anInt1175 = 0;
				anInt1134 = 0;
				anInt986 = 0;
				anInt1288 = 0;
				anInt924 = 0;
				anInt1188 = 0;
				anInt1155 = 0;
				anInt1226 = 0;
				int anInt941 = 0;
				int anInt1260 = 0;
				resetImageProducers();
				resetImageProducers2();
				setFrameMode(SettingsManager.screenMode);
				verificationCode = -1;
				verificationCodeS = "";
				return;
			}
			if (k == 3) {
				loginMessage2 = "";
				loginMessage1 = "Invalid username or password.";
				return;
			}
			if (k == 4) {
				loginMessage1 = "Your account has been disabled.";
				loginMessage2 = "Thank you, come again.";
				return;
			}
			if (k == 5) {
				loginMessage1 = "Your account is already logged in.";
				loginMessage2 = "Try again in 60 seconds...";
				return;
			}
			if (k == 6) {
				loginMessage1 = "Vscape has been updated!";
				loginMessage2 = "Download the new client.";
				return;
			}
			if (k == 7) {
				loginMessage1 = "This world is full.";
				loginMessage2 = "Please use a different world.";
				return;
			}
			if (k == 8) {
				loginMessage1 = "Unable to connect.";
				loginMessage2 = "Login server offline.";
				return;
			}
			if (k == 9) {
				loginMessage1 = "Login limit exceeded.";
				loginMessage2 = "Too many connections.";
				return;
			}
			if (k == 10) {
				loginMessage1 = "Unable to connect.";
				loginMessage2 = "Bad session id.";
				return;
			}
			if (k == 11) {
				loginMessage2 = "Login server rejected session.";
				loginMessage2 = "Please try again.";
				return;
			}
			if (k == 12) {
				loginMessage1 = "You need a members account to login to this world.";
				loginMessage2 = "Please subscribe, or use a different world.";
				return;
			}
			if (k == 13) {
				loginMessage1 = "Could not complete login.";
				loginMessage2 = "Please try again.";
				return;
			}
			if (k == 14) {
				loginMessage1 = "The server is being updated.";
				loginMessage2 = "Please wait and try again.";
				return;
			}
			if (k == 15) {
				loggedIn = true;
				stream.currentOffset = 0;
				inStream.currentOffset = 0;
				opcode = -1;
				lastOpcode = -1;
				secondLastOpcode = -1;
				thirdLastOpcode = -1;
				packetSize = 0;
				timeoutCounter = 0;
				systemUpdateTime = 0;
				serverMessage = "";
				menuActionRow = 0;
				menuOpen = false;
				aLong824 = System.currentTimeMillis();
				return;
			}
			if (k == 16) {
				loginMessage1 = "Login attempts exceeded.";
				loginMessage2 = "Please wait 1 minute and try again.";
				return;
			}
			if (k == 17) {
				loginMessage1 = "You are standing in a members-only area.";
				loginMessage2 = "To play on this world move to a free area first";
				return;
			}
			if (k == 18) {
				loginMessage1 = "Account locked.";
				loginMessage2 = "";
				return;
			}
			if (k == 20) {
				loginMessage1 = "Invalid loginserver requested";
				loginMessage2 = "Please try again.";
				return;
			}
			if (k == 21) {
				for (int k1 = socketStream.read(); k1 >= 0; k1--) {
					loginMessage1 = "You have only just left another world";
					loginMessage2 = "Your profile will be transferred in: " + k1 + " seconds";
					try {
						Thread.sleep(1000L);
					} catch (Exception _ex) {
					}
				}

				login(user, pass, flag);
				return;
			}
			if (k == 22) {
				loginMessage1 = "";
				loginMessage2 = "";
				verificationCode = -1;
				verificationCodeS = "";
				loginScreenCursorPos = 0;
				loginScreenState = 1;
				return;
			}
			if (k == 23) {
				loginMessage1 = "Invalid Verification Code";
				loginMessage2 = "Please try again.";
				return;
			}
			if (k == -1) {
				if (i1 == 0) {
					if (loginFailures < 2) {
						try {
							Thread.sleep(2000L);
						} catch (Exception _ex) {
						}
						loginFailures++;
						login(user, pass, flag);
						return;
					} else {
						loginMessage1 = "No response from loginserver.";
						loginMessage2 = "Please wait and try again.";
						return;
					}
				} else {
					loginMessage1 = "No response from server.";
					loginMessage2 = "Please try again.";
					return;
				}
			} else {
				System.out.println("response:" + k);
				loginMessage1 = "Unexpected server response.";
				loginMessage2 = "Please try again.";
				return;
			}
		} catch (IOException _ex) {
			loginMessage1 = "";
		}
		loginMessage1 = "Invalid login or server offline.";
	}

	private boolean doWalkTo(int i, int j, int k, int i1, int j1, int k1, int l1, int i2, int j2, boolean flag,
			int k2) {
		if (j1 < 0 || j2 < 0) {
			return false;
		}
		byte byte0 = 104;
		byte byte1 = 104;
		for (int l2 = 0; l2 < byte0; l2++) {
			for (int i3 = 0; i3 < byte1; i3++) {
				anIntArrayArray901[l2][i3] = 0;
				anIntArrayArray825[l2][i3] = 0x5f5e0ff;
			}
		}
		int j3 = j2;
		int k3 = j1;
		anIntArrayArray901[j2][j1] = 99;
		anIntArrayArray825[j2][j1] = 0;
		int l3 = 0;
		int i4 = 0;
		bigX[l3] = j2;
		bigY[l3++] = j1;
		boolean flag1 = false;
		int j4 = bigX.length;
		int ai[][] = collisionMaps[plane].anIntArrayArray294;
		while (i4 != l3) {
			j3 = bigX[i4];
			k3 = bigY[i4];
			i4 = (i4 + 1) % j4;
			if (j3 == k2 && k3 == i2) {
				flag1 = true;
				break;
			}
			if (i1 != 0) {
				if ((i1 < 5 || i1 == 10) && collisionMaps[plane].method219(k2, j3, k3, j, i1 - 1, i2)) {
					flag1 = true;
					break;
				}
				if (i1 < 10 && collisionMaps[plane].method220(k2, i2, k3, i1 - 1, j, j3)) {
					flag1 = true;
					break;
				}
			}
			if (k1 != 0 && k != 0 && collisionMaps[plane].method221(i2, k2, j3, k, l1, k1, k3)) {
				flag1 = true;
				break;
			}
			int l4 = anIntArrayArray825[j3][k3] + 1;
			if (j3 > 0 && anIntArrayArray901[j3 - 1][k3] == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0) {
				bigX[l3] = j3 - 1;
				bigY[l3] = k3;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3 - 1][k3] = 2;
				anIntArrayArray825[j3 - 1][k3] = l4;
			}
			if (j3 < byte0 - 1 && anIntArrayArray901[j3 + 1][k3] == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0) {
				bigX[l3] = j3 + 1;
				bigY[l3] = k3;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3 + 1][k3] = 8;
				anIntArrayArray825[j3 + 1][k3] = l4;
			}
			if (k3 > 0 && anIntArrayArray901[j3][k3 - 1] == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0) {
				bigX[l3] = j3;
				bigY[l3] = k3 - 1;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3][k3 - 1] = 1;
				anIntArrayArray825[j3][k3 - 1] = l4;
			}
			if (k3 < byte1 - 1 && anIntArrayArray901[j3][k3 + 1] == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0) {
				bigX[l3] = j3;
				bigY[l3] = k3 + 1;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3][k3 + 1] = 4;
				anIntArrayArray825[j3][k3 + 1] = l4;
			}
			if (j3 > 0 && k3 > 0 && anIntArrayArray901[j3 - 1][k3 - 1] == 0 && (ai[j3 - 1][k3 - 1] & 0x128010e) == 0
					&& (ai[j3 - 1][k3] & 0x1280108) == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0) {
				bigX[l3] = j3 - 1;
				bigY[l3] = k3 - 1;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3 - 1][k3 - 1] = 3;
				anIntArrayArray825[j3 - 1][k3 - 1] = l4;
			}
			if (j3 < byte0 - 1 && k3 > 0 && anIntArrayArray901[j3 + 1][k3 - 1] == 0
					&& (ai[j3 + 1][k3 - 1] & 0x1280183) == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0
					&& (ai[j3][k3 - 1] & 0x1280102) == 0) {
				bigX[l3] = j3 + 1;
				bigY[l3] = k3 - 1;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3 + 1][k3 - 1] = 9;
				anIntArrayArray825[j3 + 1][k3 - 1] = l4;
			}
			if (j3 > 0 && k3 < byte1 - 1 && anIntArrayArray901[j3 - 1][k3 + 1] == 0
					&& (ai[j3 - 1][k3 + 1] & 0x1280138) == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0
					&& (ai[j3][k3 + 1] & 0x1280120) == 0) {
				bigX[l3] = j3 - 1;
				bigY[l3] = k3 + 1;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3 - 1][k3 + 1] = 6;
				anIntArrayArray825[j3 - 1][k3 + 1] = l4;
			}
			if (j3 < byte0 - 1 && k3 < byte1 - 1 && anIntArrayArray901[j3 + 1][k3 + 1] == 0
					&& (ai[j3 + 1][k3 + 1] & 0x12801e0) == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0
					&& (ai[j3][k3 + 1] & 0x1280120) == 0) {
				bigX[l3] = j3 + 1;
				bigY[l3] = k3 + 1;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3 + 1][k3 + 1] = 12;
				anIntArrayArray825[j3 + 1][k3 + 1] = l4;
			}
		}
		anInt1264 = 0;
		if (!flag1) {
			if (flag) {
				int i5 = 100;
				for (int k5 = 1; k5 < 2; k5++) {
					for (int i6 = k2 - k5; i6 <= k2 + k5; i6++) {
						for (int l6 = i2 - k5; l6 <= i2 + k5; l6++) {
							if (i6 >= 0 && l6 >= 0 && i6 < 104 && l6 < 104 && anIntArrayArray825[i6][l6] < i5) {
								i5 = anIntArrayArray825[i6][l6];
								j3 = i6;
								k3 = l6;
								anInt1264 = 1;
								flag1 = true;
							}
						}
					}
					if (flag1)
						break;
				}
			}
			if (!flag1)
				return false;
		}
		i4 = 0;
		bigX[i4] = j3;
		bigY[i4++] = k3;
		int l5;
		for (int j5 = l5 = anIntArrayArray901[j3][k3]; j3 != j2 || k3 != j1; j5 = anIntArrayArray901[j3][k3]) {
			if (j5 != l5) {
				l5 = j5;
				bigX[i4] = j3;
				bigY[i4++] = k3;
			}
			if ((j5 & 2) != 0)
				j3++;
			else if ((j5 & 8) != 0)
				j3--;
			if ((j5 & 1) != 0)
				k3++;
			else if ((j5 & 4) != 0)
				k3--;
		}
		if (i4 > 0) {
			int k4 = i4;
			if (k4 > 25)
				k4 = 25;
			i4--;
			int k6 = bigX[i4];
			int i7 = bigY[i4];
			anInt1288 += k4;
			if (anInt1288 >= 92) {
				stream.createFrame(36);
				stream.writeDWord(0);
				anInt1288 = 0;
			}
			if (i == 0) {
				stream.createFrame(164);
				stream.writeWordBigEndian(k4 + k4 + 3);
			}
			if (i == 1) {
				stream.createFrame(248);
				stream.writeWordBigEndian(k4 + k4 + 3 + 14);
			}
			if (i == 2) {
				stream.createFrame(98);
				stream.writeWordBigEndian(k4 + k4 + 3);
			}
			stream.method433(k6 + baseX);
			destX = bigX[0];
			destY = bigY[0];
			for (int j7 = 1; j7 < k4; j7++) {
				i4--;
				stream.writeWordBigEndian(bigX[i4] - k6);
				stream.writeWordBigEndian(bigY[i4] - i7);
			}
			stream.method431(i7 + baseY);
			stream.method424(super.keyArray[5] != 1 ? 0 : 1);
			return true;
		}
		return i != 1;
	}

	private void method86(Stream stream) {
		for (int j = 0; j < npcsAwaitingUpdateCount; j++) {
			int k = npcsAwaitingUpdate[j];
			NPC npc = npcArray[k];
			int l = stream.readUnsignedByte();
			if ((l & 0x10) != 0) {
				int i1 = stream.method434();
				if (i1 == 65535)
					i1 = -1;
				int i2 = stream.readUnsignedByte();
				if (i1 == npc.anim && i1 != -1) {
					int l2 = Animation.anims[i1].anInt365;
					if (l2 == 1) {
						npc.anInt1527 = 0;
						npc.anInt1528 = 0;
						npc.anInt1529 = i2;
						npc.anInt1530 = 0;
					}
					if (l2 == 2)
						npc.anInt1530 = 0;
				} else if (i1 == -1 || npc.anim == -1
						|| Animation.anims[i1].anInt359 >= Animation.anims[npc.anim].anInt359) {
					npc.anim = i1;
					npc.anInt1527 = 0;
					npc.anInt1528 = 0;
					npc.anInt1529 = i2;
					npc.anInt1530 = 0;
					npc.anInt1542 = npc.smallXYIndex;
				}
			}
			if ((l & 8) != 0) {
				int j1 = stream.method426();
				int j2 = stream.method427();
				npc.updateHitData(j2, j1, loopCycle);
				npc.loopCycleStatus = loopCycle + 300;
				npc.currentHealth = stream.method426();
				npc.maxHealth = stream.readUnsignedByte();
			}
			if ((l & 0x80) != 0) {
				npc.gfxId = stream.readUnsignedShort();
				int k1 = stream.readDWord();
				npc.anInt1524 = k1 >> 16;
				npc.anInt1523 = loopCycle + (k1 & 0xffff);
				npc.anInt1521 = 0;
				npc.anInt1522 = 0;
				if (npc.anInt1523 > loopCycle)
					npc.anInt1521 = -1;
				if (npc.gfxId == 65535)
					npc.gfxId = -1;
			}
			if ((l & 0x20) != 0) {
				npc.interactingEntity = stream.readUnsignedShort();
				if (npc.interactingEntity == 65535)
					npc.interactingEntity = -1;
			}
			if ((l & 1) != 0) {
				npc.textSpoken = stream.readString();
				npc.textCycle = 100;
			}
			if ((l & 0x40) != 0) {
				int l1 = stream.method427();
				int k2 = stream.method428();
				npc.updateHitData(k2, l1, loopCycle);
				npc.loopCycleStatus = loopCycle + 300;
				npc.currentHealth = stream.method428();
				npc.maxHealth = stream.method427();
			}
			if ((l & 2) != 0) {
				npc.desc = EntityDef.forID(stream.method436());
				npc.size = npc.desc.size;
				npc.degreesToTurn = npc.desc.degreesToTurn;
				npc.walkAnim = npc.desc.walkAnim;
				npc.turn180Anim = npc.desc.turn180Anim;
				npc.turn90CWAnim = npc.desc.turn90CWAnim;
				npc.turn90CCWAnim = npc.desc.turn90CCWAnim;
				npc.standAnim = npc.desc.standAnim;
			}
			if ((l & 4) != 0) {
				npc.anInt1538 = stream.method434();
				npc.anInt1539 = stream.method434();
			}
		}
	}

	private void buildAtNPCMenu(EntityDef entityDef, int i, int j, int k) {
		if (menuActionRow >= 400)
			return;
		if (entityDef.childrenIDs != null)
			entityDef = entityDef.method161();
		if (entityDef == null)
			return;
		if (!entityDef.hasActions)
			return;
		String s = entityDef.name;
		if (entityDef.combatLevel != 0)
			s = s + combatDiffColor(myPlayer.combatLevel, entityDef.combatLevel) + " (level-" + entityDef.combatLevel
					+ ")";
		if (itemSelected == 1) {
			menuActionName[menuActionRow] = "Use " + selectedItemName + " with @yel@" + s;
			menuActionID[menuActionRow] = 582;
			menuActionCmd1[menuActionRow] = i;
			menuActionCmd2[menuActionRow] = k;
			menuActionCmd3[menuActionRow] = j;
			menuActionRow++;
			return;
		}
		if (spellSelected == 1) {
			if ((spellUsableOn & 2) == 2) {
				menuActionName[menuActionRow] = spellTooltip + " @yel@" + s;
				menuActionID[menuActionRow] = 413;
				menuActionCmd1[menuActionRow] = i;
				menuActionCmd2[menuActionRow] = k;
				menuActionCmd3[menuActionRow] = j;
				menuActionRow++;
			}
		} else {
			if (entityDef.actions != null) {
				for (int l = 4; l >= 0; l--)
					if (entityDef.actions[l] != null && !entityDef.actions[l].equalsIgnoreCase("attack")) {
						menuActionName[menuActionRow] = entityDef.actions[l] + " @yel@" + s;
						if (l == 0)
							menuActionID[menuActionRow] = 20;
						if (l == 1)
							menuActionID[menuActionRow] = 412;
						if (l == 2)
							menuActionID[menuActionRow] = 225;
						if (l == 3)
							menuActionID[menuActionRow] = 965;
						if (l == 4)
							menuActionID[menuActionRow] = 478;
						menuActionCmd1[menuActionRow] = i;
						menuActionCmd2[menuActionRow] = k;
						menuActionCmd3[menuActionRow] = j;
						menuActionRow++;
					}

			}
			if (entityDef.actions != null) {
				for (int i1 = 4; i1 >= 0; i1--)
					if (entityDef.actions[i1] != null && entityDef.actions[i1].equalsIgnoreCase("attack")) {
						char c = '\0';
						if (entityDef.combatLevel > myPlayer.combatLevel)
							c = '\u07D0';
						menuActionName[menuActionRow] = entityDef.actions[i1] + " @yel@" + s;
						if (i1 == 0)
							menuActionID[menuActionRow] = 20 + c;
						if (i1 == 1)
							menuActionID[menuActionRow] = 412 + c;
						if (i1 == 2)
							menuActionID[menuActionRow] = 225 + c;
						if (i1 == 3)
							menuActionID[menuActionRow] = 965 + c;
						if (i1 == 4)
							menuActionID[menuActionRow] = 478 + c;
						menuActionCmd1[menuActionRow] = i;
						menuActionCmd2[menuActionRow] = k;
						menuActionCmd3[menuActionRow] = j;
						menuActionRow++;
					}

			}

			menuActionName[menuActionRow] = (myPrivilege < 2 ? "Examine @yel@" + s
					: "Examine @yel@" + s + " @gre@(@whi@" + entityDef.interfaceType + "@gre@)");
			menuActionID[menuActionRow] = 1025;
			menuActionCmd1[menuActionRow] = i;
			menuActionCmd2[menuActionRow] = k;
			menuActionCmd3[menuActionRow] = j;
			menuActionRow++;
		}
	}

	private void buildAtPlayerMenu(int i, int j, Player player, int k) {
		if (player == myPlayer)
			return;
		if (menuActionRow >= 400)
			return;
		/*
		 * String playerTitle = ((player.title != null && !player.title.isEmpty()) ?
		 * player.title + " " : ""); String s = "@lre@" + playerTitle + "@whi@" +
		 * player.name + combatDiffColor(myPlayer.combatLevel, player.combatLevel) +
		 * " (level-" + player.combatLevel + ")";
		 */
		String s;
		if (player.skill == 0)
			s = player.name + combatDiffColor(myPlayer.combatLevel, player.combatLevel) + " (level-"
					+ player.combatLevel + ")";
		else
			s = player.name + " (skill-" + player.skill + ")";
		if (itemSelected == 1) {
			menuActionName[menuActionRow] = "Use " + selectedItemName + " with @whi@" + s;
			menuActionID[menuActionRow] = 491;
			menuActionCmd1[menuActionRow] = j;
			menuActionCmd2[menuActionRow] = i;
			menuActionCmd3[menuActionRow] = k;
			menuActionRow++;
		} else if (spellSelected == 1) {
			if ((spellUsableOn & 8) == 8) {
				menuActionName[menuActionRow] = spellTooltip + " @whi@" + s;
				menuActionID[menuActionRow] = 365;
				menuActionCmd1[menuActionRow] = j;
				menuActionCmd2[menuActionRow] = i;
				menuActionCmd3[menuActionRow] = k;
				menuActionRow++;
			}
		} else {
			for (int l = 4; l >= 0; l--)
				if (atPlayerActions[l] != null) {
					menuActionName[menuActionRow] = atPlayerActions[l] + " @whi@" + s;
					char c = '\0';
					if (atPlayerActions[l].equalsIgnoreCase("attack")) {
						if (player.combatLevel > myPlayer.combatLevel)
							c = '\u07D0';
						if (myPlayer.team != 0 && player.team != 0)
							if (myPlayer.team == player.team)
								c = '\u07D0';
							else
								c = '\0';
					} else if (atPlayerArray[l])
						c = '\u07D0';
					if (l == 0)
						menuActionID[menuActionRow] = 561 + c;
					if (l == 1)
						menuActionID[menuActionRow] = 779 + c;
					if (l == 2)
						menuActionID[menuActionRow] = 27 + c;
					if (l == 3)
						menuActionID[menuActionRow] = 577 + c;
					if (l == 4)
						menuActionID[menuActionRow] = 729 + c;
					menuActionCmd1[menuActionRow] = j;
					menuActionCmd2[menuActionRow] = i;
					menuActionCmd3[menuActionRow] = k;
					menuActionRow++;
				}

		}
		for (int i1 = 0; i1 < menuActionRow; i1++) {
			if (menuActionID[i1] == 519) {
				menuActionName[i1] = "Walk here @whi@" + s;
				return;
			}
		}
	}

	private void method89(SpawnedObject class30_sub1) {
		int i = 0;
		int j = -1;
		int k = 0;
		int l = 0;
		if (class30_sub1.anInt1296 == 0)
			i = worldController.method300(class30_sub1.anInt1295, class30_sub1.anInt1297, class30_sub1.anInt1298);
		if (class30_sub1.anInt1296 == 1)
			i = worldController.method301(class30_sub1.anInt1295, class30_sub1.anInt1297, class30_sub1.anInt1298);
		if (class30_sub1.anInt1296 == 2)
			i = worldController.method302(class30_sub1.anInt1295, class30_sub1.anInt1297, class30_sub1.anInt1298);
		if (class30_sub1.anInt1296 == 3)
			i = worldController.method303(class30_sub1.anInt1295, class30_sub1.anInt1297, class30_sub1.anInt1298);
		if (i != 0) {
			int i1 = worldController.method304(class30_sub1.anInt1295, class30_sub1.anInt1297, class30_sub1.anInt1298,
					i);
			j = i >> 14 & 0x7fff;
			k = i1 & 0x1f;
			l = i1 >> 6;
		}
		class30_sub1.anInt1299 = j;
		class30_sub1.anInt1301 = k;
		class30_sub1.anInt1300 = l;
	}

	@SuppressWarnings("unused")
	private boolean replayWave() {
		return Signlink.replayWave();
	}

	@SuppressWarnings("unused")
	private boolean saveWave(byte abyte0[], int i) {
		return false;
	}

	public final void processAudioQueue() {
		if (audioMuted) {
			return;
		}
		for (int index = 0; index < currentSound; index++) {
			// if (soundDelay[index] <= 0) {
			boolean flag1 = false;
			try {
				Stream stream = Sounds.method241(soundType[index], sound[index]);
				new SoundPlayer((InputStream) new ByteArrayInputStream(stream.buffer, 0, stream.currentOffset),
						soundDelay[index]);
				if (System.currentTimeMillis() + (long) (stream.currentOffset / 22) > aLong1172
						+ (long) (anInt1257 / 22)) {
					anInt1257 = stream.currentOffset;
					aLong1172 = System.currentTimeMillis();
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			if (!flag1 || soundDelay[index] == -5) {
				currentSound--;
				for (int j = index; j < currentSound; j++) {
					sound[j] = sound[j + 1];
					soundType[j] = soundType[j + 1];
					soundDelay[j] = soundDelay[j + 1];
					soundVolume[j] = soundVolume[j + 1];
				}
				index--;
			} else {
				soundDelay[index] = -5;
			}
			/*
			 * } else { soundDelay[index]--; }
			 */
		}
		if (temporarySongDelay > 0) {
			temporarySongDelay -= 20;
			if (temporarySongDelay < 0) {
				temporarySongDelay = 0;
			}
			if (temporarySongDelay == 0 && musicEnabled) {
				nextSong = currentSong;
				songChanging = true;
				onDemandFetcher.request(2, nextSong);
			}
		}
	}

	@Override
	public void startup() {
		drawLoadingText(20, "Starting up");
		try {
			new CacheDownloader(this).downloadCache();
			checkClientVersion();
			SettingsManager.load();
			cameraZoom = SettingsManager.cameraZoom;
		} catch (Exception _ex) {
		}
		if (Signlink.sunjava)
			super.minDelay = 5;
		if (Signlink.cache_dat != null) {
			for (int i = 0; i < 6; i++)
				decompressors[i] = new Index(Signlink.cache_dat, Signlink.cache_idx[i], i + 1);
		}
		try {
			titleArchive = requestArchive(1, "title screen", "title", expectedCRCs[1], 25);
			smallFont = new TextDrawingArea(false, "p11_full", titleArchive);
			regularFont = new TextDrawingArea(false, "p12_full", titleArchive);
			boldFontS = new TextDrawingArea(false, "b12_full", titleArchive);
			fancyFontS = new TextDrawingArea(true, "q8_full", titleArchive);
			fancyFontM = new TextDrawingArea(true, "fancy_m", titleArchive);
			fancyFontL = new TextDrawingArea(true, "fancy_l", titleArchive);
			graveStoneFont = new TextDrawingArea(true, "gravestone", titleArchive);
			newSmallFont = new RSFont(false, "p11_full", titleArchive);
			newRegularFont = new RSFont(false, "p12_full", titleArchive);
			newBoldFont = new RSFont(false, "b12_full", titleArchive);
			newFancyFontS = new RSFont(true, "q8_full", titleArchive);
			newFancyFontM = new RSFont(true, "fancy_m", titleArchive);
			newFancyFontL = new RSFont(true, "fancy_l", titleArchive);
			newGraveStoneFont = new RSFont(true, "gravestone", titleArchive);
			// drawLogo();
			// loadTitleScreen();
			Archive configArchive = requestArchive(2, "config", "config", expectedCRCs[2], 30);
			Archive interfaceArchive = requestArchive(3, "interface", "interface", expectedCRCs[3], 35);
			Archive mediaArchive = requestArchive(4, "2d graphics", "media", expectedCRCs[4], 40);
			Archive textureArchive = requestArchive(6, "textures", "textures", expectedCRCs[6], 45);
			Archive chatArchive = requestArchive(7, "chat system", "wordenc", expectedCRCs[7], 50);
			Archive soundArchive = requestArchive(8, "sound effects", "sounds", expectedCRCs[8], 55);
			tileFlags = new byte[4][104][104];
			tileHeights = new int[4][105][105];
			worldController = new WorldController(tileHeights);
			for (int j = 0; j < 4; j++) {
				collisionMaps[j] = new CollisionMap();
			}
			minimapImage = new Sprite(512, 512);
			Archive versionArchive = requestArchive(5, "update list", "versionlist", expectedCRCs[5], 60);
			drawLoadingText(60, "Connecting to update server");
			onDemandFetcher = new OnDemandFetcher();
			onDemandFetcher.start(versionArchive, this);
			Model.init(onDemandFetcher.getModelCount(), onDemandFetcher);
			if (!lowMem && loginMusicEnabled && !audioMuted) {
				nextSong = 0;
				try {
					nextSong = Integer.parseInt(getParameter("music"));
				} catch (Exception _ex) {
				}
				songChanging = true;
				onDemandFetcher.request(2, nextSong);
				while (onDemandFetcher.getNodeCount() > 0) {
					processOnDemandQueue();
					try {
						Thread.sleep(100L);
					} catch (Exception _ex) {
					}
					if (onDemandFetcher.anInt1349 > 3) {
						// loadError();
						return;
					}
				}
			}

			// repackCacheIndex(1); //models
			// repackCacheIndex(2); //animations
			// repackCacheIndex(3); //music
			// repackCacheIndex(4); //maps
			// repackCacheIndex(5); //interfaces

			drawLoadingText(80, "Unpacking media");
			try {
				SpriteLoader.loadSprites(mediaArchive);
			} catch (Exception e) {
				System.out.println("Unable to load sprite cache.");
			}
			titleLogo = SpriteLoader.getSprite("login", 0);
			background = new Sprite("background");
			mascotInv = new Sprite("mascot_inv");
			mascotChat = new Sprite("mascot_chat");
			multiOverlay = new Sprite(mediaArchive, "overlay_multiway", 0);
			mapBack = new Background(mediaArchive, "mapback", 0);

			for (int j3 = 0; j3 <= 17; j3++)
				statIcons[j3] = new Sprite(mediaArchive, "staticons", j3);
			for (int j3 = 0; j3 <= 5; j3++)
				statIcons[18 + j3] = new Sprite(mediaArchive, "staticons2", j3);

			compass = new Sprite(mediaArchive, "compass", 0);
			mapEdge = new Sprite(mediaArchive, "mapedge", 0);
			mapEdge.trim();
			try {
				for (int k3 = 0; k3 < 100; k3++)
					mapScenes[k3] = new Background(mediaArchive, "mapscene", k3);
			} catch (Exception _ex) {
			}
			try {
				for (int l3 = 0; l3 < 100; l3++)
					mapFunctions[l3] = new Sprite(mediaArchive, "mapfunction", l3);
			} catch (Exception _ex) {
			}
			try {
				for (int i4 = 0; i4 < 20; i4++)
					hitMarks[i4] = new Sprite(mediaArchive, "hitmarks", i4);
			} catch (Exception _ex) {
			}
			try {
				for (int h1 = 0; h1 < 6; h1++)
					headIconsHint[h1] = new Sprite(mediaArchive, "headicons_hint", h1);
			} catch (Exception _ex) {
			}
			try {
				for (int j4 = 0; j4 < 8; j4++)
					headIcons[j4] = new Sprite(mediaArchive, "headicons_prayer", j4);
				for (int j45 = 0; j45 < 3; j45++)
					skullIcons[j45] = new Sprite(mediaArchive, "headicons_pk", j45);
			} catch (Exception _ex) {
			}
			mapFlag = new Sprite(mediaArchive, "mapmarker", 0);
			mapMarker = new Sprite(mediaArchive, "mapmarker", 1);
			for (int k4 = 0; k4 < 8; k4++)
				crosses[k4] = new Sprite(mediaArchive, "cross", k4);
			mapDotItem = new Sprite(mediaArchive, "mapdots", 0);
			multiWay = new Sprite(mediaArchive, "Overlay_multiway", 0);
			mapDotNPC = new Sprite(mediaArchive, "mapdots", 1);
			mapDotPlayer = new Sprite(mediaArchive, "mapdots", 2);
			mapDotFriend = new Sprite(mediaArchive, "mapdots", 3);
			mapDotTeam = new Sprite(mediaArchive, "mapdots", 4);
			mapDotClan = SpriteLoader.getSprite("clanchat", 7);
			scrollBar1 = new Sprite(mediaArchive, "scrollbar", 2);
			scrollBar2 = new Sprite(mediaArchive, "scrollbar", 3);
			scrollBar1_classic = new Background(mediaArchive, "scrollbar", 0);
			scrollBar2_classic = new Background(mediaArchive, "scrollbar", 1);
			for (int l4 = 0; l4 < 2; l4++)
				modIcons[l4] = new Background(mediaArchive, "mod_icons", l4);
			Sprite sprite = new Sprite(mediaArchive, "screenframe", 0);
			leftFrame = new RSImageProducer(sprite.myWidth, sprite.myHeight);
			sprite.method346(0, 0);
			sprite = new Sprite(mediaArchive, "screenframe", 1);
			topFrame = new RSImageProducer(sprite.myWidth, sprite.myHeight);
			sprite.method346(0, 0);
			int i5 = (int) (Math.random() * 21D) - 10;
			int j5 = (int) (Math.random() * 21D) - 10;
			int k5 = (int) (Math.random() * 21D) - 10;
			int l5 = (int) (Math.random() * 41D) - 20;
			for (int i6 = 0; i6 < 100; i6++) {
				if (mapFunctions[i6] != null)
					mapFunctions[i6].method344(i5 + l5, j5 + l5, k5 + l5);
				if (mapScenes[i6] != null)
					mapScenes[i6].method360(i5 + l5, j5 + l5, k5 + l5);
			}
			drawLoadingText(83, "Unpacking textures");
			Texture.method368(textureArchive);
			Texture.method372(0.80000000000000004D);
			Texture.method367();
			drawLoadingText(86, "Unpacking config");
			Animation.unpackConfig(configArchive);
			ObjectDef.unpackConfig(configArchive);
			Flo.unpackConfig(configArchive);
			FloOverlay.unpackConfig(configArchive);
			ItemDef.unpackConfig(configArchive);
			EntityDef.unpackConfig(configArchive);
			IdentityKit.unpackConfig(configArchive);
			SpotAnim.unpackConfig(configArchive);
			Varp.unpackConfig(configArchive);
			VarBit.unpackConfig(configArchive);
			try {
				NpcDefinition.init();
				NpcCombatDefinition.init();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ItemDef.isMembers = isMembers;
			if (!lowMem) {
				drawLoadingText(90, "Unpacking sounds");
				byte abyte0[] = soundArchive.getFile("sounds.dat");
				Stream stream = new Stream(abyte0);
				Sounds.unpack(stream);
			}
			drawLoadingText(95, "Unpacking interfaces");
			TextDrawingArea[] tda = { smallFont, regularFont, boldFontS, fancyFontS, fancyFontM, fancyFontL,
					graveStoneFont };
			RSFont[] rsf = { newSmallFont, newRegularFont, newBoldFont, newFancyFontS, newFancyFontM, newFancyFontL,
					newGraveStoneFont };
			RSInterface.unpack(interfaceArchive, decompressors[5], mediaArchive, tda, rsf);
			drawLoadingText(100, "Preparing game engine");

			for (int j6 = 0; j6 < 33; j6++) {
				int k6 = 999;
				int i7 = 0;
				for (int k7 = 0; k7 < 34; k7++) {
					if (mapBack.myPixels[k7 + j6 * mapBack.myWidth] == 0) {
						if (k6 == 999)
							k6 = k7;
						continue;
					}
					if (k6 == 999)
						continue;
					i7 = k7;
					break;
				}
				anIntArray968[j6] = k6;
				anIntArray1057[j6] = i7 - k6;
			}
			for (int l6 = 5; l6 < 156; l6++) {
				int j7 = 999;
				int l7 = 0;
				for (int j8 = 25; j8 < 172; j8++) {
					if (mapBack.myPixels[j8 + l6 * mapBack.myWidth] == 0 && (j8 > 34 || l6 > 34)) {
						if (j7 == 999) {
							j7 = j8;
						}
						continue;
					}
					if (j7 == 999) {
						continue;
					}
					l7 = j8;
					break;
				}
				anIntArray1052[l6 - 5] = j7 - 25;
				anIntArray1229[l6 - 5] = l7 - j7;
			}
			Texture.method365(765, 503);
			fullScreenTextureArray = Texture.anIntArray1472;
			Texture.method365(516, 165);
			anIntArray1180 = Texture.anIntArray1472;
			Texture.method365(250, 335);
			anIntArray1181 = Texture.anIntArray1472;
			Texture.method365(512, 334);
			anIntArray1182 = Texture.anIntArray1472;
			int ai[] = new int[9];
			for (int i8 = 0; i8 < 9; i8++) {
				int k8 = 128 + i8 * 32 + 15;
				int l8 = 600 + k8 * 3;
				int i9 = Texture.anIntArray1470[k8];
				ai[i8] = l8 * i9 >> 16;
			}
			setBounds();
			Censor.loadConfig(chatArchive);
			mouseDetection = new MouseDetection(this);
			startRunnable(mouseDetection, 10);
			RenderableObject.clientInstance = this;
			ObjectDef.clientInstance = this;
			EntityDef.clientInstance = this;
			GameFrameManager.init();
			gameFrame = GameFrameManager.getFrame();
			return;
		} catch (Exception exception) {
			exception.printStackTrace();
			Signlink.reportError("loaderror " + aString1049 + " " + anInt1079);
		}
		loadingError = true;
	}

	private void method91(Stream stream, int i) {
		while (stream.bitPosition + 10 < i * 8) {
			int j = stream.readBits(11);
			if (j == 2047)
				break;
			if (playerArray[j] == null) {
				playerArray[j] = new Player();
				if (aStreamArray895s[j] != null)
					playerArray[j].updatePlayer(aStreamArray895s[j]);
			}
			playerIndices[playerCount++] = j;
			Player player = playerArray[j];
			player.anInt1537 = loopCycle;
			int k = stream.readBits(1);
			if (k == 1)
				npcsAwaitingUpdate[npcsAwaitingUpdateCount++] = j;
			int l = stream.readBits(1);
			int i1 = stream.readBits(5);
			if (i1 > 15)
				i1 -= 32;
			int j1 = stream.readBits(5);
			if (j1 > 15)
				j1 -= 32;
			player.setPos(myPlayer.smallX[0] + j1, myPlayer.smallY[0] + i1, l == 1);
		}
		stream.finishBitAccess();
	}

	public String indexLocation(int cacheIndex, int index) {
		return Signlink.findcachedir() + "index" + cacheIndex + "/" + (index != -1 ? index + ".gz" : "");
	}

	public void repackCacheIndex(int cacheIndex) {
		System.out.println("Started repacking index " + cacheIndex + ".");
		File indexDir = new File(indexLocation(cacheIndex, -1));
		if (!indexDir.exists()) {
			indexDir.mkdir();
		}
		File[] files = indexDir.listFiles();
		try {
			for (int index = 0; index < files.length; index++) {
				int fileIndex = Integer.parseInt(getFileNameWithoutExtension(files[index].toString()));
				byte[] data = fileToByteArray(cacheIndex, fileIndex);
				if (data != null && data.length > 0) {
					decompressors[cacheIndex].put(data.length, data, fileIndex);
					System.out.println("Repacked " + fileIndex + ".");
				} else {
					System.out.println("Unable to locate index " + fileIndex + ".");
				}
			}
		} catch (Exception e) {
			System.out.println("Error packing cache index " + cacheIndex + ".");
		}
		System.out.println("Finished repacking " + cacheIndex + ".");
	}

	public byte[] fileToByteArray(int cacheIndex, int index) {
		try {
			if (indexLocation(cacheIndex, index).length() <= 0 || indexLocation(cacheIndex, index) == null) {
				return null;
			}
			File file = new File(indexLocation(cacheIndex, index));
			byte[] fileData = new byte[(int) file.length()];
			FileInputStream fis = new FileInputStream(file);
			fis.read(fileData);
			fis.close();
			return fileData;
		} catch (Exception e) {
			return null;
		}
	}

	public boolean inCircle(int circleX, int circleY, int clickX, int clickY, int radius) {
		return java.lang.Math.pow((circleX + radius - clickX), 2)
				+ java.lang.Math.pow((circleY + radius - clickY), 2) < java.lang.Math.pow(radius, 2);
	}

	private void processMainScreenClick() {
		if (anInt1021 != 0)
			return;
		if (super.clickMode3 != 1) {
			return;
		}
		if (canClickMap()) {
			final Point mapBase = gameFrame.getMapOffset(true);
			final int xOffset = mapBase.x + gameFrame.getMapImageOffset().x + 0;
			final int yOffset = mapBase.y + gameFrame.getMapImageOffset().y + 0;
			int clickX = super.saveClickX - xOffset;
			int clickY = super.saveClickY - yOffset;
			if (clickX >= 0 && clickX < 146 && clickY >= 0 && clickY < 151) {
				if (inCircle(0, 0, clickX, clickY, 77)) {
					clickX -= 73;
					clickY -= 77;
					int k = cameraHorizontal + minimapRotation & 0x7ff;
					int i1 = Texture.anIntArray1470[k];
					int j1 = Texture.anIntArray1471[k];
					i1 = i1 * (minimapZoom + 256) >> 8;
					j1 = j1 * (minimapZoom + 256) >> 8;
					int k1 = clickY * i1 + clickX * j1 >> 11;
					int l1 = clickY * j1 - clickX * i1 >> 11;
					int i2 = myPlayer.x + k1 >> 7;
					int j2 = myPlayer.y - l1 >> 7;
					boolean flag1 = doWalkTo(1, 0, 0, 0, myPlayer.smallY[0], 0, 0, j2, myPlayer.smallX[0], true, i2);
					if (flag1) {
						stream.writeWordBigEndian(clickX);
						stream.writeWordBigEndian(clickY);
						stream.writeShort(cameraHorizontal);
						stream.writeWordBigEndian(57);
						stream.writeWordBigEndian(minimapRotation);
						stream.writeWordBigEndian(minimapZoom);
						stream.writeWordBigEndian(89);
						stream.writeShort(myPlayer.x);
						stream.writeShort(myPlayer.y);
						stream.writeWordBigEndian(anInt1264);
						stream.writeWordBigEndian(63);
					}
				}
			}
			anInt1117++;
			if (anInt1117 > 1151) {
				anInt1117 = 0;
				stream.createFrame(246);
				stream.writeWordBigEndian(0);
				int l = stream.currentOffset;
				if ((int) (Math.random() * 2D) == 0)
					stream.writeWordBigEndian(101);
				stream.writeWordBigEndian(197);
				stream.writeShort((int) (Math.random() * 65536D));
				stream.writeWordBigEndian((int) (Math.random() * 256D));
				stream.writeWordBigEndian(67);
				stream.writeShort(14214);
				if ((int) (Math.random() * 2D) == 0)
					stream.writeShort(29487);
				stream.writeShort((int) (Math.random() * 65536D));
				if ((int) (Math.random() * 2D) == 0)
					stream.writeWordBigEndian(220);
				stream.writeWordBigEndian(180);
				stream.writeBytes(stream.currentOffset - l);
			}
		}
	}

	private String interfaceIntToString(int j) {
		if (j < 0x3b9ac9ff)
			return String.valueOf(j);
		else
			return "*";
	}

	private String processURL(String s) {
		if (s.contains("<url=") && s.endsWith("</url>")) {
			String text = s.substring(0, s.indexOf("<url="));
			s = s.substring(text.length() + 5).trim();
			String link = s.substring(0, s.indexOf(">"));
			s = s.substring(link.length() + 1).trim();
			String urlDisplay = s.substring(0, s.indexOf("</url>"));
			return text + " @red@" + urlDisplay;
		} else {
			return s;
		}
	}

	private String interfaceValuesToString(RSInterface rsInterface, String s) {
		if (s.indexOf("%") != -1) {
			do {
				int k7 = s.indexOf("%1");
				if (k7 == -1)
					break;
				s = s.substring(0, k7) + interfaceIntToString(extractInterfaceValues(rsInterface, 0))
						+ s.substring(k7 + 2);
			} while (true);
			do {
				int l7 = s.indexOf("%2");
				if (l7 == -1)
					break;
				s = s.substring(0, l7) + interfaceIntToString(extractInterfaceValues(rsInterface, 1))
						+ s.substring(l7 + 2);
			} while (true);
			do {
				int i8 = s.indexOf("%3");
				if (i8 == -1)
					break;
				s = s.substring(0, i8) + interfaceIntToString(extractInterfaceValues(rsInterface, 2))
						+ s.substring(i8 + 2);
			} while (true);
			do {
				int j8 = s.indexOf("%4");
				if (j8 == -1)
					break;
				s = s.substring(0, j8) + interfaceIntToString(extractInterfaceValues(rsInterface, 3))
						+ s.substring(j8 + 2);
			} while (true);
			do {
				int k8 = s.indexOf("%5");
				if (k8 == -1)
					break;
				s = s.substring(0, k8) + interfaceIntToString(extractInterfaceValues(rsInterface, 4))
						+ s.substring(k8 + 2);
			} while (true);
		}
		return s;
	}

	private void showErrorScreen() {
		Graphics g = getGameComponent().getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, 765, 503);
		method4(1);
		if (loadingError) {
			aBoolean831 = false;
			g.setFont(new Font("Helvetica", 1, 16));
			g.setColor(Color.yellow);
			int k = 35;
			g.drawString("Sorry, an error has occured whilst loading /v/scape", 30, k);
			k += 50;
			g.setColor(Color.white);
			g.drawString("To fix this try the following (in order):", 30, k);
			k += 50;
			g.setColor(Color.white);
			g.setFont(new Font("Helvetica", 1, 12));
			g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, k);
			k += 30;
			g.drawString("2: Try clearing your web-browsers cache from tools->internet options", 30, k);
			k += 30;
			g.drawString("3: Try using a different game-world", 30, k);
			k += 30;
			g.drawString("4: Try rebooting your computer", 30, k);
			k += 30;
			g.drawString("5: Try selecting a different version of Java from the play-game menu", 30, k);
		}
		if (genericLoadingError) {
			aBoolean831 = false;
			g.setFont(new Font("Helvetica", 1, 20));
			g.setColor(Color.white);
			g.drawString("Error - unable to load game!", 50, 50);
			g.drawString("To play /v/scape make sure you play from", 50, 100);
			g.drawString("vidyascape.org", 50, 150);
		}
		if (rsAlreadyLoaded) {
			aBoolean831 = false;
			g.setColor(Color.yellow);
			int l = 35;
			g.drawString("Error a copy of /v/scape already appears to be loaded", 30, l);
			l += 50;
			g.setColor(Color.white);
			g.drawString("To fix this try the following (in order):", 30, l);
			l += 50;
			g.setColor(Color.white);
			g.setFont(new Font("Helvetica", 1, 12));
			g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, l);
			l += 30;
			g.drawString("2: Try rebooting your computer, and reloading", 30, l);
			l += 30;
		}
	}

	public URL getCodeBase() {
		try {
			return new URL(server + ":" + (80 + portOff));
		} catch (Exception _ex) {
		}
		return null;
	}

	private void forceNPCUpdateBlock() {
		for (int j = 0; j < npcCount; j++) {
			int k = npcIndices[j];
			NPC npc = npcArray[k];
			if (npc != null)
				entityUpdateBlock(npc);
		}
	}

	private void entityUpdateBlock(Entity entity) {
		if (entity.x < 128 || entity.y < 128 || entity.x >= 13184 || entity.y >= 13184) {
			entity.anim = -1;
			entity.gfxId = -1;
			entity.startForceMovement = 0;
			entity.endForceMovement = 0;
			entity.x = entity.smallX[0] * 128 + entity.size * 64;
			entity.y = entity.smallY[0] * 128 + entity.size * 64;
			entity.method446();
		}
		if (entity == myPlayer && (entity.x < 1536 || entity.y < 1536 || entity.x >= 11776 || entity.y >= 11776)) {
			entity.anim = -1;
			entity.gfxId = -1;
			entity.startForceMovement = 0;
			entity.endForceMovement = 0;
			entity.x = entity.smallX[0] * 128 + entity.size * 64;
			entity.y = entity.smallY[0] * 128 + entity.size * 64;
			entity.method446();
		}
		if (entity.startForceMovement > loopCycle)
			refreshEntityPosition(entity);
		else if (entity.endForceMovement >= loopCycle)
			refreshEntityFaceDirection(entity);
		else
			getDegreesToTurn(entity);
		appendFocusDestination(entity);
		appendAnimation(entity);
	}

	private void refreshEntityPosition(Entity entity) {
		int i = entity.startForceMovement - loopCycle;
		int j = entity.anInt1543 * 128 + entity.size * 64;
		int k = entity.anInt1545 * 128 + entity.size * 64;
		entity.x += (j - entity.x) / i;
		entity.y += (k - entity.y) / i;
		entity.anInt1503 = 0;
		if (entity.direction == 0)
			entity.turnDirection = 1024;
		if (entity.direction == 1)
			entity.turnDirection = 1536;
		if (entity.direction == 2)
			entity.turnDirection = 0;
		if (entity.direction == 3)
			entity.turnDirection = 512;
	}

	private void refreshEntityFaceDirection(Entity entity) {
		if (entity.endForceMovement == loopCycle || entity.anim == -1 || entity.anInt1529 != 0
				|| entity.anInt1528 + 1 > Animation.anims[entity.anim].method258(entity.anInt1527)) {
			int i = entity.endForceMovement - entity.startForceMovement;
			int j = loopCycle - entity.startForceMovement;
			int k = entity.anInt1543 * 128 + entity.size * 64;
			int l = entity.anInt1545 * 128 + entity.size * 64;
			int i1 = entity.anInt1544 * 128 + entity.size * 64;
			int j1 = entity.anInt1546 * 128 + entity.size * 64;
			entity.x = (k * (i - j) + i1 * j) / i;
			entity.y = (l * (i - j) + j1 * j) / i;
		}
		entity.anInt1503 = 0;
		if (entity.direction == 0)
			entity.turnDirection = 1024;
		if (entity.direction == 1)
			entity.turnDirection = 1536;
		if (entity.direction == 2)
			entity.turnDirection = 0;
		if (entity.direction == 3)
			entity.turnDirection = 512;
		entity.face = entity.turnDirection;
	}

	private void getDegreesToTurn(Entity entity) {
		entity.anInt1517 = entity.standAnim;
		if (entity.smallXYIndex == 0) {
			entity.anInt1503 = 0;
			return;
		}
		if (entity.anim != -1 && entity.anInt1529 == 0) {
			Animation animation = Animation.anims[entity.anim];
			if (entity.anInt1542 > 0 && animation.anInt363 == 0) {
				entity.anInt1503++;
				return;
			}
			if (entity.anInt1542 <= 0 && animation.anInt364 == 0) {
				entity.anInt1503++;
				return;
			}
		}
		int i = entity.x;
		int j = entity.y;
		int k = entity.smallX[entity.smallXYIndex - 1] * 128 + entity.size * 64;
		int l = entity.smallY[entity.smallXYIndex - 1] * 128 + entity.size * 64;
		if (k - i > 256 || k - i < -256 || l - j > 256 || l - j < -256) {
			entity.x = k;
			entity.y = l;
			return;
		}
		if (i < k) {
			if (j < l)
				entity.turnDirection = 1280;
			else if (j > l)
				entity.turnDirection = 1792;
			else
				entity.turnDirection = 1536;
		} else if (i > k) {
			if (j < l)
				entity.turnDirection = 768;
			else if (j > l)
				entity.turnDirection = 256;
			else
				entity.turnDirection = 512;
		} else if (j < l)
			entity.turnDirection = 1024;
		else
			entity.turnDirection = 0;
		int i1 = entity.turnDirection - entity.face & 0x7ff;
		if (i1 > 1024)
			i1 -= 2048;
		int j1 = entity.turn180Anim;
		if (i1 >= -256 && i1 <= 256)
			j1 = entity.walkAnim;
		else if (i1 >= 256 && i1 < 768)
			j1 = entity.turn90CCWAnim;
		else if (i1 >= -768 && i1 <= -256)
			j1 = entity.turn90CWAnim;
		if (j1 == -1)
			j1 = entity.walkAnim;
		entity.anInt1517 = j1;
		int k1 = 4;
		if (entity.face != entity.turnDirection && entity.interactingEntity == -1 && entity.degreesToTurn != 0)
			k1 = 2;
		if (entity.smallXYIndex > 2)
			k1 = 6;
		if (entity.smallXYIndex > 3)
			k1 = 8;
		if (entity.anInt1503 > 0 && entity.smallXYIndex > 1) {
			k1 = 8;
			entity.anInt1503--;
		}
		if (entity.aBooleanArray1553[entity.smallXYIndex - 1])
			k1 <<= 1;
		if (k1 >= 8 && entity.anInt1517 == entity.walkAnim && entity.anInt1505 != -1)
			entity.anInt1517 = entity.anInt1505;
		if (i < k) {
			entity.x += k1;
			if (entity.x > k)
				entity.x = k;
		} else if (i > k) {
			entity.x -= k1;
			if (entity.x < k)
				entity.x = k;
		}
		if (j < l) {
			entity.y += k1;
			if (entity.y > l)
				entity.y = l;
		} else if (j > l) {
			entity.y -= k1;
			if (entity.y < l)
				entity.y = l;
		}
		if (entity.x == k && entity.y == l) {
			entity.smallXYIndex--;
			if (entity.anInt1542 > 0)
				entity.anInt1542--;
		}
	}

	private void appendFocusDestination(Entity entity) {
		if (entity.degreesToTurn == 0)
			return;
		if (entity.interactingEntity != -1 && entity.interactingEntity < 32768) {
			NPC npc = npcArray[entity.interactingEntity];
			if (npc != null) {
				int i1 = entity.x - npc.x;
				int k1 = entity.y - npc.y;
				if (i1 != 0 || k1 != 0)
					entity.turnDirection = (int) (Math.atan2(i1, k1) * 325.94900000000001D) & 0x7ff;
			}
		}
		if (entity.interactingEntity >= 32768) {
			int j = entity.interactingEntity - 32768;
			if (j == localPlayerIndex)
				j = myPlayerIndex;
			Player player = playerArray[j];
			if (player != null) {
				int l1 = entity.x - player.x;
				int i2 = entity.y - player.y;
				if (l1 != 0 || i2 != 0)
					entity.turnDirection = (int) (Math.atan2(l1, i2) * 325.94900000000001D) & 0x7ff;
			}
		}
		if ((entity.anInt1538 != 0 || entity.anInt1539 != 0) && (entity.smallXYIndex == 0 || entity.anInt1503 > 0)) {
			int k = entity.x - (entity.anInt1538 - baseX - baseX) * 64;
			int j1 = entity.y - (entity.anInt1539 - baseY - baseY) * 64;
			if (k != 0 || j1 != 0)
				entity.turnDirection = (int) (Math.atan2(k, j1) * 325.94900000000001D) & 0x7ff;
			entity.anInt1538 = 0;
			entity.anInt1539 = 0;
		}
		int l = entity.turnDirection - entity.face & 0x7ff;
		if (l != 0) {
			if (l < entity.degreesToTurn || l > 2048 - entity.degreesToTurn)
				entity.face = entity.turnDirection;
			else if (l > 1024)
				entity.face -= entity.degreesToTurn;
			else
				entity.face += entity.degreesToTurn;
			entity.face &= 0x7ff;
			if (entity.anInt1517 == entity.standAnim && entity.face != entity.turnDirection) {
				if (entity.anInt1512 != -1) {
					entity.anInt1517 = entity.anInt1512;
					return;
				}
				entity.anInt1517 = entity.walkAnim;
			}
		}
	}

	public void appendAnimation(Entity entity) {
		entity.aBoolean1541 = false;
		if (entity.anInt1517 != -1) {
			Animation animation = Animation.anims[entity.anInt1517];
			entity.anInt1519++;
			if (entity.anInt1518 < animation.anInt352 && entity.anInt1519 > animation.method258(entity.anInt1518)) {
				entity.anInt1519 = 0;
				entity.anInt1518++;
			}
			if (entity.anInt1518 >= animation.anInt352) {
				entity.anInt1519 = 0;
				entity.anInt1518 = 0;
			}
		}
		if (entity.gfxId != -1 && loopCycle >= entity.anInt1523) {
			if (entity.anInt1521 < 0)
				entity.anInt1521 = 0;
			Animation animation_1 = SpotAnim.cache[entity.gfxId].animationSequence;
			for (entity.anInt1522++; entity.anInt1521 < animation_1.anInt352
					&& entity.anInt1522 > animation_1.method258(entity.anInt1521); entity.anInt1521++)
				entity.anInt1522 -= animation_1.method258(entity.anInt1521);

			if (entity.anInt1521 >= animation_1.anInt352
					&& (entity.anInt1521 < 0 || entity.anInt1521 >= animation_1.anInt352))
				entity.gfxId = -1;
		}
		if (entity.anim != -1 && entity.anInt1529 <= 1) {
			Animation animation_2 = Animation.anims[entity.anim];
			if (animation_2.anInt363 == 1 && entity.anInt1542 > 0 && entity.startForceMovement <= loopCycle
					&& entity.endForceMovement < loopCycle) {
				entity.anInt1529 = 1;
				return;
			}
		}
		if (entity.anim != -1 && entity.anInt1529 == 0) {
			Animation animation_3 = Animation.anims[entity.anim];
			for (entity.anInt1528++; entity.anInt1527 < animation_3.anInt352
					&& entity.anInt1528 > animation_3.method258(entity.anInt1527); entity.anInt1527++)
				entity.anInt1528 -= animation_3.method258(entity.anInt1527);

			if (entity.anInt1527 >= animation_3.anInt352) {
				entity.anInt1527 -= animation_3.anInt356;
				entity.anInt1530++;
				if (entity.anInt1530 >= animation_3.anInt362)
					entity.anim = -1;
				if (entity.anInt1527 < 0 || entity.anInt1527 >= animation_3.anInt352)
					entity.anim = -1;
			}
			entity.aBoolean1541 = animation_3.aBoolean358;
		}
		if (entity.anInt1529 > 0)
			entity.anInt1529--;
	}

	private void drawGameScreen() {
		if (redrawGame) {
			redrawGame = false;
			if (frameMode == ScreenMode.FIXED) {
				topFrame.drawGraphics(0, super.graphics, 0);
				leftFrame.drawGraphics(4, super.graphics, 0);
			}
			redrawChatbox = true;
			redrawTab = true;
			redrawTabIcons = true;
			if (loadingStage != 2) {
				if (frameMode == ScreenMode.FIXED) {
					gameScreenImageProducer.drawGraphics(frameMode == ScreenMode.FIXED ? 4 : 0, super.graphics,
							frameMode == ScreenMode.FIXED ? 4 : 0);
					aRSImageProducer_1164.drawGraphics(0, super.graphics, 516);
				}
			}
		}
		if (loadingStage == 2) {
			drawGameWorld();
		}
		if (invOverlayInterfaceID != -1) {
			boolean bool = animateInterface(anInt945, invOverlayInterfaceID);
			if (bool) {
				redrawTab = true;
			}
		}
		if ((menuOpen && menuScreenArea == 1) || atInventoryInterfaceType == 2 || activeInterfaceType == 2) {
			redrawTab = true;
		}
		if (backDialogID == -1) {
			final int chatY = gameFrame.getChatOffsetY(true);
			final Rectangle chatBounds = gameFrame.getChatBounds();
			final int chatHeight = (int) chatBounds.getHeight() + 1;
			aClass9_1059.scrollPosition = (chatScrollMax - chatScrollPos - chatHeight);
			final int scrollBarX = (int) chatBounds.getWidth();
			final int scrollBarXMax = scrollBarX + 16;
			if (super.mouseX >= scrollBarX && super.mouseX <= scrollBarXMax && super.mouseY > chatY) {
				moveScroller(scrollBarX, chatHeight, super.mouseX, super.mouseY, aClass9_1059,
						chatY + (int) chatBounds.getY(), false, chatScrollMax);
			}
			int i = chatScrollMax - chatHeight - aClass9_1059.scrollPosition;
			if (i > chatScrollMax - chatHeight)
				i = chatScrollMax - chatHeight;
			if (i < 0)
				i = 0;
			if (chatScrollPos != i) {
				chatScrollPos = i;
				redrawChatbox = true;
			}
		}
		if (backDialogID == -1 && (inputDialogState >= 3 && inputDialogState <= 5)) {
			final int chatY = gameFrame.getChatOffsetY(true) + 14;
			final Rectangle chatBounds = gameFrame.getChatBounds();
			final int chatHeight = (int) chatBounds.getHeight() + 1;
			aClass9_1059.scrollPosition = searchResultScrollPos;
			final int scrollBarX = (int) chatBounds.getWidth();
			final int scrollBarXMax = scrollBarX + 16;
			if (super.mouseX >= scrollBarX && super.mouseX <= scrollBarXMax && super.mouseY > chatY) {
				moveScroller(scrollBarX, chatHeight, super.mouseX, super.mouseY, aClass9_1059,
						chatY + (int) chatBounds.getY(), false, searchResultScrollMax);
			}
			int i = aClass9_1059.scrollPosition;
			if (i > searchResultScrollMax - chatHeight)
				i = searchResultScrollMax - chatHeight;
			if (i < 0)
				i = 0;
			if (searchResultScrollPos != i) {
				searchResultScrollPos = i;
				redrawChatbox = true;
			}
			if (isChatArea()) {
				redrawChatbox = true;
			}
		}
		if (backDialogID != -1) {
			boolean flag2 = animateInterface(anInt945, backDialogID);
			if (flag2)
				redrawChatbox = true;
		}
		if (atInventoryInterfaceType == 3 || activeInterfaceType == 3 || aString844 != null
				|| (menuOpen && menuScreenArea == 2)) {
			redrawChatbox = true;
		}
		if (loadingStage == 2) {
			if (refreshMinimap) {
				refreshMinimap = false;
				refreshMinimap(plane);
			}
		}
		if (flashingSidebar != -1) {
			redrawTabIcons = true;
		}
		/*
		 * if(redrawTabIcons) { if(flashingSidebar != -1 && flashingSidebar == tabID) {
		 * flashingSidebar = -1; stream.createFrame(120);
		 * stream.writeWordBigEndian(tabID); } redrawTabIcons = false;
		 * aRSImageProducer_1125.initDrawingArea();
		 * gameScreenImageProducer.initDrawingArea(); }
		 */
		if (redrawTabIcons) {
			if (flashingSidebar != -1 && flashingSidebar == tabID) {
				flashingSidebar = -1;
				stream.createFrame(120);
				stream.writeWordBigEndian(tabID);
			}
		}
		if (frameMode == ScreenMode.FIXED) {
			drawGameframe();
		}
		anInt945 = 0;
	}

	private boolean buildFriendsListMenu(RSInterface class9) {
		int i = class9.contentType;
		if (i >= 1 && i <= 200 || i >= 701 && i <= 900) {
			if (i >= 801)
				i -= 701;
			else if (i >= 701)
				i -= 601;
			else if (i >= 101)
				i -= 101;
			else
				i--;
			menuActionName[menuActionRow] = "Remove @whi@" + friendsList[i];
			menuActionID[menuActionRow] = 792;
			menuActionRow++;
			menuActionName[menuActionRow] = "Message @whi@" + friendsList[i];
			menuActionID[menuActionRow] = 639;
			menuActionRow++;
			return true;
		}
		if (i >= 401 && i <= 500) {
			menuActionName[menuActionRow] = "Remove @whi@" + class9.disabledText;
			menuActionID[menuActionRow] = 322;
			menuActionRow++;
			return true;
		} else {
			return false;
		}
	}

	private boolean buildClanChatMenu(RSInterface class9) {
		int i = class9.contentType;
		if (i >= 20001 && i <= 20101) {
			final int pos = i - 20001;
			final int myClanRights = getClanRights(myPlayer.name);
			if (myClanRights > 0) {
				String chatMember = TextUtil.nameForLong(clanListAsLongs[pos]);
				if (chatMember != null && !chatMember.isEmpty()) {
					if (chatMember.startsWith("@cc1@") || chatMember.startsWith("@cc2@")
							|| chatMember.startsWith("@cc3@")) {
						chatMember = chatMember.substring(5);
					}
					chatMember = TextUtil.fixName(chatMember);
					final int clanMemberRights = clanListRights[pos];
					if (clanMemberRights >= 2 || chatMember.equalsIgnoreCase(myPlayer.name)) {
						return true;
					}
					menuActionName[menuActionRow] = "Ban @whi@" + chatMember;
					menuActionID[menuActionRow] = 1502;
					menuActionRow++;
					menuActionName[menuActionRow] = "Demote @whi@" + chatMember;
					menuActionID[menuActionRow] = 1501;
					menuActionRow++;
					menuActionName[menuActionRow] = "Promote @whi@" + chatMember;
					menuActionID[menuActionRow] = 1500;
					menuActionRow++;
				}
			}
			return true;
		}
		return false;
	}

	private int getClanRights(String nameCheck) {
		for (int j3 = 0; j3 < clanCount; j3++) {
			if (clanListAsLongs[j3] <= 0)
				continue;
			String chatMember = TextUtil.nameForLong(clanListAsLongs[j3]);
			if (chatMember == null || chatMember.isEmpty() || !chatMember.equalsIgnoreCase(nameCheck)) {
				continue;
			}
			return clanListRights[j3];
		}
		return 0;
	}

	private void createStationaryGraphics() { // method104
		AnimableObject class30_sub2_sub4_sub3 = (AnimableObject) incompleteAnimables.reverseGetFirst();
		for (; class30_sub2_sub4_sub3 != null; class30_sub2_sub4_sub3 = (AnimableObject) incompleteAnimables
				.reverseGetNext())
			if (class30_sub2_sub4_sub3.anInt1560 != plane || class30_sub2_sub4_sub3.aBoolean1567)
				class30_sub2_sub4_sub3.unlink();
			else if (loopCycle >= class30_sub2_sub4_sub3.anInt1564) {
				class30_sub2_sub4_sub3.method454(anInt945);
				if (class30_sub2_sub4_sub3.aBoolean1567)
					class30_sub2_sub4_sub3.unlink();
				else
					worldController.method285(class30_sub2_sub4_sub3.anInt1560, 0, class30_sub2_sub4_sub3.anInt1563, -1,
							class30_sub2_sub4_sub3.anInt1562, 60, class30_sub2_sub4_sub3.anInt1561,
							class30_sub2_sub4_sub3, false);
			}

	}

	public void drawBlackBox(int xPos, int yPos) {
		DrawingArea.drawPixels(71, yPos - 1, xPos - 2, 0x726451, 1);
		DrawingArea.drawPixels(69, yPos, xPos + 174, 0x726451, 1);
		DrawingArea.drawPixels(1, yPos - 2, xPos - 2, 0x726451, 178);
		DrawingArea.drawPixels(1, yPos + 68, xPos, 0x726451, 174);
		DrawingArea.drawPixels(71, yPos - 1, xPos - 1, 0x2E2B23, 1);
		DrawingArea.drawPixels(71, yPos - 1, xPos + 175, 0x2E2B23, 1);
		DrawingArea.drawPixels(1, yPos - 1, xPos, 0x2E2B23, 175);
		DrawingArea.drawPixels(1, yPos + 69, xPos, 0x2E2B23, 175);
		DrawingArea.method335(0, yPos, 174, 68, 220, xPos);
	}

	public void updateBankInterface(int selectedTab, int tabsUsed) {
		try {
			RSInterface[] bankComponents = RSInterface.interfaceCache[174];
			RSInterface previewInv = bankComponents[78];
			previewInv.inventoryItemIds[0] = 0;
			previewInv.inventoryStackSizes[0] = 0;
			for (int tab = 0; tab < 10; tab++) {
				int tabButtonID = 9 + (tab * 4);
				RSInterface tabButton = bankComponents[tabButtonID];
				RSInterface tabButtonHvrSpr = bankComponents[tabButtonID + 2];
				RSInterface tabButtonIco = bankComponents[tabButtonID + 3];
				RSInterface tabInventory = bankComponents[50 + tab];
				tabButton.disabledSprite = SpriteLoader.getSprite("bank", 0);
				tabButtonHvrSpr.disabledSprite = SpriteLoader.getSprite("bank", 1);
				if (tab > 0) {
					tabButton.tooltip = "";
					tabButton.isHidden = true;
					tabButtonIco.isHidden = true;
					if (tab >= tabsUsed) {
						if (tab == tabsUsed) {
							tabButton.tooltip = "New tab";
							tabButton.isHidden = false;
							tabButtonIco.isHidden = false;
						}
					} else {
						tabButton.tooltip = "View Tab @lre@" + tab;
						tabButton.isHidden = false;
						tabButtonIco.isHidden = true;
					}
					previewInv.inventoryItemIds[tab] = tabInventory.inventoryItemIds[0];
					previewInv.inventoryStackSizes[tab] = tabInventory.inventoryStackSizes[0];
				}
				if (tab <= 8) {
					RSInterface tabLine = bankComponents[60 + tab];
					RSInterface tabText = bankComponents[69 + tab];
					tabText.isHidden = true;
					tabLine.isHidden = true;
				}
				if (tab != selectedBankTab) {
					tabInventory.isHidden = true;
				} else {
					tabInventory.isHidden = false;
				}
			}
			if (selectedTab >= 0 && selectedTab <= 9) {
				int tabButtonID = 9 + (selectedTab * 4);
				RSInterface selectTabBtn = bankComponents[tabButtonID];
				RSInterface selectTabBtnHvrSpr = bankComponents[tabButtonID + 2];
				selectTabBtn.disabledSprite = SpriteLoader.getSprite("bank", 2);
				selectTabBtnHvrSpr.disabledSprite = SpriteLoader.getSprite("bank", 2);
			}
		} catch (Exception ex) {
		}
	}

	private int getInventoryHeight(RSInterface inventory) {
		return (getInvRowsUsed(inventory) * (32 + inventory.spritePaddingY));
	}

	private int getInvRowsUsed(RSInterface inventory) {
		int itemCount = getItemCount(inventory);
		return (int) Math.ceil((double) itemCount / inventory.width);
	}

	private int getItemCount(RSInterface inventory) {
		int itemCount = 0;
		if (inventory != null && inventory.type == 2) {
			if (inventory.inventoryItemIds != null) {
				for (int i = 0; i < inventory.inventoryItemIds.length; i++) {
					if (inventory.inventoryItemIds[i] > 0) {
						itemCount++;
					}
				}
			}
		}
		return itemCount;
	}

	private final static int BANK_SEPERATION_AMOUNT = 14;

	private void drawBank(int baseX, int baseY) {
		if (openInterfaceID != 174)
			return;
		try {
			RSInterface[] bankComponents = RSInterface.interfaceCache[174];
			RSInterface invContainer = bankComponents[49];
			RSInterface mainInventory = bankComponents[50];
			String bankTitle = bankComponents[1].disabledText.toLowerCase();
			mainInventory.height = getInvRowsUsed(mainInventory);
			for (int i = 0; i < mainInventory.inventoryItemIds.length; i++)
			{
				if (mainInventory.inventoryItemIds[i] > 0 && mainInventory.inventoryItemIds[i] < 65535)
				{
//					if (searchString != "" && searchString != null && searchString.length() != 0)
//					{
//						ItemDef item = ItemDef.forID(mainInventory.inventoryItemIds[i] - 1);
//						String name = item.name.toLowerCase();
//						if (!name.contains(searchString.toLowerCase()))
//						{
//							mainInventory.inventoryItemIds = removeElement(mainInventory.inventoryItemIds, i);
//							mainInventory.inventoryStackSizes = removeElement(mainInventory.inventoryStackSizes, i);
//						}
////						if (!ItemDef.forID(tabInventory.inventoryItemIds[i] - 1).name.toLowerCase().contains(searchString.trim().toLowerCase()))
////						{
////							tabInventory.inventoryItemIds[i] = 0;
////							tabInventory.inventoryStackSizes[i] = 0;
////						}
//					}
				}
			}
			if (selectedBankTab <= 0) {
				int scrollMax = getInventoryHeight(mainInventory) + BANK_SEPERATION_AMOUNT;
				int drawY = mainInventory.y + scrollMax;
				for (int tab = 0; tab < 9; tab++) {
					RSInterface tabInventory = bankComponents[51 + tab];
					RSInterface tabLine = bankComponents[60 + tab];
					RSInterface tabText = bankComponents[69 + tab];
					int itemCount = getItemCount(tabInventory);
					int scrollHeight = getInventoryHeight(tabInventory) + BANK_SEPERATION_AMOUNT;
					boolean hidden = (itemCount <= 0);
					tabText.isHidden = hidden;
					tabInventory.isHidden = hidden;
					tabLine.isHidden = hidden;
					if (hidden)
						continue;
					tabText.y = drawY - 22;
					tabLine.y = drawY - 8;
					tabInventory.y = drawY;
					tabInventory.height = getInvRowsUsed(tabInventory);
					drawY += scrollHeight;
					scrollMax += scrollHeight;
//					for (int i = 0; i < tabInventory.inventoryItemIds.length; i++)
//					{
//						if (tabInventory.inventoryItemIds[i] > 0 && tabInventory.inventoryItemIds[i] < 65535)
//						{
//							if (searchString != "" && searchString != null && searchString.length() != 0)
//							{
//								ItemDef item = ItemDef.forID(tabInventory.inventoryItemIds[i] - 1);
//								String name = item.name.toLowerCase();
//								if (!name.contains(searchString.toLowerCase()))
//								{
//									tabInventory.inventoryItemIds = removeElement(tabInventory.inventoryItemIds, i);
//									tabInventory.inventoryStackSizes = removeElement(tabInventory.inventoryStackSizes, i);
//								}
//							}
//						}
//					}
				}
				if (invContainer.scrollMax != scrollMax)
					invContainer.scrollMax = scrollMax;
			} else {
				int scrollMax = 0;
				RSInterface tabInventory = bankComponents[50 + selectedBankTab];
				if (tabInventory != null) {
					tabInventory.y = mainInventory.y;
					tabInventory.height = getInvRowsUsed(tabInventory);
					scrollMax = getInventoryHeight(tabInventory);
				}
				if (invContainer.scrollMax != scrollMax)
					invContainer.scrollMax = scrollMax;
			}
		} catch (Exception ex) {
		}
	}

	public static int[] removeElement(int[] original, int element){
	    int[] n = new int[original.length - 1];
	    System.arraycopy(original, 0, n, 0, element );
	    System.arraycopy(original, element+1, n, element, original.length - element-1);
	    return n;
	}

	private boolean mouseOverBankTabs() {
		int x = frameMode == ScreenMode.FIXED ? 0 : (frameWidth / 2) - 356;
		int y = frameMode == ScreenMode.FIXED ? 0 : (frameHeight / 2) - 230;
		if (super.mouseY >= (y + 40) && super.mouseY <= (y + 80)) {
			if (super.mouseX >= (x + 54) && super.mouseX <= (x + 464)) {
				return true;
			}
		}
		return false;
	}

	private int getBankTabIndex() {
		int x = frameMode == ScreenMode.FIXED ? 0 : (frameWidth / 2) - 356;
		int y = frameMode == ScreenMode.FIXED ? 0 : (frameHeight / 2) - 230;
		if (super.mouseY >= (y + 40) && super.mouseY <= (y + 80)) {
			for (int tab = 0; tab < 10; tab++) {
				int xOff = 54 + (40 * tab);
				int xOffEnd = xOff + 40;
				if (super.mouseX >= (x + xOff) && super.mouseX <= (x + xOffEnd)) {
					return tab;
				}
			}
		}
		return -1;
	}

	private void processBankClick() {
		if (openInterfaceID != 174)
			return;
		if (anInt1084 < 11403314 || anInt1084 > 11403324) {
			return;
		}
		int tabIndex = getBankTabIndex();
		if (tabIndex >= 0 && tabIndex <= 10) {
			stream.createFrame(213); // SWAP item inventory
			stream.writeDWord(anInt1084);
			stream.writeDWord((11403273 + (tabIndex * 3)));
			stream.method433(lastMouseInvInterfaceIndex);
			stream.method431(tabIndex);
		}
	}

	public void drawInterface(int drawX, int drawY, int width, int height, RSInterface[] components, int parentID,
			int scrollPos) {
		int i1 = DrawingArea.topX;
		int j1 = DrawingArea.topY;
		int k1 = DrawingArea.bottomX;
		int l1 = DrawingArea.bottomY;
		if (components == null)
			return;
		DrawingArea.setDrawingArea(drawY + height, drawX, drawX + width, drawY);
		for (int childID = 0; childID < components.length; childID++) {
			RSInterface rsInterface = components[childID];
			if (rsInterface == null)
				continue;
			if (rsInterface.parentID != parentID)
				continue;
			RSInterface parentInterface = null;
			if (parentID >= 0) {
				parentInterface = RSInterface.getInterface(parentID);
			}
			if (rsInterface.toggledContainer) {
				if (rsInterface.type == 0 && !interfaceIsSelected(rsInterface)) {
					continue;
				}
			} else {
				if (rsInterface.isHidden && anInt1026 != rsInterface.interfaceHash
						&& anInt1048 != rsInterface.interfaceHash && anInt1039 != rsInterface.interfaceHash)
					continue;
			}
			int childX = rsInterface.x + rsInterface.xOffset + drawX;
			int childY = (rsInterface.y + rsInterface.yOffset + drawY) - scrollPos;// - scrollPos;
			if (rsInterface.contentType > 0) {
				drawFriendsListOrWelcomeScreen(rsInterface);
			}
			if (rsInterface.type == 0) {
				if (rsInterface.interfaceID == 174) {
					drawBank(childX, childY);
				}
				if (rsInterface.scrollPosition > rsInterface.scrollMax - rsInterface.height)
					rsInterface.scrollPosition = rsInterface.scrollMax - rsInterface.height;
				if (rsInterface.scrollPosition < 0)
					rsInterface.scrollPosition = 0;
				drawInterface(childX, childY, rsInterface.width, rsInterface.height, components,
						rsInterface.interfaceHash, rsInterface.scrollPosition);
				if (rsInterface.scrollMax > rsInterface.height)
					drawScrollbar(rsInterface.height, rsInterface.scrollPosition, childY, childX + rsInterface.width,
							rsInterface.scrollMax);
			} else if (rsInterface.type != 1) {
				if (rsInterface.type == 2) {
					int i3 = 0;
					for (int l3 = 0; l3 < rsInterface.height; l3++) {
						for (int l4 = 0; l4 < rsInterface.width; l4++) {
							int k5 = childX + l4 * (32 + rsInterface.spritePaddingX);
							int j6 = childY + l3 * (32 + rsInterface.spritePaddingY);
							if (i3 < 20) {
								k5 += rsInterface.spritesX[i3];
								j6 += rsInterface.spritesY[i3];
							}
							if (rsInterface.inventoryItemIds[i3] > 0) {
								int k6 = 0;
								int j7 = 0;
								int j9 = rsInterface.inventoryItemIds[i3] - 1;
								if (k5 > DrawingArea.topX - 32 && k5 < DrawingArea.bottomX && j6 > DrawingArea.topY - 32
										&& j6 < DrawingArea.bottomY
										|| activeInterfaceType != 0 && lastMouseInvInterfaceIndex == i3) {
									int l9 = 0;
									if (itemSelected == 1 && anInt1283 == i3 && anInt1284 == rsInterface.interfaceHash)
										l9 = 0xffffff;
									Sprite class30_sub2_sub1_sub1_2 = ItemDef.getSprite(j9,
											rsInterface.inventoryStackSizes[i3], l9);
									if (class30_sub2_sub1_sub1_2 != null) {
										if (parentInterface != null && activeInterfaceType != 0
												&& lastMouseInvInterfaceIndex == i3
												&& anInt1084 == rsInterface.interfaceHash) {
											k6 = super.mouseX - anInt1087;
											j7 = super.mouseY - anInt1088;
											if (k6 < 5 && k6 > -5)
												k6 = 0;
											if (j7 < 5 && j7 > -5)
												j7 = 0;
											if (anInt989 < 5) {
												k6 = 0;
												j7 = 0;
											}
											boolean preventScrolling = (rsInterface.interfaceID == 174
													&& mouseOverBankTabs());
											if (!preventScrolling) {
												if (j6 + j7 < DrawingArea.topY && parentInterface.scrollPosition > 0) {
													int i10 = (anInt945 * (DrawingArea.topY - j6 - j7)) / 3;
													if (i10 > anInt945 * 10)
														i10 = anInt945 * 10;
													if (i10 > parentInterface.scrollPosition)
														i10 = parentInterface.scrollPosition;
													parentInterface.scrollPosition -= i10;
													anInt1088 += i10;
												}
												if (j6 + j7 + 32 > DrawingArea.bottomY
														&& parentInterface.scrollPosition < parentInterface.scrollMax
																- parentInterface.height) {
													int j10 = (anInt945 * ((j6 + j7 + 32) - DrawingArea.bottomY)) / 3;
													if (j10 > anInt945 * 10)
														j10 = anInt945 * 10;
													if (j10 > parentInterface.scrollMax - parentInterface.height
															- parentInterface.scrollPosition)
														j10 = parentInterface.scrollMax - parentInterface.height
																- parentInterface.scrollPosition;
													parentInterface.scrollPosition += j10;
													anInt1088 -= j10;
												}
											}
											DrawingArea.setDrawingArea(l1, i1, k1, j1);
											class30_sub2_sub1_sub1_2.drawSprite1(k5 + k6, j6 + j7);
											DrawingArea.setDrawingArea(drawY + height, drawX, drawX + width, drawY);
										} else if (atInventoryInterfaceType != 0 && atInventoryIndex == i3
												&& atInventoryInterface == rsInterface.interfaceHash) {
											class30_sub2_sub1_sub1_2.drawSprite1(k5, j6);
										} else {
											class30_sub2_sub1_sub1_2.drawSprite(k5, j6);
										}
										if (rsInterface.drawInvAmount && (class30_sub2_sub1_sub1_2.maxWidth == 33
												|| rsInterface.inventoryStackSizes[i3] != 1)) {
											int k10 = rsInterface.inventoryStackSizes[i3];
											smallFont.method385(0, intToKOrMil(k10), j6 + 10 + j7, k5 + 1 + k6);
											smallFont.method385(0xffff00, intToKOrMil(k10), j6 + 9 + j7, k5 + k6);
										}
									}
								}
							} else if (rsInterface.sprites != null && i3 < 20) {
								Sprite class30_sub2_sub1_sub1_1 = rsInterface.sprites[i3];
								if (class30_sub2_sub1_sub1_1 != null)
									class30_sub2_sub1_sub1_1.drawSprite(k5, j6);
							}
							i3++;
						}
					}
				} else if (rsInterface.type == 3) {
					boolean hoveringOver = false;
					if (anInt1039 == rsInterface.interfaceHash || anInt1048 == rsInterface.interfaceHash
							|| anInt1026 == rsInterface.interfaceHash)
						hoveringOver = true;
					int boxColor;
					if (interfaceIsSelected(rsInterface)) {
						boxColor = rsInterface.enabledColor;
						if (hoveringOver && rsInterface.enabledMouseOverColor != 0)
							boxColor = rsInterface.enabledMouseOverColor;
					} else {
						boxColor = rsInterface.disabledColor;
						if (hoveringOver && rsInterface.disabledMouseOverColor != 0)
							boxColor = rsInterface.disabledMouseOverColor;
					}
					if (rsInterface.opacity == 0) {
						if (rsInterface.filled)
							DrawingArea.drawPixels(rsInterface.height, childY, childX, boxColor, rsInterface.width);
						else
							DrawingArea.fillPixels(childX, rsInterface.width, rsInterface.height, boxColor, childY);
					} else if (rsInterface.filled)
						DrawingArea.method335(boxColor, childY, rsInterface.width, rsInterface.height,
								256 - (rsInterface.opacity & 0xff), childX);
					else
						DrawingArea.method338(childY, rsInterface.height, 256 - (rsInterface.opacity & 0xff), boxColor,
								rsInterface.width, childX);
				} else if (rsInterface.type == 4) {
					DrawingArea textDraw = rsInterface.getFont();
					String s = rsInterface.disabledText;
					boolean flag1 = false;
					if (anInt1039 == rsInterface.interfaceHash || anInt1048 == rsInterface.interfaceHash
							|| anInt1026 == rsInterface.interfaceHash)
						flag1 = true;
					int textColor;
					if (interfaceIsSelected(rsInterface)) {
						textColor = rsInterface.enabledColor;
						if (flag1 && rsInterface.enabledMouseOverColor != 0)
							textColor = rsInterface.enabledMouseOverColor;
						if (rsInterface.enabledText.length() > 0)
							s = rsInterface.enabledText;
					} else {
						textColor = rsInterface.disabledColor;
						if (flag1 && rsInterface.disabledMouseOverColor != 0)
							textColor = rsInterface.disabledMouseOverColor;
					}
					if (rsInterface.actionType == 6 && continueDialogue) {
						s = "Please wait...";
						textColor = rsInterface.disabledColor;
					}
					if ((backDialogID != -1 || dialogID != -1
							|| rsInterface.disabledText.toLowerCase().contains("click here to continue"))
							&& (rsInterface.interfaceID == backDialogID || rsInterface.interfaceID == dialogID)) {
						if (textColor == 0xffff00)
							textColor = 255;
						if (textColor == 49152)
							textColor = 0xffffff;
					}
					s = processURL(s);
					s = interfaceValuesToString(rsInterface, s);
					if (rsInterface.fontSystem == 1) {
						s = legacyColorConvert(s);
						if (textDraw instanceof RSFont) {
							((RSFont) textDraw).drawWrappedText(s, childX, childY, rsInterface.width,
									rsInterface.height, textColor, rsInterface.textShadow ? 0 : -1,
									rsInterface.horizontalAlignment, rsInterface.verticalAlignment,
									rsInterface.lineSpacing);
						}
					} else {
						int textCharHeight = 0;
						if (textDraw instanceof TextDrawingArea) {
							textCharHeight = ((TextDrawingArea) textDraw).anInt1497;
						} else if (textDraw instanceof RSFont) {
							textCharHeight = ((RSFont) textDraw).baseCharacterHeight;
						}
						for (int l6 = childY + textCharHeight; s.length() > 0; l6 += textCharHeight) {
							int nlIndex = s.indexOf("\\n");
							int brIndex = s.indexOf("<br>");
							String s1;
							if (nlIndex != -1) {
								s1 = s.substring(0, nlIndex);
								s = s.substring(nlIndex + 2);
							} else if (brIndex != -1) {
								s1 = s.substring(0, brIndex);
								s = s.substring(brIndex + 4);
							} else {
								s1 = s;
								s = "";
							}
							if (rsInterface.centerText) {
								if (textDraw instanceof TextDrawingArea) {
									((TextDrawingArea) textDraw).method382(textColor, childX + rsInterface.width / 2,
											s1, l6, rsInterface.textShadow);
								} else if (textDraw instanceof RSFont) {
									((RSFont) textDraw).drawCenteredString(s1, childX + rsInterface.width / 2, l6,
											textColor, rsInterface.textShadow ? 0 : -1);
								}
							} else {
								if (textDraw instanceof TextDrawingArea) {
									((TextDrawingArea) textDraw).method389(rsInterface.textShadow, childX, textColor,
											s1, l6);
								} else if (textDraw instanceof RSFont) {
									((RSFont) textDraw).drawBasicString(s1, childX, l6, textColor,
											rsInterface.textShadow ? 0 : -1);
								}
							}
						}
					}
				} else if (rsInterface.type == 5) {
					Sprite sprite = rsInterface.getSprite(interfaceIsSelected(rsInterface));
					if (sprite != null) {
						if (spellSelected == 1 && rsInterface.interfaceHash == spellID && spellID != 0)
							sprite.drawSprite(childX, childY, 0xffffff);
						else
							sprite.drawSprite(childX, childY);
					}
				} else if (rsInterface.type == 6) {
					int k3 = Texture.textureInt1;
					int j4 = Texture.textureInt2;
					Texture.textureInt1 = childX + rsInterface.width / 2;
					Texture.textureInt2 = childY + rsInterface.height / 2;
					int i5 = Texture.anIntArray1470[rsInterface.modelRotationX] * rsInterface.modelZoom >> 16;
					int l5 = Texture.anIntArray1471[rsInterface.modelRotationX] * rsInterface.modelZoom >> 16;
					boolean flag2 = interfaceIsSelected(rsInterface);
					int i7;
					if (flag2)
						i7 = rsInterface.secondaryAnimationId;
					else
						i7 = rsInterface.defaultAnimationId;
					Model model;
					if (i7 == -1) {
						model = rsInterface.getAnimatedMedia(-1, -1, flag2);
					} else {
						Animation animation = Animation.anims[i7];
						model = rsInterface.getAnimatedMedia(animation.anIntArray354[rsInterface.anInt246],
								animation.anIntArray353[rsInterface.anInt246], flag2);
					}
					if (model != null)
						model.method482(rsInterface.modelRotationY, 0, rsInterface.modelRotationX, 0, i5, l5);
					Texture.textureInt1 = k3;
					Texture.textureInt2 = j4;
				} else if (rsInterface.type == 7) {
					DrawingArea textDraw = rsInterface.getFont();
					int k4 = 0;
					for (int column = 0; column < rsInterface.width; column++) {
						for (int row = 0; row < rsInterface.height; row++) {
							if (rsInterface.inventoryItemIds[k4] > 0) {
								ItemDef itemDef = ItemDef.forID(rsInterface.inventoryItemIds[k4] - 1);
								String s2 = itemDef.name;
								if (itemDef.stackable || rsInterface.inventoryStackSizes[k4] != 1)
									s2 = s2 + " x" + intToKOrMilLongName(rsInterface.inventoryStackSizes[k4]);
								int itemX = childX + (column * (115 + rsInterface.spritePaddingX));
								int itemY = childY + (row * (12 + rsInterface.spritePaddingY));
								if (rsInterface.fontSystem == 1) {
									if (textDraw instanceof RSFont) {
										RSFont font = (RSFont) textDraw;
										if (rsInterface.horizontalAlignment == 0) {
											font.drawBasicString(s2, itemX, itemY, rsInterface.disabledColor,
													rsInterface.textShadow ? 0 : -1);
										} else if (rsInterface.horizontalAlignment == 1) {
											font.drawCenteredString(s2, itemX + rsInterface.width / 2, itemY,
													rsInterface.disabledColor, rsInterface.textShadow ? 0 : -1);
										} else {
											font.drawStringRight(s2, itemX + rsInterface.width - 1, itemY,
													rsInterface.disabledColor, rsInterface.textShadow ? 0 : -1);
										}
									}
								} else {
									if (rsInterface.centerText) {
										if (textDraw instanceof TextDrawingArea) {
											((TextDrawingArea) textDraw).method382(rsInterface.disabledColor,
													itemX + rsInterface.width / 2, s2, itemY, rsInterface.textShadow);
										} else if (textDraw instanceof RSFont) {
											((RSFont) textDraw).drawCenteredString(s2, itemX + rsInterface.width / 2,
													itemY, rsInterface.disabledColor, rsInterface.textShadow ? 0 : -1);
										}
									} else {
										if (textDraw instanceof TextDrawingArea) {
											((TextDrawingArea) textDraw).method389(rsInterface.textShadow, itemX,
													rsInterface.disabledColor, s2, itemY);
										} else if (textDraw instanceof RSFont) {
											((RSFont) textDraw).drawBasicString(s2, itemX, itemY,
													rsInterface.disabledColor, rsInterface.textShadow ? 0 : -1);
										}
									}
								}
							}
							k4++;
						}
					}
				} else if (rsInterface.type == 8 && (anInt1500 == rsInterface.interfaceHash
						|| anInt1044 == rsInterface.interfaceHash || anInt1129 == rsInterface.interfaceHash)
						&& anInt1501 == 0 && !menuOpen) {
					int boxWidth = 0;
					int boxHeight = 0;
					TextDrawingArea textDrawingArea_2 = regularFont;
					String hoverMessage = rsInterface.disabledText;
					if (interfaceIsSelected(rsInterface)) {
						if (rsInterface.enabledText.length() > 0) {
							hoverMessage = rsInterface.enabledText;
						}
					}
					hoverMessage = interfaceValuesToString(rsInterface, hoverMessage);
					for (String s1 = hoverMessage; s1.length() > 0;) {
						int nlIndex = s1.indexOf("\\n");
						int brIndex = s1.indexOf("<br>");
						String s4;
						if (nlIndex != -1) {
							s4 = s1.substring(0, nlIndex);
							s1 = s1.substring(nlIndex + 2);
						} else if (brIndex != -1) {
							s4 = s1.substring(0, brIndex);
							s1 = s1.substring(brIndex + 4);
						} else {
							s4 = s1;
							s1 = "";
						}
						int j10 = textDrawingArea_2.getTextWidth(s4);
						if (j10 > boxWidth) {
							boxWidth = j10;
						}
						boxHeight += textDrawingArea_2.anInt1497 + 1;
					}
					boxWidth += 6;
					boxHeight += 7;
					int boxPosX = childX;
					int boxPosY = childY + rsInterface.height + 2;
					if (boxPosX + boxWidth > (drawX + width)) {
						boxPosX = (drawX + width) - boxWidth;
					}
					if (boxPosX < drawX) {
						boxPosX = drawX;
					}
					if (boxPosY + boxHeight > (drawY + height)) {
						boxPosY = (drawY + height) - boxHeight; // height - boxHeight;
					}
					if (boxPosY < childY) {
						if (boxPosY + boxHeight > childY) {
							boxPosY = childY - boxHeight - 2;
						}
					}
					if (boxPosY > childY) {
						if (boxPosY < (childY + rsInterface.height)) {
							boxPosY = childY - boxHeight - 2;
						}
					}
					if (boxPosY < drawY) {
						boxPosY = drawY;
					}
					if (boxPosY + boxHeight >= frameHeight) {
						boxPosY = frameHeight - boxHeight;
					}
					DrawingArea.drawPixels(boxHeight, boxPosY, boxPosX, 0xFFFFA0, boxWidth);
					DrawingArea.fillPixels(boxPosX, boxWidth, boxHeight, 0, boxPosY);
					String s2 = interfaceValuesToString(rsInterface, hoverMessage);
					for (int j11 = boxPosY + textDrawingArea_2.anInt1497 + 2; s2
							.length() > 0; j11 += textDrawingArea_2.anInt1497 + 1) {// anInt1497
						int nlIndex = s2.indexOf("\\n");
						int brIndex = s2.indexOf("<br>");
						String s5;
						if (nlIndex != -1) {
							s5 = s2.substring(0, nlIndex);
							s2 = s2.substring(nlIndex + 2);
						} else if (brIndex != -1) {
							s5 = s2.substring(0, brIndex);
							s2 = s2.substring(brIndex + 4);
						} else {
							s5 = s2;
							s2 = "";
						}
						if (rsInterface.centerText) {
							textDrawingArea_2.method382(0, boxPosX + rsInterface.width / 2, s5, j11, false);
						} else {
							if (s5.contains("\\r")) {
								String text = s5.substring(0, s5.indexOf("\\r"));
								String text2 = s5.substring(s5.indexOf("\\r") + 2);
								textDrawingArea_2.method389(false, boxPosX + 3, 0, text, j11);
								int rightX = boxWidth + boxPosX - textDrawingArea_2.getTextWidth(text2) - 2;
								textDrawingArea_2.method389(false, rightX, 0, text2, j11);
							} else {
								textDrawingArea_2.method389(false, boxPosX + 3, 0, s5, j11);
							}
						}
					}
				}
			}
		}
		DrawingArea.setDrawingArea(l1, i1, k1, j1);
	}

	private void method107(int i, int j, Stream stream, Player player) {
		if ((i & 0x400) != 0) {
			player.anInt1543 = stream.method428();
			player.anInt1545 = stream.method428();
			player.anInt1544 = stream.method428();
			player.anInt1546 = stream.method428();
			player.startForceMovement = stream.method436() + loopCycle;
			player.endForceMovement = stream.method435() + loopCycle;
			player.direction = stream.method428();
			player.method446();
		}
		if ((i & 0x100) != 0) {
			player.gfxId = stream.method434();
			int k = stream.readDWord();
			player.anInt1524 = k >> 16;
			player.anInt1523 = loopCycle + (k & 0xffff);
			player.anInt1521 = 0;
			player.anInt1522 = 0;
			if (player.anInt1523 > loopCycle)
				player.anInt1521 = -1;
			if (player.gfxId == 65535)
				player.gfxId = -1;
		}
		if ((i & 8) != 0) {
			int l = stream.method434();
			if (l == 65535)
				l = -1;
			int i2 = stream.method427();
			if (l == player.anim && l != -1) {
				int i3 = Animation.anims[l].anInt365;
				if (i3 == 1) {
					player.anInt1527 = 0;
					player.anInt1528 = 0;
					player.anInt1529 = i2;
					player.anInt1530 = 0;
				}
				if (i3 == 2)
					player.anInt1530 = 0;
			} else if (l == -1 || player.anim == -1
					|| Animation.anims[l].anInt359 >= Animation.anims[player.anim].anInt359) {
				player.anim = l;
				player.anInt1527 = 0;
				player.anInt1528 = 0;
				player.anInt1529 = i2;
				player.anInt1530 = 0;
				player.anInt1542 = player.smallXYIndex;
			}
		}
		if ((i & 4) != 0) {
			player.textSpoken = stream.readString();
			if (player.textSpoken.charAt(0) == '~') {
				player.textSpoken = player.textSpoken.substring(1);
				pushMessage(player.textSpoken, 2, player.name);
			} else if (player == myPlayer) {
				pushMessage(player.textSpoken, 2, player.name);
			}
			player.textColour = 0;
			player.textEffect = 0;
			player.textCycle = 150;
		}
		if ((i & 0x80) != 0) {
			// right fucking here
			int i1 = stream.method434();
			int j2 = stream.readUnsignedByte();
			int j3 = stream.method427();
			int k3 = stream.currentOffset;
			if (player.name != null && player.visible) {
				long l3 = TextUtil.longForName(player.name);
				boolean flag = false;
				if (j2 <= 1) {
					for (int i4 = 0; i4 < ignoreCount; i4++) {
						if (ignoreListAsLongs[i4] != l3)
							continue;
						flag = true;
						break;
					}

				}
				if (!flag && onTutorialIsland == 0)
					try {
						aStream_834.currentOffset = 0;
						stream.method442(j3, 0, aStream_834.buffer);
						aStream_834.currentOffset = 0;
						String s = TextInput.method525(j3, aStream_834);
						// s = Censor.doCensor(s);
						player.textSpoken = s;
						player.textColour = i1 >> 8;
						player.privilege = j2;
						player.textEffect = i1 & 0xff;
						player.textCycle = 150;
						int channel = j2 >= 1 ? 1 : 2;
						if (player.ironMan == 1) {
							pushMessage(s, channel, "@irn@" + player.name, j2);
						} else if (player.ironMan == 2) {
							pushMessage(s, channel, "@hci@" + player.name, j2);
						} else if (player.ironMan == 3) {
							pushMessage(s, channel, "@ult@" + player.name, j2);
						} else {
							pushMessage(s, channel, player.name, j2);
						}
					} catch (Exception exception) {
						Signlink.reportError("cde2");
					}
			}
			stream.currentOffset = k3 + j3;
		}
		if ((i & 1) != 0) {
			player.interactingEntity = stream.method434();
			if (player.interactingEntity == 65535)
				player.interactingEntity = -1;
		}
		if ((i & 0x10) != 0) {
			int j1 = stream.method427();
			byte abyte0[] = new byte[j1];
			Stream stream_1 = new Stream(abyte0);
			stream.readBytes(j1, 0, abyte0);
			aStreamArray895s[j] = stream_1;
			player.updatePlayer(stream_1);
		}
		if ((i & 2) != 0) {
			player.anInt1538 = stream.method436();
			player.anInt1539 = stream.method434();
		}
		if ((i & 0x20) != 0) {
			int k1 = stream.readUnsignedByte();
			int k2 = stream.method426();
			player.updateHitData(k2, k1, loopCycle);
			player.loopCycleStatus = loopCycle + 300;
			player.currentHealth = stream.method427();
			player.maxHealth = stream.readUnsignedByte();
		}
		if ((i & 0x200) != 0) {
			int l1 = stream.readUnsignedByte();
			int l2 = stream.method428();
			player.updateHitData(l2, l1, loopCycle);
			player.loopCycleStatus = loopCycle + 300;
			player.currentHealth = stream.readUnsignedByte();
			player.maxHealth = stream.method427();
		}
	}

	private boolean canMouseDragCamera() {
		if (super.clickType == 5 || super.altKeyDown && super.clickType == 5) {
			return true;
		}
		return false;
	}

	public void mouseCameraDrag(int i, int j) {
		if (!SettingsManager.middleMouseCamera || !canMouseDragCamera() || !loggedIn)
			return;
		int xSense = SettingsManager.camDragSensitivity;
		if (xSense < 1)
			xSense = 1;
		if (xSense > 10)
			xSense = 10;
		this.anInt1186 += i * xSense;
		this.anInt1187 += j << 1;
	}

	private void method108() {
		try {
			int j = myPlayer.x + cameraOffsetX;
			int k = myPlayer.y + cameraOffsetY;
			if (anInt1014 - j < -500 || anInt1014 - j > 500 || anInt1015 - k < -500 || anInt1015 - k > 500) {
				anInt1014 = j;
				anInt1015 = k;
			}
			if (anInt1014 != j)
				anInt1014 += (j - anInt1014) / 16;
			if (anInt1015 != k)
				anInt1015 += (k - anInt1015) / 16;
			if (super.keyArray[1] == 1)
				anInt1186 += (-24 - anInt1186) / 2;
			else if (super.keyArray[2] == 1)
				anInt1186 += (24 - anInt1186) / 2;
			else
				anInt1186 /= 2;
			if (super.keyArray[3] == 1)
				anInt1187 += (12 - anInt1187) / 2;
			else if (super.keyArray[4] == 1)
				anInt1187 += (-12 - anInt1187) / 2;
			else
				anInt1187 /= 2;
			cameraHorizontal = cameraHorizontal + anInt1186 / 2 & 0x7ff;
			anInt1184 += anInt1187 / 2;
			if (anInt1184 < 128)
				anInt1184 = 128;
			if (anInt1184 > 383)
				anInt1184 = 383;
			int l = anInt1014 >> 7;
			int i1 = anInt1015 >> 7;
			int j1 = method42(plane, anInt1015, anInt1014);
			int k1 = 0;
			if (l > 3 && i1 > 3 && l < 100 && i1 < 100) {
				for (int l1 = l - 4; l1 <= l + 4; l1++) {
					for (int k2 = i1 - 4; k2 <= i1 + 4; k2++) {
						int l2 = plane;
						if (l2 < 3 && (tileFlags[1][l1][k2] & 2) == 2)
							l2++;
						int i3 = j1 - tileHeights[l2][l1][k2];
						if (i3 > k1)
							k1 = i3;
					}

				}

			}
			anInt1005++;
			if (anInt1005 > 1512) {
				anInt1005 = 0;
				stream.createFrame(77);
				stream.writeWordBigEndian(0);
				int i2 = stream.currentOffset;
				stream.writeWordBigEndian((int) (Math.random() * 256D));
				stream.writeWordBigEndian(101);
				stream.writeWordBigEndian(233);
				stream.writeShort(45092);
				if ((int) (Math.random() * 2D) == 0)
					stream.writeShort(35784);
				stream.writeWordBigEndian((int) (Math.random() * 256D));
				stream.writeWordBigEndian(64);
				stream.writeWordBigEndian(38);
				stream.writeShort((int) (Math.random() * 65536D));
				stream.writeShort((int) (Math.random() * 65536D));
				stream.writeBytes(stream.currentOffset - i2);
			}
			int j2 = k1 * 192;
			if (j2 > 0x17f00)
				j2 = 0x17f00;
			if (j2 < 32768)
				j2 = 32768;
			if (j2 > anInt984) {
				anInt984 += (j2 - anInt984) / 24;
				return;
			}
			if (j2 < anInt984) {
				anInt984 += (j2 - anInt984) / 80;
			}
		} catch (Exception _ex) {
			Signlink.reportError("glfc_ex " + myPlayer.x + "," + myPlayer.y + "," + anInt1014 + "," + anInt1015 + ","
					+ anInt1069 + "," + anInt1070 + "," + baseX + "," + baseY);
			throw new RuntimeException("eek");
		}
	}

	@Override
	public void repaintGame() {
		if (rsAlreadyLoaded || loadingError || genericLoadingError) {
			showErrorScreen();
			return;
		}
		if (!loggedIn)
			drawLoginScreen(false);
		else
			drawGameScreen();

		anInt1213 = 0;
	}

	private boolean isFriendOrSelf(String s) {
		if (s == null)
			return false;
		for (int i = 0; i < friendsCount; i++)
			if (s.equalsIgnoreCase(friendsList[i]))
				return true;
		return s.equalsIgnoreCase(myPlayer.name);
	}

	private static String combatDiffColor(int i, int j) {
		int k = i - j;
		if (k < -9)
			return "@red@";
		if (k < -6)
			return "@or3@";
		if (k < -3)
			return "@or2@";
		if (k < 0)
			return "@or1@";
		if (k > 9)
			return "@gre@";
		if (k > 6)
			return "@gr3@";
		if (k > 3)
			return "@gr2@";
		if (k > 0)
			return "@gr1@";
		else
			return "@yel@";
	}

	private void setWaveVolume(int i) {
		Signlink.wavevol = i;
	}

	private boolean fullyFaded = false;
	private boolean doFade = false;
	private int curFadeAlpha = 0;
	private int fadeColor = 0;
	private boolean fadingIn = false;

	private void handleFading() {
		if (!doFade)
			return;
		if (curFadeAlpha == -10)
			return;
		if (!fullyFaded) {
			if (fadingIn) {
				curFadeAlpha += 5;
				if (curFadeAlpha >= 255) {
					curFadeAlpha = 255;
					fullyFaded = true;
				}
			} else {
				curFadeAlpha -= 5;
				if (curFadeAlpha <= 0) {
					curFadeAlpha = 0;
					fullyFaded = true;
				}
			}
		}
		if (curFadeAlpha <= 0) {
			return;
		}
		DrawingArea.method335(fadeColor, 0, frameWidth, frameHeight, curFadeAlpha, 0);
	}

	private void setFade(boolean fadeIn, int color) {
		if (!fullyFaded) {
			if (curFadeAlpha == -10) {
				fadingIn = fadeIn;
				fadeColor = color;
				doFade = true;
				if (fadingIn) {
					curFadeAlpha = 0;
				} else {
					curFadeAlpha = 255;
				}
			}
		}
	}

	private void resetFade() {
		doFade = false;
		fullyFaded = false;
		fadingIn = false;
		curFadeAlpha = -10;
	}

	private int targetTint = 0;
	private int tintColor = 0;

	private void handleTinting() {
		if (targetTint > 0 && frameMode != ScreenMode.FIXED) {
			DrawingArea.method335(tintColor, 0, frameWidth, frameHeight, targetTint, 0);
		}
	}

	private void setTint(int tint, int color) {
		targetTint = tint;
		tintColor = color;
	}

	private void draw3dScreen() {
		handleTinting();
		handleFading();
		XPDrop.draw();
		if (showChatComponents) {
			drawSplitPrivateChat();
		}
		if (crossType == 1) {
			int offSet = frameMode == ScreenMode.FIXED ? 4 : 0;
			crosses[crossIndex / 100].drawSprite(crossX - 8 - offSet, crossY - 8 - offSet);
		}
		if (crossType == 2) {
			int offSet = frameMode == ScreenMode.FIXED ? 4 : 0;
			crosses[4 + crossIndex / 100].drawSprite(crossX - 8 - offSet, crossY - 8 - offSet);
		}
		if (anInt1055 == 1) {
			int skullOffset = (walkableInterfaceID == 4 ? frameWidth - 90 : frameWidth - 120);
			multiOverlay.drawSprite(frameMode == ScreenMode.FIXED ? 472 : skullOffset,
					frameMode == ScreenMode.FIXED ? 296 : 174);
		}
		if (walkableInterfaceID != -1) {
			animateInterface(anInt945, walkableInterfaceID);
			int width = 512;// 765;
			int height = 334;// 503;
			if (walkableInterfaceID == 4 && frameMode != ScreenMode.FIXED) {
				drawInterface(frameWidth - width - 80, -80, width, height,
						RSInterface.interfaceCache[walkableInterfaceID], -1, 0);
			} else if (walkableInterfaceID == 5 && frameMode != ScreenMode.FIXED) {
				drawInterface(frameWidth - width - 80, -120, width, height,
						RSInterface.interfaceCache[walkableInterfaceID], -1, 0);
			} else if ((walkableInterfaceID == 293 || walkableInterfaceID == 484 || walkableInterfaceID == 485)
					&& frameMode != ScreenMode.FIXED) {
				drawInterface(0, 0, width, height, RSInterface.interfaceCache[walkableInterfaceID], -1, 0);
			} else if ((walkableInterfaceID == 119 || walkableInterfaceID == 300) && frameMode != ScreenMode.FIXED) {
				drawInterface(((frameWidth / 2) - (width / 2)) - 10, 150, width, height,
						RSInterface.interfaceCache[walkableInterfaceID], -1, 0);
			} else if (walkableInterfaceID == 156 && frameMode != ScreenMode.FIXED) {
				drawInterface(((frameWidth / 2) - (width / 2)) - 40, ((frameHeight / 2) - (height / 2)) - 50, width,
						height, RSInterface.interfaceCache[walkableInterfaceID], -1, 0);
			} else if ((walkableInterfaceID == 423 || walkableInterfaceID == 424 || walkableInterfaceID == 425
					|| walkableInterfaceID == 427) && frameMode != ScreenMode.FIXED) {
				drawInterface((walkableInterfaceID == 423 ? -width + 200 : -width + 170), 25, width, height,
						RSInterface.interfaceCache[walkableInterfaceID], -1, 0);
			} else if (walkableInterfaceID == 260 && frameMode != ScreenMode.FIXED) {
				drawInterface(0, (frameHeight - height) - 165, width, height,
						RSInterface.interfaceCache[walkableInterfaceID], -1, 0);
			} else if (walkableInterfaceID == 259) {
				setFade(true, 0);
			} else if (walkableInterfaceID == 421) {
				setFade(false, 0);
			} else if (walkableInterfaceID == 464) {
				setFade(true, 8881798);
			} else if (walkableInterfaceID == 468) {
				setFade(false, 8881798);
			} else if ((walkableInterfaceID == 328 || walkableInterfaceID == 329 || walkableInterfaceID == 330)
					&& frameMode != ScreenMode.FIXED) {
				if (walkableInterfaceID == 329)
					setTint(50, 0);
				if (walkableInterfaceID == 330)
					setTint(100, 0);
				if (walkableInterfaceID == 328)
					setTint(200, 0);
			} else if (walkableInterfaceID == 467 && frameMode != ScreenMode.FIXED) {
				setTint(75, 5082768);
			} else if (walkableInterfaceID == 495 && frameMode != ScreenMode.FIXED) {
				setTint(255, 0);
				int drawX = ((frameWidth / 2) - (width / 2));
				int drawY = ((frameHeight / 2) - (height / 2)) - 40;
				drawInterface(drawX, drawY, width, height, RSInterface.interfaceCache[walkableInterfaceID], -1, 0);
			} else {
				int drawX = frameMode == ScreenMode.FIXED ? 0 : (frameWidth / 2) + 80;
				int drawY = frameMode == ScreenMode.FIXED ? 0 : (frameHeight / 2) - 550;
				drawInterface(drawX, drawY, width, height, RSInterface.interfaceCache[walkableInterfaceID], -1, 0);
			}
		}
		if (openInterfaceID != -1) {
			animateInterface(anInt945, openInterfaceID);
			int width = 512;// 765;
			int height = 334;// 503;
			if (openInterfaceID == 259) {
				setFade(true, 0);
			} else if (openInterfaceID == 421) {
				setFade(false, 0);
			} else if (openInterfaceID == 464) {
				setFade(true, 8881798);
			} else if (openInterfaceID == 468) {
				setFade(false, 8881798);
			} else if ((openInterfaceID == 328 || openInterfaceID == 329 || openInterfaceID == 330)
					&& frameMode != ScreenMode.FIXED) {
				if (openInterfaceID == 329)
					setTint(50, 0);
				if (openInterfaceID == 330)
					setTint(100, 0);
				if (openInterfaceID == 328)
					setTint(200, 0);
			} else if (openInterfaceID == 467 && frameMode != ScreenMode.FIXED) {
				setTint(75, 5082768);
			} else {
				int drawX = frameMode == ScreenMode.FIXED ? 0 : (frameWidth / 2) - 356;
				int drawY = frameMode == ScreenMode.FIXED ? 0 : (frameHeight / 2) - 230;
				drawInterface(drawX, drawY, width, height, RSInterface.interfaceCache[openInterfaceID], -1, 0);
			}
		}
		checkTutorialIsland();
		if (!serverMessage.isEmpty()) {
			int drawmsgY = frameMode == ScreenMode.FIXED ? 329 : frameHeight - 171;

			if (showChatComponents) {
				regularFont.method389(true, 4, 0xffff00, serverMessage, drawmsgY);
			} else {
				regularFont.method389(true, 4, 0xffff00, serverMessage, frameHeight - 30);
			}
		}

		if (!menuOpen) {
			processRightClick();
			drawTooltip();
		} else if (menuScreenArea == 0) {
			drawMenu((frameMode == ScreenMode.FIXED ? 4 : 0), (frameMode == ScreenMode.FIXED ? 4 : 0));
		}
		if (fpsOn) {
			char c = '\u01FB';
			int k = 20;
			int i1 = 0xffff00;
			if (super.fps < 15)
				i1 = 0xff0000;
			regularFont.method380("Fps:" + super.fps, c, i1, k);
			k += 15;
			Runtime runtime = Runtime.getRuntime();
			int j1 = (int) ((runtime.totalMemory() - runtime.freeMemory()) / 1024L);
			i1 = 0xffff00;
			if (j1 > 0x2000000 && lowMem)
				i1 = 0xff0000;
			regularFont.method380("Mem:" + j1 + "k", c, 0xffff00, k);
			k += 15;
		}
		int x = baseX + (myPlayer.x - 6 >> 7);
		int y = baseY + (myPlayer.y - 6 >> 7);
		if (clientData) {
			int debugYOffset = showChatComponents ? frameHeight - 173 : frameHeight - 34;
			final RSFont textFont = newBoldFont;
			final int lineSpacing = textFont.baseCharacterHeight + 3;
			final int textColour = 0xffff00;
			int fpsColour = 0xffff00;
			if (super.fps < 15) {
				fpsColour = 0xff0000;
			}
			Runtime runtime = Runtime.getRuntime();
			int clientMemory = (int) ((runtime.totalMemory() - runtime.freeMemory()) / 1024L);
			textFont.drawBasicString("Fps: " + super.fps, 5, debugYOffset, fpsColour, 0);
			debugYOffset -= lineSpacing;
			textFont.drawBasicString("Memory Usage: " + clientMemory + "k", 5, debugYOffset, textColour, 0);
			debugYOffset -= lineSpacing;
			textFont.drawBasicString("Screen Mode: " + frameMode, 5, debugYOffset, textColour, 0);
			debugYOffset -= lineSpacing;
			textFont.drawBasicString("Client Resolution: " + frameWidth + "x" + frameHeight, 5, debugYOffset,
					textColour, 0);
			debugYOffset -= lineSpacing;
			textFont.drawBasicString("Client Zoom: " + cameraZoom, 5, debugYOffset, textColour, 0);
			debugYOffset -= lineSpacing;
			textFont.drawBasicString(
					"frameWidth: " + (mouseX - frameWidth) + ", frameHeight: " + (mouseY - frameHeight), 5,
					debugYOffset, textColour, 0);
			debugYOffset -= lineSpacing;
			textFont.drawBasicString("Mouse X: " + mouseX + ", Mouse Y: " + mouseY, 5, debugYOffset, textColour, 0);
			debugYOffset -= lineSpacing;
			textFont.drawBasicString("Coords: " + x + ", " + y, 5, debugYOffset, textColour, 0);
			debugYOffset -= lineSpacing;
			textFont.drawBasicString("Floor Map Array: " + Arrays.toString(floorMapArray), 5, debugYOffset, textColour,
					0);
			debugYOffset -= lineSpacing;
			textFont.drawBasicString("Object Map Array: " + Arrays.toString(objectMapArray), 5, debugYOffset,
					textColour, 0);
		}
		if (systemUpdateTime != 0) {
			int yPos = 0;

			if (frameMode == ScreenMode.FIXED) {
				yPos = 329;
			} else {
				yPos = frameHeight - 170;
			}

			if (!serverMessage.isEmpty()) {
				yPos -= 14;
			}

			int j = systemUpdateTime / 50;
			int l = j / 60;
			j %= 60;
			if (j < 10)
				regularFont.method385(0xffff00, "System update in: " + l + ":0" + j, yPos, 4);
			else
				regularFont.method385(0xffff00, "System update in: " + l + ":" + j, yPos, 4);
			anInt849++;
			if (anInt849 > 75) {
				anInt849 = 0;
				stream.createFrame(148);
			}
		}
	}

	public long getOverallExp() {
		long overall = 0;
		for (int i = 0; i < currentExp.length; i++) {
			overall += currentExp[i];
		}
		return overall;
	}

	private void addIgnore(long l) {
		try {
			if (l == 0L)
				return;
			if (ignoreCount >= 100) {
				pushMessage("Your ignore list is full. Max of 100 hit", 0, "", true);
				return;
			}
			String s = TextUtil.fixName(TextUtil.nameForLong(l));
			for (int j = 0; j < ignoreCount; j++)
				if (ignoreListAsLongs[j] == l) {
					pushMessage(s + " is already on your ignore list", 0, "", true);
					return;
				}
			for (int k = 0; k < friendsCount; k++)
				if (friendsListAsLongs[k] == l) {
					pushMessage("Please remove " + s + " from your friend list first", 0, "", true);
					return;
				}
			if (s.equals(myPlayer.name)) {
				return;
			}
			ignoreListAsLongs[ignoreCount++] = l;
			redrawTab = true;
			stream.createFrame(133);
			stream.writeQWord(l);
			return;
		} catch (RuntimeException runtimeexception) {
			Signlink.reportError("45688, " + l + ", " + 4 + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	private void method114() {
		for (int i = -1; i < playerCount; i++) {
			int j;
			if (i == -1)
				j = myPlayerIndex;
			else
				j = playerIndices[i];
			Player player = playerArray[j];
			if (player != null)
				entityUpdateBlock(player);
		}

	}

	private void method115() {
		if (loadingStage == 2) {
			for (SpawnedObject class30_sub1 = (SpawnedObject) aClass19_1179
					.reverseGetFirst(); class30_sub1 != null; class30_sub1 = (SpawnedObject) aClass19_1179
							.reverseGetNext()) {
				if (class30_sub1.anInt1294 > 0)
					class30_sub1.anInt1294--;
				if (class30_sub1.anInt1294 == 0) {
					if (class30_sub1.anInt1299 < 0
							|| MapRegion.method178(class30_sub1.anInt1299, class30_sub1.anInt1301)) {
						method142(class30_sub1.anInt1298, class30_sub1.anInt1295, class30_sub1.anInt1300,
								class30_sub1.anInt1301, class30_sub1.anInt1297, class30_sub1.anInt1296,
								class30_sub1.anInt1299);
						class30_sub1.unlink();
					}
				} else {
					if (class30_sub1.anInt1302 > 0)
						class30_sub1.anInt1302--;
					if (class30_sub1.anInt1302 == 0 && class30_sub1.anInt1297 >= 1 && class30_sub1.anInt1298 >= 1
							&& class30_sub1.anInt1297 <= 102 && class30_sub1.anInt1298 <= 102
							&& (class30_sub1.anInt1291 < 0
									|| MapRegion.method178(class30_sub1.anInt1291, class30_sub1.anInt1293))) {
						method142(class30_sub1.anInt1298, class30_sub1.anInt1295, class30_sub1.anInt1292,
								class30_sub1.anInt1293, class30_sub1.anInt1297, class30_sub1.anInt1296,
								class30_sub1.anInt1291);
						class30_sub1.anInt1302 = -1;
						if (class30_sub1.anInt1291 == class30_sub1.anInt1299 && class30_sub1.anInt1299 == -1)
							class30_sub1.unlink();
						else if (class30_sub1.anInt1291 == class30_sub1.anInt1299
								&& class30_sub1.anInt1292 == class30_sub1.anInt1300
								&& class30_sub1.anInt1293 == class30_sub1.anInt1301)
							class30_sub1.unlink();
					}
				}
			}

		}
	}

	private void determineMenuSize() {
		int boxLength = boldFontS.getTextWidth("Choose option");
		for (int row = 0; row < menuActionRow; row++) {
			int actionLength = newBoldFont.getTextWidth(legacyColorConvert(menuActionName[row]));
			if (actionLength > boxLength)
				boxLength = actionLength;
		}
		boxLength += 8;
		int offset = 15 * menuActionRow + 21;
		if (super.saveClickX > 0 && super.saveClickY > 0 && super.saveClickX < frameWidth
				&& super.saveClickY < frameHeight) {
			int xClick = super.saveClickX - boxLength / 2;
			if (xClick + boxLength > frameWidth - 4) {
				xClick = frameWidth - 4 - boxLength;
			}
			if (xClick < 0) {
				xClick = 0;
			}
			int yClick = super.saveClickY - 0;
			if (yClick + offset > frameHeight - 6) {
				yClick = frameHeight - 6 - offset;
			}
			if (yClick < 0) {
				yClick = 0;
			}
			/*
			 * if(isTabArea()) { menuScreenArea = 1; } else if(isChatArea()) {
			 * menuScreenArea = 2; } else { menuScreenArea = 0; }
			 */
			menuOpen = true;
			menuOffsetX = xClick;
			menuOffsetY = yClick;
			menuWidth = boxLength;
			menuHeight = 15 * menuActionRow + 22;
		}
	}

	public int centerX = frameWidth / 2;
	public int centerY = frameHeight / 2;

	private void method117(Stream stream) {
		stream.initBitAccess();
		int j = stream.readBits(1);
		if (j == 0)
			return;
		int k = stream.readBits(2);
		if (k == 0) {
			npcsAwaitingUpdate[npcsAwaitingUpdateCount++] = myPlayerIndex;
			return;
		}
		if (k == 1) {
			int l = stream.readBits(3);
			myPlayer.moveInDir(false, l);
			int k1 = stream.readBits(1);
			if (k1 == 1)
				npcsAwaitingUpdate[npcsAwaitingUpdateCount++] = myPlayerIndex;
			return;
		}
		if (k == 2) {
			int i1 = stream.readBits(3);
			myPlayer.moveInDir(true, i1);
			int l1 = stream.readBits(3);
			myPlayer.moveInDir(true, l1);
			int j2 = stream.readBits(1);
			if (j2 == 1)
				npcsAwaitingUpdate[npcsAwaitingUpdateCount++] = myPlayerIndex;
			return;
		}
		if (k == 3) {
			plane = stream.readBits(2);
			int j1 = stream.readBits(1);
			int i2 = stream.readBits(1);
			if (i2 == 1)
				npcsAwaitingUpdate[npcsAwaitingUpdateCount++] = myPlayerIndex;
			int k2 = stream.readBits(7);
			int l2 = stream.readBits(7);
			myPlayer.setPos(l2, k2, j1 == 1);
		}
	}

	private void nullLoader() {
		aBoolean831 = false;
	}

	private boolean animateInterface(int i, int j) {
		boolean flag1 = false;
		RSInterface[] components = RSInterface.interfaceCache[j];
		for (int childID = 0; childID < components.length; childID++) {
			RSInterface child = components[childID];
			if (child == null)
				continue;
			if (child.type == 6 && (child.defaultAnimationId != -1 || child.secondaryAnimationId != -1)) {
				boolean flag2 = interfaceIsSelected(child);
				int l;
				if (flag2)
					l = child.secondaryAnimationId;
				else
					l = child.defaultAnimationId;
				if (l != -1) {
					Animation animation = Animation.anims[l];
					for (child.anInt208 += i; child.anInt208 > animation.method258(child.anInt246);) {
						child.anInt208 -= animation.method258(child.anInt246) + 1;
						child.anInt246++;
						if (child.anInt246 >= animation.anInt352) {
							child.anInt246 -= animation.anInt356;
							if (child.anInt246 < 0 || child.anInt246 >= animation.anInt352)
								child.anInt246 = 0;
						}
						flag1 = true;
					}
				}
			}
		}
		return flag1;
	}

	private int setCameraLocation() { // method120
		if (!SettingsManager.showRoofs) {
			return plane;
		}
		int j = 3;
		if (yCameraCurve < 310) {
			int k = xCameraPos >> 7;
			int l = yCameraPos >> 7;
			int i1 = myPlayer.x >> 7;
			int j1 = myPlayer.y >> 7;
			if ((tileFlags[plane][k][l] & 4) != 0)
				j = plane;
			int k1;
			if (i1 > k)
				k1 = i1 - k;
			else
				k1 = k - i1;
			int l1;
			if (j1 > l)
				l1 = j1 - l;
			else
				l1 = l - j1;
			if (k1 > l1) {
				int i2 = (l1 * 0x10000) / k1;
				int k2 = 32768;
				while (k != i1) {
					if (k < i1)
						k++;
					else if (k > i1)
						k--;
					if ((tileFlags[plane][k][l] & 4) != 0)
						j = plane;
					k2 += i2;
					if (k2 >= 0x10000) {
						k2 -= 0x10000;
						if (l < j1)
							l++;
						else if (l > j1)
							l--;
						if ((tileFlags[plane][k][l] & 4) != 0)
							j = plane;
					}
				}
			} else {
				int j2 = (k1 * 0x10000) / l1;
				int l2 = 32768;
				while (l != j1) {
					if (l < j1)
						l++;
					else if (l > j1)
						l--;
					if ((tileFlags[plane][k][l] & 4) != 0)
						j = plane;
					l2 += j2;
					if (l2 >= 0x10000) {
						l2 -= 0x10000;
						if (k < i1)
							k++;
						else if (k > i1)
							k--;
						if ((tileFlags[plane][k][l] & 4) != 0)
							j = plane;
					}
				}
			}
		}
		if ((tileFlags[plane][myPlayer.x >> 7][myPlayer.y >> 7] & 4) != 0)
			j = plane;
		return j;
	}

	private int resetCameraHeight() { // method121
		if (cutSceneHideRoofs) {
			return plane;
		}
		int j = method42(plane, yCameraPos, xCameraPos);
		if (j - zCameraPos < 800 && (tileFlags[plane][xCameraPos >> 7][yCameraPos >> 7] & 4) != 0)
			return plane;
		else
			return 3;
	}

	private void delIgnore(long l) {
		try {
			if (l == 0L)
				return;
			for (int j = 0; j < ignoreCount; j++)
				if (ignoreListAsLongs[j] == l) {
					ignoreCount--;
					redrawTab = true;
					System.arraycopy(ignoreListAsLongs, j + 1, ignoreListAsLongs, j, ignoreCount - j);

					stream.createFrame(74);
					stream.writeQWord(l);
					return;
				}
			return;
		} catch (RuntimeException runtimeexception) {
			Signlink.reportError("47229, " + 3 + ", " + l + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	private void clanChatJoin(long l) {
		try {
			if (l == 0L)
				return;
			stream.createFrame(61);
			stream.writeQWord(l);
			return;
		} catch (RuntimeException runtimeexception) {
			Signlink.reportError("47229, " + 3 + ", " + l + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	private void clanChatUserAction(long l, int action) {
		try {
			if (l == 0L)
				return;
			if (action < 0)
				return;
			stream.createFrame(62);
			stream.writeQWord(l);
			stream.writeShort(action);
			return;
		} catch (RuntimeException runtimeexception) {
			Signlink.reportError("47229, " + 3 + ", " + l + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	public String getParameter(String s) {
		if (Signlink.mainapp != null)
			return Signlink.mainapp.getParameter(s);
		else
			return super.getParameter(s);
	}

	private int extractInterfaceValues(RSInterface class9, int j) {
		if (class9.scripts == null || j >= class9.scripts.length)
			return -2;
		try {
			int ai[] = class9.scripts[j];
			int k = 0;
			int l = 0;
			int i1 = 0;
			do {
				int j1 = ai[l++];
				int k1 = 0;
				byte byte0 = 0;
				if (j1 == 0)
					return k;
				if (j1 == 1)
					k1 = currentStats[ai[l++]];
				if (j1 == 2)
					k1 = maxStats[ai[l++]];
				if (j1 == 3)
					k1 = currentExp[ai[l++]];
				if (j1 == 4) {
					int interfaceHash = ai[l++];
					int k2 = ai[l++];
					RSInterface rsInterface = RSInterface.getInterface(interfaceHash);
					if (k2 >= 0 && k2 < ItemDef.totalItems && (!ItemDef.forID(k2).membersObject || isMembers)) {
						for (int j3 = 0; j3 < rsInterface.inventoryItemIds.length; j3++)
							if (rsInterface.inventoryItemIds[j3] == k2 + 1)
								k1 += rsInterface.inventoryStackSizes[j3];
					}
				}
				if (j1 == 5)
					k1 = variousSettings[ai[l++]];
				if (j1 == 6)
					k1 = anIntArray1019[maxStats[ai[l++]] - 1];
				if (j1 == 7)
					k1 = (variousSettings[ai[l++]] * 100) / 46875;
				if (j1 == 8)
					k1 = myPlayer.combatLevel;
				if (j1 == 9) {
					for (int l1 = 0; l1 < Skills.skillsCount; l1++)
						if (Skills.skillEnabled[l1])
							k1 += maxStats[l1];

				}
				if (j1 == 10) {
					int interfaceHash = ai[l++];
					RSInterface rsInterface = RSInterface.getInterface(interfaceHash);
					int l2 = ai[l++] + 1;
					if (l2 >= 0 && l2 < ItemDef.totalItems && isMembers) {
						for (int k3 = 0; k3 < rsInterface.inventoryItemIds.length; k3++) {
							if (rsInterface.inventoryItemIds[k3] != l2)
								continue;
							k1 = 0x3b9ac9ff;
							break;
						}
					}
				}
				if (j1 == 11)
					k1 = energy;
				if (j1 == 12)
					k1 = weight;
				if (j1 == 13) {
					int i2 = variousSettings[ai[l++]];
					int i3 = ai[l++];
					k1 = (i2 & 1 << i3) == 0 ? 0 : 1;
				}
				if (j1 == 14) {
					int index = ai[l++];
					VarBit varBit = VarBit.cache[index];
					int setting = varBit.configId;
					int low = varBit.leastSignificantBit;
					int high = varBit.mostSignificantBit;
					int mask = BIT_MASKS[high - low];
					k1 = variousSettings[setting] >> low & mask;
				}
				if (j1 == 15)
					byte0 = 1;
				if (j1 == 16)
					byte0 = 2;
				if (j1 == 17)
					byte0 = 3;
				if (j1 == 18)
					k1 = (myPlayer.x >> 7) + baseX;
				if (j1 == 19)
					k1 = (myPlayer.y >> 7) + baseY;
				if (j1 == 20)
					k1 = ai[l++];
				if (byte0 == 0) {
					if (i1 == 0)
						k += k1;
					if (i1 == 1)
						k -= k1;
					if (i1 == 2 && k1 != 0)
						k /= k1;
					if (i1 == 3)
						k *= k1;
					i1 = 0;
				} else {
					i1 = byte0;
				}
			} while (true);
		} catch (Exception _ex) {
			return -1;
		}
	}

	private void drawTooltip() {
		if (menuActionRow < 2 && itemSelected == 0 && spellSelected == 0)
			return;
		String s;
		if (itemSelected == 1 && menuActionRow < 2)
			s = "Use " + selectedItemName + " with...";
		else if (spellSelected == 1 && menuActionRow < 2)
			s = spellTooltip + "...";
		else
			s = menuActionName[menuActionRow - 1];
		if (menuActionRow > 2)
			s = s + "@whi@ / " + (menuActionRow - 2) + " more options";
		newBoldFont.drawBasicString(legacyColorConvert(s), 4, 15, 0xffffff, 0);
	}

	private void method81(Sprite sprite, int y, int x) {
		int l = x * x + y * y;
		if (l > 4225 && l < 0x15f90) {
			int i1 = cameraHorizontal + minimapRotation & 0x7ff;
			int sine = Model.modelIntArray1[i1];
			int cosine = Model.modelIntArray2[i1];
			sine = (sine * 256) / (minimapZoom + 256);
			cosine = (cosine * 256) / (minimapZoom + 256);
			int l1 = y * sine + x * cosine >> 16;
			int i2 = y * cosine - x * sine >> 16;
			double d = Math.atan2(l1, i2);
			int j2 = (int) (Math.sin(d) * 58D);
			int k2 = (int) (Math.cos(d) * 52D);
			final Point mapBase = gameFrame.getMapOffset(false);
			final int xOffset = mapBase.x + gameFrame.getMapImageOffset().x + 68;
			final int yOffset = mapBase.y + gameFrame.getMapImageOffset().y + 83;
			mapEdge.method353(yOffset - k2 - 20, d, (xOffset + j2 + 4) - 10);
		} else {
			markMinimap(sprite, x, y);
		}
	}

	private void markMinimap(Sprite sprite, int x, int y) {
		if (sprite == null) {
			return;
		}
		int k = cameraHorizontal + minimapRotation & 0x7ff;
		int l = sprite.myWidth + sprite.myHeight + x * x + y * y;
		if (l > 6400) {
			return;
		}
		int sine = Model.modelIntArray1[k];
		int cosine = Model.modelIntArray2[k];
		sine = (sine * 256) / (minimapZoom + 256);
		cosine = (cosine * 256) / (minimapZoom + 256);
		int k1 = y * sine + x * cosine >> 16;
		int l1 = y * cosine - x * sine >> 16;
		final Point mapBase = gameFrame.getMapOffset(false);
		final int xOffset = mapBase.x + gameFrame.getMapImageOffset().x + 69;
		final int yOffset = mapBase.y + gameFrame.getMapImageOffset().y + 78;
		sprite.drawSprite(((xOffset + k1) - sprite.maxWidth / 2) + 4, yOffset - l1 - sprite.maxHeight / 2 - 4);
	}

	private void drawMinimapToScreen() {
		if (loadingStage != 2) {
			return;
		}
		if (frameMode == ScreenMode.FIXED) {
			aRSImageProducer_1164.initDrawingArea();
		}
		if (anInt1021 == 2) {
			gameFrame.drawMapArea(this, true);
			if (menuOpen) {
				drawMenu(frameMode == ScreenMode.FIXED ? 516 : 0, 0);
			}
			if (frameMode == ScreenMode.FIXED) {
				aRSImageProducer_1164.initDrawingArea();
			}
		} else {
			final Point mapBase = gameFrame.getMapOffset(false);
			final int xOffset = mapBase.x + gameFrame.getMapImageOffset().x;
			final int yOffset = mapBase.y + gameFrame.getMapImageOffset().y;
			int mapRotation = cameraHorizontal + minimapRotation & 0x7ff;
			int mapX = 48 + myPlayer.x / 32;
			int mapY = 464 - myPlayer.y / 32;
			minimapImage.shapeImageToPixels(151, mapRotation, anIntArray1229, 256 + minimapZoom, anIntArray1052, mapY,
					yOffset, xOffset, 146, mapX);

			for (int j5 = 0; j5 < anInt1071; j5++) {
				int k = (anIntArray1072[j5] * 4 + 2) - myPlayer.x / 32;
				int i3 = (anIntArray1073[j5] * 4 + 2) - myPlayer.y / 32;
				markMinimap(aClass30_Sub2_Sub1_Sub1Array1140[j5], k, i3);
			}
			for (int k5 = 0; k5 < 104; k5++) {
				for (int l5 = 0; l5 < 104; l5++) {
					NodeList class19 = groundArray[plane][k5][l5];
					if (class19 != null) {
						int l = (k5 * 4 + 2) - myPlayer.x / 32;
						int j3 = (l5 * 4 + 2) - myPlayer.y / 32;
						markMinimap(mapDotItem, l, j3);
					}
				}
			}
			for (int i6 = 0; i6 < npcCount; i6++) {
				NPC npc = npcArray[npcIndices[i6]];
				if (npc != null && npc.isVisible()) {
					EntityDef entityDef = npc.desc;
					if (entityDef.childrenIDs != null)
						entityDef = entityDef.method161();
					if (entityDef != null && entityDef.drawMapDot && entityDef.hasActions) {
						int i1 = npc.x / 32 - myPlayer.x / 32;
						int k3 = npc.y / 32 - myPlayer.y / 32;
						markMinimap(mapDotNPC, i1, k3);
					}
				}
			}
			for (int j6 = 0; j6 < playerCount; j6++) {
				Player player = playerArray[playerIndices[j6]];
				if (player != null && player.isVisible()) {
					int j1 = player.x / 32 - myPlayer.x / 32;
					int l3 = player.y / 32 - myPlayer.y / 32;
					boolean flag1 = false;
					boolean flag3 = false;
					for (int j3 = 0; j3 < clanCount; j3++) {
						if (clanListAsLongs[j3] <= 0)
							continue;
						String chatMember = TextUtil.nameForLong(clanListAsLongs[j3]);
						if (!chatMember.equalsIgnoreCase(player.name))
							continue;
						flag3 = true;
						break;
					}
					long l6 = TextUtil.longForName(player.name);
					for (int k6 = 0; k6 < friendsCount; k6++) {
						if (l6 != friendsListAsLongs[k6] || friendsNodeIDs[k6] == 0)
							continue;
						flag1 = true;
						break;
					}
					boolean flag2 = false;
					if (myPlayer.team != 0 && player.team != 0 && myPlayer.team == player.team)
						flag2 = true;
					if (flag1)
						markMinimap(mapDotFriend, j1, l3);
					else if (flag3)
						markMinimap(mapDotClan, j1, l3);
					else if (flag2)
						markMinimap(mapDotTeam, j1, l3);
					else
						markMinimap(mapDotPlayer, j1, l3);
				}
			}
			if (anInt855 != 0 && loopCycle % 20 < 10) {
				if (anInt855 == 1 && anInt1222 >= 0 && anInt1222 < npcArray.length) {
					NPC class30_sub2_sub4_sub1_sub1_1 = npcArray[anInt1222];
					if (class30_sub2_sub4_sub1_sub1_1 != null) {
						int k1 = class30_sub2_sub4_sub1_sub1_1.x / 32 - myPlayer.x / 32;
						int i4 = class30_sub2_sub4_sub1_sub1_1.y / 32 - myPlayer.y / 32;
						method81(mapMarker, i4, k1);
					}
				}
				if (anInt855 == 2) {
					int l1 = ((anInt934 - baseX) * 4 + 2) - myPlayer.x / 32;
					int j4 = ((anInt935 - baseY) * 4 + 2) - myPlayer.y / 32;
					method81(mapMarker, j4, l1);
				}
				if (anInt855 == 10 && anInt933 >= 0 && anInt933 < playerArray.length) {
					Player class30_sub2_sub4_sub1_sub2_1 = playerArray[anInt933];
					if (class30_sub2_sub4_sub1_sub2_1 != null) {
						int i2 = class30_sub2_sub4_sub1_sub2_1.x / 32 - myPlayer.x / 32;
						int k4 = class30_sub2_sub4_sub1_sub2_1.y / 32 - myPlayer.y / 32;
						method81(mapMarker, k4, i2);
					}
				}
			}
			if (destX != 0) {
				int j2 = (destX * 4 + 2) - myPlayer.x / 32;
				int l4 = (destY * 4 + 2) - myPlayer.y / 32;
				markMinimap(mapFlag, j2, l4);
			}
			gameFrame.drawMapArea(this, false);
		}
		drawOrbs();
		if (menuOpen) {
			drawMenu(frameMode == ScreenMode.FIXED ? 516 : 0, 0);
		}
		if (frameMode == ScreenMode.FIXED) {
			gameScreenImageProducer.initDrawingArea();
			aRSImageProducer_1164.drawGraphics(0, super.graphics, 516);
		}
	}

	private void npcScreenPos(Entity entity, int i) {
		calcEntityScreenPos(entity.x, i, entity.y);
	}

	private void renderGroundItemNames() {
		for (int x = 0; x < 104; x++) {
			for (int y = 0; y < 104; y++) {
				NodeList node = groundArray[plane][x][y];
				int offset = 12;
				if (node != null) {
					for (Item item = (Item) node.getFirst(); item != null; item = (Item) node.getNext()) {
						ItemDef itemDef = ItemDef.forID(item.ID);
						calcEntityScreenPos((x << 7) + 64, 64, (y << 7) + 64);
						newSmallFont.drawCenteredString(itemDef.name + (item.itemCount > 1 ? " (" + item.itemCount + ")" : ""), spriteDrawX, spriteDrawY - offset, 0xffffff, 1);
						offset += 12;
					}
				}
			}
		}
	}

	private void calcEntityScreenPos(int i, int j, int l) {
		if (i < 128 || l < 128 || i > 13056 || l > 13056) {
			spriteDrawX = -1;
			spriteDrawY = -1;
			return;
		}
		int i1 = method42(plane, l, i) - j;
		i -= xCameraPos;
		i1 -= zCameraPos;
		l -= yCameraPos;
		int j1 = Model.modelIntArray1[yCameraCurve];
		int k1 = Model.modelIntArray2[yCameraCurve];
		int l1 = Model.modelIntArray1[xCameraCurve];
		int i2 = Model.modelIntArray2[xCameraCurve];
		int j2 = l * l1 + i * i2 >> 16;
		l = l * i2 - i * l1 >> 16;
		i = j2;
		j2 = i1 * k1 - l * j1 >> 16;
		l = i1 * j1 + l * k1 >> 16;
		i1 = j2;
		if (l >= 50) {
			spriteDrawX = Texture.textureInt1 + (i << log_view_dist) / l;
			spriteDrawY = Texture.textureInt2 + (i1 << log_view_dist) / l;
		} else {
			spriteDrawX = -1;
			spriteDrawY = -1;
		}
	}

	private int miniMapHover = -1;

	public void processMinimapHover() {
		final boolean fixed = frameMode == ScreenMode.FIXED;
		miniMapHover = -1;
		if (SettingsManager.orbsEnabled) {
			boolean right = SettingsManager.orbsOnRight;
			Point cXPf = new Point(248, 18);
			Point cXPr = new Point(240, 22);
			Point cRf = new Point(!right ? 230 : 75, 122);
			Point cRr = new Point(224, 126);
			if (super.mouseX >= frameWidth - (fixed ? cXPf.x : cXPr.x)
					&& super.mouseX <= frameWidth - ((fixed ? cXPf.x : cXPr.x) - 22)
					&& super.mouseY >= (fixed ? cXPf.y : cXPr.y) && super.mouseY <= ((fixed ? cXPf.y : cXPr.y) + 28)) {
				miniMapHover = 0;
			} else if (super.mouseX >= frameWidth - (fixed ? cRf.x : cRr.x)
					&& super.mouseX <= frameWidth - ((fixed ? cRf.x : cRr.x) - 56)
					&& super.mouseY >= (fixed ? cRf.y : cRr.y) && super.mouseY <= ((fixed ? cRf.y : cRr.y) + 28)) {
				miniMapHover = 3;
			}
		}
	}

	private void drawOrbs() {
		try {
			if (SettingsManager.orbsEnabled) {
				drawXPorb();
				drawHPOrb();
				drawPrayerOrb();
				drawRunOrb();
			}
		} catch (Exception e) {
		}
	}

	private void drawXPorb() {
		final boolean fixed = frameMode == ScreenMode.FIXED;
		int x = fixed ? 0 : frameWidth - 240, y = fixed ? 18 : 22;
		int spriteId = XPDrop.isEnabled() ? 2 : 0;
		SpriteLoader.getSprite("xpdrop", miniMapHover == 0 ? (spriteId + 1) : spriteId).drawSprite(x, y);
	}

	private boolean runClicked = false;

	// ODEL
	private void drawHPOrb() {

		final boolean fixed = frameMode == ScreenMode.FIXED;
		int currentHP = currentStats[3], maxHP = maxStats[3];
		int health = (int) (((double) currentHP / (double) maxHP) * 100D);
		double modifier = ((double) health / 100D);
		int depleteAmt = 26 - (int) (26 * modifier);

		boolean right = SettingsManager.orbsOnRight;
		Point dF = new Point(!right ? 0 : 192, 40);
		Point dR = new Point(!right ? frameWidth - 240 : frameWidth - 57, 44);
		int x = fixed ? dF.x : dR.x, y = fixed ? dF.y : dR.y;
		SpriteLoader.getSprite("orbs", miniMapHover == 1 ? (!right ? 1 : 3) : (!right ? 0 : 2)).drawSprite(x, y);
		SpriteLoader.getSprite("orbs", 9).drawSprite(x + (!right ? 27 : 4), y + 4);
		Sprite orbFill = SpriteLoader.getSprite("orbs", 4);
		orbFill.myHeight = depleteAmt < 0 ? 0 : depleteAmt;
		orbFill.drawSprite(x + (!right ? 27 : 4), y + 4);
		SpriteLoader.getSprite("orbs", 5).drawSprite(x + (!right ? 33 : 9), y + 10);
		smallFont.method382(getOrbTextColor(health), x + (!right ? 15 : 45), currentHP + "", y + 26, true);
	}

	private void drawPrayerOrb() {
		final boolean fixed = frameMode == ScreenMode.FIXED;
		int currentPP = currentStats[5], maxPP = maxStats[5];
		int prayer = (int) (((double) currentPP / (double) maxPP) * 100D);
		double modifier = ((double) prayer / 100D);
		int depleteAmt = 26 - (int) (26 * modifier);

		boolean right = SettingsManager.orbsOnRight;
		Point dF = new Point(!right ? 2 : 190, 80);
		Point dR = new Point(!right ? frameWidth - 238 : frameWidth - 57, 84);
		int x = fixed ? dF.x : dR.x, y = fixed ? dF.y : dR.y;
		SpriteLoader.getSprite("orbs", miniMapHover == 2 ? (!right ? 1 : 3) : (!right ? 0 : 2)).drawSprite(x, y);
		SpriteLoader.getSprite("orbs", 12).drawSprite(x + (!right ? 27 : 4), y + 4);
		Sprite orbFill = SpriteLoader.getSprite("orbs", 4);
		orbFill.myHeight = depleteAmt < 0 ? 0 : depleteAmt;
		orbFill.drawSprite(x + (!right ? 27 : 4), y + 4);
		SpriteLoader.getSprite("orbs", 6).drawSprite(x + (!right ? 30 : 7), y + 7);
		smallFont.method382(getOrbTextColor(prayer), x + (!right ? 15 : 45), currentPP + "", y + 26, true);
	}

	private void drawRunOrb() {
		final boolean fixed = frameMode == ScreenMode.FIXED;
		int curEnergy = energy, maxEnergy = 100;
		int energyPer = (int) (((double) curEnergy / (double) maxEnergy) * 100D);
		double modifier = ((double) energyPer / 100D);
		int depleteAmt = 26 - (int) (26 * modifier);

		boolean right = SettingsManager.orbsOnRight;
		Point dF = new Point(!right ? 18 : 174, 120);
		Point dR = new Point(!right ? frameWidth - 222 : frameWidth - 57, 124);
		int x = fixed ? dF.x : dR.x, y = fixed ? dF.y : dR.y;
		SpriteLoader.getSprite("orbs", miniMapHover == 3 ? (!right ? 1 : 3) : (!right ? 0 : 2)).drawSprite(x, y);
		SpriteLoader.getSprite("orbs", runClicked ? 15 : 14).drawSprite(x + (!right ? 27 : 4), y + 4);
		Sprite orbFill = SpriteLoader.getSprite("orbs", 4);
		orbFill.myHeight = depleteAmt < 0 ? 0 : depleteAmt;
		orbFill.drawSprite(x + (!right ? 27 : 4), y + 4);
		SpriteLoader.getSprite("orbs", runClicked ? 8 : 7).drawSprite(x + (!right ? 33 : 10), y + 8);
		smallFont.method382(getOrbTextColor(energyPer), x + (!right ? 15 : 45), curEnergy + "", y + 26, true);
	}

	public int getOrbTextColor(int statusInt) {
		if (statusInt >= 75) {
			return 0x00FF00;
		} else if (statusInt >= 50 && statusInt <= 74) {
			return 0xFFFF00;
		} else if (statusInt >= 25 && statusInt <= 49) {
			return 0xFF981F;
		} else {
			return 0xFF0000;
		}
	}

	private void buildSplitPrivateChatMenu() {
		if (splitPrivateChat == 0)
			return;
		int i = 0;
		if (!serverMessage.isEmpty()) {
			i++;
		}
		if (systemUpdateTime != 0) {
			i++;
		}
		for (int j = 0; j < 100; j++)
			if (chatMessages[j] != null) {
				int k = chatTypes[j];
				String s = chatNames[j];
				/*
				 * if (s != null && s.startsWith("@cr1@")) { s = s.substring(5); } if (s != null
				 * && s.startsWith("@cr2@")) { s = s.substring(5); } if (s != null &&
				 * s.startsWith("@cr3@")) { s = s.substring(5); }
				 */
				if (s != null && (s.startsWith("@irn@") || s.startsWith("@hci@") || s.startsWith("@ult@"))) {
					s = s.substring(5);
				}
				if ((k == 3 || k == 7)
						&& (k == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s))) {
					int l = 329 - i * 13;
					if (frameMode != ScreenMode.FIXED) {
						l = frameHeight - 176 - i * 13;
					}
					if (super.mouseX > 4 && super.mouseY - 4 > l - 10 && super.mouseY - 4 <= l + 3) {
						int i1 = regularFont.getTextWidth("From:  " + s + chatMessages[j]) + 25;
						if (i1 > 450)
							i1 = 450;
						if (super.mouseX < 4 + i1) {
							if (myPrivilege >= 1) {
								menuActionName[menuActionRow] = "Report abuse @whi@" + s;
								menuActionID[menuActionRow] = 2606;
								menuActionRow++;
							}
							menuActionName[menuActionRow] = "Add ignore @whi@" + s;
							menuActionID[menuActionRow] = 2042;
							menuActionRow++;
							menuActionName[menuActionRow] = "Add friend @whi@" + s;
							menuActionID[menuActionRow] = 2337;
							menuActionRow++;
						}
					}
					if (++i >= 5)
						return;
				}
				if ((k == 5 || k == 6) && privateChatMode < 2 && ++i >= 5)
					return;
			}

	}

	private void method130(int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2) {
		SpawnedObject class30_sub1 = null;
		for (SpawnedObject class30_sub1_1 = (SpawnedObject) aClass19_1179
				.reverseGetFirst(); class30_sub1_1 != null; class30_sub1_1 = (SpawnedObject) aClass19_1179
						.reverseGetNext()) {
			if (class30_sub1_1.anInt1295 != l1 || class30_sub1_1.anInt1297 != i2 || class30_sub1_1.anInt1298 != j1
					|| class30_sub1_1.anInt1296 != i1)
				continue;
			class30_sub1 = class30_sub1_1;
			break;
		}

		if (class30_sub1 == null) {
			class30_sub1 = new SpawnedObject();
			class30_sub1.anInt1295 = l1;
			class30_sub1.anInt1296 = i1;
			class30_sub1.anInt1297 = i2;
			class30_sub1.anInt1298 = j1;
			method89(class30_sub1);
			aClass19_1179.insertHead(class30_sub1);
		}
		class30_sub1.anInt1291 = k;
		class30_sub1.anInt1293 = k1;
		class30_sub1.anInt1292 = l;
		class30_sub1.anInt1302 = j2;
		class30_sub1.anInt1294 = j;
	}

	private boolean interfaceIsSelected(RSInterface class9) {
		if (class9.valueCompareType == null)
			return false;
		for (int i = 0; i < class9.valueCompareType.length; i++) {
			int j = extractInterfaceValues(class9, i);
			int k = class9.requiredValues[i];
			if (class9.valueCompareType[i] == 2) {
				if (j >= k)
					return false;
			} else if (class9.valueCompareType[i] == 3) {
				if (j <= k)
					return false;
			} else if (class9.valueCompareType[i] == 4) {
				if (j == k)
					return false;
			} else if (j != k)
				return false;
		}

		return true;
	}

	private DataInputStream openJagGrabInputStream(String s) throws IOException {
		if (aSocket832 != null) {
			try {
				aSocket832.close();
			} catch (Exception _ex) {
			}
			aSocket832 = null;
		}
		aSocket832 = openSocket(43595);
		aSocket832.setSoTimeout(10000);
		java.io.InputStream inputstream = aSocket832.getInputStream();
		OutputStream outputstream = aSocket832.getOutputStream();
		outputstream.write(("JAGGRAB /" + s + "\n\n").getBytes());
		return new DataInputStream(inputstream);
	}

	private void method134(Stream stream) {
		int j = stream.readBits(8);
		if (j < playerCount) {
			for (int k = j; k < playerCount; k++)
				anIntArray840[anInt839++] = playerIndices[k];

		}
		if (j > playerCount) {
			Signlink.reportError(myUsername + " Too many players");
			throw new RuntimeException("eek");
		}
		playerCount = 0;
		for (int l = 0; l < j; l++) {
			int i1 = playerIndices[l];
			Player player = playerArray[i1];
			int j1 = stream.readBits(1);
			if (j1 == 0) {
				playerIndices[playerCount++] = i1;
				player.anInt1537 = loopCycle;
			} else {
				int k1 = stream.readBits(2);
				if (k1 == 0) {
					playerIndices[playerCount++] = i1;
					player.anInt1537 = loopCycle;
					npcsAwaitingUpdate[npcsAwaitingUpdateCount++] = i1;
				} else if (k1 == 1) {
					playerIndices[playerCount++] = i1;
					player.anInt1537 = loopCycle;
					int l1 = stream.readBits(3);
					player.moveInDir(false, l1);
					int j2 = stream.readBits(1);
					if (j2 == 1)
						npcsAwaitingUpdate[npcsAwaitingUpdateCount++] = i1;
				} else if (k1 == 2) {
					playerIndices[playerCount++] = i1;
					player.anInt1537 = loopCycle;
					int i2 = stream.readBits(3);
					player.moveInDir(true, i2);
					int k2 = stream.readBits(3);
					player.moveInDir(true, k2);
					int l2 = stream.readBits(1);
					if (l2 == 1)
						npcsAwaitingUpdate[npcsAwaitingUpdateCount++] = i1;
				} else if (k1 == 3)
					anIntArray840[anInt839++] = i1;
			}
		}
	}

	private String loginTooltip = "";

	private void drawLoginTooltip(final int x, final int y) {
		if (loginTooltip == null || loginTooltip.isEmpty()) {
			return;
		}
		int xPos = x;
		int yPos = y;
		int border = 4;
		final int boxLength = newBoldFont.getTextWidth(loginTooltip) + (border * 2);
		final int boxHeight = newBoldFont.baseCharacterHeight + (border * 2) + 2;
		if (xPos + boxLength > frameWidth) {
			xPos = (frameWidth - boxLength);
		}
		if (xPos < 0) {
			xPos = 0;
		}
		if (yPos > frameHeight) {
			yPos = frameHeight;
		}
		if (yPos < boxHeight) {
			yPos = boxHeight;
		}
		DrawingArea.method335(0, yPos - boxHeight, boxLength, boxHeight, 200, xPos);
		newBoldFont.drawBasicString(loginTooltip, xPos + border, yPos - border - 2, 0xffffff, 0);
	}

	private void drawLoginScreen(boolean flag) {
		resetImageProducers();
		aRSImageProducer_1109.initDrawingArea();
		final int centerX = frameWidth / 2, centerY = frameHeight / 2;
		if (background != null) {
			background.drawSprite(0, 0);
		}
		/** all screens */
		// Settings Button
		boolean settingHover = mouseInRegion2(frameWidth - 52, 10, 42, 42);
		SpriteLoader
				.getSprite("login", "settings",
						settingHover ? (loginScreenState == 2 ? 2 : 1) : (loginScreenState == 2 ? 1 : 0))
				.drawSprite(frameWidth - 52, 10);
		// Title Music Button
		if (!audioMuted) {
			if (loginMusicEnabled) {
				SpriteLoader.getSprite("login", 4).drawSprite(frameWidth - 104, 10);
			} else {
				SpriteLoader.getSprite("login", 5).drawSprite(frameWidth - 104, 10);
			}
		}
		if (loginScreenState == 0 || loginScreenState == 1) {
			titleLogo.drawSprite(centerX - (titleLogo.myWidth / 2), 24);
			final int drawX = centerX - 145, drawY = centerY - 135;
			SpriteLoader.getSprite("login", "login_box", 0).drawARGBSprite(drawX, drawY);
			if (loginScreenState == 0) {
				newBoldFont.drawCenteredString("Login", centerX, drawY + 42, 0xf3b13f, 0);
				final int userBox = mouseInRegion2(drawX + 35, drawY + 68, 218, 27) ? 2 : 1;
				final int passBox = mouseInRegion2(drawX + 35, drawY + 114, 218, 27) ? 2 : 1;
				newRegularFont.drawBasicString("Username:", drawX + 36, drawY + 66, 0xebe0bc, 0);
				SpriteLoader.getSprite("login", "login_box", userBox).drawSprite(drawX + 35, drawY + 68);
				newRegularFont.drawBasicString("Password:", drawX + 36, drawY + 112, 0xebe0bc, 0);
				SpriteLoader.getSprite("login", "login_box", passBox).drawSprite(drawX + 35, drawY + 114);
				// username & password input text
				newBoldFont.drawBasicString(
						myUsername + ((loginScreenCursorPos == 0) & (loopCycle % 40 < 20) ? "|" : ""), drawX + 40,
						drawY + 87, 0xf3b13f, 0);
				newBoldFont.drawBasicString(
						TextUtil.passwordAsterisks(myPassword)
								+ ((loginScreenCursorPos == 1) & (loopCycle % 40 < 20) ? "|" : ""),
						drawX + 40, drawY + 135, 0xf3b13f, 0);
				// remember me
				if (rememberMe) {
					SpriteLoader.getSprite("login", 2).drawSprite(drawX + 35, drawY + 150);
				} else {
					boolean hover = mouseInRegion2(drawX + 35, drawY + 150, 106, 13);
					SpriteLoader.getSprite("login", hover ? 3 : 1).drawSprite(drawX + 35, drawY + 150);
				}
				newBoldFont.drawBasicString("Remember Me", drawX + 52, drawY + 161, 0xf3b13f, 0);
				// login btn
				final int loginBtn = mouseInRegion2(drawX + 55, drawY + 174, 179, 25) ? 4 : 3;
				SpriteLoader.getSprite("login", "login_box", loginBtn).drawSprite(drawX + 55, drawY + 174);
				newRegularFont.drawCenteredString("Log In", centerX, drawY + 191, 0, -1);
			} else if (loginScreenState == 1) {
				newBoldFont.drawCenteredString("Authenticator", centerX, drawY + 42, 0xf3b13f, 0);
				newBoldFont.drawWrappedText("Enter the 6-digit code generated by your authenticator app.", drawX + 34,
						drawY + 60, 228, 32, 0xebe0bc, 0, 0, 0, 16);
				final int userBox = mouseInRegion2(drawX + 35, drawY + 105, 218, 27) ? 2 : 1;
				final int verifyBtn = mouseInRegion2(centerX - 57, drawY + 145, 114, 25) ? 6 : 5;
				final int cancelBtn = mouseInRegion2(centerX - 57, drawY + 178, 114, 25) ? 6 : 5;
				SpriteLoader.getSprite("login", "login_box", userBox).drawSprite(drawX + 35, drawY + 105);
				SpriteLoader.getSprite("login", "login_box", verifyBtn).drawSprite(centerX - 57, drawY + 145);
				SpriteLoader.getSprite("login", "login_box", cancelBtn).drawSprite(centerX - 57, drawY + 178);
				newRegularFont.drawCenteredString("Verify", centerX, drawY + 162, 0, -1);
				newRegularFont.drawCenteredString("Cancel", centerX, drawY + 195, 0, -1);
				// code string
				newBoldFont.drawCenteredString(
						TextUtil.passwordAsterisks(verificationCodeS)
								+ ((loginScreenCursorPos == 0) & (loopCycle % 40 < 20) ? "|" : ""),
						centerX, drawY + 124, 0xf3b13f, 0);
			}
			// line
			DrawingArea.method339(drawY + 207, 0, 215, drawX + 35);
			// login messages
			newBoldFont.drawCenteredString(loginMessage1, centerX, drawY + 226, 0xf3b13f, 0);
			newBoldFont.drawCenteredString(loginMessage2, centerX, drawY + 247, 0xf3b13f, 0);
		} else if (loginScreenState == 2) {
			final int drawX = centerX - 145;
			final int drawY = 64;
			// settings screen
			newFancyFontL.drawCenteredString("SETTINGS", centerX, drawY, 0xffffff, 0);
			/** UI AREA */
			final Rectangle uiArea = new Rectangle(centerX - 138, drawY + 24, 276, 200);
			// area bg
			DrawingArea.method335(0, uiArea.y, uiArea.width, uiArea.height, 100, uiArea.x);
			newFancyFontL.drawCenteredString("UI", (int) uiArea.getCenterX(), uiArea.y + 26, 0xffffff, 0);
			final int frameButtonY = uiArea.y + 30;
			// default frame
			boolean curUI = GameFrameManager.isCurrentUI(GameFrameUI.DEFAULT);
			SpriteLoader
					.getSprite("login", "settings",
							((curUI || mouseInRegion2((int) uiArea.getCenterX() - 132, frameButtonY, 128, 96)) ? 4 : 3))
					.drawSprite((int) uiArea.getCenterX() - 132, frameButtonY);
			newFancyFontM.drawCenteredString("2007", (int) uiArea.getCenterX() - 68, frameButtonY + 112,
					curUI ? 0xffff00 : 0xffffff, 0);
			// classic frame
			curUI = GameFrameManager.isCurrentUI(GameFrameUI.CLASSIC);
			SpriteLoader
					.getSprite("login", "settings",
							((curUI || mouseInRegion2((int) uiArea.getCenterX() + 4, frameButtonY, 128, 96)) ? 6 : 5))
					.drawSprite((int) uiArea.getCenterX() + 4, frameButtonY);
			newFancyFontM.drawCenteredString("Pre-2007", (int) uiArea.getCenterX() + 68, frameButtonY + 112,
					curUI ? 0xffff00 : 0xffffff, 0);

			if (pixelScaling == 1) {
				boolean hover = mouseInRegion2(drawX + 85, drawY + 180, 106, 13);
				SpriteLoader.getSprite("login", hover ? 3 : 1).drawSprite(drawX + 85, drawY + 180);
			} else if (pixelScaling == 2) {
				SpriteLoader.getSprite("login", 2).drawSprite(drawX + 85, drawY + 180);
			}
			newBoldFont.drawBasicString("Pixel doubling", drawX + 102, drawY + 192, 0xf3b13f, 0);
			newBoldFont.drawBasicString("(only works in fixed client mode for now)", drawX + 12, drawY + 210, 0xf3b13f,
					0);
		}
		// versioning
		if (outDated) {
			newBoldFont.drawBasicString("New Client Available", 16, frameHeight - 16, 0xff0000, 0);
		}
		drawLoginTooltip(super.mouseX, super.mouseY);
		aRSImageProducer_1109.drawGraphics(0, super.graphics, 0);
	}

	private void processLoginScreenInput() {
		if (super.clickMode2 == 1 || super.clickMode3 == 1)
			anInt1213++;

		loginTooltip = "";
		final int centerX = frameWidth / 2, centerY = frameHeight / 2;

		// tooltip Settings button
		if (mouseInRegion2(frameWidth - 52, 10, 42, 42)) {
			if (loginScreenState != 1) {
				loginTooltip = loginScreenState != 2 ? "Settings" : "Main Screen";
			} else {
				loginTooltip = "Disabled on this screen";
			}
		}
		// tooltip Mute Music button
		if (mouseInRegion2(frameWidth - 104, 10, 42, 42)) {
			loginTooltip = (loginMusicEnabled ? "Mute" : "Un-Mute") + " Title Music";
		}
		if (super.clickMode3 == 1) {
			/** all screens */
			// Settings button
			if (loginScreenState != 1) {
				if (clickInRegion(frameWidth - 52, 10, 42, 42)) {
					loginScreenState = (loginScreenState != 2 ? 2 : 0);
					return;
				}
			}
			// Mute Music button
			if (clickInRegion(frameWidth - 104, 10, 42, 42)) {
				if (audioMuted) {
					return;
				}
				loginMusicEnabled = !loginMusicEnabled;
				if (loginMusicEnabled) {
					musicVolume = 256;
					nextSong = 0;
					songChanging = true;
					onDemandFetcher.request(2, nextSong);
				} else {
					stopMidi();
				}
				SettingsManager.write();
				return;
			}
			if (loginScreenState == 0) {
				final int drawX = centerX - 145, drawY = centerY - 135;
				if (clickInRegion(drawX + 35, drawY + 68, 218, 27)) { // username
					loginScreenCursorPos = 0;
				} else if (clickInRegion(drawX + 35, drawY + 114, 218, 27)) { // password
					loginScreenCursorPos = 1;
				} else if (clickInRegion(drawX + 35, drawY + 150, 106, 13)) { // remember me
					rememberMe = !rememberMe;
					SettingsManager.write();
				} else if (clickInRegion(drawX + 55, drawY + 174, 179, 25)) { // login button
					attemptLogin();
				}
			} else if (loginScreenState == 1) {
				final int drawX = centerX - 145, drawY = centerY - 135;
				if (clickInRegion(centerX - 57, drawY + 145, 114, 25)) { // verify button
					attemptLogin();
				} else if (clickInRegion(centerX - 57, drawY + 178, 114, 25)) { // cancel button
					loginScreenCursorPos = 0;
					loginScreenState = 0;
					verificationCode = -1;
					verificationCodeS = "";
					loginMessage1 = "";
					loginMessage2 = "";
				}
			} else if (loginScreenState == 2) {
				final int drawX = centerX - 145;
				final int drawY = 64;
				/** UI AREA */
				final Rectangle uiArea = new Rectangle(centerX - 138, drawY + 24, 276, 150);
				final int frameButtonY = uiArea.y + 30;
				if (!GameFrameManager.isCurrentUI(GameFrameUI.DEFAULT)
						&& clickInRegion((int) uiArea.getCenterX() - 132, frameButtonY, 128, 96)) { // default frame
					GameFrameManager.setFrameUI(GameFrameUI.DEFAULT);
					gameFrame = GameFrameManager.getFrame();
					SettingsManager.write();
				} else if (!GameFrameManager.isCurrentUI(GameFrameUI.CLASSIC)
						&& clickInRegion((int) uiArea.getCenterX() + 4, frameButtonY, 128, 96)) { // classic frame
					GameFrameManager.setFrameUI(GameFrameUI.CLASSIC);
					gameFrame = GameFrameManager.getFrame();
					SettingsManager.write();
				} else if (clickInRegion(drawX + 85, drawY + 180, 106, 13)) { // remember me
					if (pixelScaling == 1) {
						pixelScaling = 2;
					} else if (pixelScaling == 2) {
						pixelScaling = 1;
					}
					rebuildFrameSize(SettingsManager.screenMode, frameWidth, frameHeight);
				}
			}
		}
		// handle typing
		if (loginScreenState == 0 || loginScreenState == 1) {
			final int curChar = readChar(-796);
			if (curChar == -1)
				return;
			if (loginScreenState == 0) {
				boolean isValidChar = TextUtil.validChar((char) curChar, loginScreenCursorPos == 1);
				if (loginScreenCursorPos == 0) {
					if (curChar == 8 && myUsername.length() > 0)
						myUsername = myUsername.substring(0, myUsername.length() - 1);
					if (curChar == 9 || curChar == 10 || curChar == 13)
						loginScreenCursorPos = 1;
					if (isValidChar)
						myUsername += (char) curChar;
					if (myUsername.length() > 12)
						myUsername = myUsername.substring(0, 12);
				} else if (loginScreenCursorPos == 1) {
					if (curChar == 8 && myPassword.length() > 0)
						myPassword = myPassword.substring(0, myPassword.length() - 1);
					if (curChar == 9)
						loginScreenCursorPos = 0;
					if (isValidChar)
						myPassword += (char) curChar;
					if (myPassword.length() > 20)
						myPassword = myPassword.substring(0, 20);
					if (curChar == 13 || curChar == 10) {
						attemptLogin();
					}
				}
			} else if (loginScreenState == 1) {
				boolean isValidChar = (curChar >= 48 && curChar <= 57);
				if (loginScreenCursorPos == 0) {
					if (curChar == 8 && verificationCodeS.length() > 0)
						verificationCodeS = verificationCodeS.substring(0, verificationCodeS.length() - 1);
					if (isValidChar)
						verificationCodeS += (char) curChar;
					if (verificationCodeS.length() > 6)
						verificationCodeS = verificationCodeS.substring(0, 6);
					if (verificationCodeS != null && !verificationCodeS.isEmpty()) {
						try {
							verificationCode = Integer.parseInt(verificationCodeS);
						} catch (Exception ex) {
							verificationCode = -1;
							verificationCodeS = "";
							return;
						}
					}
					if (curChar == 13 || curChar == 10) {
						attemptLogin();
					}
				}
			}
		}
	}

	private void attemptLogin() {
		if (loggedIn) {
			return;
		}
		if (TextUtil.validName(myUsername) && TextUtil.validPassword(myPassword)) {
			loginFailures = 0;

			login(myUsername, myPassword, false);
			if (!SettingsManager.savedUsername.equals(myUsername)
					|| !SettingsManager.savedPassword.equals(myPassword)) {
				SettingsManager.write();
			}
			stopMidi();
		} else {
			loginMessage2 = "";
			loginMessage1 = "Invalid username or password.";
		}
	}

	private void checkClientVersion() throws IOException {
		String versionURL = "http://vidyascape.org/files/client/clientVersion.dat";
		BufferedReader cacheVerReader = new BufferedReader(new InputStreamReader(new URL(versionURL).openStream()));
		String line;
		try {
			if ((line = cacheVerReader.readLine()) != null) {
				outDated = !line.equalsIgnoreCase(ClientSettings.CLIENT_VERSION);
			}
			cacheVerReader.close();
		} catch (IOException e) {
			System.out.println("problem reading remote client version");
			cacheVerReader.close();
		}
	}

	@Override
	public void redraw() {
		redrawGame = true;
	}

	private void parseRegionPackets(Stream stream, int opCode) {
		if (opCode == 84) { // alter item count
			int k = stream.readUnsignedByte();
			int j3 = tempPositionX + (k >> 4 & 7);
			int i6 = tempPositionY + (k & 7);
			int l8 = stream.readUnsignedShort();
			int k11 = stream.readUnsignedShort();
			int l13 = stream.readUnsignedShort();
			if (j3 >= 0 && i6 >= 0 && j3 < 104 && i6 < 104) {
				NodeList groundItemsNodeList = groundArray[plane][j3][i6];
				if (groundItemsNodeList != null) {
					for (Item groundItem = (Item) groundItemsNodeList
							.reverseGetFirst(); groundItem != null; groundItem = (Item) groundItemsNodeList
									.reverseGetNext()) {
						if (groundItem.ID != (l8 & 0x7fff) || groundItem.itemCount != k11)
							continue;
						groundItem.itemCount = l13;
						break;
					}
					updateGroundItem(j3, i6);
				}
			}
			return;
		}
		if (opCode == 105) { // Play sound in location
			int k = stream.readUnsignedByte();
			int j3 = tempPositionX + (k >> 4 & 7);
			int i6 = tempPositionY + (k & 7);
			int l8 = stream.readUnsignedShort();
			int k11 = stream.readUnsignedByte();
			int l13 = k11 >> 4 & 0xf;
			int l15 = k11 & 7;
			if (myPlayer.smallX[0] >= j3 - l13 && myPlayer.smallX[0] <= j3 + l13 && myPlayer.smallY[0] >= i6 - l13
					&& myPlayer.smallY[0] <= i6 + l13 && soundEnabled && !lowMem && currentSound < 50) {
				playSound(l8, l15, 0);
			}
		}
		if (opCode == 215) { // Something to do with ground items
			int i1 = stream.method435();
			int l3 = stream.method428();
			int k6 = tempPositionX + (l3 >> 4 & 7);
			int j9 = tempPositionY + (l3 & 7);
			int i12 = stream.method435();
			int j14 = stream.readUnsignedShort();
			if (k6 >= 0 && j9 >= 0 && k6 < 104 && j9 < 104 && i12 != localPlayerIndex) {
				Item class30_sub2_sub4_sub2_2 = new Item();
				class30_sub2_sub4_sub2_2.ID = i1;
				class30_sub2_sub4_sub2_2.itemCount = j14;
				if (groundArray[plane][k6][j9] == null)
					groundArray[plane][k6][j9] = new NodeList();
				groundArray[plane][k6][j9].insertHead(class30_sub2_sub4_sub2_2);
				updateGroundItem(k6, j9);
			}
			return;
		}
		if (opCode == 156) { // Removes an item on the ground.
			int j1 = stream.method426();
			int i4 = tempPositionX + (j1 >> 4 & 7);
			int l6 = tempPositionY + (j1 & 7);
			int k9 = stream.readUnsignedShort();
			if (i4 >= 0 && l6 >= 0 && i4 < 104 && l6 < 104) {
				NodeList class19 = groundArray[plane][i4][l6];
				if (class19 != null) {
					for (Item item = (Item) class19.reverseGetFirst(); item != null; item = (Item) class19
							.reverseGetNext()) {
						if (item.ID != (k9 & 0x7fff))
							continue;
						item.unlink();
						break;
					}

					if (class19.reverseGetFirst() == null)
						groundArray[plane][i4][l6] = null;
					updateGroundItem(i4, l6);
				}
			}
			return;
		}
		if (opCode == 160) { // Animates a game object
			int k1 = stream.method428();
			int j4 = tempPositionX + (k1 >> 4 & 7);
			int i7 = tempPositionY + (k1 & 7);
			int l9 = stream.method428();
			int j12 = l9 >> 2;
			int k14 = l9 & 3;
			int j16 = anIntArray1177[j12];
			int j17 = stream.method435();
			if (j4 >= 0 && i7 >= 0 && j4 < 103 && i7 < 103) {
				int j18 = tileHeights[plane][j4][i7];
				int i19 = tileHeights[plane][j4 + 1][i7];
				int l19 = tileHeights[plane][j4 + 1][i7 + 1];
				int k20 = tileHeights[plane][j4][i7 + 1];
				if (j16 == 0) {
					Wall class10 = worldController.method296(plane, j4, i7);
					if (class10 != null) {
						int k21 = class10.key >> 14 & 0x7fff;
						if (j12 == 2) {
							class10.aClass30_Sub2_Sub4_278 = new RenderableObject(k21, 4 + k14, 2, i19, l19, j18, k20,
									j17, false);
							class10.aClass30_Sub2_Sub4_279 = new RenderableObject(k21, k14 + 1 & 3, 2, i19, l19, j18,
									k20, j17, false);
						} else {
							class10.aClass30_Sub2_Sub4_278 = new RenderableObject(k21, k14, j12, i19, l19, j18, k20,
									j17, false);
						}
					}
				}
				if (j16 == 1) {
					WallDecoration class26 = worldController.method297(j4, i7, plane);
					if (class26 != null)
						class26.aClass30_Sub2_Sub4_504 = new RenderableObject(class26.key >> 14 & 0x7fff, 0, 4, i19,
								l19, j18, k20, j17, false);
				}
				if (j16 == 2) {
					GameObject class28 = worldController.method298(j4, i7, plane);
					if (j12 == 11)
						j12 = 10;
					if (class28 != null)
						class28.renderable = new RenderableObject(class28.key >> 14 & 0x7fff, k14, j12, i19, l19, j18,
								k20, j17, false);
				}
				if (j16 == 3) {
					GroundDecoration class49 = worldController.method299(i7, j4, plane);
					if (class49 != null)
						class49.aClass30_Sub2_Sub4_814 = new RenderableObject(class49.key >> 14 & 0x7fff, k14, 22, i19,
								l19, j18, k20, j17, false);
				}
			}
			return;
		}
		if (opCode == 147) { // Transforms a player into a game object
			int l1 = stream.method428();
			int k4 = tempPositionX + (l1 >> 4 & 7);
			int j7 = tempPositionY + (l1 & 7);
			int i10 = stream.readUnsignedShort();
			byte byte0 = stream.method430();
			int l14 = stream.method434();
			byte byte1 = stream.method429();
			int k17 = stream.readUnsignedShort();
			int k18 = stream.method428();
			int j19 = k18 >> 2;
			int i20 = k18 & 3;
			int l20 = anIntArray1177[j19];
			byte byte2 = stream.readSignedByte();
			int l21 = stream.readUnsignedShort();
			byte byte3 = stream.method429();
			Player player;
			if (i10 == localPlayerIndex)
				player = myPlayer;
			else
				player = playerArray[i10];
			if (player != null) {
				ObjectDef class46 = ObjectDef.forID(l21);
				if (class46 != null) {
					int i22 = tileHeights[plane][k4][j7];
					int j22 = tileHeights[plane][k4 + 1][j7];
					int k22 = tileHeights[plane][k4 + 1][j7 + 1];
					int l22 = tileHeights[plane][k4][j7 + 1];
					Model model = class46.method578(j19, i20, i22, j22, k22, l22, -1);
					if (model != null) {
						method130(k17 + 1, -1, 0, l20, j7, 0, plane, k4, l14 + 1);
						player.anInt1707 = l14 + loopCycle;
						player.anInt1708 = k17 + loopCycle;
						player.aModel_1714 = model;
						int i23 = class46.width;
						int j23 = class46.length;
						if (i20 == 1 || i20 == 3) {
							i23 = class46.length;
							j23 = class46.width;
						}
						player.anInt1711 = k4 * 128 + i23 * 64;
						player.anInt1713 = j7 * 128 + j23 * 64;
						player.anInt1712 = method42(plane, player.anInt1713, player.anInt1711);
						if (byte2 > byte0) {
							byte byte4 = byte2;
							byte2 = byte0;
							byte0 = byte4;
						}
						if (byte3 > byte1) {
							byte byte5 = byte3;
							byte3 = byte1;
							byte1 = byte5;
						}
						player.anInt1719 = k4 + byte2;
						player.anInt1721 = k4 + byte0;
						player.anInt1720 = j7 + byte3;
						player.anInt1722 = j7 + byte1;
					}
				}
			}
		}
		if (opCode == 151) { // Adds a regional object to the game world
			int i2 = stream.method426();
			int l4 = tempPositionX + (i2 >> 4 & 7);
			int k7 = tempPositionY + (i2 & 7);
			int j10 = stream.method434();
			int k12 = stream.method428();
			int i15 = k12 >> 2;
			int k16 = k12 & 3;
			int l17 = anIntArray1177[i15];
			if (l4 >= 0 && k7 >= 0 && l4 < 104 && k7 < 104) {
				method130(-1, j10, k16, l17, k7, i15, plane, l4, 0);
				refreshMinimap = true;
			}
			return;
		}
		if (opCode == 4) { // Displays a stationary animation
			int j2 = stream.readUnsignedByte();
			int i5 = tempPositionX + (j2 >> 4 & 7);
			int l7 = tempPositionY + (j2 & 7);
			int k10 = stream.readUnsignedShort();
			int l12 = stream.readUnsignedByte();
			int j15 = stream.readUnsignedShort();
			if (i5 >= 0 && l7 >= 0 && i5 < 104 && l7 < 104) {
				i5 = i5 * 128 + 64;
				l7 = l7 * 128 + 64;
				AnimableObject class30_sub2_sub4_sub3 = new AnimableObject(plane, loopCycle, j15, k10,
						method42(plane, l7, i5) - l12, l7, i5);
				incompleteAnimables.insertHead(class30_sub2_sub4_sub3);
			}
			return;
		}
		if (opCode == 44) { // Display's a ground item at a specified coordinate
			int k2 = stream.method436();
			int j5 = stream.readUnsignedShort();
			int i8 = stream.readUnsignedByte();
			int l10 = tempPositionX + (i8 >> 4 & 7);
			int i13 = tempPositionY + (i8 & 7);
			if (l10 >= 0 && i13 >= 0 && l10 < 104 && i13 < 104) {
				Item class30_sub2_sub4_sub2_1 = new Item();
				class30_sub2_sub4_sub2_1.ID = k2;
				class30_sub2_sub4_sub2_1.itemCount = j5;
				if (groundArray[plane][l10][i13] == null)
					groundArray[plane][l10][i13] = new NodeList();
				groundArray[plane][l10][i13].insertHead(class30_sub2_sub4_sub2_1);
				updateGroundItem(l10, i13);
			}
			return;
		}
		if (opCode == 101) { // Removes a regional object from the game world
			int l2 = stream.method427();
			int k5 = l2 >> 2;
			int j8 = l2 & 3;
			int i11 = anIntArray1177[k5];
			int j13 = stream.readUnsignedByte();
			int k15 = tempPositionX + (j13 >> 4 & 7);
			int l16 = tempPositionY + (j13 & 7);
			if (k15 >= 0 && l16 >= 0 && k15 < 104 && l16 < 104) {
				method130(-1, -1, j8, i11, l16, k5, plane, k15, 0);
			}
			return;
		}
		if (opCode == 117) { // Displays a projectile for the player
			int i3 = stream.readUnsignedByte();
			int l5 = tempPositionX + (i3 >> 4 & 7);
			int k8 = tempPositionY + (i3 & 7);
			int j11 = l5 + stream.readSignedByte();
			int k13 = k8 + stream.readSignedByte();
			int l15 = stream.readSignedShort();
			int i17 = stream.readUnsignedShort();
			int i18 = stream.readUnsignedByte() * 4;
			int l18 = stream.readUnsignedByte() * 4;
			int k19 = stream.readUnsignedShort();
			int j20 = stream.readUnsignedShort();
			int i21 = stream.readUnsignedByte();
			int j21 = stream.readUnsignedByte();
			if (l5 >= 0 && k8 >= 0 && l5 < 104 && k8 < 104 && j11 >= 0 && k13 >= 0 && j11 < 104 && k13 < 104
					&& i17 != 65535) {
				l5 = l5 * 128 + 64;
				k8 = k8 * 128 + 64;
				j11 = j11 * 128 + 64;
				k13 = k13 * 128 + 64;
				Projectile class30_sub2_sub4_sub4 = new Projectile(i21, l18, k19 + loopCycle, j20 + loopCycle, j21,
						plane, method42(plane, k8, l5) - i18, k8, l5, l15, i17);
				class30_sub2_sub4_sub4.method455(k19 + loopCycle, k13, method42(plane, k13, j11) - l18, j11);
				projectiles.insertHead(class30_sub2_sub4_sub4);
			}
		}
	}

	private void moveNpcs(Stream stream) {
		stream.initBitAccess();
		int k = stream.readBits(8);
		if (k < npcCount) {
			for (int l = k; l < npcCount; l++)
				anIntArray840[anInt839++] = npcIndices[l];

		}
		if (k > npcCount) {
			Signlink.reportError(myUsername + " Too many npcs");
			throw new RuntimeException("eek");
		}
		npcCount = 0;
		for (int i1 = 0; i1 < k; i1++) {
			int j1 = npcIndices[i1];
			NPC npc = npcArray[j1];
			int k1 = stream.readBits(1);
			if (k1 == 0) {
				npcIndices[npcCount++] = j1;
				npc.anInt1537 = loopCycle;
			} else {
				int l1 = stream.readBits(2);
				if (l1 == 0) {
					npcIndices[npcCount++] = j1;
					npc.anInt1537 = loopCycle;
					npcsAwaitingUpdate[npcsAwaitingUpdateCount++] = j1;
				} else if (l1 == 1) {
					npcIndices[npcCount++] = j1;
					npc.anInt1537 = loopCycle;
					int i2 = stream.readBits(3);
					npc.moveInDir(false, i2);
					int k2 = stream.readBits(1);
					if (k2 == 1)
						npcsAwaitingUpdate[npcsAwaitingUpdateCount++] = j1;
				} else if (l1 == 2) {
					npcIndices[npcCount++] = j1;
					npc.anInt1537 = loopCycle;
					int j2 = stream.readBits(3);
					npc.moveInDir(true, j2);
					int l2 = stream.readBits(3);
					npc.moveInDir(true, l2);
					int i3 = stream.readBits(1);
					if (i3 == 1)
						npcsAwaitingUpdate[npcsAwaitingUpdateCount++] = j1;
				} else if (l1 == 3)
					anIntArray840[anInt839++] = j1;
			}
		}

	}

	private void method142(int i, int j, int k, int l, int i1, int j1, int k1) {
		if (i1 >= 1 && i >= 1 && i1 <= 102 && i <= 102) {
			if (lowMem && j != plane)
				return;
			int i2 = 0;
			if (j1 == 0)
				i2 = worldController.method300(j, i1, i);
			if (j1 == 1)
				i2 = worldController.method301(j, i1, i);
			if (j1 == 2)
				i2 = worldController.method302(j, i1, i);
			if (j1 == 3)
				i2 = worldController.method303(j, i1, i);
			if (i2 != 0) {
				int i3 = worldController.method304(j, i1, i, i2);
				int j2 = i2 >> 14 & 0x7fff;
				int k2 = i3 & 0x1f;
				int l2 = i3 >> 6;
				if (j1 == 0) {
					worldController.method291(i1, j, i, (byte) -119);
					ObjectDef class46 = ObjectDef.forID(j2);
					if (class46 != null && class46.solid)
						collisionMaps[j].method215(l2, k2, class46.impenetrable, i1, i);
				}
				if (j1 == 1)
					worldController.method292(i, j, i1);
				if (j1 == 2) {
					worldController.method293(j, i1, i);
					ObjectDef class46_1 = ObjectDef.forID(j2);
					if (class46_1 == null)
						return;
					if (i1 + class46_1.width > 103 || i + class46_1.width > 103 || i1 + class46_1.length > 103
							|| i + class46_1.length > 103)
						return;
					if (class46_1.solid)
						collisionMaps[j].method216(l2, class46_1.width, i1, i, class46_1.length,
								class46_1.impenetrable);
				}
				if (j1 == 3) {
					worldController.method294(j, i, i1);
					ObjectDef class46_2 = ObjectDef.forID(j2);
					if (class46_2 == null)
						return;
					if (class46_2.solid && class46_2.hasActions)
						collisionMaps[j].method218(i, i1);
				}
			}
			if (k1 >= 0) {
				int j3 = j;
				if (j3 < 3 && (tileFlags[1][i1][i] & 2) == 2)
					j3++;
				MapRegion.method188(worldController, k, i, l, j3, collisionMaps[j], tileHeights, i1, k1, j);
			}
		}
	}

	private void updatePlayers(int i, Stream stream) {
		anInt839 = 0;
		npcsAwaitingUpdateCount = 0;
		method117(stream);
		method134(stream);
		method91(stream, i);
		method49(stream);
		for (int k = 0; k < anInt839; k++) {
			int l = anIntArray840[k];
			if (playerArray[l].anInt1537 != loopCycle)
				playerArray[l] = null;
		}

		if (stream.currentOffset != i) {
			Signlink.reportError("Error packet size mismatch in getplayer pos:" + stream.currentOffset + " psize:" + i);
			throw new RuntimeException("eek");
		}
		for (int i1 = 0; i1 < playerCount; i1++)
			if (playerArray[playerIndices[i1]] == null) {
				Signlink.reportError(myUsername + " null entry in pl list - pos:" + i1 + " size:" + playerCount);
				throw new RuntimeException("eek");
			}

	}

	private void setCameraPos(int j, int k, int l, int i1, int j1, int k1) {
		int l1 = 2048 - k & 0x7ff;
		int i2 = 2048 - j1 & 0x7ff;
		int j2 = 0;
		int k2 = 0;
		int l2 = j;
		if (l1 != 0) {
			int i3 = Model.modelIntArray1[l1];
			int k3 = Model.modelIntArray2[l1];
			int i4 = k2 * k3 - l2 * i3 >> 16;
			l2 = k2 * i3 + l2 * k3 >> 16;
			k2 = i4;
		}
		if (i2 != 0) {
			/*
			 * xxx if(cameratoggle){ if(zoom == 0) zoom = k2; if(lftrit == 0) lftrit = j2;
			 * if(fwdbwd == 0) fwdbwd = l2; k2 = zoom; j2 = lftrit; l2 = fwdbwd; }
			 */
			int j3 = Model.modelIntArray1[i2];
			int l3 = Model.modelIntArray2[i2];
			int j4 = l2 * j3 + j2 * l3 >> 16;
			l2 = l2 * l3 - j2 * j3 >> 16;
			j2 = j4;
		}
		xCameraPos = l - j2;
		zCameraPos = i1 - k2;
		yCameraPos = k1 - l2;
		yCameraCurve = k;
		xCameraCurve = j1;
	}

	public void sendFrame36(int id, int state) {
		anIntArray1045[id] = state;
		if (variousSettings[id] != state) {
			variousSettings[id] = state;
			doConfigAction(id);
			redrawTab = true;
			if (dialogID != -1)
				redrawChatbox = true;
		}
	}

	public void changeConfig(int configId, int configValue) {
		anIntArray1045[configId] = configValue;
		if (variousSettings[configId] != configValue) {
			variousSettings[configId] = configValue;
			redrawTab = true;
			if (dialogID != -1)
				redrawChatbox = true;
		}
	}

	public void sendFrame219() {
		if (invOverlayInterfaceID != -1) {
			invOverlayInterfaceID = -1;
			redrawTabIcons = true;
			redrawTab = true;
		}
		if (backDialogID != -1) {
			backDialogID = -1;
			redrawChatbox = true;
		}
		if (inputDialogState != 0) {
			inputDialogState = 0;
			redrawChatbox = true;
		}
		openInterfaceID = -1;
		continueDialogue = false;
	}

	public void sendFrame248(int interfaceID, int sideInterfaceID) {
		if (backDialogID != -1) {
			backDialogID = -1;
			redrawChatbox = true;
		}
		if (inputDialogState != 0) {
			inputDialogState = 0;
			redrawChatbox = true;
		}
		openInterfaceID = interfaceID;
		invOverlayInterfaceID = sideInterfaceID;
		redrawTab = true;
		redrawTabIcons = true;
		continueDialogue = false;
	}

	private boolean parsePacket() {
		if (socketStream == null)
			return false;
		try {
			int available = socketStream.available();
			if (available == 0)
				return false;
			if (opcode == -1) {
				socketStream.flushInputStream(inStream.buffer, 1);
				opcode = inStream.buffer[0] & 0xff;
				if (encryption != null)
					opcode = opcode - encryption.getNextKey() & 0xff;
				packetSize = PacketConstants.packetSizes[opcode];
				available--;
			}
			if (packetSize == -1)
				if (available > 0) {
					socketStream.flushInputStream(inStream.buffer, 1);
					packetSize = inStream.buffer[0] & 0xff;
					available--;
				} else {
					return false;
				}
			if (packetSize == -2)
				if (available > 1) {
					socketStream.flushInputStream(inStream.buffer, 2);
					inStream.currentOffset = 0;
					packetSize = inStream.readUnsignedShort();
					available -= 2;
				} else {
					return false;
				}
			if (available < packetSize)
				return false;
			inStream.currentOffset = 0;
			socketStream.flushInputStream(inStream.buffer, packetSize);
			timeoutCounter = 0;
			thirdLastOpcode = secondLastOpcode;
			secondLastOpcode = lastOpcode;
			lastOpcode = opcode;
			switch (opcode) {
			case 81: // PlayerUpdating update()
				updatePlayers(packetSize, inStream);
				aBoolean1080 = false;
				opcode = -1;
				return true;

			case 176: // Welcome screen packet UNUSED
				daysSinceRecovChange = inStream.method427();
				unreadMessages = inStream.method435();
				membersInt = inStream.readUnsignedByte();
				anInt1193 = inStream.method440();
				daysSinceLastLogin = inStream.readUnsignedShort();
				if (anInt1193 != 0 && openInterfaceID == -1) {
					Signlink.dnslookup(TextUtil.method586(anInt1193));
					clearTopInterfaces();
					char c = '\u028A';
					if (daysSinceRecovChange != 201 || membersInt == 1)
						c = '\u028F';
					reportAbuseInput = "";
					canMute = false;
					// TODO?
					/*
					 * for (int k9 = 0; k9 < RSInterface.interfaceCache.length; k9++) { if
					 * (RSInterface.interfaceCache[k9] == null ||
					 * RSInterface.interfaceCache[k9].contentType != c) continue; openInterfaceID =
					 * RSInterface.interfaceCache[k9].parentID;
					 *
					 * }
					 */
				}
				opcode = -1;
				return true;

			case 64: // delete Ground item? UNUSED
				tempPositionX = inStream.method427();
				tempPositionY = inStream.method428();
				for (int j = tempPositionX; j < tempPositionX + 8; j++) {
					for (int l9 = tempPositionY; l9 < tempPositionY + 8; l9++)
						if (groundArray[plane][j][l9] != null) {
							groundArray[plane][j][l9] = null;
							updateGroundItem(j, l9);
						}
				}
				for (SpawnedObject class30_sub1 = (SpawnedObject) aClass19_1179
						.reverseGetFirst(); class30_sub1 != null; class30_sub1 = (SpawnedObject) aClass19_1179
								.reverseGetNext())
					if (class30_sub1.anInt1297 >= tempPositionX && class30_sub1.anInt1297 < tempPositionX + 8
							&& class30_sub1.anInt1298 >= tempPositionY && class30_sub1.anInt1298 < tempPositionY + 8
							&& class30_sub1.anInt1295 == plane)
						class30_sub1.anInt1294 = 0;
				opcode = -1;
				return true;

			case 185: // sendPlayerDialogueHead()
				int phInterfaceID = inStream.method436();
				int phChildID = inStream.method436();
				RSInterface phInterface = RSInterface.getInterface(phInterfaceID, phChildID);
				if (phInterface != null) {
					phInterface.defaultMediaType = 3;
					if (myPlayer.desc == null)
						phInterface.defaultMedia = (myPlayer.anIntArray1700[0] << 25)
								+ (myPlayer.anIntArray1700[4] << 20) + (myPlayer.equipment[0] << 15)
								+ (myPlayer.equipment[8] << 10) + (myPlayer.equipment[11] << 5) + myPlayer.equipment[1];
					else
						phInterface.defaultMedia = (int) (0x12345678L + myPlayer.desc.interfaceType);
				}
				opcode = -1;
				return true;

			case 216: // ActionSender sendClanChat() Clan Chat message string
				try {
					String name = inStream.readString();
					String message = inStream.readString();
					int rights = inStream.readUnsignedByte();
					int chatIcon = inStream.readUnsignedByte();
					boolean ignore = false;
					long nameeLong = TextUtil.longForName(name);
					for (int l29 = 0; l29 < ignoreCount; l29++) {
						if (ignoreListAsLongs[l29] != nameeLong)
							continue;
						ignore = true;

					}
					if (!ignore) {
						pushMessage(16, getClanPrefix(),
								(chatIcon > 0 ? iconSymbol(chatIcon) : "") + TextUtil.fixName(name), message, rights);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				opcode = -1;
				return true;

			case 217: // ActionSender sendGlobalChat() yell message
				try {
					String name = inStream.readString();
					String message = inStream.readString();
					int rights = inStream.readUnsignedByte();
					int chatIcon = inStream.readUnsignedByte();
					boolean ignore = false;
					long nameeLong = TextUtil.longForName(name);
					for (int l29 = 0; l29 < ignoreCount; l29++) {
						if (ignoreListAsLongs[l29] != nameeLong)
							continue;
						ignore = true;

					}
					if (!ignore) {
						pushMessage(9, globalPrefix,
								(chatIcon > 0 ? iconSymbol(chatIcon) : "") + TextUtil.fixName(name), message, rights);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				opcode = -1;
				return true;

			case 212: // update chat channel names
				globalPrefix = inStream.readString();
				opcode = -1;
				return true;

			case 107: // ActionSender resetCamera() & sendPacket107()
				cutSceneCamera = false;
				cutSceneHideRoofs = false;
				for (int l = 0; l < 5; l++)
					aBooleanArray876[l] = false;
				opcode = -1;
				return true;

			case 108: // ActionSender hideCameraRoof()
				cutSceneHideRoofs = (inStream.readUnsignedByte() == 1);
				opcode = -1;
				return true;

			case 72: // Clears an interface's inventory
				int sciInterfaceID = inStream.method434();
				int sciChildID = inStream.method434();
				RSInterface sciInterface = RSInterface.getInterface(sciInterfaceID, sciChildID);
				if (sciInterface != null) {
					for (int k15 = 0; k15 < sciInterface.inventoryItemIds.length; k15++) {
						sciInterface.inventoryItemIds[k15] = 0;
						sciInterface.inventoryStackSizes[k15] = 0;
					}
				}
				opcode = -1;
				return true;

			case 213: // ActionSender sendClanList() sends the list of members in a clan chat
				clanCount = inStream.readUnsignedByte();
				if (clanCount > 0) {
					for (int j1 = 0; j1 < clanCount; j1++) {
						clanListAsLongs[j1] = inStream.readQWord();
						clanListRights[j1] = inStream.readUnsignedByte();
					}
				}
				opcode = -1;
				return true;

			case 214: // ActionSender sendIgnoreList()
				ignoreCount = packetSize / 8;
				if (ignoreCount > 0) {
					for (int j1 = 0; j1 < ignoreCount; j1++) {
						ignoreListAsLongs[j1] = inStream.readQWord();
					}
				}
				opcode = -1;
				return true;

			case 166: // ActionSender moveCameraTo()
				cutSceneCamera = true;
				anInt1098 = inStream.readUnsignedByte();
				anInt1099 = inStream.readUnsignedByte();
				anInt1100 = inStream.readUnsignedShort();
				anInt1101 = inStream.readUnsignedByte();
				anInt1102 = inStream.readUnsignedByte();
				if (anInt1102 >= 100) {
					xCameraPos = anInt1098 * 128 + 64;
					yCameraPos = anInt1099 * 128 + 64;
					zCameraPos = method42(plane, yCameraPos, xCameraPos) - anInt1100;
				}
				opcode = -1;
				return true;

			case 134: // ActionSender sendSkill()
				redrawTab = true;
				int skill = inStream.readUnsignedByte();
				int xp = inStream.method439();
				int level = inStream.readUnsignedByte();

				if (loadingStage == 2) {
					int xpGained = xp - currentExp[skill];
					if (xpGained > 0 && currentExp[skill] >= 0) {
						XPDrop.addXPDrop(skill, xpGained);
					}
				}

				currentExp[skill] = xp;
				currentStats[skill] = level;
				maxStats[skill] = 1;
				for (int k20 = 0; k20 < 98; k20++)
					if (xp >= anIntArray1019[k20])
						maxStats[skill] = k20 + 2;
				opcode = -1;
				return true;

			case 71: // sendSidebarInterface()
				int l1 = inStream.readUnsignedShort();
				int j10 = inStream.method426();
				if (l1 == 65535)
					l1 = -1;
				tabInterfaceIDs[j10] = l1;
				redrawTab = true;
				redrawTabIcons = true;
				opcode = -1;
				return true;

			case 74: // ActionSender sendSong()
				int songID = inStream.method434();
				if (songID == 65535) {
					songID = -1;
				}
				if (!audioMuted) {
					if (songID != currentSong && musicEnabled && !lowMem && temporarySongDelay == 0) {
						nextSong = songID;
						songChanging = true;
						onDemandFetcher.request(2, nextSong);
					}
					currentSong = songID;
				}
				opcode = -1;
				return true;

			case 121: // ActionSender sendQuickSong()
				int songId = inStream.method436();
				int songDelay = inStream.method435();
				if (!audioMuted) {
					if (musicEnabled && !lowMem) {
						nextSong = songId;
						songChanging = false;
						onDemandFetcher.request(2, nextSong);
						temporarySongDelay = songDelay;
					}
				}
				opcode = -1;
				return true;

			case 109: // ActionSender sendLogout()
				resetLogout();
				opcode = -1;
				return false;

			case 70: // ActionSender moveInterface() & updateSpecialBar()
				int miInterfaceID = inStream.method434();
				int miChildId = inStream.method434();
				int miX = inStream.readSignedShort();
				int miY = inStream.readSignedShort();
				RSInterface miInterface = RSInterface.getInterface(miInterfaceID, miChildId);
				if (miInterface != null) {
					miInterface.xOffset = miX;
					miInterface.yOffset = miY;
				}
				opcode = -1;
				return true;

			case 73: // ActionSender sendMapRegion()
			case 241: // ActionSender sendCopyMapRegion()
				int regionx = anInt1069;
				int regiony = anInt1070;
				if (opcode == 73) {
					regionx = inStream.method435();
					regiony = inStream.readUnsignedShort();
					constructedViewport = false;
				}
				if (opcode == 241) {
					regiony = inStream.method435();
					inStream.initBitAccess();
					for (int j16 = 0; j16 < 4; j16++) {
						for (int l20 = 0; l20 < 13; l20++) {
							for (int j23 = 0; j23 < 13; j23++) {
								int i26 = inStream.readBits(1);
								if (i26 == 1)
									copyMapPalette[j16][l20][j23] = inStream.readBits(26);
								else
									copyMapPalette[j16][l20][j23] = -1;
							}
						}
					}
					inStream.finishBitAccess();
					regionx = inStream.readUnsignedShort();
					constructedViewport = true;
				}
				if (anInt1069 == regionx && anInt1070 == regiony && loadingStage == 2) {
					opcode = -1;
					return true;
				}
				anInt1069 = regionx;
				anInt1070 = regiony;
				baseX = (anInt1069 - 6) * 8;
				baseY = (anInt1070 - 6) * 8;
				aBoolean1141 = (anInt1069 / 8 == 48 || anInt1069 / 8 == 49) && anInt1070 / 8 == 48;
				if (anInt1069 / 8 == 48 && anInt1070 / 8 == 148)
					aBoolean1141 = true;
				loadingStage = 1;
				aLong824 = System.currentTimeMillis();
				gameScreenImageProducer.initDrawingArea();
				int loadXBox = frameMode == ScreenMode.FIXED ? 200 : (frameWidth / 2) - 55;
				int loadYBox = frameMode == ScreenMode.FIXED ? 150 : (frameHeight / 2) - 45;
				DrawingArea.fillPixels(loadXBox, 130, 22, 0xffffff, loadYBox);
				DrawingArea.drawPixels(20, loadYBox + 1, loadXBox + 1, 0, 128);
				regularFont.drawText(0, "Loading - please wait.", loadYBox + 18, loadXBox + 68);
				regularFont.drawText(0xffffff, "Loading - please wait.", loadYBox + 17, loadXBox + 67);
				gameScreenImageProducer.drawGraphics(frameMode == ScreenMode.FIXED ? 4 : 0, super.graphics,
						frameMode == ScreenMode.FIXED ? 4 : 0);
				if (opcode == 73) {
					int k16 = 0;
					for (int i21 = (anInt1069 - 6) / 8; i21 <= (anInt1069 + 6) / 8; i21++) {
						for (int k23 = (anInt1070 - 6) / 8; k23 <= (anInt1070 + 6) / 8; k23++)
							k16++;
					}
					floorMapBytes = new byte[k16][];
					objectMapBytes = new byte[k16][];
					regionIdArray = new int[k16];
					floorMapArray = new int[k16];
					objectMapArray = new int[k16];
					k16 = 0;
					for (int l23 = (anInt1069 - 6) / 8; l23 <= (anInt1069 + 6) / 8; l23++) {
						for (int j26 = (anInt1070 - 6) / 8; j26 <= (anInt1070 + 6) / 8; j26++) {
							regionIdArray[k16] = (l23 << 8) + j26;
							if (aBoolean1141
									&& (j26 == 49 || j26 == 149 || j26 == 147 || l23 == 50 || l23 == 49 && j26 == 47)) {
								floorMapArray[k16] = -1;
								objectMapArray[k16] = -1;
								k16++;
							} else {
								int k28 = floorMapArray[k16] = onDemandFetcher.getMapId(0, j26, l23);
								if (k28 != -1)
									onDemandFetcher.request(3, k28);
								int j30 = objectMapArray[k16] = onDemandFetcher.getMapId(1, j26, l23);
								if (j30 != -1)
									onDemandFetcher.request(3, j30);
								k16++;
							}
						}
					}
				}
				if (opcode == 241) { // Constructs a dynamic map region using a palette of 8*8 tile
					int regionCount = 0;
					int ai[] = new int[676];
					for (int z = 0; z < 4; z++) {
						for (int x = 0; x < 13; x++) {
							for (int y = 0; y < 13; y++) {
								int paletteData = copyMapPalette[z][x][y];
								if (paletteData != -1) {
									int tileX = paletteData >> 14 & 0x3ff;
									int tileY = paletteData >> 3 & 0x7ff;
									int regionId = (tileX / 8 << 8) + tileY / 8;
									for (int j33 = 0; j33 < regionCount; j33++) {
										if (ai[j33] != regionId)
											continue;
										regionId = -1;
									}
									if (regionId != -1)
										ai[regionCount++] = regionId;
								}
							}
						}
					}
					floorMapBytes = new byte[regionCount][];
					objectMapBytes = new byte[regionCount][];
					regionIdArray = new int[regionCount];
					floorMapArray = new int[regionCount];
					objectMapArray = new int[regionCount];
					for (int l26 = 0; l26 < regionCount; l26++) {
						int regionId = regionIdArray[l26] = ai[l26];
						int regionX = regionId >> 8 & 0xff;
						int regionY = regionId & 0xff;
						int floorMapData = floorMapArray[l26] = onDemandFetcher.getMapId(0, regionY, regionX);
						if (floorMapData != -1)
							onDemandFetcher.request(3, floorMapData);
						int objectMapData = objectMapArray[l26] = onDemandFetcher.getMapId(1, regionY, regionX);
						if (objectMapData != -1)
							onDemandFetcher.request(3, objectMapData);
					}
				}
				int i17 = baseX - anInt1036;
				int j21 = baseY - anInt1037;
				anInt1036 = baseX;
				anInt1037 = baseY;
				for (int j24 = 0; j24 < 16384; j24++) {
					NPC npc = npcArray[j24];
					if (npc != null) {
						for (int j29 = 0; j29 < 10; j29++) {
							npc.smallX[j29] -= i17;
							npc.smallY[j29] -= j21;
						}
						npc.x -= i17 * 128;
						npc.y -= j21 * 128;
					}
				}
				for (int i27 = 0; i27 < maxPlayers; i27++) {
					Player player = playerArray[i27];
					if (player != null) {
						for (int i31 = 0; i31 < 10; i31++) {
							player.smallX[i31] -= i17;
							player.smallY[i31] -= j21;
						}
						player.x -= i17 * 128;
						player.y -= j21 * 128;
					}
				}
				aBoolean1080 = true;
				byte byte1 = 0;
				byte byte2 = 104;
				byte byte3 = 1;
				if (i17 < 0) {
					byte1 = 103;
					byte2 = -1;
					byte3 = -1;
				}
				byte byte4 = 0;
				byte byte5 = 104;
				byte byte6 = 1;
				if (j21 < 0) {
					byte4 = 103;
					byte5 = -1;
					byte6 = -1;
				}
				for (int k33 = byte1; k33 != byte2; k33 += byte3) {
					for (int l33 = byte4; l33 != byte5; l33 += byte6) {
						int i34 = k33 + i17;
						int j34 = l33 + j21;
						for (int k34 = 0; k34 < 4; k34++)
							if (i34 >= 0 && j34 >= 0 && i34 < 104 && j34 < 104)
								groundArray[k34][k33][l33] = groundArray[k34][i34][j34];
							else
								groundArray[k34][k33][l33] = null;
					}
				}
				for (SpawnedObject class30_sub1_1 = (SpawnedObject) aClass19_1179
						.reverseGetFirst(); class30_sub1_1 != null; class30_sub1_1 = (SpawnedObject) aClass19_1179
								.reverseGetNext()) {
					class30_sub1_1.anInt1297 -= i17;
					class30_sub1_1.anInt1298 -= j21;
					if (class30_sub1_1.anInt1297 < 0 || class30_sub1_1.anInt1298 < 0 || class30_sub1_1.anInt1297 >= 104
							|| class30_sub1_1.anInt1298 >= 104)
						class30_sub1_1.unlink();
				}
				if (destX != 0) {
					destX -= i17;
					destY -= j21;
				}
				cutSceneCamera = false;
				cutSceneHideRoofs = false;
				opcode = -1;
				return true;

			case 208: // ActionSender sendWalkableInterface()
				int i3 = inStream.method437();
				if (i3 >= 0) {
					resetInterfaceAnimation(i3);
					walkableInterfaceMode = true;
				} else
					walkableInterfaceMode = false;
				walkableInterfaceID = i3;
				resetFade();
				setTint(0, 0);
				opcode = -1;
				return true;

			case 99: // ActionSender sendMapState()
				anInt1021 = inStream.readUnsignedByte();
				opcode = -1;
				return true;

			case 75: // ActionSender sendNPCDialogueHead()
				int npcDHInterfaceID = inStream.method436();
				int npcDHChildID = inStream.method436();
				int npcDHMedia = inStream.method436();
				RSInterface npcDHInterface = RSInterface.getInterface(npcDHInterfaceID, npcDHChildID);
				if (npcDHInterface != null) {
					npcDHInterface.defaultMediaType = 2;
					npcDHInterface.defaultMedia = npcDHMedia;
				}
				opcode = -1;
				return true;

			case 114: // ActionSender sendUpdateServer()
				systemUpdateTime = inStream.method434() * 30;
				opcode = -1;
				return true;

			case 115: // Server wide msg, aka ::servermsg
				int worldMessageLength = inStream.readUnsignedShort();
				if (worldMessageLength > 0) {
					try {
						serverMessage = inStream.readString();
					} catch (Exception exception1) {
						Signlink.reportError("cde1");
					}
				} else {
					serverMessage = "";
				}
				opcode = -1;
				return true;

			case 60: // coordinate something something dark side UNUSED
				tempPositionY = inStream.readUnsignedByte();
				tempPositionX = inStream.method427();
				while (inStream.currentOffset < packetSize) {
					int k3 = inStream.readUnsignedByte();
					parseRegionPackets(inStream, k3);
				}
				opcode = -1;
				return true;

			case 35: // ActionSender shakeScreen()
				int l3 = inStream.readUnsignedByte();
				int k11 = inStream.readUnsignedByte();
				int j17 = inStream.readUnsignedByte();
				int k21 = inStream.readUnsignedByte();
				aBooleanArray876[l3] = true;
				anIntArray873[l3] = k11;
				anIntArray1203[l3] = j17;
				anIntArray928[l3] = k21;
				anIntArray1030[l3] = 0;
				opcode = -1;
				return true;

			case 174: // ActionSender sendSound()
				int i4 = inStream.readUnsignedShort();
				int l11 = inStream.readUnsignedByte();
				int k17 = inStream.readUnsignedShort();
				if (soundEnabled && !lowMem && currentSound < 50) {
					playSound(i4, l11, k17);
				}
				opcode = -1;
				return true;

			case 104: // ActionSender sendPlayerOption()
				int j4 = inStream.method427();
				int i12 = inStream.method426();
				String s6 = inStream.readString();
				if (j4 >= 1 && j4 <= 5) {
					if (s6.equalsIgnoreCase("null"))
						s6 = null;
					atPlayerActions[j4 - 1] = s6;
					atPlayerArray[j4 - 1] = i12 == 0;
				}
				opcode = -1;
				return true;

			case 78: // reset walking? Clears the minimap flag from the minimap?
				destX = 0;
				opcode = -1;
				return true;

			case 253: // ActionSender sendMessage()
				String s = inStream.readString();
				boolean isFiltered = inStream.readUnsignedByte() == 1;
				if (s.endsWith(":tradereq:")) {
					String s3 = s.substring(0, s.indexOf(":"));
					long l17 = TextUtil.longForName(s3);
					boolean flag2 = false;
					for (int j27 = 0; j27 < ignoreCount; j27++) {
						if (ignoreListAsLongs[j27] != l17)
							continue;
						flag2 = true;

					}
					if (!flag2 && onTutorialIsland == 0) {
						pushMessage("wishes to trade with you.", 4, s3);
					}
				} else if (s.endsWith(":duelreq:")) {
					String s4 = s.substring(0, s.indexOf(":"));
					long l18 = TextUtil.longForName(s4);
					boolean flag3 = false;
					for (int k27 = 0; k27 < ignoreCount; k27++) {
						if (ignoreListAsLongs[k27] != l18)
							continue;
						flag3 = true;

					}
					if (!flag3 && onTutorialIsland == 0) {
						pushMessage("wishes to duel with you.", 8, s4);
					}
				} else if (s.endsWith(":chalreq:")) {
					String s5 = s.substring(0, s.indexOf(":"));
					long l19 = TextUtil.longForName(s5);
					boolean flag4 = false;
					for (int l27 = 0; l27 < ignoreCount; l27++) {
						if (ignoreListAsLongs[l27] != l19)
							continue;
						flag4 = true;

					}
					if (!flag4 && onTutorialIsland == 0) {
						String s8 = s.substring(s.indexOf(":") + 1, s.length() - 9);
						pushMessage(s8, 8, s5);
					}
				} else if (s.contains("<url=") && s.endsWith("</url>")) {
					String text = s.substring(0, s.indexOf("<url="));
					s = s.substring(text.length() + 5).trim();
					String link = s.substring(0, s.indexOf(">"));
					s = s.substring(link.length() + 1).trim();
					String urlDisplay = s.substring(0, s.indexOf("</url>"));
					pushMessage(12, urlDisplay, link, text, 0);
				} else {
					pushMessage(s, 0, "", isFiltered);
				}
				opcode = -1;
				return true;

			case 1: // reset all animations in the area UNUSED
				for (int k4 = 0; k4 < playerArray.length; k4++)
					if (playerArray[k4] != null)
						playerArray[k4].anim = -1;
				for (int j12 = 0; j12 < npcArray.length; j12++)
					if (npcArray[j12] != null)
						npcArray[j12].anim = -1;
				opcode = -1;
				return true;

			case 50: // ActionSender sendFriendList()
				long l4 = inStream.readQWord();
				int i18 = inStream.readUnsignedByte();
				String s7 = TextUtil.fixName(TextUtil.nameForLong(l4));
				for (int k24 = 0; k24 < friendsCount; k24++) {
					if (l4 != friendsListAsLongs[k24])
						continue;
					if (friendsNodeIDs[k24] != i18) {
						friendsNodeIDs[k24] = i18;
						redrawTab = true;
						if (i18 >= 2) {
							pushMessage(s7 + " has logged in.", 5, "");
						}
						if (i18 <= 1) {
							pushMessage(s7 + " has logged out.", 5, "");
						}
					}
					s7 = null;

				}
				if (s7 != null && friendsCount < 200) {
					friendsListAsLongs[friendsCount] = l4;
					friendsList[friendsCount] = s7;
					friendsNodeIDs[friendsCount] = i18;
					friendsCount++;
					redrawTab = true;
				}
				for (boolean flag6 = false; !flag6;) {
					flag6 = true;
					for (int k29 = 0; k29 < friendsCount - 1; k29++)
						if (friendsNodeIDs[k29] != nodeID && friendsNodeIDs[k29 + 1] == nodeID
								|| friendsNodeIDs[k29] == 0 && friendsNodeIDs[k29 + 1] != 0) {
							int j31 = friendsNodeIDs[k29];
							friendsNodeIDs[k29] = friendsNodeIDs[k29 + 1];
							friendsNodeIDs[k29 + 1] = j31;
							String s10 = friendsList[k29];
							friendsList[k29] = friendsList[k29 + 1];
							friendsList[k29 + 1] = s10;
							long l32 = friendsListAsLongs[k29];
							friendsListAsLongs[k29] = friendsListAsLongs[k29 + 1];
							friendsListAsLongs[k29 + 1] = l32;
							redrawTab = true;
							flag6 = false;
						}
				}
				opcode = -1;
				return true;

			case 110: // ActionSender sendEnergy()
				if (tabID == 12) {
					redrawTab = true;
				}
				energy = inStream.readUnsignedByte();
				opcode = -1;
				return true;

			case 254: // ActionSender createObjectHints() & createPlayerHints()
				anInt855 = inStream.readUnsignedByte();
				if (anInt855 == 1)
					anInt1222 = inStream.readUnsignedShort();
				if (anInt855 >= 2 && anInt855 <= 6) {
					if (anInt855 == 2) {
						anInt937 = 64;
						anInt938 = 64;
					}
					if (anInt855 == 3) {
						anInt937 = 0;
						anInt938 = 64;
					}
					if (anInt855 == 4) {
						anInt937 = 128;
						anInt938 = 64;
					}
					if (anInt855 == 5) {
						anInt937 = 64;
						anInt938 = 0;
					}
					if (anInt855 == 6) {
						anInt937 = 64;
						anInt938 = 128;
					}
					anInt855 = 2;
					anInt934 = inStream.readUnsignedShort();
					anInt935 = inStream.readUnsignedShort();
					anInt936 = inStream.readUnsignedByte();
				}
				if (anInt855 == 10)
					anInt933 = inStream.readUnsignedShort();
				opcode = -1;
				return true;

			case 248: // ActionSender sendInterface() with inventoryId argument / Displays an item
						// model inside an interface
				int i5 = inStream.method435();
				int k12 = inStream.readUnsignedShort();
				if (backDialogID != -1) {
					backDialogID = -1;
					redrawChatbox = true;
				}
				if (inputDialogState != 0) {
					inputDialogState = 0;
					redrawChatbox = true;
				}
				if (!showTabComponents) {
					showTabComponents = true;
				}
				openInterfaceID = i5;
				resetFade();
				setTint(0, 0);
				invOverlayInterfaceID = k12;
				redrawTab = true;
				redrawTabIcons = true;
				continueDialogue = false;
				opcode = -1;
				return true;

			case 79: // ActionSender sendScrollInterface() / Sets the scrollbar position of an
						// interface
				int siInterfaceID = inStream.method434();
				int siChildID = inStream.method434();
				int siScrollPosition = inStream.method435();
				RSInterface siInterface = RSInterface.getInterface(siInterfaceID, siChildID);
				if (siInterface != null) {
					if (siInterface.type == 0) {
						if (siScrollPosition < 0)
							siScrollPosition = 0;
						if (siScrollPosition > siInterface.scrollMax - siInterface.height)
							siScrollPosition = siInterface.scrollMax - siInterface.height;
						siInterface.scrollPosition = siScrollPosition;
					}
				}
				opcode = -1;
				return true;

			case 68:// reset all button states UNUSED
				for (int k5 = 0; k5 < variousSettings.length; k5++)
					if (variousSettings[k5] != anIntArray1045[k5]) {
						variousSettings[k5] = anIntArray1045[k5];
						doConfigAction(k5);
						redrawTab = true;
					}
				opcode = -1;
				return true;

			case 196: // ActionSender sendPrivateMessage() pm whisper message
				long pmSenderLong = inStream.readQWord();
				int j18 = inStream.readDWord();
				int rights = inStream.readUnsignedByte();
				int chatIcon = inStream.readUnsignedByte();
				boolean ignorePm = false;
				for (int l29 = 0; l29 < ignoreCount; l29++) {
					if (ignoreListAsLongs[l29] != pmSenderLong) {
						continue;
					}
					ignorePm = true;
				}
				if (!ignorePm && onTutorialIsland == 0) {
					try {
						String s9 = TextInput.method525(packetSize - 14, inStream);
						String chatIconString = chatIcon > 0 ? iconSymbol(chatIcon) : "";
						if (rights >= 1 && rights <= 3) {
							pushMessage(s9, 7, chatIconString + TextUtil.fixName(TextUtil.nameForLong(pmSenderLong)),
									rights);
						} else {
							pushMessage(s9, 3, chatIconString + TextUtil.fixName(TextUtil.nameForLong(pmSenderLong)),
									rights);
						}
						/*
						 * if(!gameHasFocus) { //&& pmPingSound playSound("pm"); }
						 */
					} catch (Exception exception1) {
						Signlink.reportError("cde1");
					}
				}
				opcode = -1;
				return true;

			case 85: // ActionSender sendCoords() & sendCoordinates2()
				tempPositionY = inStream.method427();
				tempPositionX = inStream.method427();
				opcode = -1;
				return true;

			case 24: // ActionSender flashSideBarIcon()
				flashingSidebar = inStream.method428();
				if (flashingSidebar == tabID) {
					if (flashingSidebar == 3) {
						tabID = 1;
					} else {
						tabID = 3;
					}
					redrawTab = true;
				}
				opcode = -1;
				return true;

			case 246: // ActionSender sendItemOnInterface()
				int ioiInterfaceID = inStream.method434();
				int ioiChildId = inStream.method434();
				int ioiItem = inStream.readUnsignedShort();
				int ioiZoom = inStream.readUnsignedShort();
				RSInterface ioiInterface = RSInterface.getInterface(ioiInterfaceID, ioiChildId);
				if (ioiInterface != null) {
					if (ioiItem == 65535) {
						ioiInterface.defaultMediaType = 0;
					} else {
						ItemDef itemDef = ItemDef.forID(ioiItem);
						ioiInterface.defaultMediaType = 4;
						ioiInterface.defaultMedia = ioiItem;
						ioiInterface.modelRotationX = itemDef.modelRotationX;
						ioiInterface.modelRotationY = itemDef.modelRotationY;
						ioiInterface.modelZoom = (itemDef.modelZoom * 100) / ioiZoom;
					}
				}
				opcode = -1;
				return true;

			case 171: // ActionSender sendInterfaceHidden()
				int ihInterfaceID = inStream.readUnsignedShort();
				int ihChildId = inStream.readUnsignedShort();
				boolean ihFlag = inStream.readUnsignedByte() == 1;
				RSInterface ihInterface = RSInterface.getInterface(ihInterfaceID, ihChildId);
				if (ihInterface != null) {
					ihInterface.isHidden = ihFlag;
				}
				opcode = -1;
				return true;

			case 142: // open inventory? UNUSED
				int j6 = inStream.method434();
				resetInterfaceAnimation(j6);
				if (backDialogID != -1) {
					backDialogID = -1;
					redrawChatbox = true;
				}
				if (inputDialogState != 0) {
					inputDialogState = 0;
					redrawChatbox = true;
				}
				invOverlayInterfaceID = j6;
				redrawTabIcons = true;
				redrawTab = true;
				openInterfaceID = -1;
				continueDialogue = false;
				opcode = -1;
				return true;

			case 126: // ActionSender sendString() & sendQuestLogString() / Attaches text to an
						// interface
				try {
					int ssInterfaceID = inStream.method435();
					int ssChildID = inStream.method435();
					String text = inStream.readString();
					sendString(text, ssInterfaceID, ssChildID);
				} catch (Exception e) {
				}
				opcode = -1;
				return true;

			case 206: // chat settings / Sends the chat privacy settings
				publicChatMode = inStream.readUnsignedByte();
				privateChatMode = inStream.readUnsignedByte();
				tradeMode = inStream.readUnsignedByte();
				redrawChatbox = true;
				opcode = -1;
				return true;

			case 240: // ActionSender sendWeight()
				if (tabID == 12) {
					redrawTab = true;
				}
				weight = inStream.readSignedShort();
				opcode = -1;
				return true;

			case 8: // ActionSender sendComponentInterface()
				int scInterfaceID = inStream.method436();
				int scChildID = inStream.method436();
				int scComponentID = inStream.readUnsignedShort();
				RSInterface scInterface = RSInterface.getInterface(scInterfaceID, scChildID);
				if (scInterface != null) {
					scInterface.defaultMediaType = 1;
					scInterface.defaultMedia = scComponentID;
				}
				opcode = -1;
				return true;

			case 122: // ActionSender sendStringColor()
				int sscInterfaceID = inStream.method436();
				int sscChildID = inStream.method436();
				int sscColor = inStream.method436();
				int sscR = sscColor >> 10 & 0x1f;
				int sscG = sscColor >> 5 & 0x1f;
				int sscB = sscColor & 0x1f;
				RSInterface sscInterface = RSInterface.getInterface(sscInterfaceID, sscChildID);
				if (sscInterface != null) {
					sscInterface.disabledColor = (sscR << 19) + (sscG << 11) + (sscB << 3);
				}
				opcode = -1;
				return true;

			case 53: // ActionSender sendUpdateItems()
				redrawTab = true;
				int uisInterfaceID = inStream.method434();
				int uisChildID = inStream.method434();
				RSInterface uisInterface = RSInterface.getInterface(uisInterfaceID, uisChildID);
				if (uisInterface != null) {
					int j19 = inStream.readUnsignedShort();
					for (int j22 = 0; j22 < j19; j22++) {
						int i25 = inStream.readUnsignedByte();
						if (i25 == 255)
							i25 = inStream.method440();
						uisInterface.inventoryItemIds[j22] = inStream.method436();
						uisInterface.inventoryStackSizes[j22] = i25;
					}
					for (int j25 = j19; j25 < uisInterface.inventoryItemIds.length; j25++) {
						uisInterface.inventoryItemIds[j25] = 0;
						uisInterface.inventoryStackSizes[j25] = 0;
					}
				}
				opcode = -1;
				return true;

			case 230: // ActionSender sendInterfaceRotation()
				int sirInterfaceID = inStream.method434();
				int sirChildID = inStream.method434();
				int sirRotX = inStream.readUnsignedShort();
				int sirRotY = inStream.readUnsignedShort();
				int sirZoom = inStream.readUnsignedShort();
				RSInterface sirInterface = RSInterface.getInterface(sirInterfaceID, sirChildID);
				if (sirInterface != null) {
					sirInterface.modelRotationX = sirRotX;
					sirInterface.modelRotationY = sirRotY;
					sirInterface.modelZoom = sirZoom;
				}
				opcode = -1;
				return true;

			case 221: // ActionSender sendPMServer()
				anInt900 = inStream.readUnsignedByte();
				redrawTab = true;
				opcode = -1;
				return true;

			case 177: // ActionSender turnCameraTo()
				cutSceneCamera = true;
				anInt995 = inStream.readUnsignedByte();
				anInt996 = inStream.readUnsignedByte();
				anInt997 = inStream.readUnsignedShort();
				anInt998 = inStream.readUnsignedByte();
				anInt999 = inStream.readUnsignedByte();
				if (anInt999 >= 100) {
					int k7 = anInt995 * 128 + 64;
					int k14 = anInt996 * 128 + 64;
					int i20 = method42(plane, k14, k7) - anInt997;
					int l22 = k7 - xCameraPos;
					int k25 = i20 - zCameraPos;
					int j28 = k14 - yCameraPos;
					int i30 = (int) Math.sqrt(l22 * l22 + j28 * j28);
					yCameraCurve = (int) (Math.atan2(k25, i30) * 325.94900000000001D) & 0x7ff;
					xCameraCurve = (int) (Math.atan2(l22, j28) * -325.94900000000001D) & 0x7ff;
					if (yCameraCurve < 128)
						yCameraCurve = 128;
					if (yCameraCurve > 383)
						yCameraCurve = 383;
				}
				opcode = -1;
				return true;

			case 249: // ActionSender sendDetails()
				anInt1046 = inStream.method426();
				localPlayerIndex = inStream.method436();
				opcode = -1;
				return true;

			case 65: // NpcUpdating update()
				updateNPCs(inStream, packetSize);
				opcode = -1;
				return true;

			case 27: // ActionSender openXInterface() & ChatInterfacePacketHandler showEnterX()
				boolean xTitle = inStream.readUnsignedByte() == 1;
				if (xTitle) {
					amountOrNameTitle = inStream.readString();
				} else {
					amountOrNameTitle = "Enter Amount";
				}
				messagePromptRaised = false;
				inputDialogState = 1;
				amountOrNameInput = "";
				redrawChatbox = true;
				opcode = -1;
				return true;

			case 187: // ActionSender openNameInterface() ChatInterfacePacketHandler
				boolean nameTilt = inStream.readUnsignedByte() == 1;
				if (nameTilt) {
					amountOrNameTitle = inStream.readString();
				} else {
					amountOrNameTitle = "Enter Name";
				}
				messagePromptRaised = false;
				inputDialogState = 2;
				amountOrNameInput = "";
				redrawChatbox = true;
				opcode = -1;
				return true;

			case 97: // ActionSender sendInterface()
				int l7 = inStream.readUnsignedShort();
				resetInterfaceAnimation(l7);
				if (invOverlayInterfaceID != -1) {
					redrawTab = true;
					invOverlayInterfaceID = -1;
					redrawTabIcons = true;
				}
				if (backDialogID != -1) {
					backDialogID = -1;
					redrawChatbox = true;
				}
				if (inputDialogState != 0) {
					inputDialogState = 0;
					redrawChatbox = true;
				}
				openInterfaceID = l7;
				continueDialogue = false;
				resetFade();
				setTint(0, 0);
				opcode = -1;
				return true;

			case 218: // ActionSender sendChatboxOverlay()
				int i8 = inStream.method438();
				dialogID = i8;
				redrawChatbox = true;
				opcode = -1;
				return true;

			case 87: // config of somesort UNUSED
				int j8 = inStream.method434();
				int l14 = inStream.method439();
				anIntArray1045[j8] = l14;
				if (variousSettings[j8] != l14) {
					variousSettings[j8] = l14;
					doConfigAction(j8);
					redrawTab = true;
					if (dialogID != -1)
						redrawChatbox = true;
				}
				opcode = -1;
				return true;

			case 36: // ActionSender sendConfig()
				int k8 = inStream.method434();
				byte byte0 = inStream.readSignedByte();
				anIntArray1045[k8] = byte0;
				if (variousSettings[k8] != byte0) {
					variousSettings[k8] = byte0;
					doConfigAction(k8);
					redrawTab = true;
					if (dialogID != -1)
						redrawChatbox = true;
				}
				opcode = -1;
				return true;

			case 61: // ActionSender sendMultipleInterface()
				anInt1055 = inStream.readUnsignedByte();
				opcode = -1;
				return true;

			case 200: // ActionSender sendInterfaceAnimation()
				int animInterfaceID = inStream.readUnsignedShort();
				int animChildID = inStream.readUnsignedShort();
				int animID = inStream.readSignedShort();
				RSInterface animInterface = RSInterface.getInterface(animInterfaceID, animChildID);
				if (animInterface != null) {
					animInterface.defaultAnimationId = animID;
					animInterface.anInt246 = 0;
					animInterface.anInt208 = 0;
				}
				opcode = -1;
				return true;

			case 219: // ActionSender removeInterfaces()
				if (invOverlayInterfaceID != -1) {
					invOverlayInterfaceID = -1;
					redrawTab = true;
					redrawTabIcons = true;
				}
				if (backDialogID != -1) {
					backDialogID = -1;
					redrawChatbox = true;
				}
				if (inputDialogState != 0) {
					inputDialogState = 0;
					redrawChatbox = true;
				}
				if (doFade) {
					resetFade();
				}
				openInterfaceID = -1;
				continueDialogue = false;
				opcode = -1;
				return true;

			case 34: // ActionSender sendUpdateItem() & sendDuelEquipment()
				redrawTab = true;
				int suiInterfaceID = inStream.method434();
				int suiChildID = inStream.method434();
				RSInterface suiInterface = RSInterface.getInterface(suiInterfaceID, suiChildID);
				if (suiInterface != null) {
					while (inStream.currentOffset < packetSize) {
						int slot = inStream.readUnsignedShort();
						int i23 = inStream.readUnsignedShort();
						int l25 = inStream.readUnsignedByte();
						if (l25 == 255)
							l25 = inStream.readDWord();
						if (slot >= 0 && slot < suiInterface.inventoryItemIds.length) {
							suiInterface.inventoryItemIds[slot] = i23;
							suiInterface.inventoryStackSizes[slot] = l25;
						}
					}
				}
				opcode = -1;
				return true;

			case 4: // ActionSender sendStillGraphic()
			case 44: // ActionSender sendGroundItem()
			case 84: // modify ground item amount UNUSED
			case 101: // ActionSender sendObjectType()
			case 105: // play sound at location UNUSED
			case 117: // ActionSender sendProjectile()
			case 147: // unknown and UNUSED
			case 151: // object spawn / creation ActionSender sendObject() & sendPlayerObject()
			case 156: // ActionSender removeGroundItem()
			case 160: // object animation ActionSender animateObject() & animateObject2()
			case 215: // unknown and UNUSED
				parseRegionPackets(inStream, opcode);
				opcode = -1;
				return true;

			case 106: // ActionSender sendFrame106()
				tabID = inStream.method427();
				redrawTab = true;
				redrawTabIcons = true;
				opcode = -1;
				return true;

			case 164: // ActionSender sendChatInterface()
				int interfaceIDChat = inStream.method434();
				resetInterfaceAnimation(interfaceIDChat);
				if (invOverlayInterfaceID != -1) {
					redrawTab = true;
					invOverlayInterfaceID = -1;
					redrawTabIcons = true;
				}
				backDialogID = interfaceIDChat;
				redrawChatbox = true;
				openInterfaceID = -1;
				continueDialogue = false;
				opcode = -1;
				return true;

			case 180: // ActionSender updateBank
				selectedBankTab = inStream.readUnsignedShort();
				bankTabsUsed = inStream.readUnsignedShort();
				updateBankInterface(selectedBankTab, bankTabsUsed);
				opcode = -1;
				return true;
			/*
			 * case 69: //ActionSender sendFullscreenInterface() UNUSED :( return true;
			 */
			/*
			 * case 152: //ActionSender updateFlashingSideIcon() UNUSED return true;
			 */

			}
			Signlink.reportError(
					"T1 - " + opcode + "," + packetSize + " - " + secondLastOpcode + "," + thirdLastOpcode);
		} catch (IOException _ex) {
			dropClient();
		} catch (Exception exception) {
			String s2 = "T2 - " + opcode + "," + secondLastOpcode + "," + thirdLastOpcode + " - " + packetSize + ","
					+ (baseX + myPlayer.smallX[0]) + "," + (baseY + myPlayer.smallY[0]) + " - ";
			for (int j15 = 0; j15 < packetSize && j15 < 50; j15++)
				s2 = s2 + inStream.buffer[j15] + ",";
			Signlink.reportError(s2);
		}
		opcode = -1;
		return true;
	}

	public static int log_view_dist = 9;

	private void drawGameWorld() { // method 146
		try {
			anInt1265++;
			showOtherPlayers(true);
			showNPCs(true);
			showOtherPlayers(false);
			showNPCs(false);
			createProjectiles();
			createStationaryGraphics();
			if (!cutSceneCamera) {
				int i = anInt1184;
				if (anInt984 / 256 > i)
					i = anInt984 / 256;
				if (aBooleanArray876[4] && anIntArray1203[4] + 128 > i)
					i = anIntArray1203[4] + 128;
				int k = cameraHorizontal + viewRotationOffset & 0x7ff;
				setCameraPos(cameraZoom + i * (frameMode == ScreenMode.FIXED ? cameraPos1 : 5), i, anInt1014,
						method42(plane, myPlayer.y, myPlayer.x) - 50, k, anInt1015);
			}
			int j;
			if (!cutSceneCamera)
				j = setCameraLocation();
			else
				j = resetCameraHeight();
			int l = xCameraPos;
			int i1 = zCameraPos;
			int j1 = yCameraPos;
			int k1 = yCameraCurve;
			int l1 = xCameraCurve;
			for (int i2 = 0; i2 < 5; i2++)
				if (aBooleanArray876[i2]) {
					int j2 = (int) ((Math.random() * (double) (anIntArray873[i2] * 2 + 1) - (double) anIntArray873[i2])
							+ Math.sin((double) anIntArray1030[i2] * ((double) anIntArray928[i2] / 100D))
									* (double) anIntArray1203[i2]);
					if (i2 == 0)
						xCameraPos += j2;
					if (i2 == 1)
						zCameraPos += j2;
					if (i2 == 2)
						yCameraPos += j2;
					if (i2 == 3)
						xCameraCurve = xCameraCurve + j2 & 0x7ff;
					if (i2 == 4) {
						yCameraCurve += j2;
						if (yCameraCurve < 128)
							yCameraCurve = 128;
						if (yCameraCurve > 383)
							yCameraCurve = 383;
					}
				}
			int k2 = Texture.anInt1481;
			Model.aBoolean1684 = true;
			Model.anInt1687 = 0;
			Model.anInt1685 = super.mouseX - 4;
			Model.anInt1686 = super.mouseY - 4;
			DrawingArea.setAllPixelsToZero();
			// DrawingArea.method336(clientHeight, 0, 0, 0xC8C0A8, clientWidth);
			worldController.method313(xCameraPos, yCameraPos, xCameraCurve, zCameraPos, j, yCameraCurve);
			worldController.clearObj5Cache();
			// handleLighting();
			updateEntities();
			drawHeadIcon();
			writeBackgroundTexture(k2);
			if (showItemNames)
			{
				renderGroundItemNames();
			}
			draw3dScreen();
			if (frameMode != ScreenMode.FIXED) {
				drawGameframe();
			}
			gameScreenImageProducer.drawGraphics(frameMode == ScreenMode.FIXED ? 4 : 0, super.graphics,
					frameMode == ScreenMode.FIXED ? 4 : 0);
			xCameraPos = l;
			zCameraPos = i1;
			yCameraPos = j1;
			yCameraCurve = k1;
			xCameraCurve = l1;
		} catch (RuntimeException runtimeexception) {
			Signlink.reportError("97263, " + runtimeexception.toString());
			throw new RuntimeException();
		}
	}

	public void clearTopInterfaces() {
		stream.createFrame(130);
		if (invOverlayInterfaceID != -1) {
			invOverlayInterfaceID = -1;
			redrawTab = true;
			continueDialogue = false;
			redrawTabIcons = true;
		}
		if (backDialogID != -1) {
			backDialogID = -1;
			redrawChatbox = true;
			continueDialogue = false;
		}
		openInterfaceID = -1;
		fullscreenInterfaceID = -1;
	}

	private void resetAllImageProducers() {
		if (super.fullGameScreen != null) {
			return;
		}
		aRSImageProducer_1166 = null;
		aRSImageProducer_1164 = null;
		tabImageProducer = null;
		gameScreenImageProducer = null;
		aRSImageProducer_1125 = null;
		aRSImageProducer_1107 = null;
		aRSImageProducer_1108 = null;
		aRSImageProducer_1109 = null;
		aRSImageProducer_1110 = null;
		aRSImageProducer_1111 = null;
		aRSImageProducer_1112 = null;
		aRSImageProducer_1113 = null;
		aRSImageProducer_1114 = null;
		aRSImageProducer_1115 = null;
		super.fullGameScreen = new RSImageProducer(765, 503);
		redrawGame = true;
	}

	private boolean isSearchingBank()
    {
        if (searchString != "" && searchString != null && searchString.length() > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

	private void launchURL(String url) {
		if (url == null) {
			return;
		}
		url = url.trim();
		if (url.isEmpty()) {
			return;
		}
		boolean desktopSupport = false;
		try {
			System.out.println("Opening page: " + url);
			desktopSupport = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
			if (desktopSupport) {
				Desktop.getDesktop().browse(new URI(url));
			} else {
				System.out
						.println("This OS does not support Java desktop browse, Attempting to launch legacy browsing");
				// if desktop support does not exist try legacy/os speicfic operations
				final String osName = System.getProperty("os.name");
				if (osName.startsWith("Mac OS")) { // non dynamic
					Runtime runtime = Runtime.getRuntime();
					String[] args = { "osascript", "-e", "open location \"" + url + "\"" };
					try {
						Process process = runtime.exec(args);
					} catch (IOException e) {
					}
				} else if (osName.startsWith("Windows")) {
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
				} else { // assume Unix or Linux
					String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape",
							"safari" };
					String browser = null;
					for (int count = 0; count < browsers.length && browser == null; count++) {
						if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0) {
							browser = browsers[count];
							break;
						}
					}
					if (browser == null) {
						throw new Exception("Could not find web browser");
					} else {
						Runtime.getRuntime().exec(new String[] { browser, url });
					}
				}
			}
		} catch (Exception e) {
			if (loggedIn) {
				pushMessage("Failed to open URL.", 0, "");
			} else {
				System.out.println("Failed to open URL.");
			}
		}
	}

	public Client() {
		if (!audioMuted) {
			try {
				midiPlayer = new MidiPlayer();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setFrameMode(ScreenMode.FIXED);
		if (ClientSettings.DevMode) {
		    //server = "127.0.0.1";
			// TODO: Change back for testing
			server = ClientSettings.SERVER_IP;
		} else {
			server = ClientSettings.SERVER_IP;
		}
		fullscreenInterfaceID = -1;
		chatRights = new int[messageDisplayLimit];
		chatFiltered = new boolean[messageDisplayLimit];
		chatTypeView = 0;
		clanChatMode = 0;
		globalMode = 0;
		gameMode = 0;
		cButtonHPos = -1;
		chatTypeIndex = 0;
		anIntArrayArray825 = new int[104][104];
		friendsNodeIDs = new int[200];
		groundArray = new NodeList[4][104][104];
		aBoolean831 = false;
		aStream_834 = new Stream(new byte[5000]);
		npcArray = new NPC[16384];
		npcIndices = new int[16384];
		anIntArray840 = new int[1000];
		aStream_847 = Stream.create();
		soundEnabled = true;
		openInterfaceID = -1;
		currentExp = new int[Skills.skillsCount];
		aBoolean872 = false;
		anIntArray873 = new int[5];
		aBooleanArray876 = new boolean[5];
		reportAbuseInput = "";
		localPlayerIndex = -1;
		menuOpen = false;
		inputString = "";
		maxPlayers = 2048;
		myPlayerIndex = 2047;
		playerArray = new Player[maxPlayers];
		playerIndices = new int[maxPlayers];
		npcsAwaitingUpdate = new int[maxPlayers];
		aStreamArray895s = new Stream[maxPlayers];
		anIntArrayArray901 = new int[104][104];
		aByteArray912 = new byte[16384];
		currentStats = new int[Skills.skillsCount];
		clanListAsLongs = new long[100];
		clanListRights = new int[100];
		ignoreListAsLongs = new long[100];
		loadingError = false;
		anIntArray928 = new int[5];
		anIntArrayArray929 = new int[104][104];
		chatTypes = new int[messageDisplayLimit];
		chatNames = new String[messageDisplayLimit];
		chatMessages = new String[messageDisplayLimit];
		chatPrefix = new String[messageDisplayLimit];
		statIcons = new Sprite[24];
		gameHasFocus = true;
		friendsListAsLongs = new long[200];
		currentSong = -1;
		spriteDrawX = -1;
		spriteDrawY = -1;
		anIntArray968 = new int[33];
		decompressors = new Index[6];
		variousSettings = new int[2000];
		aBoolean972 = false;
		anInt975 = 50;
		anIntArray976 = new int[anInt975];
		anIntArray977 = new int[anInt975];
		anIntArray978 = new int[anInt975];
		anIntArray979 = new int[anInt975];
		anIntArray980 = new int[anInt975];
		anIntArray981 = new int[anInt975];
		anIntArray982 = new int[anInt975];
		aStringArray983 = new String[anInt975];
		lastPlane = -1;
		refreshMinimap = false;
		hitMarks = new Sprite[20];
		anIntArray990 = new int[5];
		aBoolean994 = false;
		amountOrNameInput = "";
		amountOrNameTitle = "Enter Value";
		projectiles = new NodeList();
		aBoolean1017 = false;
		walkableInterfaceID = -1;
		anIntArray1030 = new int[5];
		aBoolean1031 = false;
		mapFunctions = new Sprite[100];
		dialogID = -1;
		maxStats = new int[Skills.skillsCount];
		anIntArray1045 = new int[2000];
		aBoolean1047 = true;
		anIntArray1052 = new int[152];
		anIntArray1229 = new int[152];
		flashingSidebar = -1;
		incompleteAnimables = new NodeList();
		anIntArray1057 = new int[33];
		aClass9_1059 = new RSInterface();
		mapScenes = new Background[100];
		barFillColor = 0x4d4233;
		anIntArray1065 = new int[7];
		anIntArray1072 = new int[1000];
		anIntArray1073 = new int[1000];
		aBoolean1080 = false;
		friendsList = new String[200];
		inStream = Stream.create();
		expectedCRCs = new int[9];
		menuActionCmd2 = new int[500];
		menuActionCmd3 = new int[500];
		menuActionID = new int[500];
		menuActionCmd1 = new int[500];
		headIcons = new Sprite[20];
		skullIcons = new Sprite[20];
		headIconsHint = new Sprite[20];
		redrawTabIcons = false;
		aString1121 = "";
		atPlayerActions = new String[5];
		atPlayerArray = new boolean[5];
		copyMapPalette = new int[4][13][13];
		aClass30_Sub2_Sub1_Sub1Array1140 = new Sprite[1000];
		aBoolean1141 = false;
		continueDialogue = false;
		crosses = new Sprite[8];
		musicEnabled = true;
		redrawTab = false;
		loggedIn = false;
		canMute = false;
		constructedViewport = false;
		cutSceneCamera = false;
		cutSceneHideRoofs = false;
		// myUsername = "";
		// myPassword = "";
		genericLoadingError = false;
		reportAbuseInterfaceID = 201;
		aClass19_1179 = new NodeList();
		anInt1184 = 128;
		invOverlayInterfaceID = -1;
		stream = Stream.create();
		menuActionName = new String[500];
		anIntArray1203 = new int[5];
		sound = new int[50];
		chatScrollMax = 100;
		promptInput = "";
		modIcons = new Background[2];
		tabID = 3;
		redrawChatbox = false;
		songChanging = true;
		collisionMaps = new CollisionMap[4];
		soundType = new int[50];
		aBoolean1242 = false;
		soundDelay = new int[50];
		soundVolume = new int[50];
		rsAlreadyLoaded = false;
		redrawGame = false;
		messagePromptRaised = false;
		loginMessage1 = "";
		loginMessage2 = "";
		backDialogID = -1;
		bigX = new int[4000];
		bigY = new int[4000];
	}

	public static int getMaxWidth() {
		return (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	}

	public static int getMaxHeight() {
		return (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	}

	public boolean isWebclient() {
		return rsFrame == null && isApplet == true;
	}

	public boolean outDated = true;
	private static boolean audioMuted = false;
	public double chosenBrightness = 0.80000000000000004D;
	private int selectedBankTab = 0;
	private int tempSelectedBankTab = 0;
	private int bankTabsUsed = 1;
	public String searchString = null;
    private int[] bankInvTemp = new int[9 * 89];
    private int[] bankStackTemp = new int[9 * 89];
	public String globalPrefix;
	private final int messageDisplayLimit = 500;
	private final int[] chatRights;
	private final boolean[] chatFiltered;
	public int chatTypeView;
	public int clanChatMode;
	public int duelMode;
	public int globalMode;
	public int gameMode;
	public int cButtonHPos;
	public int chatTypeIndex;
	private Sprite titleLogo;
	private Sprite background;
	public Sprite mascotInv;
	public Sprite mascotChat;
	public static GameFrame gameFrame;
	/**/
	private RSImageProducer leftFrame;
	private RSImageProducer topFrame;
	private int ignoreCount;
	private long aLong824;
	private int[][] anIntArrayArray825;
	private int[] friendsNodeIDs;
	private NodeList[][][] groundArray;
	private volatile boolean aBoolean831;
	private ProxySocket aSocket832;
	private int loginScreenState = 0;
	private Stream aStream_834;
	private NPC[] npcArray;
	private int npcCount;
	private int[] npcIndices;
	private int anInt839;
	private int[] anIntArray840;
	private int lastOpcode;
	private int secondLastOpcode;
	private int thirdLastOpcode;
	private String aString844;
	public int privateChatMode;
	private Stream aStream_847;
	private boolean soundEnabled;
	private static int anInt849;
	private static int anInt854;
	private int anInt855;
	static int openInterfaceID;
	private int xCameraPos;
	private int zCameraPos;
	private int yCameraPos;
	private int yCameraCurve;
	private int xCameraCurve;
	private int myPrivilege;
	private final int[] currentExp;

	public final int[] getCurrentExp() {
		return currentExp;
	}

	private Sprite mapFlag;
	private Sprite mapMarker;
	private boolean aBoolean872;
	private final int[] anIntArray873;
	private final boolean[] aBooleanArray876;

	private long aLong1172;
	private int anInt1257;

	private int weight;
	private MouseDetection mouseDetection;
	private String reportAbuseInput;
	private int localPlayerIndex;
	public boolean shiftDown = false;
	private boolean menuOpen;
	private int hoverInterface;
	private String inputString;
	private final int maxPlayers;
	private final int myPlayerIndex;
	private Player[] playerArray;
	private int playerCount;
	private int[] playerIndices;
	private int npcsAwaitingUpdateCount;
	private int[] npcsAwaitingUpdate;
	private Stream[] aStreamArray895s;
	private int viewRotationOffset;
	private int friendsCount;
	private int clanCount;
	private int anInt900;
	private int[][] anIntArrayArray901;
	private byte[] aByteArray912;
	private int anInt913;
	private int crossX;
	private int crossY;
	private int crossIndex;
	private int crossType;
	private int plane;
	private final int[] currentStats;
	private static int anInt924;
	private long[] ignoreListAsLongs;
	private boolean loadingError;
	private final int[] anIntArray928;
	private int[][] anIntArrayArray929;
	private Sprite aClass30_Sub2_Sub1_Sub1_931;
	private Sprite aClass30_Sub2_Sub1_Sub1_932;
	private int anInt933;
	private int anInt934;
	private int anInt935;
	private int anInt936;
	private int anInt937;
	private int anInt938;
	private final int[] chatTypes;
	private final String[] chatNames;
	private final String[] chatMessages;
	private final String[] chatPrefix;
	private int anInt945;
	private WorldController worldController;
	public Sprite[] statIcons;
	private int menuScreenArea;
	private int menuOffsetX;
	private int menuOffsetY;
	private int menuWidth;
	private int menuHeight;
	private long aLong953;
	private boolean gameHasFocus;
	private long[] friendsListAsLongs;
	private long[] clanListAsLongs;
	private int[] clanListRights;
	private int currentSong;
	private static int nodeID = 10;
	public static int portOff;
	static boolean clientData = false;
	static boolean clientDebug;
	private static boolean isMembers = true;
	private static boolean lowMem;
	private int spriteDrawX;
	private int spriteDrawY;
	private final int[] anIntArray965 = { 0xffff00, 0xff0000, 65280, 65535, 0xff00ff, 0xffffff };
	public final int[] anIntArray968;
	public final Index[] decompressors;
	public int variousSettings[];
	private boolean aBoolean972;
	private final int anInt975;
	private final int[] anIntArray976;
	private final int[] anIntArray977;
	private final int[] anIntArray978;
	private final int[] anIntArray979;
	private final int[] anIntArray980;
	private final int[] anIntArray981;
	private final int[] anIntArray982;
	private final String[] aStringArray983;
	private int anInt984;
	private boolean refreshMinimap;
	private int lastPlane;
	private static int anInt986;
	private Sprite[] hitMarks;
	private int anInt989;
	public final int[] anIntArray990;
	private final boolean aBoolean994;
	private int anInt995;
	private int anInt996;
	private int anInt997;
	private int anInt998;
	private int anInt999;
	private ISAACRandomGen encryption;
	private Sprite mapEdge;
	private Sprite multiOverlay;
	public static final int[][] anIntArrayArray1003 = {
			{ 6798, 107, 10283, 16, 4797, 7744, 5799, 4634, 33697, 22433, 2983, 54193 },
			{ 8741, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 56621, 4783, 1341, 16578, 35003, 25239 },
			{ 25238, 8742, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 56621, 4783, 1341, 16578, 35003 },
			{ 4626, 11146, 6439, 12, 4758, 10270 }, { 4550, 4537, 5681, 5673, 5790, 6806, 8076, 4574 } };
	private String amountOrNameInput;
	private String amountOrNameTitle;
	private static int anInt1005;
	private int daysSinceLastLogin;
	private int packetSize;
	private int opcode;
	private int timeoutCounter;
	private int anInt1010;
	private int anInt1011;
	private NodeList projectiles;
	private int anInt1014;
	private int anInt1015;
	private int anInt1016;
	private boolean aBoolean1017;
	private int walkableInterfaceID;
	private boolean walkableInterfaceMode;
	private static final int[] anIntArray1019;
	private int anInt1021;
	private int loadingStage;
	private Sprite scrollBar1;
	private Sprite scrollBar2;
	private Background scrollBar1_classic;
	private Background scrollBar2_classic;
	private int anInt1026;
	private final int[] anIntArray1030;
	private boolean aBoolean1031;
	private Sprite[] mapFunctions;
	private int baseX;
	private int baseY;
	private int anInt1036;
	private int anInt1037;
	private int loginFailures;
	private int anInt1039;
	private int dialogID;
	private final int[] maxStats;
	private final int[] anIntArray1045;
	private int anInt1046;
	private boolean aBoolean1047;
	private int anInt1048;
	private String aString1049;
	private static int anInt1051;
	private final int[] anIntArray1052;
	private Archive titleArchive;
	public int flashingSidebar;
	private int anInt1055;
	private NodeList incompleteAnimables;
	public final int[] anIntArray1057;
	public final RSInterface aClass9_1059;
	private Background[] mapScenes;
	private int currentSound;
	private final int barFillColor;
	private int friendsListAction;
	private final int[] anIntArray1065;
	private int mouseInvInterfaceIndex;
	private int lastActiveInvInterface;
	public OnDemandFetcher onDemandFetcher;
	private int anInt1069;
	private int anInt1070;
	private int anInt1071;
	private int[] anIntArray1072;
	private int[] anIntArray1073;
	private Sprite mapDotItem;
	private Sprite mapDotNPC;
	private Sprite mapDotPlayer;
	private Sprite mapDotFriend;
	private Sprite mapDotTeam;
	private Sprite mapDotClan;
	private int anInt1079;
	private boolean aBoolean1080;
	private String[] friendsList;
	private Stream inStream;
	private int anInt1084;
	private int lastMouseInvInterfaceIndex;
	private int activeInterfaceType;
	private int anInt1087;
	private int anInt1088;
	public static int chatScrollPos;
	public static int spellID = 0;
	public int cameraPos1 = 3;
	public static int cameraZoom = 600;
	public static int cameraZoomMin = 180;
	public static int cameraZoomMax = 900;
	public static int totalRead = 0;
	private final int[] expectedCRCs;
	private int[] menuActionCmd2;
	private int[] menuActionCmd3;
	public int[] menuActionID;
	private int[] menuActionCmd1;
	private Sprite multiWay;
	private Sprite[] headIcons;
	private Sprite[] skullIcons;
	private Sprite[] headIconsHint;
	private static int anInt1097;
	private int anInt1098;
	private int anInt1099;
	private int anInt1100;
	private int anInt1101;
	private int anInt1102;
	public static boolean redrawTabIcons;
	private int systemUpdateTime;
	private String serverMessage = "";
	private RSImageProducer aRSImageProducer_1107;
	private RSImageProducer aRSImageProducer_1108;
	private RSImageProducer aRSImageProducer_1109;
	private RSImageProducer aRSImageProducer_1110;
	private RSImageProducer aRSImageProducer_1111;
	private RSImageProducer aRSImageProducer_1112;
	private RSImageProducer aRSImageProducer_1113;
	private RSImageProducer aRSImageProducer_1114;
	private RSImageProducer aRSImageProducer_1115;
	private static int anInt1117;
	private int membersInt;
	private String aString1121;
	private Sprite compass;
	private RSImageProducer aRSImageProducer_1123;
	private RSImageProducer aRSImageProducer_1124;
	private RSImageProducer aRSImageProducer_1125;
	public static Player myPlayer;
	private final String[] atPlayerActions;
	private final boolean[] atPlayerArray;
	private final int[][][] copyMapPalette;
	public static final int[] tabInterfaceIDs = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
	private int cameraOffsetY;
	public int menuActionRow;
	private static int anInt1134;
	private int spellSelected;
	private int anInt1137;
	private int spellUsableOn;
	private String spellTooltip;
	private Sprite[] aClass30_Sub2_Sub1_Sub1Array1140;
	private boolean aBoolean1141;
	private int energy;
	public boolean continueDialogue;
	private Sprite[] crosses;
	private static boolean musicEnabled;
	public static boolean loginMusicEnabled;
	public static boolean redrawTab;
	private int unreadMessages;
	private static int anInt1155;
	private static boolean fpsOn;
	public static boolean loggedIn;
	private boolean canMute;
	private boolean constructedViewport;
	private boolean cutSceneCamera;
	private boolean cutSceneHideRoofs;
	public static int loopCycle;
	public static RSImageProducer tabImageProducer;
	private RSImageProducer aRSImageProducer_1164;
	private static RSImageProducer gameScreenImageProducer;
	private static RSImageProducer aRSImageProducer_1166;
	private int daysSinceRecovChange;
	private RSSocket socketStream;
	private int minimapZoom;
	public static String myUsername = "";
	public static String myPassword = "";
	public static boolean rememberMe = false;
	public static int verificationCode = -1;
	public static String verificationCodeS = "";
	public static boolean showItemNames = true;
	private static int anInt1175;
	private boolean genericLoadingError;
	private final int[] anIntArray1177 = { 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3 };
	private int reportAbuseInterfaceID;
	private NodeList aClass19_1179;
	private static int[] anIntArray1180;
	public static int[] anIntArray1181;
	private static int[] anIntArray1182;
	private byte[][] floorMapBytes;
	private int anInt1184;
	public int cameraHorizontal;
	private int anInt1186;
	private int anInt1187;
	private static int anInt1188;
	public int invOverlayInterfaceID;
	public static Stream stream;
	private int anInt1193;
	private int splitPrivateChat;
	private Background mapBack;
	public String[] menuActionName;
	private final int[] anIntArray1203;
	public static final int[] anIntArray1204 = { 9104, 10275, 7595, 3610, 7975, 8526, 918, 38802, 24466, 10145, 58654,
			5027, 1457, 16565, 34991, 25486 };
	private static boolean flagged;
	private final int[] sound;
	private int minimapRotation;
	static int chatScrollMax;
	private String promptInput;
	private int anInt1213;
	private int[][][] tileHeights;
	private long aLong1215;
	private int loginScreenCursorPos;
	private final Background[] modIcons;
	private long aLong1220;
	public static int tabID;
	private int anInt1222;
	public static boolean redrawChatbox;
	public static boolean scrollAlreadyUsed;
	public int inputDialogState;
	private static int anInt1226;
	private int nextSong;
	private boolean songChanging;
	private int musicVolume = 256;
	private boolean loopMusic = true;
	public MidiPlayer midiPlayer;
	private final int[] anIntArray1229;
	private CollisionMap[] collisionMaps;
	public static int BIT_MASKS[];
	private int[] regionIdArray;
	private int[] floorMapArray;
	private int[] objectMapArray;
	public final int anInt1239 = 100;
	private final int[] soundType;
	private boolean aBoolean1242;
	private int atInventoryLoopCycle;
	private int atInventoryInterface;
	private int atInventoryIndex;
	private int atInventoryInterfaceType;
	private byte[][] objectMapBytes;
	public int tradeMode;
	private int anInt1249;
	private final int[] soundDelay;
	private final int[] soundVolume;
	private int onTutorialIsland;
	private final boolean rsAlreadyLoaded;
	private int anInt1253;
	private int anInt1254;
	private boolean redrawGame;
	private boolean messagePromptRaised;
	private byte[][][] tileFlags;
	private int temporarySongDelay;
	private int destX;
	private int destY;
	private Sprite minimapImage;
	private int anInt1264;
	private int anInt1265;
	private String loginMessage1;
	private String loginMessage2;
	private int tempPositionX;
	private int tempPositionY;
	public TextDrawingArea smallFont, regularFont, boldFontS, fancyFontS, fancyFontM, fancyFontL, graveStoneFont;
	public RSFont newSmallFont, newRegularFont, newBoldFont, newFancyFontS, newFancyFontM, newFancyFontL,
			newGraveStoneFont;
	public int backDialogID;
	private int cameraOffsetX;
	private int[] bigX;
	private int[] bigY;
	private int itemSelected;
	private int anInt1283;
	private int anInt1284;
	private int anInt1285;
	private String selectedItemName;
	public int publicChatMode;
	private static int anInt1288;
	public static int anInt1290;
	public static String server = "";
	public int drawCount;
	public int fullscreenInterfaceID;
	public int anInt1044;// 377
	public int anInt1129;// 377
	public int anInt1315;// 377
	public int anInt1500;// 377
	public int anInt1501;// 377
	public static int[] fullScreenTextureArray;

	static {
		anIntArray1019 = new int[99];
		int i = 0;
		for (int j = 0; j < 99; j++) {
			int l = j + 1;
			int i1 = (int) ((double) l + 300D * Math.pow(2D, (double) l / 7D));
			i += i1;
			anIntArray1019[j] = i / 4;
		}
		BIT_MASKS = new int[32];
		i = 2;
		for (int k = 0; k < 32; k++) {
			BIT_MASKS[k] = i - 1;
			i += i;
		}
	}
}