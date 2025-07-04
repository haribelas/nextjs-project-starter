package com.organiza.ai.data

import androidx.lifecycle.LiveData
import com.organiza.ai.model.Task

/**
 * Repository para centralizar o acesso aos dados de tarefas
 * Abstrai a fonte de dados e fornece uma API limpa para o ViewModel
 */
class TaskRepository(private val taskDao: TaskDao) {
    
    /**
     * Obtém todas as tarefas como LiveData
     */
    fun getAllTasks(): LiveData<List<Task>> = taskDao.getAllTasks()
    
    /**
     * Obtém tarefas pendentes
     */
    fun getPendingTasks(): LiveData<List<Task>> = taskDao.getPendingTasks()
    
    /**
     * Obtém tarefas completadas
     */
    fun getCompletedTasks(): LiveData<List<Task>> = taskDao.getCompletedTasks()
    
    /**
     * Obtém tarefas por categoria
     */
    fun getTasksByCategory(category: String): LiveData<List<Task>> = 
        taskDao.getTasksByCategory(category)
    
    /**
     * Busca tarefas por texto
     */
    fun searchTasks(searchText: String): LiveData<List<Task>> = 
        taskDao.searchTasks(searchText)
    
    /**
     * Obtém contagem de tarefas pendentes
     */
    fun getPendingTasksCount(): LiveData<Int> = taskDao.getPendingTasksCount()
    
    /**
     * Insere uma nova tarefa
     */
    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)
    
    /**
     * Atualiza uma tarefa existente
     */
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    
    /**
     * Exclui uma tarefa
     */
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    
    /**
     * Exclui uma tarefa pelo ID
     */
    suspend fun deleteTaskById(taskId: Long) = taskDao.deleteTaskById(taskId)
    
    /**
     * Obtém uma tarefa pelo ID
     */
    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)
    
    /**
     * Marca uma tarefa como completada
     */
    suspend fun markTaskAsCompleted(taskId: Long) = 
        taskDao.markTaskAsCompleted(taskId, System.currentTimeMillis())
    
    /**
     * Obtém tarefas para notificação (próximas de 1 hora)
     */
    suspend fun getTasksForNotification(startTime: Long, endTime: Long): List<Task> = 
        taskDao.getTasksForNotification(startTime, endTime)
    
    /**
     * Exclui todas as tarefas completadas
     */
    suspend fun deleteCompletedTasks() = taskDao.deleteCompletedTasks()
}
