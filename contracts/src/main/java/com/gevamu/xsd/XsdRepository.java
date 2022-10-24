package com.gevamu.xsd;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class XsdRepository {
    private static InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = XsdRepository.class.getClassLoader();
        InputStream inputStream = XsdRepository.class.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }

    private static File getFileFromResource(String fileName) throws URISyntaxException {

        ClassLoader classLoader = XsdRepository.class.getClassLoader();
        URL resource = XsdRepository.class.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {

            // failed if files have whitespaces or special characters
            //return new File(resource.getFile());

            return new File(resource.toURI());
        }

    }
    public static Source getPain_001_001_11() {
        return new StreamSource(getFileFromResourceAsStream("/pain.001.001.11.xsd"));
    }
}
