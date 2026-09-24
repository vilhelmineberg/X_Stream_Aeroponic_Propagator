package se.vilhelmineberg.x_streamaeroponicpropagator.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A propagation box, e.g. an X-Stream 40 laid out as 5 rows x 8 columns. */
@Entity(tableName = "boxes")
data class Box(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rows: Int,
    val cols: Int,
    val createdAtEpochDay: Long,
) {
    val capacity: Int get() = rows * cols
}

/**
 * A plant occupying one plug of a box. [position] is the 0-based plug index,
 * row-major: row = position / cols, column = position % cols.
 */
@Entity(
    tableName = "plants",
    foreignKeys = [
        ForeignKey(
            entity = Box::class,
            parentColumns = ["id"],
            childColumns = ["boxId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["boxId", "position"], unique = true)],
)
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val boxId: Long,
    val position: Int,
    val name: String,
    val plantedEpochDay: Long,
)

/** The standard X-Stream propagator sizes (rows = height, cols = width). */
enum class BoxPreset(val label: String, val rows: Int, val cols: Int) {
    SMALL_20("20 plugs (4 x 5)", 4, 5),
    MEDIUM_40("40 plugs (5 x 8)", 5, 8),
    LARGE_80("80 plugs (8 x 10)", 8, 10),
    XL_120("120 plugs (8 x 15)", 8, 15),
}
