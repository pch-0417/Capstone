package com.example.capstone_app

import android.os.Bundle
import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 1. 카메라 피드 이미지 로드 (Glide 사용)
        val ivCamera = findViewById<ImageView>(R.id.iv_camera_feed)
        ivCamera?.let {
            Glide.with(this)
                .load("https://images.unsplash.com/photo-1530836369250-ef72085e4800") // 예시 이미지 URL
                .centerCrop()
                .into(it)
        }

        // 2. 각 카드 데이터 세팅하기
        setupCard(
            findViewById(R.id.card_temp),
            "Temperature", "22.5°C", "Normal",
            R.drawable.ic_thermostat, R.color.temp_orange, R.color.water_green
        )

        setupCard(
            findViewById(R.id.card_lux),
            "Illuminance", "850 lux", "Warning",
            R.drawable.ic_wb_sunny, R.color.lux_yellow, R.color.lux_yellow
        )

        setupCard(
            findViewById(R.id.card_water),
            "Water Level", "78 cm", "Normal",
            R.drawable.ic_water_drop, R.color.water_green, R.color.water_green
        )

        setupCard(
            findViewById(R.id.card_ph),
            "pH", "6.8 pH", "Alert",
            R.drawable.ic_science, R.color.ph_red, R.color.ph_red
        )
    }

    // 카드를 초기화하는 헬퍼 함수
    private fun setupCard(
        cardView: View,
        title: String,
        value: String,
        status: String,
        iconRes: Int,
        mainColorRes: Int,
        statusColorRes: Int
    ) {
        val mainColor = ContextCompat.getColor(this, mainColorRes)
        val statusColor = ContextCompat.getColor(this, statusColorRes)

        cardView.findViewById<TextView>(R.id.tv_title).text = title
        cardView.findViewById<TextView>(R.id.tv_value).text = value

        val tvStatus = cardView.findViewById<TextView>(R.id.tv_status)
        tvStatus.text = status
        tvStatus.setTextColor(statusColor)

        // 아이콘 색상 및 이미지 설정
        val ivIcon = cardView.findViewById<ImageView>(R.id.iv_icon)
        ivIcon.setImageResource(iconRes)
        ivIcon.imageTintList = ColorStateList.valueOf(mainColor)

        // 상태 점 색상 설정
        val dotView = cardView.findViewById<View>(R.id.view_status_dot)
        dotView.backgroundTintList = ColorStateList.valueOf(statusColor)

        // 그래프 색상 설정
        val chart = cardView.findViewById<com.github.mikephil.charting.charts.LineChart>(R.id.lineChart)
        chart?.let {
            it.setNoDataText("Loading data...") // 데이터 없을 때 뜰 글자
            it.invalidate() // 새로고침
        }
    }
}

