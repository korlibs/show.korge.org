package korlibs.korge3d

import korlibs.math.geom.*


abstract class Camera3D : View3D() {
    //TODO: I don't think that a Camera should subtype View

    private var projMat = MMatrix3D()
    private var width: Float = 0f
    private var height: Float = 0f
    protected var dirty = true

    override var root: View3D = this

    protected inline fun dirty(cond: () -> Boolean, callback: () -> Unit) {
        if (cond()) {
            this.dirty = true
            callback()
        }
    }

    fun getProjMatrix(width: Float, height: Float): MMatrix3D {
        if (this.width != width || this.height != height) {
            this.dirty = true
            this.width = width
            this.height = height
        }
        if (dirty) {
            dirty = false
            updateMatrix(projMat, this.width, this.height)
        }
        return projMat
    }

    protected abstract fun updateMatrix(mat: MMatrix3D, width: Float, height: Float)

    override fun render(ctx: RenderContext3D) {
        // Do nothing except when debugging
    }

    abstract fun clone(): Camera3D

    class Perspective(
        fov: Angle = 60.degrees,
        near: Float = .5f,
        far: Float = 100.0f
    ) : Camera3D() {
        var fov: Angle = fov; set(value) = dirty({ field != value }) { field = value }
        var near: Float = near; set(value) = dirty({ field != value }) { field = value }
        var far: Float = far; set(value) = dirty({ field != value }) { field = value }

        fun set(fov: Angle = this.fov, near: Float = this.near, far: Float = this.far): Perspective {
            this.fov = fov
            this.near = near
            this.far = far
            return this
        }

        override fun updateMatrix(mat: MMatrix3D, width: Float, height: Float) {
            mat.setToPerspective(fov, if (height != 0.0f) width / height else 1.0f, near, far)
        }

        override fun clone(): Perspective = Perspective(fov, near, far).apply {
            this.transform.copyFrom(this@Perspective.transform)
        }
    }

    //TODO: position, target and up are also stored in transform....do we need repetition here?
    //val position = MVector4(0f, 1f, 10f)
    var position: Vector4 = Vector4(0f, 1f, 10f, 1f)
        set(value) {
            field = value
            update()
        }
    var yaw = (-90).degrees
    var pitch = 0.0.degrees
    var roll = 0.0.degrees
    var zoom = 45.degrees

    val front = MVector4(0f, 0f, -1f)
    private val worldUp = MVector4(0f, 1f, 0f)
    private val up = MVector4(0f, 1f, 0f)
    private val temp = MVector4()
    private val right = MVector4().cross(front, up).normalize()


    init {
        update()
    }

    private fun update() {
        val fx = yaw.cosine * pitch.cosine
        val fy = pitch.sine
        val fz = yaw.sine * pitch.cosine
        front.setTo(fx, fy, fz).normalize()
        right.cross(front, worldUp).normalize()
        up.cross(right, front).normalize()
        val tx = position.x + front.x
        val ty = position.y + front.y
        val tz = position.z + front.z
        this.transform.setTranslationAndLookAt(position.x, position.y, position.z, tx, ty, tz)
        invalidateRender()
    }

    @Deprecated("Use position= directly")
    fun setPosition(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        this.position = Vector4(x, y, z, 1f)
        update()
    }

    fun zoomIn(angle: Angle, deltaTime: Float) {

    }

    fun zoomOut(angle: Angle, deltaTime: Float) {

    }

    fun forward(speed: Float, deltaTime: Float) {
        val velocity = speed * deltaTime
        position = Vector4(position.x + (front.x * velocity), position.y + (front.y * velocity), position.z + (front.z * velocity), 1f)
        transform.setTranslation(position.x, position.y, position.z)
    }

    fun backwards(speed: Float, deltaTime: Float) {
        val velocity = speed * deltaTime
        position = Vector4(position.x - (front.x * velocity), position.y - (front.y * velocity), position.z - (front.z * velocity), 1f)
        transform.setTranslation(position.x, position.y, position.z)
    }

    fun strafeRight(speed: Float, deltaTime: Float) {
        val velocity = speed * deltaTime
        position = Vector4(position.x - (right.x * velocity), position.y - (right.y * velocity), position.z - (right.z * velocity), 1f)
        transform.setTranslation(position.x, position.y, position.z)
    }

    fun strafeLeft(speed: Float, deltaTime: Float) {
        val velocity = speed * deltaTime
        position = Vector4(position.x + (right.x * velocity), position.y + (right.y * velocity), position.z + (right.z * velocity), 1f)
        transform.setTranslation(position.x, position.y, position.z)
    }

    fun pitchDown(angle: Angle, deltaTime: Float) {
        pitch += (angle * deltaTime)
        update()
    }

    fun pitchUp(angle: Angle, deltaTime: Float) {
        pitch -= (angle * deltaTime)
        update()
    }

    fun yawLeft(angle: Angle, deltaTime: Float) {
        yaw -= (angle * deltaTime)
        update()
    }

    fun yawRight(angle: Angle, deltaTime: Float) {
        yaw += (angle * deltaTime)
        update()
    }

    // orbit about the target
    fun slewLeft(angle: Angle, deltaTime: Float) {

    }

    fun slewRight(angle: Angle, deltaTime: Float) {

    }
}

typealias PerspectiveCamera3D = Camera3D.Perspective
