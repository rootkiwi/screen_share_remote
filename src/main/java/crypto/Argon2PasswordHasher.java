/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package crypto;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.nio.charset.StandardCharsets;

import static de.mkammerer.argon2.Argon2Factory.Argon2Types.ARGON2i;

class Argon2PasswordHasher implements PasswordHasher {

    static final int SALT_LENGTH = 16;
    static final int HASH_OUTPUT_LENGTH = 32;

    private Argon2 argon2;

    Argon2PasswordHasher() {
        argon2 = Argon2Factory.create(ARGON2i, SALT_LENGTH, HASH_OUTPUT_LENGTH);
    }

    @Override
    public String generatePasswordHash(char[] password) {
        String hash;
        try {
            hash = argon2.hash(2, 65536, 1, password, StandardCharsets.UTF_8);
        } finally {
            argon2.wipeArray(password);
        }
        return hash;
    }

    @Override
    public boolean validatePassword(String hash, char[] password) {
        boolean matches = false;
        try {
            if (argon2.verify(hash, password, StandardCharsets.UTF_8)) {
                matches = true;
            }
        } finally {
            argon2.wipeArray(password);
        }
        return matches;
    }

}
