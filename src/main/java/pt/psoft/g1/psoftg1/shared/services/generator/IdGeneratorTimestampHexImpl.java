package pt.psoft.g1.psoftg1.shared.services.generator;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Profile("timestamp_hex")
@Component
public class IdGeneratorTimestampHexImpl implements IdGenerator{

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generateId() {
        // Timestamp em milissegundos
        long ts = System.currentTimeMillis();

        // Gera um número aleatório entre 0 e 0xFFFFFF (inclusive) e formata como 6 dígitos hex
        int rand = RANDOM.nextInt(0x1000000); // 0 .. 16777215
        String hex = String.format("%06X", rand);

        return ts + "_" + hex;
    }

}
