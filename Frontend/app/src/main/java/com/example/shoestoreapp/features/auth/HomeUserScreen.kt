package com.example.shoestoreapp.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun HomeUserScreen(
    // PASS A CALLBACK FUNCTION TO HANDLE LOGOUT EVENT
    onLogoutClick: () -> Unit
) {
    // A SIMPLE UI WITH A CENTERED BUTTON
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Welcome to Home User!")

        Spacer(modifier = Modifier.height(16.dp))

        // TRIGGER THE CALLBACK WHEN CLICKED
        Button(onClick = { onLogoutClick() }) {
            Text(text = "Logout")
        }
    }
}
