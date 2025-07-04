package com.organiza.ai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.organiza.ai.model.Task

/**
 * Banco de dados principal da aplicação usando Room
 * Contém a entidade Task e fornece acesso ao DAO
 */
@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Fornece acesso ao DAO de tarefas
     */
    abstract fun taskDao(): TaskDao
    
    companion object {
        // Singleton para evitar múltiplas instâncias do banco
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Obtém a instância do banco de dados
         * Implementa o padrão Singleton thread-safe
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "organiza_ai_database"
                )
                    .fallbackToDestructiveMigration() // Para desenvolvimento - remove em produção
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Limpa a instância do banco (útil para testes)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
