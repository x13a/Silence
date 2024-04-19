package me.lucky.silence.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import me.lucky.silence.Contact
import me.lucky.silence.Message
import me.lucky.silence.Modem
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.Utils
import me.lucky.silence.ui.common.ClickablePreference
import me.lucky.silence.ui.common.ClickableSwitchPreference
import me.lucky.silence.ui.common.Route
import me.lucky.silence.ui.common.SwitchPreference
import me.lucky.silence.ui.common.ToggleableButton

@Composable
fun ModuleList(modules: List<Module>) {
    LazyColumn {
        items(modules) { module ->
            if ((module.getPreference != null) && (module.setPreference != null) && (module.navigation != null)) {
                ClickableSwitchPreference(name = stringResource(module.name),
                    description = stringResource(module.description),
                    getIsEnabled = module.getPreference,
                    setIsEnabled = module.setPreference,
                    onModuleClick = { module.navigation.invoke() })
            } else if (module.getPreference != null && module.setPreference != null && module.navigation == null) {
                SwitchPreference(
                    name = stringResource(module.name),
                    description = stringResource(module.description),
                    getIsEnabled = module.getPreference,
                    setIsEnabled = module.setPreference,
                )
            } else if (module.navigation != null) {
                ClickablePreference(name = stringResource(module.name),
                    description = stringResource(module.description),
                    onModuleClick = { module.navigation.invoke() })
            }
        }
    }
}

data class Module(
    val name: Int,
    val description: Int,
    val getPreference: (() -> Boolean)? = null,
    val setPreference: ((Boolean) -> Unit)? = null,
    val navigation: (() -> Unit)? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    ctx: Context,
    prefs: Preferences,
    onNavigateToContacted: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToRepeated: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToSim: () -> Unit,
    onNavigateToExtra: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRegex: () -> Unit,
) {
    fun getContactedPermissions(): Array<String> {
        val contacted = prefs.contacted
        val permissions = mutableSetOf<String>()
        for (value in Contact.entries.asSequence().filter { contacted.and(it.value) != 0 }) {
            when (value) {
                Contact.CALL -> permissions.add(Manifest.permission.READ_CALL_LOG)
                Contact.MESSAGE -> permissions.add(Manifest.permission.READ_SMS)
                Contact.ANSWER -> permissions.add(Manifest.permission.READ_CALL_LOG)
            }
        }
        return permissions.toTypedArray()
    }

    fun getMessagesPermissions(): Array<String> {
        val messages = prefs.messages
        val permissions = mutableSetOf<String>()
        for (value in Message.entries.asSequence().filter { messages.and(it.value) != 0 }) {
            when (value) {
                Message.INBOX -> permissions.add(Manifest.permission.READ_SMS)
                Message.TEXT -> {}
            }
        }
        return permissions.toTypedArray()
    }

    val registerForContactedPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    fun requestContactedPermissions() =
        registerForContactedPermissions.launch(getContactedPermissions())

    val registerForRepeatedPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun requestRepeatedPermissions() =
        registerForRepeatedPermissions.launch(Manifest.permission.READ_CALL_LOG)

    val registerForMessagesPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    fun requestMessagesPermissions() =
        registerForMessagesPermissions.launch(getMessagesPermissions())

    val roleManager: RoleManager by lazy { ctx.getSystemService(RoleManager::class.java) }
    val registerForCallScreeningRole =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    fun requestCallScreeningRole() =
        registerForCallScreeningRole
            .launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))

    val modules = listOf(
        Module(
            name = R.string.groups_main,
            description = R.string.groups_description,
            getPreference = { prefs.isGroupsChecked },
            setPreference = { prefs.isGroupsChecked = it },
            navigation = onNavigateToGroups,
        ),
        Module(
            name = R.string.repeated_main,
            description = R.string.repeated_description,
            getPreference = { prefs.isRepeatedChecked },
            setPreference = { isChecked ->
                prefs.isRepeatedChecked = isChecked
                if (isChecked) requestRepeatedPermissions()
            },
            navigation = onNavigateToRepeated,
        ),
        Module(
            name = R.string.messages_main,
            description = R.string.messages_description,
            getPreference = { prefs.isMessagesChecked },
            setPreference = { isChecked ->
                prefs.isMessagesChecked = isChecked
                if (isChecked) requestMessagesPermissions()
                Utils.updateMessagesTextEnabled(ctx)
            },
            navigation = onNavigateToMessages,
        ),
        Module(
            name = R.string.regex_main,
            description = R.string.regex_description,
            navigation = onNavigateToRegex,
        ),
        *(if (Utils.getModemCount(ctx, Modem.SUPPORTED) >= 1) {
            arrayOf(
                Module(
                    name = R.string.sim,
                    description = R.string.sim_description,
                    navigation = onNavigateToSim,
                ),
            )
        } else {
            emptyArray()
        }),
        Module(
            name = R.string.contacted_main,
            description = R.string.contacted_description,
            navigation = onNavigateToContacted,
        ),
        Module(
            name = R.string.extra,
            description = R.string.extra_description,
            navigation = onNavigateToExtra,
        ),
        Module(
            name = R.string.block_main,
            description = R.string.block_description,
            getPreference = { prefs.isBlockEnabled },
            setPreference = { prefs.isBlockEnabled = it },
        ),
    )
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = stringResource(R.string.app_name)) }, actions = {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_settings_24),
                    contentDescription = stringResource(R.string.settings)
                )
            }
        })
    }, content = { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            ModuleList(modules)
            Spacer(modifier = Modifier.weight(1f))
            ToggleableButton(
                getPreference = { prefs.isEnabled },
                setPreference = { isChecked ->
                    prefs.isEnabled = isChecked
                    if (isChecked) requestCallScreeningRole()
                    Utils.updateMessagesTextEnabled(ctx)
                }
            )
        }
    })
}

@Preview
@Composable
fun ModuleScreenPreview() {
    val navController = rememberNavController()
    MainScreen(
        ctx = LocalContext.current,
        prefs = Preferences(LocalContext.current),
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