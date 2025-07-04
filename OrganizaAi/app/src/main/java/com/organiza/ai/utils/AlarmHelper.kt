package com.organiza.ai.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.organiza.ai.model.Task
import java.util.Calendar

/**
 * Helper class para gerenciar alarmes e agendamento de notificações
 * Agenda notificações 1 hora antes do horário da tarefa
 */
class AlarmHelper(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    /**
     * Agenda um alarme para lembrete de tarefa
     * O alarme será disparado 1 hora antes do horário da tarefa
     */
    fun scheduleTaskReminder(task: Task) {
        // Calcula o tempo para o lembrete (1 hora antes)
        val reminderTime = task.dateTime.time - (60 * 60 * 1000) // 1 hora em milissegundos
        val currentTime = System.currentTimeMillis()
        
        // Só agenda se o tempo de lembrete for no futuro
        if (reminderTime <= currentTime) {
            return
        }
        
        val intent = Intent(context, ReminderBroadcast::class.java).apply {
            action = ReminderBroadcast.ACTION_TASK_REMINDER
            putExtra(ReminderBroadcast.EXTRA_TASK_ID, task.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Para Android 6.0+ usa setExactAndAllowWhileIdle para garantir que o alarme dispare
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } else {
                // Para versões anteriores
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Permissão de alarme exato não concedida
            e.printStackTrace()
        }
    }
    
    /**
     * Cancela um alarme específico
     */
    fun cancelAlarm(taskId: Long) {
        val intent = Intent(context, ReminderBroadcast::class.java).apply {
            action = ReminderBroadcast.ACTION_TASK_REMINDER
            putExtra(ReminderBroadcast.EXTRA_TASK_ID, taskId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
    
    /**
     * Reagenda um alarme (usado quando uma tarefa é editada)
     */
    fun rescheduleTaskReminder(task: Task) {
        cancelAlarm(task.id)
        scheduleTaskReminder(task)
    }
    
    /**
     * Verifica se o dispositivo pode agendar alarmes exatos
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
    
    /**
     * Agenda múltiplas tarefas de uma vez
     */
    fun scheduleMultipleReminders(tasks: List<Task>) {
        tasks.forEach { task ->
            if (!task.isCompleted) {
                scheduleTaskReminder(task)
            }
        }
    }
    
    /**
     * Cancela todos os alarmes de uma lista de tarefas
     */
    fun cancelMultipleAlarms(taskIds: List<Long>) {
        taskIds.forEach { taskId ->
            cancelAlarm(taskId)
        }
    }
    
    /**
     * Calcula o tempo restante até o lembrete
     */
    fun getTimeUntilReminder(task: Task): Long {
        val reminderTime = task.dateTime.time - (60 * 60 * 1000)
        val currentTime = System.currentTimeMillis()
        return reminderTime - currentTime
    }
    
    /**
     * Verifica se uma tarefa precisa de lembrete
     */
    fun shouldScheduleReminder(task: Task): Boolean {
        val reminderTime = task.dateTime.time - (60 * 60 * 1000)
        val currentTime = System.currentTimeMillis()
        return reminderTime > currentTime && !task.isCompleted
    }
}
