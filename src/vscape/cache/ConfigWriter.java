package vscape.cache;

import com.runescape.cache.definitions.EntityDef;
import com.runescape.cache.definitions.ItemDef;
import com.runescape.cache.definitions.ObjectDef;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConfigWriter {
	
	public static String conDir =  "Configs/";

	public static void writeItemConfig()
	{
		try {
			final DataOutputStream dat = new DataOutputStream(new FileOutputStream(conDir+"obj.dat"));
			final DataOutputStream idx = new DataOutputStream(new FileOutputStream(conDir+"obj.idx"));
			idx.writeShort(ItemDef.totalItems);
			dat.writeShort(ItemDef.totalItems);
			for (int i = 0; i < ItemDef.totalItems; i++) {
				final ItemDef item = ItemDef.forID(i);
				final int offset1 = dat.size();
				if (item.modelID != 0) {
					dat.writeByte(1);
					dat.writeShort(item.modelID);
				}
				if (item.name != null) {
					dat.writeByte(2);
					writeString(dat, item.name);
				}
				if(item.description != null)
				{
					dat.writeByte(3);
					writeString(dat, new String(item.description));
				}
				if (item.modelZoom != 2000) {
					dat.writeByte(4);
					dat.writeShort(item.modelZoom);
				}
				if (item.modelRotationX != 0) {
					dat.writeByte(5);
					dat.writeShort(item.modelRotationX);
				}
				if (item.modelRotationY != 0) {
					dat.writeByte(6);
					dat.writeShort(item.modelRotationY);
				}
				if (item.modelOffsetX != 0) {
					dat.writeByte(7);
					dat.writeShort(item.modelOffsetX);
				}
				if (item.modelOffsetY != 0) {
					dat.writeByte(8);
					dat.writeShort(item.modelOffsetY);
				}
				if (item.stackable) {
					dat.writeByte(11);
				}
				if (item.value != 1) {
					dat.writeByte(12);
					dat.writeInt(item.value);
				}
				if (item.membersObject) {
					dat.writeByte(16);
				}
				if (item.maleEquip1 != -1) {
					dat.writeByte(23);
					dat.writeShort(item.maleEquip1);
					dat.writeByte(0);
				}
				if (item.maleEquip2 != -1) {
					dat.writeByte(24);
					dat.writeShort(item.maleEquip2);
				}
				if (item.femaleEquip1 != -1) {
					dat.writeByte(25);
					dat.writeShort(item.femaleEquip1);
					dat.writeByte(0);
				}
				if (item.femaleEquip2 != -1) {
					dat.writeByte(26);
					dat.writeShort(item.femaleEquip2);
				}
				if (item.groundActions != null) {
					for (int ii = 0; ii < item.groundActions.length; ii++) {
						if (item.groundActions[ii] == null) {
							continue;
						}
						dat.writeByte(30 + ii);
						writeString(dat, item.groundActions[ii]);
					}
				}
				if (item.itemActions != null) {
					for (int z = 0; z < item.itemActions.length; z++) {
						if (item.itemActions[z] == null) {
							continue;
						}
						dat.writeByte(35 + z);
						writeString(dat, item.itemActions[z]);
					}
				}
				if (item.originalModelColors != null) {
					dat.writeByte(40);
					dat.writeByte(item.originalModelColors.length);
					for (int ii = 0; ii < item.originalModelColors.length; ii++) {
						if(item.originalModelColors != null)
						dat.writeShort(item.originalModelColors[ii]);
						if( item.modifiedModelColors != null)
						dat.writeShort(item.modifiedModelColors[ii]);
					}
				}
				if (item.equipmentActions != null) {
					for (int z = 0; z < item.equipmentActions.length; z++) {
						if (item.equipmentActions[z] == null) {
							continue;
						}
						dat.writeByte(45 + z);
						writeString(dat, item.equipmentActions[z]);
					}
				}
				if (item.anInt185 != -1) {
					dat.writeByte(78);
					dat.writeShort(item.anInt185);
				}
				if (item.anInt162 != -1) {
					dat.writeByte(79);
					dat.writeShort(item.anInt162);
				}
				if (item.maleDialogueModel1 != -1) {
					dat.writeByte(90);
					dat.writeShort(item.maleDialogueModel1);
				}
				if (item.femaleDialogueModel1 != -1) {
					dat.writeByte(91);
					dat.writeShort(item.femaleDialogueModel1);
				}
				if (item.maleDialogueModel2 != -1) {
					dat.writeByte(92);
					dat.writeShort(item.maleDialogueModel2);
				}
				if (item.femaleDialogueModel2 != -1) {
					dat.writeByte(93);
					dat.writeShort(item.femaleDialogueModel2);
				}
				if (item.anInt204 != 0) {
					dat.writeByte(95);
					dat.writeShort(item.anInt204);
				}
				if (item.certID != -1) {
					dat.writeByte(97);
					dat.writeShort(item.certID);
				}
				if (item.certTemplateID != -1) {
					dat.writeByte(98);
					dat.writeShort(item.certTemplateID);
				}
				if (item.stackIDs != null) {
					for (int ii = 0; ii < item.stackIDs.length; ii++) {
						dat.writeByte(100 + ii);
						dat.writeShort(item.stackIDs[ii]);
						dat.writeShort(item.stackAmounts[ii]);
					}
				}
				if (item.anInt167 != 128) {
					dat.writeByte(110);
					dat.writeShort(item.anInt167);
				}
				if (item.anInt192 != 128) {
					dat.writeByte(111);
					dat.writeShort(item.anInt192);
				}
				if (item.anInt191 != 128) {
					dat.writeByte(112);
					dat.writeShort(item.anInt191);
				}
				if (item.anInt196 != 0) {
					dat.writeByte(113);
					dat.writeByte(item.anInt196);
				}
				if (item.anInt184 != 0) {
					dat.writeByte(114);
					dat.writeByte(item.anInt184 / 5);
				}
				if (item.team != 0) {
					dat.writeByte(115);
					dat.writeByte(item.team);
				}
				dat.writeByte(0);
				final int offset2 = dat.size();
				final int writeOffset = offset2 - offset1;
				idx.writeShort(writeOffset);
			}
			dat.close();
			idx.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
    public static void writeObjectsConfig() {
        try {
            DataOutputStream dat = new DataOutputStream(new FileOutputStream(conDir+"loc.dat"));
            DataOutputStream idx = new DataOutputStream(new FileOutputStream(conDir+"loc.idx"));
            idx.writeShort(ObjectDef.totalObjects);
            //dat.writeShort(ObjectDef.totalObjects);

            for (int index = 0; index < ObjectDef.totalObjects; index++) {
                ObjectDef obj = ObjectDef.forID(index);
                int offset1 = dat.size();

                if (obj.modelIds != null) {
                    if (obj.modelTypes != null) {
                        dat.writeByte(1);
                        dat.writeByte(obj.modelIds.length);
                        if (obj.modelIds.length > 0) {
                            for (int i = 0; i < obj.modelIds.length; i++) {
                                dat.writeShort(obj.modelIds[i]);
                                dat.writeByte(obj.modelTypes[i]);
                            }
                        }
                    } else {
                        dat.writeByte(5);
                        dat.writeByte(obj.modelIds.length);
                        if (obj.modelIds.length > 0) {
                            for (int i = 0; i < obj.modelIds.length; i++) {
                                dat.writeShort(obj.modelIds[i]);
                            }
                        }
                    }
                }

                if (obj.name != null) {
                    dat.writeByte(2);
                    writeString(dat, obj.name);
                }

                if (obj.description != null) {
                    dat.writeByte(3);
                    dat.write(obj.description);
                }

                if (obj.width != 1) {
                    dat.writeByte(14);
                    dat.writeByte(obj.width);
                }

                if (obj.length != 1) {
                    dat.writeByte(15);
                    dat.writeByte(obj.length);
                }

                if (!obj.solid) {
                    dat.writeByte(17);
                }

                if (!obj.impenetrable) {
                    dat.writeByte(18);
                }

                if (obj.hasActions) {
                    dat.writeByte(19);
                    dat.writeByte(1);
                }else{
                    dat.writeByte(19);
                    dat.writeByte(0);
                }

                if (obj.contouredGround) {
                    dat.writeByte(21);
                }

                if (obj.delayShading) {
                    dat.writeByte(22);
                }

                if (obj.occludes) {
                    dat.writeByte(23);
                }

                if (obj.animation != -1) {
                    dat.writeByte(24);
                    dat.writeShort(obj.animation);
                }

                if (obj.decorDisplacement != 16) {
                    dat.writeByte(28);
                    dat.writeByte(obj.decorDisplacement);
                }

                if (obj.ambientLighting != 0) {
                    dat.writeByte(29);
                    dat.writeByte(obj.ambientLighting);
                }

                if (obj.lightDiffusion != 0) {
                    dat.writeByte(39);
                    dat.writeByte(obj.lightDiffusion);
                }

                if (obj.actions != null) {
                    for (int i = 0; i < obj.actions.length; i++) {
                        dat.writeByte(30 + i);
                        if (obj.actions[i] != null) {
                            writeString(dat, obj.actions[i]);
                        }else{
                        	writeString(dat, "hidden");
                        }
                    }
                }

                if (obj.modifiedModelColors != null || obj.originalModelColors != null) {
                    dat.writeByte(40);
                    dat.writeByte(obj.modifiedModelColors.length);
                    for (int i = 0; i < obj.modifiedModelColors.length; i++) {
                        dat.writeShort(obj.modifiedModelColors[i]);
                        dat.writeShort(obj.originalModelColors[i]);
                    }
                }

                if (obj.minimapFunction != -1) {
                    dat.writeByte(60);
                    dat.writeShort(obj.minimapFunction);
                }

                if (obj.inverted) {
                    dat.writeByte(62);
                }

                if (!obj.castsShadow) {
                    dat.writeByte(64);
                }

                if (obj.scaleX != 128) {
                    dat.writeByte(65);
                    dat.writeShort(obj.scaleX);
                }

                if (obj.scaleY != 128) {
                    dat.writeByte(66);
                    dat.writeShort(obj.scaleY);
                }

                if (obj.scaleZ != 128) {
                    dat.writeByte(67);
                    dat.writeShort(obj.scaleZ);
                }

                if (obj.mapscene != -1) {
                    dat.writeByte(68);
                    dat.writeShort(obj.mapscene);
                }

                if (obj.surroundings != 0) {
                    dat.writeByte(69);
                    dat.writeByte(obj.surroundings);
                }

                if (obj.translateX != 0) {
                    dat.writeByte(70);
                    dat.writeShort(obj.translateX);
                }

                if (obj.translateY != 0) {
                    dat.writeByte(71);
                    dat.writeShort(obj.translateY);
                }

                if (obj.translateZ != 0) {
                    dat.writeByte(72);
                    dat.writeShort(obj.translateZ);
                }

                if (obj.obstructsGround) {
                    dat.writeByte(73);
                }

                if (obj.hollow) {
                    dat.writeByte(74);
                }

                if (obj.supportItems != -1) {
                    dat.writeByte(75);
                    dat.writeByte(obj.supportItems);
                }

                if (obj.varbit != -1 || obj.varp != -1 || obj.childrenIDs != null) {
                    dat.writeByte(77);
                    dat.writeShort(obj.varbit);
                    dat.writeShort(obj.varp);
                    dat.writeByte(obj.childrenIDs.length - 1);
                    for (int i = 0; i < obj.childrenIDs.length; i++) {
                        dat.writeShort(obj.childrenIDs[i]);
                    }
                }
                dat.writeByte(0);
                int offset2 = dat.size();
                int writeOffset = offset2 - offset1;
                idx.writeShort(writeOffset);
            }
            dat.close();
            idx.close();
            System.out.println("Finished writing.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void writeNpcConfig() {
        try {
            DataOutputStream dat = new DataOutputStream(new FileOutputStream(conDir+"npc.dat"));
            DataOutputStream idx = new DataOutputStream(new FileOutputStream(conDir+"npc.idx"));
            idx.writeShort(EntityDef.totalNPCs);
            dat.writeShort(EntityDef.totalNPCs);

            for (int index = 0; index < EntityDef.totalNPCs; index++) {
                EntityDef npc = EntityDef.forID(index);
                int offset1 = dat.size();
                
                if (npc.modelID != null) {
                	dat.writeByte(1);
                	dat.writeByte(npc.modelID.length);
                	for (int id : npc.modelID) {
                		dat.writeShort(id);
                	}
                }
                if (npc.name != null) {
                	dat.writeByte(2);
                	writeNewString(dat, npc.name);
                }
                if (npc.description != null) {
                	dat.writeByte(3);
                	writeString(dat, new String(npc.description));
                }
                if (npc.size != 1) {
                	dat.writeByte(12);
                	dat.writeByte(npc.size);
                }
                if (npc.standAnim > -1) {
                	dat.writeByte(13);
                	if (npc.standAnim >= 65535) {
                		System.out.println("npc#" + index);
                	}
                	dat.writeShort(npc.standAnim);
                }
                if (npc.walkAnim > -1) {
                	if (npc.turn180Anim <= -1 || npc.turn90CWAnim <= -1 || npc.turn90CCWAnim <= -1) {
                		dat.writeByte(14);
                		dat.writeShort(npc.walkAnim);
                	} else {
                		dat.writeByte(17);
                		dat.writeShort(npc.walkAnim);
                		dat.writeShort(npc.turn180Anim);
                		dat.writeShort(npc.turn90CWAnim);
                		dat.writeShort(npc.turn90CCWAnim);
                	}
                }
                if (npc.actions != null) {
                	if (npc.actions.length > 5) {
                		System.out.println(index);
                	}
                	for (int i = 0; i < npc.actions.length; i++) {
                		dat.writeByte(30 + i);
                		if (npc.actions[i] == null) {
                			writeNewString(dat, "hidden");
                		} else {
                			writeNewString(dat, npc.actions[i]);
                		}
                	}
                }
                if (npc.originalModelColors != null) {
                	dat.writeByte(40);
                	dat.writeByte(npc.originalModelColors.length);
                	for (int i = 0; i < npc.originalModelColors.length; i++) {
                		dat.writeShort(npc.originalModelColors[i]);
                		dat.writeShort(npc.modifiedModelColors[i]);
                	}
                }
                if (npc.additionalModels != null) {
                	dat.writeByte(60);
                	dat.writeByte(npc.additionalModels.length);
                	for (int id : npc.additionalModels) {
                		dat.writeShort(id);
                	}
                }
                //90-92? Unknown?
                if (!npc.drawMapDot) {
                	dat.writeByte(93);
                }
                if (npc.combatLevel > -1) {
                	dat.writeByte(95);
                	dat.writeShort(npc.combatLevel);
                }
                if (npc.scaleXZ != 128) {
                	dat.writeByte(97);
                	dat.writeShort(npc.scaleXZ);
                }
                if (npc.scaleY != 128) {
                	dat.writeByte(98);
                	dat.writeShort(npc.scaleY);
                }
                if (npc.priorityRender) {
                	dat.writeByte(99);
                }
                if (npc.lightModifier != 0) {
                	dat.writeByte(100);
                	dat.writeByte(npc.lightModifier);
                }
                if (npc.shadowModifier != 0) {
                	dat.writeByte(101);
                	dat.writeByte(npc.shadowModifier / 5);
                }
                if (npc.headIcon > -1) {
                	dat.writeByte(102);
                	dat.writeShort(npc.headIcon);
                }
                if (npc.degreesToTurn != 32) {
                	dat.writeByte(103);
                	dat.writeShort(npc.degreesToTurn);
                }
                if (npc.childrenIDs != null) {
                	dat.writeByte(106);
                	dat.writeShort(npc.varBitID);
                	dat.writeShort(npc.settingId);
                	dat.writeByte(npc.childrenIDs.length - 1);
                	for (int id : npc.childrenIDs) {
                		dat.writeShort(id);
                	}
                }
                if (!npc.hasActions) {
                	dat.writeByte(107);
                }
                dat.writeByte(0);
                final int offset2 = dat.size();
    			final int writeOffset = offset2 - offset1;
    			idx.writeShort(writeOffset);
            }
            
			dat.close();
            idx.close();
            System.out.println("Finished writing.");
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
	
    public static void writeString(DataOutputStream dos, String input) throws IOException {
        dos.write(input.getBytes());
        dos.writeByte(10);
    }
    
    public static void writeNewString(DataOutputStream dos, String input) throws IOException {
        dos.write(input.getBytes());
        dos.writeByte(0);
    }

    public static void writeDWordBigEndian(DataOutputStream dat, int i) throws IOException {
        dat.write((byte) (i >> 16));
        dat.write((byte) (i >> 8));
        dat.write((byte) (i >> 8));
    }
    
    public static void writeDWord(DataOutputStream dat, int i) throws IOException {
        dat.writeInt(i);
    }
}