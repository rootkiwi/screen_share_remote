/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Sha256Helper {

    public static String getSha256Fingerprint(byte[] bytes) {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error getting SHA-256 MessageDigest", e);
        }
        sha256.update(bytes);
        byte[] hash = sha256.digest();
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02X", b);
        }
        return formatter.toString();
    }

}
