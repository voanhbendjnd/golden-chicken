package vn.edu.fpt.golden_chicken.domain.response;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutResponse {

    private String redirect;

    private Map<String, Object> model = new HashMap<>();

}