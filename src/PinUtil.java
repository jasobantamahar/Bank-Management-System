
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PinUtil {

    private static final int ITERATIONS = 210000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    public static String hashPin(String pin) throws Exception {

        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
                pin.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hash = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();

        return ITERATIONS + ":"
                + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPin(
            String enteredPin,
            String storedPin) throws Exception {

        if (storedPin == null || storedPin.isEmpty()) {
            return false;
        }

        String[] parts = storedPin.split(":");

        if (parts.length != 3) {
            return false;
        }

        int iterations = Integer.parseInt(parts[0]);

        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(
                enteredPin.toCharArray(),
                salt,
                iterations,
                KEY_LENGTH
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] actualHash = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();

        if (actualHash.length != expectedHash.length) {
            return false;
        }

        int difference = 0;

        for (int i = 0; i < actualHash.length; i++) {
            difference |= actualHash[i] ^ expectedHash[i];
        }

        return difference == 0;
    }
}
