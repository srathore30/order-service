package sfa.order_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MemberGetDto {
    private Long id;
    private boolean isDayStarted;
    private boolean isAttendanceBlocked;
    private String employeeId;
    private List<String> designationName;
    private LocalDateTime checkIn;
    private String headQuarter;
    private LocalDateTime checkOut;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isDjpPlanCreated;
    private boolean isBjpPlanCreated;
    private boolean isLeaveToday;
    private Long mobile;
    private LocalDate dob;
    private LocalDate joiningDate;
    private Long designation;
    private Long region;
    private Long reportingManager;
    private String uploadFileKey;
    private List<MemberGetCityDto> cities;
    private Long ta;
    private Long da;
    private Long daExCity;
    private Double salary;
}
