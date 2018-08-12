package vscape.widgets;

import com.runescape.Client;
import com.runescape.Client.ScreenMode;
import com.runescape.cache.graphics.Sprite;
import com.runescape.drawing.DrawingArea;
import com.runescape.util.Skills;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XPDrop {
	
	private static boolean enabled = false;
	private static int hideAfter = 3; // 0 always show
	private static int dropPositionX = 1;
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
	
	public static void draw() {
		if(!enabled) return;
		try {
			baseXPos = getBaseXPos();
			startYPos = Client.frameMode == ScreenMode.FIXED ? ((Client.frameHeight - 166) / 2) : Client.frameHeight / 2;
			if(xpDrops.size() > 0) {
				for (Iterator<int[]> drops = xpDrops.iterator(); drops.hasNext();) {
					int[] xpDrop = drops.next();
					if(xpDrop == null) continue;
					int dropYPos = xpDrop[2] += 1;
					int currentYPos = startYPos - dropYPos;
					String xpString = NumberFormat.getIntegerInstance().format( xpDrop[1] );
					int dropXPos = baseXPos - 16 - (Client.getClient().newRegularFont.getTextWidth(xpString) / 2);
					Sprite statSprite = Client.getClient().statIcons[skillSprites[ xpDrop[0] ]];
					statSprite.drawSprite(dropXPos, currentYPos);
					Client.getClient().newRegularFont.drawBasicString(xpString, dropXPos + 26, currentYPos+16, 0xffffff, 0x000000);
					if(currentYPos < 10) {
						drops.remove();
					}
				}
			}
			//overall display
			if(hideAfter == 0 || (hideAfter > 0 && overallDisplayTime > 0))  {
				DrawingArea.method335(0x5A5245, 4, 120, 30, 230, baseXPos - 60);
				String overallString = NumberFormat.getIntegerInstance().format(Client.getClient().getOverallExp());
				int textWidth = Client.getClient().newSmallFont.getTextWidth(overallString);
				Sprite overallSprite = Client.gameFrame.getSideIcon(1);
				overallSprite.drawSprite(baseXPos - 60, 4);
				Client.getClient().newSmallFont.drawBasicString(overallString, (baseXPos + 58) - textWidth, 24, 0xffffff, 0x000000);
				if(hideAfter > 0 && xpDrops.size() <= 0) 
					overallDisplayTime--;
			}
		} catch(Exception ex) {
			System.out.println("XP Drop draw error");
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
