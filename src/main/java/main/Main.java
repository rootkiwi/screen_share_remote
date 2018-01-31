/*
 * Copyright 2018 rootkiwi
 *
 * screen_share_remote is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package main;

import config.Base64KeyCertHolder;
import config.ConfigHandler;
import config.ConfigHolder;
import crypto.*;
import password.PasswordGeneratorFactory;
import remote.RemoteConnectionHandler;
import remote.RemoteHandler;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;

public class Main {

    private static final int DEFAULT_PORT = 50000;
    private static final int DEFAULT_WEB_PORT = 8081;

    public static void main(String[] args) {
        if (args.length == 1) {
            String param = args[0].trim();
            if (isGenconf(param)) {
                handleGenconf();
            } else if (isNoconf(param)) {
                handleNoconf();
            } else {
                handleStart(param);
            }
        } else {
            System.out.println("Usage: java -jar screen_share_remote-<VERSION>.jar [/path/to/conf | genconf | noconf]");
            System.out.println();
            System.out.println("Example: java -jar screen_share_remote-<VERSION>.jar genconf");
            System.out.println();
            System.out.println("Available command-line options:");
            System.out.println("1. /path/to/screen_share_remote.conf (start screen_share_remote)");
            System.out.println("2. genconf                           (generate new config file)");
            System.out.println("3. noconf                            (start without saving config)");
        }
    }

    private static boolean isGenconf(String param) {
        return param.equals("genconf");
    }

    private static boolean isNoconf(String param) {
        return param.equals("noconf");
    }

    private static void handleGenconf() {
        printGenconfInfo();
        Console console = System.console();
        if (console == null) {
            System.err.println("Couldn't get console instance");
            return;
        }
        int port = readPortNumber(console);
        int webPort = readWebPortNumber(console);
        String passwordHash = readPasswordHash(console);
        System.out.print("Generating a 4096-bit RSA key pair and a self-signed certificate...");
        Base64KeyCertHolder holder = TlsHelper.generateBase64KeyAndCertForConfig();
        System.out.println(" done.");
        System.out.println();
        System.out.println("certificate fingerprint:");
        System.out.println(holder.fingerprint);
        Path configFile = Paths.get("screen_share_remote.conf");
        try {
            ConfigHandler.getCreator().createConfigFile(port, webPort, passwordHash, holder, configFile);
        } catch (IOException e) {
            System.out.println();
            System.err.println("Error creating config file: " + e.toString());
            return;
        }
        System.out.println();
        System.out.println("Config file created:");
        System.out.println(Paths.get("", configFile.toString()).toAbsolutePath());
        System.out.println();
        System.out.println("The settings 'port' and 'webPort' is changeable, the rest is not");
        System.out.println("If you need to change the password/certificate run genconf again");
    }

    private static void handleNoconf() {
        printNoconfInfo();
        Console console = System.console();
        if (console == null) {
            System.err.println("Couldn't get console instance");
            return;
        }
        int port = readPortNumber(console);
        int webPort = readWebPortNumber(console);
        String passwordHash = readPasswordHash(console);

        System.out.print("Generating a 4096-bit RSA key pair and a self-signed certificate...");
        KeyPair keyPair = RsaHelper.generateRsaKeyPair();
        Certificate certificate = TlsHelper.generateSelfSignedCertificate(keyPair);
        System.out.println(" done.");
        System.out.println();
        System.out.println("certificate fingerprint:");
        try {
            System.out.println(Sha256Helper.getSha256Fingerprint(certificate.getEncoded()));
        } catch (CertificateEncodingException e) {
            throw new RuntimeException("error getting encoded cert", e);
        }
        System.out.println();

        start(new ConfigHolder(port, webPort, passwordHash, certificate, keyPair.getPrivate()));
    }

    private static void printGenconfInfo() {
        System.out.println("Generate screen_share_remote.conf file in working directory which is:");
        System.out.println(Paths.get("").toAbsolutePath());
        System.out.println();
        System.out.println("Will overwrite if already exists");
        System.out.println();
        System.out.println("The config file will contain these attributes:");
        printConfigItems();
        System.out.println();
        System.out.println("Do note that the RSA private key is stored in cleartext, so make sure");
        System.out.println("to make the config file inaccessible for unauthorized parties.");
        System.out.println("Or you could run in the 'noconf' mode, which means a new private key will be");
        System.out.println("generated each time. Without saving to disk.");
        printInputInfo();
    }

    private static void printNoconfInfo() {
        System.out.println("Start in 'noconf' mode, no config will be saved to disk");
        System.out.println();
        System.out.println("These attributes are needed:");
        printConfigItems();
        printInputInfo();
    }

    private static void printConfigItems() {
        System.out.println("1. port number                 (port number to enter in screen_share)");
        System.out.println("2. web server port number      (port the web server will serve on)");
        System.out.println("3. password                    (password to enter in screen_share)");
        System.out.println("4. self-signed TLS certificate (whose fingerprint to enter in screen_share)");
        System.out.println("5. RSA private key             (corresponding to certificate)");
    }

    private static void printInputInfo() {
        System.out.println();
        System.out.println("Leave empty and press ENTER for the [default] value");
    }

    private static int readPortNumber(Console console) {
        return readPort(console, 1, "port", DEFAULT_PORT);
    }

    private static int readWebPortNumber(Console console) {
        return readPort(console, 2, "web server port", DEFAULT_WEB_PORT);
    }

    private static int readPort(Console console, int num, String portName, int defaulPort) {
        while (true) {
            String webPortInput = console.readLine("%d. enter %s number (0-65535) [%d]: ", num, portName, defaulPort);
            if (webPortInput.length() == 0) {
                return defaulPort;
            } else {
                try {
                    int parsedWebPort = Integer.parseInt(webPortInput.trim());
                    if (parsedWebPort >= 0 && parsedWebPort <= 65535) {
                        return parsedWebPort;
                    } else {
                        System.out.printf("invalid %s number (0-65535)%n%n", portName);
                    }
                } catch (NumberFormatException nfe) {
                    System.out.printf("invalid %s number (not a number)%n%n", portName);
                }
            }
        }
    }

    private static String readPasswordHash(Console console) {
        PasswordHasher passwordHasher = PasswordHashFactory.getHasher();
        while (true) {
            char[] passwordInput = console.readPassword("3. enter password [random]: ");
            if (passwordInput.length == 0) {
                char[] randomPassword = PasswordGeneratorFactory.getGenerator().generatePassword();
                System.out.printf("%npassword:%n");
                System.out.println(randomPassword);
                System.out.println();
                return passwordHasher.generatePasswordHash(randomPassword);
            } else {
                char[] passwordInputAgain = console.readPassword("3. enter password again: ");
                if (Arrays.equals(passwordInput, passwordInputAgain)) {
                    System.out.println();
                    Arrays.fill(passwordInputAgain, (char)0);
                    return passwordHasher.generatePasswordHash(passwordInput);
                } else {
                    System.out.printf("passwords did not match%n%n");
                    Arrays.fill(passwordInput, (char)0);
                    Arrays.fill(passwordInputAgain, (char)0);
                }
            }
        }
    }

    private static void handleStart(String configPath) {
        ConfigHolder config;
        try {
            config = ConfigHandler.getParser().parseConfigFile(configPath);
            start(config);
        } catch (Exception e) {
            System.out.println("failed to parse config file: " + e.toString());
        }
    }

    private static void start(ConfigHolder config) {
        RemoteConnectionHandler remoteHandler = new RemoteHandler();
        Runtime.getRuntime().addShutdownHook(new Thread(remoteHandler::stop));
        System.out.println("started");
        remoteHandler.start(config);
    }

}
