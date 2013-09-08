package org.nilennoct.controller;

import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-4
 * Time: 下午10:46
 */
public class AES {
	static String IV = "AAAAAAAAAAAAAAAA";
	static String plaintext = "test text 123\0\0\0"; /*Note null padding*/
	static String encryptionKey = "rBwj1MIAivVN222b";

	public static void main(String [] args) {
		try {

			System.out.println("==Java==");
			System.out.println("plain:   " + plaintext);

			String cipher = encrypt(plaintext, encryptionKey);

			System.out.print("cipher:  " + cipher);
//			for (int i=0; i<cipher.length; i++)
//				System.out.print(new Integer(cipher[i])+" ");
//			System.out.println("");
//
//			String decrypted = decrypt(cipher, encryptionKey);
//
//			System.out.println("decrypt: " + decrypted);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String encrypt(String plainText, String encryptionKey) throws Exception {
		byte[] bytes = plainText.getBytes("UTF-8");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return new BASE64Encoder().encode(cipher.doFinal(bytes, 0, bytes.length)).replaceAll("=+", "");
	}

	public static String decrypt(byte[] cipherText, String encryptionKey) throws Exception{
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return new String(cipher.doFinal(cipherText),"UTF-8");
	}
}
