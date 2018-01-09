package crypto;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PasswordHasherTest {

    private PasswordHasher passwordHasher;

    @Before
    public void setUp() {
        passwordHasher = PasswordHashFactory.getHasher();
    }

    @Test
    public void validatePassword() {
        char[] password = "nt¦yvöDû¢{èk4h6åL)>ÔR<æµ&Ä#>Úëû[Hµo¢]eî".toCharArray();
        String hash = passwordHasher.generatePasswordHash(password);
        assertTrue(passwordHasher.validatePassword(hash, "nt¦yvöDû¢{èk4h6åL)>ÔR<æµ&Ä#>Úëû[Hµo¢]eî".toCharArray()));

        char[] password2 = "nt¦yvöDû¢{èk4h6åL)>ÔR<µ&Ä#>Úëû[Hµo¢]eî".toCharArray();
        String hash2 = passwordHasher.generatePasswordHash(password2);
        assertFalse(passwordHasher.validatePassword(hash2, "nt¦yvöDû¢{èk4h6åL)>ÔR<æµ&Ä#>Úëû[Hµo¢]eî".toCharArray()));

        char[] password3 = "g".toCharArray();
        String hash3 = passwordHasher.generatePasswordHash(password3);
        assertTrue(passwordHasher.validatePassword(hash3, "g".toCharArray()));

        char[] password4 = "".toCharArray();
        String hash4 = passwordHasher.generatePasswordHash(password4);
        assertFalse(passwordHasher.validatePassword(hash4, "g".toCharArray()));
    }

    @Test
    public void testWipe() {
        char[] password = "password".toCharArray();
        String hash = passwordHasher.generatePasswordHash(password);
        for (char c : password) {
            if (c != 0) {
                fail();
            }
        }

        password = "password".toCharArray();
        passwordHasher.validatePassword(hash, password);
        for (char c : password) {
            if (c != 0) {
                fail();
            }
        }
    }

}
