package com.androiddevs.mvvmnewsapp.data.db

import android.content.Context
import androidx.room.*
import com.androiddevs.mvvmnewsapp.data.model.Article
import com.androiddevs.mvvmnewsapp.data.db.converter.SourceConverter

@Database(entities = [Article::class], version = 1)
@TypeConverters(SourceConverter::class)
abstract class ArticleDatabase : RoomDatabase() {
    abstract fun getArticleDao(): ArticleDao

    companion object {
        @Volatile
        private var instance: ArticleDatabase? = null
        private val Lock = Any()
        operator fun invoke(context: Context) = instance ?: synchronized(Lock) {
            instance ?: creatDatabase(context).also {
                instance = it
            }
        }

        private fun creatDatabase(context: Context): ArticleDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ArticleDatabase::class.java,
                "article_db.db"
            ).build()
        }
    }
}