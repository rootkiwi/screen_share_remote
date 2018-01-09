/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package crypto;

public class PasswordHashFactory {

    private PasswordHashFactory() {
    }

    public static PasswordHasher getHasher() {
        return new Argon2PasswordHasher();
    }

    public static PasswordHashEncodingVerifier getEncodingVerifier() {
        return new Argon2PasswordHashVerifier();
    }

}
