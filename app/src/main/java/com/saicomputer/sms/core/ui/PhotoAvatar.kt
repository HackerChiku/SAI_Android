package com.saicomputer.sms.core.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Initials-only avatar; lists never fetch thumbnails. */
@Composable
fun PhotoAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 44
) {
    ColoredPhotoAvatar(name = name, modifier = modifier, size = size)
}

private val AVATAR_PALETTES = listOf(
    Pair(Color(0xFFDBEAFE), Color(0xFF1D4ED8)),
    Pair(Color(0xFFEDE9FE), Color(0xFF6D28D9)),
    Pair(Color(0xFFD1FAE5), Color(0xFF047857)),
    Pair(Color(0xFFFFEDD5), Color(0xFFC2410C)),
    Pair(Color(0xFFFCE7F3), Color(0xFFBE185D)),
    Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
)

/** Avatar with pastel background color derived from the student's name. */
@Composable
fun ColoredPhotoAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 44
) {
    val initials = remember(name) { initialsOf(name) }
    val paletteIndex = remember(name) { kotlin.math.abs(name.hashCode()) % AVATAR_PALETTES.size }
    val (background, foreground) = AVATAR_PALETTES[paletteIndex]

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = foreground,
            fontWeight = FontWeight.SemiBold,
            fontSize = (size / 2.6).sp
        )
    }
}

/** Profile avatar that shows initials while loading or when no photo is available. */
@Composable
fun StudentPhotoAvatar(
    name: String,
    base64: String?,
    loading: Boolean,
    modifier: Modifier = Modifier,
    size: Int = 88
) {
    val bitmap = rememberBase64ImageBitmap(base64)
    val showPhoto = !loading && bitmap != null

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                shape = CircleShape
            )
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (showPhoto) {
            Image(
                bitmap = bitmap!!,
                contentDescription = "Profile photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            InitialsAvatar(name = name, size = size)
        }
    }
}

@Composable
private fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int
) {
    val initials = remember(name) { initialsOf(name) }
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
            fontSize = (size / 2.6).sp
        )
    }
}

@Composable
fun rememberBase64ImageBitmap(base64: String?): ImageBitmap? = remember(base64) {
    base64?.let {
        runCatching {
            val bytes = Base64.decode(it, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }
}

internal fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}
