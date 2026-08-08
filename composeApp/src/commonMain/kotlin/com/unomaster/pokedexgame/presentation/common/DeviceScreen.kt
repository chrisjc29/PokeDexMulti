package com.unomaster.pokedexgame.presentation.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.unomaster.pokedexgame.presentation.theme.DeviceShapes
import com.unomaster.pokedexgame.presentation.theme.DexTypography
import com.unomaster.pokedexgame.presentation.theme.Dimens
import com.unomaster.pokedexgame.presentation.theme.LocalAppColors
import com.unomaster.pokedexgame.presentation.theme.Motion

// The parts the Pokedex device is assembled from: the LCD panel, the lights above it and the keys
// below it. Every screen builds from these rather than from Material's Surface/Button, because the
// look is a physical object — a raised key has a 4dp moulded lip, not an elevation shadow — and none
// of Material's components have a slot for that.
//
// Nothing here knows about the game. Colours come from AppColors, sizes from Dimens: restyling the
// device is a change to those two files, not to this one.
//
// One rule holds throughout, and it is not cosmetic: a rounded shape is drawn with
// `background(colour, shape)`, never with `clip(shape)` on a container that has children inside its
// padding. Under clip, pointer input stops reaching those children — the component tests catch it as
// keys that render perfectly and do nothing when tapped. Clipping is only ever applied to the
// decoration layers below, which hold no interactive content.

/**
 * The LCD panel: pale green face, dark bezel, and the scanline texture that sells it as a screen
 * rather than a card.
 *
 * [scanning] runs the green sweep down the panel on a loop — on only while a round is live, so a
 * solved or errored screen is visibly *not* working. [glow] lights the warm halo behind a revealed
 * Pokemon.
 */
@Composable
fun DeviceScreenPanel(
    modifier: Modifier = Modifier,
    scanning: Boolean = false,
    glow: Boolean = false,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .background(colors.deviceScreen, DeviceShapes.Screen)
            .border(Dimens.DeviceScreenBorder, colors.deviceScreenInk, DeviceShapes.Screen),
        contentAlignment = contentAlignment,
    ) {
        // Everything that has to stop at the bezel goes in one of these two overlays: they are
        // clipped, and they contain nothing tappable.
        if (glow || scanning) {
            Box(modifier = Modifier.matchParentSize().clip(DeviceShapes.Screen)) {
                if (glow) RevealGlow(modifier = Modifier.align(Alignment.Center))
                if (scanning) ScanBar()
            }
        }

        content()

        // Drawn last, so the ruling sits over the artwork the way it would on a real panel.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(DeviceShapes.Screen)
                .scanlines(colors.deviceScreenInk),
        )
    }
}

private fun Modifier.scanlines(ink: Color): Modifier = drawBehind {
    val lineHeight = Dimens.DeviceScanlineHeight.toPx()
    val period = lineHeight * 2
    var y = 0f
    while (y < size.height) {
        drawRect(
            color = ink.copy(alpha = SCANLINE_ALPHA),
            topLeft = Offset(0f, y),
            size = Size(size.width, lineHeight),
        )
        y += period
    }
}

// A soft band travelling top to bottom, transparent at its leading edge and strongest at its
// trailing one, so it reads as a beam with a wake rather than a moving rectangle.
@Composable
private fun BoxScope.ScanBar() {
    val colors = LocalAppColors.current
    val transition = rememberInfiniteTransition(label = "scan")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(Motion.DurationScanSweep, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scanProgress",
    )

    Box(
        modifier = Modifier
            .matchParentSize()
            .drawBehind {
                val barHeight = Dimens.DeviceScanBarHeight.toPx()
                // Travels the full height plus its own height, so it enters and leaves cleanly.
                val top = progress * (size.height + barHeight) - barHeight
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.deviceScan.copy(alpha = 0f),
                            colors.deviceScan.copy(alpha = SCAN_BAR_ALPHA),
                        ),
                        startY = top,
                        endY = top + barHeight,
                    ),
                    topLeft = Offset(0f, top),
                    size = Size(size.width, barHeight),
                )
            },
    )
}

/** The warm halo behind a revealed Pokemon. */
@Composable
private fun RevealGlow(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .size(Dimens.RevealGlowSize)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to colors.revealFill.copy(alpha = REVEAL_GLOW_ALPHA),
                            REVEAL_GLOW_EDGE to Color.Transparent,
                            1f to Color.Transparent,
                        ),
                        center = center,
                        radius = size.minDimension / 2,
                    ),
                    radius = size.minDimension / 2,
                )
            },
    )
}

/** The big round lens on the casing. Tappable when [onClick] is given — see DeviceScaffold. */
@Composable
fun DeviceLens(
    state: DeviceLensState,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
) {
    val colors = LocalAppColors.current
    val size = if (compact) Dimens.DeviceLensSizeCompact else Dimens.DeviceLensSize
    val ring = if (compact) Dimens.DeviceLensRingCompact else Dimens.DeviceLensRing
    val face = when (state) {
        DeviceLensState.Idle -> colors.deviceLens
        DeviceLensState.Active -> colors.deviceLensActive
        DeviceLensState.Offline -> colors.deviceLensOffline
    }

    Box(
        modifier = modifier
            .size(size)
            .glow(
                color = colors.deviceLens,
                visible = state == DeviceLensState.Active,
                alpha = LENS_GLOW_ALPHA,
            )
            .background(face, CircleShape)
            .border(ring, Color.White, CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClickLabel = contentDescription, onClick = onClick)
                } else {
                    Modifier
                },
            ),
    ) {
        // The 2dp inner ring: a lens is glass in a socket, and the socket casts a line.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(ring)
                .border(Dimens.DeviceLensInnerRing, Color.Black.copy(alpha = 0.35f), CircleShape),
        )
    }
}

enum class DeviceLensState { Idle, Active, Offline }

/**
 * The three indicator lights. Passed as one composable rather than three so a screen states its
 * situation ([DeviceLights]) and the chrome decides which lights that means.
 */
@Composable
fun DeviceLeds(
    lights: DeviceLights,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = LocalAppColors.current
    val gap = if (compact) Dimens.DeviceLedGapCompact else Dimens.DeviceLedGap

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        when (lights) {
            DeviceLights.Idle -> {
                DeviceLed(colors.ledRedIdle, lit = false, compact = compact)
                DeviceLed(colors.ledAmberIdle, lit = false, compact = compact)
                DeviceLed(colors.ledGreenIdle, lit = false, compact = compact)
            }

            DeviceLights.Alert -> {
                DeviceLed(colors.ledRedOn, lit = true, compact = compact)
                DeviceLed(colors.ledAmber, lit = false, compact = compact)
                DeviceLed(colors.ledGreen, lit = false, compact = compact)
            }

            DeviceLights.Confirmed -> {
                DeviceLed(colors.ledRed, lit = false, compact = compact)
                DeviceLed(colors.ledAmber, lit = false, compact = compact)
                DeviceLed(colors.ledGreenOn, lit = true, compact = compact)
            }
        }
    }
}

/** What the dex is currently saying, in the only language three lights have. */
enum class DeviceLights { Idle, Alert, Confirmed }

@Composable
private fun DeviceLed(
    color: Color,
    lit: Boolean,
    compact: Boolean,
) {
    val size = if (compact) Dimens.DeviceLedSizeCompact else Dimens.DeviceLedSize

    Box(
        modifier = Modifier
            .size(size)
            .glow(color = color, visible = lit, alpha = LED_GLOW_ALPHA)
            .background(color, CircleShape)
            .border(Dimens.DeviceLedRim, Color.Black.copy(alpha = 0.4f), CircleShape),
    )
}

// Compose has no blurred box-shadow, so a lit indicator gets a radial gradient painted behind it
// instead. Cheaper than a real blur and, at this size, indistinguishable from one.
private fun Modifier.glow(color: Color, visible: Boolean, alpha: Float): Modifier =
    if (!visible) this else drawBehind {
        val radius = size.minDimension / 2 + Dimens.DeviceGlowSpread.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to color.copy(alpha = alpha),
                    GLOW_CORE_EDGE to color.copy(alpha = alpha * GLOW_FALLOFF),
                    1f to Color.Transparent,
                ),
                center = center,
                radius = radius,
            ),
            radius = radius,
        )
    }

/**
 * A raised key: dark face over a moulded [Dimens.DeviceKeyEdge] lip. [pressed] sinks it instead of
 * recolouring it, which is how a ruled-out answer reads as dead without moving in the grid — the lip
 * takes the face colour so the key keeps its exact height and baseline.
 */
@Composable
fun DeviceKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pressed: Boolean = false,
    enabled: Boolean = true,
    height: Dp = Dimens.DeviceKeyHeight,
    edge: Dp = Dimens.DeviceKeyEdge,
    shape: Shape = DeviceShapes.Key,
    textStyle: TextStyle = DexTypography.KeyLabel,
    strikeThrough: Boolean = false,
) {
    val colors = LocalAppColors.current
    val face = if (pressed) colors.deviceKeyPressed else colors.deviceScreenInk

    Box(
        modifier = modifier
            .heightIn(min = height)
            // The lip is a real band of colour under the face, not a shadow: it stays crisp at any
            // density, and it is what makes the key look moulded rather than floating.
            .background(if (pressed) face else colors.deviceKeyEdge, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(bottom = edge)
            .background(face, shape)
            .then(
                if (pressed) Modifier.pressedFace(shape, colors.deviceKeyLabel) else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = textStyle,
            color = if (pressed) colors.wrongLabel else colors.deviceKeyLabel,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (strikeThrough) TextDecoration.LineThrough else null,
            modifier = Modifier.padding(horizontal = Dimens.SpacingSmall),
        )
    }
}

// A pressed-in key is lit from above by nothing: a short dark gradient from the top lip stands in
// for the inset shadow, and a faint rim picks out the edge of the recess.
private fun Modifier.pressedFace(shape: Shape, rim: Color): Modifier = this
    .drawBehind {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent),
                startY = 0f,
                endY = Dimens.SpacingSmall.toPx(),
            ),
        )
    }
    .border(Dimens.DeviceKeyRim, rim.copy(alpha = PRESSED_RIM_ALPHA), shape)

/** The single main action on a screen: `START GAME`, `NEXT SPECIMEN`, `RETRY`. */
@Composable
fun DevicePrimaryKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = Dimens.DevicePrimaryKeyHeight,
) {
    DeviceKey(
        label = label,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        height = height,
        edge = Dimens.DevicePrimaryKeyEdge,
        shape = DeviceShapes.PrimaryKey,
        textStyle = DexTypography.PrimaryKeyLabel,
    )
}

/**
 * The quieter key beside or below a primary one: `END`, `DISMISS`. Cut into the casing rather than
 * raised out of it, so there is never a question about which key the screen wants pressed.
 */
@Composable
fun DeviceOutlinedKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = Dimens.DeviceDismissKeyHeight,
    textStyle: TextStyle = DexTypography.OutlinedKeyLabel,
) {
    Box(
        modifier = modifier
            .heightIn(min = height)
            .border(
                width = Dimens.DeviceOutlinedKeyBorder,
                color = Color.White.copy(alpha = OUTLINED_KEY_BORDER_ALPHA),
                shape = DeviceShapes.PrimaryKey,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.DeviceOutlinedKeyPaddingHorizontal),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = textStyle,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val PRESSED_RIM_ALPHA = 0.14f
private const val OUTLINED_KEY_BORDER_ALPHA = 0.35f
private const val SCANLINE_ALPHA = 0.07f
private const val SCAN_BAR_ALPHA = 0.45f
private const val REVEAL_GLOW_ALPHA = 0.55f
private const val REVEAL_GLOW_EDGE = 0.65f
private const val LED_GLOW_ALPHA = 0.85f
private const val LENS_GLOW_ALPHA = 0.7f
private const val GLOW_CORE_EDGE = 0.55f
private const val GLOW_FALLOFF = 0.5f
