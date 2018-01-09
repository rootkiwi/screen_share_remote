/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package password;

import java.security.SecureRandom;

class SimplePasswordGenerator implements PasswordGenerator {

    @Override
    public char[] generatePassword() {
        char[] password = new char[40];
        String alphabet = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijklmnpqrstuvwxyz";
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < password.length; i++) {
            int randomIndex = secureRandom.nextInt(alphabet.length());
            password[i] = alphabet.charAt(randomIndex);
        }

        return password;
    }

}
