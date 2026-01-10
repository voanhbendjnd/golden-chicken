package vn.edu.fpt.golden_chicken.common;

import java.util.Optional;

public class ConfigPage {
    public static int getLastPage(Optional<String> pageParam) {
        int page = 1;
        if (pageParam.isPresent()) {
            try {
                page = Integer.parseInt(pageParam.get());
                if (page < 1) {
                    page = 1;
                }
            } catch (NumberFormatException ex) {
                page = 1;
            }
        }
        return page;
    }
}
