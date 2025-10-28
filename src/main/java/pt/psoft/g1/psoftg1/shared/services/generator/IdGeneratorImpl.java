package pt.psoft.g1.psoftg1.shared.services.generator;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Component
public class IdGeneratorImpl implements IdGenerator{

    @Override
    public String generateId(String entityType) {
        String randomUUID = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("%s-%s", entityType, randomUUID);
    }

}
