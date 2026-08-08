package com.unomaster.pokedexgame.presentation.theme

import androidx.compose.ui.unit.dp

// A 4dp-based spacing scale. Screens reference these rather than literal dp values, which is what
// keeps padding consistent as the app grows.
object Dimens {
    val SpacingExtraSmall = 4.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp

    // 48dp is the floor in both Material 3 and Apple's HIG (44pt). Anything tappable gets at least
    // this, applied with heightIn/sizeIn rather than fixed size, so text can still grow.
    val MinimumTouchTarget = 48.dp

    // Game-specific sizes: the artwork and the pokeball on the start screen.
    val ArtworkSize = 260.dp
    val PokeballSize = 220.dp

    // The Pokedex device. A few of these sit off the 4dp grid — 9dp key gaps, 13dp lights, 3dp
    // bezels — because they describe moulded hardware rather than layout: a 13dp light reads as a
    // light and a 12dp one reads as a dot. They are named for the usual reason, so that adjusting
    // the casing is one edit here rather than a hunt through three screens.
    val DeviceChromePaddingTop = 10.dp
    val DeviceChromePaddingBottom = 14.dp
    val DeviceChromePaddingBottomCompact = 12.dp
    val DeviceChromePaddingHorizontal = 18.dp
    val DeviceChromePaddingHorizontalCompact = 16.dp

    val DeviceLensSize = 44.dp
    val DeviceLensSizeCompact = 38.dp
    val DeviceLensRing = 4.dp
    val DeviceLensRingCompact = 3.dp
    val DeviceLensInnerRing = 2.dp

    val DeviceLedSize = 13.dp
    val DeviceLedSizeCompact = 11.dp
    val DeviceLedGap = 8.dp
    val DeviceLedGapCompact = 6.dp
    val DeviceLedRim = 1.5.dp
    val DeviceGlowSpread = 12.dp

    val DeviceCaseMargin = 14.dp
    val DeviceCaseMarginCompact = 12.dp
    val DeviceCasePadding = 14.dp
    val DeviceCasePaddingCompact = 12.dp
    val DeviceCaseGapCompact = 10.dp
    val DeviceCaseGap = 12.dp
    val DeviceCaseGapLarge = 14.dp

    val DeviceScreenBorder = 3.dp
    // A label and its one-line explanation are the same thought, so they sit closer than the 4dp
    // step allows.
    val DeviceLabelGap = 2.dp
    val DeviceScreenPadding = 16.dp
    val DeviceScreenGap = 18.dp
    val DeviceScanlineHeight = 2.dp
    val DeviceScanBarHeight = 26.dp
    val DeviceStatusInsetTop = 8.dp
    val DeviceStatusInsetStart = 10.dp

    val DeviceKeyHeight = 56.dp
    val DevicePrimaryKeyHeight = 60.dp
    val DeviceSolvedKeyHeight = 58.dp
    val DeviceRetryKeyHeight = 54.dp
    val DeviceDismissKeyHeight = 44.dp
    val DeviceKeyEdge = 4.dp
    val DevicePrimaryKeyEdge = 5.dp
    val DeviceKeyGap = 9.dp
    val DeviceKeyRim = 1.5.dp
    val DeviceOutlinedKeyBorder = 2.dp
    val DeviceOutlinedKeyPaddingHorizontal = 18.dp

    // How far the LCD jumps sideways on a wrong guess.
    val DeviceShakeOffset = 5.dp

    // Per-state stages. The LCD is a fixed height so the key grid underneath does not shift between
    // a scanning round and a wrong guess.
    val GameScreenHeight = 288.dp
    val SolvedScreenHeight = 250.dp
    val SilhouetteSize = 200.dp
    val RevealedArtworkSize = 190.dp
    val RevealGlowSize = 260.dp
    val HomePokeballSize = 150.dp
    val LoadingPokeballSize = 84.dp
    val HomeTaglineMaxWidth = 230.dp
}
