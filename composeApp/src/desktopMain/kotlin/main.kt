@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.configureSwingGlobalsForCompose
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ffi.LibraryLoader
import io.ygdrasil.webgpu.GLFWContext
import io.ygdrasil.webgpu.glfwContextRenderer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFW.glfwWindowShouldClose

var glfwContext: GLFWContext? = null

fun main() {
    configureSwingGlobalsForCompose()
    LibraryLoader.load()

    val composeThread = Thread {
        println("thread started")
        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "TestProject",
            ) {
                App(glfwContext?.wgpuContext)
            }
        }
    }

    runBlocking {

        glfwContext = glfwContextRenderer(width = 512, height = 512, deferredRendering = true)

        println("will run compose app")
        composeThread.start()
        println("will wait now on this one")

        while (!glfwWindowShouldClose(glfwContext!!.windowHandler)) {
            glfwPollEvents()
            yield()
        }

        glfwContext!!.close()
    }
}
