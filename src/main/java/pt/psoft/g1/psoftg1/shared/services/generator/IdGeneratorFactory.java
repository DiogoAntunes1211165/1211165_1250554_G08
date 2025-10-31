package pt.psoft.g1.psoftg1.shared.services.generator;



import org.springframework.stereotype.Component;

@Component
    public class IdGeneratorFactory {

    public IdGenerator getGenerator() {
        try {
            return ApplicationContextProvider.getApplicationContext().getBean(IdGenerator.class);
        } catch (Exception e) {
            // If no bean is present for any reason, fall back to the default impl.
            return new IdGeneratorBase65EncodeImpl();
        }
    }
}
