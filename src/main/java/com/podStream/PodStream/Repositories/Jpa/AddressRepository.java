package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
