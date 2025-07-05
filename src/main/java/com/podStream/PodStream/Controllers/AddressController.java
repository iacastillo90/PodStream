package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.AddressDTO;
import com.podStream.PodStream.Models.Address;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Repositories.AddressRepository;
import com.podStream.PodStream.Services.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private AddressService addressService;

    @GetMapping
    @Operation(summary = "Lista de todas las direcciones", description = "Obtiene todas las direcciones disponibles.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getAllAddress() {
        logger.info("Fetching all address");
        return ResponseEntity.ok(ApiResponse.<List<AddressDTO>>success("Address retrieved", addressService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una direccion por ID", description = "Obtiene los detalles de una direccion especifica")
    @PreAuthorize("hasRole('Person')")
    public ResponseEntity<ApiResponse<AddressDTO>> getAddressById (@PathVariable Long id) {
        logger.info("Fetching address with id: {}",id);
        return ResponseEntity.ok(ApiResponse.success("Address retrieved", addressService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva direccion", description = "Crea una nueva direccion")
    @PreAuthorize("hasRole('Person')")
    public ResponseEntity<ApiResponse<AddressDTO>> createAddress(@Valid @RequestBody Address address) {
        logger.info("Creating Address id: {} ", address.getId());
        return new ResponseEntity<>(ApiResponse.success("Address created", addressService.newAddress(address)),  HttpStatus.CREATED );
    }


}

