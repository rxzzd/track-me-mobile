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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun StreamPage() {
    MaterialTheme {
//        var showContent by remember { mutableStateOf(false) }
        val filterInfoBlocsBounds = remember { mutableStateOf<Rect?>(null) }
        var showFilterPopUp by remember { mutableStateOf(false) }


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
            Box(
                modifier = Modifier
                    .fillMaxSize()
//                    .padding(paddingValues)
//                    .padding(top = 31.dp)
                    .background(Color(0xFFF8F3FF))
            )
            {
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
                    Row(
                        modifier = Modifier
                            .width(328.dp),
//                        .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterBtn(
                            modifier = Modifier.width(49.dp),
                            onClick = { showFilterPopUp = true })
                        SearchBar(modifier = Modifier.width(244.dp))
                        AddBtn(modifier = Modifier.width(24.dp))
                    }

                    Spacer(modifier = Modifier.height(31.dp))


                    Row(
                        modifier = Modifier.width(328.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterInfoBlock(
                            "Год(0)",
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                filterInfoBlocsBounds.value = coordinates.boundsInRoot()
                            })
                        FilterInfoBlock("Рынки(0)")
                        FilterInfoBlock("TRL(0)")
                        Spacer(modifier = Modifier.weight(1f))
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
                FilterPopUp(
                    showWindow = showFilterPopUp,
                    onDismiss = { showFilterPopUp = false },
                    anchorBounds = filterInfoBlocsBounds.value,
                    verticalOffset = (-24).dp
                )
            }
        }
    }
}