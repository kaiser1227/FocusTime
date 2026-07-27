package com.example.service

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.entities.MissionSettingsEntity
import com.example.data.repository.FocusRepository
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class LockOverlayActivity : ComponentActivity() {

    private lateinit var repository: FocusRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        repository = FocusRepository(db.appLockDao(), applicationContext)

        val lockedAppName = intent.getStringExtra("LOCKED_APP_NAME") ?: "차단된 앱"

        setContent {
            MyApplicationTheme(darkTheme = true) {
                LockOverlayScreen(
                    lockedAppName = lockedAppName,
                    repository = repository,
                    onUnlockSuccess = {
                        finish()
                    },
                    onGoHome = {
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun LockOverlayScreen(
    lockedAppName: String,
    repository: FocusRepository,
    onUnlockSuccess: () -> Unit,
    onGoHome: () -> Unit
) {
    BackHandler {
        onGoHome()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var missionSettings by remember { mutableStateOf<MissionSettingsEntity?>(null) }

    var inputMeditation by remember { mutableStateOf("") }
    var inputResolution by remember { mutableStateOf("") }
    var inputFriendCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val samplePhrase = stringResource(id = R.string.meditation_sample_phrase)

    LaunchedEffect(Unit) {
        missionSettings = repository.missionSettings.firstOrNull()
    }

    val settings = missionSettings

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (settings == null) {
            CircularProgressIndicator(color = IndigoPrimary)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Shield",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(72.dp)
                )

                Text(
                    text = "몰입 모드 가동 중!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "[$lockedAppName] 앱은 몰입 중 차단되었습니다.\n해제하려면 아래 설정된 미션을 모두 완성하세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mission 1: Meditation Transcription
                if (settings.meditationEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "✍️ 미션 1: 명상 구절 따라 쓰기",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = samplePhrase,
                                color = Color(0xFF38BDF8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = inputMeditation,
                                onValueChange = { inputMeditation = it },
                                placeholder = { Text("위 문장을 그대로 따라 쓰세요", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                        }
                    }
                }

                // Mission 2: Resolution
                if (settings.resolutionEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🎯 미션 2: 나만의 다짐문 작성 (10자 이상)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            OutlinedTextField(
                                value = inputResolution,
                                onValueChange = { inputResolution = it },
                                placeholder = { Text("스마트폰을 내려놓고 할 일을 적으세요", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                        }
                    }
                }

                // Mission 3: Friend Code
                if (settings.friendApprovalEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🤝 미션 3: 지인 해제 코드 승인 (기본: 1234)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            OutlinedTextField(
                                value = inputFriendCode,
                                onValueChange = { inputFriendCode = it },
                                placeholder = { Text("4자리 코드 입력", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                        Text(text = errorMessage!!, color = Color(0xFFEF4444), fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = {
                        // Validation logic
                        var isValid = true
                        if (settings.meditationEnabled) {
                            if (inputMeditation.trim() != samplePhrase.trim()) {
                                isValid = false
                                errorMessage = "명상 구절이 일치하지 않습니다!"
                            }
                        }
                        if (isValid && settings.resolutionEnabled) {
                            if (inputResolution.trim().length < 10) {
                                isValid = false
                                errorMessage = "다짐문은 최소 10자 이상 작성해 주세요!"
                            }
                        }
                        if (isValid && settings.friendApprovalEnabled) {
                            val targetCode = settings.friendCode.ifEmpty { "1234" }
                            if (inputFriendCode.trim() != targetCode) {
                                isValid = false
                                errorMessage = "지인 승인 코드가 올바르지 않습니다!"
                            }
                        }

                        if (isValid) {
                            scope.launch {
                                repository.setFocusMode(false) // Turn off focus mode upon successful unlock
                                Toast.makeText(context, "미션을 완수하였습니다! 몰입이 해제됩니다.", Toast.LENGTH_SHORT).show()
                                onUnlockSuccess()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("미션 완수 및 앱 잠금 해제", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = { onGoHome() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text("홈으로 돌아가기", color = Color.White)
                }
            }
        }
    }
}
