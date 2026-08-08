package com.unomaster.pokedexgame.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.unomaster.pokedexgame.presentation.theme.DeviceShapes
import com.unomaster.pokedexgame.presentation.theme.DexTypography
import com.unomaster.pokedexgame.presentation.theme.Dimens
import com.unomaster.pokedexgame.presentation.theme.LocalAppColors

// Every screen renders through this. It replaces the Material top bar with the dex's own chrome —
// lens, indicator lights, a stamped meta label — over the moulded case that holds the LCD and keys.
// Adding something to the casing happens here once rather than on each screen, which is what keeps
// the fifth screen consistent with the first.
//
// The lens doubles as the back control on screens that can go back. The design has no back arrow,
// and Android's system gesture is not available on iOS, so dropping the affordance entirely would
// strand a player inside Settings. Making the largest, most obvious circle on the casing the way
// out keeps the look exactly as designed and keeps the screen escapable — see AppNavigator.
@Composable
fun DeviceScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    metaLabel: String? = null,
    metaContentDescription: String? = null,
    onMetaClick: (() -> Unit)? = null,
    lens: DeviceLensState = DeviceLensState.Idle,
    lights: DeviceLights? = DeviceLights.Idle,
    compact: Boolean = false,
    caseGap: Dp = Dimens.DeviceCaseGap,
    onNavigateBack: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalAppColors.current

    Scaffold(
        modifier = modifier,
        containerColor = colors.deviceShell,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            DeviceChrome(
                title = title,
                metaLabel = metaLabel,
                metaContentDescription = metaContentDescription,
                onMetaClick = onMetaClick,
                lens = lens,
                lights = lights,
                compact = compact,
                onNavigateBack = onNavigateBack,
            )
            DeviceCase(compact = compact, gap = caseGap, content = content)
        }
    }
}

@Composable
private fun DeviceChrome(
    title: String?,
    metaLabel: String?,
    metaContentDescription: String?,
    onMetaClick: (() -> Unit)?,
    lens: DeviceLensState,
    lights: DeviceLights?,
    compact: Boolean,
    onNavigateBack: (() -> Unit)?,
) {
    val horizontal = if (compact) {
        Dimens.DeviceChromePaddingHorizontalCompact
    } else {
        Dimens.DeviceChromePaddingHorizontal
    }
    val bottom = if (compact) {
        Dimens.DeviceChromePaddingBottomCompact
    } else {
        Dimens.DeviceChromePaddingBottom
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = horizontal,
                end = horizontal,
                top = Dimens.DeviceChromePaddingTop,
                bottom = bottom,
            ),
        horizontalArrangement = Arrangement.spacedBy(Dimens.DeviceCaseGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DeviceLens(
            state = lens,
            compact = compact,
            onClick = onNavigateBack,
            contentDescription = "Back",
        )

        if (title != null) {
            Text(
                text = title,
                style = DexTypography.ChromeTitle,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (lights != null) {
            DeviceLeds(lights = lights, compact = compact)
        }

        if (metaLabel != null) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = metaLabel,
                style = DexTypography.HeaderMeta,
                color = Color.White.copy(alpha = CHROME_META_ALPHA),
                maxLines = 1,
                // The label is the tap target on Home, so it carries the 48dp minimum. sizeIn grows
                // the box; wrapContentSize keeps the text pinned to the casing's right edge inside
                // it rather than letting it drift to the middle.
                modifier = if (onMetaClick != null) {
                    Modifier
                        .sizeIn(
                            minWidth = Dimens.MinimumTouchTarget,
                            minHeight = Dimens.MinimumTouchTarget,
                        )
                        .clickable(onClickLabel = metaContentDescription, onClick = onMetaClick)
                        .wrapContentSize(Alignment.CenterEnd)
                } else {
                    Modifier
                },
            )
        }
    }
}

// The inset panel the LCD and keys are set into. It fills whatever the chrome leaves, so a screen
// never has to know how tall the casing is.
//
// The corners come from background(colour, shape) and the inner shadow from a clipped overlay that
// holds nothing tappable. Clipping the case itself would round the corners just as well and stop
// every key inside it registering a tap — see the note at the top of DeviceScreen.kt.
@Composable
private fun ColumnScope.DeviceCase(
    compact: Boolean,
    gap: Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalAppColors.current
    val margin = if (compact) Dimens.DeviceCaseMarginCompact else Dimens.DeviceCaseMargin
    val padding = if (compact) Dimens.DeviceCasePaddingCompact else Dimens.DeviceCasePadding
    val shape = if (compact) DeviceShapes.CaseCompact else DeviceShapes.Case

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(start = margin, end = margin, bottom = margin)
            .background(colors.deviceCase, shape),
    ) {
        // Standing in for `inset 0 2px 6px rgba(0,0,0,.35)`: Compose has no inner shadow, and a
        // short gradient down from the top lip reads the same at this size.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = CASE_SHADOW_ALPHA),
                                Color.Transparent,
                            ),
                            startY = 0f,
                            endY = Dimens.SpacingSmall.toPx(),
                        ),
                    )
                },
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(gap),
            content = content,
        )
    }
}

private const val CHROME_META_ALPHA = 0.85f
private const val CASE_SHADOW_ALPHA = 0.35f
