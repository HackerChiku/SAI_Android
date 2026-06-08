package com.saicomputer.sms.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.StatusBlue
import com.saicomputer.sms.data.model.UserRole
import com.saicomputer.sms.navigation.AppViewModel

private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val user by appViewModel.currentUser.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandBlue)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BaseWhite)
            }
            Text(
                "Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BaseWhite
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhite)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = BaseWhite)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileField("Name", user?.fullName.orEmpty())
                    ProfileField("Email", user?.email.orEmpty())
                    ProfileField("User ID", user?.userId.orEmpty())
                    user?.role?.let { role ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Role",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSurfaceVariantLightColor
                            )
                            Pill(
                                text = when (role) {
                                    UserRole.Owner -> "Owner"
                                    UserRole.Admin -> "Admin"
                                    UserRole.Receptionist -> "Receptionist"
                                },
                                color = StatusBlue,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onChangePassword,
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = BaseWhite)
            ) {
                Text("Change Password", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariantLightColor)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
