package sfa.order_service.scheduler;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sfa.order_service.entity.InvoiceMaster;
import sfa.order_service.repo.InvoiceMasterRepo;

import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
@Getter
@RequiredArgsConstructor
public class OrderScheduler {
    private final InvoiceMasterRepo invoiceMasterRepo;
    @Scheduled(cron = "0 0 0 1 4 *")  // Runs at 00:00 on April 1st every year
    @Transactional
    public void endMemberDay(){
        try {
            List<InvoiceMaster> invoiceMasterList = invoiceMasterRepo.findAll();
            if(!invoiceMasterList.isEmpty()){
                InvoiceMaster invoiceMaster = invoiceMasterList.get(0);
                invoiceMaster.setCurrentSerialNumber(0);
                invoiceMasterRepo.save(invoiceMaster);
            }
        }catch (Exception e){
            log.info(e.toString());
        }
    }
}
