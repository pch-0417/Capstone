package com.example.settingsapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// --- 1. 색상 정의 ---
val PrimaryColor = Color(0xFF13B6EC)
val BgLight = Color(0xFFF6F8F8)
val BgDark = Color(0xFF101D22)
val TextDark = Color(0xFF1E293B)
val TextLight = Color.White
val BorderLight = Color(0xFFE2E8F0)
val BorderDark = Color(0xFF334155)

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen(onBackClick = { finish() })
        }
    }
}

@Composable
fun SettingsScreen(onBackClick: () -> Unit = {}) {
    // 다크모드 및 색상 설정
    val isDark = isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val borderColor = Color.Gray

    // 상태 변수들
    var selectedRefreshRate by remember { mutableStateOf("Standard (1s)") }
    var areNotificationsEnabled by remember { mutableStateOf(true) }
    var on by remember { mutableStateOf(false) }  // HEAT_LAMP
    var LED by remember { mutableStateOf(false) } // LED
    var Water by remember { mutableStateOf(false) }  // HEAT_LAMP
    var pan by remember { mutableStateOf(false) } // LED

    // Preview 모드인지 확인 (Preview에서는 Firebase 연결 안 함)
    val isPreview = LocalInspectionMode.current

    // Firebase 초기화 및 리스너 연결
    if (!isPreview) {
        val database = Firebase.database
        val controlRef = database.getReference("control")

        DisposableEffect(Unit) {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val serverHeatLamp = snapshot.child("HEAT_LAMP").getValue(Boolean::class.java) ?: false
                    val serverLed = snapshot.child("LED").getValue(Boolean::class.java) ?: false
                    on = serverHeatLamp
                    LED = serverLed
                    Log.d("IoT", "DB값 수신: 온열등=$on, LED=$LED")
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("IoT", "데이터 수신 에러", error.toException())
                }
            }
            controlRef.addValueEventListener(listener)
            onDispose { controlRef.removeEventListener(listener) }
        }
    }

    // DB 업데이트 헬퍼 함수
    fun updateFirebaseState(key: String, value: Boolean, revertState: () -> Unit) {
        if (isPreview) return
        Firebase.database.getReference("control").child(key).setValue(value)
            .addOnSuccessListener { Log.d("IoT", "$key 설정 변경 성공: $value") }
            .addOnFailureListener { e ->
                Log.w("IoT", "$key 설정 변경 실패", e)
                revertState() // 실패 시 UI 스위치 되돌리기
            }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor,
                    modifier = Modifier.size(24.dp).clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            SectionHeader(text = "센서 On/Off ", textColor = textColor)
            // 1. 알림 설정
            SettingsSwitchRow(
                title = "알림 설정",
                icon = Icons.Outlined.Notifications,
                checked = areNotificationsEnabled,
                onCheckedChange = { areNotificationsEnabled = it },
                textColor = textColor
            )

            // 2. 온열등 (HEAT_LAMP)
            SettingsSwitchRow(
                title = "온열등",
                icon = Icons.Outlined.Notifications,
                checked = on,
                onCheckedChange = { isChecked ->
                    on = isChecked
                    updateFirebaseState("HEAT_LAMP", isChecked) { on = !isChecked }
                },
                textColor = textColor
            )

            // 3. LED
            SettingsSwitchRow(
                title = "LED",
                icon = Icons.Outlined.Notifications,
                checked = LED,
                onCheckedChange = { isChecked ->
                    LED = isChecked
                    updateFirebaseState("LED", isChecked) { LED = !isChecked }
                },
                textColor = textColor
            )
            SettingsSwitchRow(
                title = "워터 펌프",
                icon = Icons.Outlined.Notifications,
                checked = Water,
                onCheckedChange = { isChecked ->
                    Water = isChecked
                    updateFirebaseState("Water_pump", isChecked) { on = !isChecked }
                },
                textColor = textColor
            )
            SettingsSwitchRow(
                title = "팬 모터",
                icon = Icons.Outlined.Notifications,
                checked = pan,
                onCheckedChange = { isChecked ->
                    pan = isChecked
                    updateFirebaseState("fan_moter", isChecked) { on = !isChecked }
                },
                textColor = textColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // === Section 3: General ===
            SectionHeader(text = "General", textColor = textColor)
            NavigationItem(
                icon = Icons.Outlined.Help,
                title = "도움말",
                isDark = isDark,
                textColor = textColor
            )
            NavigationItem(
                icon = Icons.Outlined.Info,
                title = "정보",
                isDark = isDark,
                textColor = textColor
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- 재사용 컴포넌트 ---

// [NEW] 스위치가 있는 행 (알림, 온열등, LED 공통 사용)
@Composable
fun SettingsSwitchRow(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = textColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 16.sp, color = textColor)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryColor
            )
        )
    }
}

@Composable
fun SectionHeader(text: String, textColor: Color) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun RefreshRateOption(
    title: String,
    isSelected: Boolean,
    textColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    val currentBorderColor = if (isSelected) PrimaryColor else borderColor
    val backgroundColor = if (isSelected) PrimaryColor.copy(alpha = 0.1f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, currentBorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        RadioButton(
            selected = isSelected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
        )
    }
}

@Composable
fun NavigationItem(
    icon: ImageVector,
    title: String,
    valueText: String? = null,
    isDark: Boolean,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { /* 상세 화면 이동 */ },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBox(icon = icon, isDark = isDark)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 16.sp, color = textColor)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (valueText != null) {
                Text(
                    text = valueText,
                    color = if (isDark) Color.LightGray else Color.Gray,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun IconBox(icon: ImageVector, isDark: Boolean) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDark) Color.White else Color(0xFF1E293B)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    SettingsScreen()
}