package pt.psoft.g1.psoftg1.shared.services.generator;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Profile("base65")
@Component
public class IdGeneratorBase65EncodeImpl implements IdGenerator{

    // Alfabeto com 65 caracteres: 0-9, A-Z, a-z, -, _, +
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_+".toCharArray();
    private static final int BASE = ALPHABET.length; // 65
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generateId() {
        // Gera um número aleatório de 64 bits não-negativo
        long value = RANDOM.nextLong();
        // Torna não-negativo (inclui caso Long.MIN_VALUE)
        value = value == Long.MIN_VALUE ? 0L : Math.abs(value);

        return encodeBase65(value);
    }

    // Codifica um valor long não-negativo em base65
    private String encodeBase65(long value) {
        if (value == 0L) {
            return String.valueOf(ALPHABET[0]);
        }

        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            int digit = (int) (value % BASE);
            sb.append(ALPHABET[digit]);
            value /= BASE;
        }

        return sb.reverse().toString();
    }

}
