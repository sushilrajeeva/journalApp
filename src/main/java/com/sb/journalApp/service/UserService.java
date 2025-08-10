package com.sb.journalApp.service;

import com.sb.journalApp.dto.UserRequest;
import com.sb.journalApp.dto.UserResponse;
import com.sb.journalApp.mapper.UserMapper;
import com.sb.journalApp.model.User;
import com.sb.journalApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // inject bean

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByUsernameIgnoreCase(userRequest.getUsername())) {
            throw new IllegalArgumentException("username already exists: " + userRequest.getUsername());
        }
        String hashPassword = passwordEncoder.encode(userRequest.getPassword());
        User user = UserMapper.toNewEntity(userRequest, hashPassword);
        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id)
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
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id)
        );

        // if username changes, re-check uniqueness
        if(!user.getUsername().equalsIgnoreCase(userRequest.getUsername())
            && userRepository.existsByUsernameIgnoreCase(userRequest.getUsername())) {
            throw new IllegalArgumentException("username already exists :" + userRequest.getUsername());
        }

        String hashPassword = passwordEncoder.encode(userRequest.getPassword());
        UserMapper.updateEntity(user, userRequest, hashPassword);

        userRepository.save((user));

        return UserMapper.toDto(user);

    }

    @Transactional
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }

        userRepository.deleteById(id);

    }

    public UserResponse getCurrentUser() {
        Long uid = Auth.currentUserId();
        var u = userRepository.findById(uid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return UserMapper.toDto(u);
    }

    @Transactional
    public UserResponse updateCurrent(UserRequest req) {
        Long uid = Auth.currentUserId();
        var u = userRepository.findById(uid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        // if username changes, check uniqueness:
        if (!u.getUsername().equalsIgnoreCase(req.getUsername())
                && userRepository.existsByUsernameIgnoreCase(req.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
        }
        var hash = passwordEncoder.encode(req.getPassword()); // or only if provided (relax validation if you want)
        UserMapper.updateEntity(u, req, hash);
        return UserMapper.toDto(userRepository.save(u));
    }

    @Transactional
    public void deleteCurrent() {
        Long uid = Auth.currentUserId();
        userRepository.deleteById(uid);
    }


}
