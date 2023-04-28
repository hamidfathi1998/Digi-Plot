package ir.hfathi.digiplot.line

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ir.hfathi.digiplot.detectDragZoomGesture
import kotlin.math.ceil


/**
 * A composable that draws a Line graph with the configurations provided by the [LinePlot]. The graph
 * can be scrolled, zoomed and touch dragged for selection. Every part of the line graph can be customized,
 * by changing the configuration in the [LinePlot].
 *
 * @param plot the configuration to render the full graph
 * @param modifier Modifier
 * @param onSelectionStart invoked when the selection has started
 * @param onSelectionEnd invoked when the selection has ended
 * @param onSelection invoked when selection changes from one point to the next. You are provided
 * with the xOffset where the selection occurred in the graph and the [DataPoint]s that are selected. If there
 * are multiple lines, you will get multiple data points.
 */

@Composable
fun LineGraph(
    plot: LinePlot,
    modifier: Modifier = Modifier,
    onSelectionStart: () -> Unit = {},
    onSelectionEnd: () -> Unit = {},
    onSelection: ((Float, List<DataPoint>) -> Unit)? = null
) {
    val paddingTop = plot.paddingTop
    val paddingRight = plot.paddingRight
    val paddingBottom = plot.paddingBottom
    val horizontalGap = plot.horizontalExtraSpace
    val isZoomAllowed = plot.isZoomAllowed

    val globalXScale = 0.92f
    val globalYScale = 1f

    val offset = remember { mutableStateOf(0f) }
    val maxScrollOffset = remember { mutableStateOf(0f) }
    val dragOffset = remember { mutableStateOf(0f) }
    val isDragging = remember { mutableStateOf(false) }
    val bgColor = MaterialTheme.colors.surface
    val allDataPoints = mutableListOf<Pair<Offset, Offset>>()
    val lines = plot.lines
    val xUnit = plot.xAxis.unit

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
    ) {
        Box(
            modifier = modifier.clipToBounds(),
        ) {
            val points = lines.flatMap { it.dataPoints }
            val (xMin, xMax, xAxisScale) = getXAxisScale(points, plot)
            val (yMin, yMax, yAxisScale) = getYAxisScale(points, plot)
            var maxMinIndexes: Pair<Int, Int> = Pair(0, 0)

            Canvas(modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .pointerInput(Unit, Unit) {
                    detectDragZoomGesture(
                        isZoomAllowed = isZoomAllowed,
                        isDragAllowed = plot.selection.enabled,
                        detectDragTimeOut = plot.selection.detectionTime,
                        onDragStart = {
                            dragOffset.value = it.x
                            onSelectionStart()
                            isDragging.value = true
                        }, onDragEnd = {
                            isDragging.value = false
                            onSelectionEnd()
                        }, onZoom = { }

                    ) { change, _ ->
                        dragOffset.value = change.position.x
                    }
                },
                onDraw = {
                    val xLeft = horizontalGap.toPx()
                    val yBottom = size.height - paddingBottom.value
                    val maxElementInYAxis =
                        getMaxElementInYAxis(yAxisScale, plot.yAxis.steps)
                    val yOffset = ((yBottom - paddingTop.toPx()) / maxElementInYAxis) * globalYScale
                    val xLastPointWithOutXOffset = (xMax - xMin) * (1 / xUnit)
                    val xOffset = size.width.div(xLastPointWithOutXOffset) * globalXScale
                    val xLastPoint =
                        xLastPointWithOutXOffset * xOffset + xLeft + paddingRight.toPx() + horizontalGap.toPx()
                    maxScrollOffset.value = if (xLastPoint > size.width) {
                        xLastPoint - size.width
                    } else 0f
                    offset.value = maxScrollOffset.value - (size.width / 54)
                    val dragLocks = mutableMapOf<LinePlot.Line, Pair<DataPoint, Offset>>()

                    // Draw Grid lines
                    val top = yBottom - ((yMax - yMin) * yOffset)
                    val region =
                        Rect(xLeft, top, size.width - paddingRight.toPx(), yBottom)
                    plot.grid?.draw?.invoke(this, region, xOffset * (1 / xUnit), yOffset)

                    // Draw Lines and Points and AreaUnderLine
                    lines.forEach { line ->
                        val intersection = line.intersection
                        val connection = line.connection
                        val areaUnderLine = line.areaUnderLine
                        val maxMinLabel = line.maxMinLabel
                        maxMinLabel?.let {
                            maxMinIndexes = getMaxMinIndexes(points)
                        }
                        // Draw area under curve
                        if (areaUnderLine != null) {
                            val pts = line.dataPoints.map { (x, y) ->
                                val x1 = ((x - xMin) * xOffset * (1 / xUnit)) + xLeft - offset.value
                                val y1 = yBottom - ((y - yMin) * yOffset)
                                Offset(x1, y1)
                            }
                            val p = androidx.compose.ui.graphics.Path()
                            pts.forEachIndexed { index, offset ->
                                if (index == 0) {
                                    p.moveTo(offset.x, yBottom)
                                }
                                p.lineTo(offset.x, offset.y)
                            }
                            val last = pts.last()
                            val first = pts.first()
                            p.lineTo(last.x, yBottom)
                            p.lineTo(first.x, yBottom)
                            areaUnderLine.draw(this, p)
                        }

                        // Draw Lines and Points

                        var curOffset: Offset? = null
                        var nextOffset: Offset? = null
                        var beforeOffset: Offset? = null


                        line.dataPoints.forEachIndexed { i, dataModel ->
                            if (i == 0) {
                                val (x, y) = line.dataPoints[i]
                                val x1 = ((x - xMin) * xOffset * (1 / xUnit)) + xLeft - offset.value
                                val y1 = yBottom - ((y - yMin) * yOffset)
                                curOffset = Offset(x1, y1)
                            }
                            if (line.dataPoints.indices.contains(i + 1)) {
                                val (x, y) = line.dataPoints[i + 1]
                                val x2 = ((x - xMin) * xOffset * (1 / xUnit)) + xLeft - offset.value
                                val y2 = yBottom - ((y - yMin) * yOffset)
                                nextOffset = Offset(x2, y2)
                            }
                            if (nextOffset != null && curOffset != null) {

                                if (isDragging.value.not()) {
                                    connection?.draw?.invoke(
                                        this,
                                        curOffset!!,
                                        nextOffset!!,
                                        1f
                                    )
                                } else {
                                    val x = dragLocks.values.firstOrNull()?.second?.x

                                    if (x == null && beforeOffset != null) {
                                        connection?.draw?.invoke(
                                            this,
                                            beforeOffset!!,
                                            curOffset!!,
                                            1f
                                        )
                                    }
                                }

                                connection?.draw?.invoke(
                                    this,
                                    curOffset!!,
                                    nextOffset!!,
                                    line.lineShadowAlpha
                                )

                                allDataPoints.add(Pair(curOffset!!, nextOffset!!))
                            }
                            curOffset?.let { curOffsetValue ->
                                if (isDragging.value && isDragLocked(
                                        dragOffset.value,
                                        curOffsetValue,
                                        xOffset
                                    )
                                ) {
                                    dragLocks[line] = line.dataPoints[i] to curOffsetValue
                                } else {
                                    intersection?.draw?.invoke(
                                        this,
                                        curOffsetValue,
                                        line.dataPoints[i]
                                    )
                                }
                            }

                            // draw high label
                            maxMinLabel?.let { maxMinLabelModel ->
                                if (isDragging.value.not()) {
                                    if (i == maxMinIndexes.first) {
                                        val displayCurrentXValue = "$${dataModel.y}"
                                        maxMinLabelModel.draw.invoke(
                                            this,
                                            displayCurrentXValue,
                                            getMaximumXTextOffset(
                                                textWidth = maxMinLabelModel.paint.measureText(
                                                    displayCurrentXValue
                                                ),
                                                horizontalGap = horizontalGap.toPx(),
                                                sizeWidth = size.width,
                                                currentItemX = curOffset!!.x - maxMinLabelModel.maxLabelXY.first
                                            ),
                                            curOffset!!.y - maxMinLabelModel.maxLabelXY.second
                                        )
                                    }

                                    if (i == maxMinIndexes.second) {
                                        val displayCurrentXValue = "$${dataModel.y}"
                                        maxMinLabelModel.draw.invoke(
                                            this,
                                            displayCurrentXValue,
                                            getMinimumXTextOffset(
                                                sizeWidth = size.width,
                                                textWidth = maxMinLabelModel.paint.measureText(
                                                    displayCurrentXValue
                                                ),
                                                horizontalGap = horizontalGap.toPx(),
                                                currentItemX = curOffset!!.x - maxMinLabelModel.minLabelXY.first
                                            ),
                                            curOffset!!.y + maxMinLabelModel.minLabelXY.second
                                        )
                                    }
                                }
                            }
                            beforeOffset = curOffset
                            curOffset = nextOffset
                        }
                    }

                    // Draw column
                    drawRect(
                        color = bgColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(0f, size.height),
                    )

                    // Draw right padding
                    drawRect(
                        color = bgColor,
                        topLeft = Offset(size.width - paddingRight.toPx(), 0f),
                        size = Size(paddingRight.toPx(), size.height)
                    )

                    // Draw drag selection Highlight
                    if (isDragging.value) {
                        // Draw Drag Line highlight
                        dragLocks.values.firstOrNull()?.let { (dataPoint, location) ->

                            val (x, _) = location
                            val con = plot.lines[0].connection!!
                            if (x >= 0 && x <= size.width - paddingRight.toPx()) {
                                plot.selection.highlight?.draw?.invoke(
                                    this,
                                    Offset(x, yBottom),
                                    Offset(x, 0f),
                                    1f
                                )

                                allDataPoints.forEachIndexed { index, pair ->
                                    if (index.toFloat() < dataPoint.x) {
                                        con.draw.invoke(
                                            this,
                                            pair.first,
                                            pair.second,
                                            1f
                                        )
                                    }
                                }
                            } else {
                                allDataPoints.forEach { pair ->
                                    con.draw.invoke(
                                        this,
                                        pair.first,
                                        pair.second,
                                        1f
                                    )
                                }
                            }
                        }
                        // Draw Point Highlight
                        dragLocks.entries.forEach { (line, lock) ->
                            val highlight = line.highlight
                            line.dataPoints
                            val location = lock.second
                            val x = location.x
                            if (x >= 0 && x <= size.width - paddingRight.toPx()) {
                                highlight?.draw?.invoke(this, location)
                            }
                        }
                    }

                    // OnSelection
                    if (isDragging.value) {
                        val x = dragLocks.values.firstOrNull()?.second?.x

                        if (x != null) {
                            onSelection?.invoke(x, dragLocks.values.map { it.first })
                        }
                    }
                })
        }
    }
}


fun getMinimumXTextOffset(
    textWidth: Float,
    sizeWidth: Float,
    currentItemX: Float,
    horizontalGap: Float
): Float {
    var xCenter = currentItemX + horizontalGap
    xCenter = when {
        xCenter + textWidth / 2f > sizeWidth -> sizeWidth - textWidth
        xCenter - textWidth / 2f < 0f -> 0f
        else -> xCenter - textWidth / 2f
    }
    return xCenter
}

fun getMaximumXTextOffset(
    textWidth: Float,
    sizeWidth: Float,
    currentItemX: Float,
    horizontalGap: Float
): Float {
    var xCenter = currentItemX + horizontalGap
    xCenter = when {
        xCenter + textWidth / 2f > sizeWidth -> sizeWidth - textWidth
        xCenter - textWidth / 2f < 0f -> 0f
        else -> xCenter - textWidth / 2f
    }
    return xCenter
}

fun getMaxMinIndexes(dataPoints: List<DataPoint>): Pair<Int, Int> {
    val maxValue = dataPoints.indexOf(dataPoints.maxByOrNull { it.y })
    val minValue = dataPoints.indexOf(dataPoints.minByOrNull { it.y })
    return Pair(maxValue, minValue)
}

private fun isDragLocked(dragOffset: Float, it: Offset, xOffset: Float) =
    ((dragOffset) > it.x - xOffset / 2) && ((dragOffset) < it.x + xOffset / 2)

private fun getXAxisScale(
    points: List<DataPoint>,
    plot: LinePlot
): Triple<Float, Float, Float> {
    val xMin = points.minOf { it.x }
    val xMax = points.maxOf { it.x }
    val totalSteps =
        (xMax - xMin) + 1
    val temp = totalSteps / plot.xAxis.steps
    val scale = if (plot.xAxis.roundToInt) ceil(temp) else temp
    return Triple(xMin, xMax, scale)
}

private fun getYAxisScale(
    points: List<DataPoint>,
    plot: LinePlot
): Triple<Float, Float, Float> {
    val steps = plot.yAxis.steps
    val yMin = points.minOf { it.y }
    val yMax = points.maxOf { it.y }

    val totalSteps = (yMax - yMin)
    val temp = totalSteps / if (steps > 1) (steps - 1) else 1

    val scale = if (plot.yAxis.roundToInt) ceil(temp) else temp
    return Triple(yMin, yMax, scale)
}

private fun getMaxElementInYAxis(offset: Float, steps: Int): Float {
    return (if (steps > 1) steps - 1 else 1) * offset
}