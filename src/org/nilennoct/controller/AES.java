package org.nilennoct.controller;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-4
 * Time: 下午10:46
 */
public class AES {
	public static boolean isNewCrypt = false;
	public static String encryptionKey = "011218525486l6u1";

	public static void main(String args[]) {
		try {
			System.out.println(new String(AES.decrypt(new ByteArrayInputStream(new BASE64Decoder().decodeBuffer("+c+ejTc0+UAAOIWi48ojTQ")))));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String encrypt(String plainText) throws Exception {
		byte[] bytes = plainText.getBytes("UTF-8");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, key);

		return new String(cipher.doFinal(bytes, 0, bytes.length));
	}

	public static String encrypt64(String plainText) throws Exception {
		byte[] bytes = plainText.getBytes("UTF-8");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, key);

		return new BASE64Encoder().encode(cipher.doFinal(bytes, 0, bytes.length));//.replaceAll("=+", "");
	}

	public static byte[] decrypt(InputStream cipherText) throws Exception{
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
//		System.out.println(cipherText.toString());
		cipher.init(Cipher.DECRYPT_MODE, key);

//		System.out.println(new String(cipher.doFinal(getBytes(cipherText)), "UTF-8"));

		return cipher.doFinal(getBytes(cipherText));
	}

	public static byte[] decrypt64(InputStream cipherText) throws Exception{
		byte[] cipherTextFromBase64 = new BASE64Decoder().decodeBuffer(cipherText);
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.DECRYPT_MODE, key);

		System.out.println(new String(cipher.doFinal(cipherTextFromBase64),"UTF-8"));

		return cipher.doFinal(cipherTextFromBase64);
	}

	public static void setNewKey(String key) {
		encryptionKey = key;
		isNewCrypt = true;
	}

	public static byte[] getBytes(InputStream is) throws IOException {

		int len;
		int size = 1024;
		byte[] buf;

		if (is instanceof ByteArrayInputStream) {
			size = is.available();
			buf = new byte[size];
			len = is.read(buf, 0, size);
		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			buf = new byte[size];
			while ((len = is.read(buf, 0, size)) != -1)
				bos.write(buf, 0, len);
			buf = bos.toByteArray();
		}
		return buf;
	}
}
