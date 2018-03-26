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
    private static BASE64Encoder b64enc = new BASE64Encoder();
    private static BASE64Decoder b64dec = new BASE64Decoder();
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
        return b64e(k.getEncoded());
    }
    public static byte[] cypher(String key, byte[] data, boolean encrypt) throws
            IOException, InvalidCipherTextException {
        AsymmetricBlockCipher e = new RSAEngine();
        e = new org.bouncycastle.crypto.encodings.PKCS1Encoding(e);
        byte[] dec = b64d(key);
        AsymmetricKeyParameter publicKey = (AsymmetricKeyParameter) (encrypt ?
                    PublicKeyFactory.createKey(dec) :
                PrivateKeyFactory.createKey(dec));
        e.init(encrypt, publicKey);
        return e.processBlock(data,0, data.length);
    }
    public static String encrypt(String pub, byte[] plaintext) throws
            IOException, InvalidCipherTextException {
        return b64e(cypher(pub, plaintext, true));
    }
    public static byte[] decrypt(String priv, String cyphertext) throws
            IOException, InvalidCipherTextException {
        return cypher(priv, b64d(cyphertext), false);
    }
    public static  String b64e(byte[] input) {
        return b64enc.encode(input).replaceAll("\\s", "");
    }
    public static byte[] b64d(String input) {
        try {
            return b64dec.decodeBuffer(input);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
