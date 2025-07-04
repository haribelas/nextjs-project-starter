package com.organiza.ai.data

import androidx.room.TypeConverter
import java.util.Date

/**
 * Converters para tipos de dados customizados no Room
 * Converte Date para Long e vice-versa para armazenamento no banco
 */
class Converters {
    
    /**
     * Converte Date para Long (timestamp)
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * Converte Long (timestamp) para Date
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}
