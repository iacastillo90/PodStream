package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.Answers;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Repositorio de búsqueda para respuestas en Elasticsearch en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */

public interface ElasticAnswerRepository extends ElasticsearchRepository<Answers, Long> {

    List<Answers> findByBodyContaining(String body);


}
