package com.example.track_me_mobile.features.tracker_list.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size // Добавили для размера
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.ic_search


@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    val searchBarColor = Color(0xFFCDAFF7)
    val search = Color(0xFF44069A)

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(1.dp, searchBarColor, RoundedCornerShape(25.dp)),
        placeholder = {
            Text(
                text = "Найти",
                color = Color.White,
                fontWeight = FontWeight.Normal
            )
        },
        leadingIcon = {
            Icon(
                // Используем мультиплатформенный ресурс
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = "Поиск",
                tint = search,
                modifier = Modifier.size(20.dp)
            )
        },
        shape = RoundedCornerShape(25.dp),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = searchBarColor,
            unfocusedContainerColor = searchBarColor,
            disabledContainerColor = searchBarColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}