package com.madalin.trackerlocationconsumer.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.madalin.trackerlocationconsumer.R
import com.madalin.trackerlocationconsumer.feature.tracker.TrackerViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.getViewModel

@Destination
@Composable
fun TrackerScreen(
    trackerViewModel: TrackerViewModel = getViewModel(),
    navigator: DestinationsNavigator
) {
    val viewState by trackerViewModel.viewState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //TrackingButton()
        CoordinatesText(viewState.coordinates)
    }
}

@Composable
fun TrackingButton() {
    Button(
        onClick = { }
    ) {
        Text(text = stringResource(R.string.start_tracking))
    }
}

@Composable
fun CoordinatesText(text: String?) {
    Text(text = text ?: "Coordinates will display here")
}

/*@Preview
@Composable
fun PreviewTrackerScreen() {
    TrackerLocationConsumerTheme {
        TrackerScreen()
    }
}*/