package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.User.Client;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ElasticClientRepository extends ElasticsearchRepository<Client, Long> {
    List<Client> findByFirstnameContainingOrLastnameContainingOrEmailContaining(String firstname, String lastname, String email);
}