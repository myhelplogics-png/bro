package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.Booking
import com.example.data.models.BookingStatus
import com.example.data.models.ChatMessage
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveBookingTrackerDialog(
    booking: Booking,
    chatMessages: List<ChatMessage>,
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit,
    onAdvanceStatusSimulation: ((BookingStatus) -> Unit)? = null
) {
    var showChatDialog by remember { mutableStateOf(false) }
    var showInvoiceDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showCallingDialog by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = booking.bookingCode,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when (booking.status) {
                                            BookingStatus.COMPLETED -> Emerald40.copy(alpha = 0.15f)
                                            BookingStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                                            else -> HighDensityPrimaryContainer
                                        }
                                    ) {
                                        Text(
                                            text = booking.status.label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = when (booking.status) {
                                                    BookingStatus.COMPLETED -> Emerald40
                                                    BookingStatus.CANCELLED -> MaterialTheme.colorScheme.error
                                                    else -> HighDensityOnPrimaryContainer
                                                }
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = booking.serviceName,
                                    style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSosDialog = true }) {
                                Icon(Icons.Default.Shield, contentDescription = "Safety SOS", tint = MaterialTheme.colorScheme.error)
                            }
                            IconButton(onClick = { showInvoiceDialog = true }) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = "Invoice", tint = HighDensityPrimary)
                            }
                        }
                    )
                },
                bottomBar = {
                    Surface(
                        tonalElevation = 4.dp,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showChatDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_chat_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Live Chat", fontWeight = FontWeight.Bold)
                            }

                            if (booking.status == BookingStatus.COMPLETED) {
                                Button(
                                    onClick = { showReviewDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = SafetyAmber40, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rate Pro", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { showCallingDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call Pro", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
                ) {
                    // 1. Live Interactive Route Map Canvas
                    item {
                        LiveMapVisualizer(booking = booking)
                    }

                    // 2. Doorstep Start Verification OTP Card (Crucial for safety & anti-fraud)
                    item {
                        val otpCode = ((booking.id % 9000) + 1000).toString()
                        Card(
                            shape = RoundedCornerShape(16.dp),
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
                                        text = "DOORSTEP START OTP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = HighDensityOutline,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    Text(
                                        text = "Share with technician upon arrival",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = HighDensityAccentLight
                                ) {
                                    Text(
                                        text = otpCode,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 4.sp,
                                            color = HighDensityAccentDarkText
                                        ),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Provider Info Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                            border = BorderStroke(1.dp, HighDensityOutlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(HighDensityPrimaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (booking.providerName?.firstOrNull() ?: 'P').toString(),
                                            color = HighDensityOnPrimaryContainer,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = booking.providerName ?: "Matching verified partner...",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Bropatch Verified Pro ★ 4.9 • Background Checked",
                                            style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText, fontSize = 11.sp)
                                        )
                                        Text(
                                            text = booking.providerPhone ?: "+91 98111 22334",
                                            style = MaterialTheme.typography.labelSmall.copy(color = HighDensityPrimary, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showCallingDialog = true },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(Emerald40.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = Emerald40)
                                }
                            }
                        }
                    }

                    // 4. State Machine Progression Timeline
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Booking Timeline & Steps",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Step ${booking.status.stepIndex + 1} of 7",
                                    style = MaterialTheme.typography.labelSmall.copy(color = HighDensityMutedText)
                                )
                            }
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, HighDensityOutlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val stages = listOf(
                                        BookingStatus.PENDING,
                                        BookingStatus.SEARCHING_PROVIDER,
                                        BookingStatus.PROVIDER_ASSIGNED,
                                        BookingStatus.PROVIDER_ON_WAY,
                                        BookingStatus.PROVIDER_ARRIVED,
                                        BookingStatus.WORK_STARTED,
                                        BookingStatus.COMPLETED
                                    )

                                    stages.forEachIndexed { index, stage ->
                                        val isPassed = booking.status.stepIndex >= stage.stepIndex
                                        val isCurrent = booking.status == stage

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isPassed) HighDensityPrimary else HighDensityOutline.copy(alpha = 0.4f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isPassed) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = "${index + 1}",
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = stage.label,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isPassed) HighDensityText else HighDensityMutedText
                                                    )
                                                )
                                            }

                                            if (isCurrent) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = HighDensitySecondaryContainer
                                                ) {
                                                    Text(
                                                        text = "Active Now",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = HighDensityOnSecondaryContainer,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Job Details Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                            border = BorderStroke(1.dp, HighDensityOutlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Service Request Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Scheduled: ${booking.scheduledDate} • ${booking.scheduledTimeSlot}", style = MaterialTheme.typography.bodySmall)
                                Text("Address: ${booking.address.streetAddress}, ${booking.address.city}", style = MaterialTheme.typography.bodySmall)
                                Text("Problem: ${booking.problemDescription}", style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText))
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = HighDensityOutlineVariant)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Payment Method", style = MaterialTheme.typography.bodySmall)
                                    Text(booking.paymentStatus, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Emerald40))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // In-App Chat Modal
    if (showChatDialog) {
        InAppChatModal(
            booking = booking,
            messages = chatMessages,
            onDismiss = { showChatDialog = false },
            onSend = onSendMessage
        )
    }

    // GST Invoice Modal
    if (showInvoiceDialog) {
        InvoiceModal(
            booking = booking,
            onDismiss = { showInvoiceDialog = false }
        )
    }

    // Rate & Review Modal
    if (showReviewDialog) {
        ReviewModal(
            booking = booking,
            onDismiss = { showReviewDialog = false }
        )
    }

    // Simulated VoIP Call Modal
    if (showCallingDialog) {
        SimulatedVoipCallDialog(
            booking = booking,
            onDismiss = { showCallingDialog = false }
        )
    }

    // Safety SOS Dialog
    if (showSosDialog) {
        SafetySosDialog(
            booking = booking,
            onDismiss = { showSosDialog = false }
        )
    }
}

@Composable
fun LiveMapVisualizer(booking: Booking) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE2E8F0))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw stylized road grid
                drawLine(Color(0xFFCBD5E1), Offset(0f, h * 0.4f), Offset(w, h * 0.4f), strokeWidth = 14f)
                drawLine(Color(0xFFCBD5E1), Offset(w * 0.35f, 0f), Offset(w * 0.35f, h), strokeWidth = 14f)
                drawLine(Color(0xFFCBD5E1), Offset(w * 0.7f, 0f), Offset(w * 0.7f, h), strokeWidth = 14f)

                // Route Path from Provider to Customer
                val start = Offset(w * 0.25f, h * 0.75f)
                val mid = Offset(w * 0.5f, h * 0.4f)
                val end = Offset(w * 0.8f, h * 0.3f)

                drawLine(
                    color = HighDensityPrimary,
                    start = start,
                    end = mid,
                    strokeWidth = 6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = HighDensityPrimary,
                    start = mid,
                    end = end,
                    strokeWidth = 6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f),
                    cap = StrokeCap.Round
                )

                // Customer Pin (End)
                drawCircle(color = HighDensityDarkCard.copy(alpha = 0.3f), radius = 24f, center = end)
                drawCircle(color = HighDensityDarkCard, radius = 12f, center = end)

                // Provider Live Location (Mid or Start)
                val providerPos = if (booking.status == BookingStatus.PROVIDER_ARRIVED || booking.status == BookingStatus.WORK_STARTED) end else mid
                drawCircle(color = HighDensityPrimary.copy(alpha = pulseAlpha), radius = 28f, center = providerPos)
                drawCircle(color = HighDensityPrimary, radius = 14f, center = providerPos)
                drawCircle(color = Color.White, radius = 6f, center = providerPos)
            }

            // ETA and Telemetry Pill
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                color = HighDensityDarkCard.copy(alpha = 0.92f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = HighDensityAccentLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when (booking.status) {
                            BookingStatus.PROVIDER_ON_WAY -> "ETA: ~12 Mins (2.4 km away)"
                            BookingStatus.PROVIDER_ARRIVED -> "Partner arrived at doorstep"
                            BookingStatus.WORK_STARTED -> "Repair in progress"
                            BookingStatus.COMPLETED -> "Service complete"
                            else -> "Live GPS Telemetry Active"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppChatModal(
    booking: Booking,
    messages: List<ChatMessage>,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val quickReplies = listOf("Technician is here", "Please bring replacement parts", "Please call when nearby", "Will be home in 5 mins")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = booking.providerName ?: "Service Chat",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Booking #${booking.bookingCode}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = HighDensityMutedText)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    )
                },
                bottomBar = {
                    Surface(
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Quick response chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(quickReplies) { reply ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = HighDensitySurfaceVariant,
                                        border = BorderStroke(1.dp, HighDensityOutlineVariant),
                                        modifier = Modifier.clickable { onSend(reply) }
                                    ) {
                                        Text(
                                            text = reply,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                color = HighDensityPrimary,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    placeholder = { Text("Type message to technician...") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chat_input_field"),
                                    shape = RoundedCornerShape(20.dp),
                                    maxLines = 3
                                )

                                IconButton(
                                    onClick = {
                                        if (inputText.isNotBlank()) {
                                            onSend(inputText.trim())
                                            inputText = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(HighDensityPrimary, CircleShape)
                                        .testTag("send_chat_button")
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(messages) { msg ->
                        val isMe = msg.senderRole.equals("Customer", ignoreCase = true)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isMe) 14.dp else 2.dp,
                                    bottomEnd = if (isMe) 2.dp else 14.dp
                                ),
                                color = if (isMe) HighDensityPrimary else HighDensitySurfaceVariant,
                                tonalElevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .widthIn(max = 260.dp)
                                ) {
                                    Text(
                                        text = msg.senderName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMe) HighDensityAccentLight else HighDensityPrimary,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = msg.messageText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isMe) Color.White else HighDensityText
                                        )
                                    )
                                    Text(
                                        text = msg.timestamp,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isMe) Color.White.copy(alpha = 0.7f) else HighDensityMutedText,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceModal(
    booking: Booking,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("BROPATCH INVOICE", fontWeight = FontWeight.Black, color = HighDensityPrimary, fontSize = 16.sp)
                        Text("GSTIN: 07AABCB1234F1Z8", style = MaterialTheme.typography.labelSmall.copy(color = HighDensityMutedText))
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = Emerald40.copy(alpha = 0.15f)) {
                        Text(
                            text = booking.paymentStatus,
                            color = Emerald40,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = HighDensityOutlineVariant)

                // Invoice Meta
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Invoice #: INV-${booking.bookingCode}", style = MaterialTheme.typography.labelSmall)
                        Text("Date: ${booking.createdAt}", style = MaterialTheme.typography.labelSmall.copy(color = HighDensityMutedText))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Customer: ${booking.customerName}", style = MaterialTheme.typography.labelSmall)
                        Text("Provider: ${booking.providerName ?: "Assigned Pro"}", style = MaterialTheme.typography.labelSmall.copy(color = HighDensityMutedText))
                    }
                }

                // Service Item Table
                Card(
                    colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, HighDensityOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(booking.serviceName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Base Amount", style = MaterialTheme.typography.bodySmall)
                            Text("₹${booking.baseAmount}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (booking.discountAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount Applied", style = MaterialTheme.typography.bodySmall.copy(color = Emerald40))
                                Text("-₹${booking.discountAmount}", style = MaterialTheme.typography.bodySmall.copy(color = Emerald40))
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST (9%) + SGST (9%)", style = MaterialTheme.typography.bodySmall)
                            Text("₹${booking.taxAmount}", style = MaterialTheme.typography.bodySmall)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = HighDensityOutlineVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid", fontWeight = FontWeight.Bold)
                            Text("₹${booking.finalAmount}", fontWeight = FontWeight.Bold, color = HighDensityPrimary)
                        }
                    }
                }

                // CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download PDF")
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewModal(
    booking: Booking,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (submitted) "Thank You!" else "Rate Your Service",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (submitted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Emerald40,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Your review and rating have been recorded in the database.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("Done")
                    }
                } else {
                    Text(
                        text = "How was your experience with ${booking.providerName ?: "the technician"} for ${booking.serviceName}?",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText)
                    )

                    // 5-Star Rating Selector
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= rating) SafetyAmber40 else HighDensityOutline,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        placeholder = { Text("Write detailed feedback...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = { submitted = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                        ) {
                            Text("Submit Review")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedVoipCallDialog(
    booking: Booking,
    onDismiss: () -> Unit
) {
    var callDurationSeconds by remember { mutableStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDurationSeconds++
        }
    }

    val minutes = callDurationSeconds / 60
    val seconds = callDurationSeconds % 60
    val durationFormatted = "%02d:%02d".format(minutes, seconds)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp)),
            color = HighDensityDarkCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "IN-APP SECURE CALL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = HighDensityOutline,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(HighDensityAccentLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = HighDensityAccentDarkText,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = booking.providerName ?: "Service Technician",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Masked Call • $durationFormatted",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Emerald40,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Phone numbers are masked for your privacy",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = HighDensityOutline,
                            fontSize = 10.sp
                        )
                    )
                }

                // Call Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                if (isMuted) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.12f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(
                        onClick = { isSpeakerOn = !isSpeakerOn },
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                if (isSpeakerOn) HighDensityAccentLight else Color.White.copy(alpha = 0.12f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            contentDescription = "Speaker",
                            tint = if (isSpeakerOn) HighDensityAccentDarkText else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SafetySosDialog(
    booking: Booking,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text("Bropatch Safety Desk", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Your safety and quality assurance are protected by Bropatch Guarantee.",
                    fontSize = 12.sp
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = HighDensitySurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("• Background-verified professionals only", fontSize = 11.sp)
                        Text("• 30-day free rework warranty on all tasks", fontSize = 11.sp)
                        Text("• Up to ₹10,000 property damage insurance cover", fontSize = 11.sp)
                        Text("• Priority 24x7 Safety Response Team", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Emergency Call Desk")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
