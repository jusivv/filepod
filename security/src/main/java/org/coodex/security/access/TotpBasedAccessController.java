package org.coodex.security.access;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.HmacHashFunction;
import com.warrenstrange.googleauth.KeyRepresentation;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BaseNCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class TotpBasedAccessController extends ScopeAccessController {
    private static Logger log = LoggerFactory.getLogger(TotpBasedAccessController.class);
    private GoogleAuthenticatorConfig googleAuthenticatorConfig;
    private GoogleAuthenticator googleAuthenticator;

    public TotpBasedAccessController() {
        googleAuthenticatorConfig = new GoogleAuthenticatorConfig();
        googleAuthenticator = new GoogleAuthenticator(googleAuthenticatorConfig);
    }

    @Override
    public boolean writable(String clientId, String token) {
        return authenticate(clientId, token);
    }

    @Override
    public boolean readable(String clientId, String token, String fileId) {
        return authenticate(clientId, token);
    }

    @Override
    public boolean deletable(String clientId, String token, String fileId) {
        return authenticate(clientId, token);
    }

    @Override
    public boolean notify(String clientId, String token, String fileId) {
        // do nothing
        return true;
    }

    @Override
    public boolean accept(String tag) {
        return "totp".equalsIgnoreCase(tag);
    }

    private boolean authenticate(String clientId, String totpKey) {
        try {
            int t = Integer.parseInt(totpKey);
            String secretBase32 =
                    clientConfigGetter.getParameterValue(clientId, "totpSecret", null);
            if (secretBase32 == null || secretBase32.trim().equals("")) {
                log.warn("TOTP secret for client {} is null", clientId);
                return false;
            }
            return googleAuthenticator.authorize(secretBase32, t);
        } catch (NumberFormatException e) {
            log.error("Illegal TOTP key: " + totpKey, e);
            return false;
        }
    }

    private byte[] digestBuff(byte[] buff, String algorithm) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(algorithm).digest(buff);
    }

    private BaseNCodec getBaseNCoder(KeyRepresentation keyRepresentation) {
        switch (keyRepresentation) {
            case BASE64:
                return new Base64();
            default:
                return new Base32();
        }
    }

    public String generateSecret(Object value) throws NoSuchAlgorithmException {
        byte[] bytes = new byte[16];
        if (value instanceof String) {
            bytes = digestBuff(((String) value).getBytes(Charset.forName("UTF-8")), "MD5");
        } else if (value instanceof byte[]) {
            if (((byte[]) value).length == 16) {
                bytes = (byte[]) value;
            } else {
                bytes = digestBuff((byte[]) value, "MD5");
            }
        } else {
            new SecureRandom().nextBytes(bytes);
        }
        return getBaseNCoder(googleAuthenticatorConfig.getKeyRepresentation()).encodeAsString(bytes);
    }

    private String parseAlgorithm(HmacHashFunction hashFunction) {
        switch (hashFunction) {
            case HmacSHA1:
                return "SHA1";
            case HmacSHA256:
                return "SHA256";
            case HmacSHA512:
                return "SHA512";
        }
        return "SHA1";
    }

    public String toGoogleAuthenticatorUri(String accountName, String secretBase32, String issuer) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=%s&digits=%d&period=%d", issuer,
                accountName, secretBase32.replaceAll("=", ""), issuer,
                parseAlgorithm(googleAuthenticatorConfig.getHmacHashFunction()),
                googleAuthenticatorConfig.getCodeDigits(),
                googleAuthenticatorConfig.getTimeStepSizeInMillis() / 1000);
    }
}
