package com.organiza.ai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.organiza.ai.data.AppDatabase
import com.organiza.ai.data.TaskRepository
import com.organiza.ai.model.Task
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar dados relacionados às tarefas
 * Contém a lógica de negócio e comunica com o Repository
 */
class TaskViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: TaskRepository
    
    // LiveData para observar mudanças nas tarefas
    val allTasks: LiveData<List<Task>>
    val pendingTasks: LiveData<List<Task>>
    val completedTasks: LiveData<List<Task>>
    val pendingTasksCount: LiveData<Int>
    
    // MutableLiveData para controle de estado da UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _taskSaved = MutableLiveData<Boolean>()
    val taskSaved: LiveData<Boolean> = _taskSaved
    
    // Filtros e busca
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery
    
    private val _selectedCategory = MutableLiveData<String>()
    val selectedCategory: LiveData<String> = _selectedCategory
    
    init {
        // Inicializa o repository com o banco de dados
        val taskDao = AppDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        
        // Inicializa os LiveData
        allTasks = repository.getAllTasks()
        pendingTasks = repository.getPendingTasks()
        completedTasks = repository.getCompletedTasks()
        pendingTasksCount = repository.getPendingTasksCount()
    }
    
    /**
     * Insere uma nova tarefa
     */
    fun insertTask(task: Task) = viewModelScope.launch {
        try {
            _isLoading.value = true
            val taskId = repository.insertTask(task)
            _taskSaved.value = true
            _errorMessage.value = null
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao salvar tarefa: ${e.message}"
            _taskSaved.value = false
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Atualiza uma tarefa existente
     */
    fun updateTask(task: Task) = viewModelScope.launch {
        try {
            _isLoading.value = true
            repository.updateTask(task.copy(updatedAt = java.util.Date()))
            _taskSaved.value = true
            _errorMessage.value = null
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao atualizar tarefa: ${e.message}"
            _taskSaved.value = false
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Exclui uma tarefa
     */
    fun deleteTask(task: Task) = viewModelScope.launch {
        try {
            repository.deleteTask(task)
            _errorMessage.value = null
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao excluir tarefa: ${e.message}"
        }
    }
    
    /**
     * Marca uma tarefa como completada
     */
    fun markTaskAsCompleted(taskId: Long) = viewModelScope.launch {
        try {
            repository.markTaskAsCompleted(taskId)
            _errorMessage.value = null
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao completar tarefa: ${e.message}"
        }
    }
    
    /**
     * Obtém uma tarefa pelo ID
     */
    suspend fun getTaskById(taskId: Long): Task? {
        return try {
            repository.getTaskById(taskId)
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao buscar tarefa: ${e.message}"
            null
        }
    }
    
    /**
     * Busca tarefas por texto
     */
    fun searchTasks(query: String): LiveData<List<Task>> {
        _searchQuery.value = query
        return repository.searchTasks(query)
    }
    
    /**
     * Filtra tarefas por categoria
     */
    fun getTasksByCategory(category: String): LiveData<List<Task>> {
        _selectedCategory.value = category
        return repository.getTasksByCategory(category)
    }
    
    /**
     * Exclui todas as tarefas completadas
     */
    fun deleteCompletedTasks() = viewModelScope.launch {
        try {
            repository.deleteCompletedTasks()
            _errorMessage.value = null
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao excluir tarefas completadas: ${e.message}"
        }
    }
    
    /**
     * Limpa mensagens de erro
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Reseta o estado de tarefa salva
     */
    fun resetTaskSaved() {
        _taskSaved.value = false
    }
    
    /**
     * Obtém tarefas para notificação
     */
    suspend fun getTasksForNotification(): List<Task> {
        val currentTime = System.currentTimeMillis()
        val oneHourLater = currentTime + (60 * 60 * 1000) // 1 hora em milissegundos
        
        return try {
            repository.getTasksForNotification(currentTime, oneHourLater)
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao buscar tarefas para notificação: ${e.message}"
            emptyList()
        }
    }
}
