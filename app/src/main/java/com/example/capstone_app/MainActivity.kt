package com.example.capstone_app // ⭐ 본인 패키지명으로 꼭 수정하세요!

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settingsapp.SettingsActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.tooling.preview.Preview
val ColorPrimary = Color(0xFF13b6ec)      // Cyan (Primary)
val ColorBackground = Color(0xFFf6f8f8)   // Light Background
val ColorTextMain = Color(0xFF18181b)     // Zinc 900
val ColorTextSub = Color(0xFF71717a)      // Zinc 500
val ColorBorder = Color(0xFFe4e4e7)       // Zinc 200
val ChartCyan = Color(0xFF25c6da)
val ChartOrange = Color(0xFFf97316)
val ChartBlue = Color(0xFF3b82f6)
val ChartPurple = Color(0xFFa855f7)
val BgColor = Color(0xFFF7F8FC)
val CardBgColor = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1C1C1E)
val TextSecondary = Color(0xFF8A8A8E)
val BorderColor = Color(0xFFE5E5EA)
val BrandPrimary = Color(0xFF13B6EC)
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
data class AlertItem(
    val title: String,
    val description: String,
    val source: String,
    val time: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconColor: Color,
    val isUnread: Boolean = false
)
data class SensorData(
    val title: String,
    val value: String,
    val unit: String,
    val statusText: String,
    val color: Color,
    val icon: ImageVector,
    val isAlert: Boolean = false,
    val graphData: List<Float> = emptyList()
)
// --- 1. 메인 화면 (화면 관리자) ---
@Composable
fun LiveMonitorApp() {
    // 현재 선택된 화면을 기억하는 변수 ("Dashboard"가 기본값)
    var currentScreen by remember { mutableStateOf("Dashboard") }

    Scaffold(
        // 하단 네비게이션 바 설정
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                // 1️⃣ Dashboard 버튼
                NavigationBarItem(
                    selected = currentScreen == "Dashboard",
                    onClick = { currentScreen = "Dashboard" }, // 클릭 시 화면 변경
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF), // 선택되면 파란색
                        selectedTextColor = Color(0xFF007AFF),
                        indicatorColor = Color(0xFF007AFF).copy(alpha = 0.1f), // 배경 연한 파랑
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                // 2️⃣ History 버튼
                NavigationBarItem(
                    selected = currentScreen == "History",
                    onClick = { currentScreen = "History" },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        selectedTextColor = Color(0xFF007AFF),
                        indicatorColor = Color(0xFF007AFF).copy(alpha = 0.1f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                // 3️⃣ Alerts 버튼
                NavigationBarItem(
                    selected = currentScreen == "Alerts",
                    onClick = { currentScreen = "Alerts" },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    label = { Text("Alerts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        selectedTextColor = Color(0xFF007AFF),
                        indicatorColor = Color(0xFF007AFF).copy(alpha = 0.1f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { paddingValues ->
        // 내용이 들어갈 공간 (Box)
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentScreen) {
                "Dashboard" -> DashboardScreen() // 대시보드 보여주기
                "History" -> HistoryScreen()     // 히스토리 보여주기
                "Alerts" -> AlertsScreen()       // 알림 보여주기
            }
        }
    }
}

// --- 2. 각 화면별 디자인 (Composable 함수) ---

@Composable
fun DashboardScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTime by remember { mutableStateOf("Loading...") }
    LaunchedEffect(Unit) {
        while (true) {
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.KOREA)
            formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            currentTime = formatter.format(Date())
            delay(1000L)
        }
    }
    val sensors = remember {
        mutableStateListOf(
            SensorData("Temperature", "-", "°C", "Loading", TempColor, Icons.Default.Thermostat),
            SensorData("Water Level", "-", " cm", "Loading", WaterColor, Icons.Default.WaterDrop),
            SensorData("pH", "-", " pH", "Loading", PhColor, Icons.Default.Science, isAlert = true),
            SensorData("Humidity", "-", " %", "Loading", BrandPrimary, Icons.Default.WaterDrop)
        )
    }
    fun updateSensor(index: Int, newVal: Float, status: String = "Live") {
        if (sensors.size > index) {
            val oldList = sensors[index].graphData
            val newList = if (oldList.isEmpty()) {
                List(20) { newVal }
            } else {
                (oldList + newVal).takeLast(20)
            }

            sensors[index] = sensors[index].copy(
                value = if (newVal == 0f) "-" else newVal.toString(), // 0f면 "-" 표시
                statusText = status,
                graphData = newList
            )
        }
    }

    val db = Firebase.firestore
    val docRef = db.collection("sensor").document("temp")

    docRef.addSnapshotListener { snapshot, e ->
        if (e != null) {
            println("Firestore Error: ${e.message}")
            return@addSnapshotListener
        }

        if (snapshot != null && snapshot.exists()) {
            // 1. 값 가져오기 (Float으로 변환하며, 실패 시 0f로 처리하여 튕김 방지)
            val tempVal = snapshot.get("Temperature")?.toString()?.toFloatOrNull() ?: 0f
            val waterVal = snapshot.get("WaterLevel")?.toString()?.toFloatOrNull() ?: 0f
            val phVal = snapshot.get("pH")?.toString()?.toFloatOrNull() ?: 0f
            val humVal = snapshot.get("Humidity")?.toString()?.toFloatOrNull() ?: 0f

            // 2. [핵심] updateSensor 함수 호출 (그래프 데이터 누적)
            // 현재 sensors 리스트 순서: 0:온도, 1:수위, 2:pH, 3:습도
            updateSensor(0, tempVal)   // Temperature (Index 0)
            updateSensor(1, waterVal)  // Water Level (Index 1)
            updateSensor(2, phVal)     // pH (Index 2)
            updateSensor(3, humVal)    // Humidity (Index 3)
        }
    }

    MaterialTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                MenuDrawerContent()
            }
        ){
            Scaffold(
                // bottomBar = { MonitorBottomBar() }, // <--- 바텀바 제거됨
                containerColor = BgColor
            ) { paddingValues ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        CameraHeaderSection(
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = currentTime,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding( bottom = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,

                            )
                    }
                    items(sensors) {sensor ->
                        Box(modifier = Modifier.padding(horizontal = 8.dp)){
                            SensorCard(sensor)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun MenuDrawerContent() {
    ModalDrawerSheet(
        drawerContainerColor = CardBgColor,
        modifier = Modifier.width(300.dp) // 너비 제한
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // 1. 프로필 영역
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                // 프로필 이미지 대용 아이콘
                Surface(
                    shape = CircleShape,
                    color = Color.LightGray,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Jane Doe",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 메뉴 아이템 리스트

            // Edit Profile (Selected Style)
            NavigationDrawerItem(
                label = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                selected = true,
                onClick = { /* 프로필 수정 이동 */ },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = BrandPrimary.copy(alpha = 0.1f),
                    selectedIconColor = BrandPrimary,
                    selectedTextColor = BrandPrimary
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Logout (Default Style)

            Spacer(modifier = Modifier.weight(1f)) // 남은 공간 차지

            // 3. 하단 버전 정보
            Text(
                text = "App Version: 1.2.3",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
@Composable
fun CameraHeaderSection(onMenuClick : () -> Unit) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp, topStart = 24.dp, topEnd = 24.dp))
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
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                .statusBarsPadding(), // 상태바 겹침 방지
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onMenuClick,
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
                onClick = {
                    val intent = Intent(context, SettingsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
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
            WaveChart(
                data = data.graphData,
                color = data.color)
        }
    }
}
@Composable
fun WaveChart(
    data: List<Float>, // ⭐ 데이터를 받도록 수정
    color: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        val points = if (data.isEmpty()) listOf(0f, 0f) else if (data.size == 1) listOf(data[0], data[0]) else data

        val width = size.width
        val height = size.height

        // 데이터 정규화 (그래프 높이에 맞추기)
        var maxVal = data.maxOrNull() ?: 0f
        var minVal = data.minOrNull() ?: 0f

        if (maxVal == minVal) {
            maxVal += 1f
            minVal -= 1f
        }
        val range = maxVal - minVal

        // 좌표 계산 함수
        fun getX(index: Int) = (index.toFloat() / (data.size - 1)) * width
        fun getY(value: Float) = height - ((value - minVal) / range) * height

        // 1. 선 그리기 경로 (Stroke)
        val strokePath = Path().apply {
            moveTo(getX(0), getY(points[0])) // 시작점
            for (i in 1 until points.size) {
                // 부드러운 곡선 대신 반응 빠른 직선(lineTo) 사용
                lineTo(getX(i), getY(points[i]))
            }
        }
        // 2. 아래 채우기 경로
        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }


        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.0f)),
                startY = 0f,
                endY = height
            )
        )

        // 6. 선 그리기
        drawPath(
            path = strokePath,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun HistoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp) // 하단 여백
    ) {
        Header()            // 상단 제목 & 공유 버튼
        TimeFrameSelector() // Day/Week/Month/Year 선택기
        StatisticsCard()    // 통계 차트 카드
        CalendarCard()      // 달력 카드
        FooterText()        // 하단 안내 문구
        }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 왼쪽 빈 공간 (균형 맞추기용)
        Box(modifier = Modifier.size(48.dp))

        Text(
            text = "Statistics",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextMain
        )

        // 공유 버튼 (기본 아이콘 사용)
        IconButton(
            onClick = { },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = ColorTextMain
            )
        }
    }
}
@Composable
fun TimeFrameSelector() {
    val options = listOf("Day", "Week", "Month", "Year")
    var selectedOption by remember { mutableStateOf("Week") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
            .background(Color(0xFFf4f4f5), RoundedCornerShape(8.dp)) // Zinc 100
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            val isSelected = selectedOption == option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { selectedOption = option },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) ColorPrimary else ColorTextSub
                )
            }
        }
    }
}
@Composable
fun StatisticsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Overall Statistics",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ColorTextMain
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Weekly Summary",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextMain,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "+3.8%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF22c55e) // Green 500
                )
            }

            // 차트 영역 (Canvas로 직접 그리기)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                CustomLineChart()
            }

            // 요일 라벨
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextSub
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = ColorBorder)

            // 범례 (Legend)
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    LegendItem(color = ChartCyan, text = "CO2 Levels", modifier = Modifier.weight(1f))
                    LegendItem(color = ChartOrange, text = "Temperature", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    LegendItem(color = ChartBlue, text = "Humidity", modifier = Modifier.weight(1f))
                    LegendItem(color = ChartPurple, text = "PM2.5", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
@Composable
fun LegendItem(color: Color, text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF3f3f46) // Zinc 700
        )
    }
}

@Composable
fun CustomLineChart() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 차트 1 (점선, Cyan)
        val path1 = Path().apply {
            moveTo(0f, height * 0.7f)
            cubicTo(width * 0.1f, height * 0.1f, width * 0.4f, height * 0.1f, width * 0.5f, height * 0.2f)
            cubicTo(width * 0.6f, height * 0.4f, width * 0.8f, height * 0.9f, width, height * 0.9f)
        }
        drawPath(
            path = path1,
            color = ChartCyan,
            style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f), cap = StrokeCap.Round)
        )

        // 차트 2 (실선, Orange)
        val path2 = Path().apply {
            moveTo(0f, height * 0.5f)
            cubicTo(width * 0.2f, height * 0.4f, width * 0.5f, height * 0.6f, width * 0.7f, height * 0.7f)
            cubicTo(width * 0.8f, height * 0.8f, width * 0.9f, height * 0.9f, width, height * 0.8f)
        }
        drawPath(path = path2, color = ChartOrange, style = Stroke(width = 5f, cap = StrokeCap.Round))

        // 차트 3 (실선, Blue)
        val path3 = Path().apply {
            moveTo(0f, height * 0.3f)
            cubicTo(width * 0.2f, height * 0.8f, width * 0.4f, height * 0.9f, width * 0.6f, height * 0.6f)
            cubicTo(width * 0.8f, height * 0.3f, width * 0.9f, height * 0.2f, width, height * 0.3f)
        }
        drawPath(path = path3, color = ChartBlue, style = Stroke(width = 5f, cap = StrokeCap.Round))

        // 차트 4 (실선, Purple)
        val path4 = Path().apply {
            moveTo(0f, height * 0.9f)
            cubicTo(width * 0.2f, height * 0.4f, width * 0.5f, height * 0.5f, width * 0.7f, height * 0.2f)
            cubicTo(width * 0.8f, height * 0.1f, width * 0.9f, height * 0.3f, width, height * 0.1f)
        }
        drawPath(path = path4, color = ChartPurple, style = Stroke(width = 5f, cap = StrokeCap.Round))
    }
}
@Composable
fun CalendarCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 달력 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 기본 아이콘 사용 (ChevronLeft)
                IconButton(onClick = { }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = ColorTextMain)
                }
                Text(
                    text = "October 2023",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextMain
                )
                // 기본 아이콘 사용 (ChevronRight)
                IconButton(onClick = { }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = ColorTextMain)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 요일 (S M T W T F S)
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextSub
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 날짜 그리드
            val days = (1..31).toList()
            val gridItems = days.chunked(7)

            gridItems.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        // HTML 스타일 로직 (특정 날짜 색상 변경)
                        val bgColor = when (day) {
                            5 -> ColorPrimary // 선택된 날짜 (파란색)
                            7 -> Color(0xFF06b6d4) // Cyan 500
                            else -> when (day) {
                                1, 9 -> Color(0xFFecfeff)
                                2, 8, 13 -> Color(0xFFcffafe)
                                3, 11 -> Color(0xFFa5f3fc)
                                4, 15 -> Color(0xFF67e8f9)
                                6 -> Color(0xFF22d3ee)
                                10 -> Color(0xFFffedd5) // Orange
                                14 -> Color(0xFFe9d5ff) // Purple
                                else -> Color.Transparent
                            }
                        }

                        val textColor = if (day == 5 || day == 7) Color.White else if(day >= 16) Color(0xFFa1a1aa) else ColorTextMain

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                    }
                    // 남은 칸 채우기
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
@Composable
fun FooterText() {
    Text(
        text = "Tap a day to see details",
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        fontSize = 14.sp,
        color = ColorTextSub
    )
}
@Composable
fun AlertsTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.8f)) // backdrop blur 흉내
            .padding(16.dp)
    ) {
        // 뒤로가기 아이콘 (왼쪽)
        Icon(
            imageVector = Icons.Default.ArrowBackIosNew,
            contentDescription = "Back",
            tint = Color(0xFF1F2937), // Gray-800
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(24.dp)
        )

        // 제목 (중앙)
        Text(
            text = "알림",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827), // Gray-900
            modifier = Modifier.align(Alignment.Center)
        )

        // 모두 읽음 버튼 (오른쪽)
        Text(
            text = "모두 읽음",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPrimary,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable { /* 모두 읽음 처리 */ }
        )
    }
    HorizontalDivider(color = ColorBorder)
}
@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) BrandPrimary else Color(0xFF9CA3AF) // Gray-400
        )
        Spacer(modifier = Modifier.height(12.dp))
        // 하단 바 (선택되면 파란색, 아니면 투명)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(if (isSelected) BrandPrimary else Color.Transparent)
        )
    }
}
@Composable
fun AlertListItem(item: AlertItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘 (원형 배경)
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(item.iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.iconColor,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 텍스트 내용
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111827) // Gray-900
            )
            Text(
                text = item.description,
                fontSize = 14.sp,
                color = Color(0xFF4B5563), // Gray-600
                maxLines = 2
            )
            Text(
                text = item.source,
                fontSize = 14.sp,
                color = Color(0xFF6B7280) // Gray-500
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 시간 및 읽음 표시
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.time,
                fontSize = 14.sp,
                color = Color(0xFF6B7280) // Gray-500
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 읽지 않음 표시 (파란 점)
            if (item.isUnread) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(BrandPrimary, CircleShape)
                )
            }
        }
    }
}
@Composable
fun AlertsScreen() {
    var selectedTab by remember { mutableStateOf("오늘") }

    // 알림 데이터 (HTML 내용 반영)
    val alerts = listOf(
        AlertItem(
            title = "온도 임계값 초과",
            description = "온도가 30°C를 초과했습니다.",
            source = "거실 센서",
            time = "5분 전",
            icon = Icons.Default.DeviceThermostat,
            iconBgColor = Color(0xFFFFE0B2), // Orange-100
            iconColor = Color(0xFFF57C00),   // Orange-500
            isUnread = true
        ),
        AlertItem(
            title = "배터리 부족 경고",
            description = "배터리 잔량이 10% 미만입니다.",
            source = "현관 카메라",
            time = "15분 전",
            icon = Icons.Default.BatteryAlert,
            iconBgColor = Color(0xFFFFCDD2), // Red-100
            iconColor = Color(0xFFD32F2F),   // Red-500
            isUnread = true
        ),
        AlertItem(
            title = "새로운 데이터 리포트",
            description = "주간 데이터 리포트가 생성되었습니다.",
            source = "시스템",
            time = "오후 2:30",
            icon = Icons.Default.Analytics,
            iconBgColor = Color(0xFFBBDEFB), // Blue-100
            iconColor = Color(0xFF1976D2),   // Blue-500
            isUnread = false
        ),
        AlertItem(
            title = "시스템이 업데이트되었습니다",
            description = "앱 안정성이 향상되었습니다.",
            source = "시스템",
            time = "오전 9:00",
            icon = Icons.Default.SystemUpdate,
            iconBgColor = Color(0xFFF5F5F5), // Gray-100
            iconColor = Color(0xFF9E9E9E),   // Gray-500
            isUnread = false
        )
    )

    Scaffold(
        topBar = {
            AlertsTopBar()
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 상단 탭 (오늘 / 이번 주)
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TabItem(
                    text = "오늘",
                    isSelected = selectedTab == "오늘",
                    onClick = { selectedTab = "오늘" },
                    modifier = Modifier.weight(1f)
                )
                TabItem(
                    text = "이번 주",
                    isSelected = selectedTab == "이번 주",
                    onClick = { selectedTab = "이번 주" },
                    modifier = Modifier.weight(1f)
                )
            }
            // 알림 리스트
            LazyColumn {
                items(alerts) { alert ->
                    AlertListItem(alert)
                    HorizontalDivider(color = ColorBorder, thickness = 1.dp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStatistics() {
    HistoryScreen()
}