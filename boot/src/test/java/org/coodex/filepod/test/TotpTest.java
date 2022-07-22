package org.coodex.filepod.test;

import org.coodex.security.access.TotpBasedAccessController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;

public class TotpTest {
    private static Logger log = LoggerFactory.getLogger(TotpTest.class);

    public static void main(String[] args) throws NoSuchAlgorithmException {
        TotpBasedAccessController accessController = new TotpBasedAccessController();
        String secret = accessController.generateSecret(null);
        log.debug("secret: {}", secret);
        log.debug("uri: {}", accessController.toGoogleAuthenticatorUri("testTotp", secret, "Filepod"));
    }
}
