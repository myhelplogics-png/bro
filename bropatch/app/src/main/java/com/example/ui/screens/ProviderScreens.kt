package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Booking
import com.example.data.models.BookingStatus
import com.example.data.models.ProviderProfile
import com.example.data.models.VerificationStatus
import com.example.ui.theme.*

@Composable
fun ProviderPartnerScreen(
    provider: ProviderProfile,
    bookings: List<Booking>,
    onAdvanceStatus: (Long, BookingStatus, String) -> Unit,
    onOpenChat: (Booking) -> Unit,
    onRequestPayout: (Double) -> Boolean,
    modifier: Modifier = Modifier
) {
    var isOnline by remember { mutableStateOf(provider.isOnline) }
    var showPayoutModal by remember { mutableStateOf(false) }

    val activeJobs = bookings.filter { it.status != BookingStatus.COMPLETED && it.status != BookingStatus.CANCELLED }
    val completedJobs = bookings.filter { it.status == BookingStatus.COMPLETED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // 1. Partner Profile & Verification Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, HighDensityOutlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = provider.name.take(2).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column {
                                Text(
                                    text = provider.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = provider.businessName,
                                    style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText)
                                )
                            }
                        }

                        // Verification Status Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (provider.verificationStatus) {
                                VerificationStatus.APPROVED -> Emerald40.copy(alpha = 0.15f)
                                VerificationStatus.PENDING -> SafetyAmber40.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.errorContainer
                            }
                        ) {
                            Text(
                                text = provider.verificationStatus.name,
                                color = when (provider.verificationStatus) {
                                    VerificationStatus.APPROVED -> Emerald40
                                    VerificationStatus.PENDING -> Color(0xFF78350F)
                                    else -> MaterialTheme.colorScheme.error
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = HighDensityOutlineVariant)

                    // Online Duty Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isOnline) "🟢 Available for Job Dispatch" else "🔴 Offline (No new dispatches)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Service Area: ${provider.serviceAreas}",
                                style = MaterialTheme.typography.labelSmall.copy(color = HighDensityMutedText)
                            )
                        }

                        Switch(
                            checked = isOnline,
                            onCheckedChange = { isOnline = it },
                            modifier = Modifier.testTag("provider_online_switch")
                        )
                    }
                }
            }
        }

        // 2. Earnings & Financial Wallet Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HighDensityDarkCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Provider Financial Wallet",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Button(
                            onClick = { showPayoutModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityAccentLight),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("request_payout_button")
                        ) {
                            Text("Withdraw", color = HighDensityAccentDarkText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Withdrawable Balance", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f)))
                            Text("₹${provider.pendingPayoutBalance}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = Color.White))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Earnings", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f)))
                            Text("₹${provider.totalEarnings}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HighDensityAccentLight))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("COD In-Hand to Reconcile: ₹${provider.codPendingAmount}", color = Color.White, fontSize = 11.sp)
                        Text("Jobs: ${provider.totalJobsCompleted}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // 3. Active Dispatch Jobs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assigned Active Jobs (${activeJobs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        if (activeJobs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                    border = BorderStroke(1.dp, HighDensityOutlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = HighDensityPrimary, modifier = Modifier.size(36.dp))
                        Text("No active job assignments right now", fontWeight = FontWeight.Bold)
                        Text("Keep your status Online to receive incoming customer bookings.", style = MaterialTheme.typography.bodySmall, color = HighDensityMutedText)
                    }
                }
            }
        } else {
            items(activeJobs) { job ->
                ProviderJobWorkflowCard(
                    booking = job,
                    onAdvanceStatus = onAdvanceStatus,
                    onOpenChat = { onOpenChat(job) }
                )
            }
        }
    }

    // Payout Request Modal
    if (showPayoutModal) {
        PayoutRequestModal(
            currentBalance = provider.pendingPayoutBalance,
            onDismiss = { showPayoutModal = false },
            onSubmit = { amt ->
                onRequestPayout(amt)
                showPayoutModal = false
            }
        )
    }
}

@Composable
fun ProviderJobWorkflowCard(
    booking: Booking,
    onAdvanceStatus: (Long, BookingStatus, String) -> Unit,
    onOpenChat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("provider_job_${booking.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, HighDensityOutlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = booking.bookingCode,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold, color = HighDensityPrimary)
                    )
                    Text(
                        text = booking.serviceName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SafetyAmber40.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = booking.status.label,
                        color = Color(0xFF78350F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Customer details & location
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                border = BorderStroke(1.dp, HighDensityOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Customer: ${booking.customerName} (${booking.customerPhone})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Address: ${booking.address.streetAddress}, ${booking.address.city}", fontSize = 11.sp)
                    Text("Problem: ${booking.problemDescription}", fontSize = 11.sp, color = HighDensityMutedText)
                    Text("Payout Share: ₹${booking.providerPayoutAmount}", fontWeight = FontWeight.Bold, color = Emerald40, fontSize = 12.sp)
                }
            }

            // Actions & State Advancement Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenChat,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chat", fontSize = 12.sp)
                }

                // Dynamic Action Button based on Current State
                when (booking.status) {
                    BookingStatus.PENDING, BookingStatus.SEARCHING_PROVIDER, BookingStatus.PROVIDER_ASSIGNED -> {
                        Button(
                            onClick = { onAdvanceStatus(booking.id, BookingStatus.PROVIDER_ACCEPTED, "Provider accepted job") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald40),
                            modifier = Modifier.weight(1.5f).testTag("accept_job_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Accept Job", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    BookingStatus.PROVIDER_ACCEPTED -> {
                        Button(
                            onClick = { onAdvanceStatus(booking.id, BookingStatus.PROVIDER_ON_WAY, "Technician departed for site") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                            modifier = Modifier.weight(1.5f).testTag("on_way_button")
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Route", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    BookingStatus.PROVIDER_ON_WAY -> {
                        Button(
                            onClick = { onAdvanceStatus(booking.id, BookingStatus.PROVIDER_ARRIVED, "Technician reached location") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber40),
                            modifier = Modifier.weight(1.5f).testTag("arrived_button")
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF78350F), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Arrived", color = Color(0xFF78350F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    BookingStatus.PROVIDER_ARRIVED -> {
                        Button(
                            onClick = { onAdvanceStatus(booking.id, BookingStatus.WORK_STARTED, "Technician began repair work") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                            modifier = Modifier.weight(1.5f).testTag("start_work_button")
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Work", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    BookingStatus.WORK_STARTED -> {
                        Button(
                            onClick = { onAdvanceStatus(booking.id, BookingStatus.COMPLETED, "Repair successfully finished") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald40),
                            modifier = Modifier.weight(1.5f).testTag("complete_work_button")
                        ) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete & Invoice", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun PayoutRequestModal(
    currentBalance: Double,
    onDismiss: () -> Unit,
    onSubmit: (Double) -> Unit
) {
    var amountInput by remember { mutableStateOf(if (currentBalance >= 500) "1000" else "0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Request Bank Payout", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Available Withdrawable Balance: ₹$currentBalance", style = MaterialTheme.typography.bodySmall.copy(color = Emerald40, fontWeight = FontWeight.Bold))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Withdrawal Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Bank Destination: HDFC Bank - A/C ending XX4819 (IFSC: HDFC0001244)", fontSize = 11.sp, color = HighDensityMutedText)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            if (amt in 100.0..currentBalance) {
                                onSubmit(amt)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                    ) {
                        Text("Submit Request")
                    }
                }
            }
        }
    }
}
