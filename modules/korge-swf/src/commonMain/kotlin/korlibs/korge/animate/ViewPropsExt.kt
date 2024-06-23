package korlibs.korge.animate

import korlibs.datastructure.*
import korlibs.korge.render.*
import korlibs.korge.view.*

/*
val View.props: MutableMap<String, String> by Extra.PropertyThis { mutableMapOf<String, String>() }

fun View.addProps(props: Map<String, String>) {
    this.props.putAll(props)
}

 */


// region Properties
private val View._props: MutableMap<String, Any?> by Extra.PropertyThis { linkedMapOf<String, Any?>() }

/** Immutable map of custom String properties attached to this view. Should use [hasProp], [getProp] and [addProp] methods to control this */
val View.props: Map<String, Any?> get() = _props

/** Checks if this view has the [key] property */
fun View.hasProp(key: String) = key in _props

/** Gets the [key] property of this view as a [String] or [default] when not found */
fun View.getPropString(key: String, default: String = "") = _props[key]?.toString() ?: default

/** Gets the [key] property of this view as an [Double] or [default] when not found */
fun View.getPropDouble(key: String, default: Double = 0.0): Double {
    val value = _props[key]
    if (value is Number) return value.toDouble()
    if (value is String) return value.toDoubleOrNull() ?: default
    return default
}

/** Gets the [key] property of this view as an [Int] or [default] when not found */
fun View.getPropInt(key: String, default: Int = 0) = getPropDouble(key, default.toDouble()).toInt()

/** Adds or replaces the property [key] with the [value] */
fun View.addProp(key: String, value: Any?) {
    _props[key] = value
    //val componentGen = views.propsTriggers[key]
    //if (componentGen != null) {
    //	componentGen(this, key, value)
    //}
}

/** Adds a list of [values] properties at once */
fun View.addProps(values: Map<String, Any?>) {
    for (pair in values) addProp(pair.key, pair.value)
}
// endregion

// Returns the typed property associated with the provided key.
// Crashes if the key is not found or if failed to cast to type.
inline fun <reified T : Any> View.getProp(key: String): T {
    return getPropOrNull(key)!!
}

// Returns the typed property associated with the provided key or null if it doesn't exist
// Crashes if failed to cast to type.
inline fun <reified T : Any> View.getPropOrNull(key: String): T? {
    return props[key] as T?
}


/** Returns a list of descendants having the property [prop] optionally matching the value [value]. */
fun View?.descendantsWithProp(prop: String, value: String? = null): List<View> {
    if (this == null) return listOf()
    return this.descendantsWith {
        if (value != null) {
            it.props[prop] == value
        } else {
            prop in it.props
        }
    }
}

/** Returns a list of descendants having the property [prop] optionally matching the value [value]. */
fun View?.descendantsWithPropString(prop: String, value: String? = null): List<Pair<View, String>> =
    this.descendantsWithProp(prop, value).map { it to it.getPropString(prop) }

/** Returns a list of descendants having the property [prop] optionally matching the value [value]. */
fun View?.descendantsWithPropInt(prop: String, value: Int? = null): List<Pair<View, Int>> =
    this.descendantsWithProp(prop, if (value != null) "$value" else null).map { it to it.getPropInt(prop) }

/** Returns a list of descendants having the property [prop] optionally matching the value [value]. */
fun View?.descendantsWithPropDouble(prop: String, value: Double? = null): List<Pair<View, Int>> =
    this.descendantsWithProp(prop, if (value != null) "$value" else null).map { it to it.getPropInt(prop) }
