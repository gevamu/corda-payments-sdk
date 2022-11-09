package com.gevamu.web.server.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

@Service
public class IdGeneratorService {

    private static final int ID_LENGTH = 32;
    private static final int END_TO_END_ID_LENGTH = 13;

    public String generateId() {
        try {
            var str = RandomStringUtils.randomAlphanumeric(ID_LENGTH);
            var digest = MessageDigest.getInstance("MD5");
            digest.update(str.getBytes());
            var bytes = digest.digest(str.getBytes());
            return DatatypeConverter.printHexBinary(bytes).toLowerCase(Locale.getDefault());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateEndToEndId() {
        var id = generateId();
        return id.substring(0, END_TO_END_ID_LENGTH);
    }
}
