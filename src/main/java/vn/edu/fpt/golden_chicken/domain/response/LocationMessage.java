package vn.edu.fpt.golden_chicken.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationMessage {
    private Long orderId;
    private double lat;
    private double lng;
}
