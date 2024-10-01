package com.whytefarms.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static String getSha256Hash(String stringToHash) {
        try {
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException ignored) {

            }
            if (digest != null) {
                digest.reset();
                return bin2hex(digest.digest(stringToHash.getBytes()));
            } else {
                return null;
            }

        } catch (Exception ignored) {
            return null;
        }
    }

    public static String bin2hex(byte[] data) {
        StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(String.format("%02x", b & 0xFF));
        return hex.toString();
    }

}
