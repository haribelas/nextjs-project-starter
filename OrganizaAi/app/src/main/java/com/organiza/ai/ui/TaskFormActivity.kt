package com.organiza.ai.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.organiza.ai.R
import com.organiza.ai.model.Task
import com.organiza.ai.utils.AlarmHelper
import com.organiza.ai.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Atividade para criar e editar tarefas
 * Contém formulário com todos os campos necessários para uma tarefa
 */
class TaskFormActivity : AppCompatActivity() {
    
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var alarmHelper: AlarmHelper
    
    // Views do formulário
    private lateinit var editTextTitle: TextInputEditText
    private lateinit var editTextDescription: TextInputEditText
    private lateinit var textInputLayoutTitle: TextInputLayout
    private lateinit var textInputLayoutDescription: TextInputLayout
    private lateinit var buttonSelectDate: Button
    private lateinit var buttonSelectTime: Button
    private lateinit var spinnerCategory: Spinner
    private lateinit var textSelectedDateTime: TextView
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    
    // Dados da tarefa
    private var selectedDate: Calendar = Calendar.getInstance()
    private var taskId: Long = -1
    private var isEditMode = false
    private var currentTask: Task? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_form)
        
        // Inicializa helpers
        alarmHelper = AlarmHelper(this)
        
        // Verifica se é modo de edição
        taskId = intent.getLongExtra("task_id", -1)
        isEditMode = taskId != -1L
        
        // Configura views
        setupViews()
        setupSpinner()
        setupObservers()
        
        // Carrega dados se for edição
        if (isEditMode) {
            loadTaskData()
        } else {
            // Define data/hora padrão para nova tarefa (1 hora a partir de agora)
            selectedDate.add(Calendar.HOUR_OF_DAY, 1)
            updateDateTimeDisplay()
        }
    }
    
    /**
     * Configura as views da tela
     */
    private fun setupViews() {
        // Configura toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = if (isEditMode) "Editar Tarefa" else "Nova Tarefa"
            setDisplayHomeAsUpEnabled(true)
        }
        
        // Inicializa views
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        textInputLayoutTitle = findViewById(R.id.textInputLayoutTitle)
        textInputLayoutDescription = findViewById(R.id.textInputLayoutDescription)
        buttonSelectDate = findViewById(R.id.buttonSelectDate)
        buttonSelectTime = findViewById(R.id.buttonSelectTime)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        textSelectedDateTime = findViewById(R.id.textSelectedDateTime)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCancel = findViewById(R.id.buttonCancel)
        
        // Configura listeners
        buttonSelectDate.setOnClickListener { showDatePicker() }
        buttonSelectTime.setOnClickListener { showTimePicker() }
        buttonSave.setOnClickListener { saveTask() }
        buttonCancel.setOnClickListener { finish() }
        
        // Atualiza texto do botão
        buttonSave.text = if (isEditMode) "Atualizar" else "Salvar"
    }
    
    /**
     * Configura o spinner de categorias
     */
    private fun setupSpinner() {
        val categories = Task.Category.values().map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }
    
    /**
     * Configura observadores do ViewModel
     */
    private fun setupObservers() {
        taskViewModel.taskSaved.observe(this) { saved ->
            if (saved) {
                setResult(RESULT_OK)
                finish()
            }
        }
        
        taskViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                showError(it)
                taskViewModel.clearErrorMessage()
            }
        }
        
        taskViewModel.isLoading.observe(this) { isLoading ->
            buttonSave.isEnabled = !isLoading
            buttonSave.text = if (isLoading) {
                if (isEditMode) "Atualizando..." else "Salvando..."
            } else {
                if (isEditMode) "Atualizar" else "Salvar"
            }
        }
    }
    
    /**
     * Carrega dados da tarefa para edição
     */
    private fun loadTaskData() {
        lifecycleScope.launch {
            currentTask = taskViewModel.getTaskById(taskId)
            currentTask?.let { task ->
                editTextTitle.setText(task.title)
                editTextDescription.setText(task.description)
                
                // Configura data/hora
                selectedDate.time = task.dateTime
                updateDateTimeDisplay()
                
                // Configura categoria
                val categoryIndex = Task.Category.values().indexOfFirst { 
                    it.displayName == task.category 
                }
                if (categoryIndex >= 0) {
                    spinnerCategory.setSelection(categoryIndex)
                }
            }
        }
    }
    
    /**
     * Mostra seletor de data
     */
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateTimeDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        
        // Define data mínima como hoje
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    /**
     * Mostra seletor de hora
     */
    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)
                selectedDate.set(Calendar.SECOND, 0)
                updateDateTimeDisplay()
            },
            selectedDate.get(Calendar.HOUR_OF_DAY),
            selectedDate.get(Calendar.MINUTE),
            true // Formato 24 horas
        )
        timePickerDialog.show()
    }
    
    /**
     * Atualiza a exibição da data/hora selecionada
     */
    private fun updateDateTimeDisplay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
        textSelectedDateTime.text = dateFormat.format(selectedDate.time)
    }
    
    /**
     * Valida e salva a tarefa
     */
    private fun saveTask() {
        // Limpa erros anteriores
        textInputLayoutTitle.error = null
        textInputLayoutDescription.error = null
        
        // Validações
        val title = editTextTitle.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val selectedCategory = Task.Category.values()[spinnerCategory.selectedItemPosition]
        
        var hasError = false
        
        if (title.isEmpty()) {
            textInputLayoutTitle.error = "Título é obrigatório"
            hasError = true
        }
        
        if (description.isEmpty()) {
            textInputLayoutDescription.error = "Descrição é obrigatória"
            hasError = true
        }
        
        // Verifica se a data/hora é no futuro
        if (selectedDate.timeInMillis <= System.currentTimeMillis()) {
            showError("A data e hora devem ser no futuro")
            hasError = true
        }
        
        if (hasError) return
        
        // Cria ou atualiza tarefa
        val task = if (isEditMode && currentTask != null) {
            currentTask!!.copy(
                title = title,
                description = description,
                dateTime = selectedDate.time,
                category = selectedCategory.displayName,
                updatedAt = Date()
            )
        } else {
            Task(
                title = title,
                description = description,
                dateTime = selectedDate.time,
                category = selectedCategory.displayName
            )
        }
        
        // Salva no banco
        if (isEditMode) {
            taskViewModel.updateTask(task)
            // Reagenda lembrete
            alarmHelper.rescheduleTaskReminder(task)
        } else {
            taskViewModel.insertTask(task)
        }
    }
    
    /**
     * Mostra mensagem de erro
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isEditMode) {
            menuInflater.inflate(R.menu.menu_task_form, menu)
        }
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_delete -> {
                confirmDeleteTask()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Confirma exclusão da tarefa
     */
    private fun confirmDeleteTask() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Excluir Tarefa")
            .setMessage("Tem certeza que deseja excluir esta tarefa?")
            .setPositiveButton("Excluir") { _, _ ->
                currentTask?.let { task ->
                    taskViewModel.deleteTask(task)
                    alarmHelper.cancelAlarm(task.id)
                    setResult(RESULT_OK)
                    finish()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    override fun onBackPressed() {
        // Verifica se há mudanças não salvas
        if (hasUnsavedChanges()) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Descartar Alterações")
                .setMessage("Você tem alterações não salvas. Deseja sair mesmo assim?")
                .setPositiveButton("Sair") { _, _ -> super.onBackPressed() }
                .setNegativeButton("Continuar Editando", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
    
    /**
     * Verifica se há mudanças não salvas
     */
    private fun hasUnsavedChanges(): Boolean {
        if (!isEditMode) {
            return editTextTitle.text.toString().trim().isNotEmpty() ||
                   editTextDescription.text.toString().trim().isNotEmpty()
        }
        
        currentTask?.let { task ->
            return editTextTitle.text.toString().trim() != task.title ||
                   editTextDescription.text.toString().trim() != task.description ||
                   selectedDate.time != task.dateTime ||
                   Task.Category.values()[spinnerCategory.selectedItemPosition].displayName != task.category
        }
        
        return false
    }
}
