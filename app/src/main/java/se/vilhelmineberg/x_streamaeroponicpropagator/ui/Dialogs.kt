package se.vilhelmineberg.x_streamaeroponicpropagator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.vilhelmineberg.x_streamaeroponicpropagator.data.BoxPreset
import se.vilhelmineberg.x_streamaeroponicpropagator.data.Plant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

fun formatDate(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).format(dateFormatter)

fun daysSinceText(epochDay: Long): String =
    when (val days = ChronoUnit.DAYS.between(LocalDate.ofEpochDay(epochDay), LocalDate.now())) {
        0L -> "today"
        1L -> "1 day"
        else -> "$days days"
    }

/** Dialog for starting a new propagation box: name + preset size. */
@Composable
fun NewBoxDialog(
    onConfirm: (name: String, preset: BoxPreset) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var preset by remember { mutableStateOf(BoxPreset.MEDIUM_40) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New propagation box") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Box name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Size",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                )
                BoxPreset.entries.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = preset == option, onClick = { preset = option }),
                    ) {
                        RadioButton(selected = preset == option, onClick = { preset = option })
                        Text(option.label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim(), preset) },
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/**
 * Dialog for entering a plant name, with suggestions from previously used names.
 * Used both for planting into selected plugs and for editing an existing plant.
 */
@Composable
fun PlantNameDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    suggestions: List<String>,
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    val matching = remember(name, suggestions) {
        suggestions
            .filter { it.contains(name.trim(), ignoreCase = true) && !it.equals(name.trim(), ignoreCase = true) }
            .take(5)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plant type") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                matching.forEach { suggestion ->
                    Text(
                        suggestion,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { name = suggestion }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim()) },
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** Detail dialog for a filled plug: shows the plant, offers edit and delete. */
@Composable
fun PlugDetailDialog(
    plant: Plant,
    positionLabel: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(plant.name) },
        text = {
            Column {
                Text("Plug $positionLabel")
                Text("Planted ${formatDate(plant.plantedEpochDay)}")
                Text("Age: ${daysSinceText(plant.plantedEpochDay)}")
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onEdit) { Text("Edit") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

/** Confirmation before deleting a whole box and everything planted in it. */
@Composable
fun DeleteBoxDialog(
    boxName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete \"$boxName\"?") },
        text = { Text("The box and all plants registered in it will be removed.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
