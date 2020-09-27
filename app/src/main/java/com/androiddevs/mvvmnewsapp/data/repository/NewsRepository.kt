package com.androiddevs.mvvmnewsapp.data.repository

import com.androiddevs.mvvmnewsapp.data.api.RetrofitInstance
import com.androiddevs.mvvmnewsapp.data.db.ArticleDatabase
import com.androiddevs.mvvmnewsapp.data.model.Article

class NewsRepository(private val database: ArticleDatabase) {
    suspend fun getArticle(countryCode: String, pageNo: Int) =
        RetrofitInstance.api.getBreakingNews(countryCode, pageNo)

    suspend fun getSearchedArticle(query: String, pageNo: Int) =
        RetrofitInstance.api.searchForNews(query, pageNo)

    suspend fun upsert(article: Article) = database.getArticleDao().insertupdate(article)
     fun getAll()=database.getArticleDao().getAllArticles()
    suspend fun delete(article: Article) = database.getArticleDao().deleteArticle(article)
}