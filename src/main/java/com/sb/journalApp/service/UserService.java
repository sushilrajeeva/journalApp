package com.sb.journalApp.service;

import com.sb.journalApp.dto.UserRequest;
import com.sb.journalApp.dto.UserResponse;
import com.sb.journalApp.mapper.UserMapper;
import com.sb.journalApp.model.User;
import com.sb.journalApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByUsernameIgnoreCase(userRequest.getUsername())) {
            throw new IllegalArgumentException("username already exists: " + userRequest.getUsername());
        }
        String hashPassword = encoder.encode(userRequest.getPassword());
        User user = UserMapper.toNewEntity(userRequest, hashPassword);
        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("User not found: " + id)
        );

        return UserMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(int page, int size) {
        Page<User> pages = userRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
        return pages.map(UserMapper::toDto);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("User not found :" + id)
        );

        // if username changes, re-check uniqueness
        if(!user.getName().equalsIgnoreCase(userRequest.getUsername())
            && userRepository.existsByUsernameIgnoreCase(userRequest.getUsername())) {
            throw new IllegalArgumentException("username already exists :" + userRequest.getUsername());
        }

        String hashPassword = encoder.encode(userRequest.getPassword());
        UserMapper.updateEntity(user, userRequest, hashPassword);

        userRepository.save((user));

        return UserMapper.toDto(user);

    }

    @Transactional
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found :" + id);
        }

        userRepository.deleteById(id);

    }

}
