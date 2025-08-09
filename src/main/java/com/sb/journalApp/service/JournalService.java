package com.sb.journalApp.service;

import com.sb.journalApp.dto.JournalRequest;
import com.sb.journalApp.dto.JournalResponse;
import com.sb.journalApp.mapper.JournalMapper;
import com.sb.journalApp.model.Journal;
import com.sb.journalApp.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalRepository journalRepository;

    @Transactional
    public JournalResponse createJournal(JournalRequest request) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Journal journal = Journal.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .createdAt(now)
                .lastModifiedAt(now)
                .build();
        journalRepository.save(journal);
        return JournalMapper.toDto(journal);
    }

    @Transactional(readOnly = true)
    public JournalResponse getJournalById(Long id) {
        Journal journal = journalRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Journal not found: " + id)
        );

        return JournalMapper.toDto(journal);
    }

    @Transactional(readOnly = true)
    public Page<JournalResponse> getAllJournals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return journalRepository.findAll(pageable).map(JournalMapper::toDto);
    }

    @Transactional
    public JournalResponse updateJournalById(Long id, JournalRequest request) {
        Journal journal = journalRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Journal not found: " + id)
        );

        JournalMapper.updateEntity(journal, request);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        journal.setLastModifiedAt(now);

        journalRepository.save(journal);

        return JournalMapper.toDto(journal);
    }

    public void deleteJournalById(Long id) {
        if (!journalRepository.existsById(id)) {
            throw new IllegalArgumentException("Journal not found: " + id);
        }
        journalRepository.deleteById(id);
    }

}
