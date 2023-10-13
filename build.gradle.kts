import korlibs.korge.gradle.*

plugins {
	alias(libs.plugins.korge)
}

korge {
	id = "org.korge.show"

// To enable all targets at once

	//targetAll()

// To enable targets based on properties/environment variables
	//targetDefault()

// To selectively enable targets

	// @TODO: Quick fix since it is generating wrong JS identifiers (files starting with numbers)
	//autoGenerateTypedResources = false
	
	targetJvm()
	targetJs()
	targetIos()
	targetAndroid()

	//proguardObfuscate = false

	// Box2d
	//bundle("https://github.com/korlibs/korge-bundles.git::korge-box2d::f400451fba241e33a863cedf924ab2b673a534cb##37fb37db98214464267051ee4a9bb9a37cfc830d520552340f88ccd5fdcd4bdc")

	//supportDragonbones()
	//supportSpine()
	//supportSwf()
	supportVibration()
}

fun sourceSetDependsOn(base: String, vararg dependencies: String) {
	for (suffix in listOf("Main", "Test")) {
		for (dependency in dependencies) {
			kotlin.sourceSets.maybeCreate("$base$suffix").dependsOn(kotlin.sourceSets.maybeCreate("$dependency$suffix"))
		}
	}
}

sourceSetDependsOn("nonJvm", "common")
sourceSetDependsOn("nonJs", "common")

sourceSetDependsOn("js", "nonJvm")
sourceSetDependsOn("jvm", "nonJs")

dependencies {
	add("commonMainApi", project(":deps"))
}

