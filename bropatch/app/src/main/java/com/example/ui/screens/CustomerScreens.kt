package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.models.*
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.*

@Composable
fun CustomerHomeScreen(
    categories: List<ServiceCategory>,
    services: List<HomeService>,
    banners: List<BannerItem>,
    savedAddresses: List<Address>,
    activeBookings: List<Booking>,
    onSelectService: (HomeService) -> Unit,
    onOpenBookingTracker: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var showDiagnosticDialog by remember { mutableStateOf(false) }

    val filteredServices = remember(services, searchQuery, selectedCategoryId) {
        services.filter { service ->
            val matchesCategory = selectedCategoryId == null || service.categoryId == selectedCategoryId
            val matchesSearch = searchQuery.isBlank() ||
                    service.name.contains(searchQuery, ignoreCase = true) ||
                    service.shortDescription.contains(searchQuery, ignoreCase = true) ||
                    service.categoryName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // 1. Search Bar (High Density Design: rounded-full, bg #F3EDF7, border #CAC4D0)
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search for 'AC Repair' or 'Plumbing'...",
                        color = HighDensityMutedText,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = HighDensityMutedText,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = HighDensityMutedText)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("service_search_input"),
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = HighDensitySurfaceVariant,
                    unfocusedContainerColor = HighDensitySurfaceVariant,
                    focusedBorderColor = HighDensityPrimary,
                    unfocusedBorderColor = HighDensityOutline
                )
            )
        }

        // 2. Categories Grid / High Density Icons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATEGORIES",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = HighDensityText
                        )
                    )
                    Text(
                        text = if (selectedCategoryId != null) "Show All" else "View All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = HighDensityPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable {
                            selectedCategoryId = null
                        }
                    )
                }

                // High Density 4-column Category Icon Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categories.take(4).forEach { category ->
                        val isSelected = selectedCategoryId == category.id
                        CategoryGridItem(
                            category = category,
                            isSelected = isSelected,
                            onClick = {
                                selectedCategoryId = if (isSelected) null else category.id
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // If more categories exist, allow horizontal row scroll for remaining
                if (categories.size > 4) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 4.dp)
                    ) {
                        items(categories.drop(4)) { category ->
                            val isSelected = selectedCategoryId == category.id
                            CategoryChip(
                                category = category,
                                isSelected = isSelected,
                                onClick = {
                                    selectedCategoryId = if (isSelected) null else category.id
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. Active Booking Highlight Card (High Density Theme)
        val ongoingBooking = activeBookings.firstOrNull { it.status != BookingStatus.COMPLETED && it.status != BookingStatus.CANCELLED }
        if (ongoingBooking != null) {
            item {
                ActiveBookingBanner(
                    booking = ongoingBooking,
                    onClick = { onOpenBookingTracker(ongoingBooking) }
                )
            }
        }

        // 4. AI Smart Problem Diagnostic & Cost Estimator Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HighDensityPrimaryContainer),
                border = BorderStroke(1.dp, HighDensityOutlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDiagnosticDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(HighDensityPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Diagnostics",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Smart Problem Diagnostics",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityOnPrimaryContainer
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = HighDensityAccentLight
                            ) {
                                Text(
                                    text = "AI ESTIMATOR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = HighDensityAccentDarkText
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Unsure what's broken? Describe the leak, spark, or noise for instant cause & cost estimate.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = HighDensityText,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Diagnostics",
                        tint = HighDensityOnPrimaryContainer
                    )
                }
            }
        }

        // 5. Promotional Offers Carousel
        if (banners.isNotEmpty() && searchQuery.isEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FEATURED OFFERS & GUARANTEES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = HighDensityMutedText
                        )
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        items(banners) { banner ->
                            PromoBannerCard(banner = banner)
                        }
                    }
                }
            }
        }

        // 6. Services Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategoryId != null) {
                        categories.find { it.id == selectedCategoryId }?.name?.uppercase() ?: "SERVICES"
                    } else "POPULAR HOME SERVICES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = HighDensityMutedText
                    )
                )
                Text(
                    text = "${filteredServices.size} available",
                    style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText)
                )
            }
        }

        // Services Cards
        items(filteredServices) { service ->
            ServiceCard(
                service = service,
                onBookClick = { onSelectService(service) }
            )
        }
    }

    if (showDiagnosticDialog) {
        SmartDiagnosticDialog(
            services = services,
            onDismiss = { showDiagnosticDialog = false },
            onSelectServiceForBooking = { service ->
                showDiagnosticDialog = false
                onSelectService(service)
            }
        )
    }
}

@Composable
fun CategoryGridItem(
    category: ServiceCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp)
            .testTag("category_grid_${category.slug}")
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isSelected) HighDensityPrimary else HighDensityPrimaryContainer)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) HighDensityOnPrimaryContainer else Color.Transparent,
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getCategoryIcon(category.icon),
                contentDescription = category.name,
                tint = if (isSelected) Color.White else HighDensityOnPrimaryContainer,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                color = if (isSelected) HighDensityPrimary else HighDensityText,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ActiveBookingBanner(
    booking: Booking,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // High Density Section Header & Rose Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACTIVE BOOKING",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = HighDensityText
                )
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HighDensityTertiaryContainer
            ) {
                Text(
                    text = booking.status.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = HighDensityOnTertiaryContainer,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // High Density Dark Highlight Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .testTag("active_booking_banner"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityDarkCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top row: Service & ETA Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = booking.serviceName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "ID: ${booking.bookingCode} • SQL-REST-SYNC",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HighDensityOutline,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HighDensityDarkCardBadge
                    ) {
                        Text(
                            text = "8 min away",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Bottom Provider Glass Card & Quick Message Action
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityPrimary)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (booking.providerName ?: "R").take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = booking.providerName ?: "Rajesh Kumar",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = SafetyAmber40,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "4.9",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                                Text(
                                    text = "CERTIFIED BROPATCH PRO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = HighDensityOutlineVariant,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }

                        // Action Buttons: Lavender Message & Call
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onClick,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HighDensityAccentLight,
                                    contentColor = HighDensityAccentDarkText
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Message", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = onClick,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(36.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromoBannerCard(banner: BannerItem) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(135.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = banner.imageUrl,
                contentDescription = banner.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0F172A).copy(alpha = 0.92f),
                                Color(0xFF0F172A).copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (banner.badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SafetyAmber40
                    ) {
                        Text(
                            text = banner.badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF78350F),
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = banner.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = banner.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: ServiceCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) RoyalBlue40 else MaterialTheme.colorScheme.surface,
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("category_chip_${category.slug}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = getCategoryIcon(category.icon),
                contentDescription = category.name,
                tint = if (isSelected) Color.White else RoyalBlue40,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun ServiceCard(
    service: HomeService,
    onBookClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("service_card_${service.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service Image
            AsyncImage(
                model = service.imageUrl,
                contentDescription = service.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            // Service Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Category & Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = service.categoryName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue40,
                            fontSize = 10.sp
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = SafetyAmber40,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${service.ratingAvg} (${service.totalReviews})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = service.shortDescription,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Price & Book CTA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val currentPrice = service.discountPrice ?: service.basePrice
                        Text(
                            text = "₹${currentPrice.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = RoyalBlue40
                            )
                        )
                        if (service.discountPrice != null) {
                            Text(
                                text = "₹${service.basePrice.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Button(
                        onClick = onBookClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue40),
                        modifier = Modifier.testTag("book_button_${service.id}")
                    ) {
                        Text(
                            text = "Book Now",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerBookingDialog(
    service: HomeService,
    savedAddresses: List<Address>,
    onDismiss: () -> Unit,
    onConfirmBooking: (
        address: Address,
        date: String,
        timeSlot: String,
        problemDescription: String,
        couponCode: String?,
        paymentMethod: PaymentMethod
    ) -> Unit
) {
    var selectedAddress by remember { mutableStateOf(savedAddresses.firstOrNull { it.isDefault } ?: savedAddresses.first()) }
    var selectedDate by remember { mutableStateOf("Today, Aug 16") }
    var selectedTimeSlot by remember { mutableStateOf("10:00 AM - 12:00 PM") }
    var problemNotes by remember { mutableStateOf("") }
    var couponInput by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf<String?>(null) }
    var discountAmount by remember { mutableStateOf(0.0) }
    var couponMessage by remember { mutableStateOf<String?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.RAZORPAY) }

    val basePrice = service.discountPrice ?: service.basePrice
    val subtotal = maxOf(0.0, basePrice - discountAmount)
    val gstTax = Math.round(subtotal * 0.18 * 100.0) / 100.0
    val totalAmount = Math.round((subtotal + gstTax) * 100.0) / 100.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Book Service",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = service.name,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Payable",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = "₹${totalAmount}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = RoyalBlue40
                                    )
                                )
                            }

                            Button(
                                onClick = {
                                    onConfirmBooking(
                                        selectedAddress,
                                        selectedDate,
                                        selectedTimeSlot,
                                        problemNotes.ifBlank { "Standard service diagnosis and repair requested." },
                                        appliedCoupon,
                                        selectedPaymentMethod
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue40),
                                modifier = Modifier.testTag("confirm_booking_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedPaymentMethod == PaymentMethod.RAZORPAY) "Pay Online & Book" else "Confirm with COD",
                                    fontWeight = FontWeight.Bold
                                )
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
                    // 1. Service Summary Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = service.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Column {
                                    Text(
                                        text = service.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Duration: ~${service.estimatedDurationMins} mins • ${service.warrantyDays}-day warranty",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Address Selector
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "1. Service Address",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            savedAddresses.forEach { addr ->
                                val isChosen = selectedAddress.id == addr.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isChosen) RoyalBlue40.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isChosen) RoyalBlue40 else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAddress = addr }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        RadioButton(
                                            selected = isChosen,
                                            onClick = { selectedAddress = addr }
                                        )
                                        Column {
                                            Text(
                                                text = "${addr.label} - ${addr.apartmentUnit ?: ""}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "${addr.streetAddress}, ${addr.city}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Date & Time Selection
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "2. Preferred Schedule",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            val dates = listOf("Today, Aug 16", "Tomorrow, Aug 17", "Mon, Aug 18")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(dates) { d ->
                                    val isDateSelected = selectedDate == d
                                    FilterChip(
                                        selected = isDateSelected,
                                        onClick = { selectedDate = d },
                                        label = { Text(d, fontWeight = if (isDateSelected) FontWeight.Bold else FontWeight.Normal) }
                                    )
                                }
                            }

                            val slots = listOf("09:00 AM - 11:00 AM", "11:00 AM - 01:00 PM", "02:00 PM - 04:00 PM", "04:00 PM - 06:00 PM")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(slots) { s ->
                                    val isSlotSelected = selectedTimeSlot == s
                                    FilterChip(
                                        selected = isSlotSelected,
                                        onClick = { selectedTimeSlot = s },
                                        label = { Text(s, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Problem Notes
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "3. Problem Description (Optional)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            OutlinedTextField(
                                value = problemNotes,
                                onValueChange = { problemNotes = it },
                                placeholder = { Text("E.g. Leaking pipe under sink, urgent fix needed...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 3,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // 5. Coupon Application
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "4. Offers & Promo Code",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = couponInput,
                                    onValueChange = { couponInput = it.uppercase() },
                                    placeholder = { Text("Try BROPATCH50 or WELCOME100") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("coupon_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Button(
                                    onClick = {
                                        if (couponInput.trim().equals("BROPATCH50", ignoreCase = true)) {
                                            discountAmount = 79.80
                                            appliedCoupon = "BROPATCH50"
                                            couponMessage = "20% discount applied (-₹79.80)"
                                        } else if (couponInput.trim().equals("WELCOME100", ignoreCase = true)) {
                                            discountAmount = 100.0
                                            appliedCoupon = "WELCOME100"
                                            couponMessage = "Flat ₹100 discount applied (-₹100.00)"
                                        } else {
                                            couponMessage = "Invalid coupon code"
                                            discountAmount = 0.0
                                            appliedCoupon = null
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber40)
                                ) {
                                    Text("Apply", color = Color(0xFF78350F), fontWeight = FontWeight.Bold)
                                }
                            }
                            if (couponMessage != null) {
                                Text(
                                    text = couponMessage!!,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (appliedCoupon != null) Emerald40 else MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // 6. Payment Method Selection
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "5. Payment Method",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPaymentMethod = PaymentMethod.RAZORPAY },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedPaymentMethod == PaymentMethod.RAZORPAY) RoyalBlue40.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.5.dp, if (selectedPaymentMethod == PaymentMethod.RAZORPAY) RoyalBlue40 else MaterialTheme.colorScheme.outline)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = RoyalBlue40)
                                        Text("Razorpay / UPI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Instant & Secure", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPaymentMethod = PaymentMethod.COD },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedPaymentMethod == PaymentMethod.COD) RoyalBlue40.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.5.dp, if (selectedPaymentMethod == PaymentMethod.COD) RoyalBlue40 else MaterialTheme.colorScheme.outline)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, tint = SafetyAmber40)
                                        Text("Cash On Delivery", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Pay after repair", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    // 7. Price Breakdown
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Price Breakdown", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Base Rate", style = MaterialTheme.typography.bodySmall)
                                    Text("₹$basePrice", style = MaterialTheme.typography.bodySmall)
                                }
                                if (discountAmount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Coupon Discount", style = MaterialTheme.typography.bodySmall.copy(color = Emerald40))
                                        Text("-₹$discountAmount", style = MaterialTheme.typography.bodySmall.copy(color = Emerald40))
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("GST Taxes (18%)", style = MaterialTheme.typography.bodySmall)
                                    Text("₹$gstTax", style = MaterialTheme.typography.bodySmall)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Amount", fontWeight = FontWeight.Bold)
                                    Text("₹$totalAmount", fontWeight = FontWeight.Bold, color = RoyalBlue40)
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
fun SmartDiagnosticDialog(
    services: List<HomeService>,
    onDismiss: () -> Unit,
    onSelectServiceForBooking: (HomeService) -> Unit
) {
    var problemInput by remember { mutableStateOf("") }
    var diagnosticResult by remember { mutableStateOf<DiagnosticInsight?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    val quickSymptomChips = listOf(
        "AC blowing warm air",
        "Water leak under sink",
        "Switchboard sparking & trip",
        "Geyser not heating water",
        "RO water low flow / taste",
        "Door lock jammed"
    )

    fun runDiagnostics(query: String) {
        if (query.isBlank()) return
        isAnalyzing = true
        val q = query.lowercase().trim()
        val result = when {
            q.contains("ac") || q.contains("cool") || q.contains("air") || q.contains("warm") -> {
                DiagnosticInsight(
                    problemTitle = "AC Cooling Coil / Refrigerant Fault",
                    categoryName = "AC & Appliances",
                    matchedServiceId = 5L,
                    matchedServiceName = "Split AC Deep Foam Jet Service",
                    severityLevel = "Medium",
                    likelyCause = "Blocked cooling fins or low gas pressure preventing heat exchange.",
                    diySafetyTip = "Turn off AC unit to prevent compressor seizure. Do not run continuously.",
                    estimatedCostMin = 499.0,
                    estimatedCostMax = 799.0
                )
            }
            q.contains("leak") || q.contains("sink") || q.contains("pipe") || q.contains("tap") || q.contains("drain") -> {
                DiagnosticInsight(
                    problemTitle = "Plumbing Leakage & Gasket Degradation",
                    categoryName = "Plumbing Services",
                    matchedServiceId = 1L,
                    matchedServiceName = "Pipe Leakage & Drainage Unblock",
                    severityLevel = "Medium",
                    likelyCause = "Worn washer seal, cracked PVC joint, or blockage in bottle trap.",
                    diySafetyTip = "Close the local angle stopcock under the basin immediately.",
                    estimatedCostMin = 299.0,
                    estimatedCostMax = 499.0
                )
            }
            q.contains("spark") || q.contains("switch") || q.contains("trip") || q.contains("shock") || q.contains("fuse") -> {
                DiagnosticInsight(
                    problemTitle = "Electrical Short Circuit & Socket Hazard",
                    categoryName = "Electrical Works",
                    matchedServiceId = 3L,
                    matchedServiceName = "Switchboard & Short Circuit Fix",
                    severityLevel = "High Hazard",
                    likelyCause = "Loose connection causing electric arching and thermal carbon build-up.",
                    diySafetyTip = "DANGER: Immediately trip the MCB distribution switch off. Do not touch.",
                    estimatedCostMin = 329.0,
                    estimatedCostMax = 549.0
                )
            }
            q.contains("geyser") || q.contains("heater") || q.contains("hot") -> {
                DiagnosticInsight(
                    problemTitle = "Water Heater Heating Element / Thermostat",
                    categoryName = "AC & Appliances",
                    matchedServiceId = 9L,
                    matchedServiceName = "Storage Water Geyser Repair & Element Fix",
                    severityLevel = "High Hazard",
                    likelyCause = "Hard water mineral scaling on heating coil or tripped thermal cutout switch.",
                    diySafetyTip = "Disconnect the 16A power cord before touching water outlet valves.",
                    estimatedCostMin = 399.0,
                    estimatedCostMax = 699.0
                )
            }
            q.contains("ro") || q.contains("purifier") || q.contains("water") || q.contains("taste") || q.contains("filter") -> {
                DiagnosticInsight(
                    problemTitle = "RO Purifier Membrane Choke",
                    categoryName = "Plumbing Services",
                    matchedServiceId = 10L,
                    matchedServiceName = "RO Water Purifier Membrane & Filter Service",
                    severityLevel = "Low",
                    likelyCause = "Exhausted sediment pre-filter and clogged active carbon cartridge.",
                    diySafetyTip = "Shut the inlet bypass adapter valve if you notice low pressure.",
                    estimatedCostMin = 499.0,
                    estimatedCostMax = 799.0
                )
            }
            q.contains("door") || q.contains("lock") || q.contains("jammed") || q.contains("hinge") -> {
                DiagnosticInsight(
                    problemTitle = "Jammed Mortise Lock & Striker Misalignment",
                    categoryName = "Carpentry & Locks",
                    matchedServiceId = 7L,
                    matchedServiceName = "Door Lock & Hinge Realignment",
                    severityLevel = "Low",
                    likelyCause = "Door sag due to loose upper hinge screw or moisture expansion.",
                    diySafetyTip = "Do not force or bend key inside the brass cylinder.",
                    estimatedCostMin = 349.0,
                    estimatedCostMax = 599.0
                )
            }
            else -> {
                DiagnosticInsight(
                    problemTitle = "General Home Maintenance Issue",
                    categoryName = "Home Repairs",
                    matchedServiceId = 1L,
                    matchedServiceName = "Pipe Leakage & Drainage Unblock",
                    severityLevel = "Low",
                    likelyCause = "Appliance / fitting wear requiring verified technician inspection.",
                    diySafetyTip = "Keep the repair area clear and dry.",
                    estimatedCostMin = 299.0,
                    estimatedCostMax = 599.0
                )
            }
        }
        diagnosticResult = result
        isAnalyzing = false
    }

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
                // Header
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(HighDensityPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = HighDensityOnPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Problem Diagnostics",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "AI Cause & Repair Cost Estimator",
                                style = MaterialTheme.typography.bodySmall.copy(color = HighDensityMutedText)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = HighDensityOutlineVariant)

                // Input Box
                OutlinedTextField(
                    value = problemInput,
                    onValueChange = { problemInput = it },
                    placeholder = { Text("e.g. AC indoor unit leaking water, or switch sparking...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = { runDiagnostics(problemInput) },
                            enabled = problemInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Analyze", tint = if (problemInput.isNotBlank()) HighDensityPrimary else HighDensityMutedText)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = HighDensitySurfaceVariant,
                        unfocusedContainerColor = HighDensitySurfaceVariant
                    )
                )

                // Quick Symptoms
                Text(
                    text = "COMMON HOUSEHOLD ISSUES:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = HighDensityMutedText,
                        fontSize = 10.sp
                    )
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickSymptomChips) { chip ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = HighDensitySurfaceVariant,
                            border = BorderStroke(1.dp, HighDensityOutlineVariant),
                            modifier = Modifier.clickable {
                                problemInput = chip
                                runDiagnostics(chip)
                            }
                        ) {
                            Text(
                                text = chip,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = HighDensityText
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Diagnostic Result Card
                if (diagnosticResult != null) {
                    val result = diagnosticResult!!
                    val severityColor = when (result.severityLevel) {
                        "High Hazard" -> Crimson40
                        "Medium" -> SafetyAmber40
                        else -> Emerald40
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                        border = BorderStroke(1.dp, HighDensityOutlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = result.problemTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = HighDensityText
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = severityColor.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, severityColor)
                                ) {
                                    Text(
                                        text = result.severityLevel.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = severityColor,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Likely Cause
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "LIKELY CAUSE:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HighDensityMutedText
                                    )
                                )
                                Text(
                                    text = result.likelyCause,
                                    style = MaterialTheme.typography.bodySmall.copy(color = HighDensityText)
                                )
                            }

                            // DIY Safety Tip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, HighDensityOutlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Emerald40, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = result.diySafetyTip,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = HighDensityText
                                        )
                                    )
                                }
                            }

                            // Estimated Cost & Book Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ESTIMATED REPAIR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = HighDensityMutedText
                                        )
                                    )
                                    Text(
                                        text = "₹${result.estimatedCostMin.toInt()} - ₹${result.estimatedCostMax.toInt()}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = HighDensityPrimary
                                        )
                                    )
                                }

                                Button(
                                    onClick = {
                                        val matched = services.find { it.id == result.matchedServiceId }
                                            ?: services.firstOrNull()
                                        if (matched != null) {
                                            onSelectServiceForBooking(matched)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Book Fix Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
