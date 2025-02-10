package com.godea.authorization.services;

import com.godea.authorization.config.Constants;
import com.godea.authorization.models.Role;
import com.godea.authorization.models.User;
import com.godea.authorization.models.dto.UserDto;
import com.godea.authorization.repositories.RoleRepository;
import com.godea.authorization.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDto createUser(User user) {
        Optional<com.godea.authorization.models.User> userOpt = userRepository.findUserByEmail(user.getUsername());
        if(userOpt.isPresent()) {
            throw new UsernameNotFoundException("Email is already taken");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Optional<Role> roleOpt = roleRepository.findRoleByName(Constants.Roles.USER);

        if(roleOpt.isEmpty()) {
            log.error("Role doesn't initialized, registration denied");
            throw new RuntimeException("Registration Error! Please contact administrator");
        }

        user.setRoles(roleOpt.get());

        User savedUser = userRepository.save(user);

        return UserDto.builder()
                .role(savedUser.getRoles())
                .email(user.getEmail())
                .build();
    }

    public void removeUser(UUID id) {
        userRepository.deleteUserById(id);
    }

    public UserDto getUser(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));

        return UserDto.builder()
                .email(user.getEmail())
                .role(user.getRoles())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username).orElseThrow(() -> new NoSuchElementException("Пользователь с таким Email уже существует"));
    }
}
