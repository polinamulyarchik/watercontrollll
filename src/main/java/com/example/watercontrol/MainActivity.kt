package com.example.watercontrol

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.CalendarContract.Colors
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.WaterDrop


val OceanBlue = Color(0xFF138AC9)
val SeafoamGreen = Color(0xFF5EC4FC)
val LightSkyBlue = Color(0xFF45B5F3)
val SandYellow = Color(0xFFFFD166)
val SeaShellWhite = Color(0xFFEEF7FA)
val CoralRed = Color(0xFF005683)


private val LightColors = lightColorScheme(
    primary = OceanBlue,
    secondary = LightSkyBlue,
    background = SeaShellWhite,
    surface = SeafoamGreen,
    onPrimary = SeaShellWhite,
    onSecondary = SeaShellWhite,
    onBackground = CoralRed,
    onSurface = CoralRed
)


fun calculateWaterIntake(gender: String, weight: Int, activityLevel: String): Int {
    val baseWater = if (gender == "Male") 35 else 30
    val activityMultiplier = when (activityLevel) {
        "Low" -> 1.0
        "Moderate" -> 1.2
        "High" -> 1.5
        else -> 1.0
    }
    return (baseWater * weight * activityMultiplier).toInt()
}


fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

fun saveDailyWaterIntake(context: Context, dailyIntake: Map<String, Int>) {
    val prefs = context.getSharedPreferences("WaterTrackerPrefs", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    dailyIntake.forEach { (day, intake) ->
        editor.putInt(day, intake)
    }
    editor.apply()
}
fun loadDailyWaterIntake(context: Context): Map<String, Int> {
    val prefs = context.getSharedPreferences("WaterTrackerPrefs", Context.MODE_PRIVATE)
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val dailyIntake = mutableMapOf<String, Int>()

    daysOfWeek.forEach { day ->
        val intake = prefs.getInt(day, 0)
        dailyIntake[day] = intake
    }

    return dailyIntake
}

fun saveToPreferences(context: Context, key: String, value: Int) {
    val sharedPreferences = context.getSharedPreferences("WaterTrackerPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putInt(key, value)
    editor.apply()
}


fun getFromPreferences(context: Context, key: String, defaultValue: Int): Int {
    val sharedPreferences = context.getSharedPreferences("WaterTrackerPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getInt(key, defaultValue)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = LightColors) {
                SideMenuApp()
            }
        }
    }
}
fun getDayOfWeek(): String {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val currentDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
    return daysOfWeek[currentDay - 1]
}

@Composable
fun GenderRadioGroup(selectedGender: String, onGenderSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(color = OceanBlue.copy(alpha = 0.1f)).clip(RoundedCornerShape(16.dp))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedGender == "Male",
                onClick = { onGenderSelected("Male") }
            )
            Text("Мужской", modifier = Modifier.padding(start = 8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedGender == "Female",
                onClick = { onGenderSelected("Female") }
            )
            Text("Женский", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun ActivityLevelRadioGroup(selectedActivityLevel: String, onActivityLevelSelected: (String) -> Unit) {

    Column(modifier = Modifier.fillMaxWidth().background(color = OceanBlue.copy(alpha = 0.1f)).clip(RoundedCornerShape(16.dp))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedActivityLevel == "Low",
                onClick = { onActivityLevelSelected("Low") }
            )
            Text("Низкая", modifier = Modifier.padding(start = 8.dp))
            Text("(Целый день за столом)", modifier = Modifier.padding(start = 8.dp), fontSize = 15.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedActivityLevel == "Moderate",
                onClick = { onActivityLevelSelected("Moderate") }
            )
            Text("Средняя", modifier = Modifier.padding(start = 8.dp))
            Text("(Пару раз выходил)", modifier = Modifier.padding(start = 8.dp), fontSize = 15.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedActivityLevel == "High",
                onClick = { onActivityLevelSelected("High") }
            )
            Text("Высокая", modifier = Modifier.padding(start = 8.dp))
            Text("(Занимался спортом)", modifier = Modifier.padding(start = 8.dp), fontSize = 15.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideMenuApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val screens = listOf("Главная", "Настройки")
    var selectedScreen by remember { mutableStateOf(screens[0]) }
    val context = LocalContext.current

    var gender = remember { mutableStateOf("Male") }
    var weight = remember { mutableStateOf(70) }
    var waterIntake: MutableState<TextFieldValue> = remember { mutableStateOf(TextFieldValue("")) }
    var activityLevel = remember { mutableStateOf("Low") }
    var recommendedWater = remember { mutableStateOf(0) }
    var dailyWaterIntake = remember { mutableStateOf(mutableMapOf<String, Int>()) }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Меню",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                screens.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen) },
                        selected = selectedScreen == screen,
                        onClick = {
                            selectedScreen = screen
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(selectedScreen) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Меню")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    when (selectedScreen) {

                        "Главная" -> StatsScreen(
                            gender,
                            weight,
                            waterIntake,
                            activityLevel,
                            recommendedWater,
                            dailyWaterIntake
                        )
                        "Настройки" -> SettingsScreen(
                            gender,
                            weight,
                            waterIntake,
                            activityLevel,
                            recommendedWater,
                            dailyWaterIntake
                        )
                    }
                }
            }
        }
    )
}


@Composable
fun StatsScreen(    gender: MutableState<String>,
                    weight: MutableState<Int>,
                    waterIntake: MutableState<TextFieldValue>,
                    activityLevel: MutableState<String>,
                    recommendedWater: MutableState<Int>,
                    dailyWaterIntake: MutableState<MutableMap<String, Int>>)
{
    val scrollState = rememberScrollState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        dailyWaterIntake.value = loadDailyWaterIntake(context).toMutableMap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp).verticalScroll(scrollState)
    ) {


        Text("Уровень активности:")
        ActivityLevelRadioGroup(activityLevel.value) { selectedActivityLevel ->
            activityLevel.value = selectedActivityLevel
            recommendedWater.value = calculateWaterIntake(gender.value, weight.value, activityLevel.value)
        }

        Spacer(modifier = Modifier.height(26.dp))

        Text("Рекомендуемая норма воды: ${recommendedWater.value} мл")

        Spacer(modifier = Modifier.height(26.dp))

        WaterIntakeGraph(dailyWaterIntake.value)

        Spacer(modifier = Modifier.height(26.dp))

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val context = LocalContext.current
            val currentDate = getCurrentDate()
            Button(
                shape = CircleShape,
                modifier = Modifier.size(150.dp),
                onClick = {
                    val dayOfWeek = getDayOfWeek()
                    val currentIntake = dailyWaterIntake.value[dayOfWeek] ?: 0
                    dailyWaterIntake.value = dailyWaterIntake.value.toMutableMap().apply {
                        put(dayOfWeek, currentIntake + 250)
                    }


                    saveDailyWaterIntake(context, dailyWaterIntake.value)

                    recommendedWater.value =
                        calculateWaterIntake(gender.value, weight.value, activityLevel.value)

                }
            ) {

                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        text = "+250 мл.",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(    gender: MutableState<String>,
                       weight: MutableState<Int>,
                       waterIntake: MutableState<TextFieldValue>,
                       activityLevel: MutableState<String>,
                       recommendedWater: MutableState<Int>,
                       dailyWaterIntake: MutableState<MutableMap<String, Int>>) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp).verticalScroll(scrollState)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text("Пол:")
        GenderRadioGroup(gender.value) { selectedGender ->
            gender.value = selectedGender
            recommendedWater.value = calculateWaterIntake(gender.value, weight.value, activityLevel.value)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Вес (кг):")
        TextField(
            value = weight.value.toString(),
            onValueChange = { weight.value = it.toIntOrNull() ?: 0 },
            label = { Text("Вес (кг)") },
            modifier = Modifier.fillMaxWidth()
        )

    }
}

@Composable
fun WaterTrackerApp() {


}

@Composable
fun WaterIntakeGraph(dailyWaterIntake: Map<String, Int>) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        val barWidth = size.width / (daysOfWeek.size * 2)
        val maxWaterIntake = (dailyWaterIntake.values.maxOrNull() ?: 1).coerceAtLeast(1)
        val scaleFactor = size.height / maxWaterIntake

        val gridLineCount = 5
        val gridStep = size.height / gridLineCount

        for (i in 0..gridLineCount) {
            val y = i * gridStep
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )

            val label = ((maxWaterIntake / gridLineCount) * (gridLineCount - i)).toString() + " мл"
            drawContext.canvas.nativeCanvas.drawText(
                label,
                10f,
                y - 4,
                android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.LEFT
                    textSize = 24f
                    color = android.graphics.Color.GRAY
                }
            )
        }

        for (i in daysOfWeek.indices) {
            val x = (i * 2 * barWidth) + (barWidth / 2)
            drawLine(
                color = Color.LightGray,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }

        daysOfWeek.forEachIndexed { index, day ->
            val intake = dailyWaterIntake[day] ?: 0
            val barHeight = intake * scaleFactor
            drawRect(
                color = OceanBlue,
                topLeft = Offset(x = (index * 2 * barWidth), y = size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )

            drawContext.canvas.nativeCanvas.drawText(
                day,
                (index * 2 * barWidth) + (barWidth / 2),
                size.height + 40,
                android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 30f
                    color = android.graphics.Color.BLACK
                }
            )
        }
    }
}
