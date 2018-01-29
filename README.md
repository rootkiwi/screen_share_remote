# screen_share_remote
This program is meant to be used together with [screen_share](https://github.com/rootkiwi/screen_share/).

This program can be started on for example your server somewhere, then you share your screen to this program
and this program can forward to connected web clients.

## Usage
```
Usage: java -jar screen_share_remote-<VERSION>.jar [/path/to/conf | genconf | noconf]

Example: java -jar screen_share_remote-<VERSION>.jar genconf

Available command-line options:
1. /path/to/screen_share_remote.conf (start screen_share_remote)
2. genconf                           (generate new config file)
3. noconf                            (start without saving config)
```

## Example generate config
```
$ java -jar screen_share_remote-<VERSION>.jar genconf
Generate screen_share_remote.conf file in working directory which is:
<working_directory>

Will overwrite if already exists

The config file will contain these attributes:
1. port number                 (port number to enter in screen_share)
2. web server port number      (port the web server will serve on)
3. password                    (password to enter in screen_share)
4. self-signed TLS certificate (whose fingerprint to enter in screen_share)
5. RSA private key             (corresponding to certificate)

Do note that the RSA private key is stored in cleartext, so make sure
to make the config file inaccessible for unauthorized parties.
Or you could run in the 'noconf' mode, which means a new private key will be
generated each time. Without saving to disk.

Leave empty and press ENTER for the [default] value
1. enter port number (0-65535) [50000]: 
2. enter web server port number (0-65535) [8081]: 
3. enter password [random]: 

password:
B9p2aPreTRh6ya8fHYtUr5JbMDVCW6veRgzJSbiz

Generating a 4096-bit RSA key pair and a self-signed certificate... done.

certificate fingerprint:
13BEE4D234B31D8EE09542639FA98EEFB3DADCBE39AED4DEAF21C447971F3167

Config file created:
<working_directory>/screen_share_remote.conf

The settings 'port' and 'webPort' is changeable, the rest is not
If you need to change the password/certificate run genconf again
```

## How to use
Download the jar file and run it.

Download here: [screen_share_remote/releases/latest](https://github.com/rootkiwi/screen_share_remote/releases/latest)

Example:
```
java -jar screen_share_remote-<VERSION>.jar
```

## How to build
Run following to output in `build/libs`
```
./gradlew shadowJar
```

## Example NGINX reverse proxy
You may want to set up `screen_share_remote` behind a reverse proxy like NGINX.
WebSockets is used so the reverse proxy need to be set up for that.
Following is an example location config block in NGINX.
```
location / {
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_pass http://localhost:8081;
}
```

## Dependencies
Java 8.

## License
[GNU General Public License 3 or later](https://www.gnu.org/licenses/gpl-3.0.html)

See LICENSE for more details.

## 3RD Party Dependencies

See also [LICENSES-3RD-PARTY](https://github.com/rootkiwi/screen_share/tree/master/LICENSES-3RD-PARTY).

### Eclipse Jetty

[https://www.eclipse.org/jetty/](https://www.eclipse.org/jetty/)

[Apache License 2.0](https://www.eclipse.org/jetty/licenses.html)


### Argon2

[https://github.com/P-H-C/phc-winner-argon2](https://github.com/P-H-C/phc-winner-argon2)

[Apache License 2.0](https://github.com/P-H-C/phc-winner-argon2/blob/master/LICENSE)


### Argon2 Binding for the JVM

[https://github.com/phxql/argon2-jvm](https://github.com/phxql/argon2-jvm)

[GNU Lesser General Public License v3.0](https://github.com/phxql/argon2-jvm/blob/master/LICENSE.txt)


### Bouncy Castle

[https://www.bouncycastle.org/](https://www.bouncycastle.org/)

[MIT License](https://www.bouncycastle.org/licence.html)


### Broadway

[https://github.com/mbebenita/Broadway/](https://github.com/mbebenita/Broadway/)

[3-clause BSD License](https://github.com/mbebenita/Broadway/blob/master/LICENSE)


### Gradle Shadow

[https://github.com/johnrengelman/shadow/](https://github.com/johnrengelman/shadow/)

[Apache License 2.0](https://github.com/johnrengelman/shadow/blob/master/LICENSE)
