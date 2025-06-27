package com.podStream.PodStream.Repositories;


import com.podStream.PodStream.Models.User.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface UserRepository extends JpaRepository<Person,Long> {
    Optional<Person> findByUsername(String username);

    Optional<Person> findByEmail(String email);
}
