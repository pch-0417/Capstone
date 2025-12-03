package com.example.capstone_app // ⭐ 본인 패키지명 확인!

// [1] 안드로이드 시스템 & 차트용 색상 별칭
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.CalendarView
import android.graphics.Color as AndroidColor // ★ 차트용 색상 별칭

// [2] Activity & Core
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

// [3] Jetpack Compose
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

// [4] Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// [5] MPAndroidChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

// [6] 기타
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// [7] 설정 액티비티 (없으면 주석 처리)
import com.example.settingsapp.SettingsActivity

// ==========================================
// [A] 색상 정의 (Compose용)
// ==========================================
val BrandPrimary = Color(0xFF13B6EC)
val BgColor = Color(0xFFF7F8FC)
val CardBgColor = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1C1C1E)
val TextSecondary = Color(0xFF8A8A8E)
val ColorBorder = Color(0xFFE5E5EA)

// 센서별 고유 색상
val TempColor = Color(0xFFFFA500) // Orange
val WaterColor = Color(0xFF34C759) // Green
val PhColor = Color(0xFFFF3B30)    // Red

// ==========================================
// [B] 데이터 클래스 (이름 충돌 방지 위해 변경됨)
// ==========================================

// 1. DB 및 그래프용 데이터
data class HistorySensorData(
    val timestamp: Long = 0,
    val temperature: Float = 0f,
    val humidity: Float = 0f,
    val waterLevel: Float = 0f,
    val ph: Float = 0f
)

// 2. 대시보드 UI 카드용 데이터
data class DashboardSensorData(
    val title: String,
    val value: String,
    val unit: String,
    val statusText: String,
    val color: Color,
    val icon: ImageVector,
    val isAlert: Boolean = false,
    val graphData: List<Float> = emptyList()
)

// 3. 알림용 데이터
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

// ==========================================
// [C] 메인 액티비티
// ==========================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveMonitorApp()
        }
    }
}

// --- 1. 메인 네비게이션 관리 ---
@Composable
fun LiveMonitorApp() {
    var currentScreen by remember { mutableStateOf("Dashboard") }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                // Dashboard
                NavigationBarItem(
                    selected = currentScreen == "Dashboard",
                    onClick = { currentScreen = "Dashboard" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(selectedTextColor = BrandPrimary, selectedIconColor = BrandPrimary)
                )
                // History
                NavigationBarItem(
                    selected = currentScreen == "History",
                    onClick = { currentScreen = "History" },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(selectedTextColor = BrandPrimary, selectedIconColor = BrandPrimary)
                )
                // Alerts
                NavigationBarItem(
                    selected = currentScreen == "Alerts",
                    onClick = { currentScreen = "Alerts" },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    label = { Text("Alerts") },
                    colors = NavigationBarItemDefaults.colors(selectedTextColor = BrandPrimary, selectedIconColor = BrandPrimary)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentScreen) {
                "Dashboard" -> DashboardScreen()
                "History" -> HistoryScreen() // 그래프 화면 연결
                "Alerts" -> AlertsScreen()
            }
        }
    }
}

// --- 2. 대시보드 화면 ---
@Composable
fun DashboardScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTime by remember { mutableStateOf("Loading...") }

    // 시계 동작
    LaunchedEffect(Unit) {
        while (true) {
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.KOREA)
            formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            currentTime = formatter.format(Date())
            delay(1000L)
        }
    }

    // 센서 데이터 리스트 (DashboardSensorData 사용)
    val sensors = remember {
        mutableStateListOf(
            DashboardSensorData("Temperature", "-", "°C", "Loading", TempColor, Icons.Default.Thermostat),
            DashboardSensorData("Water Level", "-", " %", "Loading", WaterColor, Icons.Default.WaterDrop),
            DashboardSensorData("pH", "-", " pH", "Loading", PhColor, Icons.Default.Science, isAlert = true),
            DashboardSensorData("Humidity", "-", " %", "Loading", BrandPrimary, Icons.Default.WaterDrop)
        )
    }

    // 센서값 UI 업데이트 로직
    fun updateSensor(index: Int, newVal: Float, status: String = "Live") {
        if (sensors.size > index) {
            val oldList = sensors[index].graphData
            val newList = if (oldList.isEmpty()) {
                List(20) { newVal }
            } else {
                (oldList + newVal).takeLast(20)
            }

            sensors[index] = sensors[index].copy(
                value = if (newVal == 0f) "-" else String.format("%.1f", newVal),
                statusText = status,
                graphData = newList
            )
        }
    }

    // Firebase Monitor 연결
    val database = Firebase.database
    val monitorRef = database.getReference("monitor")

    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val tempVal = snapshot.child("temperature").value?.toString()?.toFloatOrNull() ?: 0f
                    val waterVal = snapshot.child("waterLevel").value?.toString()?.toFloatOrNull() ?: 0f
                    val phVal = snapshot.child("ph").value?.toString()?.toFloatOrNull() ?: 0f
                    val humVal = snapshot.child("humidity").value?.toString()?.toFloatOrNull() ?: 0f

                    // 순서대로 업데이트 (0:온도, 1:수위, 2:pH, 3:습도)
                    updateSensor(0, tempVal)
                    updateSensor(1, waterVal)
                    updateSensor(2, phVal)
                    updateSensor(3, humVal)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        monitorRef.addValueEventListener(listener)
        onDispose { monitorRef.removeEventListener(listener) }
    }

    MaterialTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = { MenuDrawerContent() }
        ){
            Scaffold(containerColor = BgColor) { paddingValues ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        CameraHeaderSection(onMenuClick = { scope.launch { drawerState.open() } })
                    }
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = currentTime,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                    items(sensors) { sensor ->
                        SensorCard(sensor)
                    }
                }
            }
        }
    }
}

// --- 3. 히스토리 화면 (그래프 + 달력) ---
// --- 3. 히스토리 화면 (그래프 크게 + 글자 크게 수정됨) ---
@Composable
fun HistoryScreen() {
    // 1. 상태 관리
    var historyDataList by remember { mutableStateOf(listOf<HistorySensorData>()) }
    var selectedDateText by remember { mutableStateOf("날짜를 선택하세요") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(16.dp) // 전체 여백
            .verticalScroll(rememberScrollState()) // ★ 화면이 길어질 수 있으니 스크롤 추가
    ) {
        Text(
            text = " History Graph",
            fontSize = 24.sp, // 제목도 조금 더 크게
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "선택된 날짜: $selectedDateText",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // [영역 1] 그래프 (MPAndroidChart) - 크기 및 스타일 수정
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp) // ★ 1. 높이를 350dp -> 500dp로 변경 (더 길게)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(8.dp), // 그래프 내부 여백 살짝 줌
            factory = { context ->
                LineChart(context).apply {
                    description.isEnabled = false
                    setTouchEnabled(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setPinchZoom(true)

                    // 배경에 격자 그리기 (보기 편하게)
                    setDrawGridBackground(false)

                    // ★ 2. 범례 (Legend: 온도, 습도 텍스트) 설정 - 아주 크게!
                    legend.apply {
                        isEnabled = true
                        textSize = 16f        // 글자 크기 (기본 10f -> 16f)
                        formSize = 16f        // 색상 네모 크기
                        form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE // 동그라미 모양
                        textColor = AndroidColor.BLACK
                        xEntrySpace = 20f     // 항목 간 가로 간격 넓힘
                        yEntrySpace = 10f     // 줄 간격
                        isWordWrapEnabled = true // 화면 좁으면 줄바꿈
                        verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                        horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                        orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                        setDrawInside(false)
                    }

                    // ★ 3. X축 (시간) 설정 - 크게
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        textSize = 14f        // 글자 크기 확대
                        textColor = AndroidColor.DKGRAY
                        yOffset = 10f         // 그래프와 글자 사이 간격 벌림
                    }

                    // ★ 4. Y축 (숫자) 설정 - 크게
                    axisLeft.apply {
                        axisMinimum = 0f
                        axisMaximum = 100f
                        textSize = 14f        // 글자 크기 확대
                        textColor = AndroidColor.DKGRAY
                        xOffset = 10f         // 그래프와 글자 사이 간격 벌림
                    }
                    axisRight.isEnabled = false

                    // 여백 자동 계산 (글자가 잘리지 않게)
                    setExtraOffsets(10f, 10f, 10f, 20f)
                }
            },
            update = { chart ->
                if (historyDataList.isEmpty()) {
                    chart.clear()
                    return@AndroidView
                }

                // 데이터셋 생성
                val entriesTemp = historyDataList.mapIndexed { i, d -> Entry(i.toFloat(), d.temperature) }
                val entriesHumi = historyDataList.mapIndexed { i, d -> Entry(i.toFloat(), d.humidity) }
                val entriesWater = historyDataList.mapIndexed { i, d -> Entry(i.toFloat(), d.waterLevel) }
                val entriesPh = historyDataList.mapIndexed { i, d -> Entry(i.toFloat(), d.ph) }

                // ★ 5. 선 스타일 설정 - 선을 더 두껍게 (lineWidth 3f)
                val setTemp = LineDataSet(entriesTemp, "온도(℃)").apply {
                    color = AndroidColor.RED
                    setCircleColor(AndroidColor.RED)
                    lineWidth = 3f  // 선 두께 3배
                    circleRadius = 4f // 점 크기 확대
                    setDrawCircleHole(false)
                    setDrawValues(false) // 그래프 위의 작은 숫자는 지저분하니까 끔
                }
                val setHumi = LineDataSet(entriesHumi, "습도(%)").apply {
                    color = AndroidColor.BLUE
                    setCircleColor(AndroidColor.BLUE)
                    lineWidth = 3f
                    circleRadius = 4f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                }
                val setWater = LineDataSet(entriesWater, "수위(cm)").apply {
                    color = AndroidColor.CYAN
                    setCircleColor(AndroidColor.CYAN)
                    lineWidth = 3f
                    circleRadius = 4f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                }
                val setPh = LineDataSet(entriesPh, "pH").apply {
                    color = AndroidColor.GREEN
                    setCircleColor(AndroidColor.GREEN)
                    lineWidth = 3f
                    circleRadius = 4f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                }

                val lineData = LineData(setTemp, setHumi, setWater, setPh)
                chart.data = lineData

                // 데이터 갱신 시 애니메이션 효과 (X축 방향으로 스르륵)
                chart.animateX(1000)
                chart.invalidate()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // [영역 2] 달력
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AndroidView(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                factory = { context ->
                    CalendarView(context).apply {
                        setOnDateChangeListener { _, year, month, dayOfMonth ->
                            val realMonth = month + 1
                            selectedDateText = "${year}년 ${realMonth}월 ${dayOfMonth}일"

                            // Firebase 데이터 가져오기
                            fetchHistoryData(year, month, dayOfMonth) { data ->
                                historyDataList = data
                            }
                        }
                    }
                }
            )
        }

        // 바닥 여백 추가 (스크롤 편하게)
        Spacer(modifier = Modifier.height(50.dp))
    }
}
// 히스토리 데이터 로직
fun fetchHistoryData(year: Int, month: Int, day: Int, onResult: (List<HistorySensorData>) -> Unit) {
    val database = Firebase.database
    val myRef = database.getReference("history")

    val calendar = Calendar.getInstance()
    calendar.set(year, month, day, 0, 0, 0)
    val startTs = calendar.timeInMillis
    calendar.set(year, month, day, 23, 59, 59)
    val endTs = calendar.timeInMillis

    myRef.orderByChild("timestamp")
        .startAt(startTs.toDouble())
        .endAt(endTs.toDouble())
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<HistorySensorData>()
                for (child in snapshot.children) {
                    val data = child.getValue(HistorySensorData::class.java)
                    if (data != null) list.add(data)
                }
                onResult(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
}

// --- 4. 알림 화면 ---
@Composable
fun AlertsScreen() {
    var selectedTab by remember { mutableStateOf("오늘") }
    val alerts = listOf(
        AlertItem("온도 임계값 초과", "온도가 30°C를 초과했습니다.", "거실 센서", "5분 전", Icons.Default.DeviceThermostat, Color(0xFFFFE0B2), Color(0xFFF57C00), true),
        AlertItem("배터리 부족 경고", "잔량 10% 미만", "현관 카메라", "15분 전", Icons.Default.BatteryAlert, Color(0xFFFFCDD2), Color(0xFFD32F2F), true)
    )

    Scaffold(
        topBar = { AlertsTopBar() },
        containerColor = Color.White
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                TabItem("오늘", selectedTab == "오늘", { selectedTab = "오늘" }, Modifier.weight(1f))
                TabItem("이번 주", selectedTab == "이번 주", { selectedTab = "이번 주" }, Modifier.weight(1f))
            }
            LazyColumn {
                items(alerts) { alert ->
                    AlertListItem(alert)
                    HorizontalDivider(color = ColorBorder, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun AlertsTopBar() {
    Box(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.8f)).padding(16.dp)) {
        Icon(Icons.Default.ArrowBackIosNew, "Back", modifier = Modifier.align(Alignment.CenterStart).size(24.dp))
        Text("알림", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        Text("모두 읽음", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, modifier = Modifier.align(Alignment.CenterEnd).clickable { })
    }
    HorizontalDivider(color = ColorBorder)
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clickable(onClick = onClick).padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) BrandPrimary else Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(if (isSelected) BrandPrimary else Color.Transparent))
    }
}

@Composable
fun AlertListItem(item: AlertItem) {
    Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).background(item.iconBgColor, CircleShape), contentAlignment = Alignment.Center) {
            Icon(item.icon, null, tint = item.iconColor, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(item.description, fontSize = 14.sp, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(item.time, fontSize = 14.sp, color = Color.Gray)
            if (item.isUnread) Box(modifier = Modifier.size(10.dp).background(BrandPrimary, CircleShape))
        }
    }
}

// --- 5. 공통 컴포넌트 ---
@Composable
fun MenuDrawerContent() {
    ModalDrawerSheet(drawerContainerColor = CardBgColor, modifier = Modifier.width(300.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 24.dp)) {
                Surface(shape = CircleShape, color = Color.LightGray, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Jane Doe", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            NavigationDrawerItem(label = { Text("Edit Profile") }, icon = { Icon(Icons.Default.Edit, null) }, selected = true, onClick = {}, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = BrandPrimary.copy(alpha = 0.1f), selectedTextColor = BrandPrimary, selectedIconColor = BrandPrimary))
            Spacer(modifier = Modifier.weight(1f))
            Text("App Version: 1.0.0", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun CameraHeaderSection(ipAddress: String = "10.161.23.183", onMenuClick: () -> Unit) {
    val context = LocalContext.current
    val streamUrl = "http://$ipAddress:81/stream"

    Box(modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(24.dp)).background(Color.DarkGray)) {
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                settings.apply { javaScriptEnabled = true; loadWithOverviewMode = true; useWideViewPort = true }
                webViewClient = WebViewClient()
                loadUrl(streamUrl)
            }
        })
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent), startY = 0f, endY = 400f)))
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onMenuClick, modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)) { Icon(Icons.Default.Menu, "Menu", tint = Color.White) }
            Text("Live Monitor", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            IconButton(onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) }, modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)) { Icon(Icons.Default.Settings, "Settings", tint = Color.White) }
        }
    }
}

@Composable
fun SensorCard(data: DashboardSensorData) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBgColor), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, ColorBorder), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(data.icon, null, tint = data.color, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(data.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("${data.value}${data.unit}", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(data.color, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text(data.statusText, color = data.color, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            WaveChart(data.graphData, data.color)
        }
    }
}

@Composable
fun WaveChart(data: List<Float>, color: Color) {
    Canvas(modifier = Modifier.fillMaxWidth().height(50.dp)) {
        val points = if (data.isEmpty()) listOf(0f, 0f) else if (data.size == 1) listOf(data[0], data[0]) else data
        val width = size.width
        val height = size.height
        var maxVal = points.maxOrNull() ?: 0f
        var minVal = points.minOrNull() ?: 0f
        if (maxVal == minVal) { maxVal += 1f; minVal -= 1f }
        val range = maxVal - minVal

        fun getX(index: Int) = (index.toFloat() / (points.size - 1)) * width
        fun getY(value: Float) = height - ((value - minVal) / range) * height

        val strokePath = Path().apply {
            moveTo(getX(0), getY(points[0]))
            for (i in 1 until points.size) lineTo(getX(i), getY(points[i]))
        }
        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.0f)), startY = 0f, endY = height))
        drawPath(path = strokePath, color = color, style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMain() {
    LiveMonitorApp()
}