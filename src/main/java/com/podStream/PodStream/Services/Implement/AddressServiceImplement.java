package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.DTOS.AddressDTO;
import com.podStream.PodStream.Models.Address;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Repositories.AddressRepository;
import com.podStream.PodStream.Services.AddressService;
import jakarta.persistence.EntityNotFoundException;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    public AddressDTO findById(Long id) {
        logger.info("Fetching all address with id: {}", id);
        return new AddressDTO(addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Addess not found whit id: " + id)));
    }

    @Override
    public AddressDTO newAddress(Address address) {
        logger.info("Creating Address");
        return new AddressDTO(address);

    }
}
