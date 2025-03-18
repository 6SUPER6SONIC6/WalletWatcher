package com.supersonic.walletwatcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun TextFieldWithActionButton(
    modifier: Modifier = Modifier,
    title: String? = null,
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: @Composable (() -> Unit)? = null,
    actionIcon: @Composable () -> Unit,
    actionContainerColor: Color = ButtonDefaults.buttonColors().containerColor,
    imeAction: ImeAction = ImeAction.Done,
    keyboardType: KeyboardType = KeyboardType.Text,
    onActionClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier) {
        if (title != null) {
            Text(
                text = title,
                style = typography.labelLarge,
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                singleLine = true,
                isError = isError,
                placeholder =
                if (placeholder != null) {
                    {
                        Text(
                            text = placeholder,
                            color = colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else null,
                trailingIcon = {
                    AnimatedVisibility(
                        visible = text.isNotEmpty(),
                        enter = scaleIn() + fadeIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = { onTextChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear, contentDescription = "Clear text"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = imeAction,
                    keyboardType = keyboardType
                ),
                keyboardActions = KeyboardActions(
                    onAny = {
                        onActionClick()
                        keyboardController?.hide()
                    }
                ),
                modifier = Modifier.weight(1F)
            )

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = actionContainerColor),
                modifier = Modifier.size(56.dp)
            ) {
                actionIcon()
            }
        }
        if (errorMessage != null) {
            errorMessage()
        }
    }
}