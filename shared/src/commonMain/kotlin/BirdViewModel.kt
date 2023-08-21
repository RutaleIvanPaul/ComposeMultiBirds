import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.BirdImage

data class BirdUIState(
    val images: List<BirdImage> = emptyList(),
    val selectedCategory: String? = null
) {
    val categories = images.map { it.category }.toSet()
    val selectedImages = if (selectedCategory.isNullOrEmpty()) {
        images
    } else {
        images.filter { it.category == selectedCategory }
    }
}

class BirdViewModel : ViewModel() {

    init {
        updateImages()
    }

    private val _uiState = MutableStateFlow<BirdUIState>(BirdUIState())
    val uiState = _uiState.asStateFlow()

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    private suspend fun getImages(): List<BirdImage> {
        return httpClient.get("http://sebi.io/demo-image-api/pictures.json")
            .body()
    }

    fun updateImages() {
        viewModelScope.launch {
            val images2 = getImages()
            _uiState.update {
                it.copy(images = images2)
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update {
            it.copy(selectedCategory = category)
        }
    }

    override fun onCleared() {
        httpClient.close()
    }
}