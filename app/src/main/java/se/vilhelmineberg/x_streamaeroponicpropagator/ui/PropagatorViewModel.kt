package se.vilhelmineberg.x_streamaeroponicpropagator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.vilhelmineberg.x_streamaeroponicpropagator.data.AppDatabase
import se.vilhelmineberg.x_streamaeroponicpropagator.data.Box
import se.vilhelmineberg.x_streamaeroponicpropagator.data.BoxPreset
import se.vilhelmineberg.x_streamaeroponicpropagator.data.Plant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PropagatorViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val boxDao = db.boxDao()
    private val plantDao = db.plantDao()

    val boxes: StateFlow<List<Box>> = boxDao.boxes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val selectedBoxIdFlow = MutableStateFlow<Long?>(null)

    /** The currently shown box: the explicitly selected one, or the first available. */
    val selectedBox: StateFlow<Box?> =
        combine(boxes, selectedBoxIdFlow) { boxes, selectedId ->
            boxes.find { it.id == selectedId } ?: boxes.firstOrNull()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val plants: StateFlow<List<Plant>> = selectedBox
        .flatMapLatest { box ->
            if (box == null) flowOf(emptyList()) else plantDao.plantsForBox(box.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val plantNameSuggestions: StateFlow<List<String>> = plantDao.allPlantNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Positions of empty plugs selected for planting. */
    private val _selectedPositions = MutableStateFlow<Set<Int>>(emptySet())
    val selectedPositions: StateFlow<Set<Int>> = _selectedPositions

    fun selectBox(boxId: Long) {
        selectedBoxIdFlow.value = boxId
        _selectedPositions.value = emptySet()
    }

    fun createBox(name: String, preset: BoxPreset) {
        viewModelScope.launch {
            val id = boxDao.insert(
                Box(
                    name = name,
                    rows = preset.rows,
                    cols = preset.cols,
                    createdAtEpochDay = LocalDate.now().toEpochDay(),
                )
            )
            selectBox(id)
        }
    }

    fun deleteBox(boxId: Long) {
        viewModelScope.launch { boxDao.delete(boxId) }
        _selectedPositions.value = emptySet()
    }

    fun togglePlugSelection(position: Int) {
        _selectedPositions.value = _selectedPositions.value.let {
            if (position in it) it - position else it + position
        }
    }

    fun clearSelection() {
        _selectedPositions.value = emptySet()
    }

    /** Registers [name] in all currently selected plugs, dated today. */
    fun plantInSelectedPlugs(name: String) {
        val box = selectedBox.value ?: return
        val positions = _selectedPositions.value.toList()
        if (positions.isEmpty() || name.isBlank()) return
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            plantDao.upsertAll(
                positions.map { pos ->
                    Plant(boxId = box.id, position = pos, name = name.trim(), plantedEpochDay = today)
                }
            )
            _selectedPositions.value = emptySet()
        }
    }

    /** Renames a plant, keeping its original planting date. */
    fun renamePlant(plantId: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { plantDao.rename(plantId, name.trim()) }
    }

    fun deletePlant(plantId: Long) {
        viewModelScope.launch { plantDao.delete(plantId) }
    }
}
