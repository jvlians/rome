package com.ippon.rome;

import java.io.InputStream;

/**
 * Created by slaughter.
 */
public class EncryptionDTO {
    byte[] keyBytes;
    InputStream encrypted;

    public EncryptionDTO(byte[] keyBytes, InputStream encrypted) {
        this.keyBytes = keyBytes;
        this.encrypted = encrypted;
    }

    public byte[] getKeyBytes() {
        return keyBytes;
    }

    public void setKeyBytes(byte[] keyBytes) {
        this.keyBytes = keyBytes;
    }

    public InputStream getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(InputStream encrypted) {
        this.encrypted = encrypted;
    }
}
