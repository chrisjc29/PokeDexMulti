package com.unomaster.pokedexgame.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MultipleChoiceItem(
    value: String,
    onClick: (name: String) -> Unit
) {
    val textColor = if (isSystemInDarkTheme()) Color.Black else Color.Black
    val borderColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.DarkGray

    val coroutineScope = rememberCoroutineScope()
    val view = LocalHapticFeedback.current

    val offsetX = remember { Animatable(0f) }

    var selected by remember { mutableStateOf(false) }

    val animateColor by animateColorAsState(
        targetValue = if (selected) Color.Red else Color.Transparent,
        animationSpec = tween(500)
    )

    Card(
        modifier = Modifier
            .padding(12.dp)
            .offset(offsetX.value.dp, 0.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(2.dp, borderColor),
        backgroundColor = animateColor
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    onClick(value)
                    animateShakeMultipleChoiceOption(offsetX, coroutineScope, view)
                    selected = true
                }
                .padding(12.dp)
                .defaultMinSize(120.dp, 60.dp)
        ) {
            Text(
                text = value,
                color = textColor,
                overflow = TextOverflow.Visible,
                textAlign = TextAlign.Center
            )
        }
    }
}

private val shakeKeyframes: AnimationSpec<Float> = keyframes {
    durationMillis = 800
    val easing = FastOutLinearInEasing

    for (i in 1..8) {
        val x = when (i % 3) {
            0 -> 4f
            1 -> -4f
            else -> 0f
        }
        x at durationMillis / 10 * i with easing
    }
}

private fun animateShakeMultipleChoiceOption(
    offset: Animatable<Float, AnimationVector1D>,
    coroutineScope: CoroutineScope,
    view: HapticFeedback,
) {
    coroutineScope.launch {
        offset.animateTo(
            targetValue = 0f,
            animationSpec = shakeKeyframes,
        )
    }
    view.performHapticFeedback(HapticFeedbackType.LongPress)
}