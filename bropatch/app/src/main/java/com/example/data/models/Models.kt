package com.example.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class UserRole {
    CUSTOMER, PROVIDER, ADMIN
}

enum class BookingStatus(val label: String, val stepIndex: Int) {
    PENDING("Booking Received", 0),
    SEARCHING_PROVIDER("Searching Provider", 1),
    PROVIDER_ASSIGNED("Provider Assigned", 2),
    PROVIDER_ACCEPTED("Accepted by Pro", 3),
    PROVIDER_ON_WAY("On The Way", 4),
    PROVIDER_ARRIVED("Arrived at Location", 5),
    WORK_STARTED("Work in Progress", 6),
    WORK_COMPLETED("Work Completed", 7),
    PAYMENT_PENDING("Payment Pending", 8),
    COMPLETED("Completed & Verified", 9),
    CANCELLED("Cancelled", -1),
    DISPUTED("In Dispute", -2)
}

enum class VerificationStatus {
    PENDING, APPROVED, REJECTED, SUSPENDED
}

enum class PaymentMethod {
    RAZORPAY, COD, WALLET
}

@JsonClass(generateAdapter = true)
data class ServiceCategory(
    val id: Int,
    val name: String,
    val slug: String,
    val icon: String,
    val imageUrl: String,
    val description: String,
    val servicesCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class HomeService(
    val id: Long,
    val categoryId: Int,
    val categoryName: String,
    val name: String,
    val slug: String,
    val shortDescription: String,
    val description: String,
    val basePrice: Double,
    val discountPrice: Double?,
    val estimatedDurationMins: Int,
    val imageUrl: String,
    val warrantyDays: Int,
    val ratingAvg: Double,
    val totalReviews: Int,
    val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class BannerItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val ctaText: String,
    val badgeText: String?,
    val imageUrl: String,
    val categoryId: Int?
)

@JsonClass(generateAdapter = true)
data class Address(
    val id: Long,
    val userId: Long,
    val label: String,
    val streetAddress: String,
    val apartmentUnit: String?,
    val landmark: String?,
    val city: String,
    val state: String,
    val postalCode: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean = false
)

@JsonClass(generateAdapter = true)
data class Booking(
    val id: Long,
    val bookingCode: String,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String,
    val providerId: Long?,
    val providerName: String?,
    val providerPhone: String?,
    val serviceId: Long,
    val serviceName: String,
    val serviceImage: String,
    val address: Address,
    val status: BookingStatus,
    val scheduledDate: String,
    val scheduledTimeSlot: String,
    val problemDescription: String,
    val baseAmount: Double,
    val discountAmount: Double,
    val taxAmount: Double,
    val finalAmount: Double,
    val platformFee: Double,
    val providerPayoutAmount: Double,
    val paymentMethod: PaymentMethod,
    val paymentStatus: String,
    val createdAt: String,
    val statusHistory: List<StatusHistoryItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class StatusHistoryItem(
    val oldStatus: String?,
    val newStatus: String,
    val changedBy: String,
    val reason: String?,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class ProviderProfile(
    val id: Long,
    val userId: Long,
    val name: String,
    val phone: String,
    val businessName: String,
    val bio: String,
    val experienceYears: Int,
    val skills: String,
    val serviceAreas: String,
    val currentLatitude: Double,
    val currentLongitude: Double,
    val verificationStatus: VerificationStatus,
    val isAvailable: Boolean,
    val isOnline: Boolean,
    val ratingAvg: Double,
    val totalJobsCompleted: Int,
    val totalEarnings: Double,
    val pendingPayoutBalance: Double,
    val codPendingAmount: Double,
    val creditBalance: Double,
    val documentUploaded: Boolean
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: Long,
    val bookingId: Long,
    val senderId: Long,
    val senderName: String,
    val senderRole: String,
    val messageText: String,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class Coupon(
    val id: Int,
    val code: String,
    val discountType: String, // percentage or fixed
    val discountValue: Double,
    val minOrderAmount: Double,
    val maxDiscountAmount: Double?,
    val description: String
)

@JsonClass(generateAdapter = true)
data class Invoice(
    val invoiceNumber: String,
    val bookingCode: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val providerName: String,
    val serviceName: String,
    val baseAmount: Double,
    val discountAmount: Double,
    val taxGstAmount: Double,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val issuedDate: String
)

@JsonClass(generateAdapter = true)
data class AdminDashboardStats(
    val grossRevenue: Double,
    val platformEarnings: Double,
    val providerEarnings: Double,
    val totalUsers: Int,
    val totalProviders: Int,
    val activeProviders: Int,
    val pendingApprovals: Int,
    val todayBookings: Int,
    val pendingBookings: Int,
    val completedBookings: Int,
    val cancelledBookings: Int,
    val pendingPayouts: Double,
    val openDisputes: Int
)

@JsonClass(generateAdapter = true)
data class AuditLogEntry(
    val id: Long,
    val adminName: String,
    val action: String,
    val entity: String,
    val entityId: Long,
    val details: String,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class WarrantyClaim(
    val id: Long,
    val bookingId: Long,
    val bookingCode: String,
    val serviceName: String,
    val customerName: String,
    val customerPhone: String,
    val reason: String,
    val description: String,
    val status: String, // PENDING, APPROVED, REWORK_SCHEDULED, RESOLVED
    val preferredDate: String,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class DiagnosticInsight(
    val problemTitle: String,
    val categoryName: String,
    val matchedServiceId: Long,
    val matchedServiceName: String,
    val severityLevel: String, // Low, Medium, High Hazard
    val likelyCause: String,
    val diySafetyTip: String,
    val estimatedCostMin: Double,
    val estimatedCostMax: Double
)
