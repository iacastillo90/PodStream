package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.DTOS.NewClientDTO;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Repositories.Jpa.ClientRepository;
import com.podStream.PodStream.Services.ClientService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ClientServiceImplement implements ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImplement.class);

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "client:";

    @Override
    @Transactional(readOnly = true)
    public List<NewClientDTO> findAll() {
        logger.info("Fetching all clients");
        return clientRepository.findAll()
                .stream()
                .map(NewClientDTO::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NewClientDTO findById(Long id) {
        logger.info("Fetching client with id: {}", id);
        String cacheKey = CACHE_PREFIX + id;
        NewClientDTO cachedClient = (NewClientDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedClient != null) {
            logger.info("Returning cached client with id: {}", id);
            return cachedClient;
        }
        NewClientDTO client = clientRepository.findById(id)
                .map(NewClientDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + id));
        redisTemplate.opsForValue().set(cacheKey, client, 1, TimeUnit.HOURS);
        return client;
    }

    @Override
    @Transactional(readOnly = true)
    public NewClientDTO findByEmail(String email) {
        logger.info("Fetching client with email: {}", email);
        String cacheKey = CACHE_PREFIX + "email:" + email;
        NewClientDTO cachedClient = (NewClientDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedClient != null) {
            logger.info("Returning cached client with email: {}", email);
            return cachedClient;
        }
        NewClientDTO client = clientRepository.findByEmail(email)
                .map(NewClientDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with email: " + email));
        redisTemplate.opsForValue().set(cacheKey, client, 1, TimeUnit.HOURS);
        return client;
    }

    @Override
    @Transactional
    public NewClientDTO createNewClient(NewClientDTO clientDTO) {
        logger.info("Creating new client: {}", clientDTO.getEmail());
        if (clientRepository.existsByEmail(clientDTO.getEmail())) {
            logger.warn("Email already exists: {}", clientDTO.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        if (clientRepository.existsByUsername(clientDTO.getUsername())) {
            logger.warn("Username already exists: {}", clientDTO.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        Client client = clientDTO.toEntity();
        client.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        Client savedClient = clientRepository.save(client);
        NewClientDTO savedClientDTO = new NewClientDTO(savedClient);
        redisTemplate.opsForValue().set(CACHE_PREFIX + savedClient.getId(), savedClientDTO, 1, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(CACHE_PREFIX + "email:" + savedClient.getEmail(), savedClientDTO, 1, TimeUnit.HOURS);
        return savedClientDTO;
    }

    @Override
    @Transactional
    public NewClientDTO unSuscribeClient(Long id) {
        logger.info("Unsuscribing client with id: {}", id);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + id));
        if (!client.getActive()) {
            logger.warn("Client already unsuscribed: {}", id);
            throw new IllegalStateException("Client already unsuscribed");
        }
        client.setActive(false);
        Client savedClient = clientRepository.save(client);
        NewClientDTO clientDTO = new NewClientDTO(savedClient);
        updateCache(clientDTO);
        return clientDTO;
    }

    @Override
    @Transactional
    public NewClientDTO changePassword(Long id, String oldPassword, String newPassword) {
        logger.info("Changing password for client with id: {}", id);
        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 8) {
            logger.warn("Invalid new password for client id: {}", id);
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + id));
        if (!passwordEncoder.matches(oldPassword, client.getPassword())) {
            logger.warn("Invalid old password for client id: {}", id);
            throw new IllegalArgumentException("Invalid old password");
        }
        client.setPassword(passwordEncoder.encode(newPassword));
        Client savedClient = clientRepository.save(client);
        NewClientDTO clientDTO = new NewClientDTO(savedClient);
        updateCache(clientDTO);
        return clientDTO;
    }

    @Override
    @Transactional
    public NewClientDTO updateClient(Long id, NewClientDTO clientDTO) {
        logger.info("Updating client with id: {}", id);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + id));
        if (!clientDTO.getEmail().equals(client.getEmail()) && clientRepository.existsByEmail(clientDTO.getEmail())) {
            logger.warn("Email already exists: {}", clientDTO.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        if (!clientDTO.getUsername().equals(client.getUsername()) && clientRepository.existsByUsername(clientDTO.getUsername())) {
            logger.warn("Username already exists: {}", clientDTO.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        client.setUsername(clientDTO.getUsername());
        client.setFirstname(clientDTO.getFirstname());
        client.setLastname(clientDTO.getLastname());
        client.setEmail(clientDTO.getEmail());
        client.setPhone(clientDTO.getPhone());
        client.setCountry(clientDTO.getCountry());
        client.setActive(clientDTO.getActive());
        if (clientDTO.getPassword() != null && !clientDTO.getPassword().isBlank()) {
            client.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        }
        Client savedClient = clientRepository.save(client);
        NewClientDTO savedClientDTO = new NewClientDTO(savedClient);
        updateCache(savedClientDTO);
        return savedClientDTO;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        logger.info("Deleting client with id: {}", id);
        if (!clientRepository.existsById(id)) {
            logger.warn("Client not found with id: {}", id);
            throw new EntityNotFoundException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
        redisTemplate.delete(CACHE_PREFIX + id);
        redisTemplate.delete(CACHE_PREFIX + "email:" + clientRepository.findById(id).map(Client::getEmail).orElse(""));
    }

    @Override
    @Transactional
    public NewClientDTO reactivateClient(Long id) {
        logger.info("Reactivating client with id: {}", id);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + id));
        if (client.getActive()) {
            logger.warn("Client already reactivated: {}", id);
            throw new IllegalStateException("Client already reactivated");
        }
        client.setActive(true);
        Client savedClient = clientRepository.save(client);
        NewClientDTO clientDTO = new NewClientDTO(savedClient);
        updateCache(clientDTO);
        return clientDTO;
    }

    private void updateCache(NewClientDTO clientDTO) {
        redisTemplate.opsForValue().set(CACHE_PREFIX + clientDTO.getId(), clientDTO, 1, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(CACHE_PREFIX + "email:" + clientDTO.getEmail(), clientDTO, 1, TimeUnit.HOURS);
    }
}