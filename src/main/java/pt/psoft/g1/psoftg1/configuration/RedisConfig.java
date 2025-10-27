package pt.psoft.g1.psoftg1.configuration;

import java.net.URI;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.redis.url:}")
    private String redisUrl;

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${app.cache.ttl-seconds:3600}")
    private long cacheTtlSeconds;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config;

        if (redisUrl != null && !redisUrl.isBlank()) {
            try {
                URI uri = new URI(redisUrl);
                String host = uri.getHost() != null ? uri.getHost() : redisHost;
                int port = uri.getPort() > 0 ? uri.getPort() : redisPort;

                config = new RedisStandaloneConfiguration(host, port);

                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isBlank()) {
                    // userInfo may be "password" or "user:password" or ":password"
                    String password = userInfo;
                    if (userInfo.contains(":")) {
                        password = userInfo.substring(userInfo.indexOf(':') + 1);
                    }
                    if (!password.isBlank()) {
                        config.setPassword(RedisPassword.of(password));
                    }
                }
            } catch (Exception ex) {
                // If parsing fails, fall back to host/port/password
                config = new RedisStandaloneConfiguration(redisHost, redisPort);
                if (redisPassword != null && !redisPassword.isBlank()) {
                    config.setPassword(RedisPassword.of(redisPassword));
                }
            }
        } else {
            config = new RedisStandaloneConfiguration(redisHost, redisPort);
            if (redisPassword != null && !redisPassword.isBlank()) {
                config.setPassword(RedisPassword.of(redisPassword));
            }
        }

        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(LettuceConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(cacheTtlSeconds))
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
    }
}
