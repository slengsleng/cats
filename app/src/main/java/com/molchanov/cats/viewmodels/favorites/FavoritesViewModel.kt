package com.molchanov.cats.viewmodels.favorites

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.molchanov.cats.network.networkmodels.CatItem
import com.molchanov.cats.repository.CatsRepository
import com.molchanov.cats.utils.ApiStatus
import com.molchanov.cats.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val repository: CatsRepository) : ViewModel() {
    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _favoriteImages = MutableLiveData<List<CatItem>>()
    val favoriteImages: LiveData<List<CatItem>> get() = _favoriteImages

    private val _navigateToCard = MutableLiveData<CatItem>()
    val navigateToCard: LiveData<CatItem>
        get() = _navigateToCard

    private val _response = MutableLiveData<String>()
    private val response: LiveData<String> get() = _response

    init {
        Log.d("M_FavoritesViewModel", "FavoritesViewModel инициализируется")
        getFavorites()
    }

    private fun getFavorites() {
        viewModelScope.launch {
            _status.value = ApiStatus.LOADING
            try {
                _favoriteImages.value = repository.refreshFavorites()
                Log.d(
                    "M_FavoritesViewModel",
                    "Избранные картинки успешно загружены: ${favoriteImages.value?.size}"
                )
                _status.value = if (favoriteImages.value.isNullOrEmpty()) {
                    ApiStatus.EMPTY
                } else {
                    ApiStatus.DONE
                }
            } catch (e: Exception) {
                Log.d(
                    "M_FavoritesViewModel",
                    "Ошибка при загрузке избранных картинок: ${e.message}"
                )
                _status.value = ApiStatus.ERROR
            }
        }
        Log.d("M_FavoritesViewModel", "getFavorites отработал")
    }

    fun deleteFromFavorites(cat: CatItem) {
        Log.d("M_FavoritesViewModel", "deleteFromFavorites запущен. Cat: ${cat.id}")
        viewModelScope.launch {
            try {
                _response.value = repository.removeFavoriteByFavId(cat.id)
                _favoriteImages.value = repository.refreshFavorites()

                Log.d(
                    "M_FavoritesViewModel",
                    "Удалено из favorite images: ${cat.image?.id}. Размер списка: ${favoriteImages.value?.size}"
                )
                showToast("Удалено из избранного")
                Log.d("M_FavoritesViewModel", "Удалено из избранного успешно: ${response.value}")
            } catch (e: Exception) {
                showToast("Уже удалено из избранного")
                Log.d("M_FavoritesViewModel", "Ошибка при удалении из избранного: ${e.message}")
            }
        }
    }

    fun displayCatCard(currentImage: CatItem) {
        _navigateToCard.value = currentImage
    }

    fun displayCatCardComplete() {
        _navigateToCard.value = null
    }

}