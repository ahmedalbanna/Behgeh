package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class BehgehViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = BehgehRepository(
        database.userDao(),
        database.venueDao(),
        database.bookingDao()
    )

    // Current State
    var currentUserState = MutableStateFlow<User?>(null)
    var currentClientState = MutableStateFlow<Client?>(null)
    var currentOwnerState = MutableStateFlow<VenueOwner?>(null)

    // Dynamic Lists
    val allVenues = repository.getAllVenuesFlow().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val approvedBookings = repository.getAllApprovedBookingsFlow().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val pendingOwnersFlow = repository.getPendingVenueOwnersFlow().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allInvoicesFlow = repository.getAllInvoices().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Chat States
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("أهلاً بك في منصة بهجة! أنا مستشارك الذكي المخصص لمساعدتك في إيجاد قاعة أحلامك وتجهيز باقتك المثالية. اكتب لي ما تتمناه (مثال: أريد حفل زفاف يتسع لـ 300 شخص في الرياض بميزانية متوسطة).", false)
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages
    val isAnalyzingChat = MutableStateFlow(false)

    // Navigation and Selection
    val currentScreen = MutableStateFlow("user_home") // "user_home", "details", "ai_consult", "checkout", "bookings", "owner_dashboard", "admin_dashboard"
    val selectedVenue = MutableStateFlow<Venue?>(null)
    val selectedDate = MutableStateFlow("")
    val selectedPackagesMap = MutableStateFlow<Map<Long, Boolean>>(emptyMap())

    // Checkout & Block Lock States
    val activeBookingHold = MutableStateFlow<Booking?>(null)
    val holdTimeRemainingSec = MutableStateFlow(0)
    private var holdTimerJob: Job? = null

    // Owner specific stats
    val ownerBookings = MutableStateFlow<List<Booking>>(emptyList())
    val ownerVenues = MutableStateFlow<List<Venue>>(emptyList())

    // All Users for display inside Admin
    val allUsersMap = MutableStateFlow<Map<Long, User>>(emptyMap())

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            // Set default user as client to start
            val clientUser = repository.getUserByEmail("ahmed@behgeh.com")
            if (clientUser != null) {
                loginAs(clientUser)
            }
            loadAllUsers()
        }

        // Keep hold expire check active
        viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val currentHold = activeBookingHold.value
                if (currentHold != null && currentHold.status == "HOLD") {
                    if (now > currentHold.expirationTimestamp) {
                        // Hold has expired! Cancel it
                        cancelActiveHold()
                    } else {
                        val remainingSec = ((currentHold.expirationTimestamp - now) / 1000).toInt()
                        holdTimeRemainingSec.value = if (remainingSec < 0) 0 else remainingSec
                    }
                }
                delay(1000)
            }
        }
    }

    private suspend fun loadAllUsers() {
        val allVenuesList = allVenues.firstOrNull() ?: emptyList()
        val ownerIds = allVenuesList.map { it.ownerId }.distinct()
        val users = repository.getUsersByIds(ownerIds)
        allUsersMap.value = users.associateBy { it.id }
    }

    fun loginAs(user: User) {
        viewModelScope.launch {
            currentUserState.value = user
            if (user.role == "CLIENT") {
                val client = repository.getClientByUserId(user.id)
                currentClientState.value = client
                currentOwnerState.value = null
                currentScreen.value = "user_home"
            } else if (user.role == "VENUE_OWNER") {
                val owner = repository.getVenueOwnerByUserId(user.id)
                currentOwnerState.value = owner
                currentClientState.value = null
                currentScreen.value = "owner_dashboard"
                // Load owner items
                if (owner != null) {
                    repository.getOwnerBookingsFlow(user.id).collect {
                        ownerBookings.value = it
                    }
                }
                repository.getVenuesByOwnerFlow(user.id).collect {
                    ownerVenues.value = it
                }
            } else if (user.role == "ADMIN") {
                currentClientState.value = null
                currentOwnerState.value = null
                currentScreen.value = "admin_dashboard"
            }
        }
    }

    fun navigateTo(screen: String) {
        currentScreen.value = screen
    }

    fun selectVenue(venue: Venue) {
        selectedVenue.value = venue
        selectedDate.value = ""
        selectedPackagesMap.value = emptyMap()
        currentScreen.value = "details"
    }

    fun togglePackageItem(packageId: Long) {
        val current = selectedPackagesMap.value.toMutableMap()
        val isSelected = current[packageId] ?: false
        current[packageId] = !isSelected
        selectedPackagesMap.value = current
    }

    // AI consult query
    fun sendUserChatMessage(inputText: String) {
        if (inputText.isBlank()) return
        
        val userMsg = ChatMessage(text = inputText, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        isAnalyzingChat.value = true

        viewModelScope.launch {
            try {
                val currentVenues = allVenues.value
                val aiResponse = GeminiService.findMatchingVenueResponse(inputText, currentVenues)
                
                // Add AI answer
                val aiMsg = ChatMessage(text = aiResponse, isUser = false)
                _chatMessages.value = _chatMessages.value + aiMsg
                
                // Track inquiry inside Room
                currentUserState.value?.id?.let { clientId ->
                    repository.logAIInquiry(clientId, inputText, aiResponse.take(200))
                }
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage("حدث خطأ في معالجة طلبك، يرجى المحاولة لاحقاً.", false)
            } finally {
                isAnalyzingChat.value = false
            }
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(
            ChatMessage("مرحباً بك مجدداً في مستشارك الذكي لبهجة. اكتب احتياجاتك وسنقوم باللازم لتسهيل خيارك الأنسب.", false)
        )
    }

    // Try secure hold
    fun requestBlockLockHold(
        venue: Venue,
        date: String,
        basePrice: Double,
        servicesCost: Double,
        taxes: Double,
        totalCost: Double,
        selectedServicesCsv: String,
        downPayment: Double,
        cancellationAgreed: Boolean,
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val client = currentClientState.value
            if (client == null) {
                onResult("يرجى تسجيل الدخول كعميل أولاً للحجز!")
                return@launch
            }

            val result = repository.verifyAndBlockLockBooking(
                clientId = client.id,
                venueId = venue.id,
                date = date,
                basePrice = basePrice,
                servicesCost = servicesCost,
                taxes = taxes,
                totalCost = totalCost,
                selectedServicesCsv = selectedServicesCsv,
                downPayment = downPayment,
                cancellationTermsAgreed = cancellationAgreed
            )

            when (result) {
                is BookingResult.Success -> {
                    activeBookingHold.value = result.booking
                    selectedDate.value = date
                    // Set count
                    val now = System.currentTimeMillis()
                    holdTimeRemainingSec.value = ((result.booking.expirationTimestamp - now) / 1000).toInt()
                    navigateTo("checkout")
                    onResult(null)
                }
                is BookingResult.Failure -> {
                    onResult(result.message)
                }
            }
        }
    }

    fun cancelActiveHold() {
        viewModelScope.launch {
            val currentHold = activeBookingHold.value
            if (currentHold != null) {
                repository.cancelBooking(currentHold.id)
                activeBookingHold.value = null
                holdTimeRemainingSec.value = 0
                navigateTo("user_home")
            }
        }
    }

    fun completeCheckoutAndSign(
        clientName: String,
        signature: String,
        paymentMethod: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val currentHold = activeBookingHold.value
            if (currentHold == null) {
                onResult(false)
                return@launch
            }

            val success = repository.confirmPaymentAndCreateContract(
                bookingId = currentHold.id,
                clientName = clientName,
                signature = signature,
                paymentMethod = paymentMethod
            )

            if (success) {
                // Clear state
                activeBookingHold.value = null
                holdTimeRemainingSec.value = 0
                navigateTo("bookings")
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    // Client Add Review
    fun addReview(venueId: Long, rating: Int, comment: String) {
        viewModelScope.launch {
            val client = currentClientState.value ?: return@launch
            val user = currentUserState.value ?: return@launch
            repository.insertReview(
                Review(
                    clientId = client.id,
                    clientName = user.fullName,
                    venueId = venueId,
                    rating = rating,
                    comment = comment,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // CRM / Venue Owner actions
    fun addManualBlock(venueId: Long, date: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val owner = currentUserState.value ?: return@launch
            val success = repository.blockDateManually(owner.id, venueId, date)
            onResult(success)
        }
    }

    fun addNewVenue(
        nameAr: String,
        nameEn: String,
        descAr: String,
        descEn: String,
        city: String,
        address: String,
        capacity: Int,
        basePrice: Double,
        packages: List<Pair<String, Double>> // List of package name to price
    ) {
        viewModelScope.launch {
            val owner = currentUserState.value ?: return@launch
            val vId = repository.insertVenue(
                Venue(
                    ownerId = owner.id,
                    name = nameEn,
                    nameAr = nameAr,
                    description = descEn,
                    descriptionAr = descAr,
                    city = city.trim(),
                    address = address,
                    capacity = capacity,
                    basePrice = basePrice,
                    imageUrl = ""
                )
            )

            // Add selected default packages
            packages.forEach { p ->
                repository.insertPackageItem(
                    PackageItem(
                        venueId = vId,
                        name = p.first,
                        nameAr = p.first,
                        cost = p.second,
                        category = "CUSTOM"
                    )
                )
            }
        }
    }

    fun registerNewOwner(
        fullName: String,
        email: String,
        phone: String,
        crNumber: String,
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val exists = repository.getUserByEmail(email)
            if (exists != null) {
                onResult("البريد الإلكتروني مسجل بالفعل!")
                return@launch
            }

            val uId = repository.insertUser(
                User(
                    username = email.substringBefore("@"),
                    email = email,
                    passwordHash = "owner123",
                    role = "VENUE_OWNER",
                    fullName = fullName
                )
            )

            repository.insertVenueOwner(
                VenueOwner(
                    userId = uId,
                    phone = phone,
                    businessLicense = crNumber,
                    verificationStatus = "PENDING"
                )
            )
            onResult(null)
        }
    }

    // Administration verification
    fun setOwnerStatus(ownerId: Long, status: String) {
        viewModelScope.launch {
            // Find owner
            val allPending = database.userDao().getPendingVenueOwnersFlow().firstOrNull() ?: emptyList()
            allPending.find { it.id == ownerId }?.let { owner ->
                val updated = owner.copy(verificationStatus = status)
                repository.updateVenueOwner(updated)
            }
        }
    }

    fun archivePastEvents(daysAgo: Int, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dateStr = format.format(calendar.time)
            val count = repository.archivePastBookings(dateStr)
            onResult(count)
        }
    }

    // Compose helper flow retrievers
    fun getPackagesForVenueFlow(venueId: Long): Flow<List<PackageItem>> = repository.getPackagesForVenueFlow(venueId)
    
    fun getApprovedBookingsForVenueFlow(venueId: Long): Flow<List<ApprovedBooking>> = repository.getApprovedBookingsForVenueFlow(venueId)
    
    fun getClientBookingsFlow(clientId: Long): Flow<List<Booking>> = repository.getClientBookingsFlow(clientId)
    
    suspend fun getContractByBookingId(bookingId: Long): Contract? = repository.getContractByBookingId(bookingId)
    
    suspend fun getInvoiceByBookingId(bookingId: Long): Invoice? = repository.getInvoiceByBookingId(bookingId)
}
