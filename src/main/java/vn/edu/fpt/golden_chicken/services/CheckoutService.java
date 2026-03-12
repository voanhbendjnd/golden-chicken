package vn.edu.fpt.golden_chicken.services;

import java.util.List;

import vn.edu.fpt.golden_chicken.domain.response.CheckoutResponse;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

public interface CheckoutService {

    CheckoutResponse buildCheckout(
            Long productId,
            List<Long> ids,
            Long voucherId,
            Long addressId) throws PermissionException;

}