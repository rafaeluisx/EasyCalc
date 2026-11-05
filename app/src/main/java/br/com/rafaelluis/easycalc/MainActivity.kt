package br.com.rafaelluis.easycalc

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.view.WindowCompat
import java.util.Locale

private enum class AppScreen { HOME, BMI, SETTINGS }
private enum class AppTheme { LIGHT, DARK, SYSTEM }
private enum class AppLanguage { PORTUGUESE_BRAZIL, ENGLISH, SYSTEM }
private enum class MeasurementUnit { METRIC, IMPERIAL }

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { EasyCalcApp() }
    }
}

@Composable
private fun EasyCalcApp() {
    val context = LocalContext.current
    val preferences = context.getSharedPreferences("easy_calc_preferences", 0)
    var selectedTheme by rememberSaveable {
        mutableStateOf(AppTheme.entries.firstOrNull { it.name == preferences.getString("theme", "SYSTEM") } ?: AppTheme.SYSTEM)
    }
    var selectedUnit by rememberSaveable {
        mutableStateOf(MeasurementUnit.entries.firstOrNull { it.name == preferences.getString("unit", "METRIC") } ?: MeasurementUnit.METRIC)
    }
    val selectedLanguage = remember {
        val locale = AppCompatDelegate.getApplicationLocales().get(0)
        when {
            locale == null -> AppLanguage.SYSTEM
            locale.language == "pt" && locale.country.equals("BR", ignoreCase = true) -> AppLanguage.PORTUGUESE_BRAZIL
            else -> AppLanguage.ENGLISH
        }
    }
    val isDark = when (selectedTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = if (isDark) darkColors else lightColors
    var screen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    BackHandler(enabled = screen != AppScreen.HOME) { screen = AppScreen.HOME }
    val view = LocalView.current
    SideEffect {
        val window = (view.context as android.app.Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(colorScheme = colors) {
        Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
            when (screen) {
                AppScreen.HOME -> HomeScreen(
                    onOpenBmi = { screen = AppScreen.BMI },
                    onOpenSettings = { screen = AppScreen.SETTINGS }
                )
                AppScreen.BMI -> BmiScreen(unit = selectedUnit, onBack = { screen = AppScreen.HOME })
                AppScreen.SETTINGS -> SettingsScreen(
                    selectedTheme = selectedTheme,
                    selectedLanguage = selectedLanguage,
                    selectedUnit = selectedUnit,
                    onThemeSelected = { theme ->
                        selectedTheme = theme
                        preferences.edit().putString("theme", theme.name).apply()
                    },
                    onLanguageSelected = { language ->
                        val locales = when (language) {
                            AppLanguage.PORTUGUESE_BRAZIL -> LocaleListCompat.forLanguageTags("pt-BR")
                            AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
                            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
                        }
                        AppCompatDelegate.setApplicationLocales(locales)
                    },
                    onUnitSelected = { unit ->
                        selectedUnit = unit
                        preferences.edit().putString("unit", unit.name).apply()
                    },
                    onBack = { screen = AppScreen.HOME }
                )
            }
        }
    }
}

private val lightColors = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF1769AA), onPrimary = Color.White, primaryContainer = Color(0xFFD6E8FF),
    onPrimaryContainer = Color(0xFF082F57), secondary = Color(0xFF4B6078),
    background = Color(0xFFF5F8FC), surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEAF0F7), onSurface = Color(0xFF172B3E),
    onSurfaceVariant = Color(0xFF5D6F80), outline = Color(0xFFCAD5E1)
)

private val darkColors = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF8CC7FF), onPrimary = Color(0xFF003258), primaryContainer = Color(0xFF124B78),
    onPrimaryContainer = Color(0xFFD1E7FF), secondary = Color(0xFFB7C9DD),
    background = Color(0xFF101923), surface = Color(0xFF182431),
    surfaceVariant = Color(0xFF253545), onSurface = Color(0xFFE2EDF7),
    onSurfaceVariant = Color(0xFFB5C4D2), outline = Color(0xFF465B6F)
)

@Composable
private fun AppPage(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) { content() }
}

@Composable
private fun HomeScreen(onOpenBmi: () -> Unit, onOpenSettings: () -> Unit) {
    AppPage {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onOpenSettings, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings_accessibility), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))
            BrandMark(large = true)
            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.home_title), fontSize = 31.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.home_tagline), fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(34.dp))
            SectionLabel(R.string.section_calculations)
            Spacer(Modifier.height(12.dp))
            MenuCard(
                title = stringResource(R.string.bmi_title),
                subtitle = stringResource(R.string.bmi_subtitle),
                icon = Icons.Rounded.Calculate,
                onClick = onOpenBmi
            )
            Spacer(Modifier.height(12.dp))
            ComingSoonCard()
            Spacer(Modifier.height(26.dp))
            Text(stringResource(R.string.home_choose_calculation), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BrandMark(large: Boolean = false) {
    val side = if (large) 88.dp else 44.dp
    Box(
        modifier = Modifier.size(side).clip(RoundedCornerShape(if (large) 26.dp else 14.dp)).background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        if (large) {
            Text(stringResource(R.string.brand_initials), fontSize = 31.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimary, letterSpacing = (-1).sp)
        } else {
            Icon(Icons.Rounded.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(25.dp))
        }
    }
}

@Composable
private fun SectionLabel(labelRes: Int) {
    Text(stringResource(labelRes), modifier = Modifier.fillMaxWidth(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun MenuCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(15.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = stringResource(R.string.open_accessibility, title), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ComingSoonCard() {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))) {
        Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(15.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.coming_soon), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.coming_soon_description), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(stringResource(R.string.coming_soon_badge), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 9.dp, vertical = 6.dp))
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back_accessibility), tint = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

private data class BmiResult(val value: Double, val heightCm: Double)

private data class BmiBand(
    val categoryRes: Int,
    val bmiRangeRes: Int,
    val minBmi: Double?,
    val maxBmi: Double?
)

private object UnitConversions {
    const val CENTIMETERS_PER_INCH = 2.54
    const val POUNDS_PER_KILOGRAM = 2.20462262185
    const val IMPERIAL_BMI_FACTOR = 703.0
}

private val bmiBands = listOf(
    BmiBand(R.string.category_underweight, R.string.bmi_underweight_range, null, 18.5),
    BmiBand(R.string.category_healthy, R.string.bmi_healthy_range, 18.5, 25.0),
    BmiBand(R.string.category_overweight, R.string.bmi_overweight_range, 25.0, 30.0),
    BmiBand(R.string.category_obesity_one, R.string.bmi_obesity_one_range, 30.0, 35.0),
    BmiBand(R.string.category_obesity_two, R.string.bmi_obesity_two_range, 35.0, 40.0),
    BmiBand(R.string.category_obesity_three, R.string.bmi_obesity_three_range, 40.0, null)
)

private fun parseDecimal(value: String): Double? = value.trim().replace(',', '.').toDoubleOrNull()

private fun calculateBmi(weight: Double, heightCm: Double, unit: MeasurementUnit): Double? {
    if (!weight.isFinite() || !heightCm.isFinite()) return null
    val value = when (unit) {
        MeasurementUnit.METRIC -> weight / (heightCm / 100.0).let { it * it }
        MeasurementUnit.IMPERIAL -> {
            val heightInches = heightCm / UnitConversions.CENTIMETERS_PER_INCH
            weight * UnitConversions.IMPERIAL_BMI_FACTOR / (heightInches * heightInches)
        }
    }
    return value.takeIf { it.isFinite() }
}

private fun weightAtBmi(bmi: Double, heightCm: Double, unit: MeasurementUnit): Double {
    val heightMeters = heightCm / 100.0
    return when (unit) {
        MeasurementUnit.METRIC -> bmi * heightMeters * heightMeters
        MeasurementUnit.IMPERIAL -> bmi * (heightCm / UnitConversions.CENTIMETERS_PER_INCH).let { it * it } / UnitConversions.IMPERIAL_BMI_FACTOR
    }
}

private fun categoryForBmi(value: Double): Int = when {
    value < 18.5 -> R.string.category_underweight
    value < 25.0 -> R.string.category_healthy
    value < 30.0 -> R.string.category_overweight
    value < 35.0 -> R.string.category_obesity_one
    value < 40.0 -> R.string.category_obesity_two
    else -> R.string.category_obesity_three
}

@Composable
private fun BmiScreen(unit: MeasurementUnit, onBack: () -> Unit) {
    var weightInput by rememberSaveable { mutableStateOf("") }
    var heightCmInput by rememberSaveable { mutableStateOf("") }
    var heightFeetInput by rememberSaveable { mutableStateOf("") }
    var heightInchesInput by rememberSaveable { mutableStateOf("") }
    var result by remember { mutableStateOf<BmiResult?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val parsedWeight = parseDecimal(weightInput)
    val parsedHeightCm = parseDecimal(heightCmInput)
    val parsedFeet = heightFeetInput.trim().toIntOrNull()
    val parsedInches = parseDecimal(heightInchesInput)

    val validWeight = when (unit) {
        MeasurementUnit.METRIC -> parsedWeight != null && parsedWeight in 1.0..500.0
        MeasurementUnit.IMPERIAL -> parsedWeight != null && parsedWeight in UnitConversions.POUNDS_PER_KILOGRAM..(500.0 * UnitConversions.POUNDS_PER_KILOGRAM)
    }
    val imperialHeightInches = if (parsedFeet != null && parsedInches != null) parsedFeet * 12.0 + parsedInches else null
    val currentHeightCm = when (unit) {
        MeasurementUnit.METRIC -> parsedHeightCm
        MeasurementUnit.IMPERIAL -> imperialHeightInches?.times(UnitConversions.CENTIMETERS_PER_INCH)
    }
    val validHeight = when (unit) {
        MeasurementUnit.METRIC -> parsedHeightCm != null && parsedHeightCm in 50.0..250.0
        MeasurementUnit.IMPERIAL -> parsedFeet != null && parsedFeet in 1..8 &&
            parsedInches != null && parsedInches in 0.0..<12.0 &&
            currentHeightCm != null && currentHeightCm in 50.0..250.0
    }
    val canCalculate = validWeight && validHeight

    AppPage {
        TopBar(stringResource(R.string.bmi_title), onBack)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.bmi_heading), fontSize = 27.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.bmi_intro), fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))
            if (result == null) {
                Text(stringResource(R.string.weight_label), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(8) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(if (unit == MeasurementUnit.METRIC) R.string.weight_hint_kg else R.string.weight_hint_lb)) },
                    trailingIcon = { Text(stringResource(if (unit == MeasurementUnit.METRIC) R.string.unit_kg else R.string.unit_lb), modifier = Modifier.padding(end = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                )
                if (weightInput.isNotBlank() && !validWeight) {
                    FieldError(if (unit == MeasurementUnit.METRIC) R.string.invalid_weight_kg else R.string.invalid_weight_lb)
                }
                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.height_label), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                if (unit == MeasurementUnit.METRIC) {
                    OutlinedTextField(
                        value = heightCmInput,
                        onValueChange = { heightCmInput = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(6) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.height_hint_cm)) },
                        trailingIcon = { Text(stringResource(R.string.unit_cm), modifier = Modifier.padding(end = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); keyboard?.hide() })
                    )
                    if (heightCmInput.isNotBlank() && !validHeight) FieldError(R.string.invalid_height_cm)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = heightFeetInput,
                            onValueChange = { heightFeetInput = it.filter(Char::isDigit).take(1) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(R.string.height_hint_feet)) },
                            trailingIcon = { Text(stringResource(R.string.unit_ft), modifier = Modifier.padding(end = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = heightInchesInput,
                            onValueChange = { heightInchesInput = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(5) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(R.string.height_hint_inches)) },
                            trailingIcon = { Text(stringResource(R.string.unit_in), modifier = Modifier.padding(end = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); keyboard?.hide() })
                        )
                    }
                    if ((heightFeetInput.isNotBlank() || heightInchesInput.isNotBlank()) && !validHeight) FieldError(R.string.invalid_height_imperial)
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboard?.hide()
                        val weightInSelectedUnit = parsedWeight!!
                        val height = currentHeightCm!!
                        calculateBmi(weightInSelectedUnit, height, unit)?.let { result = BmiResult(it, height) }
                    },
                    enabled = canCalculate,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) { Text(stringResource(R.string.calculate), fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
            } else {
                Spacer(Modifier.height(26.dp))
                BmiResultCard(result!!, unit)
                Spacer(Modifier.height(14.dp))
                TextButton(onClick = { result = null; weightInput = ""; heightCmInput = ""; heightFeetInput = ""; heightInchesInput = ""; focusManager.clearFocus() }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text(stringResource(R.string.calculate_again), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.bmi_adult_note), fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FieldError(messageRes: Int) {
    Text(stringResource(messageRes), modifier = Modifier.padding(top = 5.dp, start = 4.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun BmiResultCard(result: BmiResult, unit: MeasurementUnit) {
    val locale = currentNumberLocale()
    val valueText = String.format(locale, "%.1f", result.value)
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.result_heading), fontSize = 11.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(valueText, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(stringResource(R.string.bmi_label), fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))
            Spacer(Modifier.height(14.dp))
            Text(stringResource(categoryForBmi(result.value)), fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
        }
    }
    Spacer(Modifier.height(18.dp))
    Text(stringResource(R.string.weight_ranges_title), modifier = Modifier.fillMaxWidth(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    Spacer(Modifier.height(5.dp))
    Text(stringResource(R.string.weight_ranges_note), modifier = Modifier.fillMaxWidth(), fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(10.dp))
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 4.dp)) {
            bmiBands.forEachIndexed { index, band ->
                val rangeText = when {
                    band.minBmi == null -> stringResource(
                        R.string.weight_less_than,
                        formatWeight(weightAtBmi(band.maxBmi!!, result.heightCm, unit), locale),
                        stringResource(if (unit == MeasurementUnit.METRIC) R.string.unit_kg else R.string.unit_lb)
                    )
                    band.maxBmi == null -> stringResource(
                        R.string.weight_at_least,
                        formatWeight(weightAtBmi(band.minBmi, result.heightCm, unit), locale),
                        stringResource(if (unit == MeasurementUnit.METRIC) R.string.unit_kg else R.string.unit_lb)
                    )
                    else -> stringResource(
                        R.string.weight_interval,
                        formatWeight(weightAtBmi(band.minBmi, result.heightCm, unit), locale),
                        formatWeight(weightAtBmi(band.maxBmi, result.heightCm, unit), locale),
                        stringResource(if (unit == MeasurementUnit.METRIC) R.string.unit_kg else R.string.unit_lb)
                    )
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(band.categoryRes), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(3.dp))
                        Text(stringResource(band.bmiRangeRes), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(rangeText, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End)
                }
                if (index < bmiBands.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            }
        }
    }
}

private fun formatWeight(value: Double, locale: Locale): String = String.format(locale, "%.1f", value)

@Composable
private fun currentNumberLocale(): Locale {
    val systemLocale = ConfigurationCompat.getLocales(LocalConfiguration.current).get(0) ?: Locale.getDefault()
    val appLocale = AppCompatDelegate.getApplicationLocales().get(0) ?: systemLocale
    return if (appLocale.language == "pt" && appLocale.country.equals("BR", ignoreCase = true)) Locale("pt", "BR") else Locale.ENGLISH
}

@Composable
private fun SettingsScreen(
    selectedTheme: AppTheme,
    selectedLanguage: AppLanguage,
    selectedUnit: MeasurementUnit,
    onThemeSelected: (AppTheme) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onUnitSelected: (MeasurementUnit) -> Unit,
    onBack: () -> Unit
) {
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    AppPage {
        TopBar(stringResource(R.string.settings_title), onBack)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.settings_subtitle), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.settings_description), fontSize = 15.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(26.dp))

            SectionLabel(R.string.section_appearance)
            Spacer(Modifier.height(10.dp))
            SettingsGroup {
                SettingsOption(stringResource(R.string.theme_light), stringResource(R.string.theme_light_description), selectedTheme == AppTheme.LIGHT, Icons.Rounded.LightMode) { onThemeSelected(AppTheme.LIGHT) }
                OptionDivider()
                SettingsOption(stringResource(R.string.theme_dark), stringResource(R.string.theme_dark_description), selectedTheme == AppTheme.DARK, Icons.Rounded.DarkMode) { onThemeSelected(AppTheme.DARK) }
                OptionDivider()
                SettingsOption(stringResource(R.string.theme_system), stringResource(R.string.theme_system_description), selectedTheme == AppTheme.SYSTEM, Icons.Rounded.Smartphone) { onThemeSelected(AppTheme.SYSTEM) }
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel(R.string.section_language)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.language_description), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            SettingsGroup {
                SettingsOption(stringResource(R.string.language_portuguese), null, selectedLanguage == AppLanguage.PORTUGUESE_BRAZIL, flagEmoji = "🇧🇷") { onLanguageSelected(AppLanguage.PORTUGUESE_BRAZIL) }
                OptionDivider()
                SettingsOption(stringResource(R.string.language_english), null, selectedLanguage == AppLanguage.ENGLISH, flagEmoji = "🇺🇸") { onLanguageSelected(AppLanguage.ENGLISH) }
                OptionDivider()
                SettingsOption(stringResource(R.string.language_system), null, selectedLanguage == AppLanguage.SYSTEM, Icons.Rounded.Smartphone) { onLanguageSelected(AppLanguage.SYSTEM) }
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel(R.string.section_units)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.units_description), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            SettingsGroup {
                SettingsOption(stringResource(R.string.unit_metric), stringResource(R.string.unit_metric_description), selectedUnit == MeasurementUnit.METRIC) { onUnitSelected(MeasurementUnit.METRIC) }
                OptionDivider()
                SettingsOption(stringResource(R.string.unit_imperial), stringResource(R.string.unit_imperial_description), selectedUnit == MeasurementUnit.IMPERIAL) { onUnitSelected(MeasurementUnit.IMPERIAL) }
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = { showAboutDialog = true }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
                Text(stringResource(R.string.about_button), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text(stringResource(R.string.about_title)) },
                text = { Text(stringResource(R.string.about_message)) },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text(stringResource(R.string.close))
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 7.dp), content = content)
    }
}

@Composable
private fun OptionDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), modifier = Modifier.padding(start = 38.dp))
}

@Composable
private fun SettingsOption(
    title: String,
    subtitle: String?,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    flagEmoji: String? = null,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onSelect).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(13.dp))
        }
        if (flagEmoji != null) {
            Text(flagEmoji, fontSize = 21.sp)
            Spacer(Modifier.width(11.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Spacer(Modifier.height(3.dp))
                Text(subtitle, fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        RadioButton(selected = selected, onClick = onSelect)
    }
}
