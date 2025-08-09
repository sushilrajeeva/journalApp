package com.sb.journalApp.service;

import com.sb.journalApp.repository.EntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DbHealthService {
    private final EntryRepository entryRepository;

    @PersistenceContext
    private EntityManager em;

    public Map<String, Object> health() {
        try {
            String dbName  = (String) em.createNativeQuery("select current_database()").getSingleResult();
            String version = (String) em.createNativeQuery("select version()").getSingleResult();
            Number tableCount = (Number) em.createNativeQuery(
                            "select count(*) from information_schema.tables where table_schema = 'public'")
                    .getSingleResult();

            @SuppressWarnings("unchecked")
            List<String> tables = em.createNativeQuery(
                            "select table_name from information_schema.tables " +
                                    "where table_schema = 'public' order by table_name limit 10")
                    .getResultList();

            long entryCount = entryRepository.count(); // pure JPA call

            return Map.of(
                    "status", "UP",
                    "database", dbName,
                    "version", version,
                    "publicTableCount", tableCount.longValue(),
                    "publicTablesSample", tables,
                    "entryCount", entryCount
            );
        } catch (Exception e) {
            return Map.of("status", "DOWN", "error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
