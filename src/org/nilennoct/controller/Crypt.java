package org.nilennoct.controller;// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

import android.util.Base64;
import com.playpiegames.clib.E_CODE;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Crypt {

	private static String rsaKey = "";
	private static String rsa_epKey = "";
	private String aesKey;

	public Crypt() {
		aesKey = null;
	}

	protected static void setRSAKey(String s) {
		rsaKey = s;
	}

	protected static void setRSA_EPKey(String s) {
		rsa_epKey = s;
	}

	private byte[] encryptByRSA_EP(String text, String key) throws Exception {
		if (key == null) {
			key = rsa_epKey;
		}

		if (key.length() >= 2) {
			PublicKey publickey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(key, 0)));
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			try {
				cipher.init(1, publickey);
			}
			catch (InvalidKeyException e) {
				e.printStackTrace();
			}
			return cipher.doFinal(text.getBytes("utf-8"));
		}

		return null;
	}

	protected String getEncryptedAESKey() throws Exception {
		return Base64.encodeToString(encryptByRSA_EP(aesKey, null), 0);
	}

	protected void setAESKey() throws Exception {
		KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
		keygenerator.init(128, SecureRandom.getInstance("SHA1PRNG"));
		aesKey = Base64.encodeToString(keygenerator.generateKey().getEncoded(), 0);
	}

	protected byte[] encrypt(E_CODE e_code, String text, String key) throws Exception {
		byte abyte3[];
		switch (e_code) {
			case RSA_EP: {
				abyte3 = encryptByRSA_EP(text, key);
				return abyte3;
			}
			case AES: {
				byte abyte0[];
				if (key != null) {
					abyte0 = key.getBytes();
				}
				else {
					abyte0 = Base64.decode(aesKey.getBytes(), 0);
				}
				byte abyte2[];
				SecretKeySpec secretkeyspec = new SecretKeySpec(abyte0, "AES");
				byte abyte1[] = text.getBytes();
				Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
				cipher.init(1, secretkeyspec);
				abyte2 = cipher.doFinal(abyte1);
				return abyte2;
			}
		}
		return null;
	}

	protected byte[] decrypt(E_CODE e_code, byte textByte[], String key) throws Exception {
		switch (e_code) {
			case RSA_EP: {
				key = key == null ? rsaKey : key;
				if (key.length() >= 2) {
					byte abyte1[];
					PrivateKey privatekey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(key, 0)));
					Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
					cipher.init(2, privatekey);
					abyte1 = cipher.doFinal(textByte);
					return abyte1;
				}
			}
			case AES: {
				byte abyte2[];
				if (key != null) {
					abyte2 = key.getBytes();
				}
				else {
					abyte2 = Base64.decode(aesKey.getBytes(), 0);
				}
				byte abyte4[];
				SecretKeySpec secretkeyspec = new SecretKeySpec(abyte2, "AES");
				byte abyte3[] = Base64.decode(textByte, 0);
				Cipher cipher1 = Cipher.getInstance("AES/ECB/PKCS5Padding");
				cipher1.init(Cipher.DECRYPT_MODE, secretkeyspec);
				abyte4 = cipher1.doFinal(abyte3);
				return abyte4;
			}
		}

		return null;
	}

}
