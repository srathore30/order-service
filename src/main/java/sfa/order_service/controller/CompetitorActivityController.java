package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.dto.request.CompetitorActivityRequest;
import sfa.order_service.dto.response.CompetitorActivityResponse;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.service.CompetitorActivityService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/competitor_activity")
public class CompetitorActivityController {

    private final CompetitorActivityService competitorActivityService;

    @PostMapping
    public ResponseEntity<CompetitorActivityResponse> createCompetitorActivity(@RequestBody CompetitorActivityRequest request) {
        CompetitorActivityResponse response = competitorActivityService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitorActivityResponse> getCompetitorActivityById(@PathVariable Long id) {
        CompetitorActivityResponse response = competitorActivityService.getById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<PaginatedResp<CompetitorActivityResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        PaginatedResp<CompetitorActivityResponse> response =
                competitorActivityService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<CompetitorActivityResponse> updateCompetitorActivity(
            @PathVariable Long id, @RequestBody CompetitorActivityRequest request) {
        CompetitorActivityResponse response = competitorActivityService.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompetitorActivity(@PathVariable Long id) {
        competitorActivityService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
