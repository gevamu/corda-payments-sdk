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

package com.gevamu.corda.web.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@ConstructorBinding
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Participant {
    @XmlElement(name = "Bic")
    String bic;

    @XmlElement(name = "Country")
    String country;

    @XmlElement(name = "Currency")
    String currency;

    @XmlElement(name = "Account")
    String account;

    @XmlElement(name = "AccountName")
    String accountName;

    String effectiveDate;

    String expiryDate;

    String paymentLimit;
}
