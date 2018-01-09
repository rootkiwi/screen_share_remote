/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package crypto;

public interface PasswordHasher {

    /**
     * Generate a password hash, passwordBytes will be zeroed after
     * @param password password bytes
     * @return password hash
     */
    String generatePasswordHash(char[] password);

    boolean validatePassword(String hash, char[] password);

}
