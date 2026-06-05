package com.example.shoestoreapp.features.user.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.shoestoreapp.features.user.profile.data.models.UserProfile

@Composable
fun ProfileHeader(
    profile: UserProfile,
    modifier: Modifier = Modifier,
    onEditAvatarClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(128.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(3.dp, Color(0xFFF5F5F5), CircleShape)
                    .clickable { onEditAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.size(18.dp))

        Text(
            text = profile.name.uppercase(),
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            color = Color.Black
        )

        Text(
            text = profile.email,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF8A8A8A),
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE6E8F1))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = "Birthday",
                tint = Color(0xFF6F6F6F),
                modifier = Modifier.size(14.dp)
            )

            Text(
                text = profile.birthday.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Color(0xFF6F6F6F),
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

