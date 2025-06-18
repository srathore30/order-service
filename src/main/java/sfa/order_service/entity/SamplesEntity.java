package sfa.order_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import sfa.order_service.constant.BundleType;
import sfa.order_service.constant.Status;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class SamplesEntity extends BaseEntity{
    Long memberId;
    Long doctorId;
    Long outletId;
    Long clientFmcgId;
    BundleType bundleType;
    Long beetLogId;
    Long doctorLogId;
    Long clientLogId;

    @Temporal(TemporalType.DATE)
    @CreatedDate
    Date sampleDate;
    Status status;
    Integer quantity;
    Long productId;
}
