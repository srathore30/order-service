package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.dto.request.InvoiceMasterReq;
import sfa.order_service.dto.response.InvoiceMasterRes;
import sfa.order_service.entity.InvoiceMaster;
import sfa.order_service.exception.NoSuchElementFoundException;
import sfa.order_service.exception.ValidationException;
import sfa.order_service.repo.InvoiceMasterRepo;

@Service
@RequiredArgsConstructor
public class InvoiceMasterService{

    private final InvoiceMasterRepo invoiceMasterRepo;

    public InvoiceMasterRes create(InvoiceMasterReq invoiceMasterReq){
        if(!invoiceMasterRepo.findAll().isEmpty()){
            throw new ValidationException(ApiErrorCodes.INVOICE_CODE_ALREADY_CREATED.getErrorCode(), ApiErrorCodes.INVOICE_CODE_ALREADY_CREATED.getErrorMessage());
        }
        InvoiceMaster invoiceMaster = new InvoiceMaster();
        invoiceMaster.setCode(invoiceMasterReq.getCode());
        invoiceMaster.setPreOrPost(invoiceMasterReq.getPreOrPost());
        invoiceMaster.setCurrentSerialNumber(0);
        invoiceMasterRepo.save(invoiceMaster);
        return new InvoiceMasterRes(invoiceMaster.getCode(), invoiceMaster.getPreOrPost(), invoiceMaster.getId());
    }

    public InvoiceMasterRes get(){
        if(invoiceMasterRepo.findAll().isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.NOT_FOUND.getErrorCode(), ApiErrorCodes.NOT_FOUND.getErrorMessage());
        }
        InvoiceMaster invoiceMaster = invoiceMasterRepo.findAll().get(0);
        return new InvoiceMasterRes(invoiceMaster.getCode(), invoiceMaster.getPreOrPost(), invoiceMaster.getId());
    }

    public InvoiceMasterRes update(InvoiceMasterReq invoiceMasterReq){
        if(!invoiceMasterRepo.findAll().isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.NOT_FOUND.getErrorCode(), ApiErrorCodes.NOT_FOUND.getErrorMessage());
        }
        InvoiceMaster invoiceMaster = invoiceMasterRepo.findAll().get(0);
        invoiceMaster.setCode(invoiceMasterReq.getCode());
        invoiceMaster.setPreOrPost(invoiceMasterReq.getPreOrPost());
        invoiceMasterRepo.save(invoiceMaster);
        return new InvoiceMasterRes(invoiceMaster.getCode(), invoiceMaster.getPreOrPost(), invoiceMaster.getId());
    }

}
