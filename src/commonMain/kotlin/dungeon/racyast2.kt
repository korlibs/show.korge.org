package dungeon

import korlibs.datastructure.*
import korlibs.math.geom.*
import kotlin.math.*

// @TODO: Move to KorGE and add companion to Ray, so this can be an static method
fun RayFromTwoPoints(start: Point, end: Point): Ray = Ray(start, Angle.between(start, end))

private fun sq(v: Double): Double = v * v
private fun signumNonZero(v: Double): Double = if (v < 0) -1.0 else +1.0

data class RayResult(val ray: Ray, val point: Point, val normal: Vector2D) : Extra by Extra.Mixin()

// https://www.youtube.com/watch?v=NbSee-XM7WA
fun Ray.firstCollisionInTileMap(
    cellSize: Size = Size(1, 1),
    maxTiles: Int = 10,
    collides: (tilePos: PointInt) -> Boolean
): RayResult? {
    val ray = this
    val rayStart = this.point / cellSize
    val rayDir = ray.direction.normalized
    val rayUnitStepSize = Vector2D(
        sqrt(1.0 + sq(rayDir.y / rayDir.x)),
        sqrt(1.0 + sq(rayDir.x / rayDir.y)),
    )
    //println("vRayUnitStepSize=$vRayUnitStepSize")
    var mapCheckX = rayStart.x.toInt()
    var mapCheckY = rayStart.y.toInt()
    val stepX = signumNonZero(rayDir.x).toInt()
    val stepY = signumNonZero(rayDir.y).toInt()
    var rayLength1Dx = when {
        rayDir.x < 0 -> (rayStart.x - (mapCheckX)) * rayUnitStepSize.x
        else -> ((mapCheckX + 1) - rayStart.x) * rayUnitStepSize.x
    }
    var rayLength1Dy = when {
        rayDir.y < 0 -> (rayStart.y - (mapCheckY)) * rayUnitStepSize.y
        else -> ((mapCheckY + 1) - rayStart.y) * rayUnitStepSize.y
    }

    // Perform "Walk" until collision or range check
    var bTileFound = false
    val fMaxDistance = hypot(cellSize.width.toDouble(), cellSize.height.toDouble()) * maxTiles
    var fDistance = 0.0
    var dx = 0
    while (fDistance < fMaxDistance) {
        // Walk along shortest path
        if (rayLength1Dx < rayLength1Dy) {
            mapCheckX += stepX
            fDistance = rayLength1Dx
            rayLength1Dx += rayUnitStepSize.x
            dx = 0
        } else {
            mapCheckY += stepY
            fDistance = rayLength1Dy
            rayLength1Dy += rayUnitStepSize.y
            dx = 1
        }

        // Test tile at new test point
        if (collides(PointInt(mapCheckX, mapCheckY))) {
            bTileFound = true
            break
        }
    }

    // Calculate intersection location
    if (bTileFound) {
        //println("vRayStart=$vRayStart: vRayDir=$vRayDir, fDistance=$fDistance")
        return RayResult(
            this,
            (rayStart + rayDir * fDistance) * cellSize,
            if (dx == 0) Vector2D(-1.0 * rayDir.x.sign, 0.0) else Vector2D(0.0, -1.0 * rayDir.y.sign)
        )
    }
    return null
}

fun IStackedIntArray2.raycast(
    ray: Ray,
    cellSize: Size = Size(1.0, 1.0),
    maxTiles: Int = 10,
    collides: IStackedIntArray2.(tilePos: PointInt) -> Boolean
): RayResult? {
    return ray.firstCollisionInTileMap(cellSize, maxTiles) { pos -> collides(this, pos) }
}

fun IntIArray2.raycast(
    ray: Ray,
    cellSize: Size = Size(1f, 1f),
    maxTiles: Int = 10,
    collides: IntIArray2.(tilePos: PointInt) -> Boolean
): RayResult? {
    return ray.firstCollisionInTileMap(cellSize, maxTiles) { pos -> collides(this, pos) }
}
