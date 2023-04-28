package ir.hfathi.digiplotsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.hfathi.digiplot.line.DataPoint
import ir.hfathi.digiplotsample.DataPoints.dataPoints2
import ir.hfathi.digiplotsample.ui.theme.DigiPlotSampleTheme
import ir.hfathi.digiplotsample.ui.theme.md_blue_gray

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DigiPlotSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        LineGraphSample1(listOf(dataPoints2))
                    }
                }
            }
        }
    }
}


@Composable
private fun LineGraphSample1(lines: List<List<DataPoint>>){
    Card(
        modifier = Modifier.padding(16.dp).background(md_blue_gray),
        shape = MaterialTheme.shapes.medium,
    ) {
        LineGraphSample(lines)
    }
}
@Composable
@Preview
fun LineGraphSample1Preview(){
    LineGraphSample1(listOf(dataPoints2))
}
