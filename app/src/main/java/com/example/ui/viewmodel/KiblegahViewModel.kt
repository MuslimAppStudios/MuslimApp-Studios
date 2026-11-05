package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Ayah
import com.example.data.model.City
import com.example.data.model.CuratedAyahs
import com.example.data.model.CuratedHadiths
import com.example.data.model.DailyHabit
import com.example.data.model.DailyPrayerTimes
import com.example.data.model.DefaultCities
import com.example.data.model.Hadith
import com.example.data.model.KazaCounter
import com.example.data.model.Note
import com.example.data.repository.HabitRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.SpiritualRepository
import com.example.data.util.PrayerTimeCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class KiblegahViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val noteRepository = NoteRepository(database.noteDao())
    private val habitRepository = HabitRepository(database.dailyHabitDao())
    private val spiritualRepository = SpiritualRepository()
    private val weatherRepository = com.example.data.repository.WeatherRepository()

    // --- City & Prayer Times ---
    private val initialCity = com.example.data.model.Turkey81Provinces.firstOrNull { it.id == "34" } ?: DefaultCities.first()
    private val _selectedCity = MutableStateFlow(initialCity)
    val selectedCity: StateFlow<City> = _selectedCity.asStateFlow()

    private val _prayerTimes = MutableStateFlow(PrayerTimeCalculator.calculate(initialCity))
    val prayerTimes: StateFlow<DailyPrayerTimes> = _prayerTimes.asStateFlow()

    // --- Live Weather ---
    private val _liveWeather = MutableStateFlow<com.example.data.model.WeatherInfo?>(null)
    val liveWeather: StateFlow<com.example.data.model.WeatherInfo?> = _liveWeather.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    private val _all81Weather = MutableStateFlow(weatherRepository.getAll81Weather())
    val all81Weather: StateFlow<List<Pair<City, com.example.data.model.WeatherInfo>>> = _all81Weather.asStateFlow()

    // --- Audio Alert Settings ---
    private val _alertSettings = MutableStateFlow(com.example.data.model.GlobalAlertSettings())
    val alertSettings: StateFlow<com.example.data.model.GlobalAlertSettings> = _alertSettings.asStateFlow()

    // --- Daily Ayah & Hadith ---
    private val _ayahIndex = MutableStateFlow(0)
    val ayahIndex: StateFlow<Int> = _ayahIndex.asStateFlow()

    private val _hadithIndex = MutableStateFlow(0)
    val hadithIndex: StateFlow<Int> = _hadithIndex.asStateFlow()

    val currentAyah: StateFlow<Ayah> = _ayahIndex.combine(_selectedCity) { idx, _ ->
        val safeIdx = idx.coerceIn(0, CuratedAyahs.lastIndex)
        CuratedAyahs[safeIdx]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CuratedAyahs.first())

    val currentHadith: StateFlow<Hadith> = _hadithIndex.combine(_selectedCity) { idx, _ ->
        val safeIdx = idx.coerceIn(0, CuratedHadiths.lastIndex)
        CuratedHadiths[safeIdx]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CuratedHadiths.first())

    // --- Daily Habit & Tracker ---
    private val todayDateKey = habitRepository.getTodayDateKey()
    private val _todayHabit = MutableStateFlow(DailyHabit(dateKey = todayDateKey))
    val todayHabit: StateFlow<DailyHabit> = _todayHabit.asStateFlow()

    private val _kazaCounter = MutableStateFlow(KazaCounter())
    val kazaCounter: StateFlow<KazaCounter> = _kazaCounter.asStateFlow()

    // --- Tasbih / Zikirmatik ---
    private val _dhikrCount = MutableStateFlow(0)
    val dhikrCount: StateFlow<Int> = _dhikrCount.asStateFlow()

    private val _dhikrTarget = MutableStateFlow(33)
    val dhikrTarget: StateFlow<Int> = _dhikrTarget.asStateFlow()

    private val _activeDhikrName = MutableStateFlow("Sübhanallâh")
    val activeDhikrName: StateFlow<String> = _activeDhikrName.asStateFlow()

    // --- Notes ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tümü")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val notes: StateFlow<List<Note>> = combine(
        noteRepository.allNotes,
        _searchQuery,
        _selectedCategory
    ) { allNotes, query, category ->
        allNotes.filter { note ->
            val matchesCategory = (category == "Tümü" || note.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications / SnackBar Events
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        // Initialize today's ayah and hadith based on calendar
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        _ayahIndex.value = dayOfYear % CuratedAyahs.size
        _hadithIndex.value = (dayOfYear + 3) % CuratedHadiths.size

        // Observe stored habits
        viewModelScope.launch {
            habitRepository.getTodayHabit().collect { stored ->
                if (stored != null) {
                    _todayHabit.value = stored
                }
            }
        }

        // Observe kaza counters
        viewModelScope.launch {
            habitRepository.getKazaCounter().collect { stored ->
                if (stored != null) {
                    _kazaCounter.value = stored
                }
            }
        }

        // Start countdown ticker for prayer times (every second)
        viewModelScope.launch {
            while (isActive) {
                _prayerTimes.value = PrayerTimeCalculator.calculate(_selectedCity.value)
                delay(1000)
            }
        }

        // Fetch live weather for initial city
        fetchWeather(initialCity)
    }

    fun fetchWeather(city: City) {
        viewModelScope.launch {
            _isWeatherLoading.value = true
            try {
                val weather = weatherRepository.getLiveWeather(city)
                _liveWeather.value = weather
                _all81Weather.value = weatherRepository.getAll81Weather()
            } finally {
                _isWeatherLoading.value = false
            }
        }
    }

    fun refreshWeather() {
        fetchWeather(_selectedCity.value)
        viewModelScope.launch {
            _snackbarEvent.emit("Hava durumu güncellendi.")
        }
    }

    fun selectCity(city: City) {
        _selectedCity.value = city
        _prayerTimes.value = PrayerTimeCalculator.calculate(city)
        fetchWeather(city)
        viewModelScope.launch {
            _snackbarEvent.emit("${city.name} seçildi.")
        }
    }

    // --- Audio Alert Controls ---
    fun toggleMasterAudio() {
        val current = _alertSettings.value
        val newState = !current.isMasterAudioEnabled
        _alertSettings.value = current.copy(isMasterAudioEnabled = newState)
        viewModelScope.launch {
            _snackbarEvent.emit(if (newState) "Sesli ezan & vakit uyarıları açıldı." else "Sesli uyarılar sessize alındı.")
        }
    }

    fun setDefaultSound(sound: com.example.ui.util.AlertSound) {
        val current = _alertSettings.value
        val updated = current.configs.mapValues { it.value.copy(alertSound = sound) }
        _alertSettings.value = current.copy(defaultSound = sound, configs = updated)
        viewModelScope.launch {
            _snackbarEvent.emit("Uyarı sesi: ${sound.displayName}")
        }
    }

    fun togglePrayerAlert(prayerType: com.example.data.model.PrayerType) {
        val current = _alertSettings.value
        val existing = current.configs[prayerType] ?: com.example.data.model.PrayerAlertConfig(prayerType = prayerType)
        val updated = current.configs.toMutableMap()
        updated[prayerType] = existing.copy(isEnabled = !existing.isEnabled)
        _alertSettings.value = current.copy(configs = updated)
    }

    fun setPrayerAlertSound(prayerType: com.example.data.model.PrayerType, sound: com.example.ui.util.AlertSound) {
        val current = _alertSettings.value
        val existing = current.configs[prayerType] ?: com.example.data.model.PrayerAlertConfig(prayerType = prayerType)
        val updated = current.configs.toMutableMap()
        updated[prayerType] = existing.copy(alertSound = sound)
        _alertSettings.value = current.copy(configs = updated)
    }

    // --- Ayah & Hadith Navigation ---
    fun nextAyah() {
        _ayahIndex.value = (_ayahIndex.value + 1) % CuratedAyahs.size
    }

    fun prevAyah() {
        _ayahIndex.value = if (_ayahIndex.value - 1 < 0) CuratedAyahs.lastIndex else _ayahIndex.value - 1
    }

    fun randomAyah() {
        _ayahIndex.value = (CuratedAyahs.indices).random()
    }

    fun nextHadith() {
        _hadithIndex.value = (_hadithIndex.value + 1) % CuratedHadiths.size
    }

    fun prevHadith() {
        _hadithIndex.value = if (_hadithIndex.value - 1 < 0) CuratedHadiths.lastIndex else _hadithIndex.value - 1
    }

    fun randomHadith() {
        _hadithIndex.value = (CuratedHadiths.indices).random()
    }

    // --- Habit Tracking ---
    fun togglePrayerCheck(prayer: String) {
        val current = _todayHabit.value
        val updated = when (prayer.lowercase()) {
            "sabah" -> current.copy(sabahNamaz = !current.sabahNamaz)
            "öğle", "ogle" -> current.copy(ogleNamaz = !current.ogleNamaz)
            "ikindi" -> current.copy(ikindiNamaz = !current.ikindiNamaz)
            "akşam", "aksam" -> current.copy(aksamNamaz = !current.aksamNamaz)
            "yatsı", "yatsi" -> current.copy(yatsiNamaz = !current.yatsiNamaz)
            "vitir" -> current.copy(vitirNamaz = !current.vitirNamaz)
            else -> current
        }
        _todayHabit.value = updated
        viewModelScope.launch {
            habitRepository.saveHabit(updated)
        }
    }

    fun setQuranPages(pages: Int) {
        val updated = _todayHabit.value.copy(quranPagesRead = pages.coerceAtLeast(0))
        _todayHabit.value = updated
        viewModelScope.launch {
            habitRepository.saveHabit(updated)
        }
    }

    // --- Kaza Namazı Counter ---
    fun updateKaza(type: String, delta: Int) {
        val current = _kazaCounter.value
        val updated = when (type.lowercase()) {
            "sabah" -> current.copy(sabah = (current.sabah + delta).coerceAtLeast(0))
            "öğle", "ogle" -> current.copy(ogle = (current.ogle + delta).coerceAtLeast(0))
            "ikindi" -> current.copy(ikindi = (current.ikindi + delta).coerceAtLeast(0))
            "akşam", "aksam" -> current.copy(aksam = (current.aksam + delta).coerceAtLeast(0))
            "yatsı", "yatsi" -> current.copy(yatsi = (current.yatsi + delta).coerceAtLeast(0))
            "vitir" -> current.copy(vitir = (current.vitir + delta).coerceAtLeast(0))
            "oruc", "oruç" -> current.copy(oruc = (current.oruc + delta).coerceAtLeast(0))
            else -> current
        }
        _kazaCounter.value = updated
        viewModelScope.launch {
            habitRepository.saveKazaCounter(updated)
        }
    }

    // --- Tasbih / Zikirmatik ---
    fun incrementDhikr(): Boolean {
        val current = _dhikrCount.value + 1
        _dhikrCount.value = current
        val total = _todayHabit.value.totalDhikr + 1
        _todayHabit.value = _todayHabit.value.copy(totalDhikr = total)
        viewModelScope.launch {
            habitRepository.saveHabit(_todayHabit.value)
        }
        val target = _dhikrTarget.value
        return target > 0 && current % target == 0
    }

    fun resetDhikr() {
        _dhikrCount.value = 0
    }

    fun setDhikrPreset(name: String, target: Int) {
        _activeDhikrName.value = name
        _dhikrTarget.value = target
        _dhikrCount.value = 0
    }

    // --- Notes CRUD ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addNote(title: String, content: String, category: String, isPinned: Boolean = false, colorIndex: Int = 0) {
        viewModelScope.launch {
            val newNote = Note(
                title = title.trim(),
                content = content.trim(),
                category = category,
                isPinned = isPinned,
                colorIndex = colorIndex,
                createdAt = System.currentTimeMillis()
            )
            noteRepository.insert(newNote)
            _snackbarEvent.emit("Not başarıyla eklendi.")
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteRepository.update(note)
            _snackbarEvent.emit("Not güncellendi.")
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.delete(note)
            _snackbarEvent.emit("Not silindi.")
        }
    }

    fun togglePinNote(note: Note) {
        viewModelScope.launch {
            noteRepository.togglePin(note)
        }
    }

    fun saveAyahToNotes(ayah: Ayah) {
        viewModelScope.launch {
            val note = Note(
                title = "${ayah.surahName}, ${ayah.ayahNumber}. Ayet",
                content = "${ayah.arabicText}\n\n\"${ayah.turkishTranslation}\"\n\nTeveccüh & Not: ${ayah.tafsirNote}",
                category = "Ayet Notu",
                isPinned = false,
                colorIndex = 1
            )
            noteRepository.insert(note)
            _snackbarEvent.emit("Ayet notlarınıza kaydedildi!")
        }
    }

    fun saveHadithToNotes(hadith: Hadith) {
        viewModelScope.launch {
            val note = Note(
                title = "Hadis-i Şerif: ${hadith.topic}",
                content = "\"${hadith.text}\"\n\nKaynak: ${hadith.source}\nRavi: ${hadith.narrator}\n\nTefekkür: ${hadith.reflection}",
                category = "Hadis Notu",
                isPinned = false,
                colorIndex = 2
            )
            noteRepository.insert(note)
            _snackbarEvent.emit("Hadis notlarınıza kaydedildi!")
        }
    }
}
