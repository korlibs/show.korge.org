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
	bundle("https://github.com/korlibs/korge-bundles.git::korge-box2d::7439e5c7de7442f2cd33a1944846d44aea31af0a##9fd9d54abd8abc4736fd3439f0904141d9b6a26e9e2f1e1f8e2ed10c51f490fd")
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
