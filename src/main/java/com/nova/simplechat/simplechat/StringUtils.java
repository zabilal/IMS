package com.nova.simplechat.simplechat;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class StringUtils {

	private MessageDigest sha512;
	private final SecureRandom secureRandom;

	public StringUtils() {
		secureRandom = new SecureRandom();
		try {
			sha512 = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException nsae) {
			throw new RuntimeException("No such algorithm : SHA-256. Something's really wrong in source code.", nsae);
		}
	}

	public String hash512(String str) {
		sha512.reset();
		try {
			sha512.update(str.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("UTF-8 is not supported by this platform", uee);
		}
		byte[] digest = sha512.digest();
		return toHexString(digest);
	}

	private String toHexString(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (byte aByte : bytes) {
			String hex = Integer.toHexString(0xff & aByte);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public String generateToken() {
		return new BigInteger(130, secureRandom).toString(5);
	}
        
        protected String getSaltString(int size) {
                String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
                StringBuilder salt = new StringBuilder();
                Random rnd = new Random();
                while (salt.length() < size) { // length of the random string.
                    int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                    salt.append(SALTCHARS.charAt(index));
                }
                String saltStr = salt.toString();
                
                return saltStr;

        }
}
