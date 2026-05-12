package com.jackyblackson.idunntemplates.backend.migration;

import com.jackyblackson.idunntemplates.backend.service.LocalAuthService;
import com.jackyblackson.idunntemplates.backend.store.repository.UserRepository;
import com.jackyblackson.idunntemplates.core.IdunnConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultSuperUserInitializer implements CommandLineRunner {

    private static final String DEFAULT_USERNAME = "SuperUser";
    private static final String DEFAULT_PASSWORD = "Cdf9988423";

    private final UserRepository userRepository;
    private final LocalAuthService localAuthService;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            localAuthService.registerUser(IdunnConstants.SUPER_USER_UUID, DEFAULT_USERNAME, DEFAULT_PASSWORD);
            log.info("No users found. Created default SuperUser account.");
            return;
        }

        userRepository.findByUsername(DEFAULT_USERNAME).ifPresent(user -> {
            if (IdunnConstants.SUPER_USER_UUID.equalsIgnoreCase(user.getUuid())) {
                return;
            }
            if (userRepository.findByUuid(IdunnConstants.SUPER_USER_UUID).isPresent()) {
                log.warn("SuperUser UUID was not updated because {} is already assigned.", IdunnConstants.SUPER_USER_UUID);
                return;
            }

            user.setUuid(IdunnConstants.SUPER_USER_UUID);
            userRepository.save(user);
            log.info("Updated SuperUser UUID to the reserved super-user UUID.");
        });
    }
}
