package com.ippon.rome;

import java.io.BufferedOutputStream;

/**
 * Created by slaughter.
 */
public class EncryptionDTO {
    byte[] keyBytes;
    BufferedOutputStream encrypted;

    public EncryptionDTO(byte[] keyBytes, BufferedOutputStream encrypted) {
        this.keyBytes = keyBytes;
        this.encrypted = encrypted;
    }

    public byte[] getKeyBytes() {
        return keyBytes;
    }

    public void setKeyBytes(byte[] keyBytes) {
        this.keyBytes = keyBytes;
    }

    public BufferedOutputStream getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(BufferedOutputStream encrypted) {
        this.encrypted = encrypted;
    }
}
