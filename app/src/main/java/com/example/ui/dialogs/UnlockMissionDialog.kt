package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.entities.MissionSettingsEntity
import com.example.ui.FocusViewModel
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.StreakDoneGreen

@Composable
fun UnlockMissionDialog(
    viewModel: FocusViewModel,
    missionSettings: MissionSettingsEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var inputMeditation by remember { mutableStateOf("") }
    var inputResolution by remember { mutableStateOf("") }
    var inputFriendCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val samplePhrase = stringResource(id = R.string.meditation_sample_phrase)

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.unlock_dialog_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "몰입을 해제하려면 설정된 아래 미션을 완성해야 합니다.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                // Mission 1
                if (missionSettings.meditationEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "✍️ 미션 1: 명상 구절 따라 쓰기",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = samplePhrase,
                                color = IndigoPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = inputMeditation,
                                onValueChange = { inputMeditation = it },
                                placeholder = { Text("위 문장을 입력하세요", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                // Mission 2
                if (missionSettings.resolutionEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🎯 미션 2: 나만의 다짐문 작성 (10자 이상)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            OutlinedTextField(
                                value = inputResolution,
                                onValueChange = { inputResolution = it },
                                placeholder = { Text(stringResource(id = R.string.resolution_hint), fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                // Mission 3
                if (missionSettings.friendApprovalEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🤝 미션 3: 지인 승인 4자리 코드 (기본: 1234)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            OutlinedTextField(
                                value = inputFriendCode,
                                onValueChange = { inputFriendCode = it },
                                placeholder = { Text("4자리 코드 입력", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                        Text(text = errorMessage!!, color = Color(0xFFEF4444), fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = {
                        var isValid = true
                        if (missionSettings.meditationEnabled) {
                            if (inputMeditation.trim() != samplePhrase.trim()) {
                                isValid = false
                                errorMessage = "명상 구절이 일치하지 않습니다."
                            }
                        }
                        if (isValid && missionSettings.resolutionEnabled) {
                            if (inputResolution.trim().length < 10) {
                                isValid = false
                                errorMessage = "다짐문은 10자 이상 작성해야 합니다."
                            }
                        }
                        if (isValid && missionSettings.friendApprovalEnabled) {
                            val targetCode = missionSettings.friendCode.ifEmpty { "1234" }
                            if (inputFriendCode.trim() != targetCode) {
                                isValid = false
                                errorMessage = "지인 승인 코드가 올바르지 않습니다."
                            }
                        }

                        if (isValid) {
                            viewModel.toggleFocusMode()
                            Toast.makeText(context, "미션을 완료하여 몰입이 해제되었습니다!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StreakDoneGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.complete_unlock),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
