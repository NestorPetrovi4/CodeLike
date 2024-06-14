package ru.netology.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDB
import ru.netology.nmedia.db.Token
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.io.File

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    val auth: LiveData<Token?> = AppAuth.getInstance().state.asLiveData()
    val isAuthorized: Boolean
        get() = AppAuth.getInstance().state.value?.token != null

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val repository: PostRepository =
        PostRepositoryImpl(AppDB.getInstance(application).postDAO())
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
            }.also() {
                repository.signUp(name, login, password)
            }
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