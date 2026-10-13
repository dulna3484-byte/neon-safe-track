package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DeviceLocation
import com.example.data.LocationHistoryEntity
import com.example.data.LocationServiceHelper
import com.example.data.SafeTrackDatabase
import com.example.data.SafeTrackRepository
import com.example.data.SecurityHelper
import com.example.data.SharingSessionEntity
import com.example.data.TrustedContactEntity
import com.example.data.UserEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppScreen {
    WELCOME,
    AUTH,
    HOME,
    SHARE,
    JOIN,
    MAP,
    PEOPLE,
    HISTORY,
    SETTINGS,
    PRIVACY_POLICY,
    TERMS_OF_SERVICE
}

enum class AuthMode {
    LOGIN,
    REGISTER
}

data class UiFeedback(
    val message: String,
    val isError: Boolean = false,
    val id: Long = System.currentTimeMillis()
)

class SafeTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SafeTrackRepository

    init {
        val database = SafeTrackDatabase.getInstance(application)
        repository = SafeTrackRepository(database.dao())
    }

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.WELCOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _previousScreen = MutableStateFlow(AppScreen.HOME)

    // Auth
    private val _authMode = MutableStateFlow(AuthMode.REGISTER)
    val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

    val activeUser: StateFlow<UserEntity?> = repository.activeUserFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    // Active session where CURRENT USER is the sharer
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeSharingSession: StateFlow<SharingSessionEntity?> = activeUser.flatMapLatest { user ->
        if (user != null) repository.getActiveSessionFlow(user.id) else flowOf(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Contacts
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val trustedContacts: StateFlow<List<TrustedContactEntity>> = activeUser.flatMapLatest { user ->
        if (user != null) repository.getContactsFlow(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Location History
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val locationHistory: StateFlow<List<LocationHistoryEntity>> = activeUser.flatMapLatest { user ->
        if (user != null) repository.getLocationHistoryFlow(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Viewing someone else's shared location
    private val _joinedSession = MutableStateFlow<SharingSessionEntity?>(null)
    val joinedSession: StateFlow<SharingSessionEntity?> = _joinedSession.asStateFlow()

    // Current Device Location
    private val _currentLocation = MutableStateFlow(
        DeviceLocation(
            latitude = 37.7749,
            longitude = -122.4194,
            accuracy = 4.2f,
            address = "Market St, San Francisco, CA"
        )
    )
    val currentLocation: StateFlow<DeviceLocation> = _currentLocation.asStateFlow()

    // Permissions
    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    // UI Feedback
    private val _feedback = MutableStateFlow<UiFeedback?>(null)
    val feedback: StateFlow<UiFeedback?> = _feedback.asStateFlow()

    // Active share configuration
    private val _selectedDurationMinutes = MutableStateFlow(60) // 15, 60, 480, -1
    val selectedDurationMinutes: StateFlow<Int> = _selectedDurationMinutes.asStateFlow()

    private val _selectedContact = MutableStateFlow<TrustedContactEntity?>(null)
    val selectedContact: StateFlow<TrustedContactEntity?> = _selectedContact.asStateFlow()

    private val _generatedCode = MutableStateFlow(SecurityHelper.generateSharingCode())
    val generatedCode: StateFlow<String> = _generatedCode.asStateFlow()

    // Join Code input
    private val _joinCodeInput = MutableStateFlow("")
    val joinCodeInput: StateFlow<String> = _joinCodeInput.asStateFlow()

    private val _isJoining = MutableStateFlow(false)
    val isJoining: StateFlow<Boolean> = _isJoining.asStateFlow()

    private var locationPeriodicJob: Job? = null

    init {
        checkPermission()
        refreshLocation()
        // If a user is already logged in, navigate straight to Home
        viewModelScope.launch {
            val user = repository.getActiveUserDirect()
            if (user != null) {
                _currentScreen.value = AppScreen.HOME
            }
        }
        startPeriodicLocationUpdates()
    }

    fun navigateTo(screen: AppScreen) {
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = screen
    }

    fun navigateBack() {
        val prev = _previousScreen.value
        _currentScreen.value = if (prev != _currentScreen.value) prev else AppScreen.HOME
    }

    fun setAuthMode(mode: AuthMode) {
        _authMode.value = mode
        navigateTo(AppScreen.AUTH)
    }

    fun checkPermission() {
        val granted = LocationServiceHelper.hasLocationPermission(getApplication())
        _hasLocationPermission.value = granted
    }

    fun onPermissionResult(granted: Boolean) {
        _hasLocationPermission.value = granted
        if (granted) {
            refreshLocation()
            showFeedback("Location permission granted.", isError = false)
        } else {
            showFeedback("Location permission denied. SafeTrack requires GPS permission to share location.", isError = true)
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            val loc = LocationServiceHelper.getCurrentLocation(getApplication())
            _currentLocation.value = loc

            val user = activeUser.value
            if (user != null && user.locationHistoryEnabled) {
                repository.recordLocationHistory(user.id, loc.latitude, loc.longitude, loc.accuracy, loc.address)
            }

            // If actively sharing, update the session
            val active = activeSharingSession.value
            if (active != null && active.sharingStatus == "ACTIVE") {
                repository.updateLocationForActiveSession(
                    active.sessionId,
                    loc.latitude,
                    loc.longitude,
                    loc.accuracy,
                    loc.address
                )
            }
        }
    }

    private fun startPeriodicLocationUpdates() {
        locationPeriodicJob?.cancel()
        locationPeriodicJob = viewModelScope.launch {
            while (true) {
                delay(15_000) // Update every 15 seconds while active
                val active = activeSharingSession.value
                if (active != null && active.sharingStatus == "ACTIVE") {
                    val loc = LocationServiceHelper.getCurrentLocation(getApplication())
                    // Add subtle realistic delta if demo coordinates
                    val deltaLat = (Random.nextDouble(-0.00015, 0.00015))
                    val deltaLng = (Random.nextDouble(-0.00015, 0.00015))
                    val newLat = loc.latitude + deltaLat
                    val newLng = loc.longitude + deltaLng
                    _currentLocation.value = loc.copy(latitude = newLat, longitude = newLng)
                    repository.updateLocationForActiveSession(
                        active.sessionId,
                        newLat,
                        newLng,
                        loc.accuracy,
                        loc.address
                    )
                }

                // If joined a session, refresh the joined session from database
                val joined = _joinedSession.value
                if (joined != null) {
                    val res = repository.validateAndJoinSession(joined.sessionId)
                    if (res.isSuccess) {
                        _joinedSession.value = res.getOrNull()
                    }
                }
            }
        }
    }

    // --- Authentication ---
    fun register(name: String, emailOrPhone: String, password: String, termsAccepted: Boolean) {
        if (!termsAccepted) {
            showFeedback("Please agree to the Terms & Privacy Policy to continue.", isError = true)
            return
        }
        if (name.isBlank() || emailOrPhone.isBlank() || password.isBlank()) {
            showFeedback("Please fill out all required fields.", isError = true)
            return
        }
        if (password.length < 6) {
            showFeedback("Password must be at least 6 characters.", isError = true)
            return
        }

        viewModelScope.launch {
            val result = repository.register(name, emailOrPhone, password)
            if (result.isSuccess) {
                showFeedback("Account created successfully! Welcome to SafeTrack.", isError = false)
                refreshLocation()
                _currentScreen.value = AppScreen.HOME
            } else {
                showFeedback(result.exceptionOrNull()?.message ?: "Failed to create account.", isError = true)
            }
        }
    }

    fun login(emailOrPhone: String, password: String) {
        if (emailOrPhone.isBlank() || password.isBlank()) {
            showFeedback("Please enter your email/phone and password.", isError = true)
            return
        }
        viewModelScope.launch {
            val result = repository.login(emailOrPhone, password)
            if (result.isSuccess) {
                showFeedback("Logged in successfully.", isError = false)
                refreshLocation()
                _currentScreen.value = AppScreen.HOME
            } else {
                showFeedback(result.exceptionOrNull()?.message ?: "Login failed.", isError = true)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _joinedSession.value = null
            _currentScreen.value = AppScreen.WELCOME
            showFeedback("You have been logged out.", isError = false)
        }
    }

    fun deleteAccount() {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.deleteAccount(user.id)
            _joinedSession.value = null
            _currentScreen.value = AppScreen.WELCOME
            showFeedback("Your account and all associated data have been permanently deleted.", isError = false)
        }
    }

    // --- Sharing Location ---
    fun setSelectedDuration(durationMinutes: Int) {
        _selectedDurationMinutes.value = durationMinutes
    }

    fun setSelectedContact(contact: TrustedContactEntity?) {
        _selectedContact.value = contact
    }

    fun regenerateSharingCode() {
        _generatedCode.value = SecurityHelper.generateSharingCode()
    }

    fun startSharing() {
        val user = activeUser.value ?: return
        if (!hasLocationPermission.value) {
            showFeedback("Location permission is required to start sharing.", isError = true)
            return
        }
        val loc = currentLocation.value
        viewModelScope.launch {
            val session = repository.startSharingSession(
                ownerUserId = user.id,
                ownerName = user.name,
                invitedContactId = _selectedContact.value?.id,
                invitedName = _selectedContact.value?.name,
                durationMinutes = _selectedDurationMinutes.value,
                currentLatitude = loc.latitude,
                currentLongitude = loc.longitude,
                accuracy = loc.accuracy,
                address = loc.address
            )
            _generatedCode.value = session.sessionId
            showFeedback("Location sharing started! Code: ${session.sessionId}", isError = false)
            _currentScreen.value = AppScreen.HOME
        }
    }

    fun stopSharing() {
        val user = activeUser.value ?: return
        val session = activeSharingSession.value ?: return
        viewModelScope.launch {
            repository.stopSharing(session.sessionId, user.id)
            showFeedback("Location sharing stopped immediately. Your location is private.", isError = false)
        }
    }

    // --- Join / Receive Location ---
    fun setJoinCodeInput(code: String) {
        _joinCodeInput.value = code.uppercase()
    }

    fun joinSharingSession() {
        val code = _joinCodeInput.value.trim().uppercase()
        if (code.isBlank()) {
            showFeedback("Please enter a valid sharing code.", isError = true)
            return
        }
        _isJoining.value = true
        viewModelScope.launch {
            val result = repository.validateAndJoinSession(code)
            _isJoining.value = false
            if (result.isSuccess) {
                _joinedSession.value = result.getOrNull()
                showFeedback("Connected to ${result.getOrNull()?.ownerName}'s shared location.", isError = false)
                _currentScreen.value = AppScreen.MAP
            } else {
                showFeedback(result.exceptionOrNull()?.message ?: "Unable to join session.", isError = true)
            }
        }
    }

    fun stopViewingSharedSession() {
        _joinedSession.value = null
        showFeedback("Stopped viewing shared location.", isError = false)
        if (_currentScreen.value == AppScreen.MAP) {
            _currentScreen.value = AppScreen.HOME
        }
    }

    // --- Contacts ---
    fun addContact(name: String, phoneOrEmail: String, relationship: String) {
        val user = activeUser.value ?: return
        if (name.isBlank() || phoneOrEmail.isBlank()) {
            showFeedback("Please provide a name and contact info.", isError = true)
            return
        }
        viewModelScope.launch {
            repository.addContact(user.id, name, phoneOrEmail, relationship)
            showFeedback("Added $name to trusted contacts.", isError = false)
        }
    }

    fun deleteContact(contactId: Long) {
        viewModelScope.launch {
            repository.deleteContact(contactId)
            showFeedback("Contact removed.", isError = false)
        }
    }

    // --- History ---
    fun toggleHistory(enabled: Boolean) {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.toggleLocationHistory(user.id, enabled)
            val msg = if (enabled) "Location history enabled." else "Location history paused."
            showFeedback(msg, isError = false)
        }
    }

    fun deleteHistoryEntry(historyId: Long) {
        viewModelScope.launch {
            repository.deleteHistoryEntry(historyId)
            showFeedback("Location entry deleted.", isError = false)
        }
    }

    fun clearAllHistory() {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.clearHistory(user.id)
            showFeedback("All location history permanently cleared.", isError = false)
        }
    }

    fun updateDefaultDuration(durationMinutes: Int) {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.updateDefaultDuration(user.id, durationMinutes)
            showFeedback("Default sharing duration updated.", isError = false)
        }
    }

    fun showFeedback(message: String, isError: Boolean = false) {
        _feedback.value = UiFeedback(message, isError)
    }

    fun clearFeedback() {
        _feedback.value = null
    }
}
