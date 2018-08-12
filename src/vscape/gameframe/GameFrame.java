package vscape.gameframe;

import com.runescape.Client;
import com.runescape.cache.graphics.Sprite;

import java.awt.*;

public abstract class GameFrame {

    public GameFrame() { }

    public abstract void init();

    public abstract void drawMapArea(Client cl, boolean covered);

    public abstract void drawRedStones(Client cl);

    public abstract void drawSideIcons(Client cl);

    public abstract void drawTabArea(Client cl);

    public abstract void processTabClick(Client cl);

    public abstract void drawChatArea(Client cl);

    public abstract void drawChannelButtons(Client cl);

    public abstract void processChatModeClick(Client cl);

    public abstract void rightClickChatButtons(Client cl);

    public int getChatOffsetY(boolean producerOffset) {
        return 0;
    }

    public abstract Rectangle getChatBounds();

    public int chatLineSpacing() {
        return 3;
    }

    public abstract Rectangle getInvBounds();

    public abstract Point getMapOffset(boolean producerOffset);

    public abstract Point getMapImageOffset();

    public abstract boolean useOldScrollBar();

    public abstract Sprite getSideIcon(int index);
}
