package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.dto.request.CompetitorActivityCompetitorRequest;
import sfa.order_service.dto.response.CompetitorActivityCompetitorResponse;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.service.CompetitorActivityCompetitorService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/competitor_activity_competitor")
public class CompetitorActivityCompetitorController {

    private final CompetitorActivityCompetitorService service;

    @PostMapping
    public ResponseEntity<CompetitorActivityCompetitorResponse> create(@RequestBody CompetitorActivityCompetitorRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitorActivityCompetitorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<PaginatedResp<CompetitorActivityCompetitorResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        return ResponseEntity.ok(service.getAll(page, size, sortBy, sortDirection));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetitorActivityCompetitorResponse> update(
            @PathVariable Long id,
            @RequestBody CompetitorActivityCompetitorRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
