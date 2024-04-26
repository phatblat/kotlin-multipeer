import live.ditto.Ditto
import live.ditto.android.DefaultAndroidDittoDependencies
import org.koin.java.KoinJavaComponent.getKoin

class AndroidDittoManager : DittoManager {
    private val dependencies = DefaultAndroidDittoDependencies(getKoin().get())
    val ditto: Ditto = Ditto(dependencies)

    override val version: String = """
        VERSION: ${Ditto.VERSION}
        sdkVersion: ${ditto.sdkVersion}
    """.trimIndent()
}

actual fun getDittoManager(): DittoManager = AndroidDittoManager()