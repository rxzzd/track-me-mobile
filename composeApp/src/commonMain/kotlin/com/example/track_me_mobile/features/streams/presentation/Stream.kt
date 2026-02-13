package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
//        var showContent by remember { mutableStateOf(false) }
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                        .background(Color(0xFF8338EB))
                        .padding(start = 18.dp, end = 18.dp)
                ) {
                    //add logo and icon
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(top = 31.dp)
                    .safeContentPadding()
                    .fillMaxSize()
                    .background(Color(0xFFF8F3FF)),
//                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .width(328.dp),
//                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterBtn(modifier = Modifier.width(49.dp))
                    SearchBar(modifier = Modifier.width(244.dp))
                    AddBtn(modifier = Modifier.width(24.dp))
                }

                Spacer(modifier = Modifier.height(31.dp))

                StreamCard(
                    title = "Название потока",
                    markets = "Рынки НТИ:",
                    trl = "TRL:",
                    flow = "Поток:"
                )
                StreamCard(
                    title = "Название потока",
                    markets = "Рынки НТИ:",
                    trl = "TRL:",
                    flow = "Поток:"
                )
                StreamCard(
                    title = "Название потока",
                    markets = "Рынки НТИ:",
                    trl = "TRL:",
                    flow = "Поток:"
                )
                StreamCard(
                    title = "Название потока",
                    markets = "Рынки НТИ:",
                    trl = "TRL:",
                    flow = "Поток:"
                )
            }
        }
    }
}