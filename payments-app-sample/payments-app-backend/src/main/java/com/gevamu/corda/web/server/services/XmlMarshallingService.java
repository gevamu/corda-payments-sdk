/*
 * Copyright 2022 Exactpro Systems Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gevamu.corda.web.server.services;

import com.gevamu.corda.web.server.models.WirePaymentRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import javax.xml.bind.JAXBContext;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

@Service
@Slf4j
public class XmlMarshallingService {
    private final transient JAXBContext jaxbContext;

    private final transient DOMImplementation domImplementation;

    private final transient Templates paymentRequestOutXslt;

    private final transient Templates paymentRequestInXslt;

    private final transient DatatypeFactory datatypeFactory;

    public XmlMarshallingService() throws XmlException {
        try {
            jaxbContext = JAXBContext.newInstance(WirePaymentRequest.class);
            domImplementation = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            try (InputStream stream = XmlMarshallingService.class.getResourceAsStream("PaymentRequest.out.xsl")) {
                paymentRequestOutXslt = transformerFactory.newTemplates(new StreamSource(stream));
            }
            try (InputStream stream = XmlMarshallingService.class.getResourceAsStream("PaymentRequest.in.xsl")) {
                paymentRequestInXslt = transformerFactory.newTemplates(new StreamSource(stream));
            }
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (Exception ex) {
            throw new XmlException("Unable to initialize XML service classes", ex);
        }
    }

    public byte[] marshalPaymentRequest(@NonNull WirePaymentRequest paymentRequest) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = domImplementation.createDocument(null, null, null);
            jaxbContext.createMarshaller().marshal(paymentRequest, document);
            paymentRequestOutXslt.newTransformer().transform(new DOMSource(document), new StreamResult(outputStream));
            byte[] result = outputStream.toByteArray();
            if (log.isDebugEnabled()) {
                String xml = new String(result, StandardCharsets.UTF_8);
                log.debug("Produced ISO20022 Payment initiation request:\n{}", xml);
            }
            return result;
        } catch (Exception ex) {
            throw new XmlException("Unable to produce ISO20022 Payment initiation request", ex);
        }
    }

    public @NotNull WirePaymentRequest unmarshalPaymentRequest(byte @NonNull [] data) throws XmlException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            Document document = domImplementation.createDocument(null, null, null);
            paymentRequestInXslt.newTransformer().transform(new StreamSource(inputStream), new DOMResult(document));
            return jaxbContext.createUnmarshaller().unmarshal(document, WirePaymentRequest.class).getValue();
        } catch (Exception ex) {
            throw new XmlException("Unable to read ISO20022 Payment initiation request", ex);
        }
    }

    public @NotNull XMLGregorianCalendar xmlNow() {
        return datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(ZonedDateTime.now(ZoneOffset.UTC)));
    }

    public @NotNull XMLGregorianCalendar xmlToday() {
        LocalDate today = LocalDate.now();
        return datatypeFactory.newXMLGregorianCalendarDate(
            today.getYear(), today.getMonthValue(), today.getDayOfMonth(), DatatypeConstants.FIELD_UNDEFINED
        );
    }
}
