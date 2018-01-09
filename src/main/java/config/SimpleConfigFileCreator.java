/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

class SimpleConfigFileCreator implements ConfigFileCreator {

    @Override
    public void createConfigFile(int port,
                                 int webPort,
                                 String passwordHash,
                                 Base64KeyCertHolder holder,
                                 Path configFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            writer.write("port    (changeable): " + port);
            writer.newLine();
            writer.write("webPort (changeable): " + webPort);
            writer.newLine();
            writer.write("fingerprint: " + holder.fingerprint);
            writer.newLine();
            writer.write(passwordHash);
            writer.newLine();
            writer.write(holder.base64Certificate);
            writer.newLine();
            char[] keyUtf8 = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(holder.base64Key)).array();
            writer.write(keyUtf8);
            writer.newLine();
            Arrays.fill(keyUtf8, (char)0);
        }
        Arrays.fill(holder.base64Key, (byte)0);
    }

}
