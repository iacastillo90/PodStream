package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.NewClientDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface ClientService {

    List<NewClientDTO> findAll();

    NewClientDTO findById(Long id);

    NewClientDTO findByEmail(String email);

    NewClientDTO createNewClient(@Valid NewClientDTO client);

    NewClientDTO unSuscribeClient(Long id);

    NewClientDTO changePassword(Long id, String oldPassword, String newPassword);

    NewClientDTO updateClient(Long id, @Valid NewClientDTO client);

    void deleteById(Long id);

    NewClientDTO reactivateClient(Long id);
}