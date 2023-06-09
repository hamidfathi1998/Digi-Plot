package ir.hfathi.digiplotsample.sample

import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import ir.hfathi.digiplot.line.DataPoint
import ir.hfathi.digiplot.line.LineGraph
import ir.hfathi.digiplot.line.LinePlot
import ir.hfathi.digiplotsample.data.DataPoints
import ir.hfathi.digiplotsample.ui.theme.DigiPlotSampleTheme
import ir.hfathi.digiplotsample.ui.theme.md_blue_gray
import ir.hfathi.digiplotsample.ui.theme.md_green
import java.text.DecimalFormat


@Composable
internal fun LineGraphSample1(modifier: Modifier = Modifier, lines: List<List<DataPoint>>) {
    val totalWidth = remember { mutableStateOf(0) }
    val density = LocalDensity.current
    Column(modifier = modifier.onGloballyPositioned {
        totalWidth.value = it.size.width
    }.background(color = Color.Transparent)) {
        val xOffset = remember { mutableStateOf(0f) }
        val cardWidth = remember { mutableStateOf(0) }
        val visibility = remember { mutableStateOf(false) }
        val points = remember { mutableStateOf(listOf<DataPoint>()) }

        Box(
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var value = points.value

                    val lastValue = lines.last()
                    if (value.isEmpty()) {
                        value = lastValue
                    }
                    val diffPrice = value[0].y - lastValue[0].y
                    val diffPriceSign = if (diffPrice < 0) "-" else String()
                    val diffPresent = (lastValue[0].y * 100) / value[0].y
                    val diffPresentValue = diffPresent - 100
                    val time = DecimalFormat("#.#").format(value[0].x)
                    Text(
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                        text = "$${value[0].y}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 8.dp),
                            text = "$diffPriceSign$$diffPrice",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )
                        Text(
                            text = "${diffPresentValue.toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Red
                        )
                    }
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            textAlign = TextAlign.Center,
                            text = "${time.toInt()}:00",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(all = 4.dp)
                                .onGloballyPositioned {
                                    cardWidth.value = it.size.width
                                }
                                .graphicsLayer(translationX = xOffset.value)
                                .alpha(if (visibility.value) 1f else 0f),
                        )
                    }
                }
            }
        }

        LineGraph(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            plot = LinePlot(
                horizontalExtraSpace = 20.dp,
                paddingTop = 50.dp,
                paddingBottom = 120.dp,
                isZoomAllowed = false,
                lines = getLinesDetails(lines = lines, density = density),
                selection = LinePlot.Selection(
                    enabled = true,
                    highlight = LinePlot.Connection(
                        color = md_green,
                        strokeWidth = 2.dp,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f, 20f), 0f),
                    ),
                    detectionTime = 100L,
                ),

                ),

            onSelectionStart = { visibility.value = true },
            onSelectionEnd = { visibility.value = false },

            ) { x, pts ->
            val cWidth = cardWidth.value.toFloat()
            var xCenter = x
            xCenter = when {
                xCenter + cWidth / 2f > totalWidth.value -> totalWidth.value - cWidth
                xCenter - cWidth / 2f < 0f -> 0f
                else -> xCenter - cWidth / 2f
            }
            xOffset.value = xCenter - (xCenter * 0.1f)
            points.value = pts
        }
    }
}

private fun getLinesDetails(lines: List<List<DataPoint>>, density: Density): List<LinePlot.Line> {
    val result = mutableListOf<LinePlot.Line>()
    lines.forEach { line ->
        result.add(
            LinePlot.Line(
                lineShadowAlpha = 0.2f,
                dataPoints = line,
                connection = LinePlot.Connection(
                    color = if (line.first().y != 5f) md_green else md_blue_gray,
                    strokeWidth = 2.dp,
                ),
                intersection = null,
                maxMinLabel = LinePlot.MaxMinLabel(
                    paint = Paint().apply {
                        color = Color.Gray.toInt()
                        textAlign = Paint.Align.LEFT
                        textSize = density.run { 14.sp.toPx() }
                    },
                    maxLabelXY = Pair(10f, 50f),
                    minLabelXY = Pair(10f, 60f)
                ),
                highlight = LinePlot.Highlight(
                    color = md_green,
                    borderColor = Color.White,
                    borderEnable = true
                ),
            )
        )
    }
    return result
}

@Preview(showBackground = true)
@Composable
fun LineGraphPreview() {
    DigiPlotSampleTheme {
        LineGraphSample1(lines = listOf(DataPoints.dataPoints1, DataPoints.dataPoints2))
    }
}


fun Color.toInt(): Int {
    return android.graphics.Color.argb(
        this.toArgb().alpha,
        this.toArgb().red,
        this.toArgb().green,
        this.toArgb().blue
    )
}