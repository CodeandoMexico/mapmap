package org.codeandomexico.mapmap.server.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.codeandomexico.mapmap.server.model.Phone;
import org.codeandomexico.mapmap.server.repository.PhoneRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class MainService {

    private final PhoneRepository phoneRepository;

    public MainService(PhoneRepository phoneRepository) {
        this.phoneRepository = phoneRepository;
    }

    public Optional<Phone> loadPhone(String imei) {
        Optional<Phone> phone = phoneRepository.findByImei(imei);
        if (phone.isEmpty()) {
            return phoneRepository.findByUnitId(imei);
        }
        return phone;
    }

    public Optional<Phone> registerPhone(String imei, String userName) {

        if (imei == null || imei.isEmpty()) {
            return Optional.empty();
        }

        Optional<Phone> phone = phoneRepository.findFirst1ByImei(imei);
        if (phone.isPresent()) {
            return phone;
        }

        Phone newPhone = new Phone();
        do {
            newPhone.unitId = generateRandomUuid().toUpperCase();
        } while (phoneRepository.countByUnitId(newPhone.unitId) > 0);
        newPhone.imei = imei;
        newPhone.userName = userName;
        newPhone.registeredOn = new Date();
        return Optional.of(phoneRepository.save(newPhone));
    }

    private String generateRandomUuid() {
        return RandomStringUtils.randomAlphanumeric(6).toUpperCase();
    }
}
