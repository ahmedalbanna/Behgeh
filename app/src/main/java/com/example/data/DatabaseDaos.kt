package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM clients WHERE userId = :userId LIMIT 1")
    suspend fun getClientByUserId(userId: Long): Client?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Query("SELECT * FROM venue_owners WHERE userId = :userId LIMIT 1")
    suspend fun getVenueOwnerByUserId(userId: Long): VenueOwner?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenueOwner(owner: VenueOwner): Long

    @Query("SELECT * FROM venue_owners WHERE verificationStatus = 'PENDING'")
    fun getPendingVenueOwnersFlow(): Flow<List<VenueOwner>>

    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    suspend fun getUsersByIds(userIds: List<Long>): List<User>

    @Update
    suspend fun updateVenueOwner(owner: VenueOwner)
}

@Dao
interface VenueDao {
    @Query("SELECT * FROM venues")
    fun getAllVenuesFlow(): Flow<List<Venue>>

    @Query("SELECT * FROM venues WHERE city = :city")
    suspend fun getVenuesByCity(city: String): List<Venue>

    @Query("SELECT * FROM venues WHERE id = :id LIMIT 1")
    suspend fun getVenueById(id: Long): Venue?

    @Query("SELECT * FROM venues WHERE ownerId = :ownerId")
    fun getVenuesByOwnerFlow(ownerId: Long): Flow<List<Venue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenue(venue: Venue): Long

    @Query("SELECT * FROM packages WHERE venueId = :venueId")
    fun getPackagesForVenueFlow(venueId: Long): Flow<List<PackageItem>>

    @Query("SELECT * FROM packages WHERE venueId = :venueId")
    suspend fun getPackagesForVenue(venueId: Long): List<PackageItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackageItem(packageItem: PackageItem): Long

    @Query("SELECT * FROM reviews WHERE venueId = :venueId")
    fun getReviewsForVenueFlow(venueId: Long): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings WHERE clientId = :clientId ORDER BY bookingTimestamp DESC")
    fun getClientBookingsFlow(clientId: Long): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: Long): Booking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    // Check if double booking occurs
    @Query("""
        SELECT * FROM bookings 
        WHERE venueId = :venueId 
        AND bookingDate = :date 
        AND (status = 'PAID' OR (status = 'HOLD' AND expirationTimestamp > :now))
        LIMIT 1
    """)
    suspend fun getConflictingBooking(venueId: Long, date: String, now: Long): Booking?

    // Approved Bookings DAO for public calendar
    @Query("SELECT * FROM approved_bookings")
    fun getAllApprovedBookingsFlow(): Flow<List<ApprovedBooking>>

    @Query("SELECT * FROM approved_bookings WHERE venueId = :venueId")
    fun getApprovedBookingsForVenueFlow(venueId: Long): Flow<List<ApprovedBooking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovedBooking(approved: ApprovedBooking): Long

    @Query("DELETE FROM approved_bookings WHERE bookingId = :bookingId")
    suspend fun deleteApprovedBookingByBookingId(bookingId: Long)

    // Payments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Query("SELECT * FROM payments WHERE bookingId = :bookingId")
    fun getPaymentsForBookingFlow(bookingId: Long): Flow<List<Payment>>

    // Contracts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: Contract): Long

    @Query("SELECT * FROM contracts WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getContractByBookingId(bookingId: Long): Contract?

    // Invoices
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Query("SELECT * FROM invoices WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getInvoiceByBookingId(bookingId: Long): Invoice?

    @Query("SELECT * FROM invoices")
    fun getAllInvoices(): Flow<List<Invoice>>

    // AI Inquiries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAIInquiry(inquiry: AIInquiry): Long

    // CRM / Admin queries
    @Query("""
        SELECT * FROM bookings 
        WHERE venueId IN (SELECT id FROM venues WHERE ownerId = :ownerId)
    """)
    fun getOwnerBookingsFlow(ownerId: Long): Flow<List<Booking>>

    // Archiving
    @Query("SELECT * FROM bookings WHERE bookingDate < :olderThanDate AND status = 'PAID'")
    suspend fun getPastPaidBookingsToArchive(olderThanDate: String): List<Booking>

    @Query("DELETE FROM bookings WHERE id IN (:ids)")
    suspend fun deleteBookings(ids: List<Long>)

    @Query("DELETE FROM approved_bookings WHERE bookingId IN (:ids)")
    suspend fun deleteApprovedBookings(ids: List<Long>)
}
