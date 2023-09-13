package com.gevamu.corda.web.server.models;

import com.gevamu.corda.web.server.config.Participant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PaymentRequest")
public class WirePaymentRequest {
    @XmlElement(name = "Creditor")
    Participant creditor;

    @XmlElement(name = "Debtor")
    Participant debtor;

    @XmlElement(name = "Amount")
    BigDecimal amount;

    @XmlElement(name = "ParticipantId")
    String participantId;

    @XmlElement(name = "MsgId")
    String msgId;

    @XmlElement(name = "PmtInfId")
    String pmtInfId;

    @XmlElement(name = "InstrId")
    String instrId;

    @XmlElement(name = "EndToEndId")
    String endToEndId;

    @XmlElement(name = "CreDtTm")
    XMLGregorianCalendar creDtTm;

    @XmlElement(name = "ReqdExctnDt")
    XMLGregorianCalendar reqdExctnDt;
}
