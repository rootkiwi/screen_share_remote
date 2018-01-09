/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package crypto;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static crypto.Argon2PasswordHasher.HASH_OUTPUT_LENGTH;
import static crypto.Argon2PasswordHasher.SALT_LENGTH;

public class Argon2PasswordHashVerifier implements PasswordHashEncodingVerifier {

    @Override
    public boolean isValidEncodedHash(String hash) {
        // valid example:
        // $argon2i$v=19$m=65536,t=2,p=1$xGtirKuVUVg2LCT+3381tw$+3Rx2BbgYB3IUhhTdpfquVtGfrhItH29eIS3yKrM5/o
        String[] split = hash.split("\\$");
        if (split.length != 6) {
            return false;
        }
        return isValidType(split[1])
                && isValidVersion(split[2])
                && isValidSettings(split[3])
                && isValidSaltAndHashOutput(split[4], split[5]);
    }

    private static boolean isValidType(String type) {
        return type.equals("argon2i");
    }

    private static boolean isValidVersion(String versionString) {
        List<Integer> validVersions = new ArrayList<>();
        validVersions.add(0x13);
        int version;
        try {
            version = Integer.parseInt(versionString.split("=")[1]);
        } catch (Exception e) {
            return false;
        }
        return validVersions.contains(version);
    }

    private static boolean isValidSettings(String settings) {
        List<String> validSettings = new ArrayList<>();
        validSettings.add("m=65536,t=2,p=1");
        return validSettings.contains(settings);
    }

    private static boolean isValidSaltAndHashOutput(String saltString, String hashOutputString) {
        try {
            Base64.Decoder b64decoder = Base64.getDecoder();
            byte[] salt = b64decoder.decode(saltString);
            byte[] hashOutput = b64decoder.decode(hashOutputString);
            return salt.length == SALT_LENGTH || hashOutput.length == HASH_OUTPUT_LENGTH;
        } catch (Exception e) {
            return false;
        }
    }

}
