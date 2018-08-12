package vscape.widgets;

import com.runescape.Client;
import com.runescape.Client.ScreenMode;
import com.runescape.cache.graphics.Sprite;
import com.runescape.drawing.DrawingArea;
import com.runescape.drawing.rsDrawingArea;
import com.runescape.entity.Entity;
import com.runescape.entity.NPC;
import com.runescape.entity.Player;
import com.runescape.util.Skills;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HPView {
	
	private static boolean enabled = true;
	private static int hideAfter = 3; // 0 always show
	private static int dropPositionX = 0;
	private final static int mergeThreshold = 40;
	private final static int[] skillSprites = { 0, 2, 1, 6, 3, 4, 5, 15, 17, 11, 14, 16, 10, 13, 12, 8, 7, 9, 19, 20, 18, 22, 23 };
	private static int baseXPos = Client.frameWidth / 2;
	private static int startYPos = Client.frameHeight / 2;
	private static int overallDisplayTime = 0;
	private final static List<int[]> xpDrops = new ArrayList<int[]>();
	
	public static int getBaseXPos() {
		switch(dropPositionX) {
			case 0 :
				return 66;
			default :
			case 1 :
				return Client.frameMode == ScreenMode.FIXED ? ((Client.frameWidth - 252) / 2) : Client.frameWidth / 2;
			case 2 :
				return (Client.frameWidth - 252) - 66;
		}
	}
	
	public static void draw(NPC mob) {
		if(!enabled) return;
		try {
			NpcCombatDefinition def = NpcCombatDefinition.forId(mob.desc.npcID);
			int maxHealth = def.getHitpoints();
			double currentHealthPercent = mob.currentHealth / 100.0;
			long currentHealth = Math.round(currentHealthPercent * maxHealth);
			String name = mob.desc.name;
		
			if (name == null)
			{
				return;
			}
			
			int height = 50;
			int width = 125;
			int xPos = 7;
			int yPos = 20;
			
			//Draw box ..
			rsDrawingArea.drawAlphaFilledPixels(xPos, yPos, width, height, 000000, 10);
			
			//Draw name..
			if(name != null) {
				Client.getClient().newSmallFont.drawCenteredString(name, xPos+(width/2), yPos + 12, 16777215, 0);
			}
			
			//Draw health..
			Client.getClient().newBoldFont.drawCenteredString(currentHealth + "/" + maxHealth, xPos+(width/2), yPos + 30, 16777215, 0);
			
			//Draw red and green pixels..
			
			//Draw missing health
			rsDrawingArea.drawFilledPixels(xPos + 2, yPos + 38, width - 4, 10, 11740160);
			
			//Draw existing health
			int pixelsLength = (int) (((double) currentHealth / (double) maxHealth) * (width - 4));
			if(pixelsLength > (width - 4)) {
				pixelsLength = (width - 4);
			}
			rsDrawingArea.drawFilledPixels(xPos + 2, yPos + 38, pixelsLength, 10, 31744);
			
		} catch(Exception ex) {
			System.out.println("XP Drop draw error" + ex);
		}
	}
	
	public static void addXPDrop(int skill, int xp) {
		try {
			if(skill >= skillSprites.length) return;
			if (!Skills.skillEnabled[skill]) return;			
			boolean addnew = true;
			int lastSkillIndex = lastSkillIndex(skill);
			if(lastSkillIndex >= 0) {
				int[] drop = xpDrops.get(lastSkillIndex);
				if(drop[2] <= mergeThreshold) {
					drop[1] += xp;
					addnew = false;
				}
			}
			if(addnew) {
				xpDrops.add(new int[] {skill, xp, 0});
				for(int i = xpDrops.size() - 1; i > 0; i--) {
					int[] entry = xpDrops.get(i);
					if(entry == null) continue;
					if(i > 0) {
						int[] prevEntry = xpDrops.get(i-1);
						if(prevEntry == null) continue;
						int diff = entry[2] - prevEntry[2];
						if(entry[2] < prevEntry[2]) {
							diff = prevEntry[2] - entry[2];
						}
						if(diff <= 32) {
							prevEntry[2] += (32 - diff);
						}
					}
				}
			}
			if(hideAfter > 0)
				overallDisplayTime = hideAfter * 50;
		} catch(Exception ex) {
			System.out.println("XP Drop error");
		}
	}
	
	public static void reset() {
		xpDrops.clear();
		overallDisplayTime = 0;
	}
	
	private static int lastSkillIndex(int skill) {
		if(xpDrops.size() > 0) {
			for(int i = 0; i < xpDrops.size(); i++) {
				int[] drop = xpDrops.get(i);
				if(drop == null) continue;
				if(drop[0] == skill) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static void setDropPosX(int pos) {
		dropPositionX = pos;
	}
	
	public static void toggle() {
		enabled = !enabled;
		reset();
		if(enabled) {
			if(hideAfter > 0)
				overallDisplayTime = hideAfter * 50;
		}
	}

	public static boolean isEnabled() {
		return enabled;
	}
	
	public static String getSettings() {
		StringBuilder bldr = new StringBuilder();
		bldr.append("enabled = " + enabled).append(System.lineSeparator());
		bldr.append("hideAfter = " + hideAfter).append(System.lineSeparator());
		bldr.append("dropPosition = " + dropPositionX);
		return bldr.toString();
	}
	
	public static void setSettings(String key, String value) {
		switch(key) {
			case "enabled" :
				enabled = Boolean.parseBoolean(value);
				break;
			case "hideAfter" :
				hideAfter = Integer.parseInt(value);
				if(hideAfter < 0) {
					hideAfter = 0;
				} else if(hideAfter > 10) {
					hideAfter = 10;
				}
				break;
			case "dropPosition" :
				dropPositionX = Integer.parseInt(value);
				if(dropPositionX < 0) {
					dropPositionX = 0;
				} else if(dropPositionX > 2) {
					dropPositionX = 2;
				}
				break;
		}
	}
}
