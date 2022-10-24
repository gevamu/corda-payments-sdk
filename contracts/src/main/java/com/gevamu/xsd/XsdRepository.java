package com.gevamu.xsd;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class XsdRepository {
    private static StreamSource getSource(String fileName) {
        InputStream inputStream = XsdRepository.class.getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return new StreamSource(inputStream);
        }

    }

    public static Source getPain_001_001_11() {
        return getSource("/xsd/pain.001.001.11.xsd");
    }
}
