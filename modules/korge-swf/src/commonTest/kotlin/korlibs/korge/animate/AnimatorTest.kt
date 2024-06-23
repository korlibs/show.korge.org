package korlibs.korge.animate

import korlibs.image.color.*
import korlibs.korge.tests.*
import korlibs.korge.tween.*
import korlibs.korge.view.*
import korlibs.math.*
import korlibs.math.geom.*
import korlibs.math.interpolation.*
import korlibs.time.*
import kotlin.test.*

class AnimatorTest : ViewsForTesting() {
    @Test
    fun test() = viewsTest {
        val view = solidRect(100, 100, Colors.RED)
        val log = arrayListOf<String>()
        animate(completeOnCancel = false) {
            moveTo(view, 100, 0)
            moveBy(view, y = +100.0)
            block { log += "${view.pos}" }
            moveBy(view, x = +10.0)
            moveTo(view, x = { view.x + 10 })
        }
        assertEquals("(120, 100)", view.pos.toString())
        assertEquals("[(100, 100)]", log.toString())
    }

    @Test
    fun testInterpolateAngle() = viewsTest {
        //        0 360 -360
        //  -90 /+--+\
        // 270 |     | 90 -270
        //      \+--+/
        //        180
        //        -180

        assertEquals(202.5.degrees, _interpolateAngle(0.25.toRatio(), 180.degrees, (-90).degrees))
        assertEquals(0.degrees, _interpolateAngle(0.5.toRatio(), 350.degrees, (10).degrees))
        assertEquals(0.degrees, _interpolateAngle(0.5.toRatio(), 10.degrees, (350).degrees))
    }

    @Test
    fun testTweenAngle() = viewsTest(frameTime = 100.milliseconds) {
        val view = solidRect(10, 10, Colors.RED)
        val log = arrayListOf<Int>()
        tween(view::rotation[350.0.degrees, 10.0.degrees], time = 1.seconds, easing = Easing.LINEAR) {
            log += view.rotation.degrees.toIntRound()
        }
        assertEquals("350,352,354,356,358,0,2,4,6,8,10", log.joinToString(","))
    }

    @Test
    fun testTweenAngleDenormalized() = viewsTest(frameTime = 100.milliseconds) {
        val view = solidRect(10, 10, Colors.RED)
        val log = arrayListOf<Int>()
        tween(view::rotation[350.0.degrees, 10.0.degrees].denormalized(), time = 1.seconds, easing = Easing.LINEAR) {
            log += view.rotation.degrees.toIntRound()
        }
        assertEquals("350,316,282,248,214,180,146,112,78,44,10", log.joinToString(","))
    }

    @PublishedApi
    internal fun _interpolateAngle(ratio: Ratio, l: Angle, r: Angle): Angle = _interpolateAngleAny(ratio, l, r, minimizeAngle = true)

    @PublishedApi
    internal fun _interpolateAngleDenormalized(ratio: Ratio, l: Angle, r: Angle): Angle = _interpolateAngleAny(ratio, l, r, minimizeAngle = false)

    internal fun _interpolateAngleAny(ratio: Ratio, l: Angle, r: Angle, minimizeAngle: Boolean = true): Angle {
        if (!minimizeAngle) return Angle.fromRatio(ratio.interpolate(l.ratio, r.ratio))
        val ln = l.normalized
        val rn = r.normalized
        return when {
            (rn - ln).absoluteValue <= 180.degrees -> Angle.fromRadians(ratio.interpolate(ln.radians, rn.radians))
            ln < rn -> Angle.fromRadians(ratio.interpolate((ln + 360.degrees).radians, rn.radians)).normalized
            else -> Angle.fromRadians(ratio.interpolate(ln.radians, (rn + 360.degrees).radians)).normalized
        }
    }
}
