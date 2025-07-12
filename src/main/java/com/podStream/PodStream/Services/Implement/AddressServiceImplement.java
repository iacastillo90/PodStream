package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.DTOS.AddressDTO;
import com.podStream.PodStream.Models.Address;
import com.podStream.PodStream.Repositories.Jpa.AddressRepository;
import com.podStream.PodStream.Services.AddressService;
import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class AddressServiceImplement implements AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressServiceImplement.class);

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public List<AddressDTO> findAll() {
        logger.info("Fetching all address");
        return addressRepository.findAll()
                .stream()
                .map( address -> new AddressDTO(address))
                .collect(Collectors.toList());
    }

    @Override
    public Address findById(Long id) {
        logger.info("Fetching all address with id: {}", id);
        return addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Addess not found whit id: " + id));
    }

    @Override
    public AddressDTO newAddress(Address address) {
        logger.info("Creating Address");
        return new AddressDTO(address);

    }

    @Override
    public Address update(Address address) {
        logger.info("Updating Address with id: {}", address.getId());
        Address existing = findById(address.getId());
        existing.setApartament(address.getApartament());
        existing.setCity(address.getCity());
        existing.setFloor(address.getFloor());
        existing.setNumber(address.getNumber());
        existing.setStreet(address.getStreet());
        existing.setZipCode(address.getZipCode());

        return addressRepository.save(existing);

    }

    @Override
    public void deleteById(Long id) {
        addressRepository.deleteById(id);
    }
}
