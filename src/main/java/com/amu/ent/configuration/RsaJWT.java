package com.amu.ent.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

public class RsaJWT {
	
	private static RSAPublicKey publickey=null;
	private static RSAPrivateKey privatekey=null;

	
	private static final Logger logger = LoggerFactory.getLogger(RsaJWT.class);
	
	public static RSAPublicKey readPublicKey()  {
		
		if (publickey != null )
			return publickey;
		
		byte[] bdata = null;
		
	    Resource resource = new ClassPathResource("JWTPublicKey.pem");
        try {
			InputStream inputStream = resource.getInputStream();
			bdata = FileCopyUtils.copyToByteArray(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Peut pas ouvrir clef publique JWTPublicKey.pem dans le répertoire de application.properties",e);
		}
        
        String key = new String(bdata, StandardCharsets.UTF_8);
	    //String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

	    String publicKeyPEM = key
	      .replace("-----BEGIN PUBLIC KEY-----", "")
	      .replaceAll(System.lineSeparator(), "")
	      .replace("-----END PUBLIC KEY-----", "");

	   	try {
	   		byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
		   	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
			publickey=(RSAPublicKey) keyFactory.generatePublic(keySpec);
		    return publickey;
		} catch (Exception e) {
			logger.error("clef publique invalide JWTPrivateKey.pem ",e);
			return null;
		}
	    
	}
	
	// Attention la clef doit être au format PKCS#8 voir ci-dessous comment transformer
	//openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in privateKey.pem -out pkcs8.key
	public static RSAPrivateKey readPrivateKey()  {
	    
		if (privatekey != null )
			return privatekey;
		
		byte[] bdata = null;
		
	    Resource resource = new ClassPathResource("JWTPrivateKey.pem");
		try {
			InputStream inputStream = resource.getInputStream();
			bdata = FileCopyUtils.copyToByteArray(inputStream);
		} catch (IOException e) {
			logger.error("Peut pas ouvrir clef privée JWTPrivateKey.pem ",e);
		}
        
        String key = new String(bdata, StandardCharsets.UTF_8);
	    
	    String rsaPrivateKey = key
	      .replace("-----BEGIN PRIVATE KEY-----", "")
	      .replaceAll(System.lineSeparator(), "")
	      .replace("-----END PRIVATE KEY-----", "");

	    try {
	    	PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rsaPrivateKey));
	    	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			privatekey=(RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		    return privatekey;
		} catch (Exception e) {
			logger.error("clef privée invalide JWTPrivateKey.pem ",e);
			return null;
		}
	}
}
