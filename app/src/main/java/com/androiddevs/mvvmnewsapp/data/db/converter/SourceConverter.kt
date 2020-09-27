package com.androiddevs.mvvmnewsapp.data.db.converter

import androidx.room.TypeConverter
import com.androiddevs.mvvmnewsapp.data.model.Source

class SourceConverter {
    @TypeConverter
    fun sourceToString(source: Source): String {
        return source.name
    }

    @TypeConverter
    fun stringToSource(name: String): Source {
        return Source(name, name)
    }
}