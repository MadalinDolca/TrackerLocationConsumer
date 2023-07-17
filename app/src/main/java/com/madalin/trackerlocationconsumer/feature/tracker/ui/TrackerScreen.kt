package com.madalin.trackerlocationconsumer.feature.tracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.madalin.trackerlocationconsumer.R
import com.madalin.trackerlocationconsumer.ui.theme.TrackerLocationConsumerTheme

@Composable
fun TrackerScreen(trackerViewModel: TrackerViewModel = viewModel()) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = {  }
        ) {
            Text(text = stringResource(R.string.start_tracking))
        }
    }
}

@Preview
@Composable
fun PreviewTrackerScreen() {
    TrackerLocationConsumerTheme {
        TrackerScreen()
    }
}