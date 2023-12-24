import io.wollinger.animals.math.Vector2
import kotlin.test.Test
import kotlin.test.assertEquals

class VectorTest {
    @Test
    fun test() {
        assertEquals(Vector2(5.0, 1.0), Vector2(5.0, 1.0))
        assertEquals(Vector2(2.0, 0.0).distance(Vector2(0.0, 0.0)), 2.0)
    }
}