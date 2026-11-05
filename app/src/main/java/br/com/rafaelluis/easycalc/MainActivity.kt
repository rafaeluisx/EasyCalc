package br.com.rafaelluis.easycalc

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.core.view.WindowCompat

private enum class AppScreen { HOME, BMI, SETTINGS }
private enum class AppTheme { LIGHT, DARK, SYSTEM }

class MainActivity : ComponentActivity() {
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
        val window = (view.context as Activity).window
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
                AppScreen.HOME -> HomeScreen(onOpenBmi = { screen = AppScreen.BMI }, onOpenSettings = { screen = AppScreen.SETTINGS })
                AppScreen.BMI -> BmiScreen(onBack = { screen = AppScreen.HOME })
                AppScreen.SETTINGS -> SettingsScreen(
                    selectedTheme = selectedTheme,
                    onThemeSelected = { theme ->
                        selectedTheme = theme
                        preferences.edit().putString("theme", theme.name).apply()
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
                Icon(Icons.Rounded.Settings, contentDescription = "Configurações", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))
            BrandMark(large = true)
            Spacer(Modifier.height(18.dp))
            Text("Calcula Fácil", fontSize = 31.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text("Contas do dia a dia, sem complicação.", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(34.dp))
            SectionLabel("CÁLCULOS")
            Spacer(Modifier.height(12.dp))
            MenuCard(title = "Índice de Massa Corporal", subtitle = "Descubra seu IMC e sua faixa", icon = Icons.Rounded.Calculate, onClick = onOpenBmi)
            Spacer(Modifier.height(12.dp))
            ComingSoonCard()
            Spacer(Modifier.height(26.dp))
            Text("Escolha uma opção para começar", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
        }
        Footer()
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
            Text("CF", fontSize = 31.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimary, letterSpacing = (-1).sp)
        } else {
            Icon(Icons.Rounded.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(25.dp))
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(label, modifier = Modifier.fillMaxWidth(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Icon(Icons.Rounded.ChevronRight, contentDescription = "Abrir $title", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text("Em desenvolvimento", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text("Em breve, um novo cálculo", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("EM BREVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 9.dp, vertical = 6.dp))
        }
    }
}

@Composable
private fun Footer() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    Text("Desenvolvido por Rafael Luis - 2026", modifier = Modifier.fillMaxWidth().padding(vertical = 17.dp, horizontal = 12.dp), textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar ao menu", tint = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

private data class BmiResult(val value: Double, val category: String, val description: String)

private fun bmiResult(weightKg: Double, heightCm: Double): BmiResult? {
    if (!weightKg.isFinite() || !heightCm.isFinite() || weightKg !in 1.0..500.0 || heightCm !in 50.0..250.0) return null
    val bmi = weightKg / (heightCm / 100.0).let { it * it }
    val (category, description) = when {
        bmi < 18.5 -> "Abaixo do peso" to "IMC abaixo de 18,5"
        bmi < 25.0 -> "Peso ideal" to "Faixa de referência: 18,5 a 24,9"
        bmi < 30.0 -> "Acima do peso" to "IMC entre 25,0 e 29,9"
        bmi < 35.0 -> "Obesidade classe I" to "IMC entre 30,0 e 34,9"
        bmi < 40.0 -> "Obesidade classe II" to "IMC entre 35,0 e 39,9"
        else -> "Obesidade classe III" to "IMC igual ou acima de 40,0"
    }
    return BmiResult(bmi, category, description)
}

@Composable
private fun BmiScreen(onBack: () -> Unit) {
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var result by remember { mutableStateOf<BmiResult?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val parsedWeight = weight.trim().replace(',', '.').toDoubleOrNull()
    val parsedHeight = height.trim().replace(',', '.').toDoubleOrNull()
    val validWeight = parsedWeight != null && parsedWeight in 1.0..500.0
    val validHeight = parsedHeight != null && parsedHeight in 50.0..250.0
    val canCalculate = validWeight && validHeight

    AppPage {
        TopBar("Índice de Massa Corporal", onBack)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Vamos calcular seu IMC", fontSize = 27.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text("Preencha seus dados para ver o resultado.", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))
            if (result == null) {
                Text("Peso", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(7) },
                    modifier = Modifier.fillMaxWidth(), placeholder = { Text("Informe seu peso em quilogramas") },
                    trailingIcon = { Text("kg", modifier = Modifier.padding(end = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true, shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                )
                if (weight.isNotBlank() && !validWeight) FieldError("Informe um peso entre 1 e 500 kg.")
                Spacer(Modifier.height(20.dp))
                Text("Altura", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = height, onValueChange = { height = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(6) },
                    modifier = Modifier.fillMaxWidth(), placeholder = { Text("Informe sua altura em centímetros") },
                    trailingIcon = { Text("cm", modifier = Modifier.padding(end = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true, shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); keyboard?.hide() })
                )
                if (height.isNotBlank() && !validHeight) FieldError("Informe uma altura entre 50 e 250 cm.")
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { focusManager.clearFocus(); keyboard?.hide(); result = bmiResult(parsedWeight!!, parsedHeight!!) },
                    enabled = canCalculate, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) { Text("Calcular", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
            } else {
                Spacer(Modifier.height(26.dp))
                BmiResultCard(result!!)
                Spacer(Modifier.height(14.dp))
                TextButton(onClick = { result = null; weight = ""; height = ""; focusManager.clearFocus() }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Calcular novamente", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("O IMC é uma referência para adultos e não substitui uma avaliação profissional.", fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
        }
        Footer()
    }
}

@Composable
private fun FieldError(message: String) {
    Text(message, modifier = Modifier.padding(top = 5.dp, start = 4.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun BmiResultCard(result: BmiResult) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SEU RESULTADO", fontSize = 11.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(String.format(java.util.Locale("pt", "BR"), "%.1f", result.value), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("IMC", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))
            Spacer(Modifier.height(14.dp))
            Text(result.category, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(result.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SettingsScreen(selectedTheme: AppTheme, onThemeSelected: (AppTheme) -> Unit, onBack: () -> Unit) {
    AppPage {
        TopBar("Configurações", onBack)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(20.dp))
            Text("Personalize o aplicativo", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text("Escolha como o Calcula Fácil aparece no seu dispositivo.", fontSize = 15.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))
            SectionLabel("APARÊNCIA")
            Spacer(Modifier.height(12.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ThemeOption(AppTheme.LIGHT, "Claro", "Sempre usar o tema claro", selectedTheme, onThemeSelected, Icons.Rounded.LightMode)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), modifier = Modifier.padding(start = 56.dp))
                    ThemeOption(AppTheme.DARK, "Escuro", "Sempre usar o tema escuro", selectedTheme, onThemeSelected, Icons.Rounded.DarkMode)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), modifier = Modifier.padding(start = 56.dp))
                    ThemeOption(AppTheme.SYSTEM, "Conforme configuração do celular", "Acompanhar o tema do dispositivo", selectedTheme, onThemeSelected, Icons.Rounded.Smartphone)
                }
            }
        }
        Footer()
    }
}

@Composable
private fun ThemeOption(theme: AppTheme, title: String, subtitle: String, selectedTheme: AppTheme, onSelect: (AppTheme) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onSelect(theme) }.padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RadioButton(selected = selectedTheme == theme, onClick = { onSelect(theme) })
    }
}
