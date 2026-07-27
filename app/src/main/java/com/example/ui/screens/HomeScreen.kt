package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.FocusViewModel
import com.example.ui.WeeklyStreakItem
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.StreakDoneGreen
import com.example.ui.theme.StreakGold
import com.example.ui.theme.TealSecondary

@Composable
fun HomeScreen(
    viewModel: FocusViewModel,
    onShowUnlockDialog: () -> Unit
) {
    val missionSettings by viewModel.missionSettings.collectAsState()
    val weeklyStreaks by viewModel.weeklyStreaks.collectAsState()
    val totalSeconds by viewModel.currentLiveFocusSeconds.collectAsState()

    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Card: Weekly Success Streak
        WeeklyStreakCard(weeklyStreaks = weeklyStreaks)

        // Today's Endured Time Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                IndigoPrimary.copy(alpha = 0.15f),
                                CyanAccent.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = IndigoPrimary
                            )
                            Text(
                                text = stringResource(id = R.string.today_endured_time),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%02d : %02d : %02d", hours, minutes, seconds),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = IndigoPrimary,
                            letterSpacing = 2.sp
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (missionSettings.isFocusing) StreakDoneGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (missionSettings.isFocusing) Icons.Default.Psychology else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (missionSettings.isFocusing) StreakDoneGreen else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Center: One-Click Large 'Start Focus' / 'Focusing' Button
        FocusButtonSection(
            isFocusing = missionSettings.isFocusing,
            onToggleFocus = {
                if (missionSettings.isFocusing) {
                    // Show unlock mission dialog to end focus
                    onShowUnlockDialog()
                } else {
                    viewModel.toggleFocusMode()
                }
            }
        )

        // Bottom: Unlock Mission Multi-Choice Checkboxes
        UnlockMissionsCard(
            meditationEnabled = missionSettings.meditationEnabled,
            resolutionEnabled = missionSettings.resolutionEnabled,
            friendApprovalEnabled = missionSettings.friendApprovalEnabled,
            onMissionsChanged = { med, res, friend ->
                viewModel.updateMissions(med, res, friend)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun WeeklyStreakCard(weeklyStreaks: List<WeeklyStreakItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = StreakGold
                    )
                    Text(
                        text = stringResource(id = R.string.weekly_streak),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "목표: 하루 30분 이상",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyStreaks.forEach { item ->
                    StreakStampBadge(item = item)
                }
            }
        }
    }
}

@Composable
fun StreakStampBadge(item: WeeklyStreakItem) {
    val bgColor = if (item.isDone) StreakDoneGreen else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (item.isDone) Color.White else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(
                    width = if (item.isDone) 2.dp else 1.dp,
                    color = if (item.isDone) StreakDoneGreen else Color.LightGray.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (item.isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = item.dayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
        if (item.isDone) {
            Text(
                text = item.dayName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = StreakDoneGreen
            )
        }
    }
}

@Composable
fun FocusButtonSection(
    isFocusing: Boolean,
    onToggleFocus: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isFocusing) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isFocusing) TealSecondary else IndigoPrimary,
        animationSpec = tween(500),
        label = "btnColor"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isFocusing) {
                            listOf(TealSecondary, IndigoDark)
                        } else {
                            listOf(IndigoPrimary, IndigoDark)
                        }
                    )
                )
                .clickable { onToggleFocus() }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isFocusing) Icons.Default.Psychology else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isFocusing) stringResource(id = R.string.focusing) else stringResource(id = R.string.start_focus),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = if (isFocusing) "터치하여 해제 미션" else "터치하여 몰입 시작",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun UnlockMissionsCard(
    meditationEnabled: Boolean,
    resolutionEnabled: Boolean,
    friendApprovalEnabled: Boolean,
    onMissionsChanged: (Boolean, Boolean, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.unlock_missions_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "몰입을 도중에 해제할 때 수행해야 할 미션들을 선택하세요.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Option 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMissionsChanged(!meditationEnabled, resolutionEnabled, friendApprovalEnabled) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = meditationEnabled,
                    onCheckedChange = { onMissionsChanged(it, resolutionEnabled, friendApprovalEnabled) },
                    colors = CheckboxDefaults.colors(checkedColor = IndigoPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.mission_meditation),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Option 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMissionsChanged(meditationEnabled, !resolutionEnabled, friendApprovalEnabled) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = resolutionEnabled,
                    onCheckedChange = { onMissionsChanged(meditationEnabled, it, friendApprovalEnabled) },
                    colors = CheckboxDefaults.colors(checkedColor = IndigoPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.mission_resolution),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Option 3
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMissionsChanged(meditationEnabled, resolutionEnabled, !friendApprovalEnabled) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = friendApprovalEnabled,
                    onCheckedChange = { onMissionsChanged(meditationEnabled, resolutionEnabled, it) },
                    colors = CheckboxDefaults.colors(checkedColor = IndigoPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.mission_friend_approval),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
