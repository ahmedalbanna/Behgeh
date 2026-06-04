package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val role: String, // "CLIENT", "VENUE_OWNER", "ADMIN"
    val fullName: String
)

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val phone: String,
    val avatarUrl: String = ""
)

@Entity(tableName = "venue_owners")
data class VenueOwner(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val phone: String,
    val businessLicense: String, // CR Number / رخصة العمل التجارية
    val verificationStatus: String // "PENDING", "APPROVED", "REJECTED"
)

@Entity(tableName = "venues")
data class Venue(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: Long,
    val name: String,
    val nameAr: String,
    val description: String,
    val descriptionAr: String,
    val city: String, // Standardized Arabic lowercase (e.g. "الرياض", "جدة")
    val address: String,
    val capacity: Int,
    val basePrice: Double,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = ""
)

@Entity(tableName = "packages")
data class PackageItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val venueId: Long,
    val name: String,
    val nameAr: String,
    val cost: Double,
    val category: String, // "CATERING", "DECORATION", "SOUND", "PHOTOGRAPHY"
    val description: String = ""
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val venueId: Long,
    val bookingDate: String, // Formatted as "YYYY-MM-DD" Block booking
    val status: String, // "HOLD" (Temporary 15m), "PAID", "CANCELLED", "PENDING_PAYMENT"
    val basePrice: Double,
    val servicesCost: Double,
    val taxes: Double,
    val totalCost: Double, // calculated: Base Venue Price + Sum(Services) + Taxes
    val selectedServicesCsv: String, // comma separated PackageItem ids
    val bookingTimestamp: Long,
    val expirationTimestamp: Long, // timestamp when temporary hold expires
    val downPayment: Double,
    val digitalSignature: String?, // User sign content
    val cancellationTermsAgreed: Boolean
)

@Entity(tableName = "approved_bookings")
data class ApprovedBooking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val venueId: Long,
    val bookingDate: String // "YYYY-MM-DD"
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val amount: Double,
    val timestamp: Long,
    val paymentMethod: String, // "VISA", "MADA", "APPLE_PAY"
    val status: String, // "SUCCESS", "FAILED"
    val transactionId: String
)

@Entity(tableName = "contracts")
data class Contract(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val clientName: String,
    val ownerName: String,
    val venueName: String,
    val bookingDate: String,
    val totalCost: Double,
    val eSignature: String,
    val timestamp: Long,
    val cancellationPolicy: String
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val venueId: Long,
    val rating: Int, // 1 to 5
    val comment: String,
    val timestamp: Long
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val invoiceNo: String,
    val clientName: String,
    val venueName: String,
    val eventDate: String,
    val basePrice: Double,
    val servicesCost: Double,
    val taxes: Double,
    val totalAmount: Double,
    val timestamp: Long,
    val status: String // "PAID", "PARTIALLY_PAID", "DRAFT"
)

@Entity(tableName = "ai_inquiries")
data class AIInquiry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val prompt: String,
    val generatedRecommendation: String,
    val timestamp: Long
)
