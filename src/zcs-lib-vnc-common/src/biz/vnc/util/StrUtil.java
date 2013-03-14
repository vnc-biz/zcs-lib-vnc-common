package biz.vnc.util;

import java.lang.StringBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Vector;

public class StrUtil {
	public static String join(Vector<String> s, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		Iterator iter = s.iterator();
		while (iter.hasNext()) {
			buffer.append(iter.next());
			if (iter.hasNext()) {
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}

	public static boolean isBlank(String s) {
		return ((s==null) || (s.equals("")));
	}

	public static String bin2hex(byte[] b) {
		if (b==null)
			return "";

		StringBuffer hexString = new StringBuffer();

		for (int i=0; i<b.length; i++)
			hexString.append(Integer.toHexString(0xFF & b[i]));

		return hexString.toString();
	}

	public static String md5_hash_hex(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(s.getBytes());
			return bin2hex(md.digest());
		} catch (java.security.NoSuchAlgorithmException e) {
			return "";
		}
	}

	/* sanitizes a filename so it may not have any ../ and no leading /
	   it does NOT guarantee that a filename with these patters will
	   be valid after sanitizing
	*/
	public static String sanitizeFilename(String fn) {
		if ((fn == null) || fn.equals("") || fn.equals("/"))
			return "";

		String s = null;
		String l[] = fn.split("/");
		for (int x=0; x<l.length; x++) {
			if ((l[x] != null) && (!l[x].equals("")) && (!l[x].equals(".")) && (!l[x].equals("..")))
				s = (s==null) ? l[x] : s+"/"+l[x];
		}
		return s;
	}
}
