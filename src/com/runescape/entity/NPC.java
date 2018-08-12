package com.runescape.entity;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import com.runescape.cache.anim.Animation;
import com.runescape.cache.anim.Frame;
import com.runescape.cache.anim.SpotAnim;
import com.runescape.cache.definitions.EntityDef;
import com.runescape.entity.model.Model;

public final class NPC extends Entity
{

	private Model method450()
	{
		if(super.anim >= 0 && super.anInt1529 == 0)
		{
			int k = Animation.anims[super.anim].anIntArray353[super.anInt1527];
			int i1 = -1;
			if(super.anInt1517 >= 0 && super.anInt1517 != super.standAnim)
				i1 = Animation.anims[super.anInt1517].anIntArray353[super.anInt1518];
			return desc.method164(i1, k, Animation.anims[super.anim].anIntArray357);
		}
		int l = -1;
		if(super.anInt1517 >= 0)
			l = Animation.anims[super.anInt1517].anIntArray353[super.anInt1518];
		return desc.method164(-1, l, null);
	}

	public Model getRotatedModel()
	{
		if(desc == null)
			return null;
		Model model = method450();
		if(model == null)
			return null;
		super.height = model.modelHeight;
		if(super.gfxId != -1 && super.anInt1521 != -1)
		{
			SpotAnim spotAnim = SpotAnim.cache[super.gfxId];
			Model model_1 = spotAnim.getModel();
			if(model_1 != null)
			{
				int j = spotAnim.animationSequence.anIntArray353[super.anInt1521];
				Model model_2 = new Model(true, Frame.method532(j), false, model_1);
				model_2.method475(0, -super.anInt1524, 0);
				model_2.method469();
				model_2.method470(j);
				model_2.anIntArrayArray1658 = null;
				model_2.anIntArrayArray1657 = null;
				if(spotAnim.resizeXY != 128 || spotAnim.resizeZ != 128)
					model_2.method478(spotAnim.resizeXY, spotAnim.resizeXY, spotAnim.resizeZ);
				model_2.method479(64 + spotAnim.modelBrightness, 850 + spotAnim.modelShadow, -30, -50, -30, true);
				Model aModel[] = {
						model, model_2
				};
				model = new Model(aModel);
			}
		}
		if(desc.size == 1)
			model.aBoolean1659 = true;
		return model;
	}

	public boolean isVisible()
	{
		return desc != null;
	}

	public NPC()
	{
	}

	public EntityDef desc;
}
