package com.organiza.ai.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.organiza.ai.data.AppDatabase
import com.organiza.ai.data.TaskRepository
import com.organiza.ai.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver para gerenciar notificações de lembrete
 * Recebe alarmes agendados e exibe notificações para o usuário
 */
class ReminderBroadcast : BroadcastReceiver() {
    
    companion object {
        const val ACTION_TASK_REMINDER = "com.organiza.ai.TASK_REMINDER"
        const val ACTION_MARK_COMPLETED = "com.organiza.ai.MARK_COMPLETED"
        const val EXTRA_TASK_ID = "task_id"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TASK_REMINDER -> {
                handleTaskReminder(context, intent)
            }
            ACTION_MARK_COMPLETED -> {
                handleMarkCompleted(context, intent)
            }
        }
    }
    
    /**
     * Processa lembrete de tarefa
     */
    private fun handleTaskReminder(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        if (taskId == -1L) return
        
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val repository = TaskRepository(database.taskDao())
                val task = repository.getTaskById(taskId)
                
                task?.let {
                    // Verifica se a tarefa ainda não foi completada
                    if (!it.isCompleted) {
                        val notificationHelper = NotificationHelper(context)
                        notificationHelper.showTaskReminder(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Processa ação de marcar tarefa como completada
     */
    private fun handleMarkCompleted(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        if (taskId == -1L) return
        
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val repository = TaskRepository(database.taskDao())
                
                // Marca a tarefa como completada
                repository.markTaskAsCompleted(taskId)
                
                // Cancela a notificação
                val notificationHelper = NotificationHelper(context)
                notificationHelper.cancelNotification(taskId)
                
                // Cancela alarmes futuros para esta tarefa
                val alarmHelper = AlarmHelper(context)
                alarmHelper.cancelAlarm(taskId)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
