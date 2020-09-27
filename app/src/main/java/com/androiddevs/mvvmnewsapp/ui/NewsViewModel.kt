package com.androiddevs.mvvmnewsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.androiddevs.mvvmnewsapp.NewsApplication
import com.androiddevs.mvvmnewsapp.data.model.Article
import com.androiddevs.mvvmnewsapp.data.model.NewsResponse
import com.androiddevs.mvvmnewsapp.data.repository.NewsRepository
import com.androiddevs.mvvmnewsapp.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    val app: Application,
    private val repository: NewsRepository
) : AndroidViewModel(app) {
    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse: NewsResponse? = null
    val searchingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchingNewsPage = 1
    var searchNewsResponse: NewsResponse? = null


    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode: String) {
        safeBreakingNewsCall(countryCode)
    }

    private fun handleBreakingNews(response: Response<NewsResponse>): Resource<NewsResponse>? {
        if (response.isSuccessful) {
            response.body()?.also {
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = it
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = it.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success<NewsResponse>(breakingNewsResponse ?: it)
            }
        }
        return Resource.Failed<NewsResponse>( response.message(),response.body())
    }

    fun getSearchedArticle(query: String) {
        safeSearchNewsCall(query)
    }

    private fun handleSearchingNews(response: Response<NewsResponse>): Resource<NewsResponse>? {
        if (response.isSuccessful) {
            response.body()?.also {
                searchingNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = it
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = it.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success<NewsResponse>(searchNewsResponse ?: it)
            }
        }
        return Resource.Failed<NewsResponse>( response.message(),response.body())
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        repository.upsert(article)
    }

    fun getSavedArticle() = repository.getAll()
    fun deleteArticle(article: Article) = viewModelScope.launch {
        repository.delete(article)
    }

    private fun safeBreakingNewsCall(countryCode: String) =
        viewModelScope.launch {
            breakingNews.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = repository.getArticle(countryCode, breakingNewsPage)
                    breakingNews.postValue(handleBreakingNews(response))
                } else {
                    breakingNews.postValue(Resource.Failed("No InternetConnection"))
                }
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> breakingNews.postValue(Resource.Failed("Network Failed"))
                    else -> breakingNews.postValue(Resource.Failed("Conversion Error"))
                }
            }

        }

    private fun safeSearchNewsCall(query: String) =
        viewModelScope.launch {
            searchingNews.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = repository.getSearchedArticle(query, searchingNewsPage)
                    searchingNews.postValue(handleSearchingNews(response))
                } else {
                    searchingNews.postValue(Resource.Failed("No InternetConnection"))
                }
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> searchingNews.postValue(Resource.Failed("Network Failed"))
                    else -> searchingNews.postValue(Resource.Failed("Conversion Error"))
                }
            }

        }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager =
            getApplication<NewsApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> return false

            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false


                }
            }
        }
        return false
    }
}