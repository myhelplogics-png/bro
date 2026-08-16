package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.models.*
import com.example.ui.theme.*

enum class CustomerTab(val label: String) {
    HOME("Home"),
    BOOKINGS("Bookings"),
    HISTORY("History"),
    PROFILE("Profile")
}

@Composable
fun CustomerBottomBar(
    currentTab: CustomerTab,
    onTabSelected: (CustomerTab) -> Unit,
    activeBookingsCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = HighDensitySurfaceVariant,
        border = BorderStroke(1.dp, HighDensityOutlineVariant),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomerTab.values().forEach { tab ->
                val isSelected = currentTab == tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) HighDensitySecondaryContainer else Color.Transparent)
                            .padding(horizontal = if (isSelected) 18.dp else 4.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (tab == CustomerTab.BOOKINGS && activeBookingsCount > 0) {
                                    Badge(
                                        containerColor = HighDensityTertiary,
                                        contentColor = Color.White
                                    ) {
                                        Text(activeBookingsCount.toString(), fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = when (tab) {
                                    CustomerTab.HOME -> Icons.Default.Home
                                    CustomerTab.BOOKINGS -> Icons.Default.CalendarMonth
                                    CustomerTab.HISTORY -> Icons.Default.History
                                    CustomerTab.PROFILE -> Icons.Default.Person
                                },
                                contentDescription = tab.label,
                                tint = if (isSelected) HighDensityOnSecondaryContainer else HighDensityMutedText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) HighDensityText else HighDensityMutedText,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerBookingsTabScreen(
    bookings: List<Booking>,
    onOpenTracker: (Booking) -> Unit,
    onCancelBooking: (Booking) -> Unit,
    onRescheduleBooking: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    val ongoingBookings = bookings.filter { it.status != BookingStatus.COMPLETED && it.status != BookingStatus.CANCELLED }
    var selectedFilter by remember { mutableStateOf("All Active") }

    val filteredList = when (selectedFilter) {
        "In Progress" -> ongoingBookings.filter { it.status in listOf(BookingStatus.WORK_STARTED, BookingStatus.PROVIDER_ARRIVED) }
        "On The Way" -> ongoingBookings.filter { it.status == BookingStatus.PROVIDER_ON_WAY }
        "Scheduled" -> ongoingBookings.filter { it.status in listOf(BookingStatus.PENDING, BookingStatus.PROVIDER_ASSIGNED, BookingStatus.PROVIDER_ACCEPTED) }
        else -> ongoingBookings
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ACTIVE APPOINTMENTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = HighDensityMutedText
                        )
                    )
                    Text(
                        text = "Live Service Orders (${ongoingBookings.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = HighDensityText
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HighDensityPrimaryContainer
                ) {
                    Text(
                        text = "Real-time Dispatch",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = HighDensityOnPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Filter chips
        item {
            val filters = listOf("All Active", "In Progress", "On The Way", "Scheduled")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { f ->
                    val isChosen = selectedFilter == f
                    FilterChip(
                        selected = isChosen,
                        onClick = { selectedFilter = f },
                        label = { Text(f, fontSize = 12.sp, fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HighDensitySecondaryContainer,
                            selectedLabelColor = HighDensityOnSecondaryContainer
                        )
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = HighDensityMutedText,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No active bookings in this category",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Browse home repair and maintenance services on the Home tab to schedule an appointment.",
                            style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText, textAlign = TextAlign.Center)
                        )
                    }
                }
            }
        } else {
            items(filteredList) { booking ->
                ActiveBookingDetailedCard(
                    booking = booking,
                    onOpenTracker = { onOpenTracker(booking) },
                    onCancel = { onCancelBooking(booking) },
                    onReschedule = { onRescheduleBooking(booking) }
                )
            }
        }
    }
}

@Composable
fun ActiveBookingDetailedCard(
    booking: Booking,
    onOpenTracker: () -> Unit,
    onCancel: () -> Unit,
    onReschedule: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_booking_card_${booking.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, HighDensityOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Service + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HighDensityPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HomeRepairService,
                            contentDescription = null,
                            tint = HighDensityOnPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = booking.serviceName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Slot: ${booking.scheduledDate} • ${booking.scheduledTimeSlot}",
                            style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText, fontSize = 11.sp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HighDensityTertiaryContainer
                ) {
                    Text(
                        text = booking.status.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = HighDensityOnTertiaryContainer,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = HighDensityOutlineVariant)

            // Provider Row (if assigned)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(HighDensitySecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (booking.providerName ?: "P").take(1).uppercase(),
                            color = HighDensityOnSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Column {
                        Text(
                            text = booking.providerName ?: "Assigning Technician...",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (booking.providerPhone != null) "Verified Partner" else "Automated Matcher",
                            style = MaterialTheme.typography.labelSmall.copy(color = HighDensityMutedText, fontSize = 10.sp)
                        )
                    }
                }

                Text(
                    text = "₹${booking.finalAmount.toInt()}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = HighDensityPrimary
                    )
                )
            }

            // Actions: Track Live & Manage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenTracker,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Track & Chat", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onReschedule,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                }

                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun CustomerHistoryTabScreen(
    bookings: List<Booking>,
    onOpenInvoice: (Booking) -> Unit,
    onRateBooking: (Booking) -> Unit,
    onRebook: (Booking) -> Unit,
    onClaimWarranty: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedBookings = bookings.filter { it.status == BookingStatus.COMPLETED || it.status == BookingStatus.CANCELLED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        item {
            Column {
                Text(
                    text = "SERVICE HISTORY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = HighDensityMutedText
                    )
                )
                Text(
                    text = "Completed & Past Bookings (${completedBookings.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HighDensityText
                    )
                )
            }
        }

        if (completedBookings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = HighDensityMutedText,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No past services yet",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Once you complete an appointment, receipts and warranty certificates will appear here.",
                            style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText, textAlign = TextAlign.Center)
                        )
                    }
                }
            }
        } else {
            items(completedBookings) { booking ->
                CompletedBookingCard(
                    booking = booking,
                    onOpenInvoice = { onOpenInvoice(booking) },
                    onRateBooking = { onRateBooking(booking) },
                    onRebook = { onRebook(booking) },
                    onClaimWarranty = { onClaimWarranty(booking) }
                )
            }
        }
    }
}

@Composable
fun CompletedBookingCard(
    booking: Booking,
    onOpenInvoice: () -> Unit,
    onRateBooking: () -> Unit,
    onRebook: () -> Unit,
    onClaimWarranty: () -> Unit
) {
    val isCompleted = booking.status == BookingStatus.COMPLETED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_card_${booking.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, HighDensityOutlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.serviceName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Completed on ${booking.scheduledDate}",
                        style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText, fontSize = 11.sp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCompleted) Emerald40.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (isCompleted) "VERIFIED COMPLETED" else "CANCELLED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Emerald40 else MaterialTheme.colorScheme.error,
                            fontSize = 9.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Summary info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pro: ${booking.providerName ?: "Certified Technician"}",
                    style = MaterialTheme.typography.bodySmall.copy(color = HighDensityText)
                )
                Text(
                    text = "₹${booking.finalAmount.toInt()}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = HighDensityPrimary
                    )
                )
            }

            // 30-Day Guarantee Banner
            if (isCompleted) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = HighDensitySurfaceVariant,
                    border = BorderStroke(1.dp, HighDensityOutlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Emerald40, modifier = Modifier.size(16.dp))
                            Text("30-Day Free Rework Guarantee", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = HighDensityText)
                        }
                        Text(
                            text = "Claim Warranty",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityPrimary,
                            modifier = Modifier.clickable { onClaimWarranty() }
                        )
                    }
                }
            }

            HorizontalDivider(color = HighDensityOutlineVariant)

            // Action Buttons: Invoice, Rate, Rebook
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isCompleted) {
                    OutlinedButton(
                        onClick = onOpenInvoice,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Invoice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onRateBooking,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = SafetyAmber40, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onRebook,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Book Again", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CustomerProfileTabScreen(
    savedAddresses: List<Address>,
    walletBalance: Double,
    coupons: List<Coupon>,
    warrantyClaims: List<WarrantyClaim>,
    onAddAddress: () -> Unit,
    onSetDefaultAddress: (Long) -> Unit,
    onDeleteAddress: (Long) -> Unit,
    onTopUpWallet: (Double) -> Unit,
    onRegisterAsPartner: (name: String, phone: String, businessName: String, skills: String, exp: Int, areas: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSupportHelpdesk by remember { mutableStateOf(false) }
    var showTopUpDialog by remember { mutableStateOf(false) }
    var showCouponsDialog by remember { mutableStateOf(false) }
    var showPartnerRegDialog by remember { mutableStateOf(false) }
    var showWarrantyListDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // User Profile Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                border = BorderStroke(1.dp, HighDensityOutlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(HighDensityAccentLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AJ",
                            color = HighDensityAccentDarkText,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alex Johnson",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "+91 98765 43210 • alex.johnson@example.com",
                            style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText)
                        )
                        Text(
                            text = "Bropatch Club Member • Since Aug 2024",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HighDensityPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }

        // Wallet & Credits Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HighDensityDarkCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BROPATCH WALLET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HighDensityOutline,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "₹${"%.2f".format(walletBalance)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Available promo cash for any repair",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = HighDensityAccentLight,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Button(
                        onClick = { showTopUpDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensityAccentLight,
                            contentColor = HighDensityAccentDarkText
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Top Up",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Saved Addresses Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAVED ADDRESSES (${savedAddresses.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = HighDensityMutedText
                    )
                )

                TextButton(onClick = onAddAddress) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = HighDensityPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add New", color = HighDensityPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        items(savedAddresses) { addr ->
            AddressCardItem(
                address = addr,
                onSetDefault = { onSetDefaultAddress(addr.id) },
                onDelete = { onDeleteAddress(addr.id) }
            )
        }

        // Partner & Coupons Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "OFFERS & PARTNER NETWORK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = HighDensityMutedText
                    )
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, HighDensityOutlineVariant)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = { Text("Available Coupons & Offers", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("View active discount promo codes") },
                            leadingContent = { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = HighDensityPrimary) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.clickable { showCouponsDialog = true }
                        )
                        HorizontalDivider(color = HighDensityOutlineVariant)
                        ListItem(
                            headlineContent = { Text("Become a Bropatch Partner", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Join our technician network & receive daily job leads") },
                            leadingContent = { Icon(Icons.Default.Engineering, contentDescription = null, tint = Emerald40) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.clickable { showPartnerRegDialog = true }
                        )
                    }
                }
            }
        }

        // Support and Safety Desk
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "HELP & PREFERENCES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = HighDensityMutedText
                    )
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, HighDensityOutlineVariant)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = { Text("24x7 Customer Support", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Instant chat with dispute & resolution team") },
                            leadingContent = { Icon(Icons.Default.SupportAgent, contentDescription = null, tint = HighDensityPrimary) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.clickable { showSupportHelpdesk = true }
                        )
                        HorizontalDivider(color = HighDensityOutlineVariant)
                        ListItem(
                            headlineContent = { Text("30-Day Guarantee Claims (${warrantyClaims.size})", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("View status of free warranty rework tickets") },
                            leadingContent = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Emerald40) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.clickable { showWarrantyListDialog = true }
                        )
                        HorizontalDivider(color = HighDensityOutlineVariant)
                        ListItem(
                            headlineContent = { Text("MySQL / REST Sync Status", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Cloud PHP Backend v2.4 Active") },
                            leadingContent = { Icon(Icons.Default.CloudDone, contentDescription = null, tint = HighDensityPrimary) }
                        )
                    }
                }
            }
        }
    }

    if (showWarrantyListDialog) {
        WarrantyClaimsListDialog(
            claims = warrantyClaims,
            onDismiss = { showWarrantyListDialog = false }
        )
    }

    if (showTopUpDialog) {
        WalletTopUpDialog(
            currentBalance = walletBalance,
            onDismiss = { showTopUpDialog = false },
            onConfirmTopUp = { amt ->
                onTopUpWallet(amt)
                showTopUpDialog = false
            }
        )
    }

    if (showCouponsDialog) {
        CouponsListDialog(
            coupons = coupons,
            onDismiss = { showCouponsDialog = false }
        )
    }

    if (showPartnerRegDialog) {
        ProviderRegistrationDialog(
            onDismiss = { showPartnerRegDialog = false },
            onRegister = { name, phone, bName, skills, exp, areas ->
                onRegisterAsPartner(name, phone, bName, skills, exp, areas)
                showPartnerRegDialog = false
            }
        )
    }

    if (showSupportHelpdesk) {
        AlertDialog(
            onDismissRequest = { showSupportHelpdesk = false },
            title = { Text("Bropatch Support Center", fontWeight = FontWeight.Bold) },
            text = {
                Text("Our customer protection and dispute team is available 24/7. Call our priority line at 1800-BROPATCH or email support@bropatch.com.")
            },
            confirmButton = {
                Button(onClick = { showSupportHelpdesk = false }) {
                    Text("Got It")
                }
            }
        )
    }
}

@Composable
fun AddressCardItem(
    address: Address,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            if (address.isDefault) 1.5.dp else 1.dp,
            if (address.isDefault) HighDensityPrimary else HighDensityOutlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (address.isDefault) HighDensityPrimaryContainer else HighDensitySurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (address.label.lowercase()) {
                            "work", "office" -> Icons.Default.Work
                            else -> Icons.Default.Home
                        },
                        contentDescription = null,
                        tint = if (address.isDefault) HighDensityOnPrimaryContainer else HighDensityMutedText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = address.label,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (address.isDefault) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = HighDensityPrimaryContainer
                            ) {
                                Text(
                                    text = "DEFAULT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = HighDensityOnPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${address.streetAddress}${address.apartmentUnit?.let { ", $it" } ?: ""}, ${address.city}",
                        style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row {
                if (!address.isDefault) {
                    IconButton(onClick = onSetDefault) {
                        Icon(Icons.Default.Check, contentDescription = "Set Default", tint = HighDensityMutedText)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Address", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// Dialogs
@Composable
fun InvoiceDialog(
    booking: Booking,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TAX INVOICE & RECEIPT", style = MaterialTheme.typography.labelSmall.copy(color = HighDensityMutedText, fontWeight = FontWeight.Bold))
                        Text(booking.bookingCode, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = HighDensityOutlineVariant)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Customer: ${booking.customerName}", style = MaterialTheme.typography.bodySmall)
                    Text("Service: ${booking.serviceName}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    Text("Assigned Pro: ${booking.providerName ?: "Direct Dispatch"}", style = MaterialTheme.typography.bodySmall)
                    Text("Date & Time: ${booking.scheduledDate}, ${booking.scheduledTimeSlot}", style = MaterialTheme.typography.bodySmall)
                    Text("Payment: ${booking.paymentMethod.name} (${booking.paymentStatus})", style = MaterialTheme.typography.bodySmall)
                }

                HorizontalDivider(color = HighDensityOutlineVariant)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Base Labor Charge", style = MaterialTheme.typography.bodySmall)
                        Text("₹${booking.baseAmount}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (booking.discountAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Coupon Discount", style = MaterialTheme.typography.bodySmall.copy(color = Emerald40))
                            Text("-₹${booking.discountAmount}", style = MaterialTheme.typography.bodySmall.copy(color = Emerald40))
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("GST Taxes (18%)", style = MaterialTheme.typography.bodySmall)
                        Text("₹${booking.taxAmount}", style = MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Paid", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text("₹${booking.finalAmount}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = HighDensityPrimary))
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                ) {
                    Text("Download PDF Receipt", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RatingDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onSubmit: (Double, String) -> Unit
) {
    var rating by remember { mutableStateOf(5.0) }
    var reviewText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rate Your Service Pro",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "How was your experience with ${booking.providerName ?: "the technician"} for ${booking.serviceName}?",
                    style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText, textAlign = TextAlign.Center)
                )

                // Star row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { starIndex ->
                        IconButton(onClick = { rating = starIndex.toDouble() }) {
                            Icon(
                                imageVector = if (rating >= starIndex) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$starIndex Star",
                                tint = SafetyAmber40,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Write your review (e.g. prompt arrival, clean work)...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onSubmit(rating, reviewText.ifBlank { "Service completed satisfactorily." })
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                    ) {
                        Text("Submit", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddAddressDialog(
    onDismiss: () -> Unit,
    onSave: (label: String, street: String, apt: String?, landmark: String?, isDefault: Boolean) -> Unit
) {
    var label by remember { mutableStateOf("Home") }
    var street by remember { mutableStateOf("") }
    var apt by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Service Location",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Label selector
                val labels = listOf("Home", "Work", "Other")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(labels) { l ->
                        FilterChip(
                            selected = label == l,
                            onClick = { label = l },
                            label = { Text(l) }
                        )
                    }
                }

                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Street Address / Flat No") },
                    placeholder = { Text("e.g. 402, Green Park Residency") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = apt,
                    onValueChange = { apt = it },
                    label = { Text("Apartment / Block (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Landmark (Optional)") },
                    placeholder = { Text("Near City Center Mall") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                    Text("Set as default address", fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (street.isNotBlank()) {
                                onSave(label, street, apt.ifBlank { null }, landmark.ifBlank { null }, isDefault)
                                onDismiss()
                            }
                        },
                        enabled = street.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                    ) {
                        Text("Save Address")
                    }
                }
            }
        }
    }
}

@Composable
fun CancelBookingDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onConfirmCancel: (String) -> Unit
) {
    var reason by remember { mutableStateOf("Change in schedule") }
    val reasons = listOf("Change in schedule", "Found alternative technician", "Booked by mistake", "Pricing issue")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Appointment?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Please select a reason for cancelling ${booking.serviceName}:", fontSize = 12.sp)
                reasons.forEach { r ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = r }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = reason == r, onClick = { reason = r })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(r, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmCancel(reason)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Confirm Cancel")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Keep Booking")
            }
        }
    )
}

@Composable
fun RescheduleBookingDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onConfirmReschedule: (newDate: String, newSlot: String) -> Unit
) {
    var selectedDate by remember { mutableStateOf("Tomorrow, Aug 17") }
    var selectedSlot by remember { mutableStateOf("11:00 AM - 01:00 PM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reschedule Service", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select new date for ${booking.serviceName}:", fontSize = 12.sp)
                val dates = listOf("Tomorrow, Aug 17", "Mon, Aug 18", "Tue, Aug 19")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(dates) { d ->
                        FilterChip(
                            selected = selectedDate == d,
                            onClick = { selectedDate = d },
                            label = { Text(d, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Select new time slot:", fontSize = 12.sp)
                val slots = listOf("09:00 AM - 11:00 AM", "11:00 AM - 01:00 PM", "02:00 PM - 04:00 PM", "04:00 PM - 06:00 PM")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(slots) { s ->
                        FilterChip(
                            selected = selectedSlot == s,
                            onClick = { selectedSlot = s },
                            label = { Text(s, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmReschedule(selectedDate, selectedSlot)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
            ) {
                Text("Save Reschedule")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WalletTopUpDialog(
    currentBalance: Double,
    onDismiss: () -> Unit,
    onConfirmTopUp: (Double) -> Unit
) {
    var selectedAmount by remember { mutableStateOf(500.0) }
    var customAmount by remember { mutableStateOf("") }
    val presetAmounts = listOf(200.0, 500.0, 1000.0, 2000.0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Add Bropatch Cash", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Current Balance: ₹${"%.2f".format(currentBalance)}", style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = HighDensityOutlineVariant)

                Text("Select Recharge Amount:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = HighDensityMutedText))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAmounts.forEach { amt ->
                        val isSelected = selectedAmount == amt && customAmount.isEmpty()
                        OutlinedButton(
                            onClick = {
                                selectedAmount = amt
                                customAmount = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) HighDensityPrimary else HighDensityOutlineVariant),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) HighDensityPrimaryContainer else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "₹${amt.toInt()}",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) HighDensityOnPrimaryContainer else HighDensityText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customAmount,
                    onValueChange = {
                        customAmount = it
                        it.toDoubleOrNull()?.let { a -> selectedAmount = a }
                    },
                    label = { Text("Or Enter Custom Amount (₹)") },
                    placeholder = { Text("e.g. 750") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = HighDensitySurfaceVariant,
                    border = BorderStroke(1.dp, HighDensityOutlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = HighDensityPrimary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Instant 100% cashback guarantee for unused balance.",
                            style = MaterialTheme.typography.labelSmall.copy(color = HighDensityMutedText, fontSize = 11.sp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val finalAmt = customAmount.toDoubleOrNull() ?: selectedAmount
                            if (finalAmt > 0) {
                                onConfirmTopUp(finalAmt)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                    ) {
                        Text("Pay & Add", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CouponsListDialog(
    coupons: List<Coupon>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Exclusive Coupons & Deals", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("${coupons.size} promo codes active", style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = HighDensityOutlineVariant)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(coupons) { coupon ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                            border = BorderStroke(1.dp, HighDensityOutlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = HighDensityPrimaryContainer
                                    ) {
                                        Text(
                                            text = coupon.code,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = HighDensityOnPrimaryContainer
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Text(
                                        text = if (coupon.discountType == "percentage") "${coupon.discountValue.toInt()}% OFF" else "₹${coupon.discountValue.toInt()} OFF",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald40
                                        )
                                    )
                                }

                                Text(
                                    text = coupon.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = HighDensityText)
                                )

                                Text(
                                    text = "Min order: ₹${coupon.minOrderAmount.toInt()} • Max savings: ${coupon.maxDiscountAmount?.let { "₹${it.toInt()}" } ?: "No Limit"}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = HighDensityMutedText, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
fun ProviderRegistrationDialog(
    onDismiss: () -> Unit,
    onRegister: (name: String, phone: String, businessName: String, skills: String, exp: Int, areas: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("Plumbing & Pipe Fitting") }
    var experienceYears by remember { mutableStateOf("5") }
    var serviceAreas by remember { mutableStateOf("South Delhi, Connaught Place") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Partner Onboarding", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Earn up to ₹45,000/mo on Bropatch", style = MaterialTheme.typography.bodySmall.copy(color = HighDensityPrimary, fontWeight = FontWeight.SemiBold))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = HighDensityOutlineVariant)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. Sunil Verma") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+91 98765 XXXXX") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business / Trade Name") },
                    placeholder = { Text("e.g. Verma Electrical Solutions") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("Primary Skills & Trades") },
                    placeholder = { Text("Electrical, Inverter Repair, AC") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = experienceYears,
                        onValueChange = { experienceYears = it },
                        label = { Text("Exp (Yrs)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = serviceAreas,
                        onValueChange = { serviceAreas = it },
                        label = { Text("Service Localities") },
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                onRegister(
                                    name,
                                    phone,
                                    businessName.ifBlank { "$name Services" },
                                    skills,
                                    experienceYears.toIntOrNull() ?: 3,
                                    serviceAreas
                                )
                            }
                        },
                        enabled = name.isNotBlank() && phone.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                    ) {
                        Text("Submit Application")
                    }
                }
            }
        }
    }
}

@Composable
fun WarrantyClaimDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onSubmitClaim: (reason: String, description: String, preferredDate: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("Same problem reoccurred") }
    var description by remember { mutableStateOf("") }
    var preferredDate by remember { mutableStateOf("Tomorrow, Aug 17, 2026 (10:00 AM)") }

    val reasons = listOf(
        "Same problem reoccurred",
        "New leakage / loose fitting",
        "Part replacement issue",
        "Electrical trip / sparking returned",
        "Dissatisfied with work quality"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Emerald40)
                        Column {
                            Text("30-Day Guarantee Claim", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("100% Free Rework Visit", fontSize = 11.sp, color = HighDensityMutedText)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Service Details Header
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = HighDensitySurfaceVariant,
                    border = BorderStroke(1.dp, HighDensityOutlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(booking.serviceName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Order #${booking.bookingCode} • Serviced by ${booking.providerName ?: "Pro"}", fontSize = 11.sp, color = HighDensityMutedText)
                    }
                }

                Text("REASON FOR WARRANTY VISIT:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = HighDensityMutedText)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    reasons.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = r }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedReason == r,
                                onClick = { selectedReason = r }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(r, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details of the issue") },
                    placeholder = { Text("Describe what happened after the technician left...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )

                OutlinedTextField(
                    value = preferredDate,
                    onValueChange = { preferredDate = it },
                    label = { Text("Preferred Re-visit Date & Time") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onSubmitClaim(selectedReason, description.ifBlank { selectedReason }, preferredDate)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("Schedule Free Rework")
                    }
                }
            }
        }
    }
}

@Composable
fun WarrantyClaimsListDialog(
    claims: List<WarrantyClaim>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Emerald40)
                        Column {
                            Text("Warranty & Rework Tickets", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${claims.size} Total Guarantee Claims", fontSize = 11.sp, color = HighDensityMutedText)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (claims.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald40, modifier = Modifier.size(36.dp))
                        Text("No Warranty Claims Submitted", fontWeight = FontWeight.Bold)
                        Text("All your services are running under pristine guarantee.", fontSize = 11.sp, color = HighDensityMutedText, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(claims) { claim ->
                            val statusColor = when (claim.status) {
                                "APPROVED" -> Emerald40
                                "RESOLVED" -> Emerald40
                                "REWORK_SCHEDULED" -> HighDensityPrimary
                                else -> SafetyAmber40
                            }

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                                border = BorderStroke(1.dp, HighDensityOutlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(claim.serviceName, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = statusColor.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, statusColor)
                                        ) {
                                            Text(
                                                claim.status,
                                                color = statusColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Order #${claim.bookingCode} • ${claim.createdAt}",
                                        fontSize = 10.sp,
                                        color = HighDensityMutedText
                                    )

                                    Text(
                                        text = "Issue: ${claim.reason}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = claim.description,
                                        fontSize = 11.sp,
                                        color = HighDensityMutedText
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Scheduled Re-visit:", fontSize = 10.sp, color = HighDensityMutedText)
                                            Text(claim.preferredDate, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HighDensityPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
