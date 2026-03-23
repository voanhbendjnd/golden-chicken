package vn.edu.fpt.golden_chicken.domain.response.ai;

import java.math.BigDecimal;

public interface ProductSuggest {
    Long getId();

    String getName();

    BigDecimal getPrice();
}