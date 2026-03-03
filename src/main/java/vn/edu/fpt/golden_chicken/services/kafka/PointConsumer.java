package vn.edu.fpt.golden_chicken.services.kafka;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.response.ActionPointMessage;
import vn.edu.fpt.golden_chicken.repositories.CustomerRepository;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PointConsumer {
    CustomerRepository customerRepository;
    UserService userService;

    @KafkaListener(topics = "customer-points-topic", groupId = "point-loyalty-group", properties = {
            "auto.offset.reset=earliest" })
    public void handlePoint(ActionPointMessage msg) throws PermissionException {
        System.out.println(">>>> ACCEPT EVENT POINT <<<<");
        System.out.println("USER ID: " + msg.getUserId());
        System.out.println("ACTION: " + msg.getAction());
        System.out.println("CHANGE POINT:" + msg.getChange());
        System.out.println("REASON: " + msg.getReason());
        System.out.println("ACTION AT:" + msg.getActionAt());
        var cus = this.customerRepository.findById(msg.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User With ID " + msg.getUserId() + " Not Found!"));
        this.userService.updateCustomerPoint(cus, msg.getChange(),
                msg.getAction().equalsIgnoreCase("add") ? true : false);
        System.out.println(">>>> SET EVENT POINT SUCCESS <<<<");
    }
}
