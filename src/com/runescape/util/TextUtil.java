package com.runescape.util;

import com.runescape.sign.Signlink;

final public class TextUtil {

	public static long longForName(String s) {
		long l = 0L;
		for (int i = 0; i < s.length() && i < 12; i++) {
			char c = s.charAt(i);
			l *= 38L;
			if (c >= 'A' && c <= 'Z')
				l += (1 + c) - 65;
			else if (c >= 'a' && c <= 'z')
				l += (1 + c) - 97;
			else if (c >= '0' && c <= '9')
				l += (27 + c) - 48;
			else if (c == '_')
				l += 37;
		}

		for (; l % 38L == 0L && l != 0L; l /= 38L)
			;
		return l;
	}

	public static String nameForLong(long l) {
		try {
			if (l <= 0L || l >= 0x7dcff8986ea31000L)
				return "invalid_name";
			if (l % 38L == 0L)
				return "invalid_name";
			int i = 0;
			char ac[] = new char[12];
			while (l != 0L) {
				long l1 = l;
				l /= 38L;
				ac[11 - i++] = validChars[(int) (l1 - l * 38L)];
			}
			return new String(ac, 12 - i, i);
		} catch (RuntimeException runtimeexception) {
			Signlink.reportError("81570, " + l + ", " + (byte) -99 + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	public static long method585(String s) {
		s = s.toUpperCase();
		long l = 0L;
		for (int i = 0; i < s.length(); i++) {
			l = (l * 61L + (long) s.charAt(i)) - 32L;
			l = l + (l >> 56) & 0xffffffffffffffL;
		}
		return l;
	}

	public static String method586(int i) {
		return (i >> 24 & 0xff) + "." + (i >> 16 & 0xff) + "." + (i >> 8 & 0xff) + "." + (i & 0xff);
	}

	public static String fixName(String s) {
		if (s.length() > 0) {
			s = s.toLowerCase();
			char ac[] = s.toCharArray();
			for (int j = 0; j < ac.length; j++)
				if (ac[j] == '_' || ac[j] == ' ') {
				//	ac[j] = ' ';
					if (j + 1 < ac.length && ac[j + 1] >= 'a' && ac[j + 1] <= 'z')
						ac[j + 1] = (char) ((ac[j + 1] + 65) - 97);
				}

			if (ac[0] >= 'a' && ac[0] <= 'z')
				ac[0] = (char) ((ac[0] + 65) - 97);
			return new String(ac);
		} else {
			return s;
		}
	}

	public static String passwordAsterisks(String s) {
		StringBuffer stringbuffer = new StringBuffer();
		for (int j = 0; j < s.length(); j++)
			stringbuffer.append("*");
		return stringbuffer.toString();
	}

	public static boolean validName(String name) {
		if(name == null || name.length() <= 3 || name.length() > 12) {
			return false;
		}
		final char firstChar = name.charAt(0);
		final char lastChar = name.charAt(name.length()-1);
		if (firstChar == ' ' || firstChar == '_' || lastChar == ' ' || lastChar == '_') {
			return false;
		}
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if(!validChar(c, false)) {
				return false;
			}
		}
		return true;
	}

	public static boolean validPassword(String pass) {
		if(pass == null || pass.length() <= 3 || pass.length() > 20) {
			return false;
		}
		for (int i = 0; i < pass.length(); i++) {
			char c = pass.charAt(i);
			if(!validChar(c, true)) {
				return false;
			}
		}
		return true;
	}

	public static boolean validChar(char charCheck, boolean password) {
		char[] charset = password ? validCharsPassword : validChars;
		for(int i = 0; i < charset.length; i++) {
			char c = charset[i];
			if (charCheck >= 'A' && charCheck <= 'Z') {
				c += '\uFFE0';
			}
			if(charCheck == c) {
				return true;
			}
		}
		return false;
	}

	private static final char[] validChars = { ' ', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' };
	private static final char[] validCharsPassword  = {
			' ', 'e', 't', 'a', 'o', 'i', 'h', 'n', 's', 'r',
			'd', 'l', 'u', 'm', 'w', 'c', 'y', 'f', 'g', 'p',
			'b', 'v', 'k', 'x', 'j', 'q', 'z', '0', '1', '2',
			'3', '4', '5', '6', '7', '8', '9', '!', '?',
			'.', ',', ':', ';', '(', ')', '-', '&', '*', '\\',
			'\'', '@', '#', '+', '=', '$', '%', '"', '[',
			']','_','<','>','^','/', '{', '|', '}', '~', '`',
			'\u00A3'
	};
}
