package live.ditto.demo.kotlinmultipeer

import Greeting
import kotlin.test.Test
import kotlin.test.assertTrue

class IosGreetingTest {
    @Test
    fun testExample() {
        assertTrue(Greeting().greet().contains("iOS"), "Check iOS is mentioned")
    }
}
