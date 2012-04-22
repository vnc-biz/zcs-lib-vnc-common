
package biz.vnc.util;

import java.lang.StringBuffer;
import java.util.Iterator;
import java.util.Vector;

public class StrUtil
{
	public static String join(Vector<String> s, String delimiter)
	{
		StringBuffer buffer = new StringBuffer();
		Iterator iter = s.iterator();
		while (iter.hasNext())
		{
			buffer.append(iter.next());
			if (iter.hasNext())
			{
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}
}
