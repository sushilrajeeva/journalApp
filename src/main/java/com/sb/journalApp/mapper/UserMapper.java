package com.sb.journalApp.mapper;

import com.sb.journalApp.dto.UserRequest;
import com.sb.journalApp.dto.UserResponse;
import com.sb.journalApp.model.Journal;
import com.sb.journalApp.model.User;

import java.util.Collections;
import java.util.List;

public class UserMapper {

    private UserMapper() {}

    public static User toNewEntity(UserRequest userRequest, String hashedPassword) {
        return User.builder()
                .name(userRequest.getName())
                .username(userRequest.getUsername())
                .password(hashedPassword)
                .build();
    }

    public static void updateEntity(User user, UserRequest userRequest, String hashedPassword) {
        user.setName(userRequest.getName());
        user.setUsername(userRequest.getUsername());
        user.setPassword(hashedPassword);
    }

    public static UserResponse toDto(User user) {

        var journals = (user.getJournalEntries() == null) ? Collections.<Journal>emptyList() : user.getJournalEntries();

        List<Long> journalIds = journals.stream().map(Journal::getId).toList();

        return UserResponse.builder()
                .id(user.getId())
                .journalIds(journalIds)
                .name(user.getName())
                .username(user.getUsername())
                .build();
    }
}
