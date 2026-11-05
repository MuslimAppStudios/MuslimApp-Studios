package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AyahHadithScreen
import com.example.ui.screens.DailyTrackerScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.KiblegahViewModel
import kotlinx.coroutines.flow.collectLatest

enum class AppTab(val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    PRAYER_TIMES("Vakitler", Icons.Outlined.AccessTime, Icons.Default.AccessTime),
    AYAH_HADITH("Ayet & Hadis", Icons.Outlined.MenuBook, Icons.Default.MenuBook),
    DAILY_TRACKER("Günlük", Icons.Outlined.CheckCircle, Icons.Default.CheckCircle),
    NOTES("Notlarım", Icons.Outlined.EditNote, Icons.Default.EditNote)
}

class MainActivity : ComponentActivity() {

    private val viewModel: KiblegahViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf(AppTab.PRAYER_TIMES) }
                val snackbarHostState = remember { SnackbarHostState() }

                // Collect snackbar events from viewModel
                LaunchedEffect(Unit) {
                    viewModel.snackbarEvent.collectLatest { message ->
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                // Handle back button when on sub-screens
                BackHandler(enabled = currentTab != AppTab.PRAYER_TIMES) {
                    currentTab = AppTab.PRAYER_TIMES
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF0A3622),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent),
                                        shadowElevation = 3.dp,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_kiblegah_logo),
                                            contentDescription = "Kıblegah HD Logo",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "KIBLEGAH",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontFamily = com.example.ui.theme.CinzelFontFamily,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.8.sp
                                            ),
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = when (currentTab) {
                                                AppTab.PRAYER_TIMES -> "Namaz Vakitleri & Kıble Pusulası"
                                                AppTab.AYAH_HADITH -> "Günün Ayeti ve Hadis-i Şerifi"
                                                AppTab.DAILY_TRACKER -> "İbadet Günlüğü & Zikirmatik"
                                                AppTab.NOTES -> "Dini Not Defteri"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            AppTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.icon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        indicatorColor = EmeraldPrimary,
                                        selectedTextColor = EmeraldPrimary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Crossfade(
                        targetState = currentTab,
                        label = "screen_crossfade",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) { tab ->
                        when (tab) {
                            AppTab.PRAYER_TIMES -> PrayerTimesScreen(viewModel = viewModel)
                            AppTab.AYAH_HADITH -> AyahHadithScreen(viewModel = viewModel)
                            AppTab.DAILY_TRACKER -> DailyTrackerScreen(viewModel = viewModel)
                            AppTab.NOTES -> NotesScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
