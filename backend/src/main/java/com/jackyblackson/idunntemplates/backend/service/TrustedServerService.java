package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.dto.CreateServerDto;
import com.jackyblackson.idunntemplates.backend.dto.TrustedServerDto;
import com.jackyblackson.idunntemplates.backend.domain.TrustedServer;
import com.jackyblackson.idunntemplates.backend.store.repository.TrustedServerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TrustedServerService {

    private final TrustedServerRepository trustedServerRepository;

    public TrustedServerService(TrustedServerRepository trustedServerRepository) {
        this.trustedServerRepository = trustedServerRepository;
    }

    @Transactional
    public TrustedServerDto createServer(CreateServerDto dto, String username) {
        TrustedServer server = new TrustedServer();
        server.setName(dto.getName());
        server.setRemarks(dto.getRemarks());
        server.setToken(UUID.randomUUID().toString());
        server.setCreatedAt(Instant.now().toEpochMilli());
        server.setCreatedByUsername(username);

        server = trustedServerRepository.save(server);
        return TrustedServerDto.fromEntity(server);
    }

    public List<TrustedServerDto> getAllServers() {
        return trustedServerRepository.findAll()
                .stream()
                .map(TrustedServerDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteServer(Long id) {
        trustedServerRepository.deleteById(id);
    }
}
