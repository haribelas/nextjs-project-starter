package com.organiza.ai.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.organiza.ai.R
import com.organiza.ai.model.Task
import com.organiza.ai.ui.MainActivity

/**
 * Helper class para gerenciar notificações do app
 * Cria e exibe notificações de lembrete de tarefas
 */
class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "task_reminders"
        const val CHANNEL_NAME = "Lembretes de Tarefas"
        const val CHANNEL_DESCRIPTION = "Notificações para lembrar de tarefas próximas"
        const val NOTIFICATION_ID_BASE = 1000
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Cria o canal de notificação (necessário para Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Exibe uma notificação de lembrete para uma tarefa
     */
    fun showTaskReminder(task: Task) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", task.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Lembrete: ${task.title}")
            .setContentText(task.description)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${task.description}\n\nCategoria: ${task.category}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_check,
                "Marcar como Concluída",
                createMarkAsCompletedPendingIntent(task.id)
            )
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_BASE + task.id.toInt(),
                notification
            )
        } catch (e: SecurityException) {
            // Permissão de notificação não concedida
            e.printStackTrace()
        }
    }
    
    /**
     * Cria um PendingIntent para marcar tarefa como completada
     */
    private fun createMarkAsCompletedPendingIntent(taskId: Long): PendingIntent {
        val intent = Intent(context, ReminderBroadcast::class.java).apply {
            action = ReminderBroadcast.ACTION_MARK_COMPLETED
            putExtra("task_id", taskId)
        }
        
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Cancela uma notificação específica
     */
    fun cancelNotification(taskId: Long) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_BASE + taskId.toInt())
    }
    
    /**
     * Cancela todas as notificações do app
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
    
    /**
     * Verifica se as notificações estão habilitadas
     */
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}
