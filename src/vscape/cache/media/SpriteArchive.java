package vscape.cache.media;

import com.runescape.cache.graphics.Sprite;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpriteArchive {

    private String name;
    private int totalSprites;
    private Sprite[] sprites = null;
    private SpriteArchive parentArchive;
    public final Map<String, SpriteArchive> sub_archives = new HashMap<>();


    public SpriteArchive(String name){
        this.name = name.toLowerCase();
    }

    public SpriteArchive(String name, SpriteArchive parent){
        this(name);
        this.parentArchive = parent;
    }

    public void readValues(DataInputStream index, DataInputStream data) throws IOException {
        totalSprites = index.readInt();
        sprites = new Sprite[totalSprites];
        for (int i = 0; i < totalSprites; i++) {
            int bufferLength = data.readInt();
            byte[] spriteData = new byte[bufferLength];
            data.readFully(spriteData);
            sprites[i] = new Sprite(spriteData);
        }
    }

    public Sprite getSprite(int spriteIndex){
        if(spriteIndex >= 0 && spriteIndex <= totalSprites) {
            return sprites[spriteIndex];
        }
        return null;
    }

    public SpriteArchive addSubArchive(final String sub_archive) {
        if(!isSubArchive()) {
            if (!sub_archives.containsKey(sub_archive)) {
                sub_archives.put(sub_archive, new SpriteArchive(sub_archive, this));
            }
            return sub_archives.get(sub_archive);
        }
        return null;
    }

    public SpriteArchive getSubArchive(final String sub_archive) {
        if(sub_archives.containsKey(sub_archive)) {
            return sub_archives.get(sub_archive);
        }
        return null;
    }

    public int getSubArchiveCount() {
        if(sub_archives != null) {
            return sub_archives.size();
        }
        return 0;
    }

    public SpriteArchive getParentArchive() {
        return parentArchive;
    }

    public boolean isSubArchive() {
        return getParentArchive() != null;
    }

    @Override
    public String toString() {
        return name;
    }
}

