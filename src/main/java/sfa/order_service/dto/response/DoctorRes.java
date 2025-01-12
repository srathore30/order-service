package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorRes {
    String name;
    String gender;
    String email;
    String age;
    String address;
    Date dob;
    Date dom;
    Long id;
    String practiceSince;
    String specialization;
    BeetRespForOrderDto beet;
}
