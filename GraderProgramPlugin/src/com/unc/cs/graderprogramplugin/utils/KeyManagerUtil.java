package com.unc.cs.graderprogramplugin.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;

/**
 * @author Andrew Vitkus
 *
 */
public class KeyManagerUtil {
	public static KeyManagerFactory getKeyManagerFactory(String keystoreFileName, String keystorePassword) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, CertificateException, IOException {
		return getKeyManagerFactory(keystoreFileName, keystorePassword, keystorePassword);
	}
	
	public static KeyManagerFactory getKeyManagerFactory(String keystoreFileName, String keystorePassword, String keyPassword) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, CertificateException, IOException {
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(setupKeyStore(keystoreFileName, keystorePassword), keyPassword.toCharArray());
        return kmf;
	}
	
	private static KeyStore setupKeyStore(String keystoreFileName, String keystorePassword) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		
	    char[] password = keystorePassword.toCharArray();

	    BufferedInputStream bis = new BufferedInputStream(getKeystoreInputStream(keystoreFileName));
	    try {
	        ks.load(bis, password);
	    } finally {
	        if (bis != null) {
	        	bis.close();
	        }
	    }
	    return ks;
	}
	
	private static InputStream getKeystoreInputStream(String keystoreFileName) {
		URL url;
		try {
		    url = new URL("platform:/plugin/GraderProgramPlugin/" + keystoreFileName);
		    InputStream inputStream = url.openConnection().getInputStream();
		    
		    return inputStream;
		 
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return null;
	}
}
