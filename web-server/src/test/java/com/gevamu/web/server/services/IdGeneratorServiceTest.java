package com.gevamu.web.server.services;

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
        var id = idGeneratorService.generateId();
        assertId(id, 32);
    }

    @Test
    public void testGenerateEndToEndId() {
        var id = idGeneratorService.generateEndToEndId();
        assertId(id, 13);
    }

    private void assertId(String id, int expectedLength) {
        assertThat(id).isNotBlank();
        assertThat(id).isLowerCase();
        assertThat(StringUtils.isAlphanumeric(id)).isTrue();
        assertThat(id).hasSize(expectedLength);
    }
}
