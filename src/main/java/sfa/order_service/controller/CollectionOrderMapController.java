package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.dto.request.CollectionOrderMapRequest;
import sfa.order_service.dto.response.CollectionOrderMapResponse;
import sfa.order_service.service.CollectionOrderMapService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/collectionOrderMap")
public class CollectionOrderMapController {
    private final CollectionOrderMapService collectionOrderMapService;
    @PostMapping
    public ResponseEntity<CollectionOrderMapResponse> createCollectionOrderMap(@RequestBody CollectionOrderMapRequest request) {
        return new ResponseEntity<>(collectionOrderMapService.createCollectionOrderMap(request), HttpStatus.CREATED);
    }
    @PutMapping("/updateById/{collectionOrderMapId}")
    public ResponseEntity<CollectionOrderMapResponse> updateCollectionOrderMap(@PathVariable Long orderMapId, @RequestBody CollectionOrderMapRequest request) {
        return new ResponseEntity<>(collectionOrderMapService.updateCollectionOrderMapById(orderMapId, request), HttpStatus.OK);
    }
    @DeleteMapping("/deleteById/{collectionOrderMapId}")
    public ResponseEntity<String> deleteCollectionOrderMap(@PathVariable Long orderMapId) {
        return new ResponseEntity<>(collectionOrderMapService.deleteCollectionOrderMapById(orderMapId), HttpStatus.OK);
    }
}
