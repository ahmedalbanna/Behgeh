package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehgehAppContent(viewModel: BehgehViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUserState.collectAsState()

    // Force RTL direction for gorgeous authentic Arabic look
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "بهجة للمناسبات والأفراح",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        // Quick switch role dialog helper
                        var showRoleSwitch by remember { mutableStateOf(false) }
                        IconButton(onClick = { showRoleSwitch = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "تغيير الحساب والدور",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (showRoleSwitch) {
                            AlertDialog(
                                onDismissRequest = { showRoleSwitch = false },
                                title = { Text("لوحة محاكاة الأدوار البرمجية", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("اختر الدور البرمجي الحسي لتجربة لوحة التحكم المخصصة لخدمته:", style = MaterialTheme.typography.bodyMedium)
                                        
                                        Button(
                                            onClick = {
                                                viewModel.loginAs(User(id = 3, username = "client", email = "ahmed@behgeh.com", passwordHash = "client123", role = "CLIENT", fullName = "أحمد البنا"))
                                                showRoleSwitch = false
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("عميل (Client): البحث وحجز الباقات والتوقيع")
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.loginAs(User(id = 1, username = "owner_riyadh", email = "riyadh@behgeh.com", passwordHash = "owner123", role = "VENUE_OWNER", fullName = "عبدالرحمن آل سعود"))
                                                showRoleSwitch = false
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                        ) {
                                            Text("مالك قاعة (Owner): إدارة CRM والكتل والأيام")
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.loginAs(User(id = 4, username = "admin", email = "admin@behgeh.com", passwordHash = "admin123", role = "ADMIN", fullName = "مدير المنصة"))
                                                showRoleSwitch = false
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                                        ) {
                                            Text("مدير نظام (Admin): تدقيق التراخيص والأرشفة")
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showRoleSwitch = false }) {
                                        Text("إلغاء")
                                    }
                                }
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (currentUser != null && currentUser!!.role == "CLIENT") {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == "user_home" || currentScreen == "details",
                            onClick = { viewModel.navigateTo("user_home") },
                            icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                            label = { Text("تصفح القاعات") }
                        )
                        NavigationBarItem(
                            selected = currentScreen == "ai_consult",
                            onClick = { viewModel.navigateTo("ai_consult") },
                            icon = { Icon(Icons.Default.Star, contentDescription = "مستشار بهجة AI") },
                            label = { Text("مستشار بهجة AI") }
                        )
                        NavigationBarItem(
                            selected = currentScreen == "bookings",
                            onClick = { viewModel.navigateTo("bookings") },
                            icon = { Icon(Icons.Default.List, contentDescription = "حجوزاتي") },
                            label = { Text("عقودي وحجوزاتي") }
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    "user_home" -> UserHomeScreen(viewModel)
                    "details" -> VenueDetailsScreen(viewModel)
                    "ai_consult" -> AiConsultScreen(viewModel)
                    "checkout" -> CheckoutScreen(viewModel)
                    "bookings" -> MyBookingsScreen(viewModel)
                    "owner_dashboard" -> OwnerDashboardScreen(viewModel)
                    "admin_dashboard" -> AdminDashboardScreen(viewModel)
                    else -> UserHomeScreen(viewModel)
                }
            }
        }
    }
}

// ----------------- CLIENT SCREENS -----------------

@Composable
fun UserHomeScreen(viewModel: BehgehViewModel) {
    val venues by viewModel.allVenues.collectAsState()
    val currentUser by viewModel.currentUserState.collectAsState()

    var selectedCity by remember { mutableStateOf("الكل") }
    var capacityFilter by remember { mutableStateOf(100f) }
    var priceFilter by remember { mutableStateOf(20000f) }

    // Filter venues
    val filteredVenues = venues.filter { v ->
        val matchCity = (selectedCity == "الكل" || v.city == selectedCity)
        val matchCapacity = v.capacity >= capacityFilter.toInt()
        val matchPrice = v.basePrice <= priceFilter.toDouble()
        matchCity && matchCapacity && matchPrice
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "مرحباً بك يا ${currentUser?.fullName ?: "ضيفنا العزيز"} ✨",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "أوجد قاعتك المثالية من كتل الحجز الكاملة، وصمم باقة الضيافة والتصوير والديكور الخاصة بمناسبتك بكل بساطة بهجة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // City Quick Selector
        item {
            Text("اختر مدينة الحفل:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val cities = listOf("الكل", "الرياض", "جدة", "الدمام")
                items(cities) { city ->
                    val isSelected = selectedCity == city
                    Button(
                        onClick = { selectedCity = city },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(city)
                    }
                }
            }
        }

        // Filters card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("خيارات البحث المتقدم والتصفية الفورية:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("الحد الأدنى لعدد المدعوين: ${capacityFilter.toInt()} ضيف", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Slider(
                        value = capacityFilter,
                        onValueChange = { capacityFilter = it },
                        valueRange = 50f..600f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("الميزانية القصوى للقاعة الأساسية: ${priceFilter.toInt()} ر.س", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Slider(
                        value = priceFilter,
                        onValueChange = { priceFilter = it },
                        valueRange = 5000f..25000f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }

        // Available venues header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "القاعات الفاخرة المتاحة (${filteredVenues.size}):",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (filteredVenues.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لم نجد قاعات مطابقة تماماً للمرشحات المحددة.", textAlign = TextAlign.Center)
                        Text("يرجى تقليل قيود عدد الضيوف أو زيادة نطاق السعر.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        items(filteredVenues) { venue ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectVenue(venue) }
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    // Decorative colored header holding title banner with a gorgeous purple gradient from HTML
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Surface(
                                color = Color(0xFFD0BCFF),
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    "تمت المطابقة بواسطة الذكاء الاصطناعي",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    color = Color(0xFF381E72),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                venue.nameAr,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(venue.descriptionAr, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(venue.city, style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("يتسع لـ ${venue.capacity} ضيف", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("السعر الأساسي للكتلة الزمنية:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Text(
                                "${venue.basePrice.toInt()} ريال سعودي",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VenueDetailsScreen(viewModel: BehgehViewModel) {
    val venue = viewModel.selectedVenue.collectAsState().value ?: return
    val packages by viewModel.getPackagesForVenueFlow(venue.id).collectAsState(initial = emptyList())
    val approvedList by viewModel.getApprovedBookingsForVenueFlow(venue.id).collectAsState(initial = emptyList())
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedPackagesMap by viewModel.selectedPackagesMap.collectAsState()

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var agreedPolicy by remember { mutableStateOf(false) }

    // Cost Breakdown Calculating
    val basePrice = venue.basePrice
    var servicesCost = 0.0
    packages.forEach { p ->
        if (selectedPackagesMap[p.id] == true) {
            servicesCost += p.cost
        }
    }
    val taxes = (basePrice + servicesCost) * 0.15
    val totalCost = basePrice + servicesCost + taxes
    val downPayment = totalCost * 0.20 // 20% deposit

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            IconButton(onClick = { viewModel.navigateTo("user_home") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "عودة")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الرجوع للقاعات", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                Color(0xFF1E143F)
                            )
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                // Outer column to hold content and alignment
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Row with matching badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFD0BCFF),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text(
                                text = "تمت المطابقة بواسطة الذكاء الاصطناعي",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF381E72),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Bottom Row: Location and Title on the left, floating price card on the right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                venue.nameAr,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${venue.city}، ${venue.address}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Floating Price Card to match exact HTML design spec
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "السعر الأساسي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    "${venue.basePrice.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "ريال",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("تفاصيل القاعة المعمارية والخدمية:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(venue.descriptionAr, style = MaterialTheme.typography.bodyLarge)
        }

        // INTERACTIVE CALENDAR FOR TANDEM BOOKINGS AND AVOID DOUBLE BOOKINGS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "تقويم حجز الكتل الزمنية والتواريخ المتاحة:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "تتم حجوزاتنا على أساس الكتلة اليومية الكاملة لتجهيزات الزفاف، تحقق من الأيام الشاغرة أدناه:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated 14 days calendar overlay
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val displayFormat = SimpleDateFormat("dd MMM", Locale("ar"))
                    val dates = mutableListOf<String>()
                    val calendar = Calendar.getInstance()
                    for (i in 0..11) {
                        dates.add(dateFormat.format(calendar.time))
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Custom responsive grid of dates
                        for (row in 0..2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (col in 0..3) {
                                    val index = row * 4 + col
                                    if (index < dates.size) {
                                        val dateStr = dates[index]
                                        val isTaken = approvedList.any { it.bookingDate == dateStr }
                                        val isSelected = selectedDate == dateStr
                                        
                                        // Format the text label in Arabic
                                        val dateObj = dateFormat.parse(dateStr)
                                        val labelText = if (dateObj != null) displayFormat.format(dateObj) else dateStr

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(100))
                                                .background(
                                                    when {
                                                        isTaken -> Color(0xFFFFDAD6) // Taken pinkish bg from design HTML
                                                        isSelected -> MaterialTheme.colorScheme.primary // Selected royal purple
                                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) // Normal tint
                                                    }
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color(0xFFD0BCFF) else Color.Transparent,
                                                    RoundedCornerShape(100)
                                                )
                                                .clickable(enabled = !isTaken) {
                                                    viewModel.selectedDate.value = dateStr
                                                }
                                                .padding(vertical = 12.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = labelText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        isTaken -> Color(0xFF410002) // Reddish dark text
                                                        isSelected -> Color.White
                                                        else -> MaterialTheme.colorScheme.onBackground
                                                    }
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                if (isTaken) {
                                                    // Absolute red dot indicator from Design HTML
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(RoundedCornerShape(100))
                                                            .background(Color(0xFFB3261E))
                                                    )
                                                } else {
                                                    Text(
                                                        text = "متاحة",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 9.sp,
                                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
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
            }
        }

        // PACKAGE BUILDER (بناء الباقات المخصصة)
        item {
            Text("بناء وتصميم الباقة المخصصة:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("ادمج باقات الأجهزة والتموين والتصوير لتخطيط حفلك متكاملاً في بهجة تلقائياً:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }

        items(packages) { pack ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedPackagesMap[pack.id] == true) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.togglePackageItem(pack.id) }
                    .border(
                        1.5.dp,
                        if (selectedPackagesMap[pack.id] == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        RoundedCornerShape(24.dp)
                    ),
                elevation = CardDefaults.cardElevation(if (selectedPackagesMap[pack.id] == true) 4.dp else 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(
                            pack.nameAr, 
                            fontWeight = FontWeight.Bold, 
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedPackagesMap[pack.id] == true) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            pack.description, 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = if (selectedPackagesMap[pack.id] == true) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(
                        modifier = Modifier.weight(0.5f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "+ ${pack.cost.toInt()} ر.س",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Checkbox(
                            checked = selectedPackagesMap[pack.id] == true,
                            onCheckedChange = { viewModel.togglePackageItem(pack.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                checkmarkColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // AUTOCALCULATED BILLS BREAKDOWN AND TRANSACTION DOUBLE-BOOK HOLD BLOCK
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "فاتورة التكاليف التلقائية المجدولة:", 
                        fontWeight = FontWeight.Bold, 
                        style = MaterialTheme.typography.titleMedium, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("السعر الأساسي للقصر:", style = MaterialTheme.typography.bodyMedium)
                        Text("${basePrice.toInt()} ر.س", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("مجموع الخدمات والتموين والباقات:", style = MaterialTheme.typography.bodyMedium)
                        Text("${servicesCost.toInt()} ر.س", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("ضريبة القيمة المضافة (15%):", style = MaterialTheme.typography.bodyMedium)
                        Text("${taxes.toInt()} ر.س", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("المجموع الإجمالي للتكاليف:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${totalCost.toInt()} ر.س", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("عربون التأمين والمقدم المطلوب (20%):", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${downPayment.toInt()} ر.س", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = agreedPolicy,
                    onCheckedChange = { agreedPolicy = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Text(
                    "أوافق على سياسة إلغاء الحجز والتعاقد والشروط القانونية لبهجة.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { agreedPolicy = !agreedPolicy }
                )
            }
        }

        errorMessage?.let { error ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(error, color = Color.Red, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center)
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (selectedDate.isEmpty()) {
                        errorMessage = "يرجى اختيار تاريخ المناسبة من التقويم أولاً!"
                        return@Button
                    }
                    if (!agreedPolicy) {
                        errorMessage = "يرجى الموافقة على الشروط وسياسة إلغاء عقود بهجة!"
                        return@Button
                    }
                    errorMessage = null

                    val selectedIds = packages.filter { selectedPackagesMap[it.id] == true }.map { it.id }.joinToString(",")

                    viewModel.requestBlockLockHold(
                        venue = venue,
                        date = selectedDate,
                        basePrice = basePrice,
                        servicesCost = servicesCost,
                        taxes = taxes,
                        totalCost = totalCost,
                        selectedServicesCsv = selectedIds,
                        downPayment = downPayment,
                        cancellationAgreed = true
                    ) { error ->
                        if (error != null) {
                            errorMessage = error
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("قفل الموعد مؤقتاً والتعاقد الفوري", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun CheckoutScreen(viewModel: BehgehViewModel) {
    val holdBooking by viewModel.activeBookingHold.collectAsState()
    val secondsLeft by viewModel.holdTimeRemainingSec.collectAsState()
    val selectedVenue by viewModel.selectedVenue.collectAsState()

    var clientName by remember { mutableStateOf("") }
    var signature by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("MADA") }
    var processing by remember { mutableStateOf(false) }

    if (holdBooking == null || selectedVenue == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("لا توجد معاملة حجز نشطة أو انتهى وقت قفل المعاملة.")
            Button(onClick = { viewModel.navigateTo("user_home") }) {
                Text("تصفح القاعات")
            }
        }
        return
    }

    // Format remaining minutes safely
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timeStr = String.format("%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9DEDC)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFFF2B8B5), RoundedCornerShape(28.dp))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Immersive Red Circle Bullet from Design HTML
                            Canvas(modifier = Modifier.size(10.dp)) {
                                drawCircle(color = Color(0xFFB3261E))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "تأمين الحجز اللحظي نشط! 🔒",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB3261E),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "تم حظر وتأمين هذا التاريخ مؤقتاً لك عبر بروتوكول قفل المعاملة اللحظي الحصري لـ 'بهجة'. يرجى إتمام توقيع العقد وسداد العربون قبل انتهاء المهلة:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF601410)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFB3261E), RoundedCornerShape(100))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            timeStr,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }

        item {
            Text("تفاصيل الحجز المؤمّن:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("${selectedVenue!!.nameAr}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("موعد وتاريخ الحفل: ${holdBooking!!.bookingDate}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("تكلفة القصر والباقات متكاملة: ${holdBooking!!.totalCost.toInt()} ريال سعودي", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("عربون التأمين والمقدم لربط الحجز (20%): ${holdBooking!!.downPayment.toInt()} ريال سعودي", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        item {
            Text("العقد القانوني الموحد والتوقيع الإلكتروني (e-Signature):", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "عقد إجارة قاعة أفراح رقم #${holdBooking!!.id}",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "بموجب هذا العقد الإلكتروني الموثق، يتعهد الطرف الأول (مالك القصر) بتهيئة وتجهيز القاعة والباقات المحددة للطرف الثاني (العميل) في تاريخ ${holdBooking!!.bookingDate}. يلتزم العميل بسداد عربون تأمين مقداره ${holdBooking!!.downPayment.toInt()} ر.س فورياً، وسداد المتبقي قبل موعد المناسبة بـ 15 يوماً. يخضع العقد لسياسة الإلغاء الإدارية لبهجة.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        // INPUTS
        item {
            OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = { Text("الاسم الكامل للمتعاقد (الطرف الثاني)*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = signature,
                onValueChange = { signature = it },
                label = { Text("أكتب اسمك هنا لاعتماده توقيعاً إلكترونياً (e-Signature)*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("مثال: احمد كمال البنا") }
            )
        }

        item {
            Text("بوابة السداد الإلكتروني الآمنة:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                val methods = listOf("MADA" to "مدى", "VISA" to "فيزا", "APPLE_PAY" to "Apple Pay")
                methods.forEach { (m, label) ->
                    val isSel = paymentMethod == m
                    Button(
                        onClick = { paymentMethod = m },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.15f),
                            contentColor = if (isSel) Color.White else Color.DarkGray
                        )
                    ) {
                        Text(label)
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (clientName.isBlank() || signature.isBlank()) return@Button
                    processing = true
                    viewModel.completeCheckoutAndSign(
                        clientName = clientName,
                        signature = signature,
                        paymentMethod = paymentMethod
                    ) { ok ->
                        processing = false
                    }
                },
                enabled = clientName.isNotBlank() && signature.isNotBlank() && !processing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (processing) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("سداد العربون (${holdBooking!!.downPayment.toInt()} ر.س) وتوقيع العقد", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            TextButton(
                onClick = { viewModel.cancelActiveHold() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إلغاء الحجز الفوري وإطلاق القفل للعموم", color = Color.Red)
            }
        }
    }
}

@Composable
fun MyBookingsScreen(viewModel: BehgehViewModel) {
    val client by viewModel.currentClientState.collectAsState()
    val bookings by viewModel.getClientBookingsFlow(client?.id ?: 0L).collectAsState(initial = emptyList())
    val venues by viewModel.allVenues.collectAsState()
    val scope = rememberCoroutineScope()

    var showContractBookingId by remember { mutableStateOf<Long?>(null) }
    var showInvoiceBookingId by remember { mutableStateOf<Long?>(null) }
    var contractData by remember { mutableStateOf<Contract?>(null) }
    var invoiceData by remember { mutableStateOf<Invoice?>(null) }

    var reviewBookingId by remember { mutableStateOf<Long?>(null) }
    var reviewRating by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    if (bookings.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ليست لديك أي عقود أو حجوزات مسجلة بعد.", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.navigateTo("user_home") }) {
                Text("ابحث واحجز قاعتك الآن")
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("عقودك وحجوزاتك المؤكدة لبهجة 📜", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("تحقق من بطاقات الدخول الرقمية والعقود والفواتير في مكان واحد:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }

        items(bookings) { b ->
            val v = venues.find { it.id == b.venueId } ?: return@items
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (b.status == "PAID") Color(0xFF2E7D32) else Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(v.nameAr, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (b.status == "PAID") Color(0xFF2E7D32) else Color.Red,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (b.status == "PAID") "حجز وتأمين مؤكد" else "ملغية / منتهية",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("موعد الحفل الموقّع: ${b.bookingDate}", fontWeight = FontWeight.Bold)
                    Text("إجمالي التكلفة المتعاقد عليها: ${b.totalCost.toInt()} ريال سعودي", style = MaterialTheme.typography.bodyMedium)
                    Text("العربون المسدد: ${b.downPayment.toInt()} ريال سعودي", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // DIGITAL QR TICKET ELEMENT
                    if (b.status == "PAID") {
                        Text("بطاقة الدخول الرقمية المعتمدة (يوم الحفل):", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            // Synthesized interactive barcode canvas satisfying "QR Code supported tickets"
                            Canvas(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(Color.White)
                                    .border(1.dp, Color.Black)
                            ) {
                                val cols = 7
                                val sizeOfCell = size.width / cols
                                // Draw mock high-tech QR blocks
                                for (r in 0 until cols) {
                                    for (c in 0 until cols) {
                                        if ((r + c) % 2 == 0 || (r == 0 && c == 0) || (r == cols - 1 && c == 0) || (r == 0 && c == cols - 1) || (r == 3 && c == 3)) {
                                            drawRect(
                                                color = Color.Black,
                                                topLeft = Offset(c * sizeOfCell, r * sizeOfCell),
                                                size = Size(sizeOfCell, sizeOfCell)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("تذكرة دخول القفل الذكي لـ بهجة", fontWeight = FontWeight.Bold)
                                Text("كود الحجز: BGH-${b.id}-${b.bookingDate.takeLast(2)}", style = MaterialTheme.typography.bodyMedium)
                                Text("يتسع لـ ${v.capacity} ضيف مع باقة الخدمات المحددة.", style = MaterialTheme.typography.bodyMedium, fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    showContractBookingId = b.id
                                    scope.launch {
                                        contractData = viewModel.getContractByBookingId(b.id)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تفاصيل العقد المبرم", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    showInvoiceBookingId = b.id
                                    scope.launch {
                                        invoiceData = viewModel.getInvoiceByBookingId(b.id)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الفاتورة والرسوم", fontSize = 11.sp)
                            }
                        }

                        // Add Review Form
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (reviewBookingId == b.id) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("إضافة تقييمك للقاعة والخدمة بعد الحفل:", fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    for (star in 1..5) {
                                        val isSel = star <= reviewRating
                                        IconButton(onClick = { reviewRating = star }) {
                                            Icon(
                                                imageVector = if (isSel) Icons.Default.Star else Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (isSel) MaterialTheme.colorScheme.primary else Color.LightGray
                                            )
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = reviewComment,
                                    onValueChange = { reviewComment = it },
                                    label = { Text("اكتب رأيك هنا وتعليقك") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = {
                                        viewModel.addReview(v.id, reviewRating, reviewComment)
                                        reviewBookingId = null
                                        reviewComment = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("أرسل التقييم")
                                }
                            }
                        } else {
                            TextButton(onClick = { reviewBookingId = b.id }) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("هل انقضت مناسبتك؟ يرجى كتابة مراجعة وتقييم وتوثيق الخدمة")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Details Contract display
    showContractBookingId?.let { bId ->
        AlertDialog(
            onDismissRequest = { showContractBookingId = null },
            title = { Text("عقد إجارة المناسبة المبرم إلكترونياً", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                contractData?.let { c ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("رقم العقد الموثق: BGH-CTR-${c.id}", fontWeight = FontWeight.Bold)
                        Text("الطرف الأول (المالك): ${c.ownerName}")
                        Text("الطرف الثاني (العميل): ${c.clientName}")
                        Text("اسم القصر المحجوز بالكامل: ${c.venueName}")
                        Text("تاريخ ليلة الاستخدام: ${c.bookingDate}")
                        Text("إجمالي تكلفة التعاقد: ${c.totalCost.toInt()} ريال سعودي")
                        Text("الشروط والإلغاء والاسترداد:\n${c.cancellationPolicy}", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text("التوقيع الإلكتروني المعتمد للعميل (الطرف الثاني):\n✍️ ${c.eSignature}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } ?: CircularProgressIndicator()
            },
            confirmButton = {
                TextButton(onClick = { showContractBookingId = null }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Modal Invoice details display
    showInvoiceBookingId?.let { bId ->
        AlertDialog(
            onDismissRequest = { showInvoiceBookingId = null },
            title = { Text("فاتورة تفاصيل الرسوم والسداد المالي", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                invoiceData?.let { inv ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("رقم الفاتورة: ${inv.invoiceNo}", fontWeight = FontWeight.Bold)
                        Text("العميل: ${inv.clientName}")
                        Text("القصر المحجوز: ${inv.venueName}")
                        Text("تاريخ الحفل المعتمد: ${inv.eventDate}")
                        Divider()
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("السعر الأساسي للقاعة:")
                            Text("${inv.basePrice.toInt()} ر.س")
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("مجموع الباقات والتموين المضاف:")
                            Text("${inv.servicesCost.toInt()} ر.س")
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("ضريبة القيمة المضافة (15%):")
                            Text("${inv.taxes.toInt()} ر.س")
                        }
                        Divider()
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("إجمالي الفاتورة شامل الضرائب:", fontWeight = FontWeight.Bold)
                            Text("${inv.totalAmount.toInt()} ريال سعودي", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("الحالة المالية للفاتورة:")
                            Text(inv.status, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                } ?: CircularProgressIndicator()
            },
            confirmButton = {
                TextButton(onClick = { showInvoiceBookingId = null }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

@Composable
fun AiConsultScreen(viewModel: BehgehViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingChat.collectAsState()

    var userText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "مستشار بهجة الذكي (AI AI Wedding Engine) 🤖✨",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "اكتب رغبتك نثرياً لتقوم نماذج الذكاء الاصطناعي بمطابقتها لحظياً مع قواعد البيانات",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            IconButton(onClick = { viewModel.clearChatHistory() }) {
                Icon(Icons.Default.Refresh, contentDescription = "مسح المحادثة")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                val bubbleBg = if (msg.isUser) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                val bubbleBorder = if (msg.isUser) MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                val alignment = if (msg.isUser) Alignment.End else Alignment.Start

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .background(bubbleBg, RoundedCornerShape(12.dp))
                            .border(1.dp, bubbleBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            if (isAnalyzing) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("مستشارك الذكي يطابق طلبك مع قواعد بيانات قاعات بهجة الرياض وجدة والدمام...", fontSize = 11.sp, fontStyle = FontStyle.Italic)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // INPUT TYPEBAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userText,
                onValueChange = { userText = it },
                placeholder = { Text("مثال: أريد قاعة زفاف تتسع لـ 200 ضيف في الرياض بحدود 15000 ريال") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (userText.isNotBlank()) {
                        viewModel.sendUserChatMessage(userText)
                        userText = ""
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .size(56.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Color.White)
            }
        }
    }
}

// ----------------- OWNER SERVICES (CRM) -----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(viewModel: BehgehViewModel) {
    val ownerBookings by viewModel.ownerBookings.collectAsState()
    val ownerVenues by viewModel.ownerVenues.collectAsState()
    val ownerState by viewModel.currentOwnerState.collectAsState()

    var showAddVenueDialog by remember { mutableStateOf(false) }
    var showBlockDateDialog by remember { mutableStateOf(false) }

    // State of new venue form
    var venueNameAr by remember { mutableStateOf("") }
    var venueNameEn by remember { mutableStateOf("") }
    var venueDescAr by remember { mutableStateOf("") }
    var venueDescEn by remember { mutableStateOf("") }
    var venueCity by remember { mutableStateOf("الرياض") }
    var venueAddress by remember { mutableStateOf("") }
    var venueCapacity by remember { mutableStateOf("200") }
    var venueBasePrice by remember { mutableStateOf("12000") }

    // Manual Block Date forms
    var blockDateStr by remember { mutableStateOf("") }
    var selectedVenueIdForBlock by remember { mutableStateOf(0L) }
    var blockMessage by remember { mutableStateOf<String?>(null) }

    // CRM Statistics
    val paidBookings = ownerBookings.filter { it.status == "PAID" && it.clientId > 0L }
    val totalRevenue = paidBookings.sumOf { it.downPayment } // Revenues as down payment sum
    val totalWeddings = paidBookings.size
    val platformCommissions = totalRevenue * 0.05 // 5% platform standard commission
    val occupancyRate = (paidBookings.size / 30.0 * 100).toInt().coerceIn(0, 100) // occupancy out of last 30 days scale

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBox, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "مرحباً بك في بوابة شركاء بهجة لملاك القاعات 🏰✨",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "تابع حجوزاتك وإيراداتك اللحظية ومستوى الإشغال الإداري لقصورك وعقاراتك.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    ownerState?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "رقم السجل التجاري والترخيص: ${it.businessLicense} | حالة توثيق القصر: ${it.verificationStatus}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (it.verificationStatus == "APPROVED") Color(0xFF2E7D32) else Color.Red
                        )
                    }
                }
            }
        }

        // STATS CARDS GRID (CRM Analytics)
        item {
            Text("لوحة التحليلات المتقدمة لإحصاءات الأعمال (CRM):", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("إجمالي الإيرادات", fontSize = 11.sp, color = Color.Gray)
                        Text("${totalRevenue.toInt()} ر.س", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("معدل إشغال الكتل", fontSize = 11.sp, color = Color.Gray)
                        Text("$occupancyRate%", fontWeight = FontWeight.Bold, color = Color.Blue, fontSize = 16.sp)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("عمولة المنصة مستحقة", fontSize = 11.sp, color = Color.Gray)
                        Text("${platformCommissions.toInt()} ر.س", fontWeight = FontWeight.Bold, color = Color(0xFF5D4037), fontSize = 14.sp)
                    }
                }
            }
        }

        // DOCK CONTROLS
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { showAddVenueDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إدراج قاعة جديدة")
                }
                Button(
                    onClick = { showBlockDateDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حظر وإغلاق أيام يدوياً")
                }
            }
        }

        // Venues List owned by owner
        item {
            Text(
                "عقارات وقاعات مناسباتك المسجلة (${ownerVenues.size}):",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(ownerVenues) { v ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(v.nameAr, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${v.city} - ${v.address}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text("السعة: ${v.capacity} ضيف | السعر اليومي: ${v.basePrice.toInt()} ر.س", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Active Booking Requests List
        item {
            Text(
                "طلبات الحجوزات والتعاقد الكلية والمنفذة (${ownerBookings.size}):",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(ownerBookings) { b ->
            val vName = ownerVenues.find { it.id == b.venueId }?.nameAr ?: "قصر مناسبات"
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (b.clientId == 0L) Color.LightGray.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("القاعة: $vName", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (b.clientId == 0L) "حظر وحجب يدوي" else "حجز عميل مدفوع",
                            color = if (b.clientId == 0L) Color.Gray else Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("تاريخ ليلة الحجز: ${b.bookingDate}", fontWeight = FontWeight.Bold)
                    if (b.clientId > 0L) {
                        Text("إجمالي القيمة: ${b.totalCost.toInt()} ريال سعودي", style = MaterialTheme.typography.bodyMedium)
                        Text("العربون المحصل: ${b.downPayment.toInt()} ريال سعودي", color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    // Modal dialog to add a new venue
    if (showAddVenueDialog) {
        AlertDialog(
            onDismissRequest = { showAddVenueDialog = false },
            title = { Text("إدراج قصر قاعات مناسبات جديدة في بهجة") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(value = venueNameAr, onValueChange = { venueNameAr = it }, label = { Text("اسم القاعة بالعربية") })
                    }
                    item {
                        OutlinedTextField(value = venueNameEn, onValueChange = { venueNameEn = it }, label = { Text("اسم القاعة بالإنجليزية") })
                    }
                    item {
                        OutlinedTextField(value = venueDescAr, onValueChange = { venueDescAr = it }, label = { Text("وصف القاعة بالعربية") })
                    }
                    item {
                        OutlinedTextField(value = venueDescEn, onValueChange = { venueDescEn = it }, label = { Text("وصف القاعة بالإنجليزية") })
                    }
                    item {
                        OutlinedTextField(value = venueCity, onValueChange = { venueCity = it }, label = { Text("المدينة (الرياض، جدة، الدمام)") })
                    }
                    item {
                        OutlinedTextField(value = venueAddress, onValueChange = { venueAddress = it }, label = { Text("العنوان والجغرافيا") })
                    }
                    item {
                        OutlinedTextField(value = venueCapacity, onValueChange = { venueCapacity = it }, label = { Text("السعة الإستيعابية للضيوف") })
                    }
                    item {
                        OutlinedTextField(value = venueBasePrice, onValueChange = { venueBasePrice = it }, label = { Text("السعر الأساسي للكتلة") })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addNewVenue(
                            nameAr = venueNameAr,
                            nameEn = venueNameEn,
                            descAr = venueDescAr,
                            descEn = venueDescEn,
                            city = venueCity,
                            address = venueAddress,
                            capacity = venueCapacity.toIntOrNull() ?: 200,
                            basePrice = venueBasePrice.toDoubleOrNull() ?: 12000.0,
                            packages = listOf(
                                "ضيافة تموين: بوفيه فاخر متكامل" to 4000.0,
                                "كوشة: تنسيق ورد طبيعي فاخر" to 3000.0
                            )
                        )
                        showAddVenueDialog = false
                    }
                ) { Text("تسجيل وإرسال للمراجعة") }
            },
            dismissButton = {
                TextButton(onClick = { showAddVenueDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Modal dialog to block dates manually
    if (showBlockDateDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDateDialog = false },
            title = { Text("حظر وإغلاق يوم يدوي في تقاويم بهجة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("يستخدم هذا الخيار لحجب تواريخ معينة تمت لأفراح تم الاتفاق عليها خارج الإنترنت بهدف منع الحجز المزدوج:")
                    OutlinedTextField(
                        value = blockDateStr,
                        onValueChange = { blockDateStr = it },
                        label = { Text("التاريخ المستهدف بصيغة YYYY-MM-DD") },
                        placeholder = { Text("مثال: 2026-06-15") }
                    )
                    
                    Text("اختر قصرك المستهدف:")
                    ownerVenues.forEach { v ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVenueIdForBlock = v.id }
                                .background(if (selectedVenueIdForBlock == v.id) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedVenueIdForBlock == v.id, onClick = { selectedVenueIdForBlock = v.id })
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(v.nameAr)
                        }
                    }

                    blockMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (blockDateStr.isNotBlank() && selectedVenueIdForBlock > 0L) {
                            viewModel.addManualBlock(selectedVenueIdForBlock, blockDateStr.trim()) { ok ->
                                if (ok) {
                                    blockMessage = "تم سد وحظر التاريخ بالكامل بنجاح!"
                                    showBlockDateDialog = false
                                } else {
                                    blockMessage = "فشل! الموعد متداخل أو محجوز مسبقاً!"
                                }
                            }
                        }
                    }
                ) { Text("تأكيد الحجب والمنع") }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDateDialog = false }) { Text("رجوع") }
            }
        )
    }
}

// ----------------- ADMIN DASHBOARD -----------------

@Composable
fun AdminDashboardScreen(viewModel: BehgehViewModel) {
    val pendingOwners by viewModel.pendingOwnersFlow.collectAsState()
    val allInvoices by viewModel.allInvoicesFlow.collectAsState()

    var archiveDays by remember { mutableStateOf("30") }
    var archiveStatus by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF5D4037)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "بوابة الإدارة التقنية والرقابة الكلية لبهجة 🛠️🔍",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "مراجعة وتدقيق المستندات القانونية لتراخيص ملاك القاعات الجدد وتتبع العمليات النقدية والعمولات الإدارية.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Verification Section
        item {
            Text("ملاك قاعات بانتظار المراجعة والتدقيق القانوني (${pendingOwners.size}):", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (pendingOwners.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("لا يوجد ملاك قيد الانتظار حالياً، تم تدقيق كافة الشركاء الجدد.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                }
            }
        }

        items(pendingOwners) { owner ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("السجل التجاري المرخص: ${owner.businessLicense}", fontWeight = FontWeight.Bold)
                    Text("رقم هاتف الشريك: ${owner.phone}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.setOwnerStatus(owner.id, "APPROVED") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("اعتماد وتوثيق الترخيص")
                        }
                        Button(
                            onClick = { viewModel.setOwnerStatus(owner.id, "REJECTED") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("رفض لعدم المطابقة")
                        }
                    }
                }
            }
        }

        // Platform Receipts
        item {
            Text("دفتر تتبع المعاملات والرسوم لجميع القاعات (${allInvoices.size}):", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        items(allInvoices) { inv ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("رقم الفاتورة: ${inv.invoiceNo}", fontWeight = FontWeight.Bold)
                        Text(inv.status, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                    Text("القصر: ${inv.venueName}", style = MaterialTheme.typography.bodyMedium)
                    Text("تاريخ الحفل: ${inv.eventDate}", style = MaterialTheme.typography.bodyMedium)
                    Text("إجمالي التكلفة: ${inv.totalAmount.toInt()} ريال سعودي", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Dynamic Archiving Facility for actives database
        item {
            Text("أرشفة سجلات قاعدة البيانات القديمة المنزلية:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("للمحافظة على أعلى مستويات الاستجابة والسرعة لقواعد البيانات النشطة، يعزل النظام السجلات القديمة ويحفظها بمستودعات مستقلة:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = archiveDays,
                        onValueChange = { archiveDays = it },
                        label = { Text("أرشفة الحجوزات المنتهية الأقدم من (أيام)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.archivePastEvents(archiveDays.toIntOrNull() ?: 30) { count ->
                                archiveStatus = "تم نقل وترحيل $count حجز وفاتورة منتهية بنجاح إلى سجلات الأرشيف المعزولة!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                    ) {
                        Text("بدء الأرشفة والترحيل الآلي")
                    }

                    archiveStatus?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(it, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
