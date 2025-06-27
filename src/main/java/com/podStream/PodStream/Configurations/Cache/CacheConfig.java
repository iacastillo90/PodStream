package com.podStream.PodStream.Configurations.Cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.List;


/**
 * Configuración del sistema de caché para el proyecto PodStream.
 * <p>Habilita el soporte de caché en Spring y configura {@link CacheManager} según el perfil activo:
 * <ul>
 *     <li><b>prod</b>: Usa Redis con serialización JSON y TTL configurable.</li>
 *     <li><b>dev/test</b>: Usa Caffeine con TTL y tamaño máximo configurables.</li>
 * </ul>
 * Define cachés para recomendaciones de usuarios y productos.
 *
 * @author [Tu Nombre o Equipo PodStream]
 * @since 1.0.0-SNAPSHOT
 */


@EnableCaching
public class CacheConfig {

    // Nombres de los cachés
    private static final List<String> CACHE_NAMES = List.of(
            "userRecommendations",
            "itemRecommendations"
    );

    // Propiedades configurables desde application.yml
    @Value("${podstream.cache.ttl-minutes:10}")
    private long cacheTtlMinutes;

    @Value("${podstream.cache.caffeine.max-size:1000}")
    private long caffeineMaxSize;


/**
     * Configura el CacheManager para producción usando Redis.
     *
     * @param redisConnectionFactory Conexión a Redis.
     * @param objectMapper ObjectMapper para personalizar la serialización JSON.
     * @return CacheManager basado en Redis.
     */

    @Bean
    @Profile("prod")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(cacheTtlMinutes))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig);

        // Configurar cachés específicos
        CACHE_NAMES.forEach(cacheName ->
                builder.withCacheConfiguration(cacheName, defaultConfig));

        return builder.build();
    }


/**
     * Configura el CacheManager para desarrollo y pruebas usando Caffeine.
     *
     * @return CacheManager basado en Caffeine.
     */

    @Bean
    @Profile({"dev", "test"})
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(CACHE_NAMES);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(cacheTtlMinutes))
                .maximumSize(caffeineMaxSize));
        return cacheManager;
    }


/**
     * Proporciona un ObjectMapper para la serialización en Redis.
     * <p>Puede personalizarse según las necesidades del proyecto.
     *
     * @return ObjectMapper configurado.
     */

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

