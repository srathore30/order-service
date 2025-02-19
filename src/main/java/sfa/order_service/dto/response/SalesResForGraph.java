package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.YearMonth;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SalesResForGraph {
    YearMonth date;
    Double totalSale;
}
