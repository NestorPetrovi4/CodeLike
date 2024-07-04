package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.Token
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth
) : ViewModel() {
    val auth: LiveData<Token?> = appAuth.state.asLiveData()
    val isAuthorized: Boolean
        get() = appAuth.state.value?.token != null

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _photo = MutableLiveData(PhotoModel())
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun signIn(login: String, password: String) = viewModelScope.launch {
        try {
            _dataState.postValue(FeedModelState(loading = true))
            repository.signIn(login, password)
            _dataState.postValue(FeedModelState())
        } catch (e: NetworkException) {
            _dataState.postValue(FeedModelState(error = true, errorText = e.msg))
        } catch (e: Exception) {
            _dataState.postValue(FeedModelState(error = true, errorText = e.message))
        }
    }

    fun signUp(name: String, login: String, password: String) = viewModelScope.launch {
        try {
            _dataState.postValue(FeedModelState(loading = true))
            _photo.value?.file?.let { file ->
                repository.signUp(name, login, password, file)
            } ?: repository.signUp(name, login, password)
            _dataState.postValue(FeedModelState())
        } catch (e: NetworkException) {
            _dataState.postValue(FeedModelState(error = true, errorText = e.msg))
        } catch (e: Exception) {
            _dataState.postValue(FeedModelState(error = true, errorText = e.message))
        }
    }

    fun dropState() {
        _dataState.postValue(FeedModelState())
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun dropPhoto() {
        _photo.value = PhotoModel()
    }
}