package com.runescape.setting;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import com.runescape.cache.Archive;
import com.runescape.io.Stream;

public final class VarBit {

	public static void unpackConfig(Archive streamLoader)
	{
		Stream stream = new Stream(streamLoader.getFile("varbit.dat"));
		int cacheSize = stream.readUnsignedShort();
		if(cache == null)
			cache = new VarBit[cacheSize];
		for(int j = 0; j < cacheSize; j++)
		{
			if(cache[j] == null)
				cache[j] = new VarBit();
			cache[j].readValues(stream);
		}

		if(stream.currentOffset != stream.buffer.length)
			System.out.println("varbit load mismatch");
	}

	private void readValues(Stream stream)
	{
		configId = stream.readUnsignedShort();
		leastSignificantBit = stream.readUnsignedByte();
		mostSignificantBit = stream.readUnsignedByte();
	}

	private VarBit()
	{
	}

	public static VarBit cache[];
	public int configId;
	public int leastSignificantBit;
	public int mostSignificantBit;
}
