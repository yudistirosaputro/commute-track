# CommuteTrack Android App - Complete Implementation Plan

## 📋 Executive Summary

**Project Goal**: Build a battery-efficient Android commute tracking app to collect 3 months of daily commute data for AI-powered analysis of optimal departure times.

**Core Philosophy**: Simple, battery-efficient time tracking without maps or background services. Focus on accurate time recording with pause functionality for stops (gas, shopping, etc.).

**Timeline**: 3-phase development over 6-8 weeks
- Phase 1 (2 weeks): Core tracking with pause/resume
- Phase 2 (2-3 weeks): Statistics & UI polish
- Phase 3 (2-3 weeks): AI analysis & insights

---

## 🎯 User Requirements & Goals

### Primary Requirements
1. **Simple Time Tracking**
   - Single button to START trip (records departure time)
   - Single button to STOP trip (records arrival time)
   - PAUSE button for temporary stops (gas, shopping) to save battery
   - NO continuous GPS tracking
   - NO background services
   - NO real-time map display

2. **Efficient Battery Usage**
   - No background location polling
   - Timer updates only when app is visible
   - Pause functionality to stop any processing during stops
   - OLED-friendly dark theme to minimize battery drain

3. **Data Collection for Analysis**
   - Track departure time, arrival time, total duration
   - Record delays/stops (pause duration)
   - Optional: distance traveled, average speed
   - Goal: 3 months of baseline data before AI analysis

4. **Analytics Goal**
   - Identify optimal departure times to avoid traffic
   - Analyze patterns: which departure times lead to shortest commutes
   - Compare weekdays vs specific days
   - Factor in stops/delays

---

## 🏗️ Current Architecture Analysis

### ✅ What's Already Implemented

**Clean Architecture Structure**
```
✅ Domain Layer (Pure Kotlin)
   - CommuteSession model (with PAUSED status already defined!)
   - Use cases: StartSession, EndSession, GetActiveSession, GetHistory, GetStatistics
   - Repository interfaces

✅ Data Layer
   - Room database with CommuteSessionDao
   - Repository implementations
   - Entity mappers

✅ Presentation Layer
   - 5 feature modules: dashboard, tracking, history, statistics, settings
   - Jetpack Compose UI
   - ViewModels with StateFlow
   - Material3 theme
```

**Current Tracking Implementation**
- ✅ Start trip with location & transport mode
- ✅ Real-time elapsed timer (updates every second)
- ✅ End trip dialog with end location & distance
- ❌ No PAUSE/RESUME functionality in UI
- ❌ Timer runs continuously in viewModelScope (not battery efficient)
- ❌ No speed calculation

### 🔧 What Needs to Be Modified/Added

1. **Enhanced Tracking Features**
   - Add PAUSE/RESUME buttons to UI
   - Implement pause duration tracking
   - Simplify timer (remove continuous 1-second updates when not needed)
   - Add speed calculation (distance / duration)
   - Add manual distance input option

2. **Battery Optimization**
   - Replace continuous timer with on-demand updates
   - Only calculate elapsed time when screen is visible
   - Stop all updates when paused
   - Use WorkManager only for rare background tasks (if needed)

3. **Statistics Enhancement**
   - Add "Optimal Departure Time" analysis view
   - Group sessions by departure hour (6 AM, 7 AM, 8 AM, etc.)
   - Calculate average duration per departure time slot
   - Show weekly/monthly trends
   - Export data for AI analysis (CSV/JSON)

4. **UI/Theme Implementation**
   - Apply deep dark green theme (#0A0F0D background)
   - Neon green accents (#00FF47) for active states
   - Implement all 6 Stitch screens
   - Large monospace fonts for timers
   - Glassy dark cards (#16201A)

---

## 🎨 UI Design System (Based on Stitch & Requirements)

### Color Palette
```kotlin
// Theme.kt additions
val DarkestGreen = Color(0xFF0A0F0D)      // Background (OLED black-green)
val NeonGreen = Color(0xFF00FF47)          // Primary accent (active states, CTAs)
val SlateGreen = Color(0xFF8E9994)         // Secondary text
val GlassyCard = Color(0xFF16201A)         // Card surface (semi-transparent)
val BorderGreen = Color(0xFF2A3830)        // Subtle borders

// Status colors
val ActiveGreen = Color(0xFF00FF47)        // Active trip
val PausedAmber = Color(0xFFFFB800)        // Paused state
val CompletedGray = Color(0xFF5A6C64)      // Completed trip
```

### Typography
```kotlin
// Use monospace for timers
val MonoFamily = FontFamily(Font(R.font.jetbrains_mono))

// Timer text style
val TimerStyle = TextStyle(
    fontFamily = MonoFamily,
    fontSize = 48.sp,
    fontWeight = FontWeight.Bold,
    color = NeonGreen
)
```

### Screen Layouts (6 Stitch Screens)

1. **Commute Dashboard** (`e0dc7c9752324bb390f9649ad4f7cad8`)
   - Current status: Not Started / Active / Paused
   - Quick stats: Today's commute, This week average
   - Large START button (when no active trip)
   - Active trip card (when tracking)

2. **Active Commute Session** (`94422e641d024ac8bc1513b7ea9fc12c`)
   - Large monospace timer (MM:SS or HH:MM)
   - Start location & time
   - Transport mode icon
   - PAUSE button (prominent, amber when paused)
   - STOP button (red, requires confirmation)
   - Pause counter (if any pauses occurred)

3. **Commute Statistics Overview** (`2d78710a7d23443ba954231555d1a75e`)
   - Total trips this month
   - Average duration
   - Total distance
   - Most used transport mode
   - Chart: Duration trend over time

4. **Trip History List** (`54983f41f44042fd99176a468e5ebb37`)
   - Scrollable list of all trips
   - Each card shows: Date, Start time, Duration, Distance, Transport mode
   - Filter by: Date range, Transport mode
   - Search by location

5. **History and Settings** (`da03baa6b16945e8a39804502787a384`)
   - Combined view with tabs
   - History tab: Recent trips
   - Settings tab: Quick toggles

6. **App Settings** (`ef8abb4b131c41c9bc08912744946b62`)
   - Distance unit (km/miles)
   - Default transport mode
   - Home/Work saved locations
   - Export data (CSV for AI analysis)
   - Dark theme (always on for this app)
   - Notifications toggle

---

## 💾 Enhanced Data Model

### CommuteSession Enhancements
```kotlin
// Already exists, but clarifying usage:
data class CommuteSession(
    val id: Long = 0,
    val startTime: LocalDateTime,           // When START button pressed
    val endTime: LocalDateTime? = null,      // When STOP button pressed
    val startLocation: String,
    val endLocation: String = "",
    val transportMode: TransportMode,
    val distanceKm: Double = 0.0,           // Manual input or GPS-based
    val durationMinutes: Int = 0,           // Total: (endTime - startTime) - pausedMinutes
    val status: SessionStatus,              // ACTIVE, PAUSED, COMPLETED, CANCELLED
    val date: LocalDate = startTime.date,
    val notes: String = "",

    // NEW FIELDS TO ADD:
    val pausedMinutes: Int = 0,             // Total time spent paused
    val pauseCount: Int = 0,                // Number of times paused
    val averageSpeedKmh: Double = 0.0,      // distanceKm / (durationMinutes/60)
    val pauseHistory: List<PauseRecord> = emptyList()  // Optional: track each pause
)

data class PauseRecord(
    val pausedAt: LocalDateTime,
    val resumedAt: LocalDateTime?,
    val reason: String = ""  // e.g., "Gas", "Shopping", "Traffic"
)
```

### Statistics Model Enhancement
```kotlin
data class CommuteStatistics(
    // Existing fields...
    val totalTrips: Int,
    val totalDistanceKm: Double,
    val averageDurationMinutes: Int,
    val mostUsedTransportMode: TransportMode,

    // NEW: Optimal Departure Analysis
    val departureTimeAnalysis: List<DepartureTimeStats>,
    val bestDepartureTime: DepartureTimeStats?,  // Shortest avg duration
    val worstDepartureTime: DepartureTimeStats?  // Longest avg duration
)

data class DepartureTimeStats(
    val hourOfDay: Int,                     // 0-23 (e.g., 7 for 7 AM)
    val tripCount: Int,
    val averageDurationMinutes: Int,
    val averageSpeedKmh: Double,
    val averagePauseCount: Int,
    val dayOfWeekBreakdown: Map<DayOfWeek, Int>  // Average per day
)
```

---

## 🔨 Implementation Details

### Phase 1: Core Tracking with Pause/Resume (Week 1-2)

#### 1.1 Update TrackingViewModel
```kotlin
class TrackingViewModel {
    // Add pause tracking
    private var pauseStartTime: LocalDateTime? = null
    private var totalPausedMinutes: Int = 0
    private var pauseCount: Int = 0

    fun pauseTrip() {
        viewModelScope.launch {
            _uiState.value.activeSession?.let { session ->
                pauseStartTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                pauseSession(session.id)  // Update status to PAUSED in DB
                elapsedTimeJob?.cancel()  // Stop timer updates
            }
        }
    }

    fun resumeTrip() {
        viewModelScope.launch {
            pauseStartTime?.let { pauseStart ->
                val pauseEnd = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val pauseDuration = calculateMinutes(pauseStart, pauseEnd)
                totalPausedMinutes += pauseDuration
                pauseCount++

                resumeSession(session.id, pauseDuration)  // Update to ACTIVE
                startElapsedTimeTracking(session)  // Resume timer
                pauseStartTime = null
            }
        }
    }

    // Simplified timer - only update when visible
    private fun startElapsedTimeTracking(session: CommuteSession) {
        elapsedTimeJob?.cancel()
        elapsedTimeJob = viewModelScope.launch {
            // Update every 10 seconds instead of every second to save battery
            while (true) {
                val elapsed = calculateElapsedTime(session)
                _uiState.update { it.copy(elapsedTime = elapsed.formatDuration()) }
                kotlinx.coroutines.delay(10_000)  // 10 seconds
            }
        }
    }

    private fun calculateElapsedTime(session: CommuteSession): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val totalMinutes = calculateMinutes(session.startTime, now)
        return (totalMinutes - totalPausedMinutes).coerceAtLeast(0)
    }
}
```

#### 1.2 Update TrackingScreen UI
```kotlin
@Composable
fun ActiveTripCard(
    session: CommuteSession,
    elapsedTime: String,
    isPaused: Boolean,
    pauseCount: Int,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = GlassyCard
        ),
        border = BorderStroke(1.dp, BorderGreen)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Status indicator
            StatusChip(
                text = if (isPaused) "PAUSED" else "ACTIVE",
                color = if (isPaused) PausedAmber else NeonGreen
            )

            // Large timer (monospace)
            Text(
                text = elapsedTime,
                style = TimerStyle,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Trip info
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Started", style = MaterialTheme.typography.labelSmall)
                    Text(session.startTime.format(), style = MaterialTheme.typography.bodyLarge)
                }
                Column {
                    Text("Pauses", style = MaterialTheme.typography.labelSmall)
                    Text(pauseCount.toString(), style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isPaused) {
                    Button(
                        onClick = onResume,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("RESUME")
                    }
                } else {
                    Button(
                        onClick = onPause,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PausedAmber)
                    ) {
                        Icon(Icons.Default.Pause, null)
                        Spacer(Modifier.width(8.dp))
                        Text("PAUSE")
                    }
                }

                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Stop, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("STOP", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
```

#### 1.3 New Use Cases to Create
```kotlin
// PauseSessionUseCase.kt
class PauseSessionUseCase @Inject constructor(
    private val repository: CommuteRepository
) {
    suspend operator fun invoke(sessionId: Long) {
        repository.updateSessionStatus(sessionId, SessionStatus.PAUSED)
    }
}

// ResumeSessionUseCase.kt
class ResumeSessionUseCase @Inject constructor(
    private val repository: CommuteRepository
) {
    suspend operator fun invoke(sessionId: Long, pauseDurationMinutes: Int) {
        repository.resumeSession(sessionId, pauseDurationMinutes)
    }
}
```

#### 1.4 Update CommuteRepository
```kotlin
interface CommuteRepository {
    // Add new methods
    suspend fun updateSessionStatus(sessionId: Long, status: SessionStatus)
    suspend fun resumeSession(sessionId: Long, pauseDurationMinutes: Int)
    suspend fun addPauseRecord(sessionId: Long, pauseStart: LocalDateTime, pauseEnd: LocalDateTime)
}
```

### Phase 2: Statistics & Analytics (Week 3-5)

#### 2.1 Departure Time Analysis Use Case
```kotlin
class GetDepartureTimeAnalysisUseCase @Inject constructor(
    private val repository: CommuteRepository
) {
    suspend operator fun invoke(monthsBack: Int = 3): List<DepartureTimeStats> {
        val sessions = repository.getCompletedSessions(monthsBack)

        return sessions
            .groupBy { it.startTime.hour }  // Group by hour (0-23)
            .map { (hour, trips) ->
                DepartureTimeStats(
                    hourOfDay = hour,
                    tripCount = trips.size,
                    averageDurationMinutes = trips.map { it.durationMinutes }.average().toInt(),
                    averageSpeedKmh = trips.map { it.averageSpeedKmh }.average(),
                    averagePauseCount = trips.map { it.pauseCount }.average().toInt(),
                    dayOfWeekBreakdown = trips.groupBy { it.date.dayOfWeek }
                        .mapValues { it.value.map { s -> s.durationMinutes }.average().toInt() }
                )
            }
            .sortedBy { it.hourOfDay }
    }
}
```

#### 2.2 Enhanced Statistics Screen
```kotlin
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // Overview stats
        OverviewStatsSection(uiState.statistics)

        // Optimal Departure Time Section (NEW)
        OptimalDepartureCard(
            bestTime = uiState.statistics.bestDepartureTime,
            worstTime = uiState.statistics.worstDepartureTime
        )

        // Departure time chart
        DepartureTimeChart(
            data = uiState.statistics.departureTimeAnalysis
        )

        // Weekly breakdown
        WeeklyBreakdownChart(uiState.statistics)

        // Export data button
        Button(
            onClick = { viewModel.exportDataForAI() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, null)
            Text("Export for AI Analysis")
        }
    }
}

@Composable
fun OptimalDepartureCard(
    bestTime: DepartureTimeStats?,
    worstTime: DepartureTimeStats?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GlassyCard),
        border = BorderStroke(1.dp, NeonGreen)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "🎯 Optimal Departure Time",
                style = MaterialTheme.typography.titleLarge,
                color = NeonGreen
            )

            Spacer(Modifier.height(16.dp))

            bestTime?.let {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BEST TIME", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "${it.hourOfDay}:00",
                            style = MaterialTheme.typography.headlineMedium,
                            color = NeonGreen,
                            fontFamily = MonoFamily
                        )
                        Text(
                            "Avg: ${it.averageDurationMinutes} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateGreen
                        )
                    }

                    worstTime?.let { worst ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text("WORST TIME", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "${worst.hourOfDay}:00",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontFamily = MonoFamily
                            )
                            Text(
                                "Avg: ${worst.averageDurationMinutes} min",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateGreen
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "💡 Leaving at ${it.hourOfDay}:00 saves you ~${worstTime?.averageDurationMinutes?.minus(it.averageDurationMinutes) ?: 0} minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateGreen
                )
            } ?: Text(
                "Need at least 10 trips to calculate optimal time",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateGreen
            )
        }
    }
}
```

#### 2.3 Data Export for AI Analysis
```kotlin
class ExportDataUseCase @Inject constructor(
    private val repository: CommuteRepository,
    private val context: Context
) {
    suspend operator fun invoke(): Result<Uri> {
        val sessions = repository.getAllCompletedSessions()

        val csvData = buildString {
            // CSV Header
            appendLine("date,day_of_week,departure_time,arrival_time,duration_minutes,paused_minutes,pause_count,distance_km,avg_speed_kmh,transport_mode,start_location,end_location")

            // Data rows
            sessions.forEach { session ->
                appendLine(
                    "${session.date}," +
                    "${session.date.dayOfWeek}," +
                    "${session.startTime.time}," +
                    "${session.endTime?.time}," +
                    "${session.durationMinutes}," +
                    "${session.pausedMinutes}," +
                    "${session.pauseCount}," +
                    "${session.distanceKm}," +
                    "${session.averageSpeedKmh}," +
                    "${session.transportMode}," +
                    "\"${session.startLocation}\"," +
                    "\"${session.endLocation}\""
                )
            }
        }

        return try {
            val file = File(context.cacheDir, "commute_data_${System.currentTimeMillis()}.csv")
            file.writeText(csvData)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Phase 3: UI Polish & Optimization (Week 6-8)

#### 3.1 Apply Complete Theme
```kotlin
// core/ui/theme/Color.kt
object CommuteColors {
    val DarkestGreen = Color(0xFF0A0F0D)
    val NeonGreen = Color(0xFF00FF47)
    val SlateGreen = Color(0xFF8E9994)
    val GlassyCard = Color(0xFF16201A)
    val BorderGreen = Color(0xFF2A3830)
    val PausedAmber = Color(0xFFFFB800)
}

// Theme.kt
private val DarkColorScheme = darkColorScheme(
    primary = CommuteColors.NeonGreen,
    onPrimary = CommuteColors.DarkestGreen,
    secondary = CommuteColors.SlateGreen,
    background = CommuteColors.DarkestGreen,
    surface = CommuteColors.GlassyCard,
    onSurface = Color.White,
    onSurfaceVariant = CommuteColors.SlateGreen
)
```

#### 3.2 Implement All Stitch Screens

**Dashboard Screen**
- Show current trip status or start button
- Quick stats for today/week
- Recent trips list (last 3)
- Large START button (neon green)

**Active Session Screen**
- Full-screen focus on current trip
- Large monospace timer
- Prominent PAUSE/RESUME button
- Secondary STOP button
- Trip metadata (start time, transport mode)

**Statistics Overview**
- Total trips, distance, avg duration cards
- Optimal departure time highlight
- Bar chart of departure times vs duration
- Line chart of duration trends over time

**Trip History**
- Filterable list (date range, transport mode)
- Each trip card shows: date, duration, distance, speed
- Swipe actions: delete, edit
- Search by location

**Settings**
- Saved locations (Home, Work with addresses)
- Default transport mode picker
- Distance unit toggle (km/miles)
- Export data button
- About section

#### 3.3 Battery Optimization Checklist
- ✅ Remove continuous 1-second timer updates → Change to 10-second intervals
- ✅ Cancel timer coroutine when app goes to background
- ✅ Use `lifecycleScope` instead of `viewModelScope` for UI updates
- ✅ Implement `onPause()` / `onResume()` in MainActivity to stop/start timer
- ✅ No location tracking or GPS usage
- ✅ No WorkManager or background services
- ✅ Manual distance input only (no continuous location polling)

---

## 📊 AI Analysis Preparation

### Data Requirements for AI Analysis
After 3 months of data collection, you'll need:

1. **Minimum Dataset**: 60-90 trips (1 trip per day × 90 days)
2. **Key Features for ML Model**:
   - Departure time (hour of day)
   - Day of week
   - Duration (target variable)
   - Pauses count and duration
   - Transport mode
   - Distance (if available)
   - Weather data (optional, can be added via API later)

### Analysis Questions to Answer
1. **What time should I leave to minimize commute time?**
   - Simple: Average duration by departure hour
   - Advanced: ML model predicting duration based on departure time + day of week

2. **Does day of week matter?**
   - Compare Monday vs Friday commute times
   - Identify patterns (e.g., "Fridays at 4 PM are 20% slower")

3. **How do stops/delays affect total time?**
   - Correlation between pause count and total duration
   - Is it better to leave earlier and stop for coffee, or leave later?

4. **Transport mode efficiency**
   - Compare different modes (car vs bike vs transit)
   - Speed vs reliability trade-offs

### AI Integration (Post-MVP)
```kotlin
// Future: AI prediction endpoint
class GetOptimalDepartureTimeUseCase @Inject constructor(
    private val repository: CommuteRepository,
    private val aiPredictionService: AIPredictionService  // Ktor client to your AI service
) {
    suspend operator fun invoke(targetArrivalTime: LocalTime): PredictedDeparture {
        val historicalData = repository.getAllCompletedSessions()

        // Call your AI model (could be local TensorFlow Lite or remote API)
        return aiPredictionService.predictOptimalDeparture(
            targetArrival = targetArrivalTime,
            historicalData = historicalData,
            dayOfWeek = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek
        )
    }
}

data class PredictedDeparture(
    val recommendedDepartureTime: LocalTime,
    val estimatedDuration: Int,
    val confidencePercent: Int,
    val reasoning: String  // e.g., "Based on 23 similar trips, you'll arrive on time"
)
```

---

## 🧪 Testing Strategy

### Unit Tests
```kotlin
// TrackingViewModelTest.kt (already exists, enhance it)
class TrackingViewModelTest {
    @Test
    fun `pause trip should stop timer and update status`() = runTest {
        // Given: active trip
        viewModel.startTrip()
        advanceTimeBy(5.minutes)

        // When: pause
        viewModel.pauseTrip()

        // Then: timer stopped, status = PAUSED
        assertTrue(viewModel.uiState.value.isPaused)
        assertEquals(5, viewModel.uiState.value.elapsedMinutes)
    }

    @Test
    fun `resume trip should exclude paused time from total duration`() = runTest {
        // Given: trip with 5 min active, 3 min paused
        viewModel.startTrip()
        advanceTimeBy(5.minutes)
        viewModel.pauseTrip()
        advanceTimeBy(3.minutes)  // Paused time
        viewModel.resumeTrip()
        advanceTimeBy(2.minutes)

        // When: end trip
        viewModel.endTrip("Office", 10.0)

        // Then: total duration = 5 + 2 = 7 minutes (excludes 3 min pause)
        assertEquals(7, viewModel.uiState.value.activeSession?.durationMinutes)
        assertEquals(3, viewModel.uiState.value.activeSession?.pausedMinutes)
    }
}
```

### Integration Tests
- Room database CRUD operations
- Use case orchestration (start → pause → resume → end)
- Data export to CSV

### UI Tests
```kotlin
@Test
fun `clicking pause button should show paused state`() {
    composeTestRule.setContent {
        TrackingScreen()
    }

    // Start trip
    composeTestRule.onNodeWithText("Start Trip").performClick()

    // Pause trip
    composeTestRule.onNodeWithText("PAUSE").performClick()

    // Verify paused state
    composeTestRule.onNodeWithText("PAUSED").assertExists()
    composeTestRule.onNodeWithText("RESUME").assertExists()
}
```

---

## 📦 Dependencies to Add

```kotlin
// build.gradle.kts (app module)
dependencies {
    // Charts for statistics
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // Or use Compose charts
    implementation("com.patrykandpatrick.vico:compose:1.13.1")

    // CSV export
    implementation("com.opencsv:opencsv:5.8")

    // (Optional) TensorFlow Lite for on-device AI
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
}
```

---

## 🚀 Development Roadmap

### Week 1: Pause/Resume Core Logic
- [ ] Add `pausedMinutes` and `pauseCount` fields to `CommuteSession`
- [ ] Create `PauseSessionUseCase` and `ResumeSessionUseCase`
- [ ] Update `CommuteRepository` with pause methods
- [ ] Implement pause tracking in `TrackingViewModel`
- [ ] Add Room migration for new database fields

### Week 2: Tracking UI Implementation
- [ ] Apply dark green theme colors to app
- [ ] Redesign `TrackingScreen` with PAUSE/RESUME buttons
- [ ] Add monospace timer display
- [ ] Implement status chips (ACTIVE/PAUSED)
- [ ] Show pause count in active trip card
- [ ] Simplify timer updates (10-second interval)

### Week 3: Statistics Foundation
- [ ] Create `DepartureTimeStats` and related models
- [ ] Implement `GetDepartureTimeAnalysisUseCase`
- [ ] Add statistics calculation logic
- [ ] Update `StatisticsViewModel` with new data

### Week 4: Statistics UI & Charts
- [ ] Design "Optimal Departure Time" card
- [ ] Implement departure time bar chart (using Vico or MPAndroidChart)
- [ ] Add duration trend line chart
- [ ] Create weekly breakdown table

### Week 5: Data Export & Polish
- [ ] Implement CSV export functionality
- [ ] Add "Export for AI Analysis" button
- [ ] Test data export with sample data
- [ ] Create share intent for CSV file

### Week 6: All Stitch Screens
- [ ] Implement complete Dashboard screen
- [ ] Finalize Active Session screen
- [ ] Complete History list with filters
- [ ] Build Settings screen with saved locations

### Week 7: Battery Optimization & Testing
- [ ] Optimize timer updates (on-demand only)
- [ ] Remove unnecessary background work
- [ ] Test battery drain (compare before/after)
- [ ] Write unit tests for pause logic
- [ ] Write UI tests for tracking flow

### Week 8: Beta Testing & Refinement
- [ ] Internal testing (use app daily for 1 week)
- [ ] Fix bugs found during testing
- [ ] Polish animations and transitions
- [ ] Prepare for 3-month data collection phase

---

## 📈 Post-MVP: AI Analysis Phase (Month 4+)

After collecting 3 months of data:

### Step 1: Data Exploration
- Export CSV and load into Python/Jupyter Notebook
- Visualize distributions (departure times, durations, day of week)
- Identify correlations (e.g., departure time vs duration)

### Step 2: Simple Analysis
```python
import pandas as pd
import matplotlib.pyplot as plt

# Load data
df = pd.read_csv('commute_data.csv')
df['departure_hour'] = pd.to_datetime(df['departure_time']).dt.hour

# Group by departure hour
analysis = df.groupby('departure_hour').agg({
    'duration_minutes': ['mean', 'std', 'count'],
    'paused_minutes': 'mean',
    'avg_speed_kmh': 'mean'
})

# Find optimal time
optimal_hour = analysis['duration_minutes']['mean'].idxmin()
print(f"Optimal departure time: {optimal_hour}:00")
print(f"Average duration: {analysis.loc[optimal_hour, ('duration_minutes', 'mean')]:.1f} min")

# Plot
analysis['duration_minutes']['mean'].plot(kind='bar')
plt.xlabel('Departure Hour')
plt.ylabel('Average Duration (minutes)')
plt.title('Commute Duration by Departure Time')
plt.show()
```

### Step 3: ML Model (Advanced)
```python
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split

# Features
X = df[['departure_hour', 'day_of_week_encoded', 'transport_mode_encoded']]
y = df['duration_minutes']

# Train model
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)
model = RandomForestRegressor(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

# Predict optimal departure time for next Monday
predictions = []
for hour in range(6, 10):  # 6 AM to 10 AM
    pred = model.predict([[hour, 0, 0]])  # Monday, driving
    predictions.append((hour, pred[0]))

optimal = min(predictions, key=lambda x: x[1])
print(f"AI recommends leaving at {optimal[0]}:00 (predicted {optimal[1]:.0f} min)")
```

### Step 4: Integrate Back into App
- Add `AIPredictionService` with Ktor
- Call your model (hosted on cloud or use TFLite)
- Show recommendations in Statistics screen
- Add "What if?" simulator: "If I leave at 7:30 AM tomorrow, how long will it take?"

---

## 🎯 Success Metrics

### Phase 1 Success Criteria
- ✅ Can start/stop a trip with one button press
- ✅ Can pause/resume trip and see pause count
- ✅ Timer excludes paused time from total duration
- ✅ App uses <2% battery during 30-minute trip

### Phase 2 Success Criteria
- ✅ Statistics screen shows optimal departure time (after 10+ trips)
- ✅ Can export data to CSV for analysis
- ✅ Charts display departure time trends

### Phase 3 Success Criteria
- ✅ All 6 Stitch screens implemented
- ✅ Dark green theme applied throughout app
- ✅ App feels fast and responsive
- ✅ Successfully collected 90 days of commute data

### AI Analysis Success
- ✅ Identified optimal departure time with statistical confidence
- ✅ Quantified time savings (e.g., "Leaving at 7:15 AM saves 12 minutes vs 8:00 AM")
- ✅ Discovered patterns (e.g., "Friday mornings have 30% less traffic")

---

## 🔮 Future Enhancements (Post-AI Analysis)

1. **Smart Notifications**
   - "Leave now to arrive by 9:00 AM based on historical data"
   - "Traffic is heavier than usual today"

2. **Predictive Insights**
   - "Tomorrow is predicted to have heavy traffic, leave 15 min earlier"
   - Integrate with weather API for weather-based predictions

3. **Social Features**
   - Compare with other users' commutes (anonymized)
   - "Your commute is 20% faster than average for your route"

4. **Route Tracking (Optional)**
   - Add GPS tracking as opt-in feature
   - Display actual route taken on map (post-trip)
   - Compare different routes to same destination

5. **Wear OS Widget**
   - Quick start/stop from smartwatch
   - Show elapsed time on watch face

6. **Integration with Calendar**
   - Auto-suggest departure time based on next calendar event
   - "You have a 10 AM meeting, leave by 9:15 AM"

---

## 📝 Key Technical Decisions

### Why No Background Services?
- **Battery Life**: Background location tracking drains battery significantly
- **User Goal**: User only needs time tracking, not GPS trail
- **Simplicity**: Easier to implement, test, and maintain
- **Privacy**: No continuous location data collection

### Why Manual Distance Input?
- Accurate GPS distance requires continuous tracking
- User can estimate distance (it's usually the same route daily)
- Alternatively: use GPS only at start/end to calculate straight-line distance
- Focus is on time analysis, not precise distance

### Why 10-Second Timer Updates?
- User doesn't need second-by-second precision
- 10-second updates are imperceptible to user
- Saves CPU cycles and battery
- Can switch to 1-second updates when app is in foreground if needed

### Why CSV Export Instead of Cloud Sync?
- **MVP Speed**: Faster to implement
- **Privacy**: Data stays on device until user explicitly shares
- **Flexibility**: User can analyze data in any tool (Excel, Python, R)
- **Future**: Can add cloud sync later with user opt-in

---

## 🎨 Design Principles

1. **Clarity Over Cleverness**: Large buttons, clear labels, obvious actions
2. **Speed Over Features**: Fast app launch, instant feedback, no loading spinners
3. **Battery Over Beauty**: OLED-friendly colors, minimal animations, efficient updates
4. **Privacy Over Convenience**: Data stays local, explicit export only
5. **Actionable Over Informative**: Show insights that lead to action ("Leave at 7:15 AM"), not just stats

---

## 📚 References & Resources

### Architecture
- [Clean Architecture Guide](https://developer.android.com/topic/architecture)
- [Jetpack Compose Best Practices](https://developer.android.com/jetpack/compose/architecture)

### Battery Optimization
- [Android Battery Optimization](https://developer.android.com/topic/performance/vitals/battery)
- [WorkManager vs Foreground Service](https://developer.android.com/guide/background/persistent)

### Data Analysis
- [Pandas Documentation](https://pandas.pydata.org/docs/)
- [Scikit-learn ML Guide](https://scikit-learn.org/stable/user_guide.html)
- [Time Series Analysis](https://www.tensorflow.org/tutorials/structured_data/time_series)

### UI/UX
- [Material Design 3](https://m3.material.io/)
- [Compose Animation Guide](https://developer.android.com/jetpack/compose/animation)
- [OLED Dark Mode Best Practices](https://material.io/design/color/dark-theme.html)

---

## ✅ Pre-Launch Checklist

- [ ] All 6 screens implemented and tested
- [ ] Pause/resume functionality works correctly
- [ ] Timer excludes paused time accurately
- [ ] Dark green theme applied throughout
- [ ] Data export generates valid CSV
- [ ] Battery usage tested (target: <3% per 30-min trip)
- [ ] Unit tests written for core logic
- [ ] UI tests for critical flows
- [ ] Crash reporting enabled (Firebase Crashlytics)
- [ ] Analytics enabled (Firebase Analytics or equivalent)
- [ ] Privacy policy prepared (even for personal use)
- [ ] Backup strategy (local export before data loss)

---

## 🤝 Contributing & Maintenance

Since this is a personal project, future maintenance should focus on:

1. **Data Quality**: Regularly review exported data for anomalies
2. **Bug Fixes**: Fix issues as they're discovered during daily use
3. **Model Improvements**: Retrain AI model quarterly with new data
4. **Feature Requests**: Add features based on insights from data analysis

---

## 📞 Support & Feedback

**Developer**: Yudistiro Saputro
**GitHub**: [@yudistirosaputro](https://github.com/yudistirosaputro)
**Project**: [CommuteTrack](https://github.com/yudistirosaputro/commuteTrack)

---

**Last Updated**: February 2026
**Version**: 1.0.0 (Plan Document)
**Status**: Ready for Implementation 🚀
