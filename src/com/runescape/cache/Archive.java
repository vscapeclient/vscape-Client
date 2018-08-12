package com.runescape.cache;

import com.runescape.io.Stream;

final public class Archive {
	
	private final byte[] archiveBuffer;
	private final int dataSize;
	private final int[] nameHashes;
	private final int[] uncompressedSizes;
	private final int[] compressedSizes;
	private final int[] startOffsets;
	private final boolean compressed;

	public Archive(byte[] dataBuffer)
	{
		Stream stream = new Stream(dataBuffer);
		int i = stream.read3Bytes();
		int j = stream.read3Bytes();
		if(j != i)
		{
			byte abyte1[] = new byte[i];
			BZip2Decompressor.decompress(abyte1, i, dataBuffer, j, 6);
			archiveBuffer = abyte1;
			stream = new Stream(archiveBuffer);
			compressed = true;
		} else
		{
			archiveBuffer = dataBuffer;
			compressed = false;
		}
		dataSize = stream.readUnsignedShort();
		nameHashes = new int[dataSize];
		uncompressedSizes = new int[dataSize];
		compressedSizes = new int[dataSize];
		startOffsets = new int[dataSize];
		int k = stream.currentOffset + dataSize * 10;
		for(int l = 0; l < dataSize; l++)
		{
			nameHashes[l] = stream.readDWord();
			uncompressedSizes[l] = stream.read3Bytes();
			compressedSizes[l] = stream.read3Bytes();
			startOffsets[l] = k;
			k += compressedSizes[l];
		}
	}

	public byte[] getFile(String file)
	{
		byte dataBuffer[] = null; //was a parameter
		int i = 0;
		file = file.toUpperCase();
		for(int j = 0; j < file.length(); j++)
			i = (i * 61 + file.charAt(j)) - 32;

		for(int k = 0; k < dataSize; k++)
			if(nameHashes[k] == i)
			{
				if(dataBuffer == null)
					dataBuffer = new byte[uncompressedSizes[k]];
				if(!compressed) {
					BZip2Decompressor.decompress(dataBuffer, uncompressedSizes[k], archiveBuffer, compressedSizes[k], startOffsets[k]);
				} else {
					System.arraycopy(archiveBuffer, startOffsets[k], dataBuffer, 0, uncompressedSizes[k]);
				}
				return dataBuffer;
			}

		return null;
	}
}
