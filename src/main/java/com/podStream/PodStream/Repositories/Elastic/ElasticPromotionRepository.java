package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.Promotion;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de búsqueda para promociones en Elasticsearch en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-09
 */
public interface ElasticPromotionRepository extends ElasticsearchRepository<Promotion, Long> {

    /**
     * Busca promociones por código (coincidencia parcial).
     *
     * @param code El código de la promoción.
     * @return Lista de promociones que coinciden.
     */
    List<Promotion> findByCodeContainingIgnoreCase(String code);

    /**
     * Busca promociones activas válidas hasta una fecha dada.
     *
     * @param date La fecha límite.
     * @return Lista de promociones válidas.
     */
    List<Promotion> findByActiveTrueAndValidUntilAfter(LocalDateTime date);
}