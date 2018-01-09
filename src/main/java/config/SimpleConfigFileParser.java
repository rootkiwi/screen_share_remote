/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package config;

import crypto.PasswordHashFactory;
import crypto.RsaHelper;
import crypto.Sha256Helper;
import crypto.TlsHelper;

import java.io.BufferedReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Base64;

class SimpleConfigFileParser implements ConfigFileParser {

    @Override
    public ConfigHolder parseConfigFile(String configPath) throws Exception {
        String portLine;
        String webPortLine;
        String fingerprintLine;
        String passwordHashLine;
        String certificateLine;
        byte[] privateKeyBytes;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(configPath), StandardCharsets.UTF_8)) {
            portLine = reader.readLine();
            webPortLine = reader.readLine();
            fingerprintLine = reader.readLine();
            passwordHashLine = reader.readLine();
            certificateLine = reader.readLine();
            CharBuffer base64PrivateKeyChars = CharBuffer.allocate(8192);
            int keySize = reader.read(base64PrivateKeyChars);
            if (keySize == -1) {
                throw new Exception("could not read private key");
            }
            if (base64PrivateKeyChars.get(base64PrivateKeyChars.position()-1) == '\n') {
                base64PrivateKeyChars.position(base64PrivateKeyChars.position() - System.lineSeparator().length());
            }
            base64PrivateKeyChars.flip();
            try {
                ByteBuffer base64PrivateKeyBytes = StandardCharsets.UTF_8.encode(base64PrivateKeyChars);
                privateKeyBytes = Base64.getDecoder().decode(base64PrivateKeyBytes).array();
                Arrays.fill(base64PrivateKeyChars.array(), (char)0);
                Arrays.fill(base64PrivateKeyBytes.array(), (byte)0);
            } catch (Exception e) {
                throw new Exception("error decoding private key: " + e.getMessage());
            }
        }

        if (portLine == null
                || webPortLine == null
                || fingerprintLine == null
                || passwordHashLine == null
                || certificateLine == null) {
            throw new Exception("invalid config file");
        }

        int port = parsePort(portLine);
        int webPort = parsePort(webPortLine);

        if (!PasswordHashFactory.getEncodingVerifier().isValidEncodedHash(passwordHashLine)) {
            throw new Exception("invalid password hash");
        }

        byte[] certificateBytes;
        try {
            certificateBytes = Base64.getDecoder().decode(certificateLine);
        } catch (Exception e) {
            throw new Exception("error decoding certificate: " + e.getMessage());
        }

        String[] fingerprintSplit = fingerprintLine.split(":");
        if (fingerprintSplit.length != 2
                || !Sha256Helper.getSha256Fingerprint(certificateBytes).equals(fingerprintSplit[1].trim())) {
            throw new Exception("fingerprint not matching certificate");
        }

        Certificate certificate;
        try {
            certificate = TlsHelper.getCertificateFromBytes(certificateBytes);
        } catch (Exception e) {
            throw new Exception("corrupt certificate in config: " + e.getMessage());
        }

        PrivateKey privateKey = RsaHelper.getPrivateKeyFromBytes(privateKeyBytes);
        Arrays.fill(privateKeyBytes, (byte)0);
        try {
            certificate.verify(RsaHelper.getPublicFromPrivate(privateKey));
        } catch (Exception e) {
            throw new Exception("key and certificate from config does not match");
        }

        return new ConfigHolder(port, webPort, passwordHashLine, certificate, privateKey);
    }

    private static int parsePort(String line) throws Exception {
        String[] split = line.split(":");
        if (split.length == 2) {
            int port;
            try {
                port = Integer.parseInt(split[1].trim());
                if (port >= 0 && port <= 65535) {
                    return port;
                }
            } catch (NumberFormatException e) {}
        }
        throw new Exception("invalid line: " + line);
    }

}
