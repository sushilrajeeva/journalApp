package com.sb.journalApp.repository;

import com.sb.journalApp.model.Journal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalRepository extends JpaRepository<Journal, Long> {}