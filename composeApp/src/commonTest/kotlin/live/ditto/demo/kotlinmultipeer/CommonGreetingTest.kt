package live.ditto.demo.kotlinmultipeer

import DittoManager
import Greeting
import kotlin.test.Test
import kotlin.test.assertTrue

object MockDitto : DittoManager() {
    override val version = "0.0.0"

    override fun startSync() { /* no-op */ }
}

class CommonGreetingTest {
    @Test
    fun testExample() {
        assertTrue(Greeting(MockDitto).greet().contains("Hello"), "Check 'Hello' is mentioned")
    }
}
