package korlibs.korge.ext.fla

import korlibs.io.async.*
import korlibs.io.file.std.*
import kotlin.test.*

class FlaTest {
	@Test
	fun name() = suspendTestNoJs {
		val fla = Fla.read(resourcesVfs["simple1.fla"])
	}
}
