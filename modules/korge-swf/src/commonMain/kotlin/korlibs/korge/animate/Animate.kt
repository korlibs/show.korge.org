package korlibs.korge.animate

import korlibs.datastructure.*
import korlibs.datastructure.iterators.fastForEach
import korlibs.graphics.shader.*
import korlibs.korge.view.property.*
import korlibs.image.bitmap.Bitmaps
import korlibs.image.bitmap.BmpSlice
import korlibs.image.color.*
import korlibs.image.vector.EmptyShape
import korlibs.image.vector.Shape
import korlibs.io.async.Signal
import korlibs.io.async.launchImmediately
import korlibs.io.lang.Closeable
import korlibs.io.util.Once
import korlibs.korge.animate.internal.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.korge.view.IHtml
import korlibs.korge.view.IText
import korlibs.korge.view.textOld.*
import korlibs.math.*
import korlibs.math.geom.*
import korlibs.math.geom.vector.VectorPath
import korlibs.math.interpolation.*
import korlibs.time.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job

interface AnElement {
	val library: AnLibrary
	val symbol: AnSymbol
}

fun AnElement.createDuplicated() = symbol.create(library)
fun AnElement.createDuplicatedView() = symbol.create(library) as View

abstract class AnBaseShape(final override val library: AnLibrary, final override val symbol: AnSymbolBaseShape) :
	Container(), AnElement {
	var ninePatch: MRectangle? = null

	abstract var dx: Float
	abstract var dy: Float
	abstract val tex: BmpSlice
    abstract val shape: Shape?
    abstract val graphicsRenderer: GraphicsRenderer
	abstract val texScale: Double
	abstract val texWidth: Float
	abstract val texHeight: Float
	abstract val smoothing: Boolean
    var graphics: Graphics? = null
    //private var graphics: Graphics? = null

	@ViewProperty
    var dxDouble: Double
        get() = dx.toDouble()
        set(value) { dx = value.toFloat() }

	@ViewProperty
    var dyDouble: Double
        get() = dy.toDouble()
        set(value) { dy = value.toFloat() }

    val posCuts = arrayOf(Point(0.0, 0.0), Point(0.25, 0.25), Point(0.75, 0.75), Point(1.0, 1.0))
	val texCuts = arrayOf(Point(0.0, 0.0), Point(0.25, 0.25), Point(0.75, 0.75), Point(1.0, 1.0))

    private var cachedShape: Shape? = null

    private fun ensureShape(): Shape? {
        if (shape != null) {
            if (graphics == null) {
                graphics = graphics(EmptyShape, library.graphicsRenderer ?: graphicsRenderer)
                //graphics = graphics(shape!!)
            }
            if (cachedShape !== shape) {
                cachedShape = shape
                graphics?.shape = shape!!
            }
        }
        return shape
    }

	override fun renderInternal(ctx: RenderContext) {
		if (!visible) return
        if (ensureShape() != null) {
            graphics?.renderer = library.graphicsRenderer ?: graphicsRenderer
            super.renderInternal(ctx)
            return
        }
        ctx.useBatcher { batch ->
            //println("%08X".format(globalColor))
            //println("$id: " + globalColorTransform + " : " + colorTransform + " : " + parent?.colorTransform)
            //println(ninePatch)

            if (ninePatch != null) {
                val np = ninePatch!!
                val lm = parent!!.localMatrix

                val npLeft = np.left - dx
                val npTop = np.top - dy

                val npRight = np.right - dx
                val npBottom = np.bottom - dy

                val ascaleX = lm.a
                val ascaleY = lm.d

                posCuts[1] = Point(((npLeft) / texWidth) / ascaleX, ((npTop) / texHeight) / ascaleY)
                posCuts[2] = Point(
                    1.0 - ((texWidth - npRight) / texWidth) / ascaleX,
                    1.0 - ((texHeight - npBottom) / texHeight) / ascaleY
                )
                texCuts[1] = Point((npLeft / texWidth), (npTop / texHeight))
                texCuts[2] = Point((npRight / texWidth), (npBottom / texWidth))

                batch.drawNinePatch(
                    ctx.getTex(tex),
                    x = dx,
                    y = dy,
                    width = texWidth,
                    height = texHeight,
                    posCuts = posCuts,
                    texCuts = texCuts,
                    m = this.globalMatrix.mutable,
                    filtering = smoothing,
                    colorMul = renderColorMul,
                    //colorAdd = renderColorAdd,
                    blendMode = renderBlendMode,
                    //premultiplied = tex.base.premultiplied,
                    //wrap = false,
                )
            } else {
                batch.drawQuad(
                    ctx.getTex(tex),
                    x = dx,
                    y = dy,
                    width = texWidth,
                    height = texHeight,
                    m = globalMatrix,
                    filtering = smoothing,
                    colorMul = renderColorMul,
                    //colorAdd = renderColorAdd,
                    blendMode = renderBlendMode,
                    //premultiplied = tex.base.premultiplied,
                    //wrap = false,
                )
            }
        }
	}

    override var hitShape: VectorPath? = null
        get() = field ?: symbol.path
        set(value) { field = value }

    //override fun hitTest(x: Double, y: Double): View? {
	//	val sLeft = dx.toDouble()
	//	val sTop = dy.toDouble()
	//	val sRight = sLeft + texWidth
	//	val sBottom = sTop + texHeight
	//	return if (checkGlobalBounds(x, y, sLeft, sTop, sRight, sBottom) &&
	//		(symbol.path?.containsPoint(globalToLocalX(x, y), globalToLocalY(x, y)) != false)
	//	) this else null
	//}

	override fun getLocalBoundsInternal(): Rectangle {
        if (ensureShape() != null) {
            return super.getLocalBoundsInternal()
        }
		return Rectangle(dx, dy, texWidth, texHeight)
	}

	override fun toString(): String = super.toString() + ":symbol=" + symbol

	override fun createInstance(): View = symbol.create(library) as View

    //override fun buildDebugComponent(views: Views, container: UiContainer) {
    //    val view = this
    //    container.uiCollapsibleSection("AnBaseShape") {
    //        uiEditableValue(Pair(view::dxDouble, view::dyDouble), name = "dxy", clamp = false)
    //        button("Center").onClick {
    //            view.dx = (-view.width / 2).toFloat()
    //            view.dy = (-view.height / 2).toFloat()
    //        }
    //    }
    //    super.buildDebugComponent(views, container)
    //}
}

class AnShape(library: AnLibrary, val shapeSymbol: AnSymbolShape) : AnBaseShape(library, shapeSymbol), AnElement {
	override var dx = shapeSymbol.bounds.x.toFloat()
	override var dy = shapeSymbol.bounds.y.toFloat()
	override val tex = shapeSymbol.textureWithBitmap?.texture ?: Bitmaps.transparent
    override val shape: Shape? = shapeSymbol.shapeGen?.invoke()
    override val graphicsRenderer: GraphicsRenderer get() = shapeSymbol.graphicsRenderer
	override val texScale = shapeSymbol.textureWithBitmap?.scale ?: 1.0
	override val texWidth = (tex.width / texScale).toFloat()
	override val texHeight = (tex.height / texScale).toFloat()
	override val smoothing = true
}

class AnMorphShape(library: AnLibrary, val morphSymbol: AnSymbolMorphShape) : AnBaseShape(library, morphSymbol),
	AnElement {
	private val timedResult = Timed.Result<TextureWithBitmapSlice>()

	var texWBS: TextureWithBitmapSlice? = null
	override var dx: Float = 0f
	override var dy: Float = 0f
	override var tex: BmpSlice = Bitmaps.transparent
    override var shape: Shape? = null
    override var graphicsRenderer: GraphicsRenderer = morphSymbol.graphicsRenderer
	override var texScale = 1.0
	override var texWidth = 0f
	override var texHeight = 0f
	override var smoothing = true

	private fun updatedRatio() {
        if (morphSymbol.shapeGen != null) {
            shape = morphSymbol.shapeGen!!.invoke(ratio.toDouble())
            return
        }
		val result = morphSymbol.texturesWithBitmap.find(ratio.toDouble().seconds, timedResult)
		texWBS = result.left ?: result.right

		dx = texWBS?.bounds?.x?.toFloat() ?: 0f
		dy = texWBS?.bounds?.y?.toFloat() ?: 0f
		tex = texWBS?.texture ?: Bitmaps.transparent
		texScale = texWBS?.scale ?: 1.0
		texWidth = (tex.width / texScale).toFloat()
		texHeight = (tex.height / texScale).toFloat()
		smoothing = true
	}

    @ViewProperty
    var ratio: Ratio = Ratio.ZERO
		set(value) {
			field = value
			updatedRatio()
		}

	init {
		updatedRatio()
	}

	override fun createInstance(): View = AnMorphShape(library, morphSymbol)

	override fun copyPropsFrom(source: View) {
		val src = (source as AnMorphShape)
		this.dx = src.dx
		this.dy = src.dy
		this.tex = src.tex
		this.texScale = src.texScale
		this.texWidth = src.texWidth
		this.texHeight = src.texHeight
		this.smoothing = src.smoothing
	}
}

class AnEmptyView(override val library: AnLibrary, override val symbol: AnSymbolEmpty = AnSymbolEmpty) : DummyView(), AnElement {
	override fun createInstance(): View = symbol.create(library) as View
}

@OptIn(KorgeDeprecated::class)
class AnTextField(override val library: AnLibrary, override val symbol: AnTextFieldSymbol) : Container(),
	AnElement, IText, IHtml, ViewLeaf {
    private val textField = TextOld("", 16.0).apply {
        fontsCatalog = library.fontsCatalog
		textBounds.copyFrom(this@AnTextField.symbol.bounds)
		html = this@AnTextField.symbol.initialHtml
		relayout()
	}

	init {
		this += textField
	}

	var format: Html.Format by textField::format

	@ViewProperty
	override var text: String by textField::text
	override var html: String by textField::html

	override fun createInstance(): View = symbol.create(library) as View

    //override fun buildDebugComponent(views: Views, container: UiContainer) {
    //    container.uiCollapsibleSection("AnTextField") {
    //        uiEditableValue(::text)
    //    }
    //    super.buildDebugComponent(views, container)
    //}
}

//class PopMaskView(views: Views) : View(views)

class TimelineRunner(val view: AnMovieClip, val symbol: AnSymbolMovieClip) {
	//var firstUpdateSingleFrame = false
	val library: AnLibrary get() = view.library
	val context get() = library.context
	var currentTime = 0.milliseconds
	var currentStateName: String? = null
	var currentSubtimeline: AnSymbolMovieClipSubTimeline? = null
	val currentStateTotalTime: TimeSpan get() = if (currentSubtimeline != null) currentSubtimeline!!.totalTime else 0.milliseconds
	val onStop = Signal<Unit>()
	val onChangeState = Signal<String>()
	val onEvent = Signal<String>()

	var running = true
		internal set(value) {
			field = value
			if (!value) {
				onStop(Unit)
			}
		}

	init {
		gotoAndPlay("default")
	}

	fun getStateTime(name: String): TimeSpan {
		val substate = symbol.states[name] ?: return 0.milliseconds
		return substate.subTimeline.totalTime - substate.startTime
	}

	fun gotoAndRunning(running: Boolean, name: String, time: TimeSpan = 0.milliseconds) {
		val substate = symbol.states[name]
		if (substate != null) {
			this.currentStateName = substate.name
			this.currentSubtimeline = substate.subTimeline
			this.currentTime = substate.startTime + time
			this.running = running
			//this.firstUpdateSingleFrame = true
			update(0.milliseconds)
			onChangeState(name)
		} else {
            println("Can't find state with name '$name' : ${symbol.states.keys}")
        }
        //println("gotoAndRunning: running=$running, name=$name, time=$time")
		//println("currentStateName: $currentStateName, running=$running, currentTime=$currentTime, time=$time, totalTime=$currentStateTotalTime")
	}

	fun gotoAndPlay(name: String, time: TimeSpan = 0.milliseconds) = gotoAndRunning(true, name, time)
	fun gotoAndStop(name: String, time: TimeSpan = 0.milliseconds) = gotoAndRunning(false, name, time)

    var ratio: Ratio
        get() = Ratio(currentTime / currentStateTotalTime)
        set(value) {
            currentTime = (currentStateTotalTime * value.toDouble().clamp01())
        }

	fun update(time: TimeSpan) {
		//println("Update[1]: $currentTime")
		//println("$currentStateName: $currentTime: running=$running")
		if (!running) return
		//println("Update[2]: $currentTime")
		if (currentSubtimeline == null) return
		//println("Update[3]: $currentTime")
		val cs = currentSubtimeline!!
		eval(currentTime, min(currentStateTotalTime, currentTime + time))
		currentTime += time
		//println("Update[4]: $currentTime : delta=$time")
		if (currentTime >= currentStateTotalTime) {
			//println("currentTime >= currentStateTotalTime :: ${currentTime} >= ${currentStateTotalTime}")
			val accumulatedTime = currentTime - currentStateTotalTime
			val nextState = cs.nextState

			if (nextState == null) {
				running = false
			} else {
				//gotoAndRunning(cs.nextStatePlay, nextState, accumulatedTime)
				gotoAndRunning(cs.nextStatePlay, nextState, 0.milliseconds)
				currentTime += accumulatedTime
				eval(currentTime - accumulatedTime, currentTime)
			}

		}
	}

	private val tempRangeResult = Timed.RangeResult()

	private fun eval(prev: TimeSpan, current: TimeSpan) {
		if (prev >= current) return
		val actionsTimeline = this.currentSubtimeline?.actions ?: return
		val result = actionsTimeline.getRangeIndices(prev, current - 1.microseconds, out = tempRangeResult)

		execution@ for (n in result.startIndex..result.endIndex) {
			val action = actionsTimeline.objects[n]
			//println(" Action: $action")
			when (action) {
				is AnPlaySoundAction -> {
                    launchImmediately(library.context.coroutineContext) {
						(library.symbolsById[action.soundId] as AnSymbolSound?)?.getNativeSound()?.play()
					}
					//println("play sound!")
				}
				is AnEventAction -> {
					//println("Dispatched event(${onEvent}): ${action.event}")
					onEvent(action.event)
				}
			}
		}
	}
}

interface AnPlayable {
	fun play(name: String): Unit
}

class AnSimpleAnimation(
	val frameTime: TimeSpan,
	val animations: Map<String, List<BmpSlice?>>,
	val anchor: Anchor = Anchor.TOP_LEFT
) : Container(), AnPlayable {
	override fun createInstance(): View = AnSimpleAnimation(frameTime, animations, anchor)

	val image = Image(Bitmaps.transparent)
	val defaultAnimation = animations.values.firstOrNull() ?: listOf()
	var animation = defaultAnimation
	val numberOfFrames get() = animation.size
	private var elapsedTime = 0.milliseconds

	init {
		image.anchor = anchor
		myupdate()
		this += image
	}

	override fun play(name: String) {
		animation = animations[name] ?: defaultAnimation
	}

	init {
		addUpdater {
			elapsedTime = (elapsedTime + it) % (frameTime * numberOfFrames)
			myupdate()
		}
	}

	private fun myupdate() {
		val frameNum = (elapsedTime / frameTime).toInt()
		val bmpSlice = animation.getOrNull(frameNum % numberOfFrames) ?: Bitmaps.transparent
		image.bitmap = bmpSlice
	}
}

class AnMovieClip(override val library: AnLibrary, override val symbol: AnSymbolMovieClip) : Container(),
	AnElement, AnPlayable {
	override fun clone(): View = createInstance().apply {
		this@apply.copyPropsFrom(this)
	}

    override fun getLocalBoundsInternal(): Rectangle {
        if (symbol.id == 0) {
            return Rectangle(0, 0, library.width, library.height)
        } else {
            return super.getLocalBoundsInternal()
        }
    }

    override fun createInstance(): View = symbol.create(library) as View

	private val tempTimedResult = Timed.Result<AnSymbolTimelineFrame>()
	val totalDepths = symbol.limits.totalDepths
	val totalUids = symbol.limits.totalUids
	val dummyDepths = Array(totalDepths) { DummyView() }
	val maskPushDepths = IntArray(totalDepths + 10) { -1 }
	val maskPopDepths = BooleanArray(totalDepths + 10) { false }
	val viewUids = Array(totalUids) {
		val info = symbol.uidInfo[it]
        if (info.characterId == symbol.id) error("Recursive detection")
		(library.create(info.characterId) as View).also { it.addProps(info.extraProps) }
	}
	var firstUpdate = true
	var smoothing = library.defaultSmoothing
	val singleFrame = symbol.limits.totalFrames <= 1
	val unsortedChildren = ArrayList<View>(dummyDepths.toList())
	val timelineRunner = TimelineRunner(this, symbol)
	val onStop get() = timelineRunner.onStop
	val onEvent get() = timelineRunner.onEvent
	val onChangeState get() = timelineRunner.onChangeState

	val currentState: String? get() = timelineRunner.currentStateName

	init {
		dummyDepths.fastForEach { d ->
			this += d
		}
        addUpdater { updateInternal(it) }
	}

	private fun replaceDepth(depth: Int, view: View): Boolean {
		val result = unsortedChildren[depth].replaceWith(view)
		unsortedChildren[depth] = view
		return result
	}

	override fun reset() {
		super.reset()
		viewUids.fastForEach { view ->
			view.reset()
		}
		for (n in 0 until unsortedChildren.size) {
			replaceDepth(n, dummyDepths[n])
		}
	}

	private val tempMatrix = MMatrix()
	override fun renderInternal(ctx: RenderContext) {
		if (!visible) return

		maskPopDepths.fill(false)

		var usedStencil = false

		var state = 0

		//println("::::")

        forEachChildWithIndex { depth: Int, child: View ->
			val maskDepth = maskPushDepths.getOrElse(depth) { -1 }

			// Push Mask
			if (maskDepth >= 0) {
				if (maskDepth in maskPopDepths.indices) {
					maskPopDepths[maskDepth] = true
					ctx.stencilIndex++
					usedStencil = true
                    MaskStates.STATE_SHAPE.set(ctx, ctx.stencilIndex)
					state = 1
					//println(" shape")
				}
			}

			val showChild = when {
				ctx.masksEnabled -> true
				else -> {
					true
					//(ctx.stencilIndex <= 0) || (state != 2)
				}
			}

			//println("$depth:")
			//println(ctx.batch.colorMask)
			if (showChild) {
				child.render(ctx)
			}

			// Mask content
			if (maskDepth >= 0) {
				//println(" content")
                MaskStates.STATE_CONTENT.set(ctx, ctx.stencilIndex)
				state = 2
			}

			// Pop Mask
			if (maskPopDepths.getOrElse(depth) { false }) {
				//println(" none")
                MaskStates.STATE_NONE.set(ctx, referenceValue = 0)
				ctx.stencilIndex--
				state = 0
			}

			//println("  " + ctx.batch.colorMask)
		}

        // Reset stencil
		if (usedStencil && ctx.stencilIndex <= 0) {
			//println("ctx.stencilIndex: ${ctx.stencilIndex}")
			ctx.stencilIndex = 0
			ctx.clear(clearColor = false, clearDepth = false, clearStencil = true, stencil = ctx.stencilIndex)
		}
	}

	private fun update() {
		for (depth in 0 until totalDepths) {
			val timelines = timelineRunner.currentSubtimeline?.timelines ?: continue
			val timeline = timelines[depth]
			if (timeline.size <= 0) continue // No Frames!
			val hasMultipleFrames = timeline.size > 1

			if (smoothing) {
				val (index, left, right, ratio) = timeline.find(timelineRunner.currentTime, out = tempTimedResult)
				if (left != null) maskPushDepths[left.depth] = left.clipDepth

				val view = if (left != null && left.uid >= 0) viewUids[left.uid] else dummyDepths[depth]

				//if (view.name == "action") {
				//	println("action")
				//}

				val placed = replaceDepth(depth, view)
				if (placed || hasMultipleFrames) {
					if ((left != null) && (right != null) && (left.uid == right.uid)) {
						//println("$currentTime: $index")
						AnSymbolTimelineFrame.setToViewInterpolated(view, left, right, ratio.toRatio())
					} else {
						//println("$currentTime: $index")
						left?.setToView(view)
						//println(left.colorTransform)
					}
					if (symbol.ninePatch != null && view is AnBaseShape) view.ninePatch = symbol.ninePatch
				}
			} else {
				val (index, left) = timeline.findWithoutInterpolation(timelineRunner.currentTime, out = tempTimedResult)
				if (left != null) maskPushDepths[left.depth] = left.clipDepth
				val view = if (left != null && left.uid >= 0) viewUids[left.uid] else dummyDepths[depth]
				//println("$currentTime: $index")
				val placed = replaceDepth(depth, view)
				if (placed || hasMultipleFrames) {
					left?.setToView(view)
					if (symbol.ninePatch != null && view is AnBaseShape) view.ninePatch = symbol.ninePatch
				}
			}
		}
		//timelineRunner.firstUpdateSingleFrame = false
	}

	val stateNames get() = symbol.states.map { it.value.name }

	/**
	 * Changes the state and plays it
	 */
	override fun play(name: String) {
		timelineRunner.gotoAndPlay(name)
		update()
	}

    fun playAndStop(name: String) {
        timelineRunner.gotoAndStop(name)
        update()
    }

	@ViewProperty
    fun play() {
        //println("stop")
        timelineRunner.running = true
        update()
    }

	@ViewProperty
    fun stop() {
        //println("stop")
        timelineRunner.running = false
        update()
    }

   //override fun buildDebugComponent(views: Views, container: UiContainer) {
   //    container.uiCollapsibleSection("SWF") {
   //        addChild(UiRowEditableValue(app, "symbol", UiListEditableValue(app, { library.symbolsByName.keys.toList() }, ObservableProperty(
   //            name = "symbol",
   //            internalSet = { symbolName ->
   //                val views = stage?.views
   //                val newView = library.create(symbolName) as View
   //                this@AnMovieClip.replaceWith(newView)
   //                views?.debugHightlightView(newView)
   //            },
   //            internalGet = { symbol.name ?: "#${symbol.id}" },
   //        ))))
   //        addChild(UiRowEditableValue(app, "gotoAndPlay", UiListEditableValue(app, { stateNames }, ObservableProperty(
   //            name = "gotoAndPlay",
   //            internalSet = { frameName -> this@AnMovieClip.play(frameName) },
   //            internalGet = { timelineRunner.currentStateName ?: "__start" },
   //        ))))
   //        addChild(UiRowEditableValue(app, "gotoAndStop", UiListEditableValue(app, { stateNames }, ObservableProperty(
   //            name = "gotoAndStop",
   //            internalSet = { frameName -> this@AnMovieClip.playAndStop(frameName) },
   //            internalGet = { timelineRunner.currentStateName ?: "__start" },
   //        ))))
   //        button("start").onClick { play() }
   //        button("stop").onClick { stop() }
   //    }
   //    super.buildDebugComponent(views, container)
   //}

    var ratio: Ratio
        get() = timelineRunner.ratio
        set(value) {
            //println("set ratio: $value")
            timelineRunner.ratio = value
            update()
        }

    suspend fun playAndWaitStop(name: String) { playAndWaitEvent(name, setOf()) }

	suspend fun playAndWaitEvent(name: String, vararg events: String): String? = playAndWaitEvent(name, events.toSet())

	suspend fun playAndWaitEvent(name: String, eventsSet: Set<String>): String? {
		return _waitEvent(eventsSet) { play(name) }
	}

	suspend fun waitStop() = _waitEvent(setOf())

	suspend fun waitEvent(vararg events: String) = _waitEvent(events.toSet())
	suspend fun waitEvent(eventsSet: Set<String>) = _waitEvent(eventsSet)

	private suspend fun _waitEvent(eventsSet: Set<String>, afterSignals: () -> Unit = {}): String? {
		val once = Once()
		val deferred = CompletableDeferred<String?>(Job())
		val closeables = arrayListOf<Closeable>()
		//println("Listening($onEvent) : $eventsSet")
		closeables += onStop {
			//println("onStop")
			once { deferred.complete(null) }
		}
		if (eventsSet.isNotEmpty()) {
			closeables += onChangeState {
				//println("onChangeState: $it")
				if (it in eventsSet) {
					//println("completed! $it")
					once { deferred.complete(it) }
				}
			}
			closeables += onEvent {
				//println("onEvent: $it")
				if (it in eventsSet) {
					//println("completed! $it")
					once { deferred.complete(it) }
				}
			}
		}
		try {
			afterSignals()
			return deferred.await()
		} finally {
			closeables.fastForEach { c ->
				c.close()
			}
		}
	}

	/**
	 * Changes the state, seeks a point in between the state and pauses it. Useful for interpolate between points, eg. progressBars.
	 */
	fun seekStill(name: String, ratio: Double = 0.0) {
		val totalTime = timelineRunner.getStateTime(name)
		timelineRunner.gotoAndStop(name, (totalTime * ratio))
		//println("seekStill($name,$ratio) : $currentTime,$running,$currentState")
		update()
	}

	private fun updateInternal(dt: TimeSpan) {
		if (timelineRunner.running && (firstUpdate || !singleFrame)) {
			firstUpdate = false
			timelineRunner.update(dt)
            //println("Updating ${dtMs * 1000}")
			update()
		} else {
            //println("Not updating")
        }
	}

	override fun toString(): String = super.toString() + ":symbol=" + symbol
}

fun View?.play(name: String) { (this as? AnPlayable?)?.play(name) }
suspend fun View?.playAndWaitStop(name: String) { (this as? AnMovieClip?)?.playAndWaitStop(name) }
suspend fun View?.playAndWaitEvent(name: String, vararg events: String) =
	run { (this as? AnMovieClip?)?.playAndWaitEvent(name, *events) }

suspend fun View?.waitStop() { (this as? AnMovieClip?)?.waitStop() }
suspend fun View?.waitEvent(vararg events: String) { (this as? AnMovieClip?)?.waitEvent(*events) }

val View?.playingName: String? get() = (this as? AnMovieClip?)?.timelineRunner?.currentStateName
fun View?.seekStill(name: String, ratio: Double = 0.0) { (this as? AnMovieClip?)?.seekStill(name, ratio) }




/**
 * Draws/buffers a 9-patch image with the texture [tex] at [x], [y] with the total size of [width] and [height].
 * [posCuts] and [texCuts] are [Point] an array of 4 points describing ratios (values between 0 and 1) inside the width/height of the area to be drawn,
 * and the positions inside the texture.
 *
 * The 9-patch looks like this (dividing the image in 9 parts).
 *
 * 0--+-----+--+
 * |  |     |  |
 * |--1-----|--|
 * |  |SSSSS|  |
 * |  |SSSSS|  |
 * |  |SSSSS|  |
 * |--|-----2--|
 * |  |     |  |
 * +--+-----+--3
 *
 * 0: Top-left of the 9-patch
 * 1: Top-left part where scales starts
 * 2: Bottom-right part where scales ends
 * 3: Bottom-right of the 9-patch
 *
 * S: Is the part that is scaled. The other regions are not scaled.
 *
 * It uses the transform [m] matrix, with an optional [filtering] and [colorMul]/[colorAdd], [blendMode] and [program]
 */
private fun BatchBuilder2D.drawNinePatch(
    tex: TextureCoords,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    posCuts: Array<Point>,
    texCuts: Array<Point>,
    m: MMatrix = MMatrix(),
    filtering: Boolean = true,
    colorMul: RGBA = Colors.WHITE,
    blendMode: BlendMode = BlendMode.NORMAL,
    program: Program? = null,
) {
    setStateFast(tex.base, filtering, blendMode, program, icount = 6 * 9, vcount = 4 * 4)
    //val texIndex: Int = currentTexIndex
    val texIndex: Int = 0 // @TODO: Restore currentTexIndex, once available in 4.0.0 final

    val ptt1 = MPoint()
    val ptt2 = MPoint()

    val pt1 = MPoint()
    val pt2 = MPoint()
    val pt3 = MPoint()
    val pt4 = MPoint()
    val pt5 = MPoint()

    val pt6 = MPoint()
    val pt7 = MPoint()
    val pt8 = MPoint()

    val p_o = pt1.setToTransform(m, ptt1.setTo(x, y))
    val p_dU = pt2.setToSub(ptt1.setToTransform(m, ptt1.setTo(x + width, y)), p_o)
    val p_dV = pt3.setToSub(ptt1.setToTransform(m, ptt1.setTo(x, y + height)), p_o)

    val t_o = pt4.setTo(tex.tlX, tex.tlY)
    val t_dU = pt5.setToSub(ptt1.setTo(tex.trX, tex.trY), t_o)
    val t_dV = pt6.setToSub(ptt1.setTo(tex.blX, tex.blY), t_o)

    val start = vertexCount

    for (cy in 0 until 4) {
        val posCutY = posCuts[cy].y
        val texCutY = texCuts[cy].y
        for (cx in 0 until 4) {
            val posCutX = posCuts[cx].x
            val texCutX = texCuts[cx].x

            val p = pt7.setToAdd(
                p_o,
                ptt1.setToAdd(
                    ptt1.setToMul(p_dU, posCutX),
                    ptt2.setToMul(p_dV, posCutY)
                )
            )

            val t = pt8.setToAdd(
                t_o,
                ptt1.setToAdd(
                    ptt1.setToMul(t_dU, texCutX),
                    ptt2.setToMul(t_dV, texCutY)
                )
            )

            addVertex(
                p.x.toFloat(), p.y.toFloat(),
                t.x.toFloat(), t.y.toFloat(),
                colorMul, texIndex
            )
        }
    }

    for (cy in 0 until 3) {
        for (cx in 0 until 3) {
            // v0...v1
            // .    .
            // v2...v3

            val v0 = start + cy * 4 + cx
            val v1 = v0 + 1
            val v2 = v0 + 4
            val v3 = v0 + 5

            addIndex(v0)
            addIndex(v1)
            addIndex(v2)
            addIndex(v2)
            addIndex(v1)
            addIndex(v3)
        }
    }
}

private var RenderContext.stencilIndex: Int by Extra.Property { 0 }
