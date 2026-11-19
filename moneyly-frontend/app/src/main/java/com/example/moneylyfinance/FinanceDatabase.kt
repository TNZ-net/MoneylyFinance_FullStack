
package com.example.moneylyfinance

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ExpenseEntity::class,
        BudgetEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun budgetDao(): BudgetDao
    abstract fun expenseDao(): ExpenseDao

    companion object {

        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        // Existing migration: version 1 to 2 adds expense categories.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE expenses ADD COLUMN category TEXT NOT NULL DEFAULT 'Other'"
                )
            }
        }

        // New migration: version 2 to 3 creates the budget table.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS budgets (
                        id INTEGER NOT NULL,
                        monthlyLimit REAL NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
            }
        }


        // Migration: version 3 to 4 adds sync tracking to expenses.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE expenses ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE expenses ADD COLUMN serverId INTEGER DEFAULT NULL"
                )
            }
        }


        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
