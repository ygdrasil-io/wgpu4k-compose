@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.configureSwingGlobalsForCompose
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ygdrasil.wgpu.GLFWContext
import io.ygdrasil.wgpu.WGPU.Companion.loadLibrary
import io.ygdrasil.wgpu.glfwContextRenderer
import io.ygdrasil.wgpu.internal.jvm.panama.WGPULogCallback
import io.ygdrasil.wgpu.internal.jvm.panama.wgpu_h
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFW.glfwWindowShouldClose
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

val callback = WGPULogCallback.allocate({ level, message, data ->
    println("LOG {$level} ${message.getString(0)}")
}, Arena.global())

var glfwContext: GLFWContext? = null

fun main() {
    configureSwingGlobalsForCompose()
    loadLibrary()
    wgpu_h.wgpuSetLogLevel(1)
    wgpu_h.wgpuSetLogCallback(callback, MemorySegment.NULL)

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
