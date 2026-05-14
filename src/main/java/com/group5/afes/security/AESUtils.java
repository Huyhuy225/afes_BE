package com.group5.afes.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESUtils {

    // Khóa và IV khớp hoàn toàn với C++ (mqtt_setup.cpp)
    private static final byte[] AES_KEY = {
            (byte) 0x2B, (byte) 0x7E, (byte) 0x15, (byte) 0x16,
            (byte) 0x28, (byte) 0xAE, (byte) 0xD2, (byte) 0xA6,
            (byte) 0xAB, (byte) 0xF7, (byte) 0x15, (byte) 0x88,
            (byte) 0x09, (byte) 0xCF, (byte) 0x4F, (byte) 0x3C
    };

    private static final byte[] AES_IV = {
            (byte) 0x28, (byte) 0x34, (byte) 0xA5, (byte) 0xAF,
            (byte) 0xBE, (byte) 0xB8, (byte) 0x14, (byte) 0xF5,
            (byte) 0x08, (byte) 0xE1, (byte) 0x24, (byte) 0xD2,
            (byte) 0xB3, (byte) 0xAB, (byte) 0xDB, (byte) 0xCE
    };

    /**
     * Mã hóa chuỗi plain-text → Base64(AES-CBC encrypted).
     * Dùng PKCS5Padding (= PKCS7 = CMS) để khớp với AESLib mặc định bên C++.
     */
    public static String encryptData(String plaintext) {
        try {
            if (plaintext == null || plaintext.isEmpty())
                return null;

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(AES_IV);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.err.println("❌ [ENCRYPT ERROR] " + e.getMessage());
            return null;
        }
    }

    /**
     * Giải mã Base64(AES-CBC encrypted) → chuỗi plain-text.
     * Dùng PKCS5Padding để khớp với AESLib mặc định bên C++.
     */
    public static String decryptData(String ciphertext) {
        try {
            if (ciphertext == null || ciphertext.isEmpty())
                return null;

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(AES_IV);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decodedBytes = Base64.getDecoder().decode(ciphertext);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            // PKCS5Padding tự cắt padding, nhưng nếu C++ padding bằng \0 thì cần clear sạch
            return new String(decryptedBytes, StandardCharsets.UTF_8).replace("\0", "").trim();
        } catch (Exception e) {
            System.err.println("❌ [DECRYPT ERROR] " + e.getMessage());
            return null;
        }
    }
}
