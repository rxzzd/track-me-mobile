package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AddStreamPage() {
    MaterialTheme {
        Scaffold(topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .background(Color(0xFF8338EB))
                    .padding(start = 18.dp, end = 18.dp)
            ) {
                //add logo and icon
            }
        }) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
//                    .padding(paddingValues)
//                    .padding(top = 31.dp)
                    .background(Color(0xFFF8F3FF))
            ) {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
//                        .padding(top = 31.dp)
                        .safeContentPadding()
                        .fillMaxSize()
                        .background(Color(0xFFF8F3FF))
                        .verticalScroll(rememberScrollState()),
//                    .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AddStreamPhoto()
                    Spacer(modifier = Modifier.height(20.dp))
                    AddStreamMarket()
                }
            }
        }
    }
}