package com.runescape.entity;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import com.runescape.cache.anim.Animation;

public class Entity extends Renderable {

	public final void setPos(int i, int j, boolean flag)
	{
		if(anim != -1 && Animation.anims[anim].anInt364 == 1)
			anim = -1;
		if(!flag)
		{
			int k = i - smallX[0];
			int l = j - smallY[0];
			if(k >= -8 && k <= 8 && l >= -8 && l <= 8)
			{
				if(smallXYIndex < 9)
					smallXYIndex++;
				for(int i1 = smallXYIndex; i1 > 0; i1--)
				{
					smallX[i1] = smallX[i1 - 1];
					smallY[i1] = smallY[i1 - 1];
					aBooleanArray1553[i1] = aBooleanArray1553[i1 - 1];
				}

				smallX[0] = i;
				smallY[0] = j;
				aBooleanArray1553[0] = false;
				return;
			}
		}
		smallXYIndex = 0;
		anInt1542 = 0;
		anInt1503 = 0;
		smallX[0] = i;
		smallY[0] = j;
		x = smallX[0] * 128 + size * 64;
		y = smallY[0] * 128 + size * 64;
	}

	public final void method446()
	{
		smallXYIndex = 0;
		anInt1542 = 0;
	}

	public final void updateHitData(int j, int k, int l)
	{
		for(int i1 = 0; i1 < 4; i1++)
			if(hitsLoopCycle[i1] <= l)
			{
				hitArray[i1] = k;
				hitMarkTypes[i1] = j;
				hitsLoopCycle[i1] = l + 70;
				return;
			}
	}

	public final void moveInDir(boolean flag, int i)
	{
		int j = smallX[0];
		int k = smallY[0];
		if(i == 0)
		{
			j--;
			k++;
		}
		if(i == 1)
			k++;
		if(i == 2)
		{
			j++;
			k++;
		}
		if(i == 3)
			j--;
		if(i == 4)
			j++;
		if(i == 5)
		{
			j--;
			k--;
		}
		if(i == 6)
			k--;
		if(i == 7)
		{
			j++;
			k--;
		}
		if(anim != -1 && Animation.anims[anim].anInt364 == 1)
			anim = -1;
		if(smallXYIndex < 9)
			smallXYIndex++;
		for(int l = smallXYIndex; l > 0; l--)
		{
			smallX[l] = smallX[l - 1];
			smallY[l] = smallY[l - 1];
			aBooleanArray1553[l] = aBooleanArray1553[l - 1];
		}
			smallX[0] = j;
			smallY[0] = k;
			aBooleanArray1553[0] = flag;
	}

	public int entScreenX;
	public int entScreenY;
	public final int index = -1;
	public boolean isVisible()
	{
		return false;
	}

	Entity()
	{
		smallX = new int[10];
		smallY = new int[10];
		interactingEntity = -1;
		degreesToTurn = 32;
		anInt1505 = -1;
		height = 200;
		standAnim = -1;
		anInt1512 = -1;
		hitArray = new int[4];
		hitMarkTypes = new int[4];
		hitsLoopCycle = new int[4];
		anInt1517 = -1;
		gfxId = -1;
		anim = -1;
		loopCycleStatus = -1000;
		textCycle = 100;
		size = 1;
		aBoolean1541 = false;
		aBooleanArray1553 = new boolean[10];
		walkAnim = -1;
		turn180Anim = -1;
		turn90CWAnim = -1;
		turn90CCWAnim = -1;
	}

	public final int[] smallX;
	public final int[] smallY;
	public int interactingEntity;
	public int anInt1503;
	public int degreesToTurn;
	public int anInt1505;
	public String textSpoken;
	public int height;
	public int turnDirection;
	public int standAnim;
	public int anInt1512;
	public int textColour;
	public final int[] hitArray;
	public final int[] hitMarkTypes;
	public final int[] hitsLoopCycle;
	public int anInt1517;
	public int anInt1518;
	public int anInt1519;
	public int gfxId;
	public int anInt1521;
	public int anInt1522;
	public int anInt1523;
	public int anInt1524;
	public int smallXYIndex;
	public int anim;
	public int anInt1527;
	public int anInt1528;
	public int anInt1529;
	public int anInt1530;
	public int textEffect;
	public int loopCycleStatus;
	public int currentHealth;
	public int maxHealth;
	public int textCycle;
	public int anInt1537;
	public int anInt1538;
	public int anInt1539;
	public int size;
	public boolean aBoolean1541;
	public int anInt1542;
	public int anInt1543;
	public int anInt1544;
	public int anInt1545;
	public int anInt1546;
	public int startForceMovement;
	public int endForceMovement;
	public int direction;
	public int x;
	public int y;
	public int face;
	public final boolean[] aBooleanArray1553;
	public int walkAnim;
	public int turn180Anim;
	public int turn90CWAnim;
	public int turn90CCWAnim;
}
