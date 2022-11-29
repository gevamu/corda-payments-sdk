
import com.gevamu.payments.app.workflows.services.EntityManagerService
import com.gevamu.payments.app.workflows.services.IdGeneratorService
import com.gevamu.payments.app.workflows.services.PaymentInstructionBuilderService
import com.gevamu.payments.app.workflows.services.RegistrationService
import net.corda.core.serialization.SerializeAsToken
import net.corda.testing.node.MockServices
import net.corda.testing.node.createMockCordaService
import org.mockito.Mockito.spy

class SpyServices(
    private val services: MockServices = MockServices(listOf("com.gevamu.payments.app.workflows"))
) {
    init {
        createMockCordaService(services) { spy(RegistrationService(it)) }
        createMockCordaService(services) { spy(PaymentInstructionBuilderService(it)) }
        createMockCordaService(services) { spy(IdGeneratorService(it)) }
        createMockCordaService(services) { spy(EntityManagerService(it)) }
    }

    fun <T : SerializeAsToken> cordaService(type: Class<T>): T = services.cordaService(type)
}
