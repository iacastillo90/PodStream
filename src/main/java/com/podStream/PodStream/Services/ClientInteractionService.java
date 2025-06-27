package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.ClientInteractionRequest;
import com.podStream.PodStream.Models.ClientInteraction;
import jakarta.validation.Valid;

public interface ClientInteractionService {


    ClientInteraction recordInteraction(@Valid ClientInteractionRequest request);
}

