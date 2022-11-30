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

package com.gevamu.web.server.services;

import com.gevamu.payments.app.contracts.schemas.AppSchemaV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletionStage;

@Service
public class ParticipantService {

    @Autowired
    private transient CordaRpcClientService cordaRpcClientService;

    public CompletionStage<List<? extends AppSchemaV1.Account>> getCreditors() {
        return cordaRpcClientService.getCreditors();
    }

    public CompletionStage<List<? extends AppSchemaV1.Account>> getDebtors() {
        return cordaRpcClientService.getDebtors();
    }
}
