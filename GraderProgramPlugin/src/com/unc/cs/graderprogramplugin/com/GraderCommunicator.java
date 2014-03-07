package com.unc.cs.graderprogramplugin.com;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.unc.cs.graderprogramplugin.utils.KeyManagerUtil;
import com.unc.cs.graderprogramplugin.utils.TrustManagerUtil;

/**
 * @author Andrew Vitkus
 *
 */
public class GraderCommunicator implements IGraderCommunicator {

	private static final String SSL_PROTOCOL = "TLSv1";
	private static final String KEYSTORE_FILENAME = "keystore.jks";
	private static final String KEYSTORE_PASSWORD = "1qaz3edc5tgb";
	private static final String[] CIPHERS = {"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
		                               "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
		                               "TLS_RSA_WITH_AES_128_CBC_SHA256",
		                               "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
		                               "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
		                               "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
		                               "TLS_ECDH_RSA_WITH_RC4_128_SHA"};
	private static String[] PROTOCOLS = {"SSLv3",
		                                 "TLSv1",
		                                 "TLSv1.1",
		                                 "TLSv1.2"};
	
	private static final InetSocketAddress gradingServerAddress = new InetSocketAddress(GradingServerConstants.GRADER_SERVER, GradingServerConstants.COM_PORT);
	private boolean open = false;
	
	private SSLSocket graderSocket;
	
	public void connect() throws UnknownHostException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		graderSocket = setupSSLSocket();
		graderSocket.connect(gradingServerAddress);
		open = true;
	}
	
	private SSLSocket setupSSLSocket() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {
		KeyManagerFactory kmf = KeyManagerUtil.getKeyManagerFactory(KEYSTORE_FILENAME, KEYSTORE_PASSWORD);
        TrustManagerFactory tmf = TrustManagerUtil.getTrustManagerFactory(KEYSTORE_FILENAME, KEYSTORE_PASSWORD);
        SSLContext ctx = SSLContext.getInstance(SSL_PROTOCOL);
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        SSLSocketFactory factory = ctx.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket();
        socket.setEnabledCipherSuites(CIPHERS);
        socket.setEnabledProtocols(PROTOCOLS);
        return socket;
	}
	
	public void disconnect() throws IOException {
		if (graderSocket != null) {
			graderSocket.close();
			graderSocket = null;
		}
		open = false;
	}
	
	public void sendAssignment(File file, String assignment, String course, String vfykey) throws IOException {
		if (!open) {
			try {
				connect();
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		DataOutputStream dos= null;
		BufferedInputStream br = null;
		try {
			dos = new DataOutputStream(graderSocket.getOutputStream());
			dos.writeUTF(vfykey);
			dos.writeUTF(course);
			dos.writeUTF(assignment);
			dos.writeLong(file.length());
			
			br = new BufferedInputStream(new FileInputStream(file));
			
			byte[] data = new byte[GradingServerConstants.BUFFER_SIZE];
	        int bytesRead = -1;
	        while ((bytesRead = br.read(data)) != -1) {
	        	//System.out.println(bytesRead);
	        	dos.write(data, 0, bytesRead);
	        }
	        dos.flush();
		} finally {
			if (br != null) {
				br.close();
			}
		}
		
	}
	
	public boolean getBooleanResponse() throws IOException {
		DataInputStream dis = new DataInputStream(graderSocket.getInputStream());
		boolean response = dis.readBoolean();
		return response;
	}
	
	public String getUTFResponse() throws IOException {
		DataInputStream dis = new DataInputStream(graderSocket.getInputStream());
		String response = dis.readUTF();
		//System.out.println("Recieved " + response + " from server " + graderSocket.getInetAddress().getHostAddress());
		return response;
	}
}
