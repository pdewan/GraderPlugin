package com.unc.cs.graderprogramplugin.com;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * @author Andrew Vitkus
 *
 */
public class HttpGraderCommunicator {
	public static String submitAssignment(File file, String assignment, String course, String type, String vfykey) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
        	String url = GradingServerConstants.GRADER_SERVER + "/" + GradingServerConstants.SUBMISSION_PATH;
            HttpPost httppost = new HttpPost(url);

            FileBody submission = new FileBody(file);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("file", submission)
                    .addTextBody("assignment", assignment, ContentType.TEXT_PLAIN)
                    .addTextBody("course", course, ContentType.TEXT_PLAIN)
                    .addTextBody("type", type, ContentType.TEXT_PLAIN)
                    .addTextBody("vfykey", vfykey, ContentType.TEXT_PLAIN)
                    .build();

            httppost.setEntity(reqEntity);

            System.out.println("executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                }
                String responseStr = EntityUtils.toString(resEntity);
                System.out.println(responseStr);
                EntityUtils.consume(resEntity);
                return responseStr;
            } finally {
                try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        } catch (Exception e) {
        	
        } finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return "Error reading grading, please resubmit.";
	}
}
