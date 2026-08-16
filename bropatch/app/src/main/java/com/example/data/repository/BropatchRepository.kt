package com.example.data.repository

import com.example.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BropatchRepository private constructor() {

    companion object {
        @Volatile
        private var instance: BropatchRepository? = null

        fun getInstance(): BropatchRepository {
            return instance ?: synchronized(this) {
                instance ?: BropatchRepository().also { instance = it }
            }
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // 1. Current User / Role State
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _backendUrl = MutableStateFlow("https://api.bropatch.com/api")
    val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

    private val _isServerConnected = MutableStateFlow(true)
    val isServerConnected: StateFlow<Boolean> = _isServerConnected.asStateFlow()

    // 2. Categories & Services
    private val _categories = MutableStateFlow<List<ServiceCategory>>(emptyList())
    val categories: StateFlow<List<ServiceCategory>> = _categories.asStateFlow()

    private val _services = MutableStateFlow<List<HomeService>>(emptyList())
    val services: StateFlow<List<HomeService>> = _services.asStateFlow()

    private val _banners = MutableStateFlow<List<BannerItem>>(emptyList())
    val banners: StateFlow<List<BannerItem>> = _banners.asStateFlow()

    // 3. Saved Addresses
    private val _savedAddresses = MutableStateFlow<List<Address>>(emptyList())
    val savedAddresses: StateFlow<List<Address>> = _savedAddresses.asStateFlow()

    // 4. Bookings
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    // 5. Providers
    private val _providers = MutableStateFlow<List<ProviderProfile>>(emptyList())
    val providers: StateFlow<List<ProviderProfile>> = _providers.asStateFlow()

    // 6. Active Chat Messages map (BookingId -> List<ChatMessage>)
    private val _chatMessages = MutableStateFlow<Map<Long, List<ChatMessage>>>(emptyMap())
    val chatMessages: StateFlow<Map<Long, List<ChatMessage>>> = _chatMessages.asStateFlow()

    // 7. Coupons
    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    // 8. Wallet Balance
    private val _walletBalance = MutableStateFlow(500.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    // 9. Warranty Claims & 30-Day Guarantee
    private val _warrantyClaims = MutableStateFlow<List<WarrantyClaim>>(emptyList())
    val warrantyClaims: StateFlow<List<WarrantyClaim>> = _warrantyClaims.asStateFlow()

    // 10. Audit Logs
    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        // Categories
        val seededCategories = listOf(
            ServiceCategory(
                id = 1,
                name = "Plumbing Services",
                slug = "plumbing",
                icon = "plumbing",
                imageUrl = "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?w=600&auto=format&fit=crop&q=80",
                description = "Pipe repairs, sink blockages, faucet & shower fittings",
                servicesCount = 2
            ),
            ServiceCategory(
                id = 2,
                name = "Electrical Works",
                slug = "electrical",
                icon = "bolt",
                imageUrl = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=600&auto=format&fit=crop&q=80",
                description = "Switchboards, wiring inspection, fans & chandeliers",
                servicesCount = 2
            ),
            ServiceCategory(
                id = 3,
                name = "AC & Appliances",
                slug = "ac-appliance",
                icon = "ac_unit",
                imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600&auto=format&fit=crop&q=80",
                description = "High-pressure foam jet wash, gas charging, fridge fixes",
                servicesCount = 1
            ),
            ServiceCategory(
                id = 4,
                name = "Deep Cleaning",
                slug = "cleaning",
                icon = "cleaning_services",
                imageUrl = "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=600&auto=format&fit=crop&q=80",
                description = "Bathroom de-scaling, kitchen degrease, sanitization",
                servicesCount = 1
            ),
            ServiceCategory(
                id = 5,
                name = "Carpentry & Locks",
                slug = "carpentry",
                icon = "handyman",
                imageUrl = "https://images.unsplash.com/photo-1538688525198-9b88f6f53126?w=600&auto=format&fit=crop&q=80",
                description = "Door locks, hinge realignment, furniture assemble",
                servicesCount = 1
            ),
            ServiceCategory(
                id = 6,
                name = "Wall Painting & Seepage",
                slug = "painting",
                icon = "format_paint",
                imageUrl = "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=600&auto=format&fit=crop&q=80",
                description = "Water dampness patching, wall plastering & touch-ups",
                servicesCount = 1
            )
        )
        _categories.value = seededCategories

        // Services
        val seededServices = listOf(
            HomeService(
                id = 1L,
                categoryId = 1,
                categoryName = "Plumbing Services",
                name = "Pipe Leakage & Drainage Unblock",
                slug = "pipe-leak-drainage",
                shortDescription = "Instant diagnosis & fix for pipe leaks, sink clogs & traps",
                description = "Complete repair for leaking PVC/GI water pipes under counters, bathroom drain lines clearing with pressure rod, washer replacements, and 30-day leak-free guarantee.",
                basePrice = 499.00,
                discountPrice = 399.00,
                estimatedDurationMins = 45,
                imageUrl = "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?w=600&auto=format&fit=crop&q=80",
                warrantyDays = 30,
                ratingAvg = 4.90,
                totalReviews = 142
            ),
            HomeService(
                id = 2L,
                categoryId = 1,
                categoryName = "Plumbing Services",
                name = "Faucet & Shower Fitting",
                slug = "faucet-shower-fitting",
                shortDescription = "Precision installation of bathroom taps, mixers & showers",
                description = "Expert replacement of wall mixer taps, overhead rain showers, angle valves, and health faucets with teflon sealing.",
                basePrice = 349.00,
                discountPrice = 299.00,
                estimatedDurationMins = 40,
                imageUrl = "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=600&auto=format&fit=crop&q=80",
                warrantyDays = 30,
                ratingAvg = 4.85,
                totalReviews = 98
            ),
            HomeService(
                id = 3L,
                categoryId = 2,
                categoryName = "Electrical Works",
                name = "Switchboard & Short Circuit Fix",
                slug = "switchboard-short-circuit",
                shortDescription = "Burnt socket replacement, MCB trip fix & earthing check",
                description = "Certified electrician inspection of main distribution board, earthing voltage verification, and replacement of up to 4 switch modules.",
                basePrice = 399.00,
                discountPrice = 329.00,
                estimatedDurationMins = 50,
                imageUrl = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=600&auto=format&fit=crop&q=80",
                warrantyDays = 30,
                ratingAvg = 4.95,
                totalReviews = 210
            ),
            HomeService(
                id = 4L,
                categoryId = 2,
                categoryName = "Electrical Works",
                name = "Ceiling Fan & Chandelier Install",
                slug = "fan-chandelier-install",
                shortDescription = "High-strength ceiling mount, blade balancing & wiring",
                description = "Vibration-free fan mounting, downrod assembly, regulator setup, and decorative light fixtures installation.",
                basePrice = 299.00,
                discountPrice = 249.00,
                estimatedDurationMins = 35,
                imageUrl = "https://images.unsplash.com/photo-1544717302-de2939b7ef71?w=600&auto=format&fit=crop&q=80",
                warrantyDays = 30,
                ratingAvg = 4.88,
                totalReviews = 142
            ),
            HomeService(
                id = 5L,
                categoryId = 3,
                categoryName = "AC & Appliances",
                name = "Split AC Deep Foam Jet Service",
                slug = "split-ac-foam-service",
                shortDescription = "Deep cooling coil wash, filter sterilize & pressure check",
                description = "Advanced 2x deep foam wash using high-pressure jet removing 99% accumulated mold and grime. Includes gas pressure check & cooling test.",
                basePrice = 799.00,
                discountPrice = 649.00,
                estimatedDurationMins = 60,
                imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600&auto=format&fit=crop&q=80",
                warrantyDays = 60,
                ratingAvg = 4.92,
                totalReviews = 340
            ),
            HomeService(
                id = 6L,
                categoryId = 4,
                categoryName = "Deep Cleaning",
                name = "Intense Bathroom Scrub & De-scaling",
                slug = "bathroom-deep-clean",
                shortDescription = "Limescale removal from tiles, mirrors & floor sanitation",
                description = "Eco-safe de-scaling chemicals for removing stubborn hard water marks from glass partitions, tiles, fittings, and commode sanitization.",
                basePrice = 699.00,
                discountPrice = 549.00,
                estimatedDurationMins = 75,
                imageUrl = "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=600&auto=format&fit=crop&q=80",
                warrantyDays = 15,
                ratingAvg = 4.88,
                totalReviews = 184
            ),
            HomeService(
                id = 7L,
                categoryId = 5,
                categoryName = "Carpentry & Locks",
                name = "Door Lock & Hinge Realignment",
                slug = "door-lock-hinge-repair",
                shortDescription = "Fix jamming doors, install mortise locks & cylinder repair",
                description = "Door plane shaving, heavy duty bearing hinges lubrication, and digital/manual deadbolt lock alignment.",
                basePrice = 449.00,
                discountPrice = 379.00,
                estimatedDurationMins = 45,
                imageUrl = "https://images.unsplash.com/photo-1538688525198-9b88f6f53126?w=600&auto=format&fit=crop&q=80",
                warrantyDays = 45,
                ratingAvg = 4.79,
                totalReviews = 88
            ),
            HomeService(
                id = 8L,
                categoryId = 6,
                categoryName = "Wall Painting & Seepage",
                name = "Water Seepage Wall Patch & Touchup",
                slug = "wall-patch-touchup",
                shortDescription = "Anti-dampness putty coat, scrape flaking paint & emulsion",
                description = "Waterproof barrier coat for damp corners up to 25 sq.ft, putty smoothing, and matching color emulsion coat.",
                basePrice = 899.00,
                discountPrice = 749.00,
                estimatedDurationMins = 90,
                imageUrl = "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=600&auto=format&fit=crop&q=80",
                warrantyDays = 90,
                ratingAvg = 4.91,
                totalReviews = 76
            ),
            HomeService(
                id = 9L,
                categoryId = 3,
                categoryName = "AC & Appliances",
                name = "Storage Water Geyser Repair & Element Fix",
                slug = "geyser-repair-service",
                shortDescription = "Thermostat replacement, heating coil de-scaling & tank leak check",
                description = "Complete inspection of 15L-25L electric water heaters, replacement of faulty heating elements, magnesium anode rod check, and pressure valve calibration.",
                basePrice = 499.00,
                discountPrice = 399.00,
                estimatedDurationMins = 45,
                imageUrl = "https://images.unsplash.com/photo-1585338107529-13afc5f02586?w=600&auto=format&fit=crop&q=80",
                warrantyDays = 30,
                ratingAvg = 4.87,
                totalReviews = 112
            ),
            HomeService(
                id = 10L,
                categoryId = 1,
                categoryName = "Plumbing Services",
                name = "RO Water Purifier Membrane & Filter Service",
                slug = "ro-water-purifier-service",
                shortDescription = "Sediment & carbon filter change, TDS check & sanitization",
                description = "High-pressure pump check, genuine Ro membrane flushing, inline carbon filter replacement, and digital TDS water quality check.",
                basePrice = 599.00,
                discountPrice = 499.00,
                estimatedDurationMins = 40,
                imageUrl = "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?w=600&auto=format&fit=crop&q=80",
                warrantyDays = 45,
                ratingAvg = 4.93,
                totalReviews = 168
            )
        )
        _services.value = seededServices

        // Banners
        _banners.value = listOf(
            BannerItem(
                id = 1,
                title = "Monsoon Moisture Shield",
                subtitle = "Get 25% off Seepage & Dampness Patching",
                ctaText = "Book Shield",
                badgeText = "SEASONAL OFFER",
                imageUrl = "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=800&auto=format&fit=crop&q=80",
                categoryId = 6
            ),
            BannerItem(
                id = 2,
                title = "AC Power Chill Service",
                subtitle = "Deep Foam Jet Wash with 60-Day Guarantee",
                ctaText = "Get 20% Off",
                badgeText = "TOP RATED",
                imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=800&auto=format&fit=crop&q=80",
                categoryId = 3
            ),
            BannerItem(
                id = 3,
                title = "Electric Safety Inspection",
                subtitle = "Prevent short circuits & protect appliances",
                ctaText = "Inspect Now",
                badgeText = "SAFETY FIRST",
                imageUrl = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=800&auto=format&fit=crop&q=80",
                categoryId = 2
            )
        )

        // Coupons
        _coupons.value = listOf(
            Coupon(1, "BROPATCH50", "percentage", 20.0, 400.0, 150.0, "20% off up to ₹150 on orders above ₹400"),
            Coupon(2, "WELCOME100", "fixed", 100.0, 500.0, 100.0, "Flat ₹100 instant cashback on your first service"),
            Coupon(3, "FESTIVE25", "percentage", 25.0, 700.0, 250.0, "25% discount for home repairs above ₹700")
        )

        // Default Saved Addresses
        val defaultAddress = Address(
            id = 1L,
            userId = 1L,
            label = "Home",
            streetAddress = "Flat 402, Sunshine Heights, Outer Ring Road",
            apartmentUnit = "Tower B",
            landmark = "Near Metro Pillar 142",
            city = "New Delhi",
            state = "Delhi",
            postalCode = "110076",
            latitude = 28.5355,
            longitude = 77.2410,
            isDefault = true
        )
        _savedAddresses.value = listOf(
            defaultAddress,
            Address(
                id = 2L,
                userId = 1L,
                label = "Office",
                streetAddress = "Plot 18, Cyber City Phase 2",
                apartmentUnit = "Floor 3",
                landmark = "Opposite Gate 3",
                city = "Gurgaon",
                state = "Haryana",
                postalCode = "122002",
                latitude = 28.4900,
                longitude = 77.0900,
                isDefault = false
            )
        )

        // Seed Providers
        _providers.value = listOf(
            ProviderProfile(
                id = 1L,
                userId = 2L,
                name = "Vikram Singh",
                phone = "+91 98111 22334",
                businessName = "Vikram QuickFix Plumbing",
                bio = "Certified master plumber with 8+ years experience in pipelines, taps, drainage, and mixers.",
                experienceYears = 8,
                skills = "Pipe repairs, Drainage, Faucets, Overheads, Water Heaters",
                serviceAreas = "South Delhi, Central Delhi, Noida",
                currentLatitude = 28.5380,
                currentLongitude = 77.2450,
                verificationStatus = VerificationStatus.APPROVED,
                isAvailable = true,
                isOnline = true,
                ratingAvg = 4.92,
                totalJobsCompleted = 148,
                totalEarnings = 48200.00,
                pendingPayoutBalance = 3200.00,
                codPendingAmount = 450.00,
                creditBalance = 2400.00,
                documentUploaded = true
            ),
            ProviderProfile(
                id = 2L,
                userId = 3L,
                name = "Amit Verma",
                phone = "+91 98222 33445",
                businessName = "Verma Power & Electric Works",
                bio = "Licensed wireman specializing in residential switchboards, MCB distribution & lighting.",
                experienceYears = 6,
                skills = "Switchboards, MCB, Inverters, Fans, Short Circuit",
                serviceAreas = "West Delhi, South Delhi, Gurgaon",
                currentLatitude = 28.5800,
                currentLongitude = 77.2200,
                verificationStatus = VerificationStatus.APPROVED,
                isAvailable = true,
                isOnline = true,
                ratingAvg = 4.88,
                totalJobsCompleted = 112,
                totalEarnings = 36500.00,
                pendingPayoutBalance = 2450.00,
                codPendingAmount = 0.00,
                creditBalance = 1850.00,
                documentUploaded = true
            ),
            ProviderProfile(
                id = 3L,
                userId = 4L,
                name = "Deepak Kumar",
                phone = "+91 98333 44556",
                businessName = "CoolTech HVAC Solutions",
                bio = "AC & refrigeration master technician certified in eco-friendly refrigerants and high pressure jet servicing.",
                experienceYears = 5,
                skills = "Split AC, Window AC, Inverter AC, Gas Charge, Deep Foam Wash",
                serviceAreas = "East Delhi, Noida, Indirapuram, Ghaziabad",
                currentLatitude = 28.6200,
                currentLongitude = 77.2900,
                verificationStatus = VerificationStatus.APPROVED,
                isAvailable = true,
                isOnline = true,
                ratingAvg = 4.95,
                totalJobsCompleted = 230,
                totalEarnings = 78900.00,
                pendingPayoutBalance = 5800.00,
                codPendingAmount = 700.00,
                creditBalance = 3100.00,
                documentUploaded = true
            ),
            ProviderProfile(
                id = 4L,
                userId = 5L,
                name = "Sanjay Patel",
                phone = "+91 98444 55667",
                businessName = "EcoClean Sanitization Hub",
                bio = "Sanitization and deep cleaning expert awaiting onboarding document verification.",
                experienceYears = 3,
                skills = "Deep cleaning, Bathroom sanitation, Kitchen degreasing",
                serviceAreas = "North Delhi, Rohini, Pitampura",
                currentLatitude = 28.7000,
                currentLongitude = 77.1400,
                verificationStatus = VerificationStatus.PENDING,
                isAvailable = false,
                isOnline = false,
                ratingAvg = 5.00,
                totalJobsCompleted = 0,
                totalEarnings = 0.00,
                pendingPayoutBalance = 0.00,
                codPendingAmount = 0.00,
                creditBalance = 1000.00,
                documentUploaded = true
            )
        )

        // Seed Active Bookings
        val activeBooking = Booking(
            id = 1L,
            bookingCode = "BP-2026-8819",
            customerId = 1L,
            customerName = "Rahul Sharma",
            customerPhone = "+91 98765 43210",
            providerId = 1L,
            providerName = "Vikram Singh (Plumbing Pro)",
            providerPhone = "+91 98111 22334",
            serviceId = 1L,
            serviceName = "Pipe Leakage & Drainage Unblock",
            serviceImage = "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?w=600&auto=format&fit=crop&q=80",
            address = defaultAddress,
            status = BookingStatus.PROVIDER_ON_WAY,
            scheduledDate = "Today, Aug 16, 2026",
            scheduledTimeSlot = "10:00 AM - 12:00 PM",
            problemDescription = "Kitchen sink main line is leaking heavily under the counter cabinet and draining slowly.",
            baseAmount = 399.00,
            discountAmount = 79.80,
            taxAmount = 57.45,
            finalAmount = 376.65,
            platformFee = 59.85,
            providerPayoutAmount = 316.80,
            paymentMethod = PaymentMethod.RAZORPAY,
            paymentStatus = "Paid Online (Razorpay)",
            createdAt = "2026-08-16 09:15 AM",
            statusHistory = listOf(
                StatusHistoryItem(null, "Booking Placed", "Rahul Sharma", "Customer placed order", "09:15 AM"),
                StatusHistoryItem("Booking Placed", "Searching Provider", "System", "Smart matching algorithm", "09:16 AM"),
                StatusHistoryItem("Searching Provider", "Provider Assigned", "Operations Dispatch", "Assigned Vikram Singh", "09:18 AM"),
                StatusHistoryItem("Provider Assigned", "Accepted by Pro", "Vikram Singh", "Technician confirmed appointment", "09:20 AM"),
                StatusHistoryItem("Accepted by Pro", "On The Way", "Vikram Singh", "Technician departed for site (ETA 15m)", "09:35 AM")
            )
        )

        // Seed Past Completed Booking (Eligible for 30-Day Guarantee Warranty & Invoices)
        val completedBooking = Booking(
            id = 2L,
            bookingCode = "BP-2026-7412",
            customerId = 1L,
            customerName = "Rahul Sharma",
            customerPhone = "+91 98765 43210",
            providerId = 2L,
            providerName = "Amit Kumar (Master Electrician)",
            providerPhone = "+91 98222 33445",
            serviceId = 3L,
            serviceName = "Switchboard & Short Circuit Fix",
            serviceImage = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=600&auto=format&fit=crop&q=80",
            address = defaultAddress,
            status = BookingStatus.COMPLETED,
            scheduledDate = "Aug 10, 2026",
            scheduledTimeSlot = "02:00 PM - 04:00 PM",
            problemDescription = "Master bedroom AC switch spark and trip fuse replacement.",
            baseAmount = 329.00,
            discountAmount = 50.00,
            taxAmount = 50.22,
            finalAmount = 329.22,
            platformFee = 49.38,
            providerPayoutAmount = 279.84,
            paymentMethod = PaymentMethod.RAZORPAY,
            paymentStatus = "Paid Online (Razorpay)",
            createdAt = "2026-08-10 01:45 PM",
            statusHistory = listOf(
                StatusHistoryItem(null, "Booking Placed", "Rahul Sharma", "Order placed", "01:45 PM"),
                StatusHistoryItem("Booking Placed", "Completed & Verified", "Amit Kumar", "Work verified & tested", "03:40 PM")
            )
        )

        _bookings.value = listOf(activeBooking, completedBooking)

        // Seed Warranty Claims
        _warrantyClaims.value = listOf(
            WarrantyClaim(
                id = 1L,
                bookingId = 2L,
                bookingCode = "BP-2026-7412",
                serviceName = "Switchboard & Short Circuit Fix",
                customerName = "Rahul Sharma",
                customerPhone = "+91 98765 43210",
                reason = "Switchboard indicator blinking intermittently",
                description = "The newly installed modular switch works, but the LED indicator flickers under heavy geyser load.",
                status = "PENDING",
                preferredDate = "Aug 18, 2026 (10:00 AM)",
                createdAt = "Aug 15, 2026 04:10 PM"
            )
        )

        // Seed Chat Messages for Booking 1
        _chatMessages.value = mapOf(
            1L to listOf(
                ChatMessage(1L, 1L, 1L, "Rahul Sharma", "Customer", "Hi Vikram, please enter through Gate 2 near the visitor parking.", "09:22 AM"),
                ChatMessage(2L, 1L, 2L, "Vikram Singh", "Provider", "Sure Rahul! I have taken the tools and replacement pipes. En route now, arriving in 15 mins.", "09:36 AM")
            )
        )

        // Seed Audit Logs
        _auditLogs.value = listOf(
            AuditLogEntry(1L, "Operations Lead", "Approve Provider", "Providers", 1L, "Approved Vikram Singh documents", "08:30 AM"),
            AuditLogEntry(2L, "Operations Lead", "Assign Dispatch", "Bookings", 1L, "Assigned Booking BP-2026-8819 to Vikram Singh", "09:18 AM")
        )
    }

    // Role Switcher
    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    fun setBackendUrl(url: String) {
        _backendUrl.value = url
    }

    // Coupon validation server-side calculation
    fun validateCoupon(code: String, orderAmount: Double): Pair<Boolean, Double> {
        val coupon = _coupons.value.find { it.code.equals(code.trim(), ignoreCase = true) }
            ?: return Pair(false, 0.0)

        if (orderAmount < coupon.minOrderAmount) {
            return Pair(false, 0.0)
        }

        var discount = if (coupon.discountType == "percentage") {
            (orderAmount * coupon.discountValue) / 100.0
        } else {
            coupon.discountValue
        }

        if (coupon.maxDiscountAmount != null && discount > coupon.maxDiscountAmount) {
            discount = coupon.maxDiscountAmount
        }

        return Pair(true, discount)
    }

    // Create Booking
    fun createBooking(
        service: HomeService,
        address: Address,
        date: String,
        timeSlot: String,
        problemDescription: String,
        couponCode: String?,
        paymentMethod: PaymentMethod
    ): Booking {
        val baseAmount = service.discountPrice ?: service.basePrice
        var discount = 0.0
        if (!couponCode.isNullOrBlank()) {
            val (valid, calculatedDiscount) = validateCoupon(couponCode, baseAmount)
            if (valid) discount = calculatedDiscount
        }

        val subtotal = maxOf(0.0, baseAmount - discount)
        val tax = Math.round(subtotal * 0.18 * 100.0) / 100.0 // 18% GST
        val finalAmount = Math.round((subtotal + tax) * 100.0) / 100.0
        val platformFee = Math.round(subtotal * 0.15 * 100.0) / 100.0
        val providerPayout = Math.round((subtotal - platformFee) * 100.0) / 100.0

        // Smart assign available approved provider
        val assignedProvider = _providers.value.firstOrNull { it.verificationStatus == VerificationStatus.APPROVED && it.isAvailable }

        val newBookingId = (_bookings.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val code = "BP-" + (2000 + (10..99).random()) + "-" + (1000..9999).random()
        val initialStatus = if (assignedProvider != null) BookingStatus.PROVIDER_ASSIGNED else BookingStatus.SEARCHING_PROVIDER

        val newBooking = Booking(
            id = newBookingId,
            bookingCode = code,
            customerId = 1L,
            customerName = "Rahul Sharma",
            customerPhone = "+91 98765 43210",
            providerId = assignedProvider?.id,
            providerName = assignedProvider?.name ?: "Searching Partner...",
            providerPhone = assignedProvider?.phone,
            serviceId = service.id,
            serviceName = service.name,
            serviceImage = service.imageUrl,
            address = address,
            status = initialStatus,
            scheduledDate = date,
            scheduledTimeSlot = timeSlot,
            problemDescription = problemDescription,
            baseAmount = baseAmount,
            discountAmount = discount,
            taxAmount = tax,
            finalAmount = finalAmount,
            platformFee = platformFee,
            providerPayoutAmount = providerPayout,
            paymentMethod = paymentMethod,
            paymentStatus = if (paymentMethod == PaymentMethod.RAZORPAY) "Paid Online" else "Pending (COD)",
            createdAt = dateFormat.format(Date()),
            statusHistory = listOf(
                StatusHistoryItem(null, "Booking Placed", "Rahul Sharma", "Order placed by customer", timeFormat.format(Date())),
                StatusHistoryItem("Booking Placed", initialStatus.label, "System", "Auto dispatch triggered", timeFormat.format(Date()))
            )
        )

        _bookings.update { listOf(newBooking) + it }

        // Log audit
        logAudit("Customer Placed Booking", "Bookings", newBookingId, "Booking $code for ${service.name}")

        return newBooking
    }

    // Update Booking State Machine Transition
    fun updateBookingStatus(bookingId: Long, newStatus: BookingStatus, actorName: String, reason: String? = null) {
        _bookings.update { currentBookings ->
            currentBookings.map { b ->
                if (b.id == bookingId) {
                    val historyItem = StatusHistoryItem(
                        oldStatus = b.status.label,
                        newStatus = newStatus.label,
                        changedBy = actorName,
                        reason = reason ?: "Status advanced to ${newStatus.label}",
                        timestamp = timeFormat.format(Date())
                    )

                    // If completed, update provider earnings
                    if (newStatus == BookingStatus.COMPLETED && b.providerId != null) {
                        _providers.update { plist ->
                            plist.map { p ->
                                if (p.id == b.providerId) {
                                    p.copy(
                                        totalJobsCompleted = p.totalJobsCompleted + 1,
                                        totalEarnings = p.totalEarnings + b.providerPayoutAmount,
                                        pendingPayoutBalance = p.pendingPayoutBalance + b.providerPayoutAmount
                                    )
                                } else p
                            }
                        }
                    }

                    b.copy(
                        status = newStatus,
                        statusHistory = b.statusHistory + historyItem
                    )
                } else b
            }
        }

        logAudit("Status Transition", "Bookings", bookingId, "Status updated to ${newStatus.label} by $actorName")
    }

    // Send In-App Chat Message
    fun sendChatMessage(bookingId: Long, senderId: Long, senderName: String, role: String, text: String) {
        val newMsg = ChatMessage(
            id = System.currentTimeMillis(),
            bookingId = bookingId,
            senderId = senderId,
            senderName = senderName,
            senderRole = role,
            messageText = text,
            timestamp = timeFormat.format(Date())
        )

        _chatMessages.update { map ->
            val list = map[bookingId]?.toMutableList() ?: mutableListOf()
            list.add(newMsg)
            map + (bookingId to list)
        }
    }

    // Provider Approvals
    fun approveProvider(providerId: Long) {
        _providers.update { plist ->
            plist.map { if (it.id == providerId) it.copy(verificationStatus = VerificationStatus.APPROVED, isAvailable = true) else it }
        }
        logAudit("Approve Provider", "Providers", providerId, "Provider verified and enabled for dispatch")
    }

    fun rejectProvider(providerId: Long) {
        _providers.update { plist ->
            plist.map { if (it.id == providerId) it.copy(verificationStatus = VerificationStatus.REJECTED, isAvailable = false) else it }
        }
        logAudit("Reject Provider", "Providers", providerId, "Provider onboarding rejected")
    }

    // Provider Onboarding Registration
    fun registerProvider(
        name: String,
        phone: String,
        businessName: String,
        skills: String,
        experienceYears: Int,
        serviceAreas: String
    ): ProviderProfile {
        val newId = (_providers.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val newProvider = ProviderProfile(
            id = newId,
            userId = 100L + newId,
            name = name,
            phone = phone,
            businessName = businessName,
            bio = "Certified home repair specialist with $experienceYears years of experience in $skills.",
            experienceYears = experienceYears,
            skills = skills,
            serviceAreas = serviceAreas,
            currentLatitude = 28.5355,
            currentLongitude = 77.2410,
            verificationStatus = VerificationStatus.PENDING,
            isAvailable = false,
            isOnline = false,
            ratingAvg = 5.0,
            totalJobsCompleted = 0,
            totalEarnings = 0.0,
            pendingPayoutBalance = 0.0,
            codPendingAmount = 0.0,
            creditBalance = 500.0,
            documentUploaded = true
        )
        _providers.update { listOf(newProvider) + it }
        logAudit("Provider Application", "Providers", newId, "New onboarding application submitted by $name ($businessName)")
        return newProvider
    }

    // Customer Wallet Top Up
    fun topUpWallet(amount: Double) {
        _walletBalance.update { it + amount }
        logAudit("Wallet Top-up", "Wallet", 1L, "Customer added ₹$amount to Bropatch Wallet")
    }

    // Provider Payout Request
    fun requestProviderPayout(providerId: Long, amount: Double): Boolean {
        var success = false
        _providers.update { plist ->
            plist.map { p ->
                if (p.id == providerId && p.pendingPayoutBalance >= amount) {
                    success = true
                    p.copy(pendingPayoutBalance = p.pendingPayoutBalance - amount)
                } else p
            }
        }
        if (success) {
            logAudit("Payout Requested", "Providers", providerId, "Payout request for ₹$amount submitted")
        }
        return success
    }

    // Service Pricing Updates from Admin
    fun updateServicePrice(serviceId: Long, newBasePrice: Double, newDiscountPrice: Double?) {
        _services.update { slist ->
            slist.map { s ->
                if (s.id == serviceId) s.copy(basePrice = newBasePrice, discountPrice = newDiscountPrice) else s
            }
        }
        logAudit("Update Service Price", "Services", serviceId, "Updated base to ₹$newBasePrice")
    }

    // Add New Service from Admin
    fun addNewService(service: HomeService) {
        _services.update { listOf(service) + it }
        logAudit("Create Service", "Services", service.id, "Added new service ${service.name}")
    }

    // Admin Dispatch
    fun assignProviderToBooking(bookingId: Long, providerId: Long) {
        val provider = _providers.value.find { it.id == providerId } ?: return
        _bookings.update { blist ->
            blist.map { b ->
                if (b.id == bookingId) {
                    val historyItem = StatusHistoryItem(
                        oldStatus = b.status.label,
                        newStatus = BookingStatus.PROVIDER_ASSIGNED.label,
                        changedBy = "Admin Dispatch",
                        reason = "Manually assigned ${provider.name}",
                        timestamp = timeFormat.format(Date())
                    )
                    b.copy(
                        providerId = provider.id,
                        providerName = provider.name,
                        providerPhone = provider.phone,
                        status = BookingStatus.PROVIDER_ASSIGNED,
                        statusHistory = b.statusHistory + historyItem
                    )
                } else b
            }
        }
        logAudit("Manual Dispatch", "Bookings", bookingId, "Assigned ${provider.name}")
    }

    // Add Address
    fun addAddress(
        label: String,
        streetAddress: String,
        apartmentUnit: String?,
        landmark: String?,
        city: String = "New Delhi",
        state: String = "Delhi",
        postalCode: String = "110001",
        isDefault: Boolean = false
    ): Address {
        val newAddr = Address(
            id = System.currentTimeMillis(),
            userId = 1L,
            label = label,
            streetAddress = streetAddress,
            apartmentUnit = apartmentUnit,
            landmark = landmark,
            city = city,
            state = state,
            postalCode = postalCode,
            latitude = 28.5355,
            longitude = 77.2410,
            isDefault = isDefault
        )
        _savedAddresses.update { current ->
            if (isDefault) {
                listOf(newAddr) + current.map { it.copy(isDefault = false) }
            } else {
                current + newAddr
            }
        }
        return newAddr
    }

    fun setDefaultAddress(addressId: Long) {
        _savedAddresses.update { current ->
            current.map { it.copy(isDefault = it.id == addressId) }
        }
    }

    fun deleteAddress(addressId: Long) {
        _savedAddresses.update { current ->
            current.filter { it.id != addressId }
        }
    }

    // Cancel Booking
    fun cancelBooking(bookingId: Long, reason: String) {
        _bookings.update { blist ->
            blist.map { b ->
                if (b.id == bookingId) {
                    val historyItem = StatusHistoryItem(
                        oldStatus = b.status.label,
                        newStatus = BookingStatus.CANCELLED.label,
                        changedBy = "Customer",
                        reason = reason,
                        timestamp = timeFormat.format(Date())
                    )
                    b.copy(
                        status = BookingStatus.CANCELLED,
                        statusHistory = b.statusHistory + historyItem
                    )
                } else b
            }
        }
        logAudit("Cancel Booking", "Bookings", bookingId, "Customer cancelled: $reason")
    }

    // Reschedule Booking
    fun rescheduleBooking(bookingId: Long, newDate: String, newTimeSlot: String) {
        _bookings.update { blist ->
            blist.map { b ->
                if (b.id == bookingId) {
                    val historyItem = StatusHistoryItem(
                        oldStatus = b.status.label,
                        newStatus = b.status.label,
                        changedBy = "Customer",
                        reason = "Rescheduled to $newDate ($newTimeSlot)",
                        timestamp = timeFormat.format(Date())
                    )
                    b.copy(
                        scheduledDate = newDate,
                        scheduledTimeSlot = newTimeSlot,
                        statusHistory = b.statusHistory + historyItem
                    )
                } else b
            }
        }
        logAudit("Reschedule Booking", "Bookings", bookingId, "Rescheduled to $newDate $newTimeSlot")
    }

    // Warranty Claims
    fun submitWarrantyClaim(
        bookingId: Long,
        reason: String,
        description: String,
        preferredDate: String
    ): WarrantyClaim? {
        val booking = _bookings.value.find { it.id == bookingId } ?: return null
        val newClaim = WarrantyClaim(
            id = System.currentTimeMillis(),
            bookingId = booking.id,
            bookingCode = booking.bookingCode,
            serviceName = booking.serviceName,
            customerName = booking.customerName,
            customerPhone = booking.customerPhone,
            reason = reason,
            description = description,
            status = "PENDING",
            preferredDate = preferredDate,
            createdAt = timeFormat.format(Date())
        )
        _warrantyClaims.update { listOf(newClaim) + it }
        logAudit("Warranty Claim", "Warranty", newClaim.id, "30-Day Guarantee rework requested for ${booking.bookingCode}")
        return newClaim
    }

    fun updateWarrantyClaimStatus(claimId: Long, newStatus: String) {
        _warrantyClaims.update { list ->
            list.map { if (it.id == claimId) it.copy(status = newStatus) else it }
        }
        logAudit("Update Warranty", "Warranty", claimId, "Claim marked as $newStatus")
    }

    // Smart Diagnostic Engine for Household Issues
    fun diagnoseHouseholdIssue(query: String): DiagnosticInsight {
        val q = query.lowercase().trim()
        return when {
            q.contains("ac") || q.contains("cool") || q.contains("air condition") || q.contains("gas") -> {
                DiagnosticInsight(
                    problemTitle = "AC Cooling / Airflow Malfunction",
                    categoryName = "AC & Appliances",
                    matchedServiceId = 5L,
                    matchedServiceName = "Split AC Deep Foam Jet Service",
                    severityLevel = "Medium",
                    likelyCause = "Grime-choked cooling coil filters or low R-32 refrigerant gas pressure.",
                    diySafetyTip = "Turn off the AC circuit breaker. Clean outer air mesh filter with tap water while waiting for the pro technician.",
                    estimatedCostMin = 499.0,
                    estimatedCostMax = 799.0
                )
            }
            q.contains("leak") || q.contains("pipe") || q.contains("sink") || q.contains("tap") || q.contains("water") || q.contains("clog") || q.contains("drain") -> {
                DiagnosticInsight(
                    problemTitle = "Plumbing Leakage or Drain Blockage",
                    categoryName = "Plumbing Services",
                    matchedServiceId = 1L,
                    matchedServiceName = "Pipe Leakage & Drainage Unblock",
                    severityLevel = "Medium",
                    likelyCause = "Worn-out rubber gasket washer, loose PVC compression nut, or organic hair/oil clog.",
                    diySafetyTip = "Shut off the local angle valve or main overhead water tank stopcock immediately to avoid flooring damage.",
                    estimatedCostMin = 299.0,
                    estimatedCostMax = 499.0
                )
            }
            q.contains("spark") || q.contains("switch") || q.contains("short") || q.contains("mcb") || q.contains("shock") || q.contains("electric") || q.contains("fan") -> {
                DiagnosticInsight(
                    problemTitle = "Electrical Short Circuit / Socket Hazard",
                    categoryName = "Electrical Works",
                    matchedServiceId = 3L,
                    matchedServiceName = "Switchboard & Short Circuit Fix",
                    severityLevel = "High Hazard",
                    likelyCause = "Overloaded terminal block, charred contact points, or loose neutral wire causing arching.",
                    diySafetyTip = "DANGER: Do NOT touch damp switches. Turn off the main MCB distribution trip lever immediately.",
                    estimatedCostMin = 329.0,
                    estimatedCostMax = 549.0
                )
            }
            q.contains("geyser") || q.contains("heater") || q.contains("hot water") -> {
                DiagnosticInsight(
                    problemTitle = "Water Geyser Heating Coil / Thermostat Fault",
                    categoryName = "AC & Appliances",
                    matchedServiceId = 9L,
                    matchedServiceName = "Storage Water Geyser Repair & Element Fix",
                    severityLevel = "High Hazard",
                    likelyCause = "Hard water limescale scaling on heating element or tripped safety thermal cutout.",
                    diySafetyTip = "Switch off the 16A heavy power plug. Do not use hot water tap till earthing and element are checked.",
                    estimatedCostMin = 399.0,
                    estimatedCostMax = 699.0
                )
            }
            q.contains("purifier") || q.contains("ro") || q.contains("filter") || q.contains("tds") -> {
                DiagnosticInsight(
                    problemTitle = "RO Water Purifier Filter Choke",
                    categoryName = "Plumbing Services",
                    matchedServiceId = 10L,
                    matchedServiceName = "RO Water Purifier Membrane & Filter Service",
                    severityLevel = "Low",
                    likelyCause = "Exhausted sediment/carbon filters or choked RO membrane causing low water flow.",
                    diySafetyTip = "Turn off the water input inlet diverter valve if water overflow is noticed.",
                    estimatedCostMin = 499.0,
                    estimatedCostMax = 799.0
                )
            }
            q.contains("door") || q.contains("lock") || q.contains("hinge") || q.contains("carpenter") || q.contains("furniture") -> {
                DiagnosticInsight(
                    problemTitle = "Carpentry / Jammed Door Lock",
                    categoryName = "Carpentry & Locks",
                    matchedServiceId = 7L,
                    matchedServiceName = "Door Lock & Hinge Realignment",
                    severityLevel = "Low",
                    likelyCause = "Humidity expansion in wooden door frames or misaligned deadbolt latch plate.",
                    diySafetyTip = "Do not force jammed key violently. Spray graphite or silicone lubricant in keyway.",
                    estimatedCostMin = 349.0,
                    estimatedCostMax = 599.0
                )
            }
            else -> {
                DiagnosticInsight(
                    problemTitle = "General Home Maintenance Diagnosis",
                    categoryName = "Home Repairs",
                    matchedServiceId = 1L,
                    matchedServiceName = "Pipe Leakage & Drainage Unblock",
                    severityLevel = "Low",
                    likelyCause = "Standard wear and tear of household fixtures and fittings.",
                    diySafetyTip = "Inspect the area carefully and avoid touching live electrical connections.",
                    estimatedCostMin = 299.0,
                    estimatedCostMax = 599.0
                )
            }
        }
    }

    // Rate Booking
    fun submitRating(bookingId: Long, rating: Double, feedback: String) {
        logAudit("Submit Rating", "Bookings", bookingId, "Rating: $rating stars - '$feedback'")
    }

    private fun logAudit(action: String, entity: String, entityId: Long, details: String) {
        val entry = AuditLogEntry(
            id = System.currentTimeMillis(),
            adminName = "Operations Lead",
            action = action,
            entity = entity,
            entityId = entityId,
            details = details,
            timestamp = timeFormat.format(Date())
        )
        _auditLogs.update { listOf(entry) + it }
    }

    // Calculate Real Admin Stats
    fun getAdminStats(): AdminDashboardStats {
        val currentBookings = _bookings.value
        val currentProviders = _providers.value
        val completed = currentBookings.filter { it.status == BookingStatus.COMPLETED }
        val gross = completed.sumOf { it.finalAmount }
        val platform = completed.sumOf { it.platformFee }
        val providerEarn = completed.sumOf { it.providerPayoutAmount }

        return AdminDashboardStats(
            grossRevenue = gross,
            platformEarnings = platform,
            providerEarnings = providerEarn,
            totalUsers = 154,
            totalProviders = currentProviders.size,
            activeProviders = currentProviders.count { it.verificationStatus == VerificationStatus.APPROVED },
            pendingApprovals = currentProviders.count { it.verificationStatus == VerificationStatus.PENDING },
            todayBookings = currentBookings.size,
            pendingBookings = currentBookings.count { it.status in listOf(BookingStatus.PENDING, BookingStatus.SEARCHING_PROVIDER, BookingStatus.PROVIDER_ASSIGNED, BookingStatus.PROVIDER_ON_WAY, BookingStatus.PROVIDER_ARRIVED, BookingStatus.WORK_STARTED) },
            completedBookings = completed.size,
            cancelledBookings = currentBookings.count { it.status == BookingStatus.CANCELLED },
            pendingPayouts = currentProviders.sumOf { it.pendingPayoutBalance },
            openDisputes = currentBookings.count { it.status == BookingStatus.DISPUTED }
        )
    }
}
