package com.organiza.ai.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.organiza.ai.R
import com.organiza.ai.model.Task
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para RecyclerView que exibe a lista de tarefas
 * Implementa ListAdapter para melhor performance com DiffUtil
 */
class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskLongClick: (Task) -> Unit,
    private val onTaskCompleted: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {
    
    /**
     * ViewHolder para cada item da lista de tarefas
     */
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxCompleted)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textDescription: TextView = itemView.findViewById(R.id.textDescription)
        val textDateTime: TextView = itemView.findViewById(R.id.textDateTime)
        val textCategory: TextView = itemView.findViewById(R.id.textCategory)
        val viewPriority: View = itemView.findViewById(R.id.viewPriority)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        bindTask(holder, task)
    }
    
    /**
     * Vincula os dados da tarefa aos views do ViewHolder
     */
    private fun bindTask(holder: TaskViewHolder, task: Task) {
        with(holder) {
            // Configura checkbox
            checkBox.isChecked = task.isCompleted
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != task.isCompleted) {
                    onTaskCompleted(task)
                }
            }
            
            // Configura textos
            textTitle.text = task.title
            textDescription.text = task.description
            textCategory.text = task.category
            
            // Formata data e hora
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            textDateTime.text = dateFormat.format(task.dateTime)
            
            // Configura aparência baseada no status
            updateTaskAppearance(holder, task)
            
            // Configura cliques
            itemView.setOnClickListener { onTaskClick(task) }
            itemView.setOnLongClickListener { 
                onTaskLongClick(task)
                true
            }
        }
    }
    
    /**
     * Atualiza a aparência visual baseada no status da tarefa
     */
    private fun updateTaskAppearance(holder: TaskViewHolder, task: Task) {
        val context = holder.itemView.context
        
        when {
            task.isCompleted -> {
                // Tarefa completada - texto riscado e cor acinzentada
                holder.textTitle.alpha = 0.6f
                holder.textDescription.alpha = 0.6f
                holder.textDateTime.alpha = 0.6f
                holder.textCategory.alpha = 0.6f
                holder.viewPriority.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.gray_light)
                )
            }
            task.isOverdue() -> {
                // Tarefa atrasada - cor vermelha
                holder.textTitle.alpha = 1.0f
                holder.textDescription.alpha = 1.0f
                holder.textDateTime.alpha = 1.0f
                holder.textCategory.alpha = 1.0f
                holder.textDateTime.setTextColor(
                    ContextCompat.getColor(context, R.color.red_error)
                )
                holder.viewPriority.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.red_error)
                )
            }
            task.isNearDeadline() -> {
                // Tarefa próxima do prazo - cor laranja
                holder.textTitle.alpha = 1.0f
                holder.textDescription.alpha = 1.0f
                holder.textDateTime.alpha = 1.0f
                holder.textCategory.alpha = 1.0f
                holder.textDateTime.setTextColor(
                    ContextCompat.getColor(context, R.color.orange_warning)
                )
                holder.viewPriority.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.orange_warning)
                )
            }
            else -> {
                // Tarefa normal - cores padrão
                holder.textTitle.alpha = 1.0f
                holder.textDescription.alpha = 1.0f
                holder.textDateTime.alpha = 1.0f
                holder.textCategory.alpha = 1.0f
                holder.textDateTime.setTextColor(
                    ContextCompat.getColor(context, R.color.text_secondary)
                )
                holder.viewPriority.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.blue_primary)
                )
            }
        }
    }
    
    /**
     * DiffCallback para otimizar atualizações da lista
     */
    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
