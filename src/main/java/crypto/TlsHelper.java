/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package crypto;

import config.Base64KeyCertHolder;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static crypto.RsaHelper.generateRsaKeyPair;

public class TlsHelper {

    public static final String[] TLSv12 = new String[]{"TLSv1.2"};
    public static final String[] TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 = new String[]{"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"};

    public static Base64KeyCertHolder generateBase64KeyAndCertForConfig() {
        KeyPair keyPair = generateRsaKeyPair();
        byte[] certificateBytes;
        try {
            certificateBytes = generateSelfSignedCertificate(keyPair).getEncoded();
        } catch (CertificateEncodingException e) {
            throw new RuntimeException("error getting encoded certificate", e);
        }
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();

        Base64.Encoder encoder = Base64.getEncoder();
        byte[] base64Key = encoder.encode(privateKeyBytes);
        String base64Certificate = new String(encoder.encode(certificateBytes), StandardCharsets.UTF_8);
        String fingerprint = Sha256Helper.getSha256Fingerprint(certificateBytes);
        Arrays.fill(privateKeyBytes, (byte)0);

        return new Base64KeyCertHolder(base64Key, base64Certificate, fingerprint);
    }

    public static Certificate getCertificateFromBytes(byte[] certificateBytes) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        X509CertificateHolder certificateHolder = new X509CertificateHolder(certificateBytes);
        return new JcaX509CertificateConverter().getCertificate(certificateHolder);
    }

    public static SSLServerSocket getTlsServerSocket(Certificate certificate, PrivateKey privateKey) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setKeyEntry("", privateKey, "".toCharArray(), new Certificate[]{certificate});

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "".toCharArray());

        SSLContext tlsContext = SSLContext.getInstance(TLSv12[0]);
        tlsContext.init(keyManagerFactory.getKeyManagers(), null, null);

        return (SSLServerSocket) tlsContext.getServerSocketFactory().createServerSocket();
    }

    public static Certificate generateSelfSignedCertificate(KeyPair keyPair) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date notBefore = calendar.getTime();
        calendar.add(Calendar.YEAR, 10);
        Date notAfter = calendar.getTime();
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, "screen_share_remote");
        nameBuilder.addRDN(BCStyle.SERIALNUMBER, new BigInteger(128, ThreadLocalRandom.current()).toString(16));

        X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                nameBuilder.build(),
                BigInteger.ONE,
                notBefore, notAfter,
                nameBuilder.build(),
                keyPair.getPublic()
        );
        Security.addProvider(new BouncyCastleProvider());
        try {
            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(keyPair.getPrivate());
            X509Certificate certificate = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .getCertificate(certificateBuilder.build(contentSigner));
            return certificate;
        } catch (Exception e) {
            throw new RuntimeException("Error generating self-signed certificate", e);
        }
    }

}
