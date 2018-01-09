/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package config;

public class Base64KeyCertHolder {

    byte[] base64Key;
    String base64Certificate;
    public String fingerprint;

    public Base64KeyCertHolder(byte[] base64Key, String base64Certificate, String fingerprint) {
        this.base64Key = base64Key;
        this.base64Certificate = base64Certificate;
        this.fingerprint = fingerprint;
    }
}
