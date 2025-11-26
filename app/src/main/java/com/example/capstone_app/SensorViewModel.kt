package com.example.capstone_app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.livemonitor.SensorCard
import com.example.livemonitor.SensorData
import com.example.livemonitor.TextSecondary
import com.google.firebase.firestore.ktx.firestore
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.ktx.Firebase

class SensorViewModel : ViewModel() {
    // 1. 파이어베이스 연결
    private val db = Firebase.firestore

    // 2. 화면이 바라볼 데이터 (처음엔 빈 리스트)
    // 메인 화면은 이 변수만 쳐다보고 있으면 됩니다.
    var sensors = mutableStateOf<List<SensorData>>(emptyList())

    init {
        // 3. 앱이 켜지면 실시간 감시 시작 (addSnapshotListener)
        fetchRealtimeData()
    }

    private fun fetchRealtimeData() {
        // "sensors" 라는 방(컬렉션)을 구독합니다.
        db.collection("sensors").addSnapshotListener { snapshot, e ->
            if (e != null) {
                // 에러가 나면 그냥 무시하거나 로그 출력
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // 문서들을 하나씩 꺼내서 우리가 쓸 'SensorData' 형태로 바꿉니다.
                val list = snapshot.documents.map { doc ->
                    val type = doc.getString("type") ?: ""
                    val value = doc.get("value").toString() // 숫자든 문자든 문자열로 변환
                    val status = doc.getString("status") ?: "Unknown"

                    // DB에 있는 'type' 글자를 보고 아이콘과 색상을 결정합니다.
                    mapToSensorData(type, value, status)
                }
                // 변수에 새 데이터를 넣으면 화면이 자동으로 갱신됩니다!
                sensors.value = list
            }
        }
    }

    // 데이터를 예쁘게 포장하는 함수
    private fun mapToSensorData(type: String, value: String, status: String): SensorData {
        return when (type) {
            "temp" -> SensorData(
                title = "Temperature",
                value = value,
                unit = "°C",
                statusText = status,
                color = Color(0xFFFFA500), // 주황색
                icon = Icons.Default.Thermostat
            )
            "illum" -> SensorData(
                title = "Illuminance",
                value = value,
                unit = " lx",
                statusText = status,
                color = Color(0xFFFFD60A), // 노란색
                icon = Icons.Default.WbSunny,
                isAlert = status == "Warning"
            )
            "water" -> SensorData(
                title = "Water Level",
                value = value,
                unit = " cm",
                statusText = status,
                color = Color(0xFF34C759), // 초록색
                icon = Icons.Default.WaterDrop
            )
            "ph" -> SensorData(
                title = "pH",
                value = value,
                unit = " pH",
                statusText = status,
                color = Color(0xFFFF3B30), // 빨간색
                icon = Icons.Default.Science,
                isAlert = status == "Alert"
            )
            else -> SensorData(
                title = "Unknown",
                value = value,
                unit = "",
                statusText = status,
                color = Color.Gray,
                icon = Icons.Default.Help
            )
        }
    }
}



// ... import 아래쪽에 추가


// ...

@Composable
fun LiveMonitorApp(
    // 여기에 뷰모델을 추가합니다. (자동으로 생성됨)
    viewModel: SensorViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ★ 핵심: 뷰모델에 있는 데이터를 가져옵니다.
    val sensors = viewModel.sensors.value

    MaterialTheme {
        ModalNavigationDrawer(

        ) {
            Scaffold(

            ) { paddingValues ->
                Column(...) {
                // ... (헤더, 시간 표시 기존 코드 유지) ...

                // ★ 수정: sensors 변수를 넘겨줍니다.
                SensorGridSection(sensors = sensors)
            }
            }
        }
    }
}

@Composable
fun SensorGridSection(sensors: List<SensorData>) { // <-- 인자로 받도록 수정!

    // ▼ 예전에 있던 가짜 데이터 코드는 삭제하세요! ▼
    // val sensors = listOf(...)

    // 데이터가 없을 때 로딩 표시 (선택사항)
    if (sensors.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("데이터 수신 중...", color = TextSecondary)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 받아온 sensors 리스트로 그리기
            items(sensors) { sensor ->
                SensorCard(sensor)
            }
        }
    }
}