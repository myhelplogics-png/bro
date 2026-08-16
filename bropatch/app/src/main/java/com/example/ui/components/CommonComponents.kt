package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.UserRole
import com.example.ui.theme.*

@Composable
fun AppRoleHeader(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    activeBookingsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Top Row: Avatar & Brand & Server Status (High Density Design Theme)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(HighDensityAccentLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            color = HighDensityAccentDarkText,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                    Column {
                        Text(
                            text = "Bropatch Home Services",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = HighDensityMutedText,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "Hello, Alex Johnson",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = HighDensityText,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                // Backend Sync Status Indicator & Notification Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = HighDensityTertiaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityOnTertiaryContainer)
                            )
                            Text(
                                text = "SQL-REST SYNC",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityOnTertiaryContainer,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = HighDensityMutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-Role Segmented Switcher in High Density Lavender Palette
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("role_switcher")
            ) {
                SegmentedButton(
                    selected = currentRole == UserRole.CUSTOMER,
                    onClick = { onRoleSelected(UserRole.CUSTOMER) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = HighDensitySecondaryContainer,
                        activeContentColor = HighDensityOnSecondaryContainer,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        inactiveContentColor = HighDensityMutedText
                    ),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                ) {
                    Text(
                        text = "Customer",
                        fontSize = 12.sp,
                        fontWeight = if (currentRole == UserRole.CUSTOMER) FontWeight.Bold else FontWeight.Medium
                    )
                }

                SegmentedButton(
                    selected = currentRole == UserRole.PROVIDER,
                    onClick = { onRoleSelected(UserRole.PROVIDER) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = HighDensitySecondaryContainer,
                        activeContentColor = HighDensityOnSecondaryContainer,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        inactiveContentColor = HighDensityMutedText
                    ),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                ) {
                    Text(
                        text = "Partner Pro",
                        fontSize = 12.sp,
                        fontWeight = if (currentRole == UserRole.PROVIDER) FontWeight.Bold else FontWeight.Medium
                    )
                }

                SegmentedButton(
                    selected = currentRole == UserRole.ADMIN,
                    onClick = { onRoleSelected(UserRole.ADMIN) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = HighDensitySecondaryContainer,
                        activeContentColor = HighDensityOnSecondaryContainer,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        inactiveContentColor = HighDensityMutedText
                    ),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                ) {
                    Text(
                        text = "Admin",
                        fontSize = 12.sp,
                        fontWeight = if (currentRole == UserRole.ADMIN) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "plumbing" -> Icons.Default.Plumbing
        "bolt", "electrical" -> Icons.Default.Bolt
        "ac_unit", "ac-appliance" -> Icons.Default.AcUnit
        "cleaning_services", "cleaning" -> Icons.Default.CleaningServices
        "handyman", "carpentry" -> Icons.Default.Handyman
        "format_paint", "painting" -> Icons.Default.FormatPaint
        else -> Icons.Default.Build
    }
}
