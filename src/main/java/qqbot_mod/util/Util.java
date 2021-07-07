
package com.github.zyxgad.qqbot_mod.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Util{
	private Util(){}

	public static byte[] md5(byte[] data){
		try{
			return MessageDigest.getInstance("md5").digest(data);
		}catch(NoSuchAlgorithmException e){
		}
		return null;
	}

	public static String bytesToString(final byte[] bytes){
		if(bytes == null){
			return null;
		}
		try{
			return new String(bytes, "UTF-8");
		}catch(UnsupportedEncodingException e){
			return new String(bytes);
		}
	}

	public static byte[] stringToBytes(final String str){
		if(str == null){
			return null;
		}
		try{
			return str.getBytes("UTF-8");
		}catch(UnsupportedEncodingException e){
			return str.getBytes();
		}
	}

	private static final String hexTable = "0123456789abcdef";
	public static byte[] hexToBytes(String hex){
		if(hex.length() % 2 == 1){
			hex = "0" + hex;
		}
		final int len = hex.length() / 2;
		final byte[] data = new byte[len];
		for(int i = 0;i < len;i++){
			data[i] = (byte)(
				(byte)(hexTable.indexOf((int)(hex.charAt(i * 2))) << 4) |
				(byte)(hexTable.indexOf((int)(hex.charAt(i * 2 + 1))) & 0x0f));
		}
		return data;
	}

	public static String bytesToHex(byte[] data){
		StringBuilder hex = new StringBuilder();
		for(byte b: data){
			hex.append(hexTable.charAt((b & 0xf0) >> 4)).append(hexTable.charAt(b & 0x0f));
		}
		return hex.toString();
	}
}