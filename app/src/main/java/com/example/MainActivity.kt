package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.FirestoreSyncRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.performSignOut
import com.example.ui.screens.SimpleMainScreen
import com.example.ui.theme.AmarHisabTheme
import com.example.ui.theme.AppThemes
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.LocalIsBengali
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.ExpenseViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            val credentialManager = remember { CredentialManager.create(context) }

            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember {
                ExpenseRepository(
                    database.transactionDao(),
                    database.monthlyBudgetDao(),
                    database.accountDao(),
                    database.loanDao()
                )
            }
            val syncRepository = remember { FirestoreSyncRepository(context) }
            val preferencesRepository = remember { UserPreferencesRepository(context) }

            val viewModel: ExpenseViewModel = viewModel(
                factory = ExpenseViewModelFactory(repository, preferencesRepository, syncRepository)
            )

            val currentThemeKey by viewModel.selectedThemeKey.collectAsStateWithLifecycle()
            val currentLanguageKey by viewModel.selectedLanguageKey.collectAsStateWithLifecycle()

            val activeTheme = remember(currentThemeKey) { AppThemes.getTheme(currentThemeKey) }
            val isBengali = currentLanguageKey == UserPreferencesRepository.LANG_BN

            var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
            var isCheckingAuth by remember { mutableStateOf(Firebase.auth.currentUser == null) }
            var isGuestBypass by remember { mutableStateOf(false) }

            DisposableEffect(Unit) {
                val authListener = FirebaseAuth.AuthStateListener { auth ->
                    currentUser = auth.currentUser
                    isCheckingAuth = false
                }
                Firebase.auth.addAuthStateListener(authListener)
                onDispose {
                    Firebase.auth.removeAuthStateListener(authListener)
                }
            }

            LaunchedEffect(Unit) {
                if (currentUser == null) {
                    attemptAutoSignIn(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = { isCheckingAuth = false },
                        onUnauthenticated = { isCheckingAuth = false },
                        scope = coroutineScope
                    )
                }
            }

            CompositionLocalProvider(
                LocalAppTheme provides activeTheme,
                LocalIsBengali provides isBengali
            ) {
                AmarHisabTheme(themeColors = activeTheme) {
                    when {
                        isCheckingAuth && !isGuestBypass -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                activeTheme.topBarBg,
                                                activeTheme.primaryDark,
                                                activeTheme.background
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        modifier = Modifier.size(80.dp),
                                        shape = RoundedCornerShape(22.dp),
                                        color = activeTheme.primary.copy(alpha = 0.25f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(42.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        text = if (isBengali) "আমার হিসাব" else "Amar Hisab",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    CircularProgressIndicator(
                                        color = activeTheme.highlight,
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                }
                            }
                        }
                        currentUser == null && !isGuestBypass -> {
                            AuthScreen(
                                onAuthSuccess = {
                                    currentUser = Firebase.auth.currentUser
                                    isCheckingAuth = false
                                },
                                onSkip = {
                                    isGuestBypass = true
                                    isCheckingAuth = false
                                }
                            )
                        }
                        else -> {
                            SimpleMainScreen(
                                viewModel = viewModel,
                                onSignOut = {
                                    performSignOut(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onSignOutComplete = {
                                            currentUser = null
                                            isGuestBypass = false
                                        },
                                        scope = coroutineScope
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
