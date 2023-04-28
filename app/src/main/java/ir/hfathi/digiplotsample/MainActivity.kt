package ir.hfathi.digiplotsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.hfathi.digiplotsample.data.DataPoints.dataPoints2
import ir.hfathi.digiplotsample.sample.LineGraphSample1
import ir.hfathi.digiplotsample.ui.theme.DigiPlotSampleTheme
import ir.hfathi.digiplotsample.ui.theme.md_light_gray

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DigiPlotSampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.background(md_light_gray)) {
                        Card(
                            modifier = Modifier
                                .padding(16.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            LineGraphSample1(lines = listOf(dataPoints2))
                        }
                    }
                }
            }
        }
    }
}
