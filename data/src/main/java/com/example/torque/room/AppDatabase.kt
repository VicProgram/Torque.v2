package com.example.torque.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.torque.room.entity.dao.MotoDao
import com.example.torque.room.entity.dao.MantenimientoDao
import com.example.torque.room.entity.dao.FotoDao
import com.example.torque.room.entity.MotoEntity
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.room.entity.FotoEntity

@Database(
    entities = [MotoEntity::class, MantenimientoEntity::class, FotoEntity::class],
    version = 3,
    exportSchema = false
)
abstract class BaseDeDatosApp : RoomDatabase() {
    abstract fun motoDao(): MotoDao
    abstract fun mantenimientoDao(): MantenimientoDao
    abstract fun fotoDao(): FotoDao

    companion object {
        @Volatile
        private var INSTANCE: BaseDeDatosApp? = null

        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE mantenimientos ADD COLUMN tipoMantenimientoId INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE mantenimientos ADD COLUMN porcentajeVida INTEGER NOT NULL DEFAULT 100")
            }
        }

        fun obtenerInstancia(context: Context): BaseDeDatosApp =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BaseDeDatosApp::class.java,
                    "torque_room.db"
                ).addMigrations(MIGRACION_1_2, MIGRACION_2_3)
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }

        fun cerrarYReiniciar() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
