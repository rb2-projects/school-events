package com.schoolevents.infrastructure.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

class AesEncryptionUtilTest {

    @Test
    void testEncryptDecrypt() throws Exception {
        String originalText = "{\"events\": []}";
        String password = "superSecretPassword123";

        // Encrypt
        String encrypted = AesEncryptionUtil.encrypt(originalText, password);

        assertNotNull(encrypted);
        assertNotEquals(originalText, encrypted);
        assertTrue(encrypted.startsWith("v1|"));

        // Manual Decrypt to verify (mirroring the logic the UI would use)
        String decrypted = manualDecrypt(encrypted, password);
        assertEquals(originalText, decrypted);
    }

    private String manualDecrypt(String encryptedData, String password) throws Exception {
        String[] parts = encryptedData.split("\\|"); // v1, salt, iv, ciphertext
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] iv = Base64.getDecoder().decode(parts[2]);
        byte[] cipherText = Base64.getDecoder().decode(parts[3]);

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] plainBytes = cipher.doFinal(cipherText);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }
}
