package vscape.gameframe;

import com.runescape.Client;
import com.runescape.cache.graphics.RSInterface;
import com.runescape.cache.graphics.Sprite;
import com.runescape.drawing.DrawingArea;
import vscape.cache.media.SpriteLoader;

import java.awt.*;

public final class ClassicFrame extends GameFrame {

    private final static String archive_name = "frame_classic";

    @Override
    public void init() {
        /** Redstones */
        //top left
        Sprite redStone_0 = SpriteLoader.getSprite(archive_name, "redstones", 0);
        Sprite redStone_1 = SpriteLoader.getSprite(archive_name, "redstones", 1);
        //top middle
        Sprite redStone_2 = SpriteLoader.getSprite(archive_name, "redstones", 2);
        //top right flip
        Sprite redStone_3 = redStone_1.clone();
        redStone_3.flipHorizontal();
        Sprite redStone_4 = redStone_0.clone();
        redStone_4.flipHorizontal();
        //bottom left flip
        Sprite redStone_5 = redStone_0.clone();
        redStone_5.flipVertical();
        Sprite redStone_6 = redStone_1.clone();
        redStone_6.flipVertical();
        //bottom middle
        Sprite redStone_7 = redStone_2.clone();
        redStone_7.flipVertical();
        //bottom right flip
        Sprite redStone_8 = redStone_3.clone();
        redStone_8.flipVertical();
        Sprite redStone_9 = redStone_4.clone();
        redStone_9.flipVertical();
        redstones = new Sprite[]{redStone_0, redStone_1, redStone_2, redStone_3, redStone_4, redStone_5, redStone_6, redStone_7, redStone_8, redStone_9};
        compass = SpriteLoader.getSprite(archive_name, "map", 4);
    }

    @Override
    public void drawMapArea(Client cl, boolean covered) {
        final Point mapBase = getMapOffset(false);
		final int drawX = mapBase.x;
		final int drawY = mapBase.y;
        final int mapCX = mapBase.x + 125;
        final int mapCY = mapBase.y + (Client.frameMode == Client.ScreenMode.FIXED ? 82 : 82);
        compass.shapeImageToPixels(33, cl.cameraHorizontal, cl.anIntArray1057, 256, cl.anIntArray968, 25, mapBase.y + (Client.frameMode == Client.ScreenMode.FIXED ? 3 : 4), mapBase.x + (Client.frameMode == Client.ScreenMode.FIXED ? 28 : 29), 33, 25);
        if(covered) {
            SpriteLoader.getSprite(archive_name, "map", Client.frameMode == Client.ScreenMode.FIXED ? 1 : 3).drawSprite(drawX, drawY);
        } else {
            DrawingArea.drawPixels(3, mapCY, mapCX, 0xffffff, 3);
            SpriteLoader.getSprite(archive_name, "map", Client.frameMode == Client.ScreenMode.FIXED ? 0 : 2).drawSprite(drawX, drawY);
        }
    }

    @Override
    public void drawRedStones(Client cl) {
        if(Client.showTabComponents && Client.tabID >= 0 && Client.tabID < 14) {
            if(Client.tabInterfaceIDs[Client.tabID] <= -1){
                return;
            }
            if(Client.frameMode == Client.ScreenMode.FIXED) {
                Sprite redStone = redstones[stoneType[Client.tabID]];
                if(redStone != null) {
                    redStone.drawSprite(stoneX[Client.tabID % 7], stoneY[Client.tabID]);
                }
            } else {
                boolean largeTab = Client.frameWidth >= Client.smallTabThreshold;
                int xOffset = largeTab ? Client.frameWidth - 462 : Client.frameWidth - 231;
                int yOffset = largeTab ? Client.frameHeight - 36 : Client.frameHeight - 72;
                if(!largeTab && Client.tabID > 6) {
                    yOffset += 36;
                }
                xOffset += 33 * (!largeTab ? (Client.tabID % 7) : Client.tabID);
                SpriteLoader.getSprite(archive_name, "tabs", 5).drawSprite(xOffset, yOffset);
            }
        }
    }

    @Override
    public void drawSideIcons(Client cl) {
        int baseX = 0;
        int baseY = 0;
        boolean largeTab = Client.frameWidth >= Client.smallTabThreshold;
        if(Client.frameMode != Client.ScreenMode.FIXED) {
            baseX = largeTab ? Client.frameWidth - 462 : Client.frameWidth - 231;
            baseY = largeTab ? Client.frameHeight - 36 : Client.frameHeight - 72;
        }
        for (int i = 0; i < 14; i++) {
            int x = baseX + stoneX[i % 7] + siX[i];
            int y = baseY + stoneY[i] + siY[i];
            if(Client.frameMode != Client.ScreenMode.FIXED) {
                x = baseX + (33 * (!largeTab ? (i % 7) : i)) + siRX[i];
                y = baseY + siRY[i];
                if (!largeTab && i > 6) {
                    y += 36;
                }
            }
            boolean drawIcon = Client.tabInterfaceIDs[i] != -1 && cl.flashingSidebar != i;
            if(cl.flashingSidebar == i) {
                drawIcon = ((Client.loopCycle % 20) < 10);
            }
            if(drawIcon) {
                SpriteLoader.getSprite(archive_name, "sideicons", i).drawSprite(x, y);
            }
        }
    }

    @Override
    public void drawTabArea(Client cl) {
        Rectangle invBounds = getInvBounds();
        boolean largeTab = Client.frameWidth >= Client.smallTabThreshold;
        if (Client.frameMode == Client.ScreenMode.FIXED) {
            SpriteLoader.getSprite(archive_name, "tabs", 0).drawSprite(0, 0);
            cl.mascotInv.draw24BitSprite(30, 37);
        } else {
            int xOffset = largeTab ? Client.frameWidth - 462 : Client.frameWidth - 231;
            int yOffset = largeTab ? Client.frameHeight - 36 : Client.frameHeight - 72;
            SpriteLoader.getSprite(archive_name, "tabs", largeTab ? 4 : 3).drawSprite(xOffset, yOffset);
            if (Client.showTabComponents) {
                final int xOffSp = (int)invBounds.getWidth() + (largeTab ? 0 : 14);
                final int yOffSp = (int)invBounds.getHeight();
                final int xOffBg = xOffSp - 7;
                final int yOffBg = yOffSp - 7;
                SpriteLoader.getSprite(archive_name, "tabs", 1).drawARGBSprite(Client.frameWidth - xOffSp, yOffset - yOffSp, 200);
                cl.mascotInv.draw24BitSprite(Client.frameWidth - (xOffBg-1), yOffset - yOffBg);
                SpriteLoader.getSprite(archive_name, "tabs", 2).drawSprite(Client.frameWidth - xOffSp, yOffset - yOffSp);
            }
        }
        if (cl.invOverlayInterfaceID == -1) {
            drawRedStones(cl);
            drawSideIcons(cl);
        }
        if (Client.showTabComponents) {
            int x = Client.frameMode == Client.ScreenMode.FIXED ? 31 : Client.frameWidth - 211;
            int y = Client.frameMode == Client.ScreenMode.FIXED ? 36 : Client.frameHeight - 340;
            if(Client.frameMode != Client.ScreenMode.FIXED && largeTab) {
                x += 16;
                y += 37;
            }
            if (cl.invOverlayInterfaceID != -1) {
                cl.drawInterface(x, y, 190, 260, RSInterface.interfaceCache[cl.invOverlayInterfaceID], -1, 0);
            } else if (Client.tabInterfaceIDs[Client.tabID] != -1) {
                cl.drawInterface(x, y, 190, 260, RSInterface.interfaceCache[Client.tabInterfaceIDs[Client.tabID]], -1, 0);
            }
        }
    }

	@Override
    public void processTabClick(Client cl) {
        if (cl.clickMode3 != 1) {
            return;
        }
        if(Client.frameMode == Client.ScreenMode.FIXED) {
            int baseX = 516;
            int baseY = 168;
            if (cl.mouseX >= baseX && cl.mouseX <= baseX + 249 && cl.mouseY >= baseY && cl.mouseY <= baseY + 335) {
                for (int i = 0; i < 14; i++) {
                    if(Client.tabInterfaceIDs[i] == -1) {
                        continue;
                    }
                    Sprite redStone = redstones[stoneType[i]];
                    if (redStone == null) {
                        continue;
                    }
                    int clickX = baseX + stoneX[i % 7];
                    int clickXMax = clickX + redStone.myWidth;
                    int clickY = baseY + stoneY[i];
                    int clickYMax = clickY + redStone.myHeight;
                    if (cl.mouseX >= clickX && cl.mouseX < clickXMax && cl.mouseY >= clickY && cl.mouseY < clickYMax) {
                        Client.tabID = i;
                        Client.redrawTabIcons = true;
                        Client.redrawTab = true;
                        break;
                    }
                }
            }
        } else {
            boolean largeTab = Client.frameWidth >= Client.smallTabThreshold;
            int baseX = largeTab ? Client.frameWidth - 462 : Client.frameWidth - 231;
            int baseY = largeTab ? Client.frameHeight - 36 : Client.frameHeight - 72;
            if (cl.mouseX >= baseX && cl.mouseX <= Client.frameWidth && cl.mouseY >= baseY && cl.mouseY <= Client.frameHeight ) {
                for (int i = 0; i < 14; i++) {
                    if(Client.tabInterfaceIDs[i] == -1) {
                        continue;
                    }
                    int clickX = baseX + (33 * (!largeTab ? (i % 7) : i));
                    int clickXMax = clickX + 33;
                    int clickY = stoneY[i];
                    if (!largeTab && i > 6) {
                        clickY += 36;
                    }
                    int clickYMax = baseY + clickY + 36;
                    if (cl.mouseX >= clickX && cl.mouseX < clickXMax && cl.mouseY >= clickY && cl.mouseY < clickYMax) {
                        if (Client.tabID == i) {
                            Client.showTabComponents = !Client.showTabComponents;
                        } else {
                            Client.showTabComponents = true;
                        }
                        Client.tabID = i;
                        Client.redrawTab = true;
                        Client.redrawTabIcons = true;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void drawChatArea(Client cl) {
        int yOffset = getChatOffsetY(false);
        if (Client.showChatComponents) {
            if (Client.resizableChatArea) {
                SpriteLoader.getSprite(archive_name, "chat", 1).drawARGBSprite(0, yOffset - 1, 200);
                SpriteLoader.getSprite(archive_name, "chat", 2).drawSprite(0, yOffset - 1);
                cl.mascotChat.draw24BitSprite(7, yOffset + 6);
            } else {
                SpriteLoader.getSprite(archive_name, "chat", 0).drawSprite(0, yOffset);
                cl.mascotChat.draw24BitSprite(7, yOffset + 6);
            }
        }
        if (Client.resizableChatArea) {
            SpriteLoader.getSprite(archive_name, "chat", 3).drawSprite(5, Client.frameHeight - 23);
        }
        drawChannelButtons(cl);
    }

    @Override
    public void drawChannelButtons(Client cl) {
        final int yOffset = this.getChatOffsetY(false);
        if(cl.chatTypeIndex >= 0 & cl.chatTypeIndex <= 6) {
            SpriteLoader.getSprite(archive_name, "chat", 5).drawSprite(cbX[cl.chatTypeIndex], 142 + yOffset);
        }
        if(cl.cButtonHPos >= 0 & cl.cButtonHPos <= 6) {
            SpriteLoader.getSprite(archive_name, "chat", cl.cButtonHPos == cl.chatTypeIndex ? 6 : 4).drawSprite(cbX[cl.cButtonHPos], 142 + yOffset);
        }
        int[] modes = { cl.gameMode, cl.publicChatMode, cl.privateChatMode, cl.clanChatMode, cl.tradeMode, cl.globalMode};
        for (int i = 0; i < chatModeNames.length; i++) {
            cl.smallFont.method389(true, cmNX[i], 0xffffff, chatModeNames[i], cmNY[i] + yOffset);
        }
        for (int i = 0; i < modes.length; i++) {
            final int[] textColor = cmCol[cmCTI[i]];
            final String[] text = cmTxt[cmCTI[i]];
            cl.smallFont.method382(textColor[modes[i]], cmX[i], text[modes[i]], 163 + yOffset, true);
        }
    }

    @Override
    public void processChatModeClick(Client cl) {
        final int y = Client.frameMode != Client.ScreenMode.FIXED ? Client.frameHeight - 21 : 482;
        if(cl.mouseX >= 5 && cl.mouseX <= 61 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.cButtonHPos = 0;
            Client.redrawChatbox = true;
        } else if(cl.mouseX >= 71 && cl.mouseX <= 127 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.cButtonHPos = 1;
            Client.redrawChatbox = true;
        } else if(cl.mouseX >= 137 && cl.mouseX <= 193 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.cButtonHPos = 2;
            Client.redrawChatbox = true;
        } else if(cl.mouseX >= 203 && cl.mouseX <= 259 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.cButtonHPos = 3;
            Client.redrawChatbox = true;
        } else if( cl.mouseX >= 269 && cl.mouseX <= 325 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.cButtonHPos = 4;
            Client.redrawChatbox = true;
        } else if(cl.mouseX >= 335 && cl.mouseX <= 391 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.cButtonHPos = 5;
            Client.redrawChatbox = true;
        } else if(cl.mouseX >= 401 && cl.mouseX <= 457 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.cButtonHPos = 6;
            Client.redrawChatbox = true;
        } else if(cl.cButtonHPos != -1) {
            cl.cButtonHPos = -1;
            Client.redrawChatbox = true;
        }
    }

    @Override
    public void rightClickChatButtons(Client cl) {
        final int y = Client.frameMode != Client.ScreenMode.FIXED ? Client.frameHeight - 21 : 482;
        if(cl.mouseX >= 5 && cl.mouseX <= 61 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.menuActionName[1] = "View All";
            cl.menuActionID[1] = 999;
            cl.menuActionRow = 2;
        } else if(cl.mouseX >= 71 && cl.mouseX <= 127 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.menuActionName[1] = "Off Game";
            cl.menuActionID[1] = 1002;
            cl.menuActionName[2] = "Filtered Game";
            cl.menuActionID[2] = 1001;
            cl.menuActionName[3] = "On Game";
            cl.menuActionID[3] = 1000;
            cl.menuActionName[4] = "View Game";
            cl.menuActionID[4] = 998;
            cl.menuActionRow = 5;
        } else if(cl.mouseX >= 137 && cl.mouseX <= 193 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.menuActionName[1] = "Hide public";
            cl.menuActionID[1] = 997;
            cl.menuActionName[2] = "Off public";
            cl.menuActionID[2] = 996;
            cl.menuActionName[3] = "Friends public";
            cl.menuActionID[3] = 995;
            cl.menuActionName[4] = "On public";
            cl.menuActionID[4] = 994;
            cl.menuActionName[5] = "View public";
            cl.menuActionID[5] = 993;
            cl.menuActionRow = 6;
        } else if(cl.mouseX >= 203 && cl.mouseX <= 259 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.menuActionName[1] = "Off private";
            cl.menuActionID[1] = 992;
            cl.menuActionName[2] = "Friends private";
            cl.menuActionID[2] = 991;
            cl.menuActionName[3] = "On private";
            cl.menuActionID[3] = 990;
            cl.menuActionName[4] = "View private";
            cl.menuActionID[4] = 989;
            cl.menuActionRow = 5;
        } else if(cl.mouseX >= 269 && cl.mouseX <= 325 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.menuActionName[1] = "Off clan";
            cl.menuActionID[1] = 983;
            cl.menuActionName[2] = "Hidden clan";
            cl.menuActionID[2] = 982;
            cl.menuActionName[3] = "On clan";
            cl.menuActionID[3] = 981;
            cl.menuActionName[4] = "View clan";
            cl.menuActionID[4] = 980;
            cl.menuActionRow = 5;
        } else if(cl.mouseX >= 335 && cl.mouseX <= 391 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.menuActionName[1] = "Off trade";
            cl.menuActionID[1] = 987;
            cl.menuActionName[2] = "Friends trade";
            cl.menuActionID[2] = 986;
            cl.menuActionName[3] = "On trade";
            cl.menuActionID[3] = 985;
            cl.menuActionName[4] = "View trade";
            cl.menuActionID[4] = 984;
            cl.menuActionRow = 5;
        } else if(cl.mouseX >= 401 && cl.mouseX <= 457 && cl.mouseY >= y && cl.mouseY <= Client.frameHeight) {
            cl.menuActionName[1] = "Off global";
            cl.menuActionID[1] = 1008;
            cl.menuActionName[2] = "Hidden global";
            cl.menuActionID[2] = 1007;
            cl.menuActionName[3] = "On global";
            cl.menuActionID[3] = 1006;
            cl.menuActionName[4] = "View global";
            cl.menuActionID[4] = 1005;
            cl.menuActionRow = 5;
        }
    }

    @Override
    public int getChatOffsetY(boolean producerOffset) {
        if(producerOffset) {
            return Client.frameMode == Client.ScreenMode.FIXED ? 338 : Client.frameHeight - 165;
        }
        return Client.frameMode == Client.ScreenMode.FIXED ? 0 : Client.frameHeight - 165;
    }

    @Override
    public Rectangle getChatBounds() {
        return (Client.frameMode == Client.ScreenMode.FIXED ? chatBounds : chatBounds_res);
    }

    @Override
    public int chatLineSpacing() {
        return 3;
    }

    @Override
    public Rectangle getInvBounds() {
        return inventoryBounds;
    }

    @Override
    public Point getMapOffset(boolean producerOffset) {
        return new Point(Client.frameMode == Client.ScreenMode.FIXED ? (producerOffset ? 516 : 0) : (Client.frameWidth - 238), producerOffset ? 0 : (Client.frameMode == Client.ScreenMode.FIXED ? 0 : 3));
    }

    @Override
    public Point getMapImageOffset() {
        return miOffset;
    }

    @Override
    public boolean useOldScrollBar() {
        return true;
    }

    @Override
    public Sprite getSideIcon(int index) {
        return SpriteLoader.getSprite(archive_name, "sideicons", index);
    }

    //map
    private static Sprite compass;
    private final static Point miOffset = new Point(54, 9);
    //Redstones
    private Sprite[] redstones = new Sprite[10];
    private final static int[] stoneType = {
            0, 1, 1, 2, 3, 3, 4, 5, 6, 6, 7, 8, 8, 9
    };
    private final static int[] stoneX = {
            17, 48, 76, 105, 148, 177, 204
    };
    private final static int[] stoneY = {
            1, 1, 1, 1, 1, 1, 1, 297, 297, 297, 300, 297, 297, 297
    };
    //Sideicons
    private final static int[] siX = {
            10, 4, 5, 6, 2, 2, 3, 8, 3, 3, 9, 2, 5, 4
    };
    private final static int[] siY = {
            9, 7, 7, 4, 6, 6, 7, 2, 7, 7, 6, 5, 6, 6
    };
    private final static int[] siRX = {
            7, 4, 5, 1, 4, 4, 4, 5, 5, 5, 3, 3, 7, 6
    };
    private final static int[] siRY = {
            9, 7, 7, 4, 5, 5, 6, 4, 6, 6, 6, 4, 5, 5
    };
    //Chat buttons
    private final static int[] cbX = { 5, 71, 137, 203, 269, 335, 401 };
    private final static int[] cmNX = { 26, 86, 150, 212, 286, 349, 411 }, cmNY = { 158, 153, 153, 153, 153, 153, 153 }, cmX = { 100, 164, 230, 296, 362, 428};
    private final static int[][] cmCol = {
            { 65280, 0xffff00, 0xff0000, 65535 }, { 65280, 0xffff00, 0xff0000 }, { 65280, 65535, 0xff0000 }
    };
    private final static String[][] cmTxt = {
            { "On", "Friends", "Off", "Hide" }, { "On", "Filtered", "Off" }, { "On", "Hidden", "Off" }
    };
    private final static int[] cmCTI = {1, 0, 0, 2, 0, 2};
    private final static String[] chatModeNames = { "All", "Game", "Public", "Private", "Clan", "Trade", "Global" };
    //Chat bounds
    private final static Rectangle chatBounds = new Rectangle(20,20,486,100);
    private final static Rectangle chatBounds_res = new Rectangle(10,6,497,114);
    //Inventory Bounds
    private final static Rectangle inventoryBounds = new Rectangle(0,0,204,275);
}
