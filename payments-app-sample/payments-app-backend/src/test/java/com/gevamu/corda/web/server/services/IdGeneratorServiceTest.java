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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class IdGeneratorServiceTest {

    @MockBean
    private transient CordaRpcClientService cordaRpcClientService;

    @Autowired
    private transient IdGeneratorService idGeneratorService;

    @Test
    public void testGenerateId() {
        String id = idGeneratorService.generateId();
        assertId(id, 32);
    }

    @Test
    public void testGenerateEndToEndId() {
        String id = idGeneratorService.generateEndToEndId();
        assertId(id, 13);
    }

    private void assertId(String id, int expectedLength) {
        assertThat(id).isNotBlank();
        assertThat(id).isLowerCase();
        assertThat(StringUtils.isAlphanumeric(id)).isTrue();
        assertThat(id).hasSize(expectedLength);
    }
}
