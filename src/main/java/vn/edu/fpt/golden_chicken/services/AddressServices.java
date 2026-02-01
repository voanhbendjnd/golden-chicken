package vn.edu.fpt.golden_chicken.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Address;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.AddressFormDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResAddress;
import vn.edu.fpt.golden_chicken.repositories.AddressRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressServices {
    private static final String ACTIVE = "ACTIVE";
    private static final String INACTIVE = "INACTIVE";
    private static final String CITY = "Cần Thơ";
    AddressRepository addressRepository;
    ProfileService profileService;

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

    public List<ResAddress> getAllAddresses() {
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

    public ResAddress getDefaultAddress() {
        var user = profileService.getCurrentUser();
        if (user == null) {
            return null;
        }

        Optional<Address> addressOpt = this.addressRepository.findFirstByUserIdAndStatusAndIsDefaultTrue(user.getId(),
                ACTIVE);
        if (addressOpt.isPresent()) {
            return toDto(addressOpt.get());
        }
        return null;
    }

    public void createMyAddress(AddressFormDTO dto) {
        User user = profileService.getCurrentUser();
        if (user == null)
            return;

        Address a = new Address();
        a.setUser(user);
        a.setRecipientName(dto.getRecipientName());
        a.setRecipientPhone(dto.getRecipientPhone());
        a.setSpecificAddress(dto.getSpecificAddress());
        a.setWard(dto.getWard());
        a.setDistrict(dto.getDistrict());
        a.setCity(CITY);
        a.setStatus(ACTIVE);

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            unsetDefaultForUser(user.getId());
            a.setIsDefault(true);
        } else {
            a.setIsDefault(false);
        }

        addressRepository.save(a);
    }

    public AddressFormDTO getMyAddressForm(Long id) {
        User user = profileService.getCurrentUser();
        if (user == null)
            return null;

        Optional<Address> opt = addressRepository.findByIdAndUserId(id, user.getId());
        if (opt.isEmpty())
            return null;

        Address a = opt.get();
        if (!ACTIVE.equals(a.getStatus()))
            return null;

        AddressFormDTO dto = new AddressFormDTO();
        dto.setId(a.getId());
        dto.setRecipientName(a.getRecipientName());
        dto.setRecipientPhone(a.getRecipientPhone());
        dto.setSpecificAddress(a.getSpecificAddress());
        dto.setWard(a.getWard());
        dto.setDistrict(a.getDistrict());
        dto.setIsDefault(a.getIsDefault());

        return dto;
    }

    public void updateUserAddress(AddressFormDTO dto) {
        User user = this.profileService.getCurrentUser();

        if (user == null || dto.getId() == null)
            return;

        Optional<Address> opt = addressRepository.findByIdAndUserId(dto.getId(), user.getId());
        if (opt.isEmpty())
            return;

        Address add = opt.get();
        if (!ACTIVE.equals(add.getStatus()))
            return;

        add.setRecipientName(dto.getRecipientName());
        add.setRecipientPhone(dto.getRecipientPhone());
        add.setSpecificAddress(dto.getSpecificAddress());
        add.setWard(dto.getWard());
        add.setDistrict(dto.getDistrict());
        add.setCity(CITY);

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            unsetDefaultForUser(user.getId());
            add.setIsDefault(true);
        }

        addressRepository.save(add);
    }

    private void unsetDefaultForUser(Long userId) {
        List<Address> defaults = addressRepository.findAllByUserIdAndStatusAndIsDefaultTrue(userId, ACTIVE);

        for (Address a : defaults) {
            a.setIsDefault(false);
        }

        if (!defaults.isEmpty()) {
            addressRepository.saveAll(defaults);
        }
    }

    public void setCurrentUserDefaultAddress(Long id) {
        User user = profileService.getCurrentUser();
        if (user == null)
            return;

        Optional<Address> opt = addressRepository.findByIdAndUserId(id, user.getId());

        if (opt.isEmpty())
            return;

        Address a = opt.get();
        if (!ACTIVE.equals(a.getStatus()))
            return;

        unsetDefaultForUser(user.getId());
        a.setIsDefault(true);
        addressRepository.save(a);
    }

    public void deleteMyAddress(Long id) {
        User user = profileService.getCurrentUser();
        if (user == null)
            return;

        Optional<Address> opt = addressRepository.findByIdAndUserId(id, user.getId());
        if (opt.isEmpty())
            return;

        Address a = opt.get();
        if (!ACTIVE.equals(a.getStatus()))
            return;

        boolean wasDefault = Boolean.TRUE.equals(a.getIsDefault());

        a.setStatus(INACTIVE);
        a.setIsDefault(false);
        addressRepository.save(a);

        // delete default → set other default
        if (wasDefault) {
            List<Address> remain = addressRepository.findAllByUserIdAndStatusOrderByIsDefaultDescIdDesc(
                    user.getId(), ACTIVE);

            if (!remain.isEmpty()) {
                unsetDefaultForUser(user.getId());
                Address first = remain.get(0);
                first.setIsDefault(true);
                addressRepository.save(first);
            }
        }
    }

}
