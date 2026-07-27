package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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
import com.example.R
import com.example.ui.AppItemUiState
import com.example.ui.FocusViewModel
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.StreakDoneGreen

@Composable
fun SettingsScreen(viewModel: FocusViewModel) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    val isAccPermissionGranted by viewModel.isAccessibilityPermissionGranted.collectAsState()
    val isOverlayPermissionGranted by viewModel.isOverlayPermissionGranted.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val lockedCount = remember(installedApps) {
        installedApps.count { it.isLocked }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Permission Status Section Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = IndigoPrimary
                    )
                    Text(
                        text = stringResource(id = R.string.permission_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Accessibility Permission
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.permission_accessibility_title),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = stringResource(id = R.string.permission_accessibility_desc),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    if (isAccPermissionGranted) {
                        AssistChip(
                            onClick = {},
                            label = { Text("허용됨", fontSize = 11.sp, color = StreakDoneGreen) },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, tint = StreakDoneGreen) }
                        )
                    } else {
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("허용", fontSize = 12.sp)
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Overlay Permission
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.permission_overlay_title),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = stringResource(id = R.string.permission_overlay_desc),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    if (isOverlayPermissionGranted) {
                        AssistChip(
                            onClick = {},
                            label = { Text("허용됨", fontSize = 11.sp, color = StreakDoneGreen) },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, tint = StreakDoneGreen) }
                        )
                    } else {
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("허용", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Header & Search Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.locked_apps_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.total_apps_count, installedApps.size, lockedCount),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            placeholder = { Text(stringResource(id = R.string.search_apps_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        // Notice for Default Excluded Apps
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(id = R.string.default_excluded_apps_notice),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Installed App List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                AppLockItemRow(
                    app = app,
                    onToggle = { isChecked ->
                        viewModel.toggleAppLock(app.packageName, isChecked)
                    }
                )
            }
        }
    }
}

@Composable
fun AppLockItemRow(
    app: AppItemUiState,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isLocked) IndigoPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (app.isDefaultExcluded) Color.Gray.copy(alpha = 0.2f)
                            else if (app.isLocked) IndigoPrimary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (app.isDefaultExcluded) Icons.Default.Phone
                                           else if (app.isLocked) Icons.Default.Lock
                                           else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (app.isDefaultExcluded) Color.Gray
                                   else if (app.isLocked) IndigoPrimary
                                   else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = app.appName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = app.packageName,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            if (app.isDefaultExcluded) {
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(id = R.string.excluded_label), fontSize = 11.sp, color = Color.Gray) }
                )
            } else {
                Switch(
                    checked = app.isLocked,
                    onCheckedChange = { onToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = IndigoPrimary
                    )
                )
            }
        }
    }
}
