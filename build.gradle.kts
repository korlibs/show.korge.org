import com.soywiz.korge.gradle.*

buildscript {
	val korgePluginVersion: String by project

	repositories {
		mavenLocal()
		mavenCentral()
		google()
		maven { url = uri("https://plugins.gradle.org/m2/") }
	}
	dependencies {
		classpath("com.soywiz.korlibs.korge.plugins:korge-gradle-plugin:$korgePluginVersion")
	}
}

apply<KorgeGradlePlugin>()

korge {
	id = "org.korge.show"

// To enable all targets at once

	//targetAll()

// To enable targets based on properties/environment variables
	//targetDefault()

// To selectively enable targets
	
	targetJvm()
	targetJs()
	//targetDesktop()
	//targetIos()
	//targetAndroidIndirect() // targetAndroidDirect()
	//targetAndroidDirect()

	// Box2d
	bundle("https://github.com/korlibs/korge-bundles.git::korge-box2d::f400451fba241e33a863cedf924ab2b673a534cb##37fb37db98214464267051ee4a9bb9a37cfc830d520552340f88ccd5fdcd4bdc")

	supportDragonbones()
	supportSpine()
	supportSwf()
	supportTriangulation()
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
