package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.data.models.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConsoleScreen(
    stats: AdminDashboardStats,
    providers: List<ProviderProfile>,
    bookings: List<Booking>,
    services: List<HomeService>,
    auditLogs: List<AuditLogEntry>,
    warrantyClaims: List<WarrantyClaim>,
    backendUrl: String,
    onApproveProvider: (Long) -> Unit,
    onRejectProvider: (Long) -> Unit,
    onAssignProvider: (Long, Long) -> Unit,
    onUpdatePrice: (Long, Double, Double?) -> Unit,
    onUpdateWarrantyClaimStatus: (Long, String) -> Unit,
    onUpdateBackendUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAdminTab by remember { mutableStateOf(0) }
    var editingService by remember { mutableStateOf<HomeService?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // 1. KPI Telemetry Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Operational Metrics & Revenue",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Gross Revenue",
                        value = "₹${stats.grossRevenue.toInt()}",
                        subtitle = "Completed GMV",
                        color = HighDensityPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Platform Fee (15%)",
                        value = "₹${stats.platformEarnings.toInt()}",
                        subtitle = "Net Commission",
                        color = Emerald40,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Active Providers",
                        value = "${stats.activeProviders}",
                        subtitle = "${stats.pendingApprovals} awaiting review",
                        color = SafetyAmber40,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Total Bookings",
                        value = "${stats.todayBookings}",
                        subtitle = "${stats.pendingBookings} in progress",
                        color = HighDensityPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Admin Sub-Navigation
        item {
            val tabs = listOf("Providers (${stats.pendingApprovals})", "Dispatch", "Warranty (${warrantyClaims.size})", "Pricing", "Audit Logs")
            PrimaryTabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedAdminTab == index,
                        onClick = { selectedAdminTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedAdminTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            }
        }

        // 3. Tab Contents
        when (selectedAdminTab) {
            // TAB 0: PROVIDER ONBOARDING QUEUE
            0 -> {
                item {
                    Text("Provider Verification Queue", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                val pendingProviders = providers.filter { it.verificationStatus == VerificationStatus.PENDING }
                if (pendingProviders.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                            border = BorderStroke(1.dp, HighDensityOutlineVariant)
                        ) {
                            Text(
                                text = "All provider applications have been reviewed!",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(pendingProviders) { p ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, HighDensityOutlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(p.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(p.businessName, style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText))
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = SafetyAmber40.copy(alpha = 0.2f)) {
                                        Text("Pending Review", color = Color(0xFF78350F), fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                Text("Skills: ${p.skills}", fontSize = 12.sp, color = HighDensityMutedText)
                                Text("Experience: ${p.experienceYears} Years • Area: ${p.serviceAreas}", fontSize = 12.sp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { onRejectProvider(p.id) },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Reject")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { onApproveProvider(p.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald40),
                                        modifier = Modifier.testTag("approve_provider_${p.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve Partner")
                                    }
                                }
                            }
                        }
                    }
                }

                // Also list approved providers
                item {
                    Text("Verified Active Partners (${providers.count { it.verificationStatus == VerificationStatus.APPROVED }})", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                }
                items(providers.filter { it.verificationStatus == VerificationStatus.APPROVED }) { p ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                        border = BorderStroke(1.dp, HighDensityOutlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${p.businessName} • Jobs: ${p.totalJobsCompleted} • ★ ${p.ratingAvg}", fontSize = 11.sp, color = HighDensityMutedText)
                            }
                            Surface(shape = RoundedCornerShape(12.dp), color = Emerald40.copy(alpha = 0.15f)) {
                                Text("Active", color = Emerald40, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            // TAB 1: DISPATCH / BOOKINGS
            1 -> {
                item {
                    Text("Live Bookings Dispatcher", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                items(bookings) { b ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, HighDensityOutlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(b.bookingCode, fontWeight = FontWeight.Black, color = HighDensityPrimary)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (b.status) {
                                        BookingStatus.COMPLETED -> Emerald40.copy(alpha = 0.15f)
                                        BookingStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                                        else -> SafetyAmber40.copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        text = b.status.label,
                                        color = when (b.status) {
                                            BookingStatus.COMPLETED -> Emerald40
                                            BookingStatus.CANCELLED -> MaterialTheme.colorScheme.error
                                            else -> Color(0xFF78350F)
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(b.serviceName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Customer: ${b.customerName} • Payout: ₹${b.finalAmount}", fontSize = 12.sp)
                            Text("Assigned: ${b.providerName ?: "Unassigned (Pending Dispatch)"}", fontSize = 12.sp, color = HighDensityPrimary, fontWeight = FontWeight.SemiBold)

                            if (b.status == BookingStatus.SEARCHING_PROVIDER) {
                                val approved = providers.firstOrNull { it.verificationStatus == VerificationStatus.APPROVED }
                                if (approved != null) {
                                    Button(
                                        onClick = { onAssignProvider(b.id, approved.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Dispatch to ${approved.name}")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: WARRANTY & REWORK CLAIMS
            2 -> {
                item {
                    Text("Customer Guarantee & Rework Claims (${warrantyClaims.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                if (warrantyClaims.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                            border = BorderStroke(1.dp, HighDensityOutlineVariant)
                        ) {
                            Text(
                                text = "No warranty claims submitted yet. All completed jobs are within quality SLAs.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(warrantyClaims) { claim ->
                        val statusColor = when (claim.status) {
                            "APPROVED" -> Emerald40
                            "RESOLVED" -> Emerald40
                            "REWORK_SCHEDULED" -> HighDensityPrimary
                            else -> SafetyAmber40
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, HighDensityOutlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Claim #${claim.id} • Order #${claim.bookingCode}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = HighDensityPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = statusColor.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, statusColor)
                                    ) {
                                        Text(
                                            text = claim.status,
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(claim.serviceName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Customer: ${claim.customerName} • ${claim.createdAt}", fontSize = 12.sp, color = HighDensityMutedText)

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = HighDensitySurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Reported Issue: ${claim.reason}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text(claim.description, fontSize = 11.sp, color = HighDensityMutedText)
                                        Text("Preferred Re-visit: ${claim.preferredDate}", fontSize = 11.sp, color = HighDensityPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (claim.status == "PENDING_REVIEW") {
                                        Button(
                                            onClick = { onUpdateWarrantyClaimStatus(claim.id, "APPROVED") },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Emerald40),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve Free Rework", fontSize = 11.sp)
                                        }
                                    } else if (claim.status == "APPROVED" || claim.status == "REWORK_SCHEDULED") {
                                        Button(
                                            onClick = { onUpdateWarrantyClaimStatus(claim.id, "RESOLVED") },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Mark Resolved", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 3: PRICING & SERVICES
            3 -> {
                item {
                    Text("Services & Dynamic Pricing Management", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                items(services) { s ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, HighDensityOutlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Category: ${s.categoryName} • Warranty: ${s.warrantyDays}d", fontSize = 11.sp, color = HighDensityMutedText)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Base: ₹${s.basePrice.toInt()}", fontWeight = FontWeight.Bold)
                                    if (s.discountPrice != null) {
                                        Text("Offer: ₹${s.discountPrice.toInt()}", fontWeight = FontWeight.Bold, color = HighDensityPrimary)
                                    }
                                }
                            }

                            Button(
                                onClick = { editingService = s },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                            ) {
                                Text("Edit Price", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // TAB 4: AUDIT LOGS
            4 -> {
                item {
                    Text("System Operations Audit Log", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                items(auditLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                        border = BorderStroke(1.dp, HighDensityOutlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HighDensityPrimary)
                                Text(log.timestamp, fontSize = 10.sp, color = HighDensityMutedText)
                            }
                            Text(log.details, fontSize = 11.sp)
                            Text("Admin: ${log.adminName} • Entity: ${log.entity} #${log.entityId}", fontSize = 9.sp, color = HighDensityMutedText)
                        }
                    }
                }
            }
        }
    }

    // Price Edit Modal
    if (editingService != null) {
        val s = editingService!!
        var baseInput by remember { mutableStateOf(s.basePrice.toInt().toString()) }
        var discountInput by remember { mutableStateOf(s.discountPrice?.toInt()?.toString() ?: "") }

        Dialog(onDismissRequest = { editingService = null }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Update Pricing: ${s.name}", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = baseInput,
                        onValueChange = { baseInput = it },
                        label = { Text("Base Price (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = discountInput,
                        onValueChange = { discountInput = it },
                        label = { Text("Discounted Offer Price (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { editingService = null }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val bPrice = baseInput.toDoubleOrNull() ?: s.basePrice
                                val dPrice = discountInput.toDoubleOrNull()
                                onUpdatePrice(s.id, bPrice, dPrice)
                                editingService = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                        ) {
                            Text("Save Price")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, HighDensityOutlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = HighDensityMutedText,
                    fontSize = 10.sp
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = color
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = HighDensityMutedText,
                    fontSize = 10.sp
                )
            )
        }
    }
}
