package com.example.settingsapp // 패키지명은 본인의 프로젝트에 맞게 수정해주세요.

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



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
    // [변경 1] Firestore 대신 Realtime Database 연결
    val database = Firebase.database
    // 아까 JSON 구조에서 만든 "control" 폴더를 바라봅니다.
    val controlRef = database.getReference("control")

    // 다크모드 감지 (기존 코드 유지)
    val isDark = isSystemInDarkTheme()
    // * 색상 변수들이 외부에 정의되어 있다고 가정하고 그대로 둠
    // (컴파일 에러 나면 기존에 쓰던 색상 코드로 채워넣으세요)
    // val bgColor = if (isDark) BgDark else BgLight
    // val textColor = if (isDark) TextLight else TextDark
    // val borderColor = if (isDark) BorderDark else BorderLight

    // 테스트용 임시 색상 (기존 코드에 상수 정의가 안보여서 임시로 넣음, 원래대로 쓰시면 됩니다)
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val borderColor = Color.Gray
    val PrimaryColor = Color(0xFF6200EE)


    // 상태 변수
    var selectedRefreshRate by remember { mutableStateOf("Standard (1s)") }
    var areNotificationsEnabled by remember { mutableStateOf(true) }

    // 스위치 변수 (기존과 동일)
    var on by remember { mutableStateOf(false) } // 온열등 (HEAT_LAMP)
    var LED by remember { mutableStateOf(false) } // LED
    var captureInterval by remember { mutableFloatStateOf(30f) }

    // [변경 2] 실시간 데이터 감지 (앱 켜면 DB값 읽어오기)
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // DB에서 값이 바뀌면 여기로 들어옵니다.
                // control 밑에 있는 HEAT_LAMP와 LED 값을 가져옵니다.
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
            // SectionHeader(text = "배터리 절전 모드", textColor = textColor) // 기존 컴포저블 유지
            Text("배터리 절전 모드", color = textColor, fontWeight = FontWeight.Bold) // 임시 대체

            Text(
                text = "사진 촬영 시간 조정" ,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.LightGray else Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

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
                    // IconBox(icon = Icons.Outlined.Notifications, isDark = isDark) // 기존 함수 유지
                    Icon(Icons.Outlined.Notifications, contentDescription = null, tint = textColor)
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

            // [변경 3] 온열등 스위치 로직 수정
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // IconBox(icon = Icons.Outlined.Notifications, isDark = isDark)
                    Icon(Icons.Outlined.Notifications, contentDescription = null, tint = textColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "온열등", fontSize = 16.sp, color = textColor)
                }

                Switch(
                    checked = on,
                    onCheckedChange = { isChecked ->
                        on = isChecked
                        // Realtime DB에 값 쓰기 (control/HEAT_LAMP)
                        controlRef.child("HEAT_LAMP").setValue(isChecked)
                            .addOnSuccessListener {
                                Log.d("IoT", "온열등 설정 변경 성공: $isChecked")
                            }
                            .addOnFailureListener { e ->
                                Log.w("IoT", "온열등 설정 변경 실패", e)
                                on = !isChecked // 실패하면 스위치 되돌리기
                            }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryColor
                    )
                )
            }

            // [변경 4] LED 스위치 로직 수정
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // IconBox(icon = Icons.Outlined.Notifications, isDark = isDark)
                    Icon(Icons.Outlined.Notifications, contentDescription = null, tint = textColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "LED", fontSize = 16.sp, color = textColor)
                }
                Switch(
                    checked = LED,
                    onCheckedChange = { isChecked ->
                        LED = isChecked
                        // Realtime DB에 값 쓰기 (control/LED)
                        controlRef.child("LED").setValue(isChecked)
                            .addOnSuccessListener {
                                Log.d("IoT", "LED 설정 변경 성공: $isChecked")
                            }
                            .addOnFailureListener { e ->
                                Log.w("IoT", "LED 설정 변경 실패", e)
                                LED = !isChecked
                            }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === Section 3: General === (기존 유지)
            // SectionHeader(text = "General", textColor = textColor)
            Text("General", color = textColor, fontWeight = FontWeight.Bold)

            // NavigationItem(...)


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