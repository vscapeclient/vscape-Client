package com.runescape.entity;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import com.runescape.cache.definitions.ItemDef;
import com.runescape.entity.model.Model;

final public class Item extends Renderable {

	public final Model getRotatedModel()
	{
		ItemDef itemDef = ItemDef.forID(ID);
			return itemDef.method201(itemCount);
	}

	public Item()
	{
	}

	public int ID;
	public int x;
	public int y;
	public int itemCount;
}
