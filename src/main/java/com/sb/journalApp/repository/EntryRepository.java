package com.sb.journalApp.repository;

import com.sb.journalApp.model.Entry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryRepository extends JpaRepository<Entry, Long> {}
