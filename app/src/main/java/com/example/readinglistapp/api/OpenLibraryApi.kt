package com.example.readinglistapp.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class OpenLibraryIsbnResponse(
    @SerializedName("title") val title: String?,
    @SerializedName("publish_date") val publishDate: String?,
    @SerializedName("number_of_pages") val numberOfPages: Int?,
    @SerializedName("covers") val covers: List<Long>?,
    @SerializedName("authors") val authors: List<AuthorRef>?
)

data class AuthorRef(
    @SerializedName("key") val key: String?
)

data class OpenLibrarySearchResponse(
    @SerializedName("numFound") val numFound: Int,
    @SerializedName("docs") val docs: List<SearchDoc>?
)

data class SearchDoc(
    @SerializedName("key") val key: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("author_name") val authorNames: List<String>?,
    @SerializedName("first_publish_year") val firstPublishYear: Int?,
    @SerializedName("isbn") val isbns: List<String>?,
    @SerializedName("cover_i") val coverI: Long?
)

interface OpenLibraryService {
    @GET("isbn/{isbn}.json")
    suspend fun getBookByIsbn(@Path("isbn") isbn: String): OpenLibraryIsbnResponse

    @GET("search.json")
    suspend fun searchBooks(@Query("q") query: String): OpenLibrarySearchResponse
}

object OpenLibraryApiClient {
    private const val BASE_URL = "https://openlibrary.org/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    val service: OpenLibraryService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenLibraryService::class.java)
    }

    fun getCoverUrlByIsbn(isbn: String): String {
        return "https://covers.openlibrary.org/b/isbn/$isbn-M.jpg"
    }

    fun getCoverUrlByCoverId(coverId: Long): String {
        return "https://covers.openlibrary.org/b/id/$coverId-M.jpg"
    }
}
