package com.irys.app.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.irys.app.core.ui.components.IrysCard
import com.irys.app.core.ui.components.IrysTopAppBar
import com.irys.app.core.ui.theme.BorderSubtle
import com.irys.app.core.ui.theme.EmergencyAmber
import com.irys.app.core.ui.theme.StatusConnecting
import com.irys.app.core.ui.theme.StatusOnline
import com.irys.app.domain.model.Message
import com.irys.app.domain.model.MessagePriority
import com.irys.app.domain.model.MessageStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            IrysTopAppBar(
                title = uiState.conversation?.title ?: "Chat",
                onNavigateBack = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .imePadding()
        ) {
            // Message List
            if (uiState.messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IrysCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Start Offline Conversation",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Messages typed here are stored locally in Room database and queued for transmission over Bluetooth mesh.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.messages, key = { it.messageId }) { message ->
                        MessageBubble(
                            message = message,
                            onStatusChange = { newStatus ->
                                viewModel.updateMessageStatus(message.messageId, newStatus)
                            },
                            onRetry = {
                                viewModel.retryMessage(message.messageId)
                            }
                        )
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = { viewModel.onInputTextChanged(it) },
                    placeholder = {
                        Text(
                            text = "Type offline message...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                IconButton(
                    onClick = { viewModel.sendMessage() },
                    enabled = uiState.inputText.isNotBlank() && !uiState.isSending,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (uiState.inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (uiState.inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    onStatusChange: (MessageStatus) -> Unit,
    onRetry: () -> Unit
) {
    val isOutgoing = message.isOutgoing
    var showMenu by remember { mutableStateOf(false) }

    val alignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isOutgoing) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isOutgoing) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isOutgoing) 16.dp else 4.dp,
                            bottomEnd = if (isOutgoing) 4.dp else 16.dp
                        )
                    )
                    .background(bubbleColor)
                    .clickable { showMenu = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    if (message.priority == MessagePriority.CRITICAL || message.priority == MessagePriority.HIGH) {
                        Text(
                            text = "[${message.priority.name}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Text(
                        text = message.payload,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = formatTimestamp(message.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isOutgoing) {
                            Spacer(modifier = Modifier.width(4.dp))
                            StatusIcon(status = message.status)
                        }
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Status: ${message.status.name}") },
                        onClick = { showMenu = false },
                        enabled = false
                    )
                    DropdownMenuItem(
                        text = { Text("Set Delivered") },
                        onClick = {
                            showMenu = false
                            onStatusChange(MessageStatus.DELIVERED)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Set Stored (Relayed)") },
                        onClick = {
                            showMenu = false
                            onStatusChange(MessageStatus.STORED)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Set Failed") },
                        onClick = {
                            showMenu = false
                            onStatusChange(MessageStatus.FAILED)
                        }
                    )
                    if (message.status == MessageStatus.FAILED) {
                        DropdownMenuItem(
                            text = { Text("Retry") },
                            onClick = {
                                showMenu = false
                                onRetry()
                            }
                        )
                    }
                }
            }

            if (isOutgoing && message.status == MessageStatus.FAILED) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onRetry)
                        .padding(top = 2.dp, end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Failed. Tap to retry",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusIcon(status: MessageStatus) {
    when (status) {
        MessageStatus.CREATED, MessageStatus.QUEUED -> {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Queued",
                tint = EmergencyAmber,
                modifier = Modifier.size(13.dp)
            )
        }
        MessageStatus.STORED, MessageStatus.TRANSFERRING -> {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Stored/Transferring",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(13.dp)
            )
        }
        MessageStatus.FORWARDED, MessageStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Delivered",
                tint = StatusOnline,
                modifier = Modifier.size(13.dp)
            )
        }
        MessageStatus.FAILED -> {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Failed",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(13.dp)
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = status.name,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
