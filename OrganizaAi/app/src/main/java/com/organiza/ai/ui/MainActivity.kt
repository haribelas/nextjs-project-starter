package com.organiza.ai.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.organiza.ai.R
import com.organiza.ai.model.Task
import com.organiza.ai.utils.AlarmHelper
import com.organiza.ai.utils.NotificationHelper
import com.organiza.ai.viewmodel.TaskViewModel

/**
 * Atividade principal que exibe a lista de tarefas
 * Contém RecyclerView com tarefas e botão flutuante para adicionar novas tarefas
 */
class MainActivity : AppCompatActivity() {
    
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var alarmHelper: AlarmHelper
    private lateinit var notificationHelper: NotificationHelper
    
    companion object {
        const val REQUEST_ADD_TASK = 1
        const val REQUEST_EDIT_TASK = 2
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Configura tema baseado nas preferências do sistema
        setupTheme()
        
        // Inicializa helpers
        alarmHelper = AlarmHelper(this)
        notificationHelper = NotificationHelper(this)
        
        // Configura views
        setupViews()
        setupRecyclerView()
        setupObservers()
        
        // Verifica se foi aberto através de uma notificação
        handleNotificationIntent()
        
        // Verifica permissões de notificação
        checkNotificationPermissions()
    }
    
    /**
     * Configura o tema da aplicação
     */
    private fun setupTheme() {
        // Segue o tema do sistema (claro/escuro)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
    
    /**
     * Configura as views da tela
     */
    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewTasks)
        fabAddTask = findViewById(R.id.fabAddTask)
        
        // Configura toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Organiza Aí"
        
        // Configura FAB
        fabAddTask.setOnClickListener {
            openTaskForm()
        }
    }
    
    /**
     * Configura o RecyclerView com o adapter
     */
    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task -> openTaskForm(task) },
            onTaskLongClick = { task -> showTaskOptions(task) },
            onTaskCompleted = { task -> toggleTaskCompletion(task) }
        )
        
        recyclerView.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }
    }
    
    /**
     * Configura observadores do ViewModel
     */
    private fun setupObservers() {
        // Observa lista de tarefas
        taskViewModel.allTasks.observe(this) { tasks ->
            taskAdapter.submitList(tasks)
            
            // Agenda lembretes para tarefas novas/atualizadas
            tasks.forEach { task ->
                if (alarmHelper.shouldScheduleReminder(task)) {
                    alarmHelper.scheduleTaskReminder(task)
                }
            }
        }
        
        // Observa mensagens de erro
        taskViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                showError(it)
                taskViewModel.clearErrorMessage()
            }
        }
        
        // Observa estado de carregamento
        taskViewModel.isLoading.observe(this) { isLoading ->
            // Aqui você pode mostrar/esconder um indicador de carregamento
        }
    }
    
    /**
     * Abre o formulário para adicionar/editar tarefa
     */
    private fun openTaskForm(task: Task? = null) {
        val intent = Intent(this, TaskFormActivity::class.java)
        task?.let {
            intent.putExtra("task_id", it.id)
        }
        startActivityForResult(intent, if (task == null) REQUEST_ADD_TASK else REQUEST_EDIT_TASK)
    }
    
    /**
     * Alterna o status de conclusão da tarefa
     */
    private fun toggleTaskCompletion(task: Task) {
        if (task.isCompleted) {
            // Reativar tarefa
            val updatedTask = task.copy(isCompleted = false)
            taskViewModel.updateTask(updatedTask)
            alarmHelper.scheduleTaskReminder(updatedTask)
        } else {
            // Completar tarefa
            taskViewModel.markTaskAsCompleted(task.id)
            alarmHelper.cancelAlarm(task.id)
            notificationHelper.cancelNotification(task.id)
            
            Snackbar.make(
                recyclerView,
                "Tarefa '${task.title}' concluída!",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Mostra opções para uma tarefa (editar, excluir)
     */
    private fun showTaskOptions(task: Task) {
        val options = arrayOf("Editar", "Excluir", "Cancelar")
        
        AlertDialog.Builder(this)
            .setTitle(task.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openTaskForm(task) // Editar
                    1 -> confirmDeleteTask(task) // Excluir
                    // 2 -> Cancelar (não faz nada)
                }
            }
            .show()
    }
    
    /**
     * Confirma exclusão de tarefa
     */
    private fun confirmDeleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Tarefa")
            .setMessage("Tem certeza que deseja excluir '${task.title}'?")
            .setPositiveButton("Excluir") { _, _ ->
                taskViewModel.deleteTask(task)
                alarmHelper.cancelAlarm(task.id)
                notificationHelper.cancelNotification(task.id)
                
                Snackbar.make(
                    recyclerView,
                    "Tarefa excluída",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Processa intent de notificação
     */
    private fun handleNotificationIntent() {
        val taskId = intent.getLongExtra("task_id", -1)
        if (taskId != -1L) {
            // Aqui você pode destacar a tarefa específica ou abrir detalhes
            Toast.makeText(this, "Tarefa selecionada da notificação", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Verifica permissões de notificação
     */
    private fun checkNotificationPermissions() {
        if (!notificationHelper.areNotificationsEnabled()) {
            AlertDialog.Builder(this)
                .setTitle("Permissão de Notificação")
                .setMessage("Para receber lembretes, habilite as notificações nas configurações.")
                .setPositiveButton("OK", null)
                .show()
        }
        
        if (!alarmHelper.canScheduleExactAlarms()) {
            AlertDialog.Builder(this)
                .setTitle("Permissão de Alarmes")
                .setMessage("Para lembretes precisos, habilite alarmes exatos nas configurações.")
                .setPositiveButton("OK", null)
                .show()
        }
    }
    
    /**
     * Mostra mensagem de erro
     */
    private fun showError(message: String) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.red_error))
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_completed -> {
                confirmDeleteCompletedTasks()
                true
            }
            R.id.action_settings -> {
                // Abrir configurações (implementar se necessário)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Confirma exclusão de tarefas completadas
     */
    private fun confirmDeleteCompletedTasks() {
        AlertDialog.Builder(this)
            .setTitle("Limpar Concluídas")
            .setMessage("Excluir todas as tarefas concluídas?")
            .setPositiveButton("Excluir") { _, _ ->
                taskViewModel.deleteCompletedTasks()
                Toast.makeText(this, "Tarefas concluídas removidas", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_ADD_TASK -> {
                    Toast.makeText(this, "Tarefa adicionada com sucesso!", Toast.LENGTH_SHORT).show()
                }
                REQUEST_EDIT_TASK -> {
                    Toast.makeText(this, "Tarefa atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
