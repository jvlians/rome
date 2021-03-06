package com.ippon.rome;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;

/**
 * Created by slaughter.
 */
public class FileProcessor {
    // NOTE: IV precedes key. First 16 bytes of keyBytes should be IV.
    // Remaining should all be key.
    // TODO: Determine length of key in keyBytes, make static.
    static int numIVBytes = 16;

    // Encrypt data from the BufferedInputStream given the SecretKeySpec + IvParameterSpec combo.
    // Return a DTO with the encrypted data and the key bytes (SecretKeySpec + IvParameterSpec)
    public static EncryptionDTO encrypt(BufferedInputStream input, byte[] keyBytes) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        // Resolve Key from keyBytes
        Key key = new SecretKeySpec(keyBytes, numIVBytes, keyBytes.length-numIVBytes, "AES");

        // Prepare Cipher with IV
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(keyBytes, 0, numIVBytes));

        // Return populated DTO
        return new EncryptionDTO(keyBytes, new CipherInputStream(input, cipher));
    }

    // Encrypt data from the BufferedInputStream given no key information.
    // Return a DTO with the encrypted data and the key bytes (SecretKeySpec + IvParameterSpec)
    public static EncryptionDTO encrypt(BufferedInputStream input) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        // Generate key / IV pair to be used.
        byte[] keyBytes = KeyGenerator.getInstance("AES").generateKey().getEncoded();
        byte[] ivBytes = new byte[numIVBytes];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(ivBytes);

        // Store pair in byteArray to be used later.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(ivBytes);
        outputStream.write(keyBytes);

        byte pair[] = outputStream.toByteArray();

        // Convert bytes into usable key.
        Key key = new SecretKeySpec(keyBytes, "AES");

        // Prepare Cipher with IV
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivBytes));

        // Encrypt and return populated DTO
        return new EncryptionDTO(pair, new CipherInputStream(input, cipher));
    }

    // Decrypt data from
    public static InputStream decrypt(byte[] keyBytes, BufferedInputStream encrypted) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        // Resolve Key from keyBytes
        Key key = new SecretKeySpec(keyBytes, numIVBytes, keyBytes.length-numIVBytes, "AES");

        // Prepare Cipher with IV
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(keyBytes, 0, numIVBytes));

        // Return decrypted data as BufferedOutputStream
        return new CipherInputStream(encrypted, cipher);
    }

    // Decrypt data from
    public static InputStream decrypt(EncryptionDTO encDTO) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        // Resolve Key from keyBytes
        Key key = new SecretKeySpec(encDTO.getKeyBytes(), numIVBytes, encDTO.getKeyBytes().length-numIVBytes, "AES");

        // Prepare Cipher with IV
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(encDTO.getKeyBytes(), 0, numIVBytes));

        // Return decrypted data as BufferedOutputStream
        return new CipherInputStream(encDTO.getEncrypted(), cipher);
    }
}
