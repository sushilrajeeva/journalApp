package com.sb.journalApp.repository;

import com.sb.journalApp.model.Journal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalRepository extends JpaRepository<Journal, Long> {
    Page<Journal> findByUser_Id(Long userId, Pageable pageable);
}