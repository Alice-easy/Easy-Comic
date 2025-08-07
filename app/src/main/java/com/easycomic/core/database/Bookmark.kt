package com.easycomic.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "bookmark",
    foreignKeys = [
        ForeignKey(
            entity = Manga::class,
            parentColumns = ["id"],
            childColumns = ["mangaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mangaId: Long,
    val page: Int,
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)