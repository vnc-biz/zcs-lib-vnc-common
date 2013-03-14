package biz.vnc.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class StreamUtil {

	public static byte[] readBytes(InputStream input)
	throws IOException {
		byte buffer[] = new byte[1024];
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		int count = input.read(buffer);
		while (0 < count) {
			output.write(buffer, 0, count);
			count = input.read(buffer);
		}
		input.close();
		return output.toByteArray();
	}
}
