package com.example.shoestoreapp.features.auth.ui.reset_password

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordTopBar(
    onBackClick: () -> Unit = {}
) {
    TopAppBar(
        title = { 
            Text(text = "", color = Color.Black) 
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        },
    );

}

@Composable
fun ResetPasswordContent(
    paddingValues: PaddingValues,
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Header title
        Text(
            text = title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            lineHeight = 40.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Short Description
        Text(
            text = description,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(50.dp))
        
        content()
    }
}
@Composable
fun TitleBottom(){
    Text(
        text = "Shoe Store App",
        color = Color.LightGray,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 20.dp)
    )
}