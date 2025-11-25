package com.example.livemonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

// --- 컬러 팔레트 정의 (CSS 기반) ---
val BgColor = Color(0xFFF7F8FC)
val CardBgColor = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1C1C1E)
val TextSecondary = Color(0xFF8A8A8E)
val BorderColor = Color(0xFFE5E5EA)

// 센서별 색상
val TempColor = Color(0xFFFFA500) // Orange
val IllumColor = Color(0xFFFFD60A) // Yellow
val WaterColor = Color(0xFF34C759) // Green
val PhColor = Color(0xFFFF3B30)    // Red

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveMonitorApp()
        }
    }
}

// --- 데이터 모델 ---
data class SensorData(
    val title: String,
    val value: String,
    val unit: String,
    val statusText: String,
    val color: Color,
    val icon: ImageVector,
    val isAlert: Boolean = false
)

@Composable
fun LiveMonitorApp() {
    MaterialTheme {
        Scaffold(
            bottomBar = { MonitorBottomBar() },
            containerColor = BgColor
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 1. 상단 카메라 영역 (헤더 포함)
                CameraHeaderSection()

                // 2. 시간 표시
                Text(
                    text = "10:30:55",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // 3. 센서 그리드
                SensorGridSection()
            }
        }
    }
}

@Composable
fun CameraHeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Color.DarkGray) // 실제 이미지가 있다면 Image 컴포넌트로 대체
    ) {
        // 배경 이미지 홀더 (Gradient로 대체)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF))
                    )
                )
        )

        // 상단 헤더 (메뉴, 타이틀, 설정)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(), // 상태바 겹침 방지
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }

            Text(
                text = "Live Monitor",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }

        // LIVE 배지 (좌상단)
        Row(
            modifier = Modifier
                .padding(top = 80.dp, start = 16.dp)
                .background(Color(0xFFDC2626).copy(alpha = 0.8f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.White, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        // 전체화면 버튼 (우상단)
        IconButton(
            onClick = {},
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
                .size(32.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun SensorGridSection() {
    // 샘플 데이터 생성
    val sensors = listOf(
        SensorData("Temperature", "22.5", "°C", "Normal", TempColor, Icons.Default.Thermostat),
        SensorData("Illuminance", "850", " lux", "Warning", IllumColor, Icons.Default.WbSunny, isAlert = true),
        SensorData("Water Level", "78", " cm", "Normal", WaterColor, Icons.Default.WaterDrop),
        SensorData("pH", "6.8", " pH", "Alert", PhColor, Icons.Default.Science, isAlert = true)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(sensors) { sensor ->
            SensorCard(sensor)
        }
    }
}

@Composable
fun SensorCard(data: SensorData) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // 아이콘 + 타이틀
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = data.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 값
            Text(
                text = "${data.value}${data.unit}",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // 상태 표시 (점 + 텍스트)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(data.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = data.statusText,
                    color = data.color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 미니 차트 (Canvas로 웨이브 그리기)
            WaveChart(color = data.color)
        }
    }
}

@Composable
fun WaveChart(color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val width = size.width
        val height = size.height

        // 간단한 베지어 곡선 시뮬레이션
        val path = Path().apply {
            moveTo(0f, height * 0.7f)
            cubicTo(
                width * 0.25f, height * 0.9f,
                width * 0.5f, height * 0.2f,
                width * 0.75f, height * 0.6f
            )
            cubicTo(
                width * 0.8f, height * 0.7f,
                width * 0.9f, height * 0.3f,
                width, height * 0.5f
            )
            // 아래 영역 채우기를 위해 닫기
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        // 그라데이션 채우기
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.0f)),
                startY = 0f,
                endY = height
            )
        )

        // 상단 선 그리기 (채우기 path와 별도로 라인만 다시 그림)
        val strokePath = Path().apply {
            moveTo(0f, height * 0.7f)
            cubicTo(
                width * 0.25f, height * 0.9f,
                width * 0.5f, height * 0.2f,
                width * 0.75f, height * 0.6f
            )
            cubicTo(
                width * 0.8f, height * 0.7f,
                width * 0.9f, height * 0.3f,
                width, height * 0.5f
            )
        }

        drawPath(
            path = strokePath,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun MonitorBottomBar() {
    NavigationBar(
        containerColor = BgColor.copy(alpha = 0.9f),
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Dashboard, contentDescription = null) },
            label = { Text("Dashboard") },
            selected = true,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF007AFF),
                selectedTextColor = Color(0xFF007AFF),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.History, contentDescription = null) },
            label = { Text("History") },
            selected = false,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
            label = { Text("Alerts") },
            selected = false,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LiveMonitorApp()
}