package com.example.settingsapp // 패키지명은 본인의 프로젝트에 맞게 수정해주세요.

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


// --- 1. 색상 정의 (요청하신 디자인 컬러) ---
val PrimaryColor = Color(0xFF13B6EC)
val BgLight = Color(0xFFF6F8F8)
val BgDark = Color(0xFF101D22)
val TextDark = Color(0xFF1E293B) // slate-800
val TextLight = Color.White
val BorderLight = Color(0xFFE2E8F0) // slate-200
val BorderDark = Color(0xFF334155) // slate-700

// --- 2. 액티비티 클래스 ---
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // onBackClick: 뒤로가기 버튼 누르면 액티비티 종료(finish)
            SettingsScreen(onBackClick = { finish() })
        }
    }
}

// --- 3. 설정 화면 UI (Composable) ---
@Composable
fun SettingsScreen(onBackClick: () -> Unit = {}) {
    val db = Firebase.firestore
    val configRef = db.collection("settings").document("config")
    // 다크모드 감지
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) BgDark else BgLight
    val textColor = if (isDark) TextLight else TextDark
    val borderColor = if (isDark) BorderDark else BorderLight

    // 상태 변수 (데이터 수신 주기, 알림, 촬영 간격)
    var selectedRefreshRate by remember { mutableStateOf("Standard (1s)") }
    var areNotificationsEnabled by remember { mutableStateOf(true) }
    var on by remember { mutableStateOf(true) }
    var LED by remember { mutableStateOf(true) }
    var captureInterval by remember { mutableFloatStateOf(30f) }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
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
            // === Section 1: Data & Refresh ===
            SectionHeader(text = "배터리 절전 모드", textColor = textColor)
            Text(
                text = "사진 촬영 시간 조정" ,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.LightGray else Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            // 라디오 버튼 옵션들
            RefreshRateOption(
                title = "Standard (1s)",
                isSelected = selectedRefreshRate == "Standard (1s)",
                textColor = textColor,
                borderColor = borderColor,
                onClick = { selectedRefreshRate = "Standard (1s)" }
            )
            Spacer(modifier = Modifier.height(8.dp))
            RefreshRateOption(
                title = "Power Saving (1m)",
                isSelected = selectedRefreshRate == "Power Saving (1m)",
                textColor = textColor,
                borderColor = borderColor,
                onClick = { selectedRefreshRate = "Power Saving (1m)" }
            )
            Spacer(modifier = Modifier.height(8.dp))
            RefreshRateOption(
                title = "Ultra Power Saving (10m)",
                isSelected = selectedRefreshRate == "Ultra Power Saving (10m)",
                textColor = textColor,
                borderColor = borderColor,
                onClick = { selectedRefreshRate = "Ultra Power Saving (10m)" }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 알림 스위치
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBox(icon = Icons.Outlined.Notifications, isDark = isDark)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "알림 설정", fontSize = 16.sp, color = textColor)
                }
                Switch(
                    checked = areNotificationsEnabled,
                    onCheckedChange = { areNotificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryColor
                    )
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBox(icon = Icons.Outlined.Notifications, isDark = isDark)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "온열등", fontSize = 16.sp, color = textColor)
                }

                Switch(
                    checked = on,
                    onCheckedChange = { isChecked ->
                        on = isChecked
                        configRef.update("ON", isChecked)
                            .addOnSuccessListener {
                               Log.d("DB", "온열등 설정 변경 성공: $isChecked")
                            }
                            .addOnFailureListener { e ->
                                Log.w("DB", "온열등 설정 변경 실패", e)
                            }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryColor
                    )
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBox(icon = Icons.Outlined.Notifications, isDark = isDark)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "LED", fontSize = 16.sp, color = textColor)
                }
                Switch(
                    checked = LED, // 현재 LED 상태 변수
                    onCheckedChange = { isChecked ->
                        // 1. 앱 화면의 스위치 모양을 즉시 바꿈
                        LED = isChecked

                        // 2. 파이어베이스 DB에 값 전송 ("LED" 필드를 수정)
                        configRef.update("LED", isChecked)
                            .addOnSuccessListener {
                                Log.d("DB", "LED 설정 변경 성공: $isChecked")
                            }
                            .addOnFailureListener { e ->
                                Log.w("DB", "LED 설정 변경 실패", e)
                            }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryColor
                    )
                )
            }

            // === Section 2: Camera ===



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

// --- 4. 재사용 컴포넌트들 (Helper Functions) ---

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
            onClick = null, // Row 전체 클릭으로 처리
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