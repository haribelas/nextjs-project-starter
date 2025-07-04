package com.organiza.ai.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.organiza.ai.model.Task

/**
 * DAO (Data Access Object) para operações de banco de dados com tarefas
 * Contém funções para inserção, atualização, exclusão e listagem
 */
@Dao
interface TaskDao {
    
    /**
     * Insere uma nova tarefa no banco de dados
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    /**
     * Atualiza uma tarefa existente
     */
    @Update
    suspend fun updateTask(task: Task)
    
    /**
     * Exclui uma tarefa específica
     */
    @Delete
    suspend fun deleteTask(task: Task)
    
    /**
     * Exclui uma tarefa pelo ID
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)
    
    /**
     * Busca todas as tarefas ordenadas por data/hora
     */
    @Query("SELECT * FROM tasks ORDER BY dateTime ASC")
    fun getAllTasks(): LiveData<List<Task>>
    
    /**
     * Busca uma tarefa específica pelo ID
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?
    
    /**
     * Busca tarefas por categoria
     */
    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY dateTime ASC")
    fun getTasksByCategory(category: String): LiveData<List<Task>>
    
    /**
     * Busca tarefas pendentes (não completadas)
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dateTime ASC")
    fun getPendingTasks(): LiveData<List<Task>>
    
    /**
     * Busca tarefas completadas
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY dateTime DESC")
    fun getCompletedTasks(): LiveData<List<Task>>
    
    /**
     * Busca tarefas que precisam de notificação (próximas de 1 hora)
     */
    @Query("SELECT * FROM tasks WHERE dateTime BETWEEN :startTime AND :endTime AND isCompleted = 0")
    suspend fun getTasksForNotification(startTime: Long, endTime: Long): List<Task>
    
    /**
     * Marca uma tarefa como completada
     */
    @Query("UPDATE tasks SET isCompleted = 1, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun markTaskAsCompleted(taskId: Long, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Busca tarefas por texto (título ou descrição)
     */
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :searchText || '%' OR description LIKE '%' || :searchText || '%' ORDER BY dateTime ASC")
    fun searchTasks(searchText: String): LiveData<List<Task>>
    
    /**
     * Conta o total de tarefas pendentes
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun getPendingTasksCount(): LiveData<Int>
    
    /**
     * Exclui todas as tarefas completadas
     */
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()
}
