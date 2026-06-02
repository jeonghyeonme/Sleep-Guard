package com.sleepguard

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleepguard.sensor.AccelerometerManager
import com.sleepguard.service.SleepGuardService
import com.sleepguard.util.DeviceAdminManager
import com.sleepguard.ui.theme.SleepGuardTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var accelerometerManager: AccelerometerManager
    private lateinit var deviceAdminManager: DeviceAdminManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accelerometerManager = AccelerometerManager(this)
        deviceAdminManager = DeviceAdminManager(this)

        setContent {
            SleepGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SleepGuardDashboard(
                        accelerometerManager = accelerometerManager,
                        onStartService = { startGuardService() },
                        onStopService = { stopGuardService() },
                        checkUsagePermission = { checkUsageStatsPermission() },
                        openUsageSettings = { openUsageStatsSettings() },
                        checkOverlayPermission = { checkOverlayPermission() },
                        openOverlaySettings = { openOverlaySettings() },
                        checkAdminPermission = { deviceAdminManager.isAdminActive() },
                        openAdminSettings = { deviceAdminManager.requestAdminPermission() }
                    )
                }
            }
        }
    }

    private fun startGuardService() {
        val intent = Intent(this, SleepGuardService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopGuardService() {
        val intent = Intent(this, SleepGuardService::class.java)
        stopService(intent)
    }

    private fun checkUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun openUsageStatsSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        accelerometerManager.start()
    }

    override fun onPause() {
        super.onPause()
        accelerometerManager.stop()
    }
}

@Composable
fun SleepGuardDashboard(
    accelerometerManager: AccelerometerManager,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    checkUsagePermission: () -> Boolean,
    openUsageSettings: () -> Unit,
    checkOverlayPermission: () -> Boolean,
    openOverlaySettings: () -> Unit,
    checkAdminPermission: () -> Boolean,
    openAdminSettings: () -> Unit
) {
    val data by accelerometerManager.data.collectAsState()
    val isDetected by accelerometerManager.isMovementDetected.collectAsState()
    var hasUsagePermission by remember { mutableStateOf(checkUsagePermission()) }
    var hasOverlayPermission by remember { mutableStateOf(checkOverlayPermission()) }
    var hasAdminPermission by remember { mutableStateOf(checkAdminPermission()) }

    LaunchedEffect(Unit) {
        while(true) {
            hasUsagePermission = checkUsagePermission()
            hasOverlayPermission = checkOverlayPermission()
            hasAdminPermission = checkAdminPermission()
            delay(2000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Sleep Guard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Permission Cards
        if (!hasUsagePermission) {
            PermissionCard("Usage Stats Required", "Needed to detect target apps.", openUsageSettings)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (!hasOverlayPermission) {
            PermissionCard("Overlay Required", "Needed for 'Are you asleep?' popup.", openOverlaySettings)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!hasAdminPermission) {
            PermissionCard("Device Admin Required", "Needed to lock screen on sleep.", openAdminSettings)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDetected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isDetected) "Movement Detected!" else "Live Sensor Data",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDetected) Color.White else MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SensorText("X: ${"%.3f".format(data.x)}", isDetected)
                SensorText("Y: ${"%.3f".format(data.y)}", isDetected)
                SensorText("Z: ${"%.3f".format(data.z)}", isDetected)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Service Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onStartService,
                modifier = Modifier.weight(1f),
                enabled = hasUsagePermission && hasOverlayPermission && hasAdminPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Start Guard")
            }
            
            Button(
                onClick = onStopService,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Stop Guard")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Monitoring: YouTube, Netflix",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun PermissionCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, fontSize = 14.sp)
            Button(
                onClick = onClick,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Grant")
            }
        }
    }
}

@Composable
fun SensorText(text: String, active: Boolean) {
    Text(
        text = text,
        fontSize = 16.sp,
        color = if (active) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
    )
}
