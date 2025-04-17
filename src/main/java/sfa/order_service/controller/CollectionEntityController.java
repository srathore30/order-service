package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.constant.UserRole;
import sfa.order_service.dto.request.CollectionEntityRequest;
import sfa.order_service.dto.request.CollectionEntityStatusUpdateReq;
import sfa.order_service.dto.response.CollectionEntityResponse;
import sfa.order_service.interceptor.UserAuthorization;
import sfa.order_service.service.CollectionEntityService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/collectionEntity")
public class CollectionEntityController {
    private final CollectionEntityService collectionEntityService;
    @PostMapping
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<CollectionEntityResponse> createCollection(@RequestBody CollectionEntityRequest request) {
        return new ResponseEntity<>(collectionEntityService.createCollection(request), HttpStatus.CREATED);
    }
    @PutMapping("/updateByCollectionId/{collectionId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<CollectionEntityResponse> updateCollection(@PathVariable Long collectionId, @RequestBody CollectionEntityRequest request) {
        return new ResponseEntity<>(collectionEntityService.updateCollection(collectionId, request), HttpStatus.OK);
    }
    @GetMapping("/getByCollectionId/{collectionId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<CollectionEntityResponse> getCollectionById(@PathVariable Long collectionId) {
        return new ResponseEntity<>(collectionEntityService.getCollectionById(collectionId), HttpStatus.OK);
    }
    @PutMapping("/updateStatusByCollectionId/{collectionId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<CollectionEntityResponse> updateStatusByCollectionId(@PathVariable Long collectionId, @RequestBody CollectionEntityStatusUpdateReq request) {
        return new ResponseEntity<>(collectionEntityService.updateStatusByCollectionId(collectionId, request), HttpStatus.OK);
    }
}
