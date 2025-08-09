package com.sb.journalApp.controller;

import com.sb.journalApp.dto.JournalPatchRequest;
import com.sb.journalApp.dto.JournalRequest;
import com.sb.journalApp.dto.JournalResponse;
import com.sb.journalApp.service.JournalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Tag(name = "Journals", description = "Journal CRUD Operations")
@RestController
@RequestMapping("/api/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JournalResponse createJournal(@Valid @RequestBody JournalRequest journalRequest) {
        return journalService.createJournal(journalRequest);
    }

    @GetMapping("/{id}")
    public JournalResponse getJournalById(@PathVariable Long id) {
        return journalService.getJournalById(id);
    }

    // Pagination defaults keep responses lean; change size as you like
    @GetMapping
    public Page<JournalResponse> getAllJournals(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return journalService.getAllJournals(page, size);
    }

    @PutMapping("/{id}")
    public JournalResponse updateJournalById(@PathVariable Long id, @Valid @RequestBody JournalRequest journalRequest) {
        return journalService.updateJournalById(id, journalRequest);
    }

    @PatchMapping("/{id}") // partial update
    public JournalResponse patchJournalById(@PathVariable Long id,
                                            @RequestBody JournalPatchRequest patch) {
        return journalService.patchJournalById(id, patch);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJournalById(@PathVariable Long id) {
        journalService.deleteJournalById(id);
    }


}

