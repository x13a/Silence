package me.lucky.silence.ui

import android.content.Context
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import me.lucky.silence.Preferences
import me.lucky.silence.ui.common.Animation
import me.lucky.silence.ui.common.Route

@Composable
fun App(ctx: Context, navController: NavHostController) {
    val prefs = Preferences(ctx)
    NavHost(navController = navController, startDestination = Route.MODULES,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = Animation.slideInEffect,
                initialOffset = Animation.offsetFunc,
            ) + fadeIn(animationSpec = Animation.fadeInEffect)
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = Animation.slideOutEffect,
                targetOffset = Animation.offsetFunc,
            ) + fadeOut(animationSpec = Animation.fadeOutEffect)
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = Animation.slideInEffect,
                initialOffset = Animation.offsetFunc,
            ) + fadeIn(animationSpec = Animation.fadeInEffect)
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = Animation.slideOutEffect,
                targetOffset = Animation.offsetFunc,
            ) + fadeOut(animationSpec = Animation.fadeOutEffect)
        }) {
        composable(Route.MODULES) {
            MainScreen(ctx = ctx,
                prefs = prefs,
                onNavigateToContacted = { navController.navigate(Route.CONTACTED) },
                onNavigateToGroups = { navController.navigate(Route.GROUPS) },
                onNavigateToRepeated = { navController.navigate(Route.REPEATED) },
                onNavigateToMessages = { navController.navigate(Route.MESSAGES) },
                onNavigateToSim = { navController.navigate(Route.SIM) },
                onNavigateToExtra = { navController.navigate(Route.EXTRA) },
                onNavigateToSettings = { navController.navigate(Route.SETTINGS) },
                onNavigateToRegex = { navController.navigate(Route.REGEX) },
            )
        }
        composable(Route.CONTACTED) { ContactedScreen(prefs) { navController.popBackStack() } }
        composable(Route.GROUPS) { GroupsScreen(prefs) { navController.popBackStack() } }
        composable(Route.REPEATED) { RepeatedScreen(prefs) { navController.popBackStack() } }
        composable(Route.MESSAGES) { MessagesScreen(prefs) { navController.popBackStack() } }
        composable(Route.SIM) { SimScreen(prefs) { navController.popBackStack() } }
        composable(Route.EXTRA) { ExtraScreen(prefs) { navController.popBackStack() } }
        composable(Route.SETTINGS) { SettingsScreen(ctx, prefs) { navController.popBackStack() } }
        composable(Route.REGEX) { RegexScreen(prefs) { navController.popBackStack() } }
    }
}