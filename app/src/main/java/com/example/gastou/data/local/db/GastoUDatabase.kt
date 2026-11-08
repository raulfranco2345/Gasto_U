package com.example.gastou.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local (Room). Es la fuente de verdad de la interfaz: los datos se ven
 * aunque no haya internet y se conservan al cerrar y abrir la app.
 */
@Database(
    entities = [ExpenseEntity::class, SavingGoalEntity::class, BudgetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GastoUDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun savingGoalDao(): SavingGoalDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        fun build(context: Context): GastoUDatabase =
            Room.databaseBuilder(context, GastoUDatabase::class.java, "gastou.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
