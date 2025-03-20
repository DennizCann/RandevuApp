package com.denizcan.randevuapp.navigation

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
import com.denizcan.randevuapp.viewmodel.AuthViewModel.AuthState
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

sealed class Screen(val route: String) {
    object CustomerLogin : Screen("customer_login")
    object BusinessLogin : Screen("business_login")
    object CustomerRegister : Screen("customer_register")
    object BusinessRegister : Screen("business_register")
    object CustomerHome : Screen("customer_home")
    object BusinessHome : Screen("business_home")
    object CustomerInfo : Screen("customer_info")
    object BusinessInfo : Screen("business_info")
    object BusinessList : Screen("business_list")
    object BusinessDetail : Screen("business_detail")
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
}

@Composable
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val userInfoViewModel: UserInfoViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()

    // AuthState'i dinle
    LaunchedEffect(authViewModel.authState) {
        authViewModel.authState.collect { state ->
            when (state) {
                is AuthState.Success -> {
                    if (state.isNewUser) {
                        // Yeni kullanıcı ise bilgi formuna yönlendir
                        when (state.userType) {
                            "customer" -> {
                                navController.navigate(Screen.CustomerInfo.route + "/${state.userId}") {
                                    popUpTo(Screen.CustomerLogin.route) { inclusive = true }
                                }
                            }
                            "business" -> {
                                navController.navigate(Screen.BusinessInfo.route + "/${state.userId}") {
                                    popUpTo(Screen.BusinessLogin.route) { inclusive = true }
                                }
                            }
                        }
                    } else {
                        // Mevcut kullanıcı ise direkt ana sayfaya yönlendir
                        when (state.userType) {
                            "customer" -> {
                                navController.navigate(Screen.CustomerHome.route) {
                                    popUpTo(Screen.CustomerLogin.route) { inclusive = true }
                                }
                            }
                            "business" -> {
                                navController.navigate(Screen.BusinessHome.route) {
                                    popUpTo(Screen.BusinessLogin.route) { inclusive = true }
                                }
                            }
                        }
                    }
                }
                else -> {} // Diğer durumlar için şimdilik bir şey yapmıyoruz
            }
        }
    }

    // UserInfoState'i dinle
    LaunchedEffect(userInfoViewModel.userInfoState) {
        userInfoViewModel.userInfoState.collect { state ->
            when (state) {
                is UserInfoState.Success -> {
                    // Kullanıcı tipine göre ana sayfaya yönlendir
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    when {
                        currentRoute?.startsWith(Screen.CustomerInfo.route) == true -> {
                            navController.navigate(Screen.CustomerHome.route) {
                                popUpTo(Screen.CustomerInfo.route + "/{userId}") { inclusive = true }
                            }
                        }
                        currentRoute?.startsWith(Screen.BusinessInfo.route) == true -> {
                            navController.navigate(Screen.BusinessHome.route) {
                                popUpTo(Screen.BusinessInfo.route + "/{userId}") { inclusive = true }
                            }
                        }
                    }
                }
                else -> {} // Diğer durumlar için şimdilik bir şey yapmıyoruz
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.CustomerLogin.route
    ) {
        composable(Screen.CustomerLogin.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.signIn(email, password, false)
                },
                onRegisterClick = {
                    navController.navigate(Screen.CustomerRegister.route)
                },
                isBusinessLogin = false,
                onSwitchLoginType = {
                    navController.navigate(Screen.BusinessLogin.route) {
                        popUpTo(Screen.CustomerLogin.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.BusinessLogin.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.signIn(email, password, true)
                },
                onRegisterClick = {
                    navController.navigate(Screen.BusinessRegister.route)
                },
                isBusinessLogin = true,
                onSwitchLoginType = {
                    navController.navigate(Screen.CustomerLogin.route) {
                        popUpTo(Screen.BusinessLogin.route) { inclusive = true }
                    }
                }
            )
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
        }

        composable(
            route = Screen.CustomerInfo.route + "/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            CustomerInfoScreen(
                onSaveClick = { fullName, phone ->
                    userInfoViewModel.saveCustomerInfo(userId, fullName, phone)
                },
                userId = userId
            )
        }

        composable(
            route = Screen.BusinessInfo.route + "/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            BusinessInfoScreen(
                onSaveClick = { businessName, address, phone, sector ->
                    userInfoViewModel.saveBusinessInfo(userId, businessName, address, phone, sector)
                },
                userId = userId
            )
        }

        composable(Screen.CustomerHome.route) {
            val viewModel: CustomerHomeViewModel = viewModel()
            val state = viewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                val currentUser = homeViewModel.getCurrentUser()
                if (currentUser != null) {
                    viewModel.loadCustomerData(currentUser.uid)
                }
            }

            when (val currentState = state.value) {
                is CustomerHomeState.Loading -> {
                    CircularProgressIndicator()
                }
                is CustomerHomeState.Success -> {
                    CustomerHomeScreen(
                        customerName = currentState.customer.fullName,
                        onBusinessListClick = {
                            navController.navigate(Screen.BusinessList.route)
                        },
                        onAppointmentsClick = {
                            navController.navigate(Screen.CustomerAppointments.route)
                        },
                        onLogoutClick = {
                            homeViewModel.signOut()
                            navController.navigate(Screen.CustomerLogin.route) {
                                popUpTo(Screen.CustomerHome.route) { inclusive = true }
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
                            homeViewModel.signOut()
                            navController.navigate(Screen.BusinessLogin.route) {
                                popUpTo(Screen.BusinessHome.route) { inclusive = true }
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

            LaunchedEffect(businessId) {
                viewModel.loadBusinessData(businessId)
            }

            when (val currentState = state.value) {
                is BusinessHomeState.Success -> {
                    WorkingHoursScreen(
                        workingDays = currentState.business.workingDays,
                        workingHours = currentState.business.workingHours,
                        onSaveClick = { days, hours ->
                            viewModel.updateWorkingHours(days, hours)
                            navController.navigateUp()
                        },
                        onBackClick = {
                            navController.navigateUp()
                        }
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

            when (val currentState = state.value) {
                is BusinessListState.Loading -> {
                    // Loading UI
                    CircularProgressIndicator()
                }
                is BusinessListState.Success -> {
                    BusinessListScreen(
                        businesses = currentState.businesses,
                        sectors = currentState.sectors,
                        onBusinessClick = { businessId ->
                            navController.navigate(Screen.BusinessDetail.route + "/${businessId}")
                        },
                        onBackClick = {
                            navController.navigateUp()
                        }
                    )
                }
                is BusinessListState.Error -> {
                    // Error UI
                    Text(currentState.message)
                }
            }
        }

        composable(
            route = Screen.BusinessDetail.route + "/{businessId}",
            arguments = listOf(navArgument("businessId") { type = NavType.StringType })
        ) { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
            val viewModel: BusinessDetailViewModel = viewModel()
            val state = viewModel.uiState.collectAsState()
            val currentUser = homeViewModel.getCurrentUser()

            LaunchedEffect(businessId) {
                viewModel.loadBusinessDetail(businessId)
                viewModel.loadAvailableSlots(LocalDate.now())
            }

            when (val currentState = state.value) {
                is BusinessDetailState.Loading -> {
                    CircularProgressIndicator()
                }
                is BusinessDetailState.Success -> {
                    BusinessDetailScreen(
                        business = currentState.business,
                        availableSlots = currentState.availableSlots,
                        selectedDate = currentState.selectedDate,
                        onDateSelect = { date ->
                            viewModel.updateSelectedDate(date)
                        },
                        onTimeSelect = { time ->
                            viewModel.updateSelectedTime(time)
                        },
                        onAppointmentRequest = {
                            currentUser?.let { user ->
                                viewModel.createAppointment(user.uid)
                                // Başarılı olduğunda randevularım sayfasına git
                                navController.navigate(Screen.CustomerAppointments.route) {
                                    popUpTo(Screen.BusinessDetail.route + "/{businessId}") { inclusive = true }
                                }
                            }
                        },
                        onBackClick = {
                            navController.navigateUp()
                        },
                        isLoading = false
                    )
                }
                is BusinessDetailState.Error -> {
                    Text(currentState.message)
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
                        onDateSelect = { date ->
                            viewModel.loadAppointments(date)
                        },
                        onAppointmentStatusChange = { id, status ->
                            viewModel.updateAppointmentStatus(id, status)
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
    }
} 