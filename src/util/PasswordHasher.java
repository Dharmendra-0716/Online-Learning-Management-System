package util;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;

    private PasswordHasher() {
    }

    public static String hash(String password) {

        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);

        try {

            byte[] hash =
                    derive(password.toCharArray(), salt, ITERATIONS);

            return ITERATIONS + ":"
                    + Base64.getEncoder().encodeToString(salt)
                    + ":"
                    + Base64.getEncoder().encodeToString(hash);

        } catch (GeneralSecurityException exception) {

            throw new IllegalStateException(
                    "Unable to secure the password.",
                    exception
            );
        }
    }

    public static boolean verify(
            String password,
            String storedHash) {

        try {

            String[] parts = storedHash.split(":");

            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);

            byte[] salt =
                    Base64.getDecoder().decode(parts[1]);

            byte[] expectedHash =
                    Base64.getDecoder().decode(parts[2]);

            byte[] actualHash =
                    derive(
                            password.toCharArray(),
                            salt,
                            iterations
                    );

            return java.security.MessageDigest.isEqual(
                    actualHash,
                    expectedHash
            );

        } catch (Exception exception) {

            return false;
        }
    }

    private static byte[] derive(
            char[] password,
            byte[] salt,
            int iterations)
            throws GeneralSecurityException {

        PBEKeySpec specification =
                new PBEKeySpec(
                        password,
                        salt,
                        iterations,
                        HASH_BITS
                );

        try {

            return SecretKeyFactory
                    .getInstance(ALGORITHM)
                    .generateSecret(specification)
                    .getEncoded();

        } finally {

            specification.clearPassword();
        }
    }
}