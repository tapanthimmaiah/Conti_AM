package com.conti.application;

import java.io.Console;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;



/**
 * 
 * @author uif34242
 *
 */
public class EncryptionDecryption {


	 

	  private static final String encryptionKey = "ABCDEFGHIJKLMNOP";
	  private static final String characterEncoding = "UTF-8";
	  private static final String cipherTransformation = "AES/CBC/PKCS5PADDING";
	  private static final String aesEncryptionAlgorithem = "AES";

	  /**
	   * Method to encrypt plain text
	   * 
	   * @param plainText
	   * @return encryptedText
	   */
	  public static String encrypt(final String plainText) {
	    
	    String encryptedText = "";
	    try {
	      Cipher cipher = Cipher.getInstance(cipherTransformation);
	      byte[] key = encryptionKey.getBytes(characterEncoding);
	      SecretKeySpec secretKey = new SecretKeySpec(key, aesEncryptionAlgorithem);
	      IvParameterSpec ivparameterspec = new IvParameterSpec(key);
	      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparameterspec);
	      byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF8"));
	      Base64.Encoder encoder = Base64.getEncoder();
	      encryptedText = encoder.encodeToString(cipherText);
	     

	    }
	    catch (Exception e) {
	      
	      return null;
	    }
	    return encryptedText;
	  }

	  /**
	   * Method to get decrypt the data
	   * 
	   * @param encryptedText
	   * @return decryptedText
	   */
	  public static String decrypt(final String encryptedText) {
	 
	    String decryptedText = "";
	    try {
	      Cipher cipher = Cipher.getInstance(cipherTransformation);
	      byte[] key = encryptionKey.getBytes(characterEncoding);
	      SecretKeySpec secretKey = new SecretKeySpec(key, aesEncryptionAlgorithem);
	      IvParameterSpec ivparameterspec = new IvParameterSpec(key);
	      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivparameterspec);
	      Base64.Decoder decoder = Base64.getDecoder();
	      byte[] cipherText = decoder.decode(encryptedText.getBytes("UTF8"));
	      decryptedText = new String(cipher.doFinal(cipherText), "UTF-8");
	    
	    }
	    catch (Exception e) {
	    
	      return null;
	    }
	    return decryptedText;
	  }
	  
		 public static void main(String args[]) { 
			 
			 Console console = System.console();

		       String enteredPassword = new String(console.readPassword("Please enter your password: "));
		       System.out.println( encrypt(enteredPassword)); 
			 }
		 
	}
