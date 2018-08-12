package com.runescape.io;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class FileOperations {
	
    public FileOperations() { }
    
    public static final byte[] Decompress(byte[] inBuffer) {
    	if(inBuffer == null) return null;
		int i = 0;
		try {
			GZIPInputStream gzipinputstream = new GZIPInputStream(new ByteArrayInputStream(inBuffer));
			do {
				if (i == gzipInputBuffer.length)
					throw new RuntimeException("buffer overflow!");
				int k = gzipinputstream.read(gzipInputBuffer, i, gzipInputBuffer.length - i);
				if (k == -1)
					break;
				i += k;
			} while (true);
		} catch (IOException _ex) {
			_ex.printStackTrace();
			return null;
		}
		byte[] outBuffer = new byte[i];
		System.arraycopy(gzipInputBuffer, 0, outBuffer, 0, i);
		return outBuffer;
    }

    public static final byte[] ReadFile(String s) {
        try  {
            File file = new File(s);
            int i = (int)file.length();
            byte abyte0[] = new byte[i];
            DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new FileInputStream(s)));
            datainputstream.readFully(abyte0, 0, i);
            datainputstream.close();
            TotalRead++;
            return abyte0;
        } catch(Exception exception) {
        }
        return null;
    }

    public static final void WriteFile(String s, byte abyte0[]) {
        try {
            (new File((new File(s)).getParent())).mkdirs();
            FileOutputStream fileoutputstream = new FileOutputStream(s);
            fileoutputstream.write(abyte0, 0, abyte0.length);
            fileoutputstream.close();
            TotalWrite++;
            CompleteWrite++;
        }  catch(Throwable throwable) {
            System.out.println((new StringBuilder()).append("Write Error: ").append(s).toString());
        }
    }
	
	public static boolean FileExists(String file) {
		File f = new File(file);
		if(f.exists())
			return true;
		else
			return false;
	}

    public static int TotalRead = 0;
    public static int TotalWrite = 0;
    public static int CompleteWrite = 0;
    private static byte[] gzipInputBuffer = new byte[0x71868];
}