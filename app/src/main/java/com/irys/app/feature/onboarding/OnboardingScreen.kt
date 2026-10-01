package com.irys.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.irys.app.core.ui.components.IrysButton
import com.irys.app.core.ui.components.IrysButtonStyle
import com.irys.app.core.ui.components.IrysCard

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onFinished()
        }
    }

    val currentStep = uiState.steps.getOrNull(uiState.currentStepIndex) ?: return
    val isLastStep = uiState.currentStepIndex == uiState.steps.size - 1

    val stepIcon: ImageVector = when (uiState.currentStepIndex) {
        0 -> Icons.Default.CellTower
        1 -> Icons.Default.WifiOff
        2 -> Icons.Default.Hub
        3 -> Icons.Default.Lock
        4 -> Icons.AutoMirrored.Filled.BluetoothSearching
        else -> Icons.Default.CellTower
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar: Step counter and Skip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STEP ${uiState.currentStepIndex + 1} OF ${uiState.steps.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isLastStep) {
                TextButton(onClick = { viewModel.skipOnboarding() }) {
                    Text(
                        text = "Skip",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        // Center Content with Animated Transition
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = uiState.currentStepIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "OnboardingContentTransition"
            ) { index ->
                val step = uiState.steps[index]
                val icon = when (index) {
                    0 -> Icons.Default.CellTower
                    1 -> Icons.Default.WifiOff
                    2 -> Icons.Default.Hub
                    3 -> Icons.Default.Lock
                    4 -> Icons.AutoMirrored.Filled.BluetoothSearching
                    else -> Icons.Default.CellTower
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(88.dp)
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    IrysCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = step.subtitle.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Indicator Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                uiState.steps.indices.forEach { index ->
                    val isSelected = index == uiState.currentStepIndex
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 10.dp else 7.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    )
                    if (index < uiState.steps.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }

        // Bottom Controls
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.currentStepIndex > 0) {
                    IrysButton(
                        text = "Back",
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.weight(1f),
                        style = IrysButtonStyle.SECONDARY
                    )
                }

                IrysButton(
                    text = if (isLastStep) "Get Started" else "Next",
                    onClick = { viewModel.nextStep() },
                    modifier = Modifier.weight(if (uiState.currentStepIndex > 0) 2f else 1f),
                    style = IrysButtonStyle.PRIMARY
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
