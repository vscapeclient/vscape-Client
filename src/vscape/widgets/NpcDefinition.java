package vscape.widgets;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class NpcDefinition {
	
	public static final transient int MAX_DEFINITIONS = 6403;
	private static transient NpcDefinition[] definitions = new NpcDefinition[MAX_DEFINITIONS + 1];
	
	private int id;
	private String name, examine;
	private int size = 1, standAnim = 808, walkAnim = 819, walkRange = 4;
	private boolean canWalk = true;
	private boolean canFollow = true;

	public static void init() throws IOException {
		//List<NpcDefinition> defs = (List<NpcDefinition>) XStreamUtil.getxStream().fromXML(new FileInputStream("data/npcs/npcDefinitions.xml"));
		FileReader reader = new FileReader("./data/npcs/npcDefinitions.json");
		try {
			List<NpcDefinition> defs = new Gson().fromJson(reader, new TypeToken<List<NpcDefinition>>() {} .getType());
			for (final NpcDefinition def : defs) {
				if (def.getId() > MAX_DEFINITIONS) {
					break;
				}
				definitions[def.getId()] = def;
			}
			reader.close();
			System.out.println("Loaded " + defs.size() + " npc definitions json.");
//			Code to write / update npcDefinitions
			/*String json = new GsonBuilder().setPrettyPrinting().create().toJson(defs);
			try {
				FileWriter writer = new FileWriter("./data/npcs/npcDefinitions2.json");
				writer.write(json);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		} catch (IOException e) {
			reader.close();
			System.err.println("failed to load npc definitions json.");
		}

	}
	
	public NpcDefinition() { }

	public static NpcDefinition[] getDefinitions() {
		return definitions;
	}

	public static NpcDefinition forName(String name) {
		if (name == null) {
			return null;
		}
		for (NpcDefinition d : getDefinitions()) {
			if (d != null && d.getName().toLowerCase().equalsIgnoreCase(name.toLowerCase())) {
				return d;
			}
		}
		return null;
	}

	public static NpcDefinition forId(int id) {
		if (id < 0 || id >= definitions.length)
			return produceDefinition(id);
		NpcDefinition d = getDefinitions()[id];
		if (d == null) {
			d = produceDefinition(id);
		}
		return d;
	}
	
	public static NpcDefinition produceDefinition(int id) {
		final NpcDefinition def = new NpcDefinition();
		def.id = id;
		def.name = "NPC #" + def.id;
		def.examine = "It's an NPC.";
		return def;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getExamine() {
		return examine;
	}

	public int getStandAnim() {
		return standAnim;
	}

	public int getWalkAnim() {
		return walkAnim;
	}
	
	public int getWalkRange() {
		return walkRange;
	}
	
	public int getSize() {
		return size;
	}

	public boolean canWalk() {
		return canWalk;
	}

	public boolean canFollow() {
		return canFollow;
	}
}
