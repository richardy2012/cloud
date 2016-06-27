package com.aug3.storage.redisclient.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.codec.binary.Base64;

public class ObjectResolver {

	public static Object deserializeObj(byte[] buf) throws IOException, ClassNotFoundException {

		Object obj = null;
		if (buf != null) {
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(new ByteArrayInputStream(buf));
				obj = ois.readObject();
			} catch (IOException e) {
				throw e;
			} catch (ClassNotFoundException e) {
				throw e;
			} finally {
				if (ois != null) {
					try {
						ois.close();
					} catch (IOException e) {
						throw e;
					}
				}
			}
		}

		return obj;
	}

	public static byte[] serializeObj(Object obj) throws IOException {
		byte[] buf = null;

		if (obj != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(baos);
				oos.writeObject(obj);
				buf = baos.toByteArray();
			} catch (IOException e) {
				throw e;
			} finally {
				if (oos != null) {
					try {
						oos.close();
					} catch (IOException e) {
					}
				}
			}
		}

		return buf;

	}

	public static byte[] decode(String s) {
		return Base64.decodeBase64(s);
	}

	public static String encode(byte[] b) {
		return Base64.encodeBase64String(b);
	}

}
