// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.playpiegames.clib;

import org.nilennoct.controller.Crypt;


// Referenced classes of package com.playpiegames.clib:
//            E_CODE

public class CLibMain extends Crypt {

	private boolean flag;
	private String[] pubKey = {
		"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANV2ohKiVs/2cOiGN7TICmQ/NbkuellbTtcKbuDbIlBMocH+Eu0n2nBYZQ2xQbAv+E9na8K2FyMyVY4+RIYEJ+0CAwEAAQ==",
		"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAOLtTe70uQZ2BAneeTyNezMH/yn/uDu6qabQ3XHhmqqW8C4ZLxG7uW6bNmUdZQSUk8dO2+7ZTbN5lQw/u70Av2ECAwEAAQ==",
		"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAM5U06JAbYWdRBrnMdE2bEuDmWgUav7xNKm7i8s1Uy/fvpvfxLeoWowLGIBKz0kDLIvhuLV8Lv4XV0+aXdl2j4kCAwEAAQ==",
		"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAL1mnz5vCQEa1xPeyIUQ2WHIzKIsZp9PKAzJ6etXV2NpyxoGheqlDZ5rXQVLEY2JSY2nBlt/QBdo9xkp8XcFgUsCAwEAAQ==",
		"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKFTx5sYAmW9kFineXZj6NwGPGA6QSgui+nwb9ru30oeoYviC6e5iHD/Qk7Gc8JPpIA335YHo6K1/U8U9gM3BncCAwEAAQ==",
		"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAL3EP/qaJ9XGmpEia4KqoJkCYFvgpJtWK3zPZ7d/qCF1GbQSSzPI+FUnuJjAXSZ43vvYYmQNHNYg791C9SrSOT0CAwEAAQ==",
		"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANWNwx1kRSlR5sl3dHkPtW//F5KlRQMPWbrLG3ZyCI1q3NUMOC+xdC87gGiINs4WC3S28j0/DrrocIXS7zf2MdECAwEAAQ==",
		"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANzMvdAQ/lmyAQQ3S0B1BkzlwvR8mYrCiATLRV0t/HeudLvhUgbkWm2UNr4M84f0wA52XqFPABMyp+o59D4DEwUCAwEAAQ==",
		"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANr/4m+Z7qKlr2kmyZmgNjf49LSgm6QP5JZwk+Wi2m8E68sUMyfKkhr1t2OXlvNAEfQrSYHu6rlXqpSf2o1zvSkCAwEAAQ==",
		"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANqJlJznVfrsXd/Nnn4L7E7Kcz2u1YwIExrJC3uyxsEk+HiCnNJ8ZUFtkc7XeZKEyw2UFfiQ73SOFAzhVfkCCS0CAwEAAQ=="
	};

	public CLibMain() {
		flag = false;
		initComplete(null, null);
	}

	public String getPUBKey(int i) {
		if (i <= 9) {
			return pubKey[i];
		}

		return "";
	}

	public String getPVKKey() {
		return "";
	}

	public static void initComplete(String s, String s1) {
		if (s == null) {
			return;
		}
		setRSA_EPKey(s);
		setRSAKey(s1);
	}

	private void nativeInit() {
		setRSAKey(getPUBKey(2));
		setRSA_EPKey(getPUBKey(2));
	};

	public byte[] decrypt(E_CODE e_code, byte textByte[]) throws Exception {
		if (!flag) {
			return null;
		} else {
			return decrypt(e_code, textByte, null);
		}
	}

	public byte[] encrypt(E_CODE e_code, String text) throws Exception {
		if (!flag) {
			return null;
		} else {
			return encrypt(e_code, text, null);
		}
	}

	public String getSigned() throws Exception {
		return getEncryptedAESKey();
	}

	public void init() throws Exception {
		flag = true;
		nativeInit();
		setAESKey();
	}
}
