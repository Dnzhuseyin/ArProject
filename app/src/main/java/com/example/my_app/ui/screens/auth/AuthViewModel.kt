package com.example.my_app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_app.data.repository.AuthRepository
import com.example.my_app.data.repository.ProfileSetupData
import com.example.my_app.data.repository.SignUpData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    // Common fields
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    
    // Login/Register fields
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullName: String = "",
    val username: String = "",
    val acceptTerms: Boolean = false,
    
    // Password visibility
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    
    // Validation errors
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val fullNameError: String? = null,
    val usernameError: String? = null,
    
    // Profile setup fields
    val isAthlete: Boolean? = null,
    val hasSurgery: Boolean? = null,
    val surgeryDetails: String = "",
    val doctorName: String = "",
    val profileSetupCompleted: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isUserLoggedIn()
            _uiState.update { it.copy(isAuthenticated = isLoggedIn) }
        }
    }
    
    // Email and password updates
    fun updateEmail(email: String) {
        _uiState.update { 
            it.copy(
                email = email,
                emailError = null,
                errorMessage = null
            )
        }
    }
    
    fun updatePassword(password: String) {
        _uiState.update { 
            it.copy(
                password = password,
                passwordError = null,
                errorMessage = null
            )
        }
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { 
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }
    
    fun updateFullName(fullName: String) {
        _uiState.update { 
            it.copy(
                fullName = fullName,
                fullNameError = null,
                errorMessage = null
            )
        }
    }
    
    fun updateUsername(username: String) {
        _uiState.update { 
            it.copy(
                username = username.lowercase().replace(" ", "_"),
                usernameError = null,
                errorMessage = null
            )
        }
    }
    
    fun updateAcceptTerms(accept: Boolean) {
        _uiState.update { it.copy(acceptTerms = accept) }
    }
    
    // Password visibility toggles
    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }
    
    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }
    
    // Profile setup updates
    fun updateIsAthlete(isAthlete: Boolean) {
        _uiState.update { it.copy(isAthlete = isAthlete) }
    }
    
    fun updateHasSurgery(hasSurgery: Boolean) {
        _uiState.update { 
            it.copy(
                hasSurgery = hasSurgery,
                surgeryDetails = if (!hasSurgery) "" else it.surgeryDetails
            )
        }
    }
    
    fun updateSurgeryDetails(details: String) {
        _uiState.update { it.copy(surgeryDetails = details) }
    }
    
    fun updateDoctorName(doctorName: String) {
        _uiState.update { it.copy(doctorName = doctorName) }
    }
    
    // Authentication actions
    fun signInWithEmail() {
        val currentState = _uiState.value
        
        if (!validateLoginForm(currentState)) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            authRepository.signInWithEmailAndPassword(
                currentState.email,
                currentState.password
            ).fold(
                onSuccess = { result ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isAuthenticated = result.data != null,
                            errorMessage = result.errorMessage
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Giriş yapılırken hata oluştu"
                        )
                    }
                }
            )
        }
    }
    
    fun signUpWithEmail() {
        val currentState = _uiState.value
        
        if (!validateRegisterForm(currentState)) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val signUpData = SignUpData(
                email = currentState.email,
                password = currentState.password,
                username = currentState.username,
                fullName = currentState.fullName
            )
            
            authRepository.signUpWithEmailAndPassword(signUpData).fold(
                onSuccess = { result ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isAuthenticated = result.data != null,
                            errorMessage = result.errorMessage
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Kayıt olurken hata oluştu"
                        )
                    }
                }
            )
        }
    }
    
    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // TODO: Implement Google Sign-In with actual Google Auth SDK
            // For now, show a placeholder message
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    errorMessage = "Google ile giriş yakında eklenecek"
                )
            }
        }
    }
    
    fun signInWithFacebook() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // TODO: Implement Facebook Sign-In with actual Facebook SDK
            // For now, show a placeholder message
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    errorMessage = "Facebook ile giriş yakında eklenecek"
                )
            }
        }
    }
    
    fun forgotPassword() {
        val currentState = _uiState.value
        
        if (currentState.email.isBlank()) {
            _uiState.update { 
                it.copy(emailError = "Şifre sıfırlama için e-posta adresi gerekli")
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            authRepository.resetPassword(currentState.email).fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Şifre sıfırlama e-postası gönderildi"
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Şifre sıfırlama hatası"
                        )
                    }
                }
            )
        }
    }
    
    fun completeProfileSetup() {
        val currentState = _uiState.value
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val profileData = ProfileSetupData(
                isAthlete = currentState.isAthlete ?: false,
                hasSurgery = currentState.hasSurgery ?: false,
                surgeryDetails = currentState.surgeryDetails,
                doctorName = currentState.doctorName
            )
            
            authRepository.updateUserProfile(profileData).fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            profileSetupCompleted = true
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Profil güncellenirken hata oluştu"
                        )
                    }
                }
            )
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { 
                AuthUiState() // Reset to initial state
            }
        }
    }
    
    // Validation functions
    private fun validateLoginForm(state: AuthUiState): Boolean {
        var isValid = true
        
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "E-posta adresi gerekli") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Geçerli bir e-posta adresi girin") }
            isValid = false
        }
        
        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Şifre gerekli") }
            isValid = false
        }
        
        return isValid
    }
    
    private fun validateRegisterForm(state: AuthUiState): Boolean {
        var isValid = true
        
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(fullNameError = "Ad soyad gerekli") }
            isValid = false
        }
        
        if (state.username.isBlank()) {
            _uiState.update { it.copy(usernameError = "Kullanıcı adı gerekli") }
            isValid = false
        } else if (state.username.length < 3) {
            _uiState.update { it.copy(usernameError = "Kullanıcı adı en az 3 karakter olmalı") }
            isValid = false
        }
        
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "E-posta adresi gerekli") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Geçerli bir e-posta adresi girin") }
            isValid = false
        }
        
        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Şifre gerekli") }
            isValid = false
        } else if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Şifre en az 6 karakter olmalı") }
            isValid = false
        }
        
        if (state.confirmPassword.isBlank()) {
            _uiState.update { it.copy(confirmPasswordError = "Şifre tekrarı gerekli") }
            isValid = false
        } else if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Şifreler eşleşmiyor") }
            isValid = false
        }
        
        if (!state.acceptTerms) {
            _uiState.update { it.copy(errorMessage = "Kullanım şartlarını kabul etmelisiniz") }
            isValid = false
        }
        
        return isValid
    }
}