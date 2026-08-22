package uz.sharif.vocabbrain.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import uz.sharif.vocabbrain.feature.word.data.local.WordDao
import uz.sharif.vocabbrain.feature.word.data.local.WordEntity

@Database(entities = [WordEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
}
