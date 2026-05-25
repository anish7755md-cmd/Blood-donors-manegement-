package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.database.BloodDatabase
import com.example.database.DonorEntity
import com.example.database.EmergencyRequestEntity
import com.example.repository.BloodRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BloodViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var database: BloodDatabase
    private lateinit var repository: BloodRepository
    private lateinit var viewModel: BloodViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize local SQLite Room Db
        database = Room.databaseBuilder(
            applicationContext,
            BloodDatabase::class.java,
            "blood_donors_v3.db"
        ).build()

        repository = BloodRepository(database.donorDao(), database.emergencyDao())
        viewModel = BloodViewModel(repository)

        // Seed data verification on launch
        lifecycleScope.launch {
            val count = repository.totalCount.first()
            if (count == 0) {
                seedInitialData()
            }
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel)
            }
        }
    }

    private suspend fun seedInitialData() {
        val sampleDonors = listOf(
            DonorEntity(name = "Anish Kumar", district = "Bengaluru Urban", city = "Koramangala", bloodGroup = "O+", gender = "Male", dob = "1994-04-12", age = 32, phone = "9876543210", email = "anish.k@gmail.com", availability = "Available", lastDonationDate = "2026-01-10"),
            DonorEntity(name = "Kavitha Rao", district = "Mysuru", city = "Gokulam", bloodGroup = "B+", gender = "Female", dob = "1997-08-25", age = 28, phone = "8765432109", email = "kavitha.mysore@yahoo.com", availability = "Available", lastDonationDate = "2025-11-15"),
            DonorEntity(name = "Mohammed Tariq", district = "Dakshina Kannada", city = "Mangaluru Port", bloodGroup = "A-", gender = "Male", dob = "1991-12-05", age = 34, phone = "7654321098", email = "tariq.mang@gmail.com", availability = "Available", lastDonationDate = null),
            DonorEntity(name = "Priya Deshpande", district = "Dharwad", city = "Hubballi West", bloodGroup = "AB+", gender = "Female", dob = "1999-03-30", age = 27, phone = "6543210987", email = "priya_desh@hotmail.com", availability = "Unavailable", lastDonationDate = "2026-05-01"),
            DonorEntity(name = "Raghavendra Bhat", district = "Udupi", city = "Manipal Medical", bloodGroup = "O-", gender = "Male", dob = "1988-10-15", age = 37, phone = "9900112233", email = "raghu.udupi@gmail.com", availability = "Available", lastDonationDate = "2025-09-05"),
            DonorEntity(name = "Suma Gowda", district = "Mandya", city = "Sanjay Circle", bloodGroup = "A+", gender = "Female", dob = "1996-05-18", age = 30, phone = "7766554433", email = "suma.mandya@gmail.com", availability = "Available", lastDonationDate = "2026-02-14")
        )
        for (d in sampleDonors) {
            repository.insertDonor(d)
        }

        val sampleRequests = listOf(
            EmergencyRequestEntity(patientName = "Shankarappa Hegde", bloodGroup = "O+", hospitalName = "Sanjay Gandhi Institute", location = "Bengaluru Urban", urgencyLevel = "Critical", contactPhone = "9844001122", message = "Cardiac surgery Scheduled. Urgently requires 3 units."),
            EmergencyRequestEntity(patientName = "Siddaramaiah KM", bloodGroup = "O-", hospitalName = "K.R. Hospital", location = "Mysuru", urgencyLevel = "Urgent", contactPhone = "8971002233", message = "Sepsis critical care request.")
        )
        for (r in sampleRequests) {
            repository.insertRequest(r)
        }
    }
}

// UI Colors Red & White Palette - Professional Polish Theme
val PrimaryRed = Color(0xFFBA1A1A)
val DarkRed = Color(0xFF410002)
val AccentRedBg = Color(0xFFFFDAD6)
val SoftRedAccent = Color(0xFFF9DEDC)
val PurpleAccentBg = Color(0xFFE8DEF8)
val PurpleAccentText = Color(0xFF21005D)
val BlueAccentBg = Color(0xFFD0E4FF)
val BlueAccentText = Color(0xFF001D34)
val BackgroundColor = Color(0xFFFFFBFF)
val TextDark = Color(0xFF1C1B1F)
val TextMuted = Color(0xFF757575)
val DividerGrey = Color(0xFFE2E8F0)

// Karnataka Districts Packaged List
val KARNATAKA_DISTRICTS = listOf(
    "Bengaluru Urban", "Bengaluru Rural", "Mysuru", "Mandya", "Hassan",
    "Shivamogga", "Dakshina Kannada", "Udupi", "Tumakuru", "Ballari",
    "Belagavi", "Dharwad", "Kolar", "Chikkamagaluru", "Raichur",
    "Bidar", "Kalaburagi", "Kodagu", "Bagalkote", "Chamarajanagar",
    "Chikkaballapur", "Chitradurga", "Davanagere", "Gadag", "Haveri",
    "Koppal", "Ramanagara", "Uttara Kannada", "Vijayapura", "Yadgir"
)

val BLOOD_GROUPS_LIST = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: BloodViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryRed, shape = CircleShape)
                        ) {
                            Text("🩸", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KarnaBlood",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = PrimaryRed
                            )
                            Text(
                                text = "KARNATAKA DONOR NETWORK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor),
                actions = {
                    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
                    if (isLoggedIn) {
                        IconButton(
                            onClick = { viewModel.logoutAdmin() },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .background(Color(0xFFF1F5F9), shape = CircleShape)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Log Out Admin", tint = DarkRed, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(36.dp)
                                .background(Color(0xFFF8FAFC), shape = CircleShape)
                                .border(1.dp, DividerGrey, shape = CircleShape)
                        ) {
                            Text("🔔", fontSize = 14.sp)
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier.border(width = 1.dp, color = DividerGrey)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search Tab", tint = if (selectedTab == 0) PrimaryRed else TextMuted) },
                    label = { Text("Search", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium, color = if (selectedTab == 0) PrimaryRed else TextMuted) },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        indicatorColor = AccentRedBg
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Warning, contentDescription = "Emergencies Tab", tint = if (selectedTab == 1) PrimaryRed else TextMuted) },
                    label = { Text("Emergencies", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium, color = if (selectedTab == 1) PrimaryRed else TextMuted) },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        indicatorColor = AccentRedBg
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Admin Tab", tint = if (selectedTab == 2) PrimaryRed else TextMuted) },
                    label = { Text("Admin Console", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium, color = if (selectedTab == 2) PrimaryRed else TextMuted) },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        indicatorColor = AccentRedBg
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundColor)
        ) {
            when (selectedTab) {
                0 -> PublicSearchScreen(viewModel)
                1 -> EmergencyBoardScreen(viewModel)
                2 -> AdminConsoleScreen(viewModel)
            }
        }
    }
}

// --- SCREEN 1: PUBLIC SEARCH ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicSearchScreen(viewModel: BloodViewModel) {
    val context = LocalContext.current
    
    // Filter State
    val searchBg by viewModel.searchBloodGroup.collectAsState()
    val searchDist by viewModel.searchDistrict.collectAsState()
    val searchCity by viewModel.searchCity.collectAsState()
    val onlyAvail by viewModel.showOnlyAvailable.collectAsState()
    
    val matchingDonors by viewModel.matchingDonors.collectAsState()
    val totalDonors by viewModel.totalDonorsCount.collectAsState()
    val availableDonors by viewModel.availableDonorsCount.collectAsState()
    val emergencyReqs by viewModel.emergencyRequests.collectAsState()

    var bgDropdownExpanded by remember { mutableStateOf(false) }
    var distDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // General Stats Indicators bar - Professional Polish Theme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Active Donors
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(112.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentRedBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.5f), shape = CircleShape)
                        ) {
                            Text("👥", fontSize = 14.sp)
                        }
                        Column {
                            Text(
                                text = "$totalDonors",
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = DarkRed
                            )
                            Text(
                                text = "Active Donors",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkRed.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Card 2: Urgent Requests
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(112.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftRedAccent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.5f), shape = CircleShape)
                        ) {
                            Text("🚨", fontSize = 14.sp)
                        }
                        Column {
                            Text(
                                text = "${emergencyReqs.size}",
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = PrimaryRed
                            )
                            Text(
                                text = "Urgent Requests",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRed.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        item {
            // Header Search Board
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DividerGrey),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(16.dp)
                                .background(PrimaryRed, shape = RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Find Donors Quickly",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextDark
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filter Fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Blood Group Select
                        Box(modifier = Modifier.weight(1.1f)) {
                            Card(
                                onClick = { bgDropdownExpanded = true },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, DividerGrey),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp)
                                ) {
                                    Text(
                                        text = searchBg.ifEmpty { "Blood Group" },
                                        color = if (searchBg.isEmpty()) TextMuted else PrimaryRed,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text("▼", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                            DropdownMenu(
                                expanded = bgDropdownExpanded,
                                onDismissRequest = { bgDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Any Group") },
                                    onClick = { 
                                        viewModel.searchBloodGroup.value = ""
                                        bgDropdownExpanded = false 
                                    }
                                )
                                BLOOD_GROUPS_LIST.forEach { bg ->
                                    DropdownMenuItem(
                                        text = { Text(bg) },
                                        onClick = { 
                                            viewModel.searchBloodGroup.value = bg
                                            bgDropdownExpanded = false 
                                        }
                                    )
                                }
                            }
                        }

                        // District Select
                        Box(modifier = Modifier.weight(1.5f)) {
                            Card(
                                onClick = { distDropdownExpanded = true },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, DividerGrey),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp)
                                ) {
                                    Text(
                                        text = searchDist.ifEmpty { "District" },
                                        color = if (searchDist.isEmpty()) TextMuted else TextDark,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text("▼", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                            DropdownMenu(
                                expanded = distDropdownExpanded,
                                onDismissRequest = { distDropdownExpanded = false },
                                modifier = Modifier.heightIn(max = 240.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Any District") },
                                    onClick = { 
                                        viewModel.searchDistrict.value = ""
                                        distDropdownExpanded = false 
                                    }
                                )
                                KARNATAKA_DISTRICTS.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d) },
                                        onClick = { 
                                            viewModel.searchDistrict.value = d
                                            distDropdownExpanded = false 
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // City field
                    OutlinedTextField(
                        value = searchCity,
                        onValueChange = { viewModel.searchCity.value = it },
                        placeholder = { Text("Filter by City / Area...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = DividerGrey,
                            unfocusedBorderColor = DividerGrey,
                            focusedPlaceholderColor = TextMuted,
                            unfocusedPlaceholderColor = TextMuted,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Only Available toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.showOnlyAvailable.value = !onlyAvail }
                    ) {
                        Checkbox(
                            checked = onlyAvail,
                            onCheckedChange = { viewModel.showOnlyAvailable.value = it },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show only available active donors", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        item {
            Text(
                "Filter Results (${matchingDonors.size} Matches)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextDark,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        // Search Results List
        if (matchingDonors.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🩸", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Matching Donors found", fontWeight = FontWeight.Bold, color = TextDark)
                    Text(
                        "Try clearing filters or check back later.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(matchingDonors) { donor ->
                DonorCard(donor = donor, onCall = {
                    triggerCallIntent(context, donor.phone)
                }, onWhatsApp = {
                    triggerWhatsAppIntent(context, donor.phone, donor.name, donor.bloodGroup)
                })
            }
        }
    }
}

@Composable
fun DonorCard(
    donor: DonorEntity,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DividerGrey),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Blood Drop Box - Professional Polish style
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(AccentRedBg, shape = RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                donor.bloodGroup,
                                fontWeight = FontWeight.Black,
                                color = DarkRed,
                                fontSize = 16.sp
                            )
                            Text(
                                "GROUP",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkRed.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(donor.name, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                        Text("${donor.city}, ${donor.district}", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
                
                // Active badge
                val isAvail = donor.availability == "Available"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isAvail) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        donor.availability,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAvail) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerGrey)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Age: ${donor.age} Yrs", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextMuted)
                    Text("Gender: ${donor.gender}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextMuted)
                    if (donor.lastDonationDate != null) {
                        Text("Donated: ${donor.lastDonationDate}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextMuted)
                    }
                }

                if (donor.availability == "Available") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // WhatsApp Chat circle
                        IconButton(
                            onClick = onWhatsApp,
                            modifier = Modifier
                                .size(36.dp)
                                .background(BlueAccentBg, shape = CircleShape)
                        ) {
                            Text("💬", fontSize = 15.sp)
                        }
                        // Call circle
                        IconButton(
                            onClick = onCall,
                            modifier = Modifier
                                .size(36.dp)
                                .background(PurpleAccentBg, shape = CircleShape)
                        ) {
                            Text("📞", fontSize = 14.sp)
                        }
                    }
                } else {
                    Text("Unavailable", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- SCREEN 2: EMERGENCY BOARD ---
@Composable
fun EmergencyBoardScreen(viewModel: BloodViewModel) {
    val context = LocalContext.current
    val requests by viewModel.emergencyRequests.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Emergency Request Board",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                        Text(
                            "Live emergency needs in Karnataka",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Post Emergency", modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Post Need", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (requests.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💚", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Board is completely clear!", fontWeight = FontWeight.Bold, color = TextDark)
                        Text("No active matching emergencies reported today.", fontSize = 11.sp, color = TextMuted)
                    }
                }
            } else {
                items(requests) { req ->
                    EmergencyCard(req = req, onCall = {
                        triggerCallIntent(context, req.contactPhone)
                    }, onWhatsApp = {
                        triggerCoordinatorWhatsAppIntent(context, req.contactPhone, req.patientName, req.bloodGroup)
                    })
                }
            }
        }

        if (showDialog) {
            CreateEmergencyDialog(onDismiss = { showDialog = false }, onSave = { rec ->
                viewModel.postEmergencyRequest(rec)
                showDialog = false
                Toast.makeText(context, "Emergency Request Posted Successfully!", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

@Composable
fun EmergencyCard(
    req: EmergencyRequestEntity,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DividerGrey),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Blood Group display - Professional Polish style
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(AccentRedBg, shape = RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = req.bloodGroup,
                                fontWeight = FontWeight.Black,
                                color = DarkRed,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "GROUP",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkRed.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Patient: ${req.patientName}",
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 15.sp
                        )
                        Text(
                            text = req.hospitalName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryRed
                        )
                    }
                }

                // Urgency level badge
                val isCritical = req.urgencyLevel == "Critical"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCritical) Color(0xFF1C1B1F) else Color(0xFFFFB4AB))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        req.urgencyLevel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCritical) Color.White else Color(0xFF410002)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Location: ${req.location}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
            
            if (!req.message.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, DividerGrey), shape = RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        req.message,
                        fontSize = 11.sp,
                        color = TextDark,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Call Coordinator Button
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccentBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(38.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("📞", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Coordinator", fontSize = 11.sp, color = PurpleAccentText, fontWeight = FontWeight.Bold)
                }
                
                // Chat WhatsApp Button
                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccentBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(38.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("💬", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat WhatsApp", fontSize = 11.sp, color = BlueAccentText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CreateEmergencyDialog(
    onDismiss: () -> Unit,
    onSave: (EmergencyRequestEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var bg by remember { mutableStateOf("") }
    var hospital by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf("Urgent") }
    var phone by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }

    var bgExpanded by remember { mutableStateOf(false) }
    var urgencyExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            border = BorderStroke(1.dp, DividerGrey)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(18.dp)
                            .background(PrimaryRed, shape = RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Post Emergency Blood Need",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDark
                    )
                }
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Patient Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = DividerGrey
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Blood drop select
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = bg,
                            onValueChange = {},
                            label = { Text("Blood Group") },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.Info, contentDescription = "Select", tint = PrimaryRed) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { bgExpanded = true },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = TextDark,
                                disabledBorderColor = DividerGrey,
                                disabledLabelColor = TextMuted
                            )
                        )
                        DropdownMenu(expanded = bgExpanded, onDismissRequest = { bgExpanded = false }) {
                            BLOOD_GROUPS_LIST.forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = {
                                    bg = item
                                    bgExpanded = false
                                })
                            }
                        }
                    }

                    // Urgency select
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = urgency,
                            onValueChange = {},
                            label = { Text("Urgency") },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { urgencyExpanded = true },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = TextDark,
                                disabledBorderColor = DividerGrey,
                                disabledLabelColor = TextMuted
                            )
                        )
                        DropdownMenu(expanded = urgencyExpanded, onDismissRequest = { urgencyExpanded = false }) {
                            listOf("Critical", "Urgent", "Normal").forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = {
                                    urgency = item
                                    urgencyExpanded = false
                                })
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = hospital,
                    onValueChange = { hospital = it },
                    label = { Text("Hospital Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = DividerGrey
                    )
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("City, District") },
                    placeholder = { Text("e.g. Mysuru") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = DividerGrey
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Coordinator Phone") },
                    placeholder = { Text("10 digits") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = DividerGrey
                    )
                )

                OutlinedTextField(
                    value = msg,
                    onValueChange = { msg = it },
                    label = { Text("Additional Message Details") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = DividerGrey
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = TextDark)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && bg.isNotEmpty() && hospital.isNotEmpty() && phone.length == 10) {
                                val item = EmergencyRequestEntity(
                                    patientName = name,
                                    bloodGroup = bg,
                                    hospitalName = hospital,
                                    location = location,
                                    urgencyLevel = urgency,
                                    contactPhone = phone,
                                    message = msg
                                )
                                onSave(item)
                            } else {
                                // Validation check
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Post", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// --- SCREEN 3: ADMIN CONSOLE ---
@Composable
fun AdminConsoleScreen(viewModel: BloodViewModel) {
    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    
    if (!isLoggedIn) {
        AdminLoginView(onLogin = { id, pass ->
            viewModel.loginAdmin(id, pass)
        })
    } else {
        AdminControlBoard(viewModel)
    }
}

@Composable
fun AdminLoginView(onLogin: (String, String) -> Boolean) {
    var adminId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .background(AccentRedBg, shape = CircleShape)
        ) {
            Icon(Icons.Default.Lock, contentDescription = "Lock icon", modifier = Modifier.size(32.dp), tint = PrimaryRed)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Admin Terminal", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = DarkRed)
        Text("Authorized system coordinators and management access", fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = adminId,
            onValueChange = { adminId = it },
            label = { Text("Admin ID") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryRed) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryRed,
                unfocusedBorderColor = DividerGrey
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryRed) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryRed,
                unfocusedBorderColor = DividerGrey
            )
        )

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                val success = onLogin(adminId, password)
                if (success) {
                    Toast.makeText(context, "Successfully logged in as Admin!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Incorrect Credentials!", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Access Portal", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // college reference dialog credentials helper block
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, DividerGrey),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "PROJECT EVALUATOR LOGIN CREDENTIALS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryRed,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Admin ID:", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("Blooddonorssystem", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Password:", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("blooddonor@123", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Full Dashboard with CRUD and Lists
@Composable
fun AdminControlBoard(viewModel: BloodViewModel) {
    val context = LocalContext.current
    val totalDonors by viewModel.totalDonorsCount.collectAsState()
    val allDonorsList by viewModel.allDonors.collectAsState()
    val emergencyReqs by viewModel.emergencyRequests.collectAsState()

    var showEditDonorDialog by remember { mutableStateOf(false) }
    var selectedDonorToEdit by remember { mutableStateOf<DonorEntity?>(null) }
    
    // Create new dialog
    var showCreateDonorDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Admin Dashboard", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                    Text("Database Live Synchronizer Console", fontSize = 11.sp, color = TextMuted)
                }
                Button(
                    onClick = { showCreateDonorDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Register", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Donor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            // Metrics Bar - Match search view aesthetics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Registered Donors
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentRedBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📂 REGISTRY", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = DarkRed.copy(alpha = 0.6f))
                        Column {
                            Text(
                                "$totalDonors",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = DarkRed
                            )
                            Text("Active Profiles", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkRed.copy(alpha = 0.7f))
                        }
                    }
                }

                // Active Emergency Requests
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftRedAccent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🚨 LIVE NEED", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryRed.copy(alpha = 0.6f))
                        Column {
                            Text(
                                "${emergencyReqs.size}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryRed
                            )
                            Text("Pending Resolve", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryRed.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }

        // Section: Active Donors Table Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(3.dp).height(14.dp).background(PrimaryRed, RoundedCornerShape(1.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Donor Database Registry", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
            }
        }

        // List all registered Donors with Edit / Delete actions
        if (allDonorsList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, DividerGrey)
                ) {
                    Text(
                        text = "No registered donors in native SQL database yet.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(allDonorsList) { donor ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, DividerGrey),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(AccentRedBg, shape = RoundedCornerShape(8.dp))
                                ) {
                                    Text(donor.bloodGroup, color = DarkRed, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(donor.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("${donor.city} (${donor.district})  •  ${donor.phone}", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                Text("Age: ${donor.age} Yrs", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("•", color = TextMuted, fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    donor.availability,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    color = if (donor.availability == "Available") Color(0xFF2E7D32) else Color(0xFFBA1A1A)
                                )
                            }
                        }

                        // Edit / Delete Action buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    selectedDonorToEdit = donor
                                    showEditDonorDialog = true
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFF1F5F9), shape = CircleShape)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit record", tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    viewModel.deleteDonor(donor.id)
                                    Toast.makeText(context, "Deleted record for ${donor.name}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(AccentRedBg, shape = CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete record", tint = PrimaryRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section: Active emergencies
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Box(modifier = Modifier.width(3.dp).height(14.dp).background(PrimaryRed, RoundedCornerShape(1.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Emergency Board Moderation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
            }
        }

        if (emergencyReqs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, DividerGrey)
                ) {
                    Text(
                        text = "No active emergency requests to resolve.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(emergencyReqs) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, DividerGrey)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Patient Name: ${req.patientName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(SoftRedAccent, shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(req.bloodGroup, color = PrimaryRed, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hospital: ${req.hospitalName}", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                            }
                            Text("Phone: ${req.contactPhone}", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = {
                                viewModel.resolveEmergency(req.id)
                                Toast.makeText(context, "Emergency request successfully resolved!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Resolve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Register Dialog
    if (showCreateDonorDialog) {
        AddEditDonorDialog(null, onDismiss = { showCreateDonorDialog = false }, onSave = { r ->
            viewModel.registerDonor(r) { success ->
                if (success) {
                    Toast.makeText(context, "Successfully Registered Donor!", Toast.LENGTH_SHORT).show()
                    showCreateDonorDialog = false
                } else {
                    Toast.makeText(context, "Phone number already exists in database!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // Edit Dialog
    if (showEditDonorDialog && selectedDonorToEdit != null) {
        AddEditDonorDialog(selectedDonorToEdit, onDismiss = { 
            showEditDonorDialog = false
            selectedDonorToEdit = null
        }, onSave = { updated ->
            viewModel.registerDonor(updated) { success ->
                Toast.makeText(context, "Successfully Updated Donor File!", Toast.LENGTH_SHORT).show()
                showEditDonorDialog = false
                selectedDonorToEdit = null
            }
        })
    }
}

@Composable
fun AddEditDonorDialog(
    donor: DonorEntity?,
    onDismiss: () -> Unit,
    onSave: (DonorEntity) -> Unit
) {
    var name by remember { mutableStateOf(donor?.name ?: "") }
    var email by remember { mutableStateOf(donor?.email ?: "") }
    var phone by remember { mutableStateOf(donor?.phone ?: "") }
    var gender by remember { mutableStateOf(donor?.gender ?: "Male") }
    var bg by remember { mutableStateOf(donor?.bloodGroup ?: "") }
    var district by remember { mutableStateOf(donor?.district ?: "") }
    var city by remember { mutableStateOf(donor?.city ?: "") }
    var dob by remember { mutableStateOf(donor?.dob ?: "") }
    var lastDonation by remember { mutableStateOf(donor?.lastDonationDate ?: "") }
    var availability by remember { mutableStateOf(donor?.availability ?: "Available") }

    var bgExpanded by remember { mutableStateOf(false) }
    var distExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var isAvailExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            border = BorderStroke(1.dp, DividerGrey)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(18.dp)
                            .background(PrimaryRed, shape = RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (donor == null) "Register Blood Donor" else "Update Donor Profile File",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDark
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Donor's Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = DividerGrey
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = DividerGrey
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("10 digits") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = DividerGrey
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Blood drop select
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = bg,
                            onValueChange = {},
                            label = { Text("Blood Group") },
                            trailingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryRed) },
                            readOnly = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = TextDark,
                                disabledBorderColor = DividerGrey,
                                disabledLabelColor = TextMuted
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { bgExpanded = true }
                        )
                        DropdownMenu(expanded = bgExpanded, onDismissRequest = { bgExpanded = false }) {
                            BLOOD_GROUPS_LIST.forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = {
                                    bg = item
                                    bgExpanded = false
                                })
                            }
                        }
                    }

                    // Gender Select
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            label = { Text("Gender") },
                            readOnly = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = TextDark,
                                disabledBorderColor = DividerGrey,
                                disabledLabelColor = TextMuted
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { genderExpanded = true }
                        )
                        DropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                            listOf("Male", "Female", "Other").forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = {
                                    gender = item
                                    genderExpanded = false
                                })
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // District selection dropdown
                    Box(modifier = Modifier.weight(1.5f)) {
                        OutlinedTextField(
                            value = district,
                            onValueChange = {},
                            label = { Text("District") },
                            readOnly = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = TextDark,
                                disabledBorderColor = DividerGrey,
                                disabledLabelColor = TextMuted
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { distExpanded = true }
                        )
                        DropdownMenu(
                            expanded = distExpanded,
                            onDismissRequest = { distExpanded = false },
                            modifier = Modifier.heightIn(max = 240.dp)
                        ) {
                            KARNATAKA_DISTRICTS.forEach { d ->
                                DropdownMenuItem(text = { Text(d) }, onClick = {
                                    district = d
                                    distExpanded = false
                                })
                            }
                        }
                    }

                    // City
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = DividerGrey
                        )
                    )
                }

                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = DividerGrey
                    )
                )

                OutlinedTextField(
                    value = lastDonation,
                    onValueChange = { lastDonation = it },
                    label = { Text("Last Donation Date") },
                    placeholder = { Text("YYYY-MM-DD (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = DividerGrey
                    )
                )

                // Availability Select
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = availability,
                        onValueChange = {},
                        label = { Text("Availability Status") },
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextDark,
                            disabledBorderColor = DividerGrey,
                            disabledLabelColor = TextMuted
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAvailExpanded = true }
                    )
                    DropdownMenu(expanded = isAvailExpanded, onDismissRequest = { isAvailExpanded = false }) {
                        listOf("Available", "Unavailable").forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = {
                                availability = item
                                isAvailExpanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = TextDark),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && bg.isNotEmpty() && phone.length == 10) {
                                // Calculate Age internally on Android side
                                val calculatedAge = try {
                                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    val dateObj = format.parse(dob)
                                    val birthdayCalendar = java.util.Calendar.getInstance()
                                    birthdayCalendar.time = dateObj
                                    val today = java.util.Calendar.getInstance()
                                    var age = today.get(java.util.Calendar.YEAR) - birthdayCalendar.get(java.util.Calendar.YEAR)
                                    if (today.get(java.util.Calendar.DAY_OF_YEAR) < birthdayCalendar.get(java.util.Calendar.DAY_OF_YEAR)) {
                                        age--
                                    }
                                    age
                                } catch (e: Exception) {
                                    25 // Fallback generic correct donation age
                                }

                                val entry = DonorEntity(
                                    id = donor?.id ?: 0,
                                    name = name,
                                    email = email,
                                    phone = phone,
                                    gender = gender,
                                    bloodGroup = bg,
                                    district = district,
                                    city = city,
                                    dob = dob,
                                    age = calculatedAge,
                                    lastDonationDate = lastDonation.ifEmpty { null },
                                    availability = availability
                                )
                                onSave(entry)
                            } else {
                                // Validation check fail
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Profile", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// --- SYSTEM INTENTS WRAPPERS ---
fun triggerCallIntent(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to perform dialing service", Toast.LENGTH_SHORT).show()
    }
}

fun triggerWhatsAppIntent(context: Context, phone: String, name: String, bg: String) {
    try {
        val messageTemplate = "Hello $name, this is an URGENT emergency blood request for blood group $bg under the Karnataka Blood Donor Management System. Please contact us immediately."
        val url = "https://wa.me/91$phone?text=" + Uri.encode(messageTemplate)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to launch WhatsApp application", Toast.LENGTH_SHORT).show()
    }
}

fun triggerCoordinatorWhatsAppIntent(context: Context, phone: String, name: String, bg: String) {
    try {
        val messageTemplate = "Hello Coordinator, I'm joining as a donor for Patient $name's request for blood group $bg on your Emergency Board. Please let me know how I can assist."
        val url = "https://wa.me/91$phone?text=" + Uri.encode(messageTemplate)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to launch WhatsApp application", Toast.LENGTH_SHORT).show()
    }
}
