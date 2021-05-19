package it.eng.edicolaservice.security;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.edicolaservice.filters.JWTAuthenticationFilter;


public class RSAKeys {
	
	private static final Logger log = LoggerFactory.getLogger(RSAKeys.class);		
	

    public static final String ENCRYPT_ALGORITHM = "RSA";
    private static final int DEFAULT_KEY_SIZE = 2048;
    private static final String PUB_KEY_PEM_FILE = "./pubkey.pem";
    
    @Value( "${secret}" )
    private String secret;
	
	private RSAPublicKey rPubKey = null;
	private RSAPrivateKey rPriKey = null;

	@PostConstruct
	public void init(){
		
		try {
			generateKey(PUB_KEY_PEM_FILE, DEFAULT_KEY_SIZE);
		}
		catch(Exception e) {
			log.error("Errore in inizializzazione RSA Keys: " + e.getMessage(), e);
		}

		
	}

	
	
    public void generateKey(String publicKeyPemFile, int keySize) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ENCRYPT_ALGORITHM);
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        keyPairGenerator.initialize(Math.max(keySize, DEFAULT_KEY_SIZE), secureRandom);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        
        //set keys
		rPubKey = (RSAPublicKey) keyPair.getPublic();
		rPriKey = (RSAPrivateKey) keyPair.getPrivate();

        // Get the public key and write it out
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKeyBytes);
        writeFile(publicKeyPemFile, publicKeyString);


    }
	
    private static void writeFile(String filename, String publicKeyString) throws IOException {

    	FileWriter fos = null;
    	
    	try {
	        fos = new FileWriter(filename);
	        fos.write("-----BEGIN PUBLIC KEY-----\n");
	        fos.write(publicKeyString);
	        fos.write("\n-----END PUBLIC KEY-----\n");
	        fos.close();
    	}
    	catch(IOException e) {
    		log.error("Errore in generazione file PEM Public Key: " + e.getMessage(), e);
    	}
    	finally {
    		if(fos!=null) {
    			fos.close();
    		}
    	}

    }    
	
	
	public RSAPublicKey getrPubKey() {
		return rPubKey;
	}

	public void setrPubKey(RSAPublicKey rPubKey) {
		this.rPubKey = rPubKey;
	}

	public RSAPrivateKey getrPriKey() {
		return rPriKey;
	}

	public void setrPriKey(RSAPrivateKey rPriKey) {
		this.rPriKey = rPriKey;
	}
	
	
	
}
