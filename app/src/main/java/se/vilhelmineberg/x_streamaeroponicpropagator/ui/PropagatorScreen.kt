package se.vilhelmineberg.x_streamaeroponicpropagator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import se.vilhelmineberg.x_streamaeroponicpropagator.data.Box as PropagationBox
import se.vilhelmineberg.x_streamaeroponicpropagator.data.Plant

/** Stable palette; a plant type always renders in the same color. */
private val plantColors = listOf(
    Color(0xFF66BB6A), Color(0xFF26A69A), Color(0xFF42A5F5), Color(0xFF7E57C2),
    Color(0xFFEC407A), Color(0xFFFF7043), Color(0xFFFFB300), Color(0xFF9CCC65),
    Color(0xFF5C6BC0), Color(0xFF8D6E63),
)

private fun colorFor(plantName: String): Color =
    plantColors[Math.floorMod(plantName.lowercase().hashCode(), plantColors.size)]

private fun positionLabel(position: Int, cols: Int): String =
    "row ${position / cols + 1}, column ${position % cols + 1}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropagatorScreen(viewModel: PropagatorViewModel = viewModel()) {
    val boxes by viewModel.boxes.collectAsState()
    val selectedBox by viewModel.selectedBox.collectAsState()
    val plants by viewModel.plants.collectAsState()
    val suggestions by viewModel.plantNameSuggestions.collectAsState()
    val selectedPositions by viewModel.selectedPositions.collectAsState()

    var showNewBoxDialog by remember { mutableStateOf(false) }
    var showDeleteBoxDialog by remember { mutableStateOf(false) }
    var showPlantDialog by remember { mutableStateOf(false) }
    var detailPlantId by remember { mutableStateOf<Long?>(null) }
    var editPlantId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BoxSelector(
                        boxes = boxes,
                        selected = selectedBox,
                        onSelect = viewModel::selectBox,
                    )
                },
                actions = {
                    IconButton(onClick = { showNewBoxDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New box")
                    }
                    if (selectedBox != null) {
                        IconButton(onClick = { showDeleteBoxDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete box")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (selectedPositions.isNotEmpty()) {
                SelectionBar(
                    count = selectedPositions.size,
                    onPlant = { showPlantDialog = true },
                    onClear = viewModel::clearSelection,
                )
            }
        },
    ) { padding ->
        val box = selectedBox
        if (box == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("No propagation box yet", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.size(12.dp))
                Button(onClick = { showNewBoxDialog = true }) { Text("Start a new box") }
            }
        } else {
            BoxContent(
                box = box,
                plants = plants,
                selectedPositions = selectedPositions,
                onEmptyPlugClick = viewModel::togglePlugSelection,
                onPlantClick = { detailPlantId = it.id },
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (showNewBoxDialog) {
        NewBoxDialog(
            onConfirm = { name, preset ->
                viewModel.createBox(name, preset)
                showNewBoxDialog = false
            },
            onDismiss = { showNewBoxDialog = false },
        )
    }

    val boxToDelete = selectedBox
    if (showDeleteBoxDialog && boxToDelete != null) {
        DeleteBoxDialog(
            boxName = boxToDelete.name,
            onConfirm = {
                viewModel.deleteBox(boxToDelete.id)
                showDeleteBoxDialog = false
            },
            onDismiss = { showDeleteBoxDialog = false },
        )
    }

    if (showPlantDialog) {
        PlantNameDialog(
            title = "Plant ${selectedPositions.size} plug(s)",
            confirmLabel = "Plant",
            suggestions = suggestions,
            onConfirm = { name ->
                viewModel.plantInSelectedPlugs(name)
                showPlantDialog = false
            },
            onDismiss = { showPlantDialog = false },
        )
    }

    val detailPlant = plants.find { it.id == detailPlantId }
    if (detailPlant != null && selectedBox != null) {
        PlugDetailDialog(
            plant = detailPlant,
            positionLabel = positionLabel(detailPlant.position, selectedBox!!.cols),
            onEdit = {
                editPlantId = detailPlant.id
                detailPlantId = null
            },
            onDelete = {
                viewModel.deletePlant(detailPlant.id)
                detailPlantId = null
            },
            onDismiss = { detailPlantId = null },
        )
    }

    val editPlant = plants.find { it.id == editPlantId }
    if (editPlant != null) {
        PlantNameDialog(
            title = "Edit plant",
            confirmLabel = "Save",
            initialName = editPlant.name,
            suggestions = suggestions,
            onConfirm = { name ->
                viewModel.renamePlant(editPlant.id, name)
                editPlantId = null
            },
            onDismiss = { editPlantId = null },
        )
    }
}

@Composable
private fun BoxSelector(
    boxes: List<PropagationBox>,
    selected: PropagationBox?,
    onSelect: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(enabled = boxes.isNotEmpty()) { expanded = true },
    ) {
        Text(selected?.name ?: "X-Stream Aeroponic Propagator", maxLines = 1)
        if (boxes.isNotEmpty()) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose box")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            boxes.forEach { box ->
                DropdownMenuItem(
                    text = { Text("${box.name} (${box.capacity} plugs)") },
                    onClick = {
                        onSelect(box.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun BoxContent(
    box: PropagationBox,
    plants: List<Plant>,
    selectedPositions: Set<Int>,
    onEmptyPlugClick: (Int) -> Unit,
    onPlantClick: (Plant) -> Unit,
    modifier: Modifier = Modifier,
) {
    val plantsByPosition = plants.associateBy { it.position }

    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp)) {
        item {
            Text(
                "${plants.size} of ${box.capacity} plugs in use — tap an empty plug to select it",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        items((0 until box.rows).toList(), key = { "row-$it" }) { row ->
            Row(Modifier.fillMaxWidth()) {
                (0 until box.cols).forEach { col ->
                    val position = row * box.cols + col
                    PlugCircle(
                        plant = plantsByPosition[position],
                        selected = position in selectedPositions,
                        onClick = { plant ->
                            if (plant == null) onEmptyPlugClick(position) else onPlantClick(plant)
                        },
                        modifier = Modifier.weight(1f).aspectRatio(1f).padding(3.dp),
                    )
                }
            }
        }
        if (plants.isNotEmpty()) {
            item {
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Text("Plants", style = MaterialTheme.typography.titleMedium)
            }
            items(plants, key = { it.id }) { plant ->
                PlantListRow(
                    plant = plant,
                    cols = box.cols,
                    onClick = { onPlantClick(plant) },
                )
            }
        }
    }
}

@Composable
private fun PlugCircle(
    plant: Plant?,
    selected: Boolean,
    onClick: (Plant?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fillColor = when {
        plant != null -> colorFor(plant.name)
        selected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(fillColor)
            .border(if (selected) 3.dp else 1.dp, borderColor, CircleShape)
            .clickable { onClick(plant) },
    ) {
        if (plant != null) {
            Text(
                plant.name.take(2).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PlantListRow(
    plant: Plant,
    cols: Int,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(colorFor(plant.name)),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(plant.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                positionLabel(plant.position, cols),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatDate(plant.plantedEpochDay), style = MaterialTheme.typography.bodySmall)
            Text(
                daysSinceText(plant.plantedEpochDay),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun SelectionBar(
    count: Int,
    onPlant: () -> Unit,
    onClear: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Close, contentDescription = "Clear selection")
            }
            Text(
                "$count plug(s) selected",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onPlant) { Text("Plant…") }
        }
    }
}
