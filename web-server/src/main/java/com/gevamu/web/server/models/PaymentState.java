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

package com.gevamu.web.server.models;

import com.gevamu.states.Payment;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class PaymentState {
    @NonNull
    Instant creationTime;
    @NonNull
    Instant updateTime;
    @NonNull
    UUID paymentId;
    @NonNull
    String endToEndId;
    @NonNull
    BigDecimal amount;
    @NonNull
    String currency;
    @NonNull
    Payment.PaymentStatus status;
    @NonNull
    ParticipantAccount creditor;
    @NonNull
    ParticipantAccount debtor;
}
