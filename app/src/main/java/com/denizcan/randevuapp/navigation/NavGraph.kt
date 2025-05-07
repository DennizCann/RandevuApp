package com.denizcan.randevuapp.navigation

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.denizcan.randevuapp.ui.screen.CustomerInfoScreen
import com.denizcan.randevuapp.ui.screen.BusinessInfoScreen
import com.denizcan.randevuapp.ui.screen.LoginScreen
import com.denizcan.randevuapp.ui.screen.RegisterScreen
import com.denizcan.randevuapp.ui.screen.CustomerHomeScreen
import com.denizcan.randevuapp.ui.screen.BusinessHomeScreen
import com.denizcan.randevuapp.viewmodel.AuthViewModel
import com.denizcan.randevuapp.viewmodel.UserInfoViewModel
import com.denizcan.randevuapp.viewmodel.HomeViewModel
import com.denizcan.randevuapp.viewmodel.UserInfoViewModel.UserInfoState
import com.denizcan.randevuapp.viewmodel.BusinessListViewModel
import com.denizcan.randevuapp.viewmodel.BusinessListViewModel.BusinessListState
import com.denizcan.randevuapp.ui.screen.BusinessListScreen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import com.denizcan.randevuapp.viewmodel.BusinessDetailViewModel
import com.denizcan.randevuapp.viewmodel.BusinessDetailViewModel.BusinessDetailState
import com.denizcan.randevuapp.ui.screen.BusinessDetailScreen
import com.denizcan.randevuapp.viewmodel.BusinessHomeViewModel
import com.denizcan.randevuapp.viewmodel.BusinessHomeViewModel.BusinessHomeState
import com.denizcan.randevuapp.ui.screen.WorkingHoursScreen
import com.denizcan.randevuapp.ui.screen.BusinessCalendarScreen
import com.denizcan.randevuapp.viewmodel.BusinessHomeViewModel.CalendarState
import org.threeten.bp.LocalDate
import com.denizcan.randevuapp.ui.screen.AppointmentRequestsScreen
import com.denizcan.randevuapp.ui.screen.CustomerAppointmentsScreen
import com.denizcan.randevuapp.viewmodel.CustomerHomeViewModel
import com.denizcan.randevuapp.viewmodel.CustomerHomeViewModel.CustomerHomeState
import com.denizcan.randevuapp.viewmodel.CustomerHomeViewModel.AppointmentsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.denizcan.randevuapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.denizcan.randevuapp.ui.screen.LanguageSettingsScreen
import androidx.compose.ui.platform.LocalContext
import com.denizcan.randevuapp.MainActivity
import com.denizcan.randevuapp.ui.screen.SettingsScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.R
import androidx.compose.ui.text.style.TextAlign
import com.denizcan.randevuapp.model.Appointment

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object CustomerRegister : Screen("customer_register")
    object BusinessRegister : Screen("business_register")
    object CustomerInfo : Screen("customer_info/{userId}") {
        fun createRoute(userId: String) = "customer_info/$userId"
    }
    object BusinessInfo : Screen("business_info/{userId}") {
        fun createRoute(userId: String) = "business_info/$userId"
    }
    object CustomerHome : Screen("customer_home/{customerId}") {
        fun createRoute(customerId: String) = "customer_home/$customerId"
    }
    object BusinessHome : Screen("business_home/{userId}") {
        fun createRoute(userId: String) = "business_home/$userId"
    }
    object BusinessList : Screen("business_list")
    object BusinessDetail : Screen("business_detail/{businessId}") {
        fun createRoute(businessId: String) = "business_detail/$businessId"
    }
    object WorkingHours : Screen("working_hours/{businessId}") {
        fun createRoute(businessId: String) = "working_hours/$businessId"
    }
    object BusinessCalendar : Screen("business_calendar/{businessId}") {
        fun createRoute(businessId: String) = "business_calendar/$businessId"
    }
    object AppointmentRequests : Screen("appointment_requests/{businessId}") {
        fun createRoute(businessId: String) = "appointment_requests/$businessId"
    }
    object CustomerAppointments : Screen("customer_appointments")
    object LanguageSettings : Screen("language_settings")
    object Settings : Screen("settings")
    object AppointmentConfirmation : Screen("appointment_confirmation")
}

@Composable
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val authState = authViewModel.authState.collectAsState()
    val userInfoViewModel: UserInfoViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val isBusinessLogin = remember { mutableStateOf(false) }

            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.signIn(email, password, isBusinessLogin.value)
                },
                onCustomerRegisterClick = {
                    navController.navigate(Screen.CustomerRegister.route)
                },
                onBusinessRegisterClick = {
                    navController.navigate(Screen.BusinessRegister.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                isBusinessLogin = isBusinessLogin.value,
                onSwitchLoginType = { isBusinessLogin.value = !isBusinessLogin.value }
            )

            LaunchedEffect(authState.value) {
                when (val state = authState.value) {
                    is AuthViewModel.AuthState.Success -> {
                        if (!state.isNewUser) {
                            if (state.userType == "business") {
                                navController.navigate(Screen.BusinessHome.createRoute(state.userId)) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.CustomerHome.createRoute(customerId = state.userId)) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        } else {
                            if (state.userType == "business") {
                                navController.navigate(Screen.BusinessInfo.createRoute(state.userId)) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.CustomerInfo.createRoute(state.userId)) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    }
                    is AuthViewModel.AuthState.Error -> {
                        // Hata durumunda gerekli işlemler yapılabilir
                    }
                    else -> {
                        // Diğer durumlar için gerekli işlemler
                    }
                }
            }
        }

        composable(Screen.CustomerRegister.route) {
            RegisterScreen(
                onRegisterClick = { email, password ->
                    authViewModel.signUp(email, password, false)
                },
                onBackToLoginClick = {
                    navController.navigateUp()
                },
                isBusinessRegister = false
            )

            // Kayıt başarılı olduğunda kullanıcı bilgileri sayfasına yönlendir
            LaunchedEffect(authState.value) {
                when (val state = authState.value) {
                    is AuthViewModel.AuthState.Success -> {
                        Log.d("NavGraph", "Kayıt başarılı, kullanıcı bilgileri sayfasına yönlendiriliyor...")
                        navController.navigate(Screen.CustomerInfo.createRoute(state.userId)) {
                            popUpTo(Screen.CustomerRegister.route) { inclusive = true }
                        }
                    }
                    is AuthViewModel.AuthState.Error -> {
                        Log.e("NavGraph", "Kayıt hatası: ${state.message}")
                    }
                    else -> {}
                }
            }
        }

        composable(Screen.BusinessRegister.route) {
            RegisterScreen(
                onRegisterClick = { email, password ->
                    authViewModel.signUp(email, password, true)
                },
                onBackToLoginClick = {
                    navController.navigateUp()
                },
                isBusinessRegister = true
            )

            // Kayıt başarılı olduğunda işletme bilgileri sayfasına yönlendir
            LaunchedEffect(authState.value) {
                when (val state = authState.value) {
                    is AuthViewModel.AuthState.Success -> {
                        Log.d("NavGraph", "İşletme kaydı başarılı, işletme bilgileri sayfasına yönlendiriliyor...")
                        navController.navigate(Screen.BusinessInfo.createRoute(state.userId)) {
                            popUpTo(Screen.BusinessRegister.route) { inclusive = true }
                        }
                    }
                    is AuthViewModel.AuthState.Error -> {
                        Log.e("NavGraph", "Kayıt hatası: ${state.message}")
                    }
                    else -> {}
                }
            }
        }

        composable(
            route = Screen.CustomerInfo.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            // ViewModel'i bu composable scope'unda tanımlayalım
            val userInfoViewModel: UserInfoViewModel = viewModel()
            val userInfoState = userInfoViewModel.userInfoState.collectAsState()

            // Composable içinde kullanılacak coroutine scope
            val coroutineScope = rememberCoroutineScope()

            CustomerInfoScreen(
                initialName = "",
                initialPhone = "",
                onSaveClick = { name, phone ->
                    Log.d("NavGraph", "CustomerInfoScreen'den onSaveClick çağrıldı")
                    userInfoViewModel.saveCustomerInfo(userId, name, phone)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )

            // State değişimini daha net bir şekilde izleyelim
            DisposableEffect(Unit) {
                val job = coroutineScope.launch {  // viewModelScope yerine coroutineScope
                    userInfoViewModel.userInfoState.collect { state ->
                        Log.d("NavGraph", "UserInfoState değişti: $state")
                        if (state is UserInfoState.Success) {
                            Log.d("NavGraph", "Success state algılandı, ana sayfaya yönlendiriliyor")
                            navController.navigate(Screen.CustomerHome.createRoute(customerId = userId)) {
                                popUpTo(Screen.CustomerInfo.route) { inclusive = true }
                            }
                            userInfoViewModel.resetState()
                        }
                    }
                }

                onDispose {
                    job.cancel()
                }
            }
        }

        composable(
            route = Screen.BusinessInfo.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userInfoState = userInfoViewModel.userInfoState.collectAsState()

            BusinessInfoScreen(
                initialEmail = FirebaseAuth.getInstance().currentUser?.email ?: "",
                onSaveClick = { businessName, address, phone, sector ->
                    userInfoViewModel.saveBusinessInfo(userId, businessName, address, phone, sector)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )

            // İşletme bilgileri başarıyla kaydedildiğinde ana sayfaya yönlendir
            LaunchedEffect(key1 = userInfoState.value) {
                when (val state = userInfoState.value) {
                    is UserInfoState.Success -> {
                        // Başarılı kayıttan sonra ana sayfaya geç
                        navController.navigate(Screen.BusinessHome.createRoute(userId)) {
                            popUpTo(Screen.BusinessInfo.route) { inclusive = true }
                        }

                        // ViewModel durumunu sıfırla
                        userInfoViewModel.resetState()
                    }
                    else -> {}
                }
            }
        }

        composable(
            route = Screen.CustomerHome.route,
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: ""

            val viewModel: CustomerHomeViewModel = viewModel()
            val state = viewModel.uiState.collectAsState()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                if (customerId.isNotBlank()) {
                    viewModel.loadCustomerData(customerId)
                }
            }

            when (val currentState = state.value) {
                is CustomerHomeState.Loading -> {
                    CircularProgressIndicator()
                }
                is CustomerHomeState.Success -> {
                    CustomerHomeScreen(
                        customerName = currentState.customer.fullName,
                        customerId = customerId,
                        onBusinessListClick = {
                            navController.navigate(Screen.BusinessList.route)
                        },
                        onAppointmentsClick = {
                            navController.navigate(Screen.CustomerAppointments.route)
                        },
                        onLogoutClick = {
                            FirebaseAuth.getInstance().signOut()
                            Log.d("NavGraph", "User logged out, redirecting to login screen")
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    )
                }
                is CustomerHomeState.Error -> {
                    Text(currentState.message)
                }
            }
        }

        composable(Screen.BusinessHome.route) {
            val viewModel: BusinessHomeViewModel = viewModel()
            val state = viewModel.uiState.collectAsState()

            // Mevcut kullanıcının ID'sini al ve verileri yükle
            LaunchedEffect(Unit) {
                val currentUser = homeViewModel.getCurrentUser()
                if (currentUser != null) {
                    viewModel.loadBusinessData(currentUser.uid)
                }
            }

            when (val currentState = state.value) {
                is BusinessHomeState.Loading -> {
                    CircularProgressIndicator()
                }
                is BusinessHomeState.Success -> {
                    BusinessHomeScreen(
                        businessName = currentState.business.businessName,
                        pendingAppointments = currentState.pendingAppointments,
                        onWorkingHoursClick = {
                            navController.navigate(Screen.WorkingHours.createRoute(currentState.business.id))
                        },
                        onCalendarClick = {
                            navController.navigate(Screen.BusinessCalendar.createRoute(currentState.business.id))
                        },
                        onRequestsClick = {
                            navController.navigate(Screen.AppointmentRequests.createRoute(currentState.business.id))
                        },
                        onLogoutClick = {
                            // Doğrudan auth'u burada çağıralım ve sign out yapalım
                            FirebaseAuth.getInstance().signOut()
                            Log.d("NavGraph", "User logged out, redirecting to login screen")
                            // Direkt navigasyon yapalım
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    )
                }
                is BusinessHomeState.Error -> {
                    Text(currentState.message)
                }
            }
        }

        composable(
            route = Screen.WorkingHours.route,
            arguments = listOf(navArgument("businessId") { type = NavType.StringType })
        ) { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: return@composable
            val viewModel: BusinessHomeViewModel = viewModel()
            val state = viewModel.uiState.collectAsState()
            val workingHoursState = viewModel.workingHoursState.collectAsState().value

            LaunchedEffect(businessId) {
                viewModel.loadBusinessData(businessId)
            }

            when (val currentState = state.value) {
                is BusinessHomeState.Success -> {
                    val business = currentState.business

                    WorkingHoursScreen(
                        workingDays = business.workingDays,
                        workingHours = business.workingHours,
                        onWorkingDaysChange = { updatedDays ->
                            // Güncellenmiş günleri kaydedin
                            viewModel.updateWorkingDays(updatedDays)
                        },
                        onWorkingHoursChange = { updatedHours ->
                            // Güncellenmiş saatleri kaydedin
                            viewModel.updateWorkingHours(updatedHours)
                        },
                        onSaveClick = {
                            // Burada adı 'saveWorkingHoursAndDays' yerine 'saveWorkingHours' olarak değiştirdik
                            viewModel.saveWorkingHours()
                        },
                        onBackClick = {
                            navController.popBackStack()
                        },
                        isLoading = workingHoursState is BusinessHomeViewModel.WorkingHoursState.Loading,
                        workingHoursState = workingHoursState
                    )
                }
                is BusinessHomeState.Loading -> {
                    CircularProgressIndicator()
                }
                is BusinessHomeState.Error -> {
                    Text(currentState.message)
                }
            }
        }

        composable(Screen.BusinessList.route) {
            val viewModel: BusinessListViewModel = viewModel()
            val state = viewModel.businessListState.collectAsState()

            // Buraya LaunchedEffect ekleyelim - dil değişimi sonrası yeniden yükleme için
            LaunchedEffect(Unit) {
                viewModel.loadBusinesses()
            }

            when (val currentState = state.value) {
                is BusinessListState.Loading -> {
                    CircularProgressIndicator()
                }
                is BusinessListState.Success -> {
                    BusinessListScreen(
                        businesses = currentState.businesses,
                        sectors = currentState.sectors,
                        onBusinessClick = { businessId ->
                            navController.navigate(Screen.BusinessDetail.createRoute(businessId))
                        },
                        onBackClick = {
                            navController.navigateUp()
                        },
                        navController = navController
                    )
                }
                is BusinessListState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(currentState.message)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadBusinesses() }) {
                            Text(stringResource(id = R.string.try_again))
                        }
                    }
                }
            }
        }

        composable(
            route = Screen.BusinessDetail.route,
            arguments = listOf(navArgument("businessId") { type = NavType.StringType })
        ) { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: return@composable
            val viewModel: BusinessDetailViewModel = viewModel()
            val uiState = viewModel.uiState.collectAsState().value

            // Coroutine scope'u burada oluştur (composable içinde)
            val coroutineScope = rememberCoroutineScope()

            // İşletme detaylarını yükle
            LaunchedEffect(businessId) {
                viewModel.loadBusinessDetails(businessId)
            }

            when (uiState) {
                is BusinessDetailViewModel.BusinessDetailState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is BusinessDetailViewModel.BusinessDetailState.Success -> {
                    val business = uiState.business
                    val availableSlots = uiState.availableSlots
                    val selectedDate = uiState.selectedDate

                    BusinessDetailScreen(
                        business = business,
                        availableSlots = availableSlots,
                        selectedDate = selectedDate,
                        onDateSelect = { date -> viewModel.updateSelectedDate(date) },
                        onTimeSelect = { time -> viewModel.updateSelectedTime(time) },
                        onNoteChange = { note -> viewModel.updateNote(note) },
                        onAppointmentRequest = {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser != null) {
                                Log.d("NavGraph", "Randevu talebi oluşturuluyor, kullanıcı: ${currentUser.uid}")

                                coroutineScope.launch {
                                    try {
                                        val appointmentId = viewModel.createAppointment(
                                            businessId = businessId,
                                            customerId = currentUser.uid,
                                            selectedDate = selectedDate,
                                            selectedTime = uiState.selectedTime ?: ""
                                        )
                                        Log.d("NavGraph", "Randevu başarıyla oluşturuldu, ID: $appointmentId")

                                        navController.navigate(Screen.AppointmentConfirmation.route) {
                                            popUpTo(Screen.BusinessList.route)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("NavGraph", "Randevu oluşturma hatası", e)
                                    }
                                }
                            } else {
                                Log.e("NavGraph", "Kullanıcı oturumu bulunamadı")
                            }
                        },
                        onBackClick = { navController.popBackStack() },
                        isLoading = false,
                        viewModel = viewModel,
                        customerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    )
                }

                is BusinessDetailViewModel.BusinessDetailState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.loadBusinessDetails(businessId)
                            }
                        ) {
                            Text(stringResource(id = R.string.try_again))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {
                            Text(stringResource(id = R.string.back))
                        }
                    }
                }
            }
        }

        composable(
            route = Screen.BusinessCalendar.route,
            arguments = listOf(navArgument("businessId") { type = NavType.StringType })
        ) { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: return@composable
            val viewModel: BusinessHomeViewModel = viewModel()
            val state = viewModel.calendarState.collectAsState()

            LaunchedEffect(businessId) {
                viewModel.loadBusinessData(businessId)
                viewModel.loadAppointments(LocalDate.now())
            }

            when (val currentState = state.value) {
                is CalendarState.Loading -> {
                    CircularProgressIndicator()
                }
                is CalendarState.Success -> {
                    BusinessCalendarScreen(
                        selectedDate = currentState.selectedDate,
                        appointments = currentState.appointments,
                        availableTimeSlots = currentState.availableTimeSlots,
                        onDateSelect = { date ->
                            viewModel.loadAppointments(date)
                        },
                        onAppointmentStatusChange = { id, status ->
                            viewModel.updateAppointmentStatus(id, status)
                        },
                        onTimeSlotBlock = { timeSlot ->
                            viewModel.blockTimeSlot(currentState.selectedDate, timeSlot)
                        },
                        onTimeSlotUnblock = { appointmentId ->
                            viewModel.unblockTimeSlot(appointmentId)
                        },
                        onCancelAndBlock = { appointmentId ->
                            viewModel.cancelAppointment(appointmentId)
                        },
                        onBackClick = {
                            navController.navigateUp()
                        }
                    )
                }
                is CalendarState.Error -> {
                    Text(currentState.message)
                }
            }
        }

        composable(
            route = Screen.AppointmentRequests.route,
            arguments = listOf(navArgument("businessId") { type = NavType.StringType })
        ) { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: return@composable
            val viewModel: BusinessHomeViewModel = viewModel()
            val state = viewModel.appointmentRequestsState.collectAsState()

            LaunchedEffect(businessId) {
                viewModel.loadAppointmentRequests(businessId)
            }

            when (val currentState = state.value) {
                is BusinessHomeViewModel.AppointmentRequestsState.Loading -> {
                    CircularProgressIndicator()
                }
                is BusinessHomeViewModel.AppointmentRequestsState.Success -> {
                    AppointmentRequestsScreen(
                        appointments = currentState.appointments,
                        onStatusChange = { id, status ->
                            viewModel.updateAppointmentStatus(id, status)
                        },
                        onBackClick = {
                            navController.navigateUp()
                        }
                    )
                }
                is BusinessHomeViewModel.AppointmentRequestsState.Error -> {
                    Text(currentState.message)
                }
            }
        }

        composable(Screen.CustomerAppointments.route) {
            val viewModel: CustomerHomeViewModel = viewModel()
            val state = viewModel.appointmentsState.collectAsState()

            LaunchedEffect(Unit) {
                val currentUser = homeViewModel.getCurrentUser()
                if (currentUser != null) {
                    viewModel.loadCustomerAppointments(currentUser.uid)
                }
            }

            when (val currentState = state.value) {
                is AppointmentsState.Loading -> {
                    CircularProgressIndicator()
                }
                is AppointmentsState.Success -> {
                    CustomerAppointmentsScreen(
                        appointments = currentState.appointments,
                        onCancelAppointment = { appointmentId ->
                            viewModel.cancelAppointment(appointmentId)
                        },
                        onBackClick = {
                            navController.navigateUp()
                        }
                    )
                }
                is AppointmentsState.Error -> {
                    Text(currentState.message)
                }
            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLanguageClick = { navController.navigate(Screen.LanguageSettings.route) },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.LanguageSettings.route) {
            val context = LocalContext.current
            LanguageSettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLanguageChanged = {
                    // Dil değiştirildiğinde aktiviteyi yeniden başlat
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(intent)
                }
            )
        }

        composable(route = Screen.AppointmentConfirmation.route) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.appointment_confirmation_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.appointment_confirmation_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val customerId = currentUser?.uid ?: ""
                        navController.navigate(Screen.CustomerHome.createRoute(customerId = customerId)) {
                            popUpTo(Screen.CustomerHome.createRoute(customerId)) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(stringResource(id = R.string.go_back_to_home))
                }
            }
        }
    }
}