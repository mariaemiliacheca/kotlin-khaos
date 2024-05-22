package com.kotlinkhaos.classes.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlinkhaos.classes.user.User
import kotlinx.coroutines.launch

class UserAvatarViewModel : ViewModel() {
    private var _updatedUserId = ""
    private var _updatedUserAvatarUrl = MutableLiveData<String?>()
    val updatedUserAvatarUrl: LiveData<String?> = _updatedUserAvatarUrl

    private var _userAvatarError = MutableLiveData<Exception>()
    val userAvatarError: LiveData<Exception> = _userAvatarError

    private var _avatarUrl = MutableLiveData<String>()
    val avatarUrl: LiveData<String> = _avatarUrl

    fun loadAvatarHash() {
        viewModelScope.launch {
            try {
                _avatarUrl.value = User.getProfilePicture()
            } catch (err: Exception) {
                _userAvatarError.value = err
            }
        }
    }

    fun getUpdatedAvatarUrl(userId: String): String? {
        if (_updatedUserId == userId) {
            return _updatedUserAvatarUrl.value
        }
        return null
    }

    fun updateUserAvatarUrl(userId: String, avatarHash: String) {
        _updatedUserAvatarUrl.value = User.getProfilePicture(userId, avatarHash)
        _updatedUserId = userId
    }
}
