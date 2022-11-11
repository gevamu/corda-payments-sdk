package com.gevamu.web.server.services;

import com.gevamu.iso20022.pain.CustomerCreditTransferInitiationV09;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class XmlMarshallingService {

    private final transient JAXBContext context;

    public XmlMarshallingService() throws JAXBException {
        context = JAXBContext.newInstance(CustomerCreditTransferInitiationV09.class);
    }

    public byte[] marshal(@NonNull CustomerCreditTransferInitiationV09 obj) {
        try {
            var marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            try (var output = new ByteArrayOutputStream()) {
                marshaller.marshal(
                    new JAXBElement<>(
                        new QName("CstmrCdtTrfInitn"), CustomerCreditTransferInitiationV09.class, null, obj
                    ),
                    output
                );
                var result = output.toByteArray();
                if (log.isDebugEnabled()) {
                    var xml = new String(result);
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
            var unmarshaller = context.createUnmarshaller();
            var factory = XMLInputFactory.newFactory();
            try (var input = new ByteArrayInputStream(data)) {
                var element = unmarshaller.unmarshal(
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
