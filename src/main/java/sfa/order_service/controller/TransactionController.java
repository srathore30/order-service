package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.constant.UserRole;
import sfa.order_service.dto.request.TransactionRequest;
import sfa.order_service.dto.response.TransactionResponse;
import sfa.order_service.interceptor.UserAuthorization;
import sfa.order_service.service.TransactionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    @Async
    @PostMapping
//    @UserAuthorization(allowedRoles = {UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
        TransactionResponse transaction = transactionService.createTransaction(request);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @GetMapping("getTransactionById/{id}")
    @UserAuthorization(allowedRoles = {UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        TransactionResponse transaction = transactionService.getTransactionById(id);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @PutMapping("updateTransaction/{id}")
//    @UserAuthorization(allowedRoles = {UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id, @RequestBody TransactionRequest request) {
        TransactionResponse transaction = transactionService.updateTransaction(id, request);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @DeleteMapping("deleteTransactionById/{id}")
    @UserAuthorization(allowedRoles = {UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("deleteAllTransactions")
    @UserAuthorization(allowedRoles = {UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<Void> deleteAllTransactions() {
        transactionService.deleteAllTransaction();
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
