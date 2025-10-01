package com.vs18.gradle_utils

import org.gradle.api.*
import org.gradle.api.tasks.*

abstract class PrintFlavorTask : DefaultTask() {

    @TaskAction
    fun print() {
        println(">>> Building flavor: ${project.findProject("FLAVOR") ?: "default"}")
    }
}