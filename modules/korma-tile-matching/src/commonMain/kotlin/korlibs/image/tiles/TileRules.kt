package korlibs.image.tiles

import korlibs.datastructure.*
import korlibs.math.geom.*
import korlibs.memory.*
import kotlin.math.*

fun IntGridToTileGrid(ints: IntArray2, rules: IRuleMatcher, tiles: TileMapData, updated: RectangleInt = RectangleInt(0, 0, ints.width, ints.height)) {
    val l = (updated.left - rules.maxDist).coerceIn(0, ints.width)
    val r = (updated.right + rules.maxDist).coerceIn(0, ints.width)
    val t = (updated.top - rules.maxDist).coerceIn(0, ints.height)
    val b = (updated.bottom + rules.maxDist).coerceIn(0, ints.height)
    for (y in t until b) {
        for (x in l until r) {
            tiles[x, y] = rules.get(ints, x, y)
        }
    }
}

data class SimpleTileSpec(
    val left: Boolean = false,
    val up: Boolean = false,
    val right: Boolean = false,
    val down: Boolean = false,
) {
    val bits: Int = bits(left, up, right, down)

    companion object {
        fun bits(left: Boolean, up: Boolean, right: Boolean, down: Boolean): Int = 0
            .insert(left, 0).insert(up, 1).insert(right, 2).insert(down, 3)
    }
}

data class SimpleRule(
    val tile: Tile,
    val spec: SimpleTileSpec,
) {
    val left get() = spec.left
    val right get() = spec.right
    val up get() = spec.up
    val down get() = spec.down

    constructor(tile: Tile, left: Boolean = false, up: Boolean = false, right: Boolean = false, down: Boolean = false) : this(tile,
        SimpleTileSpec(left, up, right, down)
    )

    fun flippedX(): SimpleRule = SimpleRule(tile.flippedX(), right, up, left, down)
    fun flippedY(): SimpleRule = SimpleRule(tile.flippedY(), left, down, right, up)
    fun rotated(): SimpleRule = SimpleRule(tile.rotatedRight(), up = left, right = up, down = right, left = down)
    //fun rotated(): SimpleRule = SimpleRule(tile.rotated(), up = right, right = down, down = left, left = up)

    fun match(spec: SimpleTileSpec): Boolean {
        return this.spec == spec
    }
}

interface ISimpleTileProvider : IRuleMatcher {
    fun get(spec: SimpleTileSpec): Tile
}

interface IRuleMatcherMatch {
    val maxDist: Int
    fun match(ints: IntArray2, x: Int, y: Int): Boolean
}

interface IRuleMatcher {
    val maxDist: Int
    fun get(ints: IntArray2, x: Int, y: Int): Tile
}

class CombinedRuleMatcher(val rules: List<IRuleMatcher>) : IRuleMatcher {
    constructor(vararg rules: IRuleMatcher) : this(rules.toList())

    override val maxDist: Int by lazy { rules.maxOf { it.maxDist } }

    override fun get(ints: IntArray2, x: Int, y: Int): Tile {
        for (rule in rules) {
            val tile = rule.get(ints, x, y)
            if (tile.isValid) return tile
        }
        return Tile.INVALID
    }
}

class SimpleTileProvider(val value: Int) : ISimpleTileProvider, IRuleMatcher {
    override val maxDist: Int = 1

    //val rules = mutableSetOf<SimpleRule>()
    val ruleTable = arrayOfNulls<SimpleRule>(16)

    companion object {
        val FALSE = listOf(false)
        val BOOLS = listOf(false, true)
    }

    fun rule(
        rule: SimpleRule,
        registerFlipX: Boolean = true,
        registerFlipY: Boolean = true,
        registerRotated: Boolean = true,
    ) {
        for (fx in if (registerFlipX) BOOLS else FALSE) {
            for (fy in if (registerFlipY) BOOLS else FALSE) {
                for (rot in if (registerRotated) BOOLS else FALSE) {
                    var r = rule
                    if (rot) r = r.rotated()
                    if (fx) r = r.flippedX()
                    if (fy) r = r.flippedY()
                    val bits = r.spec.bits
                    if (ruleTable[bits] == null) ruleTable[bits] = r
                    //rules += r
                }
            }
        }
    }

    override fun get(spec: SimpleTileSpec): Tile {
        ruleTable[spec.bits]?.let { return it.tile }
        //for (rule in rules) {
        //    if (rule.match(spec)) {
        //        return rule.tile
        //    }
        //}
        return Tile.INVALID
    }

    override fun get(ints: IntArray2, x: Int, y: Int): Tile {
        if (ints.getOr(x, y) != value) return Tile.INVALID
        val left = ints.getOr(x - 1, y) == value
        val right = ints.getOr(x + 1, y) == value
        val up = ints.getOr(x, y - 1) == value
        val down = ints.getOr(x, y + 1) == value
        return get(SimpleTileSpec(left, up, right, down))
    }

    fun IntArray2.getOr(x: Int, y: Int): Int = if (inside(x, y)) get(x, y) else 0
}

data class TileMatch(val id: Int, val offset: PointInt, val eq: Boolean = true) : IRuleMatcherMatch {
    override val maxDist: Int = maxOf(offset.x.absoluteValue, offset.y.absoluteValue)

    val offsetX: Int = offset.x
    val offsetY: Int = offset.y

    fun flippedX(): TileMatch = TileMatch(id, PointInt(-offset.x, offset.y), eq)
    fun flippedY(): TileMatch = TileMatch(id, PointInt(offset.x, -offset.y), eq)
    fun rotated(): TileMatch = TileMatch(id, PointInt(offset.y, offset.x), eq)

    private fun comp(value: Int): Boolean = if (eq) value == id else value != id

    override fun match(ints: IntArray2, x: Int, y: Int): Boolean {
        return comp(ints[x + offset.x, y + offset.y])
    }
}

data class TileMatchGroup(val tile: Tile, val matches: List<TileMatch>) : IRuleMatcherMatch {
    constructor(tile: Tile, vararg matches: TileMatch) : this(tile, matches.toList())

    override val maxDist: Int by lazy { matches.maxOf { it.maxDist } }
    fun flippedX(): TileMatchGroup = TileMatchGroup(tile.flippedY(), matches.map { it.flippedX() })
    fun flippedY(): TileMatchGroup = TileMatchGroup(tile.flippedY(), matches.map { it.flippedY() })
    fun rotated(): TileMatchGroup = TileMatchGroup(tile.rotatedRight(), matches.map { it.rotated() })

    override fun match(ints: IntArray2, x: Int, y: Int): Boolean {
        return matches.all { it.match(ints, x, y) }
    }
}

class GenericTileProvider : IRuleMatcher {
    override val maxDist: Int = 1

    val rules = mutableSetOf<TileMatchGroup>()

    companion object {
        val FALSE = listOf(false)
        val BOOLS = listOf(false, true)
    }

    fun rule(
        rule: TileMatchGroup,
        registerFlipX: Boolean = true,
        registerFlipY: Boolean = true,
        registerRotated: Boolean = true,
    ) {
        for (fx in if (registerFlipX) BOOLS else FALSE) {
            for (fy in if (registerFlipY) BOOLS else FALSE) {
                for (rot in if (registerRotated) BOOLS else FALSE) {
                    var r = rule
                    if (rot) r = r.rotated()
                    if (fx) r = r.flippedX()
                    if (fy) r = r.flippedY()
                    rules += r
                }
            }
        }
    }

    override fun get(ints: IntArray2, x: Int, y: Int): Tile {
        for (rule in rules) if (rule.match(ints, x, y)) return rule.tile
        return Tile.INVALID
    }
}

fun TileMapData.pushInside(x: Int, y: Int, value: Tile) {
    if (inside(x, y)) {
        this.data.push(x, y, value.raw)
    }
}
