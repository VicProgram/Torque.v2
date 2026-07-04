package com.example.torque.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.torque.room.entity.dao.MotoDao
import com.example.torque.room.entity.dao.MantenimientoDao
import com.example.torque.room.entity.dao.FotoDao
import com.example.torque.room.entity.dao.FotoMantenimientoDao
import com.example.torque.room.entity.MotoEntity
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.room.entity.FotoEntity
import com.example.torque.room.entity.FotoMantenimientoEntity

@Database(
    entities = [MotoEntity::class, MantenimientoEntity::class, FotoEntity::class, FotoMantenimientoEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Convertidores::class)
abstract class BaseDeDatosApp : RoomDatabase() {
    abstract fun motoDao(): MotoDao
    abstract fun mantenimientoDao(): MantenimientoDao
    abstract fun fotoDao(): FotoDao
    abstract fun fotoMantenimientoDao(): FotoMantenimientoDao

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

        val MIGRACION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS fotos_mantenimiento (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, idMantenimiento INTEGER NOT NULL, ruta TEXT NOT NULL, tipo TEXT NOT NULL DEFAULT 'foto', creadoEn TEXT)")
                db.execSQL("ALTER TABLE motos ADD COLUMN fechaItv TEXT")
                db.execSQL("ALTER TABLE motos ADD COLUMN fechaSeguro TEXT")
            }
        }

        val MIGRACION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE motos ADD COLUMN tipoVehiculo TEXT NOT NULL DEFAULT 'MOTO'")
                db.execSQL("ALTER TABLE motos ADD COLUMN tipoCarroceria TEXT")
                db.execSQL("ALTER TABLE motos ADD COLUMN tipoCombustible TEXT")
                db.execSQL("ALTER TABLE motos ADD COLUMN tipoTransmision TEXT")
                db.execSQL("ALTER TABLE motos ADD COLUMN numeroPuertas INTEGER")
            }
        }

        fun obtenerInstancia(context: Context): BaseDeDatosApp =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BaseDeDatosApp::class.java,
                    "torque_room.db"
                ).addMigrations(MIGRACION_1_2, MIGRACION_2_3, MIGRACION_3_4, MIGRACION_4_5)
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }

        fun cerrarYReiniciar() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
