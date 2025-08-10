package com.sb.journalApp.service;

import com.sb.journalApp.dto.JournalPatchRequest;
import com.sb.journalApp.dto.JournalRequest;
import com.sb.journalApp.dto.JournalResponse;
import com.sb.journalApp.mapper.JournalMapper;
import com.sb.journalApp.model.Journal;
import com.sb.journalApp.model.User;
import com.sb.journalApp.repository.JournalRepository;
import com.sb.journalApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalRepository journalRepository;
    private final UserRepository userRepository;

    private void assertOwner(Journal journal, Long callerId) {
        if (journal.getUser() == null || !journal.getUser().getId().equals(callerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your journal");
        }
    }

    @Transactional
    public JournalResponse createJournal(JournalRequest request) {

        Long uid = Auth.currentUserId(); // from JWT

        User owner = userRepository.findById(uid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User missing!")
        );

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Journal journal = Journal.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .createdAt(now)
                .lastModifiedAt(now)
                .user(owner) // owner = caller
                .build();
        journalRepository.save(journal);
        return JournalMapper.toDto(journal);
    }

    @Transactional(readOnly = true)
    public JournalResponse getJournalById(Long id) {

        Long uid = Auth.currentUserId();

        Journal journal = journalRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal not found: " + id)
        );

        assertOwner(journal, uid);

        return JournalMapper.toDto(journal);
    }

    @Transactional(readOnly = true)
    public Page<JournalResponse> getAllJournals(int page, int size) {

        Long uid = Auth.currentUserId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return journalRepository.findByUser_Id(uid, pageable).map(JournalMapper::toDto);
    }

    @Transactional
    public JournalResponse updateJournalById(Long id, JournalRequest request) {

        Long uid = Auth.currentUserId();

        Journal journal = journalRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal not found: " + id)
        );

        assertOwner(journal, uid);

        JournalMapper.updateEntity(journal, request);

//        // update owner only if userId is provided (null means "leave as is")
//        if(request.getUserId() != null) {
//            journal.setUser(resolveOwner(request.getUserId()));
//        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        journal.setLastModifiedAt(now);

        journalRepository.save(journal);

        return JournalMapper.toDto(journal);
    }

    @Transactional
    public JournalResponse patchJournalById(Long id, JournalPatchRequest req) {

        Long uid = Auth.currentUserId();

        Journal journal = journalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal not found: " + id));

        assertOwner(journal, uid);

        JournalMapper.patchEntity(journal, req);

//        // update owner only if userId is provided (null means "leave as is")
//        if(req.getUserId() != null) {
//            journal.setUser(resolveOwner(req.getUserId()));
//        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        journal.setLastModifiedAt(now);

        journalRepository.save(journal);

        return JournalMapper.toDto(journal);
    }

    public void deleteJournalById(Long id) {

        Long uid = Auth.currentUserId();

        Journal journal = journalRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal not found: " + id)
        );

        assertOwner(journal, uid);

        journalRepository.delete(journal);
    }

//    private User resolveOwner(Long userId) {
//        if(userId == null) {
//            return null;
//        }
//
//        return userRepository.findById(userId).orElseThrow(
//                () -> new IllegalArgumentException("User not found :" + userId)
//        );
//    }

//    @Transactional(readOnly = true)
//    public Page<JournalResponse> getJournalsByUser(Long userId, int page, int size) {
//        Page<Journal> p = journalRepository.findByUser_Id(userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
//        return p.map(JournalMapper::toDto);
//    }


}
