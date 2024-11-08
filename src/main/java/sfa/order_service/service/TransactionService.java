package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.dto.request.TransactionRequest;
import sfa.order_service.dto.response.TransactionResponse;
import sfa.order_service.entity.TransactionEntity;
import sfa.order_service.exception.NoSuchElementFoundException;
import sfa.order_service.repo.TransactionRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionEntity dtoToEntity(TransactionRequest request) {
        TransactionEntity entity = new TransactionEntity();
        entity.setTopUpAmount(request.getTopUpAmount());
        entity.setClientId(request.getClientId());
        return entity;
    }

    public TransactionResponse entityToDto(TransactionEntity transactionEntity) {
        TransactionResponse response = new TransactionResponse();
        response.setTopUpAmount(transactionEntity.getTopUpAmount());
        response.setClientId(transactionEntity.getClientId());
        return response;
    }

    public TransactionEntity updateDtoToEntity(Long id, TransactionRequest request) {
        TransactionEntity entity = transactionRepository.findById(id).get();
        entity.setClientId(request.getClientId());
        entity.setTopUpAmount(request.getTopUpAmount());
        return entity;
    }

    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("Creating transaction: {}", request);
        TransactionEntity transactionEntity = dtoToEntity(request);
        transactionRepository.save(transactionEntity);
        return entityToDto(transactionEntity);
    }

    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        log.info("Updating transaction for Id : {}", request);
        transactionRepository.findById(id).orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.TRANSACTION_NOT_FOUND.getErrorCode(), ApiErrorCodes.TRANSACTION_NOT_FOUND.getErrorMessage()));
        TransactionEntity entity = updateDtoToEntity(id, request);
        transactionRepository.save(entity);
        return entityToDto(entity);

    }

    public void deleteTransaction(Long id) {
        log.info("Deleting transaction by Id: {}", id);
        transactionRepository.findById(id).orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.TRANSACTION_NOT_FOUND.getErrorCode(), ApiErrorCodes.TRANSACTION_NOT_FOUND.getErrorMessage()));
        transactionRepository.deleteById(id);
    }

    public void deleteAllTransaction() {
        log.info("Deleting all transaction");
        transactionRepository.deleteAll();
    }

    public TransactionResponse getTransactionById(Long id) {
        transactionRepository.findById(id).orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.TRANSACTION_NOT_FOUND.getErrorCode(), ApiErrorCodes.TRANSACTION_NOT_FOUND.getErrorMessage()));
        return entityToDto(transactionRepository.findById(id).get());
    }
}
