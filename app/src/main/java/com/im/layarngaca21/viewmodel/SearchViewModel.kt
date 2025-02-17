package com.im.layarngaca21.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.im.layarngaca21.BuildConfig
import com.im.layarngaca21.R
import com.im.layarngaca21.database.AppDatabase
import com.im.layarngaca21.database.entity.Favorite
import com.im.layarngaca21.database.repository.FavoriteRepository

import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import com.im.layarngaca21.model.Movie
import com.im.layarngaca21.model.MovieResponse
import com.im.layarngaca21.model.TV
import com.im.layarngaca21.model.TVResponse
import com.im.layarngaca21.utils.LiveMessageEvent
import com.im.layarngaca21.utils.ViewMessages
import com.im.layarngaca21.utils.values.CategoryEnum
import com.im.layarngaca21.utils.values.ResponseCodeEnum
import com.im.layarngaca21.utils.values.ToastEnum


class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val listMovies:MutableLiveData<List<Movie>> = MutableLiveData()
    private val listTv:MutableLiveData<List<TV>> = MutableLiveData()
    val messagesEvent = LiveMessageEvent<ViewMessages>()
    private var db: AppDatabase? = null
    private lateinit var repoMovie: FavoriteRepository
    private lateinit var repoTV: FavoriteRepository
    private lateinit var movieFavorite: LiveData<List<Favorite>>
    private lateinit var tvFavorite: LiveData<List<Favorite>>
    private val app = application


    internal fun onViewAttached(){
        db = AppDatabase.getAppDataBase(app)
        repoMovie = FavoriteRepository(db!!.favoriteDao(), CategoryEnum.MOVIE.value)
        repoTV = FavoriteRepository(db!!.favoriteDao(), CategoryEnum.TV.value)
        movieFavorite = repoMovie.getAllFavorites()
        tvFavorite = repoTV.getAllFavorites()
    }
    internal fun searchMovies(keyWord: String) {
        val client = AsyncHttpClient()
        val url = "https://api.themoviedb.org/3/search/movie?api_key=${BuildConfig.MOVIE_API_KEY}&language=en-US&query=$keyWord"
            client.get(url, object : AsyncHttpResponseHandler() {

                override fun onSuccess(statusCode: Int, headers: Array<Header>?, responseBody: ByteArray) {
                        if(statusCode==ResponseCodeEnum.OK.code){
                            val movieResponse = Gson().fromJson(String(responseBody), MovieResponse::class.java)
                            listMovies.postValue(movieResponse.results)
                        }else{
                            messagesEvent.sendEvent {
                                Log.d("MovieViewModel",responseBody.toString())
                                showMessage(R.string.error_msg_bad_connection, ToastEnum.FAILED.value)}
                        }


                }

                override fun onFailure(statusCode: Int, headers: Array<Header>?, responseBody: ByteArray?, error: Throwable) {
                    Log.d("onFailure", error.message)
                    messagesEvent.sendEvent { showMessage(R.string.error_msg_bad_connection, ToastEnum.FAILED.value)}
                }
            })
    }


    internal fun searchTVShows(keyWord: String) {
        val client = AsyncHttpClient()
        val url = "https://api.themoviedb.org/3/search/tv?api_key=${BuildConfig.MOVIE_API_KEY}&language=en-US&query=$keyWord"

        try {
            client.get(url, object : AsyncHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<Header>?, responseBody: ByteArray) {
                    if(statusCode== ResponseCodeEnum.OK.code) {
                        val tvResponse = Gson().fromJson(String(responseBody), TVResponse::class.java)
                        listTv.postValue(tvResponse.results)
                    }else{
                        messagesEvent.sendEvent { showMessage(R.string.error_msg_bad_connection, ToastEnum.FAILED.value)}
                    }

                }

                override fun onFailure(statusCode: Int, headers: Array<Header>?, responseBody: ByteArray?, error: Throwable) {
                    messagesEvent.sendEvent { showMessage(R.string.error_msg_bad_connection, ToastEnum.FAILED.value)}
                    Log.d("onFailure", error.message)
                }
            })
        } catch (e: Exception) {
            Log.d("Exception", e.message)
        }
    }

    internal fun getMovies(): LiveData<List<Movie>?> {
        return listMovies
    }

    internal fun getTVShows(): LiveData<List<TV>?> {
        return listTv
    }

    internal fun insertMovieFavorite(fav: Favorite) {
        repoMovie.insert(fav)
    }

    internal fun insertTvFavorite(fav: Favorite) {
        repoTV.insert(fav)
    }

    internal fun deleteMovieFavorite(fav: Favorite) {
        repoMovie.delete(fav)
    }

    internal fun deleteTvFavorite(fav: Favorite) {
        repoTV.delete(fav)
    }

    internal fun getMovieFavorites(): LiveData<List<Favorite>> {
        return movieFavorite
    }

    internal fun getTvFavorites(): LiveData<List<Favorite>> {
        return tvFavorite
    }

}
