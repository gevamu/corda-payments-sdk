package com.gevamu.web.server;

import com.gevamu.web.server.services.CordaRpcClientService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ContextTest {

    @MockBean
    private transient CordaRpcClientService cordaRpcClientService;

    @Test
    public void test() {
    }
}
