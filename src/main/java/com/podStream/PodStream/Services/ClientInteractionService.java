package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.ClientInteractionDTO;
import com.podStream.PodStream.DTOS.ClientInteractionRequest;
import com.podStream.PodStream.Models.ClientInteraction;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ClientInteractionService {
    ClientInteractionDTO recordInteraction(ClientInteractionRequest request, Authentication authentication);
    ClientInteractionDTO getInteraction(Long id, Authentication authentication);
    List<ClientInteractionDTO> getInteractionsByClient(Long clientId, Authentication authentication);
    ClientInteractionDTO updateInteraction(Long id, ClientInteractionRequest request, Authentication authentication);
    void deleteInteraction(Long id, Authentication authentication);
}

