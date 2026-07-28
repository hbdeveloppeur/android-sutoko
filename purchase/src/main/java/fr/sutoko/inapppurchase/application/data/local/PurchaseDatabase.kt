package fr.sutoko.inapppurchase.application.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import fr.sutoko.inapppurchase.application.domain.model.PurchaseState

@Database(
    entities = [PurchaseEntity::class],
    version = 4,
    exportSchema = false
)
abstract class PurchaseDatabase : RoomDatabase() {
    abstract fun purchaseDao(): PurchaseDao

    companion object {
        private const val NAME = "purchases.db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE purchases ADD COLUMN purchaseState INTEGER NOT NULL DEFAULT ${PurchaseState.PURCHASED}"
                )
                db.execSQL(
                    "ALTER TABLE purchases ADD COLUMN orderId TEXT"
                )
                db.execSQL(
                    "ALTER TABLE purchases ADD COLUMN backendRegistered INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /**
         * Premium purchases were historically flagged backendRegistered without ever
         * reaching the backend (no registrar claimed the premium SKU). Reset their
         * flag so the coordinator re-registers them once at the next app start.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "UPDATE purchases SET backendRegistered = 0 WHERE sku LIKE '%premium%'"
                )
            }
        }

        @Suppress("DEPRECATION")
        fun create(context: Context): PurchaseDatabase {
            return Room.databaseBuilder(context, PurchaseDatabase::class.java, NAME)
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .build()
        }
    }
}
