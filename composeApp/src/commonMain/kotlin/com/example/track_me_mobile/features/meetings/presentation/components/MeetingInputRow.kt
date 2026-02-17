package com.example.track_me_mobile.features.meetings.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.TrackMePurple

@Composable
fun MeetingInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEnabled: Boolean,
    isMultiline: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }

    LaunchedEffect(value) {
        if (textFieldValueState.text != value) {
            textFieldValueState = textFieldValueState.copy(text = value)
        }
    }

    LaunchedEffect(isEnabled) {
        if (isEnabled) {
            textFieldValueState = textFieldValueState.copy(
                selection = TextRange(textFieldValueState.text.length)
            )
            focusRequester.requestFocus()
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, color = Color.Gray, fontSize = 13.sp)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .then(if (isMultiline) Modifier.height(110.dp) else Modifier.height(42.dp)),
            shape = RoundedCornerShape(15.dp),
            border = BorderStroke(1.dp, TrackMePurple),
            color = Color.White
        ) {
            BasicTextField(
                value = textFieldValueState,
                onValueChange = {
                    textFieldValueState = it
                    onValueChange(it.text)
                },
                enabled = isEnabled,
                modifier = Modifier.fillMaxSize().padding(12.dp).focusRequester(focusRequester),
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black)
            )
        }
    }
}