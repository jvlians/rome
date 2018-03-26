package com.ippon.rome;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.security.*;

public class KeyProcessor {
    private static SecureRandom random = new SecureRandom();
    public static BASE64Encoder b64 = new BASE64Encoder();
    public static BASE64Decoder b64d = new BASE64Decoder();
    private static KeyPairGenerator generator;
    static {
        Security.addProvider(new BouncyCastleProvider());

        try {
            generator = KeyPairGenerator.getInstance("RSA", "BC");
            generator.initialize(2048);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static KeyPair generate() { return generator.genKeyPair(); }
    public static String serialize(Key k) {
        return b64.encode(k.getEncoded());
    }
    public static byte[] cypher(String key, byte[] data, boolean encrypt) throws
            IOException, InvalidCipherTextException {
        AsymmetricBlockCipher e = new RSAEngine();
        e = new org.bouncycastle.crypto.encodings.PKCS1Encoding(e);
        byte[] dec = b64d.decodeBuffer(key);
        AsymmetricKeyParameter publicKey = (AsymmetricKeyParameter) (encrypt ?
                    PublicKeyFactory.createKey(dec) :
                PrivateKeyFactory.createKey(dec));
        e.init(encrypt, publicKey);
        return e.processBlock(data,0, data.length);
    }
    public static byte[] encrypt(String pub, byte[] plaintext) throws
            IOException, InvalidCipherTextException {
        return cypher(pub, plaintext, true);
    }
    public static byte[] decrypt(String priv, byte[] cyphertext) throws
            IOException, InvalidCipherTextException {
        return cypher(priv, cyphertext, false);
    }
}
