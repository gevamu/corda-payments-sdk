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

package com.gevamu.web.server.controllers;

import com.gevamu.payments.app.contracts.schemas.AppSchemaV1;
import com.gevamu.web.server.services.CordaRpcClientService;
import com.gevamu.web.server.util.CompletableFutures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ParticipantsControllerTest {

    private static final String PATH = "/api/v1/participants";

    @Autowired
    private transient WebTestClient webClient;

    @MockBean
    private transient CordaRpcClientService cordaRpcClientService;

    @Test
    public void testCreditors() {
        List<AppSchemaV1.Account> creditors = createCreditors();
        when(cordaRpcClientService.getCreditors())
            .thenReturn(CompletableFutures.completedStage(creditors));
        webClient.get()
            .uri(PATH + "/creditors")
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("accounts")
            .isArray()
            .jsonPath("accounts.length()")
            .isEqualTo(1)
            .jsonPath("accounts[0].accountId")
            .isEqualTo("test_creditor_account")
            .jsonPath("accounts[0].accountName")
            .isEqualTo("test_creditor_accountName")
            .jsonPath("accounts[0].currency")
            .isEqualTo("test_creditor_currency");
    }

    @Test
    public void testDebtors() {
        List<AppSchemaV1.Account> debtors = createDebtors();
        when(cordaRpcClientService.getDebtors())
            .thenReturn(CompletableFutures.completedStage(debtors));
        webClient.get()
            .uri(PATH + "/debtors")
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("accounts")
            .isArray()
            .jsonPath("accounts.length()")
            .isEqualTo(1)
            .jsonPath("accounts[0].accountId")
            .isEqualTo("test_debtor_account")
            .jsonPath("accounts[0].accountName")
            .isEqualTo("test_debtor_accountName")
            .jsonPath("accounts[0].currency")
            .isEqualTo("test_debtor_currency");
    }

    private List<AppSchemaV1.Account> createCreditors() {
        AppSchemaV1.Account account = new AppSchemaV1.Account();
        account.setBic("test_creditor_bic");
        account.setCountry(new AppSchemaV1.Country("test_creditor_country"));
        account.setCurrency(new AppSchemaV1.Currency("test_creditor_currency"));
        account.setAccount("test_creditor_account");
        account.setAccountName("test_creditor_accountName");
        return Collections.singletonList(account);
    }

    private List<AppSchemaV1.Account> createDebtors() {
        AppSchemaV1.Account account = new AppSchemaV1.Account();
        account.setBic("test_debtor_bic");
        account.setCountry(new AppSchemaV1.Country("test_debtor_country"));
        account.setCurrency(new AppSchemaV1.Currency("test_debtor_currency"));
        account.setAccount("test_debtor_account");
        account.setAccountName("test_debtor_accountName");
        return Collections.singletonList(account);
    }
}
