package com.example.shoestoreapp.features.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.sharp.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.R

// StyleAuth
data class AuthFieldStyle(
    val label: String,
    val containerColor: Color,
    val textColor: Color,
    val unfocusedBorderColor: Color = Color.Transparent
)
// Function Background for Template
@Composable
fun AuthBackground(canvasColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val yBoundary = height / 3.8f

        val path = Path().apply {
            moveTo(0f, height)
            lineTo(0f, yBoundary)
            cubicTo(
                x1 = width / 2f,
                y1 = yBoundary - 150f,
                x2 = width / 2f,
                y2 = yBoundary + 450f,
                x3 = width,
                y3 = yBoundary + 100f,
            )
            lineTo(width, height)
            close()
        }
        drawPath(path = path, color = canvasColor)
    }
}

// Function TopBar for Template
@Composable
fun AuthTopBar(
    buttonText: String,
    onButtonClick: () -> Unit,
    contentColor: Color,
    buttonContainerColor: Color
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()) {
        IconButton(
            onClick = { /* Dashboard click */ },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 5.dp)
        ) {
            Icon(
                Icons.Sharp.Dashboard,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(30.dp)
            )
        }

        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonContainerColor,
                contentColor = Color.Gray
            ),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(25.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

// Function Title for Template
@Composable
fun TitleText(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 45.sp,
        color = color,
        fontWeight = FontWeight.Normal
    )
}

// Function Email Input for Template
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorText: String?,
    style: AuthFieldStyle,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
        Text(
            text = style.label,
            fontSize = 17.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            isError = isError,
            supportingText = { errorText?.let { Text(it) } },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = style.containerColor,
                unfocusedContainerColor = style.containerColor,
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = style.unfocusedBorderColor,
                focusedTextColor = style.textColor,
                unfocusedTextColor = style.textColor.copy(alpha = 0.7f),
                cursorColor = style.textColor,
                errorContainerColor = style.containerColor
            )
        )
    }
}

// Function Password Input for Template
@Composable
fun AuthPasswordField(
    style: AuthFieldStyle,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorText: String?,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
        Text(
            text = style.label,
            fontSize = 17.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            isError = isError,
            supportingText = { errorText?.let { Text(it) } },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = style.containerColor,
                unfocusedContainerColor = style.containerColor,
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = style.unfocusedBorderColor,
                focusedTextColor = style.textColor,
                unfocusedTextColor = style.textColor.copy(alpha = 0.7f),
                cursorColor = style.textColor,
                errorContainerColor = style.containerColor
            )
        )
    }
}



// Function Button for Template
@Composable
fun AuthActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .border(
                border = BorderStroke(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFE8F33),
                            Color(0xFFC246DE)
                        )
                    )
                ),
                shape = RoundedCornerShape(50.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(50.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

// Function Social Form for Template
@Composable
fun SocialLoginSection(
    dividerColor: Color,
    textColor: Color,
    buttonContainerColor: Color,
    iconTint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = dividerColor)
            Text(
                "Or continue with",
                color = textColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = dividerColor)
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            SocialButton(R.drawable.ic_google, buttonContainerColor, iconTint)

            Spacer(modifier = Modifier.width(20.dp))

            SocialButton(R.drawable.ic_facebook, buttonContainerColor, iconTint)

            Spacer(modifier = Modifier.width(20.dp))

            SocialButton(R.drawable.ic_tiktok, buttonContainerColor, iconTint)
        }
    }
}

// Function Social Button for Template
@Composable
private fun SocialButton(iconRes: Int, containerColor: Color, iconTint: Color) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(containerColor, shape = RoundedCornerShape(10.dp))
            .clickable { /* Social Click */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource(iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(35.dp)
        )
    }
}