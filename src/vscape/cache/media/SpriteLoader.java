package vscape.cache.media;

import com.runescape.cache.Archive;
import com.runescape.cache.graphics.Sprite;
import com.runescape.io.DataUtils;
import com.runescape.io.Stream;
import com.runescape.sign.Signlink;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;


public class SpriteLoader {

	public static final Map<String, SpriteArchive> cache = new HashMap<>();

	public static void loadSprites(Archive archive) {
		try {
			Stream index = new Stream(DataUtils.readFile(Signlink.findcachedir() + "sprites.idx"));
			Stream data = new Stream(DataUtils.readFile(Signlink.findcachedir() + "sprites.dat"));
			DataInputStream indexFile = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(index.buffer)));
			DataInputStream dataFile = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(data.buffer)));
			int totalArchives = indexFile.readInt();
			for (int i = 0; i < totalArchives; i++) {
				readArchive(indexFile, dataFile, null);
			}
			indexFile.close();
			dataFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readArchive(DataInputStream idx, DataInputStream dat, SpriteArchive parentArchive) throws IOException {
		String name = idx.readUTF();
		SpriteArchive sa = parentArchive == null ? new SpriteArchive(name) : parentArchive.addSubArchive(name);
		sa.readValues(idx, dat);
		if(parentArchive == null && !sa.isSubArchive()) {
			int sub_archive_count = idx.readInt();
			for (int i = 0; i < sub_archive_count; i++) {
				readArchive(idx, dat, sa);
			}
			if (!cache.containsKey(name)) {
				cache.put(name, sa);
			}
		}
	}
	
	public static SpriteArchive getArchive(String archive){
		if(cache.containsKey(archive)) {
			return cache.get(archive);
		}
		return null;
	}

	public static SpriteArchive getSubArchive(String archive, String sub_archive){
		SpriteArchive sa = getArchive(archive);
		if(sa != null) {
			return sa.getSubArchive(sub_archive);
		}
		return null;
	}

	public static Sprite getSprite(String archive, int spriteIndex){
		SpriteArchive sa = getArchive(archive);
		if(sa != null) {
			return sa.getSprite(spriteIndex);
		}
		return null;
	}

	public static Sprite getSprite(String archive, String sub_archive, int spriteIndex){
		SpriteArchive sa = getSubArchive(archive, sub_archive);
		if(sa != null) {
			return sa.getSprite(spriteIndex);
		}
		return null;
	}
}
