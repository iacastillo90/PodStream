package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.AddressDTO;
import com.podStream.PodStream.Models.Address;
import jakarta.validation.Valid;

import java.util.List;

public interface AddressService {

    List<AddressDTO> findAll();

    AddressDTO findById(Long id);

    AddressDTO newAddress(@Valid Address address);
}

