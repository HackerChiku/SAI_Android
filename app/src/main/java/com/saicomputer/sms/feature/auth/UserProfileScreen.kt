package com.saicomputer.sms.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.ProfileSectionHeader
import com.saicomputer.sms.core.ui.ThemeModeSelector
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.model.UserRole
import com.saicomputer.sms.feature.settings.ThemeViewModel
import com.saicomputer.sms.core.ui.theme.appDimens

@Composable
fun UserProfileScreen(
    user: User?,
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    val initial = user?.fullName?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBarBox {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = appDimens().iconSizeMd, vertical = appDimens().spacingLg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
            ) {
                Box(
                    modifier = Modifier
                        .size(appDimens().iconSizeXxl)
                        .clip(CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(appDimens().iconSizeListInner)
                    )
                }
                Text(
                    "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(appDimens().spacingLg),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacingLg)
        ) {
            ProfileCard {
                ProfileSectionHeader(
                    icon = Icons.Outlined.Person,
                    title = "Account",
                    subtitle = "Your sign-in details and role."
                )
                Spacer(Modifier.height(appDimens().spacingSm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(appDimens().avatarSizeProfile)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initial,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            user?.fullName.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            user?.email.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                ProfileCardDivider()
                ProfileInfoRow(
                    icon = Icons.Outlined.Badge,
                    label = "User ID",
                    value = user?.userId.orEmpty()
                )
                user?.role?.let { role ->
                    ProfileCardDivider()
                    ProfileInfoRow(
                        icon = Icons.Outlined.Shield,
                        label = "Role",
                        value = when (role) {
                            UserRole.Owner -> "Owner"
                            UserRole.Admin -> "Admin"
                            UserRole.Receptionist -> "Receptionist"
                        }
                    )
                }
            }

            ProfileCard {
                ThemeModeSelector(
                    selected = themeMode,
                    onSelected = themeViewModel::setThemeMode
                )
            }

            ProfileCard(onClick = onChangePassword) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
                ) {
                    ProfileIconBadge(
                        icon = Icons.Outlined.Lock,
                        tint = MaterialTheme.colorScheme.tertiary,
                        background = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Change Password",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Update your account password",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(appDimens().iconSizeListInner)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = appDimens().cardShape
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .border(
                width = appDimens().strokeHairline,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().spacingNone)
    ) {
        Column(
            modifier = Modifier.padding(appDimens().spacingLg),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
        ) {
            content()
        }
    }
}

@Composable
private fun ProfileCardDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(vertical = appDimens().spacingXxs)
    )
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileIconBadge(
            icon = icon,
            tint = MaterialTheme.colorScheme.primary,
            background = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileIconBadge(
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    background: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .size(appDimens().iconSizeListBox)
            .clip(appDimens().iconShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(appDimens().iconSizeLg)
        )
    }
}
