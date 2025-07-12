package com.podStream.PodStream.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Models.User.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NewClientDTO {

    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstname;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastname;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private Role role = Role.CLIENT;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "Customer RUT cannot be empty")
    @Pattern(regexp = "\\d{1,2}\\.\\d{3}\\.\\d{3}-[0-9kK]", message = "Invalid customer RUT")
    private String customerRut;

    @NotBlank(message = "Country is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be a valid ISO 3166-1 alpha-2 code")
    private String country;

    private Boolean active = true;

    public NewClientDTO() {}

    public NewClientDTO(Client client) {
        this.id = client.getId();
        this.username = client.getUsername();
        this.firstname = client.getFirstname();
        this.lastname = client.getLastname();
        this.email = client.getEmail();
        this.role = client.getRole();
        this.phone = client.getPhone();
        this.customerRut = client.getCustomerRut();
        this.country = client.getCountry();
        this.active = client.getActive();
    }

    public Client toEntity() {
        Client client = new Client();
        client.setId(this.id);
        client.setUsername(this.username);
        client.setFirstname(this.firstname);
        client.setLastname(this.lastname);
        client.setPassword(this.password);
        client.setEmail(this.email);
        client.setRole(this.role);
        client.setPhone(this.phone);
        client.setCustomerRut(this.customerRut);
        client.setCountry(this.country);
        client.setActive(this.active);
        return client;
    }
}