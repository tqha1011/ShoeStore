package com.example.shoestoreapp.features.admin.ai_assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AI Strategy Assistant Screen
 * Optimized UI for AI interactions and campaign generation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStrategyAssistantScreen(
    onBackClick: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            // Clean Top Bar following the minimalist aesthetic
            CenterAlignedTopAppBar(
                title = {
                    Text("Strategy Assistant", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Placeholder for additional options (e.g., Clear Chat)
                    TextButton(onClick = { /* Clear logic */ }) {
                        Text("Reset", color = Color.Gray, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Chat Content Area
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Initial AI Analysis Message
                item {
                    AiMessageBubble(
                        text = "Analyzing Q3 performance markers. I've identified a strong correlation between desktop ad spend and mobile conversion attrition."
                    )
                }

                // 2. User Message (Example Request)
                item {
                    UserMessageBubble(
                        text = "Run a Revenue Optimization analysis for the last 14 days focused on mobile users."
                    )
                }

                // 3. Rich Strategy Card (The "Hero" component)
                item {
                    StrategyInsightCard(
                        growthPercent = 12.4,
                        estConversion = "+18.5%",
                        roiProjection = "4.2x",
                        onExecute = { /* Handle execution */ }
                    )
                }
            }

            // Floating Input Bar at the bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding() // Adjusts height when keyboard appears
                    .padding(16.dp)
            ) {
                ChatInputBar(
                    value = inputText,
                    onValueChange = { inputText = it },
                    onSend = { /* Handle send */ }
                )
            }
        }
    }
}

/**
 * Standard AI response bubble
 */
@Composable
fun AiMessageBubble(text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFF4F4F5), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFF4F4F5), RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                    .padding(12.dp)
            ) {
                Text(text = text, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF27272A))
            }
            Text(
                "AI STRATEGIST • NOW",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(40.dp)) // Offset for user alignment
    }
}

/**
 * Standard User message bubble aligned to the right
 */
@Composable
fun UserMessageBubble(text: String) {
    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(60.dp))
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .background(Color.Black, RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp))
                    .padding(12.dp)
            ) {
                Text(text = text, fontSize = 14.sp, color = Color.White, lineHeight = 20.sp)
            }
            Text(
                "ADMIN • JUST NOW",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp, end = 4.dp)
            )
        }
    }
}

/**
 * Advanced Insight Card with Metrics and Actions
 */
@Composable
fun StrategyInsightCard(
    growthPercent: Double,
    estConversion: String,
    roiProjection: String,
    onExecute: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(
            Icons.Default.Insights,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Black
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("PROPOSED GROWTH CAMPAIGN", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Based on the $growthPercent% mobile growth, I recommend a 48-hour app-exclusive sale.",
                fontSize = 14.sp, lineHeight = 20.sp
            )

            // Metrics Row
            Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricSmallBox("EST. CONVERSION", estConversion, Modifier.weight(1f))
                MetricSmallBox("ROI PROJECTION", roiProjection, Modifier.weight(1f))
            }

            // Buttons
            Button(
                onClick = onExecute,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("EXECUTE CAMPAIGN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MetricSmallBox(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .border(1.dp, Color(0xFFF4F4F5), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
}

/**
 * Floating input field with blur-like effect (White background with shadow)
 */
@Composable
fun ChatInputBar(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) Text("Ask about optimization...", color = Color.LightGray, fontSize = 14.sp)
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(
                onClick = onSend,
                modifier = Modifier.background(Color.Black, CircleShape).size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}