/**
 * 
 */
package com.unc.cs.graderprogramplugin.com;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * @author Andrew Vitkus
 *
 */
public interface IGraderCommunicator {
	public void connect() throws UnknownHostException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException;
	public void disconnect() throws IOException;
	public void sendAssignment(File file, String assignment, String course, String vfykey) throws IOException;
	public boolean getBooleanResponse() throws IOException;
	public String getUTFResponse() throws IOException;
}
