package com.organiza.ai.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidade Task que representa uma tarefa no banco de dados
 * Contém título, descrição, data/hora e categoria
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    val description: String,
    val dateTime: Date,
    val category: String,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    /**
     * Enum para categorias predefinidas
     */
    enum class Category(val displayName: String) {
        WORK("Trabalho"),
        PERSONAL("Pessoal"),
        STUDY("Estudos"),
        HEALTH("Saúde"),
        SHOPPING("Compras"),
        OTHER("Outros")
    }
    
    /**
     * Verifica se a tarefa está próxima (dentro de 1 hora)
     */
    fun isNearDeadline(): Boolean {
        val currentTime = System.currentTimeMillis()
        val taskTime = dateTime.time
        val oneHourInMillis = 60 * 60 * 1000
        
        return taskTime - currentTime <= oneHourInMillis && taskTime > currentTime
    }
    
    /**
     * Verifica se a tarefa está atrasada
     */
    fun isOverdue(): Boolean {
        return dateTime.time < System.currentTimeMillis() && !isCompleted
    }
}
