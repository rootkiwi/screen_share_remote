/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package password;

public class PasswordGeneratorFactory {

    private PasswordGeneratorFactory() {
    }

    public static PasswordGenerator getGenerator() {
        return new SimplePasswordGenerator();
    }

}
