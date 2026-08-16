package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.data.repository.BropatchRepository
import com.example.ui.components.AppRoleHeader
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BropatchApp()
            }
        }
    }
}

@Composable
fun BropatchApp() {
    val repository = remember { BropatchRepository.getInstance() }

    val currentRole by repository.currentRole.collectAsStateWithLifecycle()
    val categories by repository.categories.collectAsStateWithLifecycle()
    val services by repository.services.collectAsStateWithLifecycle()
    val banners by repository.banners.collectAsStateWithLifecycle()
    val savedAddresses by repository.savedAddresses.collectAsStateWithLifecycle()
    val bookings by repository.bookings.collectAsStateWithLifecycle()
    val providers by repository.providers.collectAsStateWithLifecycle()
    val chatMessagesMap by repository.chatMessages.collectAsStateWithLifecycle()
    val auditLogs by repository.auditLogs.collectAsStateWithLifecycle()
    val backendUrl by repository.backendUrl.collectAsStateWithLifecycle()
    val walletBalance by repository.walletBalance.collectAsStateWithLifecycle()
    val coupons by repository.coupons.collectAsStateWithLifecycle()
    val warrantyClaims by repository.warrantyClaims.collectAsStateWithLifecycle()

    var selectedServiceForBooking by remember { mutableStateOf<HomeService?>(null) }
    var activeTrackingBooking by remember { mutableStateOf<Booking?>(null) }
    var currentCustomerTab by remember { mutableStateOf(CustomerTab.HOME) }

    var invoiceDialogBooking by remember { mutableStateOf<Booking?>(null) }
    var ratingDialogBooking by remember { mutableStateOf<Booking?>(null) }
    var cancelDialogBooking by remember { mutableStateOf<Booking?>(null) }
    var rescheduleDialogBooking by remember { mutableStateOf<Booking?>(null) }
    var warrantyClaimBooking by remember { mutableStateOf<Booking?>(null) }
    var showAddAddressDialog by remember { mutableStateOf(false) }

    // Keep active tracking booking updated with repository changes
    LaunchedEffect(bookings) {
        if (activeTrackingBooking != null) {
            val updated = bookings.find { it.id == activeTrackingBooking!!.id }
            if (updated != null) {
                activeTrackingBooking = updated
            }
        }
    }

    val activeCount = bookings.count { it.status != BookingStatus.COMPLETED && it.status != BookingStatus.CANCELLED }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppRoleHeader(
                currentRole = currentRole,
                onRoleSelected = { repository.switchRole(it) },
                activeBookingsCount = activeCount
            )
        },
        bottomBar = {
            if (currentRole == UserRole.CUSTOMER) {
                CustomerBottomBar(
                    currentTab = currentCustomerTab,
                    onTabSelected = { currentCustomerTab = it },
                    activeBookingsCount = activeCount
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                UserRole.CUSTOMER -> {
                    when (currentCustomerTab) {
                        CustomerTab.HOME -> {
                            CustomerHomeScreen(
                                categories = categories,
                                services = services,
                                banners = banners,
                                savedAddresses = savedAddresses,
                                activeBookings = bookings,
                                onSelectService = { selectedServiceForBooking = it },
                                onOpenBookingTracker = { activeTrackingBooking = it }
                            )
                        }

                        CustomerTab.BOOKINGS -> {
                            CustomerBookingsTabScreen(
                                bookings = bookings,
                                onOpenTracker = { activeTrackingBooking = it },
                                onCancelBooking = { cancelDialogBooking = it },
                                onRescheduleBooking = { rescheduleDialogBooking = it }
                            )
                        }

                        CustomerTab.HISTORY -> {
                            CustomerHistoryTabScreen(
                                bookings = bookings,
                                onOpenInvoice = { invoiceDialogBooking = it },
                                onRateBooking = { ratingDialogBooking = it },
                                onRebook = { b ->
                                    val matchService = services.find { it.id == b.serviceId } ?: services.first()
                                    selectedServiceForBooking = matchService
                                },
                                onClaimWarranty = { warrantyClaimBooking = it }
                            )
                        }

                        CustomerTab.PROFILE -> {
                            CustomerProfileTabScreen(
                                savedAddresses = savedAddresses,
                                walletBalance = walletBalance,
                                coupons = coupons,
                                warrantyClaims = warrantyClaims,
                                onAddAddress = { showAddAddressDialog = true },
                                onSetDefaultAddress = { repository.setDefaultAddress(it) },
                                onDeleteAddress = { repository.deleteAddress(it) },
                                onTopUpWallet = { amt -> repository.topUpWallet(amt) },
                                onRegisterAsPartner = { name, phone, bName, skills, exp, areas ->
                                    repository.registerProvider(name, phone, bName, skills, exp, areas)
                                }
                            )
                        }
                    }
                }

                UserRole.PROVIDER -> {
                    val currentProvider = providers.firstOrNull() ?: ProviderProfile(
                        id = 1L,
                        userId = 2L,
                        name = "Vikram Singh",
                        phone = "+91 98111 22334",
                        businessName = "Vikram QuickFix Plumbing",
                        bio = "Master plumber",
                        experienceYears = 8,
                        skills = "Plumbing, Drainage, Taps",
                        serviceAreas = "South Delhi, Central Delhi",
                        currentLatitude = 28.5380,
                        currentLongitude = 77.2450,
                        verificationStatus = VerificationStatus.APPROVED,
                        isAvailable = true,
                        isOnline = true,
                        ratingAvg = 4.92,
                        totalJobsCompleted = 148,
                        totalEarnings = 48200.0,
                        pendingPayoutBalance = 3200.0,
                        codPendingAmount = 450.0,
                        creditBalance = 2400.0,
                        documentUploaded = true
                    )

                    ProviderPartnerScreen(
                        provider = currentProvider,
                        bookings = bookings,
                        onAdvanceStatus = { bId, nextStatus, note ->
                            repository.updateBookingStatus(bId, nextStatus, currentProvider.name, note)
                        },
                        onOpenChat = { activeTrackingBooking = it },
                        onRequestPayout = { amt ->
                            repository.requestProviderPayout(currentProvider.id, amt)
                        }
                    )
                }

                UserRole.ADMIN -> {
                    AdminConsoleScreen(
                        stats = repository.getAdminStats(),
                        providers = providers,
                        bookings = bookings,
                        services = services,
                        auditLogs = auditLogs,
                        warrantyClaims = warrantyClaims,
                        backendUrl = backendUrl,
                        onApproveProvider = { repository.approveProvider(it) },
                        onRejectProvider = { repository.rejectProvider(it) },
                        onAssignProvider = { bId, pId -> repository.assignProviderToBooking(bId, pId) },
                        onUpdatePrice = { sId, base, disc -> repository.updateServicePrice(sId, base, disc) },
                        onUpdateWarrantyClaimStatus = { id, status -> repository.updateWarrantyClaimStatus(id, status) },
                        onUpdateBackendUrl = { repository.setBackendUrl(it) }
                    )
                }
            }
        }
    }

    // Customer Booking Flow Dialog
    if (selectedServiceForBooking != null) {
        CustomerBookingDialog(
            service = selectedServiceForBooking!!,
            savedAddresses = savedAddresses,
            onDismiss = { selectedServiceForBooking = null },
            onConfirmBooking = { address, date, timeSlot, notes, coupon, paymentMethod ->
                val newBooking = repository.createBooking(
                    service = selectedServiceForBooking!!,
                    address = address,
                    date = date,
                    timeSlot = timeSlot,
                    problemDescription = notes,
                    couponCode = coupon,
                    paymentMethod = paymentMethod
                )
                selectedServiceForBooking = null
                activeTrackingBooking = newBooking
            }
        )
    }

    // Live Booking Tracking Dialog
    if (activeTrackingBooking != null) {
        val currentBookingState = bookings.find { it.id == activeTrackingBooking!!.id } ?: activeTrackingBooking!!
        val chatMessages = chatMessagesMap[currentBookingState.id] ?: emptyList()

        LiveBookingTrackerDialog(
            booking = currentBookingState,
            chatMessages = chatMessages,
            onDismiss = { activeTrackingBooking = null },
            onSendMessage = { text ->
                repository.sendChatMessage(
                    bookingId = currentBookingState.id,
                    senderId = 1L,
                    senderName = "Rahul Sharma",
                    role = "Customer",
                    text = text
                )
            }
        )
    }

    // Invoice Dialog
    if (invoiceDialogBooking != null) {
        InvoiceDialog(
            booking = invoiceDialogBooking!!,
            onDismiss = { invoiceDialogBooking = null }
        )
    }

    // Rating & Review Dialog
    if (ratingDialogBooking != null) {
        RatingDialog(
            booking = ratingDialogBooking!!,
            onDismiss = { ratingDialogBooking = null },
            onSubmit = { rating, feedback ->
                repository.submitRating(ratingDialogBooking!!.id, rating, feedback)
            }
        )
    }

    // Cancel Booking Dialog
    if (cancelDialogBooking != null) {
        CancelBookingDialog(
            booking = cancelDialogBooking!!,
            onDismiss = { cancelDialogBooking = null },
            onConfirmCancel = { reason ->
                repository.cancelBooking(cancelDialogBooking!!.id, reason)
            }
        )
    }

    // Reschedule Booking Dialog
    if (rescheduleDialogBooking != null) {
        RescheduleBookingDialog(
            booking = rescheduleDialogBooking!!,
            onDismiss = { rescheduleDialogBooking = null },
            onConfirmReschedule = { newDate, newSlot ->
                repository.rescheduleBooking(rescheduleDialogBooking!!.id, newDate, newSlot)
            }
        )
    }

    // Add Address Dialog
    if (showAddAddressDialog) {
        AddAddressDialog(
            onDismiss = { showAddAddressDialog = false },
            onSave = { label, street, apt, landmark, isDefault ->
                repository.addAddress(
                    label = label,
                    streetAddress = street,
                    apartmentUnit = apt,
                    landmark = landmark,
                    isDefault = isDefault
                )
            }
        )
    }

    // Warranty Claim Dialog
    if (warrantyClaimBooking != null) {
        val booking = warrantyClaimBooking!!
        WarrantyClaimDialog(
            booking = booking,
            onDismiss = { warrantyClaimBooking = null },
            onSubmitClaim = { reason, desc, date ->
                repository.submitWarrantyClaim(
                    bookingId = booking.id,
                    reason = reason,
                    description = desc,
                    preferredDate = date
                )
                warrantyClaimBooking = null
            }
        )
    }
}
