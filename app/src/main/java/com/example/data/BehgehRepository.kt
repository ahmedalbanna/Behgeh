package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class BehgehRepository(
    private val userDao: UserDao,
    private val venueDao: VenueDao,
    private val bookingDao: BookingDao
) {
    // User functions
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    
    suspend fun getClientByUserId(userId: Long): Client? = userDao.getClientByUserId(userId)
    suspend fun insertClient(client: Client): Long = userDao.insertClient(client)
    
    suspend fun getVenueOwnerByUserId(userId: Long): VenueOwner? = userDao.getVenueOwnerByUserId(userId)
    suspend fun insertVenueOwner(owner: VenueOwner): Long = userDao.insertVenueOwner(owner)
    
    fun getPendingVenueOwnersFlow(): Flow<List<VenueOwner>> = userDao.getPendingVenueOwnersFlow()
    suspend fun getUsersByIds(userIds: List<Long>): List<User> = userDao.getUsersByIds(userIds)
    suspend fun updateVenueOwner(owner: VenueOwner) = userDao.updateVenueOwner(owner)

    // Venue functions
    fun getAllVenuesFlow(): Flow<List<Venue>> = venueDao.getAllVenuesFlow()
    suspend fun getVenueById(id: Long): Venue? = venueDao.getVenueById(id)
    fun getVenuesByOwnerFlow(ownerId: Long): Flow<List<Venue>> = venueDao.getVenuesByOwnerFlow(ownerId)
    suspend fun insertVenue(venue: Venue): Long = venueDao.insertVenue(venue)
    
    fun getPackagesForVenueFlow(venueId: Long): Flow<List<PackageItem>> = venueDao.getPackagesForVenueFlow(venueId)
    suspend fun getPackagesForVenue(venueId: Long): List<PackageItem> = venueDao.getPackagesForVenue(venueId)
    suspend fun insertPackageItem(packageItem: PackageItem): Long = venueDao.insertPackageItem(packageItem)
    
    fun getReviewsForVenueFlow(venueId: Long): Flow<List<Review>> = venueDao.getReviewsForVenueFlow(venueId)
    suspend fun insertReview(review: Review): Long = venueDao.insertReview(review)

    // Booking & Hold functions
    fun getClientBookingsFlow(clientId: Long): Flow<List<Booking>> = bookingDao.getClientBookingsFlow(clientId)
    suspend fun getBookingById(id: Long): Booking? = bookingDao.getBookingById(id)
    suspend fun insertBooking(booking: Booking): Long = bookingDao.insertBooking(booking)
    suspend fun updateBooking(booking: Booking) = bookingDao.updateBooking(booking)

    suspend fun verifyAndBlockLockBooking(
        clientId: Long,
        venueId: Long,
        date: String,
        basePrice: Double,
        servicesCost: Double,
        taxes: Double,
        totalCost: Double,
        selectedServicesCsv: String,
        downPayment: Double,
        cancellationTermsAgreed: Boolean
    ): BookingResult {
        val now = System.currentTimeMillis()
        // Check for double bookings on that date
        val conflict = bookingDao.getConflictingBooking(venueId, date, now)
        if (conflict != null) {
            return BookingResult.Failure("القاعة محجوزة بالفعل أو قيد الحجز المؤقت في هذا التاريخ!")
        }

        // Create a Block Lock hold for 15 minutes
        val holdDurMs = 15 * 60 * 1000L
        val holdBooking = Booking(
            clientId = clientId,
            venueId = venueId,
            bookingDate = date,
            status = "HOLD",
            basePrice = basePrice,
            servicesCost = servicesCost,
            taxes = taxes,
            totalCost = totalCost,
            selectedServicesCsv = selectedServicesCsv,
            bookingTimestamp = now,
            expirationTimestamp = now + holdDurMs,
            downPayment = downPayment,
            digitalSignature = null,
            cancellationTermsAgreed = cancellationTermsAgreed
        )
        val bookingId = bookingDao.insertBooking(holdBooking)
        return BookingResult.Success(bookingId, holdBooking.copy(id = bookingId))
    }

    suspend fun confirmPaymentAndCreateContract(
        bookingId: Long,
        clientName: String,
        signature: String,
        paymentMethod: String
    ): Boolean {
        val booking = bookingDao.getBookingById(bookingId) ?: return false
        val venue = venueDao.getVenueById(booking.venueId) ?: return false
        val ownerUser = userDao.getUserById(venue.ownerId)
        val ownerName = ownerUser?.fullName ?: "مالك القاعة"

        // Save signature to booking and set status to PAID
        val updatedBooking = booking.copy(
            status = "PAID",
            digitalSignature = signature
        )
        bookingDao.updateBooking(updatedBooking)

        // Make it approved public calendar record
        val approved = ApprovedBooking(
            bookingId = bookingId,
            venueId = booking.venueId,
            bookingDate = booking.bookingDate
        )
        bookingDao.insertApprovedBooking(approved)

        // Save simulated payment record
        val payment = Payment(
            bookingId = bookingId,
            amount = booking.downPayment,
            timestamp = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            status = "SUCCESS",
            transactionId = "TXN-${UUID.randomUUID().toString().uppercase().take(8)}"
        )
        bookingDao.insertPayment(payment)

        // Save Digital Contract
        val contract = Contract(
            bookingId = bookingId,
            clientName = clientName,
            ownerName = ownerName,
            venueName = venue.nameAr,
            bookingDate = booking.bookingDate,
            totalCost = booking.totalCost,
            eSignature = signature,
            timestamp = System.currentTimeMillis(),
            cancellationPolicy = "يحق للعميل إلغاء الحجز واسترداد العربون بالكامل قبل موعد المناسبة بـ 30 يوماً. في حال الإلغاء بعد ذلك، يحتفظ مالك القاعة بمبلغ العربون لحجز الكتلة الزمنية."
        )
        bookingDao.insertContract(contract)

        // Save Invoice
        val invoice = Invoice(
            bookingId = bookingId,
            invoiceNo = "INV-${System.currentTimeMillis().toString().takeLast(6)}",
            clientName = clientName,
            venueName = venue.nameAr,
            eventDate = booking.bookingDate,
            basePrice = booking.basePrice,
            servicesCost = booking.servicesCost,
            taxes = booking.taxes,
            totalAmount = booking.totalCost,
            timestamp = System.currentTimeMillis(),
            status = "PAID"
        )
        bookingDao.insertInvoice(invoice)

        return true
    }

    suspend fun cancelBooking(bookingId: Long) {
        val booking = bookingDao.getBookingById(bookingId) ?: return
        val cancelledBooking = booking.copy(status = "CANCELLED")
        bookingDao.updateBooking(cancelledBooking)
        bookingDao.deleteApprovedBookingByBookingId(bookingId)
    }

    // Public Calendar and Lists
    fun getAllApprovedBookingsFlow(): Flow<List<ApprovedBooking>> = bookingDao.getAllApprovedBookingsFlow()
    fun getApprovedBookingsForVenueFlow(venueId: Long): Flow<List<ApprovedBooking>> = bookingDao.getApprovedBookingsForVenueFlow(venueId)
    
    // Payments & Invoice Check
    fun getPaymentsForBookingFlow(bookingId: Long): Flow<List<Payment>> = bookingDao.getPaymentsForBookingFlow(bookingId)
    suspend fun getContractByBookingId(bookingId: Long): Contract? = bookingDao.getContractByBookingId(bookingId)
    suspend fun getInvoiceByBookingId(bookingId: Long): Invoice? = bookingDao.getInvoiceByBookingId(bookingId)
    fun getAllInvoices(): Flow<List<Invoice>> = bookingDao.getAllInvoices()

    // AI Inquiries Log
    suspend fun logAIInquiry(clientId: Long, prompt: String, generatedRecommendation: String) {
        val inquiry = AIInquiry(
            clientId = clientId,
            prompt = prompt,
            generatedRecommendation = generatedRecommendation,
            timestamp = System.currentTimeMillis()
        )
        bookingDao.insertAIInquiry(inquiry)
    }

    // CRM for Venue Owners
    fun getOwnerBookingsFlow(ownerId: Long): Flow<List<Booking>> = bookingDao.getOwnerBookingsFlow(ownerId)

    // Manual block of calendar day by Owner
    suspend fun blockDateManually(ownerId: Long, venueId: Long, date: String): Boolean {
        val now = System.currentTimeMillis()
        val conflict = bookingDao.getConflictingBooking(venueId, date, now)
        if (conflict != null) return false

        val manualBooking = Booking(
            clientId = 0L, // 0 indicates system manual block
            venueId = venueId,
            bookingDate = date,
            status = "PAID",
            basePrice = 0.0,
            servicesCost = 0.0,
            taxes = 0.0,
            totalCost = 0.0,
            selectedServicesCsv = "",
            bookingTimestamp = now,
            expirationTimestamp = 0L,
            downPayment = 0.0,
            digitalSignature = "MANUAL_BLOCK",
            cancellationTermsAgreed = true
        )
        val bookingId = bookingDao.insertBooking(manualBooking)
        val approved = ApprovedBooking(
            bookingId = bookingId,
            venueId = venueId,
            bookingDate = date
        )
        bookingDao.insertApprovedBooking(approved)
        return true
    }

    // Archiving old files and bookings
    suspend fun archivePastBookings(olderThanDate: String): Int {
        val pastBookings = bookingDao.getPastPaidBookingsToArchive(olderThanDate)
        if (pastBookings.isEmpty()) return 0
        
        val ids = pastBookings.map { it.id }
        bookingDao.deleteBookings(ids)
        bookingDao.deleteApprovedBookings(ids)
        return pastBookings.size
    }

    // Database pre-population
    suspend fun prepopulateIfEmpty() {
        val usersCheck = userDao.getUserByEmail("admin@behgeh.com")
        if (usersCheck != null) return

        // Create Admin
        val adminId = userDao.insertUser(User(
            username = "admin",
            email = "admin@behgeh.com",
            passwordHash = "admin123",
            role = "ADMIN",
            fullName = "مدير المنصة"
        ))

        // Create Venue Owner 1
        val owner1UserId = userDao.insertUser(User(
            username = "owner_riyadh",
            email = "riyadh@behgeh.com",
            passwordHash = "owner123",
            role = "VENUE_OWNER",
            fullName = "عبدالرحمن آل سعود"
        ))
        val owner1Id = userDao.insertVenueOwner(VenueOwner(
            userId = owner1UserId,
            phone = "0501112222",
            businessLicense = "CR-1010349811",
            verificationStatus = "APPROVED"
        ))

        // Create Venue Owner 2
        val owner2UserId = userDao.insertUser(User(
            username = "owner_jeddah",
            email = "jeddah@behgeh.com",
            passwordHash = "owner123",
            role = "VENUE_OWNER",
            fullName = "سالم باخشب"
        ))
        val owner2Id = userDao.insertVenueOwner(VenueOwner(
            userId = owner2UserId,
            phone = "0502223333",
            businessLicense = "CR-4030221389",
            verificationStatus = "APPROVED"
        ))

        // Create Default Client
        val clientUserId = userDao.insertUser(User(
            username = "client",
            email = "ahmed@behgeh.com",
            passwordHash = "client123",
            role = "CLIENT",
            fullName = "أحمد البنا"
        ))
        userDao.insertClient(Client(
            userId = clientUserId,
            phone = "+966509998888",
            avatarUrl = ""
        ))

        // Add Venues
        val v1Id = venueDao.insertVenue(Venue(
            ownerId = owner1UserId,
            name = "Royal Alyarmouk Palace",
            nameAr = "قصر اليرموك الملكي للمناسبات",
            description = "High-end luxury wedding hall with modern Arabesque design, serving Riyadh elite.",
            descriptionAr = "قصر زفاف فاخر للغاية متكامل الخدمات مع بهو رخامي وقاعة طعام مجهزة يتسع لنخبة المناسبات في الرياض.",
            city = "الرياض",
            address = "حي الرياض، مخرج 9، الرياض",
            capacity = 500,
            basePrice = 15000.0,
            imageUrl = ""
        ))

        val v2Id = venueDao.insertVenue(Venue(
            ownerId = owner1UserId,
            name = "Rotana Crystal Hall",
            nameAr = "قاعة روتانا الكريستالية",
            description = "Elegant medium wedding hall featuring luxury crystal chandeliers in central Riyadh.",
            descriptionAr = "قاعة زفاف دافئة وأنيقة تتميز بثريات كريستالية راقية مخصصة لحفلات الزفاف والخطوبة متوسطة الحجم.",
            city = "الرياض",
            address = "شارع التخصصي، الرياض",
            capacity = 250,
            basePrice = 9000.0,
            imageUrl = ""
        ))

        val v3Id = venueDao.insertVenue(Venue(
            ownerId = owner2UserId,
            name = "Lailat Elomr Sea Wave Palace",
            nameAr = "قصر ليلة العمر - أمواج البحر",
            description = "Spectacular coastal wedding palace overlooking the Red Sea in Jeddah.",
            descriptionAr = "قاعة أفراح ساحرة بإطلالة بحرية خلابة بجوار الكورنيش تقدم خدمات متكاملة وواجهة خارجية فريدة.",
            city = "جدة",
            address = "طريق الكورنيش، جدة",
            capacity = 450,
            basePrice = 18000.0,
            imageUrl = ""
        ))

        val v4Id = venueDao.insertVenue(Venue(
            ownerId = owner2UserId,
            name = "Marina Al Sharqia Luxury Hall",
            nameAr = "قاعة مارينا الشرقية الفخمة",
            description = "A spacious, modern wedding hall in Dammam with premium service.",
            descriptionAr = "قاعة حديثة مجهزة بأحدث الصوتيات والإضاءات المتكاملة والمساحات الكافية لحفلات زفاف مميزة في الدمام.",
            city = "الدمام",
            address = "حي الشاطئ، الدمام",
            capacity = 300,
            basePrice = 11000.0,
            imageUrl = ""
        ))

        // Add Package Packages for each Venue
        val venuesList = listOf(v1Id, v2Id, v3Id, v4Id)
        for (vid in venuesList) {
            // Catering
            venueDao.insertPackageItem(PackageItem(
                venueId = vid,
                name = "Saudi Feast Catering",
                nameAr = "ضيافة تموين: عشاء ذبائح حاشي بلدي كامل مع المقبلات والشعبيات",
                cost = 4500.0,
                category = "CATERING",
                description = "ذبائح نجدي كاملة مع مقبلات وحلويات"
            ))
            venueDao.insertPackageItem(PackageItem(
                venueId = vid,
                name = "Premium Open Buffet",
                nameAr = "ضيافة تموين: بوفيه مفتوح شرقي وغربي فاخر (لـ 200 شخص)",
                cost = 6000.0,
                category = "CATERING",
                description = "بوفيه متكامل يضم اللحوم والمأكولات البحرية والحلويات والقهوة"
            ))

            // Decoration
            venueDao.insertPackageItem(PackageItem(
                venueId = vid,
                name = "Royal Floral Kosha Design",
                nameAr = "تنسيق الكوشة: كوشة ملكية بالورد الطبيعي والستائر المضيئة",
                cost = 3500.0,
                category = "DECORATION",
                description = "تصميم مخصص للمسرح مع باقات الورد الطبيعي الفاخر"
            ))
            venueDao.insertPackageItem(PackageItem(
                venueId = vid,
                name = "Tables Guest Flower & Light Styling",
                nameAr = "تنسيق القاعة: ممر ملكي مضيء وتنسيق بوكيهات ورد طاولات الضيوف",
                cost = 2000.0,
                category = "DECORATION",
                description = "تزيين ممر العروسين والشمعدانات والإنارة الرومانسية للطاولات"
            ))

            // Sound & Lighting
            venueDao.insertPackageItem(PackageItem(
                venueId = vid,
                name = "Pro Laser & Cinema Lighting",
                nameAr = "صوت وإضاءة: نظام ليزر ومؤثرات البخار وإضاءة المسرح الكبرى",
                cost = 2500.0,
                category = "SOUND",
                description = "إضاءة ليزر متحركة ومؤثرات بخار وتجهيزات دي جي"
            ))

            // Photography
            venueDao.insertPackageItem(PackageItem(
                venueId = vid,
                name = "Premium Cinematic Album Coverage",
                nameAr = "تصوير فوتوغرافي: تغطية سينمائية كاملة بـ 3 كاميرات وألبوم حراري",
                cost = 4000.0,
                category = "PHOTOGRAPHY",
                description = "تغطية كاملة من قبل استوديو نسائي احترافي يضم الفيديو واللقطات التذكارية"
            ))
        }
    }
}

sealed class BookingResult {
    data class Success(val bookingId: Long, val booking: Booking) : BookingResult()
    data class Failure(val message: String) : BookingResult()
}
