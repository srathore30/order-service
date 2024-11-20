package sfa.order_service.dto.request;

import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.UserRole;

import java.util.List;

@Getter
@Setter
public class ClientFMCGUpdateRequest {
    private Long id;
    private String clientCode;
    private String clientFirstName;
    private String clientLastName;
    private String password;
    private String email;
    private Long mobile;
    private String address;
    private Long region;
    private Long state;
    private Long city;
    private List<UserRole> userRoleList;
    private Double topUpBalance;
}
