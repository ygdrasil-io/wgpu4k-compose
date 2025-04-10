import Cube.cubePositionOffset
import Cube.cubeUVOffset
import Cube.cubeVertexArray
import Cube.cubeVertexCount
import Cube.cubeVertexSize
import io.ygdrasil.webgpu.*
import korlibs.math.geom.Angle
import korlibs.math.geom.Matrix4
import kotlin.math.PI

class RotatingCubeScene(private val context: WGPUContext) : AutoCloseable {

    var frame = 0
    protected val autoClosableContext = AutoClosableContext()

    internal val device: GPUDevice
        get() = context.device

    internal val renderingContext: RenderingContext
        get() = context.renderingContext

    lateinit var renderPipeline: GPURenderPipeline
    lateinit var projectionMatrix: Matrix4
    lateinit var renderPassDescriptor: RenderPassDescriptor
    lateinit var uniformBuffer: GPUBuffer
    lateinit var uniformBindGroup: GPUBindGroup
    lateinit var verticesBuffer: GPUBuffer

    fun initialize() = with(autoClosableContext) {
        // Create dummy texture, as we manipulate immutable data and we need to assign a texture early
        val dummyTexture by lazy {
            device.createTexture(
                TextureDescriptor(
                    size = Extent3D(1u, 1u),
                    format = GPUTextureFormat.Depth24Plus,
                    usage = setOf(GPUTextureUsage.RenderAttachment),
                )
            ).also { with(autoClosableContext) { it.bind() } }
        }

        // Create a vertex buffer from the cube data.
        verticesBuffer = device.createBuffer(
            BufferDescriptor(
                size = (cubeVertexArray.size * Float.SIZE_BYTES).toULong(),
                usage = setOf(GPUBufferUsage.Vertex),
                mappedAtCreation = true
            )
        ).bind()

        // Util method to use getMappedRange
        verticesBuffer.mapFrom(cubeVertexArray)
        verticesBuffer.unmap()

        renderPipeline = device.createRenderPipeline(
            RenderPipelineDescriptor(
                vertex = VertexState(
                    module = device.createShaderModule(
                        ShaderModuleDescriptor(
                            code = basicVertexShader
                        ),
                    ).bind(), // bind to autoClosableContext to release it later
                    entryPoint = "main",
                    buffers = listOf(
                        VertexBufferLayout(
                            arrayStride = cubeVertexSize,
                            attributes = listOf(
                                VertexAttribute(
                                    shaderLocation = 0u,
                                    offset = cubePositionOffset,
                                    format = GPUVertexFormat.Float32x4
                                ),
                                VertexAttribute(
                                    shaderLocation = 1u,
                                    offset = cubeUVOffset,
                                    format = GPUVertexFormat.Float32x2
                                )
                            )
                        )
                    )
                ),
                fragment = FragmentState(
                    module = device.createShaderModule(
                        ShaderModuleDescriptor(
                            code = vertexPositionColorShader
                        )
                    ).bind(), // bind to autoClosableContext to release it later
                    entryPoint = "main",
                    targets = listOf(
                        ColorTargetState(
                            format = renderingContext.textureFormat
                        )
                    )
                ),
                primitive = PrimitiveState(
                    topology = GPUPrimitiveTopology.TriangleList,
                    cullMode = GPUCullMode.Back
                ),
                depthStencil = DepthStencilState(
                    depthWriteEnabled = true,
                    depthCompare = GPUCompareFunction.Less,
                    format = GPUTextureFormat.Depth24Plus
                ),
                multisample = MultisampleState(
                    count = 1u,
                    mask = 0xFFFFFFFu
                )
            )
        ).bind()

        val depthTexture = device.createTexture(
            TextureDescriptor(
                size = Extent3D(renderingContext.width, renderingContext.height),
                format = GPUTextureFormat.Depth24Plus,
                usage = setOf(GPUTextureUsage.RenderAttachment),
            )
        ).bind()

        val uniformBufferSize = 4uL * 16uL // 4x4 matrix
        uniformBuffer = device.createBuffer(
            BufferDescriptor(
                size = uniformBufferSize,
                usage = setOf(GPUBufferUsage.Uniform, GPUBufferUsage.CopyDst)
            )
        ).bind()

        uniformBindGroup = device.createBindGroup(
            BindGroupDescriptor(
                layout = renderPipeline.getBindGroupLayout(0u),
                entries = listOf(
                    BindGroupEntry(
                        binding = 0u,
                        resource = BufferBinding(
                            buffer = uniformBuffer
                        )
                    )
                )
            )
        ).bind()

        renderPassDescriptor = RenderPassDescriptor(
            colorAttachments = listOf(
                RenderPassColorAttachment(
                    view = dummyTexture.createView().bind(), // Assigned later
                    loadOp = GPULoadOp.Clear,
                    clearValue = Color(0.5, 0.5, 0.5, 1.0),
                    storeOp = GPUStoreOp.Store,
                )
            ),
            depthStencilAttachment = RenderPassDepthStencilAttachment(
                view = depthTexture.createView(),
                depthClearValue = 1.0f,
                depthLoadOp = GPULoadOp.Clear,
                depthStoreOp = GPUStoreOp.Store
            )
        )


        val aspect = renderingContext.width.toInt() / renderingContext.height.toDouble()
        val fox = Angle.fromRadians((2 * PI) / 5)
        projectionMatrix = Matrix4.perspective(fox, aspect, 1.0, 100.0)
    }

    fun AutoClosableContext.render() {
        val transformationMatrix = getTransformationMatrix(
            frame / 100.0,
            projectionMatrix
        )
        device.queue.writeBuffer(
            uniformBuffer,
            0u,
            transformationMatrix,
            0u,
            transformationMatrix.size.toULong()
        )

        renderPassDescriptor = renderPassDescriptor.copy(
            colorAttachments = listOf(
                (renderPassDescriptor.colorAttachments[0] as RenderPassColorAttachment).copy(
                    view = renderingContext.getCurrentTexture()
                        .createView()
                        .bind()
                )
            )
        )

        val encoder = device.createCommandEncoder()
            .bind()

        encoder.beginRenderPass(renderPassDescriptor) {
            setPipeline(renderPipeline)
            setBindGroup(0u, uniformBindGroup)
            setVertexBuffer(0u, verticesBuffer)
            draw(cubeVertexCount)
            end()
        }

        val commandBuffer = encoder.finish()
            .bind()

        device.queue.submit(listOf(commandBuffer))
    }


    override fun close() {
        autoClosableContext.close()
    }
}


private fun getTransformationMatrix(angle: Double, projectionMatrix: Matrix4): FloatArray {
    var viewMatrix = Matrix4.IDENTITY
    viewMatrix = viewMatrix.translated(0, 0, -4)

    viewMatrix = viewMatrix.rotated(
        Angle.fromRadians(Angle.fromRadians(angle).sine),
        Angle.fromRadians(Angle.fromRadians(angle).cosine),
        Angle.fromRadians(0)
    )

    return (projectionMatrix * viewMatrix).copyToColumns()
}