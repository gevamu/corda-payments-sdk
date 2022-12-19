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

import com.gevamu.corda.iso20022.pain.CustomerCreditTransferInitiationV09;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;

@Service
@Slf4j
public class XmlMarshallingService {

    private final transient JAXBContext context;

    public XmlMarshallingService() throws JAXBException {
        context = JAXBContext.newInstance(CustomerCreditTransferInitiationV09.class);
    }

    public byte[] marshal(@NonNull CustomerCreditTransferInitiationV09 obj) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                marshaller.marshal(
                    new JAXBElement<>(
                        new QName("CstmrCdtTrfInitn"), CustomerCreditTransferInitiationV09.class, null, obj
                    ),
                    output
                );
                byte[] result = output.toByteArray();
                if (log.isDebugEnabled()) {
                    String xml = new String(result);
                    log.debug("Marshalling result:\n{}", xml);
                }
                return result;
            }
        }
        catch (Exception e) {
            throw new XmlException("XML marshalling error", e);
        }
    }

    public CustomerCreditTransferInitiationV09 unmarshal(byte[] data) {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            XMLInputFactory factory = XMLInputFactory.newFactory();
            try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
                JAXBElement<CustomerCreditTransferInitiationV09> element = unmarshaller.unmarshal(
                    factory.createXMLStreamReader(input),
                    CustomerCreditTransferInitiationV09.class
                );
                return element.getValue();
            }
        }
        catch (Exception e) {
            throw new XmlException("XML unmarshalling error", e);
        }
    }
}
