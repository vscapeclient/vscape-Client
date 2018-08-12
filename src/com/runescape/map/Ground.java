package com.runescape.map;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import com.runescape.entity.GameObject;
import com.runescape.link.Node;
import com.runescape.map.object.GroundDecoration;
import com.runescape.map.object.Wall;
import com.runescape.map.object.WallDecoration;

public final class Ground extends Node {

	public Ground(int i, int j, int k)
	{
		obj5Array = new GameObject[5];
		anIntArray1319 = new int[5];
		anInt1310 = anInt1307 = i;
		anInt1308 = j;
		anInt1309 = k;
	}

	int anInt1307;
	final int anInt1308;
	final int anInt1309;
	final int anInt1310;
	public SimpleTile simpleGroundData;
	public ShapedTile shapedGroundData;
	public Wall obj1;
	public WallDecoration obj2;
	public GroundDecoration obj3;
	public GroundItem obj4;
	int anInt1317;
	public final GameObject[] obj5Array;
	final int[] anIntArray1319;
	int anInt1320;
	int anInt1321;
	boolean aBoolean1322;
	boolean aBoolean1323;
	boolean aBoolean1324;
	int anInt1325;
	int anInt1326;
	int anInt1327;
	int anInt1328;
	public Ground aClass30_Sub3_1329;
}
