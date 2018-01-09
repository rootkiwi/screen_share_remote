/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package config;

import java.security.PrivateKey;
import java.security.cert.Certificate;

public class ConfigHolder {

    public int port;
    public int webPort;
    public String passwordHash;
    public Certificate certificate;
    public PrivateKey privateKey;

    public ConfigHolder(int port, int webPort, String passwordHash, Certificate certificate, PrivateKey privateKey) {
        this.port = port;
        this.webPort = webPort;
        this.passwordHash = passwordHash;
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

}
