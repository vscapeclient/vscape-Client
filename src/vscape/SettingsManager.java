package vscape;

import com.runescape.Client;
import com.runescape.Client.ScreenMode;
import com.runescape.sign.Signlink;
import vscape.gameframe.GameFrameManager;
import vscape.gameframe.GameFrameManager.GameFrameUI;
import vscape.widgets.XPDrop;

import java.io.*;

public class SettingsManager {

	public static final String settings_dir = Signlink.findcachedir() + "settings.ini";
	
	public static String savedUsername = "";
	public static String savedPassword = "";
	public static ScreenMode screenMode = ScreenMode.FIXED;
	public static int resizableW = 766;
	public static int resizableH = 529;
	public static int camDragSensitivity = 4;
	public static boolean zoomControl = false;
	public static int cameraZoom = 600;
	public static boolean showRoofs = true;
	public static boolean orbsEnabled = true;
	public static boolean orbsOnRight = false;
	public static boolean middleMouseCamera = true;
	public static int[] hotkeyButtons = {0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12};
	public static void write() {
		File file = new File(settings_dir);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch(IOException ioexception) {
			System.out.println("error writing settings file.");
		}
		if(file.exists())
		{
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(settings_dir))){
				writer.write("[CHARACTER]");
				writer.newLine();
				writer.write("remember = " + Client.rememberMe);
				writer.newLine();
				writer.write("username = " + (Client.rememberMe ? Client.myUsername : ""));
				writer.newLine();
				writer.write("password = " + (Client.rememberMe ? Client.myPassword : ""));
				writer.newLine(); 
				writer.newLine();
				writer.write("[CHAT]");
				writer.newLine();
				writer.write("game = " + Client.instance.gameMode);
				writer.newLine();
				writer.write("clan = " + Client.instance.clanChatMode);
				writer.newLine();
				writer.write("global = " + Client.instance.globalMode);
				writer.newLine();
				writer.newLine();
				writer.write("[GRAPHICAL]");
				writer.newLine();
				writer.write("sizeMode = " + Client.frameMode.toString());
				writer.newLine();
				writer.write("resizableW = " + Client.frameWidth);
				writer.newLine();
				writer.write("resizableH = " + Client.frameHeight);
				writer.newLine();
				writer.write("pixelScaling = " + Client.pixelScaling);
				writer.newLine();
				writer.write("gameFrame = " + GameFrameManager.getFrameUI().toString());
				writer.newLine();
				writer.write("zoomControl = " + zoomControl);
				writer.newLine();
				writer.write("showRoofs = " + showRoofs);
				writer.newLine();
				writer.write("orbsEnabled = " + orbsEnabled);
				writer.newLine();
				writer.write("orbsOnRight = " + orbsOnRight);
				writer.newLine();
				writer.write("groundItemNames = " + Client.showItemNames);
				writer.newLine();
				writer.newLine();
				writer.write("[MISC]");
				writer.newLine();
				writer.write("loginMusic = " + Client.loginMusicEnabled);
				writer.newLine();
				writer.write("camDragSensitivity = " + camDragSensitivity);
				writer.newLine();
				writer.write("middleMouseCamera = " + middleMouseCamera);
				writer.newLine();
				cameraZoom = Client.cameraZoom;
				if(cameraZoom < Client.cameraZoomMin) {
					cameraZoom = Client.cameraZoomMin;
				}
				if(cameraZoom > Client.cameraZoomMax) {
					cameraZoom = Client.cameraZoomMax;
				}
				writer.write("cameraZoom = " + cameraZoom);
				writer.newLine();
				writer.newLine();
				writer.write("[XPDROP]");
				writer.newLine();
				writer.write(XPDrop.getSettings());
				writer.newLine();
				writer.newLine();
				writer.write("[HOTKEYS]");
				writer.newLine();
				for (int i = 0; i < 12; i++) {
					writer.write("f" + (i + 1) + " = "  + hotkeyButtons[i]);
					writer.newLine();
				}
				writer.flush();
			} catch(IOException ioexception) {
				System.out.println("error writing settings file.");
			}
		}
		savedUsername = (Client.rememberMe ? Client.myUsername : "");
		savedPassword = (Client.rememberMe ? Client.myPassword : "");
		screenMode = Client.frameMode;
		resizableW = Client.frameWidth;
		resizableH = Client.frameHeight;
	}
	
	public static void load(){
		File file = new File(settings_dir);
		if (file.exists()) {
			String line = "";
			String token = "";
			String token2 = "";
			String section = "";
			try(BufferedReader reader = new BufferedReader(new FileReader(settings_dir))){
				line = reader.readLine();
				while(line != null) {
					line = line.trim();
					if(line.startsWith("[") && line.endsWith("]"))
					{
						section = line;
					}
					int equalIndex = line.indexOf("=");
					if (equalIndex > -1) {
						token = line.substring(0, equalIndex);
						token = token.trim();
						token2 = line.substring(equalIndex + 1);
						token2 = token2.trim();
						if(!token2.isEmpty() && token2.length() > 0)
						{
							switch(section) {
								case "[CHARACTER]":
									switch(token) {
										case "remember" :
											Client.rememberMe = Boolean.parseBoolean(token2);
										break;
										case "username" :
											savedUsername = token2;
											Client.myUsername = savedUsername;
										break;
										case "password" :
											savedPassword = token2;
											Client.myPassword = savedPassword;
										break;
									}
								break;
								case "[CHAT]":
									switch(token) {
										case "game" :
											 Client.instance.gameMode = Integer.parseInt(token2);
										break;
										case "clan" :
											 Client.instance.clanChatMode = Integer.parseInt(token2);
										break;
										case "global" :
											 Client.instance.globalMode = Integer.parseInt(token2);
										break;
									}
								break;
								case "[GRAPHICAL]":
									switch(token) {
										case "sizeMode" :
											try {
												screenMode = ScreenMode.valueOf(token2);
											} catch(Exception ex) {
												screenMode = ScreenMode.FIXED;
											}
										break;
										case "resizableW" :
											resizableW = Integer.parseInt(token2);
										break;
										case "resizableH" :
											resizableH = Integer.parseInt(token2);
										break;
										case "pixelScaling" :
											Client.pixelScaling = Integer.parseInt(token2);
											if (Client.pixelScaling != 1)
												Client.rebuildFrameSize(SettingsManager.screenMode, Client.frameWidth, Client.frameHeight);
										break;
										case "gameFrame" :
											try {
												GameFrameUI gameFrameUI = GameFrameUI.valueOf(token2);
												GameFrameManager.setFrameUI(gameFrameUI);
											} catch(Exception ex) {
												GameFrameManager.setFrameUI(GameFrameUI.DEFAULT);
											}
											break;
										case "zoomControl" :
											zoomControl = Boolean.parseBoolean(token2);
											Client.getClient().changeConfig(162, zoomControl ? 1 : 0);
										break;
										case "showRoofs" :
											showRoofs = Boolean.parseBoolean(token2);
											Client.getClient().changeConfig(163, showRoofs ? 0 : 1);
										break;
										case "orbsEnabled" :
											orbsEnabled = Boolean.parseBoolean(token2);
											Client.getClient().changeConfig(164, orbsEnabled ? 0 : 1);
											break;
										case "orbsOnRight" :
											orbsOnRight = Boolean.parseBoolean(token2);
											break;
										case "groundItemNames" :
											Client.showItemNames = Boolean.parseBoolean(token2);
											break;
									}
								break;
								case "[MISC]":
									switch(token) {
										case "loginMusic" :
											Client.loginMusicEnabled = Boolean.parseBoolean(token2);
										break;
										case "camDragSensitivity" :
											camDragSensitivity = Integer.parseInt(token2);
											if(camDragSensitivity < 1)
												camDragSensitivity = 1;
											if(camDragSensitivity > 10)
												camDragSensitivity = 10;
										break;
										case "middleMouseCamera" :
											middleMouseCamera = Boolean.parseBoolean(token2);
											Client.getClient().changeConfig(165, middleMouseCamera ? 0 : 1);
										break;
										case "cameraZoom" :
											cameraZoom = Integer.parseInt(token2);
											if(cameraZoom < Client.cameraZoomMin) {
												cameraZoom = Client.cameraZoomMin;
											}
											if(cameraZoom > Client.cameraZoomMax) {
												cameraZoom = Client.cameraZoomMax;
											}
										break;
									}
								break;
								case "[XPDROP]":
									XPDrop.setSettings(token, token2);
								break;
								case "[HOTKEYS]":									
									for (int i = 0; i < hotkeyButtons.length; i++) {
										String functionKey = "f" + (i + 1);
										if (token.equals(functionKey)) {
											hotkeyButtons[i] = Integer.parseInt(token2);
											if (hotkeyButtons[i] > 13 || hotkeyButtons[i] < 0) {
												hotkeyButtons[i] = 0;
											}
										}
									}
								break;
							}
						}
					}
					line = reader.readLine();
				}
			} catch(IOException ioexception) {
				System.out.println("error writing settings file.");
			}
		}
	}
}
