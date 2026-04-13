package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.domain.User;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.store.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocalAuthService {

    private final UserRepository userRepository;

    public LocalAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserContext authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                return new UserContext(user.getUsername(), user.getUuid());
            }
        }
        return null;
    }

    public boolean isRegistered(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public void registerUser(String uuid, String username, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(uuid, username, hashedPassword);
        userRepository.save(user);
    }
}
