package com.saicomputer.sms.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.saicomputer.sms.core.ui.ProvideUserMenuActions
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.UserMenuActions
import com.saicomputer.sms.data.model.UserRole
import com.saicomputer.sms.feature.audit.AuditScreen
import com.saicomputer.sms.feature.auth.ChangePasswordScreen
import com.saicomputer.sms.feature.auth.LoginScreen
import com.saicomputer.sms.feature.auth.UserProfileScreen
import com.saicomputer.sms.feature.certificates.CertificatesListScreen
import com.saicomputer.sms.feature.courses.CourseDetailScreen
import com.saicomputer.sms.feature.courses.CourseFormScreen
import com.saicomputer.sms.feature.courses.CoursesListScreen
import com.saicomputer.sms.feature.dashboard.DashboardScreen
import com.saicomputer.sms.feature.enrollments.EnrollmentDetailScreen
import com.saicomputer.sms.feature.enrollments.EnrollmentWizardScreen
import com.saicomputer.sms.feature.enrollments.EnrollmentsListScreen
import com.saicomputer.sms.feature.exports.ExportsScreen
import com.saicomputer.sms.feature.payments.PaymentFormScreen
import com.saicomputer.sms.feature.payments.PaymentsListScreen
import com.saicomputer.sms.feature.receipts.ReceiptsListScreen
import com.saicomputer.sms.feature.settings.InstituteSettingsScreen
import com.saicomputer.sms.feature.settings.UserManagementScreen
import com.saicomputer.sms.feature.students.StudentDetailScreen
import com.saicomputer.sms.feature.students.StudentFormScreen
import com.saicomputer.sms.feature.students.StudentsListScreen
import com.saicomputer.sms.feature.subscriptions.SubscriptionsListScreen

@Composable
fun SmsNavHost(
    navController: NavHostController,
    snackbarController: SnackbarController,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val bootstrap by appViewModel.bootstrap.collectAsStateWithLifecycle()
    val currentUser by appViewModel.currentUser.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route.orEmpty()

    if (bootstrap.loading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val user = currentUser
    val startDestination = when {
        user == null -> Screen.Login.route
        user.mustChangePassword -> Screen.ChangePassword.route
        user.role == UserRole.Receptionist -> Screen.Students.route
        else -> Screen.Dashboard.route
    }

    val logout: () -> Unit = {
        appViewModel.logout {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val motion = tween<Float>(280)
    val slideMotion = tween<IntOffset>(280)

    val navHost: @Composable () -> Unit = {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideInHorizontally(slideMotion) { full -> full / 5 } + fadeIn(motion)
            },
            exitTransition = {
                slideOutHorizontally(slideMotion) { full -> -full / 8 } + fadeOut(motion)
            },
            popEnterTransition = {
                slideInHorizontally(slideMotion) { full -> -full / 8 } + fadeIn(motion)
            },
            popExitTransition = {
                slideOutHorizontally(slideMotion) { full -> full / 5 } + fadeOut(motion)
            }
        ) {
            composable(Screen.Login.route) {
                LoginScreen(onLoggedIn = { loggedInUser ->
                    val dest = when {
                        loggedInUser.mustChangePassword -> Screen.ChangePassword.route
                        loggedInUser.role == UserRole.Receptionist -> Screen.Students.route
                        else -> Screen.Dashboard.route
                    }
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.ChangePassword.route) {
                val mustChange = appViewModel.currentUser.value?.mustChangePassword == true
                ChangePasswordScreen(
                    forced = mustChange,
                    onBack = if (!mustChange) {{ navController.popBackStack() }} else null,
                    onChanged = {
                        if (mustChange) {
                            val cur = appViewModel.currentUser.value
                            val dest = if (cur?.role == UserRole.Receptionist) {
                                Screen.Students.route
                            } else {
                                Screen.Dashboard.route
                            }
                            navController.navigate(dest) {
                                popUpTo(Screen.ChangePassword.route) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(Screen.Profile.route) {
                UserProfileScreen(
                    onBack = { navController.popBackStack() },
                    onChangePassword = { navController.navigate(Screen.ChangePassword.route) }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    user = currentUser,
                    onOpenStudent = { id -> navController.navigate(Screen.StudentDetail.create(id)) }
                )
            }

            composable(Screen.Students.route) {
                StudentsListScreen(
                    user = currentUser,
                    onOpenStudent = { id -> navController.navigate(Screen.StudentDetail.create(id)) },
                    onNewStudent = { navController.navigate(Screen.StudentNew.route) }
                )
            }

            composable(Screen.More.route) {
                MoreScreen(
                    user = currentUser,
                    onNavigate = { navController.navigate(it) }
                )
            }

            composable(Screen.StudentNew.route) {
                StudentFormScreen(
                    studentId = null,
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.popBackStack()
                        navController.navigate(Screen.StudentDetail.create(id))
                    },
                    snackbarController = snackbarController
                )
            }

            composable(
                Screen.StudentDetail.route,
                arguments = listOf(navArgument(Screen.ARG_ID) { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString(Screen.ARG_ID).orEmpty()
                StudentDetailScreen(
                    studentId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Screen.StudentEdit.create(id)) },
                    onNewEnrollment = { navController.navigate(Screen.EnrollmentNew.create(id)) },
                    onOpenEnrollment = { eid -> navController.navigate(Screen.EnrollmentDetail.create(eid)) },
                    onRecordPayment = { eid -> navController.navigate(Screen.PaymentNew.create(eid)) },
                    snackbarController = snackbarController
                )
            }

            composable(
                Screen.StudentEdit.route,
                arguments = listOf(navArgument(Screen.ARG_ID) { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString(Screen.ARG_ID).orEmpty()
                StudentFormScreen(
                    studentId = id,
                    onBack = { navController.popBackStack() },
                    onSaved = { _ -> navController.popBackStack() },
                    snackbarController = snackbarController
                )
            }

            composable(Screen.Search.route) {
                StudentsListScreen(
                    user = currentUser,
                    onOpenStudent = { id -> navController.navigate(Screen.StudentDetail.create(id)) },
                    onNewStudent = { navController.navigate(Screen.StudentNew.route) }
                )
            }

            composable(Screen.Courses.route) {
                CoursesListScreen(
                    user = currentUser,
                    onBack = { navController.popBackStack() },
                    onNewCourse = { navController.navigate(Screen.CourseNew.route) },
                    onOpenCourse = { id -> navController.navigate(Screen.CourseDetail.create(id)) }
                )
            }
            composable(
                Screen.CourseDetail.route,
                arguments = listOf(navArgument(Screen.ARG_ID) { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString(Screen.ARG_ID).orEmpty()
                CourseDetailScreen(
                    courseId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Screen.CourseEdit.create(id)) }
                )
            }
            composable(Screen.CourseNew.route) {
                CourseFormScreen(
                    courseId = null,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    snackbarController = snackbarController
                )
            }
            composable(
                Screen.CourseEdit.route,
                arguments = listOf(navArgument(Screen.ARG_ID) { type = NavType.StringType })
            ) { entry ->
                CourseFormScreen(
                    courseId = entry.arguments?.getString(Screen.ARG_ID),
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    snackbarController = snackbarController
                )
            }

            composable(Screen.Enrollments.route) {
                EnrollmentsListScreen(
                    user = currentUser,
                    onNewEnrollment = { navController.navigate(Screen.EnrollmentNew.create()) },
                    onOpenEnrollment = { eid -> navController.navigate(Screen.EnrollmentDetail.create(eid)) }
                )
            }

            composable(
                Screen.EnrollmentNew.route,
                arguments = listOf(navArgument(Screen.ARG_STUDENT_ID) {
                    type = NavType.StringType; nullable = true; defaultValue = null
                })
            ) { entry ->
                val studentId = entry.arguments?.getString(Screen.ARG_STUDENT_ID)?.takeIf { it.isNotBlank() }
                EnrollmentWizardScreen(
                    studentId = studentId,
                    onBack = { navController.popBackStack() },
                    onCreated = { eid ->
                        navController.popBackStack()
                        navController.navigate(Screen.EnrollmentDetail.create(eid))
                    },
                    onCreateStudent = { navController.navigate(Screen.StudentNew.route) },
                    snackbarController = snackbarController
                )
            }
            composable(
                Screen.EnrollmentDetail.route,
                arguments = listOf(navArgument(Screen.ARG_ID) { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString(Screen.ARG_ID).orEmpty()
                EnrollmentDetailScreen(
                    enrollmentId = id,
                    onBack = { navController.popBackStack() },
                    onRecordPayment = { eid -> navController.navigate(Screen.PaymentNew.create(eid)) },
                    snackbarController = snackbarController
                )
            }

            composable(
                Screen.PaymentNew.route,
                arguments = listOf(navArgument(Screen.ARG_ENROLLMENT_ID) {
                    type = NavType.StringType; nullable = true; defaultValue = null
                })
            ) { entry ->
                PaymentFormScreen(
                    enrollmentId = entry.arguments?.getString(Screen.ARG_ENROLLMENT_ID).orEmpty(),
                    onBack = { navController.popBackStack() },
                    onRecorded = { navController.popBackStack() },
                    snackbarController = snackbarController
                )
            }

            composable(Screen.Subscriptions.route) {
                SubscriptionsListScreen(
                    onOpenEnrollment = { eid -> navController.navigate(Screen.EnrollmentDetail.create(eid)) }
                )
            }

            composable(Screen.Receipts.route) {
                ReceiptsListScreen(
                    user = currentUser,
                    onBack = { navController.popBackStack() },
                    snackbarController = snackbarController
                )
            }
            composable(Screen.Payments.route) {
                PaymentsListScreen(
                    user = currentUser,
                    snackbarController = snackbarController
                )
            }
            composable(Screen.Certificates.route) {
                CertificatesListScreen(
                    user = currentUser,
                    onBack = { navController.popBackStack() },
                    snackbarController = snackbarController
                )
            }

            composable(Screen.Audit.route) {
                AuditScreen(
                    user = currentUser,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                InstituteSettingsScreen(
                    user = currentUser,
                    onBack = { navController.popBackStack() },
                    onOpenUsers = { navController.navigate(Screen.Users.route) },
                    snackbarController = snackbarController
                )
            }
            composable(Screen.Users.route) {
                UserManagementScreen(
                    onBack = { navController.popBackStack() },
                    snackbarController = snackbarController
                )
            }
            composable(Screen.Exports.route) {
                ExportsScreen(
                    user = currentUser,
                    onBack = { navController.popBackStack() },
                    snackbarController = snackbarController
                )
            }
        }
    }

    val shellContent: @Composable () -> Unit = {
        if (shouldShowBottomBar(currentRoute, user)) {
            MainShell(
                navController = navController,
                user = currentUser,
                currentRoute = currentRoute,
                showBottomBar = true
            ) {
                Box(modifier = modifier.fillMaxSize()) {
                    navHost()
                }
            }
        } else {
            Box(modifier = modifier.fillMaxSize()) {
                navHost()
            }
        }
    }

    if (user != null) {
        ProvideUserMenuActions(
            actions = UserMenuActions(
                onProfile = { navController.navigate(Screen.Profile.route) },
                onLogout = logout
            ),
            content = shellContent
        )
    } else {
        shellContent()
    }
}
