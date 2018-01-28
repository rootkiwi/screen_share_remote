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
