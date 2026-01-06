package com.example.car_shop.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Added for Color.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.car_shop.R
import com.example.car_shop.navigation.Screen

@Composable
fun OnboardingScreen(navController: NavController) {

    val brandPurple = MaterialTheme.colorScheme.primary
    val textPrimary = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Changed background to white
            .padding(horizontal = 30.dp, vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Illustration Area
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Decorative accent (The floating circle)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .offset(x = 100.dp, y = (-70).dp)
                    .background(brandPurple.copy(alpha = 0.1f), CircleShape)
            )

            Image(
                painter = painterResource(id = R.drawable.onboarding_car),
                contentDescription = "Onboarding Illustration",
                modifier = Modifier
                    .size(300.dp)
                    .padding(16.dp)
            )
        }

        // 2. Text Content Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Now you can be apart of cars\nauctions with the easiest",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp,
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "For the first time we're providing an online auction system to ease the process of buying & selling",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // 4. Action Button
        Button(
            onClick = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = brandPurple,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                "Get Start",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}