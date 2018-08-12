package com.runescape.cache;

import java.io.IOException;
import java.io.RandomAccessFile;

final public class Index {
	
	private static final byte[] buffer = new byte[520];
	private final RandomAccessFile dataFile;
	private final RandomAccessFile indexFile;
	private final int storeId;

	public Index(RandomAccessFile randomaccessfile, RandomAccessFile randomaccessfile1, int j) {
		storeId = j;
		dataFile = randomaccessfile;
		indexFile = randomaccessfile1;
	}

	public synchronized byte[] get(int i) {
		try {
			seek(indexFile, i * 6);
			int l;
			for(int j = 0; j < 6; j += l)
			{
				l = indexFile.read(buffer, j, 6 - j);
				if(l == -1)
					return null;
			}
			int i1 = ((buffer[0] & 0xff) << 16) + ((buffer[1] & 0xff) << 8) + (buffer[2] & 0xff);
			int j1 = ((buffer[3] & 0xff) << 16) + ((buffer[4] & 0xff) << 8) + (buffer[5] & 0xff);
			//if(i1 < 0 || i1 > 0xffffff)
			//	return null;
			if(j1 <= 0 || (long)j1 > dataFile.length() / 520L)
				return null;
			byte abyte0[] = new byte[i1];
			int k1 = 0;
			for(int l1 = 0; k1 < i1; l1++) {
				if(j1 == 0)
					return null;
				seek(dataFile, j1 * 520);
				int k = 0;
				int i2 = i1 - k1;
				if(i2 > 512)
					i2 = 512;
				int j2;
				for(; k < i2 + 8; k += j2) {
					j2 = dataFile.read(buffer, k, (i2 + 8) - k);
					if(j2 == -1)
						return null;
				}
				int k2 = ((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff);
				int l2 = ((buffer[2] & 0xff) << 8) + (buffer[3] & 0xff);
				int i3 = ((buffer[4] & 0xff) << 16) + ((buffer[5] & 0xff) << 8) + (buffer[6] & 0xff);
				int j3 = buffer[7] & 0xff;
				if(k2 != i || l2 != l1 || j3 != storeId)
					return null;
				if(i3 < 0 || (long)i3 > dataFile.length() / 520L)
					return null;
				for(int k3 = 0; k3 < i2; k3++)
					abyte0[k1++] = buffer[k3 + 8];

				j1 = i3;
			}

			return abyte0;
		} catch(IOException _ex) {
			return null;
		}
	}

	public synchronized boolean put(int i, byte abyte0[], int j) {
		boolean flag = put(true, j, i, abyte0);
		if(!flag)
			flag = put(false, j, i, abyte0);
		return flag;
	}

	private synchronized boolean put(boolean flag, int index, int length, byte[] byteStream) {
		try {
			int sector;
			if(flag) {
				seek(indexFile, index * 6);
				int k1;
				for(int offset = 0; offset < 6; offset += k1) {
					k1 = indexFile.read(buffer, offset, 6 - offset);
					if(k1 == -1)
						return false;
				}
				sector = ((buffer[3] & 0xff) << 16) + ((buffer[4] & 0xff) << 8) + (buffer[5] & 0xff);
				if(sector <= 0 || (long)sector > dataFile.length() / 520L)
					return false;
			} else {
				sector = (int)((dataFile.length() + 519L) / 520L);
				if(sector == 0)
					sector = 1;
			}
			buffer[0] = (byte)(length >> 16);
			buffer[1] = (byte)(length >> 8);
			buffer[2] = (byte)length;
			buffer[3] = (byte)(sector >> 16);
			buffer[4] = (byte)(sector >> 8);
			buffer[5] = (byte)sector;
			seek(indexFile, index * 6);
			indexFile.write(buffer, 0, 6);
			int written = 0;
			for(int zero = 0; written < length; zero++) {
				int nextSector = 0;
				if(flag) 	{
					seek(dataFile, sector * 520);
					int j2;
					int l2;
					for(j2 = 0; j2 < 8; j2 += l2) {
						l2 = dataFile.read(buffer, j2, 8 - j2);
						if(l2 == -1)
							break;
					}
					if(j2 == 8) {
						int i3 = ((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff);
						int j3 = ((buffer[2] & 0xff) << 8) + (buffer[3] & 0xff);
						nextSector = ((buffer[4] & 0xff) << 16) + ((buffer[5] & 0xff) << 8) + (buffer[6] & 0xff);
						int k3 = buffer[7] & 0xff;
						if(i3 != index || j3 != zero || k3 != storeId)
							return false;
						if(nextSector < 0 || (long)nextSector > dataFile.length() / 520L)
							return false;
					}
				}
				if(nextSector == 0) {
					flag = false;
					nextSector = (int)((dataFile.length() + 519L) / 520L);
					if(nextSector == 0)
						nextSector++;
					if(nextSector == sector)
						nextSector++;
				}
				if(length - written <= 512)
					nextSector = 0;
				buffer[0] = (byte)(index >> 8);
				buffer[1] = (byte)index;
				buffer[2] = (byte)(zero >> 8);
				buffer[3] = (byte)zero;
				buffer[4] = (byte)(nextSector >> 16);
				buffer[5] = (byte)(nextSector >> 8);
				buffer[6] = (byte)nextSector;
				buffer[7] = (byte)storeId;
				seek(dataFile, sector * 520);
				dataFile.write(buffer, 0, 8);
				int remaining = length - written;
				if(remaining > 512)
					remaining = 512;
				dataFile.write(byteStream, written, remaining);
				written += remaining;
				sector = nextSector;
			}

			return true;
		} catch(IOException _ex) {
			return false;
		}
	}

	private synchronized void seek(RandomAccessFile randomaccessfile, int position) throws IOException {
		try {
			randomaccessfile.seek(position);
		} catch(Exception e) { }
	}
}
