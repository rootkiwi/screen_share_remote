/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package crypto;

import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class RsaHelper {

    public static PrivateKey getPrivateKeyFromBytes(byte[] privateKeyBytes) {
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("rsa error1", e);
        }
    }

    public static PublicKey getPublicFromPrivate(PrivateKey privateRSAKey) {
        RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey) privateRSAKey;
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                rsaPrivateCrtKey.getModulus(),
                rsaPrivateCrtKey.getPublicExponent()
        );
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(publicKeySpec);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("rsa error2", e);
        }
    }

    public static KeyPair generateRsaKeyPair() {
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("rsa error3", e);
        }
        kpg.initialize(4096);
        return kpg.generateKeyPair();
    }

}
