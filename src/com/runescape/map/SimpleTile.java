package com.runescape.map;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 


final public class SimpleTile
{

	public SimpleTile(int i, int j, int k, int l, int i1, int j1, boolean flag)
	{
		flat = true;
		centreColour = i;
		eastColour = j;
		northEastColour = k;
		northColour = l;
		texture = i1;
		anInt722 = j1;
		flat = flag;
	}

	public final int centreColour;
	public final int eastColour;
	public final int northEastColour;
	public final int northColour;
	public final int texture;
	public boolean flat;
	public final int anInt722;
}
