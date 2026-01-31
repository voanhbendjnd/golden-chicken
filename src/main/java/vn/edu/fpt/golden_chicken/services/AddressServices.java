package vn.edu.fpt.golden_chicken.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.edu.fpt.golden_chicken.domain.entity.Address;
import vn.edu.fpt.golden_chicken.domain.response.ResAddress;
import vn.edu.fpt.golden_chicken.repositories.AddressRepository;

@Service
public class AddressServices {
    private static final String ACTIVE = "ACTIVE";

    private final AddressRepository addressRepository;
    private final ProfileService profileService;

    public AddressServices(AddressRepository addressRepository, ProfileService profileService) {
        this.addressRepository = addressRepository;
        this.profileService = profileService;
    }

    private ResAddress toDto(Address add) {
        ResAddress dto = new ResAddress();
        dto.setId(add.getId());
        dto.setRecipientName(add.getRecipientName());
        dto.setRecipientPhone(add.getRecipientPhone());
        dto.setSpecificAddress(add.getSpecificAddress());
        dto.setWard(add.getWard());
        dto.setDistrict(add.getDistrict());
        dto.setCity(add.getCity());
        dto.setIsDefault(add.getIsDefault());
        return dto;
    }

    public List<ResAddress> getMyAddresses() {
        var user = profileService.getCurrentUser();
        if (user == null) {
            return new ArrayList<>();
        }

        List<Address> addresses = addressRepository.findAllByUserIdAndStatusOrderByIsDefaultDescIdDesc(user.getId(),
                ACTIVE);

        List<ResAddress> result = new ArrayList<>();
        for (Address a : addresses) {
            result.add(toDto(a));
        }
        return result;
    }

    public ResAddress getMyDefaultAddress() {
        var user = profileService.getCurrentUser();
        if (user == null) {
            return null;
        }

        Optional<Address> addressOpt = this.addressRepository.findFirstByUserIdAndStatusAndIsDefaultTrue(user.getId(), ACTIVE);
        if (addressOpt.isEmpty()) {
            return null;
        }
        return toDto(addressOpt.get());
    }

}
