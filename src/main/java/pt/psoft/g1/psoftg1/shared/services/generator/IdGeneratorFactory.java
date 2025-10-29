package pt.psoft.g1.psoftg1.shared.services.generator;



import org.springframework.stereotype.Component;

@Component
    public class IdGeneratorFactory {

    public IdGenerator getGenerator() {
        // If the Spring ApplicationContext isn't available (e.g. plain unit tests),
        // fall back to a simple concrete implementation so code that needs an
        // IdGenerator can still run without a configured context.
        var ctx = ApplicationContextProvider.getApplicationContext();
        if (ctx == null) {
            return new IdGeneratorImpl();
        }
        try {
            return ctx.getBean(IdGenerator.class);
        } catch (Exception e) {
            // If no bean is present for any reason, fall back to the default impl.
            return new IdGeneratorImpl();
        }

    }
}
