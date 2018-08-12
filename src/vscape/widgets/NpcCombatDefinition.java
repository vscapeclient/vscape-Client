package vscape.widgets;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class NpcCombatDefinition {

	public int npcId;
	public String name;
	private int spawnDelay;
	private int combat = 0;
	private int hitpoints = 1;
	private int maxHit = 0;
	private int aggressionTime = 1200;
	private int attackSpeed = 4000;
	private int attackAnim = 422;
	private int blockAnim = 404;
	private int deathAnim = 2304;
	private int deathAnimLength = 6;
	
	private int[] combatStats, attackBonuses, defenceBonuses, otherBonuses;
	
	private int attackSound = 417;
	private int blockSound = 381;
	private int damageSound = 69;
	private int deathSound = 70;
	
	private boolean attackable = false;
	private boolean aggressive = false;
	private boolean retaliates = true;
	private boolean retreats = false;
	private boolean poisonous = false;
	private boolean poisonImmune = false;
	private String magicWeakness = "NONE";
	
	private transient boolean reactToProtectionPrayers;
	
	private static transient NpcCombatDefinition[] definitions = new NpcCombatDefinition[NpcDefinition.MAX_DEFINITIONS + 1];
	
	public static void init() throws IOException {
		FileReader reader = new FileReader("./data/npcs/npcCombatDefs.json");
		int combatScripts = 0;
		try {
			List<NpcCombatDefinition> defs = new Gson().fromJson(reader, new TypeToken<List<NpcCombatDefinition>>() {} .getType());
			for (final NpcCombatDefinition def : defs) {
				if (def.npcId > NpcDefinition.MAX_DEFINITIONS) {
					break;
				}
				definitions[def.npcId] = def;

				if (def.npcId != 1974 && def.npcId != 1975 && def.npcId != 1977 && !(def.npcId >= 1290 && def.npcId <= 1293)) {
					//TODO: change the stats on these fuckers in the defs to whatever they were on the old
					/*
					def.attackBonus = (int) (def.combat);
					def.defenceMelee = (int) (def.combat / 2);
					def.defenceMage = (int) (def.combat / 2);
					def.defenceRange = (int) (def.combat / 2);
					*/
				}
				
				/*
				//Code to set a weakness in tangent with writing a new NpcCombatDefinition
				if(def.name.equals("Fire giant")) {
					def.magicWeakness = "WATER";
				}
				*/
			}
//			Code to write / update npcDefinitions
//			String json = new GsonBuilder().setPrettyPrinting().create().toJson(defs);
//			try {
//				FileWriter writer = new FileWriter("./data/npcs/npcCombatDefs2.json");
//				writer.write(json);
//				writer.close();
//
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			reader.close();
			System.out.println("Loaded " + defs.size() + " npc combat definitions.");
			System.out.println("Loaded " + combatScripts + " npc combat scripts.");
		} catch (IOException e) {
			reader.close();
			System.err.println("Failed to load npc combat definitions json.");
		}
	}
	
	public static NpcCombatDefinition[] getDefinitions() {
		return definitions;
	}
	
	public static NpcCombatDefinition forId(int id) {
		NpcCombatDefinition d = getDefinitions()[id];
		if (d == null) {
			d = produceDefinition(id);
		}
		return d;
	}
	
	public static NpcCombatDefinition produceDefinition(int id) {
		final NpcCombatDefinition def = new NpcCombatDefinition();
		def.npcId = id;
		def.name = "NPC #" + def.npcId;
		return def;
	}
	
	public int getNpcId() {
		return npcId;
	}
	
	public void setNpcId(int id) {
		this.npcId = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String set) {
		this.name = set;
	}

	public int getSpawnDelay() {
		return spawnDelay;
	}
	
	public void setSpawnDelay(int set) {
		this.spawnDelay = set;
	}
	
	public int[] getCombatStats() {
		return combatStats;
	}
	
	public int[] getAttackBonuses() {
		return attackBonuses;
	}
	
	public int[] getDefenceBonuses() {
		return defenceBonuses;
	}
	
	public int[] getOtherBonuses() {
		return otherBonuses;
	}
	
	public void setCombatStats(int[] stats) {
		this.combatStats = stats;
	}
	
	public void setAttackBonuses(int[] stats) {
		this.attackBonuses = stats;
	}
	
	public void setDefenceBonuses(int[] stats) {
		this.defenceBonuses = stats;
	}
	
	public void setOtherBonuses(int[] stats) {
		this.otherBonuses = stats;
	}
	
	public int getDeathAnim() {
		return deathAnim;
	}
	
	public void setDeathAnim(int set) {
		this.deathAnim = set;
	}
	
	public int getDeathAnimLength() {
		return deathAnimLength;
	}
	
	public void setDeathAnimLength(int length) {
		this.deathAnimLength = length;
	}

	public int getBlockAnim() {
		return blockAnim;
	}
	
	public void setBlockAnim(int set) {
		this.blockAnim = set;
	}

	public int getAttackAnim() {
		return attackAnim;
	}
	
	public void setAttackAnim(int set) {
		this.attackAnim = set;
	}

	public int getAttackSound() {
		return attackSound;
	}
	
	public void setAttackSound(int set) {
		this.attackSound = set;
	}

	public int getBlockSound() {
		return blockSound;
	}
	
	public void setBlockSound(int set) {
		this.blockSound = set;
	}

	public int getDamageSound() {
		return damageSound;
	}
	
	public void setDamageSound(int set) {
		this.damageSound = set;
	}

	public int getDeathSound() {
		return deathSound;
	}
	
	public void setDeathSound(int set) {
		this.deathSound = set;
	}

	public int getCombat() {
		return combat;
	}
	
	public void setCombat(int set) {
		this.combat = set;
	}

	public boolean isAggressive() {
		return aggressive;
	}
	
	public void setIsAggressive(boolean set) {
		this.aggressive = set;
	}

	public boolean shouldRetreat() {
		return retreats;
	}
	
	public void setShouldRetreat(boolean set) {
		this.retreats = set;
	}

	public boolean isPoisonous() {
		return poisonous;
	}
	
	public void setIsPoisonous(boolean set) {
		this.poisonous = set;
	}

	public boolean isPoisonImmune() {
		return poisonImmune;
	}
	
	public void setIsPoisonImmune(boolean set) {
		this.poisonImmune = set;
	}

	public String getMagicWeakness() {
		return magicWeakness;
	}
	
	public void setMagicWeakness(String set) {
		this.magicWeakness = set;
	}

	public int getHitpoints() {
		return hitpoints;
	}
	
	public void setHitpoints(int set) {
		this.hitpoints = set;
	}

	public int getMaxHit() {
		return maxHit;
	}
	
	public void setMaxHit(int set) {
		this.maxHit = set;
	}

	public int getAggressiveTime() {
		return aggressionTime;
	}
	
	public void setAggressiveTime(int set) {
		this.aggressionTime = set;
	}
	
	public int getAttackSpeed() {
		return attackSpeed;
	}
	
	public void setAttackSpeed(int set) {
		this.attackSpeed = set;
	}

	public boolean isAttackable() {
		return attackable;
	}
	
	public void setIsAttackable(boolean set) {
		this.attackable = set;
	}

	public boolean shouldRetaliate() {
		return retaliates;
	}
	
	public void setShouldRetaliate(boolean set) {
		this.retaliates = set;
	}
	
	public void setReactToProtectionPrayers(boolean set) {
		this.reactToProtectionPrayers = set;
	}

	public boolean reactToProtectionPrayers() {
		return this.reactToProtectionPrayers;
	}


}
