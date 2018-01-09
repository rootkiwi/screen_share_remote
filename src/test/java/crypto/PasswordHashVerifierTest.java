package crypto;

import org.junit.Test;
import password.PasswordGenerator;
import password.PasswordGeneratorFactory;

import static org.junit.Assert.*;

public class PasswordHashVerifierTest {

    @Test
    public void isValidEncodedHash() {
        PasswordHashEncodingVerifier hashVerifier = PasswordHashFactory.getEncodingVerifier();
        PasswordHasher passwordHasher = PasswordHashFactory.getHasher();
        PasswordGenerator passwordGenerator = PasswordGeneratorFactory.getGenerator();

        for (int i = 0; i < 3; i++) {
            assertTrue(
                    hashVerifier.isValidEncodedHash(
                            passwordHasher.generatePasswordHash(passwordGenerator.generatePassword())
                    )
            );
        }

        String[] invalidEncodedHashes = {
                "argon2i$v=19$m=65536,t=2,p=1$xGtirKuVUVg2LCT+3381tw$+3Rx2BbgYB3IUhhTdpfquVtGfrhItH29eIS3yKrM5/o",
                "$argon2$v=19$m=65536,t=2,p=1$xGtirKuVUVg2LCT+3381tw$+3Rx2BbgYB3IUhhTdpfquVtGfrhItH29eIS3yKrM5/o",
                "$argon2i$v=18$m=65536,t=2,p=1$xGtirKuVUVg2LCT+3381tw$+3Rx2BbgYB3IUhhTdpfquVtGfrhItH29eIS3yKrM5/o",
                "$argon2i$v=19$m=6553,t=2,p=1$xGtirKuVUVg2LCT+3381tw$+3Rx2BbgYB3IUhhTdpfquVtGfrhItH29eIS3yKrM5/o",
                "$argon2i$v=19$m=65536,t=3,p=1$xGtirKuVUVg2LCT+3381tw$+3Rx2BbgYB3IUhhTdpfquVtGfrhItH29eIS3yKrM5/o",
                "$argon2i$v=19$m=65536,t=2,p=$xGtirKuVUVg2LCT+3381tw$+3Rx2BbgYB3IUhhTdpfquVtGfrhItH29eIS3yKrM5/o",
                "$argon2i$v=19$m=65536,t=2,p=1xGtirKuVUVg2LCT+3381tw$+3Rx2BbgYB3IUhhTdpfquVtGfrhItH29eIS3yKrM5/o",
                "$argon2i$v=19$m=65536,t=2,p=1$xGtirKuVUg2LCT+3381tw$+3Rx2BbgYB3IUhhTdpfquVtGfrhItH29eIS3yKrM5/o",
                "$argon2i$v=19$m=65536,t=2,p=1$xGtirKuVUVg2LCT+3381tw+3Rx2BbgYB3IUhhTdpfquVtGfrhItH29eIS3yKrM5/o",
                "$argon2i$v=19$m=65536,t=2,p=1$xGtirKuVUVg2LCT+3381tw$+3Rx2BbgYB3IUhhTdpfquVtGfrhItH29e3yKrM5/o",
                "$argon2i$v=19$m=65535,t=2,p=1$xGtirKuVUVg2LCT+3381tw$+3Rx2BbgYB3IUhhTdpfquVtGfrhItH29eIS3yKrM5/o",
                "",
                " ",
                "\n",
        };
        for (String invalid : invalidEncodedHashes) {
            assertFalse(hashVerifier.isValidEncodedHash(invalid));
        }
    }

}