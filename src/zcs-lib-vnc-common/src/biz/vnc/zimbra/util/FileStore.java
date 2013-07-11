package biz.vnc.zimbra.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import javax.servlet.http.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.Header;

public class FileStore {
	public static String upload_url(String z_scheme, String z_hostname, int z_port) {
		return z_scheme+"://"+z_hostname+ ":"+z_port+"/service/upload?fmt=raw";
	}

	public static String upload(String z_scheme, String z_hostname, int z_port, String auth_token, InputStream is, String filename)
	throws IOException {
		File downloadFile = new File("/tmp/_zimbra_upload_" + Math.random() * 5000);
		FileOutputStream fos = new FileOutputStream(downloadFile);
		int data = -1;
		while ((data = is.read()) != -1) {
			fos.write(data);
		}
		fos.close();

		HttpClient uploadClient = new HttpClient();
		PostMethod filePost = new PostMethod(upload_url(z_scheme, z_hostname, z_port));
		Part[] parts = {
			new FilePart("_attFile_",filename, downloadFile)
		};
		filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
		filePost.setRequestHeader("Cookie", "ZM_AUTH_TOKEN=" + auth_token);
		uploadClient.executeMethod(filePost);
		int statuscode = filePost.getStatusCode();
		ZLog.debug("zcs-lib-vnc-common","FileStore upload status code : "+statuscode);
		if( statuscode == HttpStatus.SC_MOVED_PERMANENTLY || statuscode == HttpStatus.SC_MOVED_TEMPORARILY ) {
			Header locationHeader = filePost.getResponseHeader("location");
			filePost.releaseConnection();
			ZLog.debug("zcs-lib-vnc-common","FileStore Location header found : "+locationHeader.getValue());
			if (locationHeader != null) {
				filePost = new PostMethod(locationHeader.getValue());
				filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
				filePost.setRequestHeader("Cookie", "ZM_AUTH_TOKEN=" + auth_token);
				uploadClient.executeMethod(filePost);
			}
		}
		String result = filePost.getResponseBodyAsString();
		downloadFile.delete();
		return result;
	}
}
