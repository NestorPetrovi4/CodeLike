package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import ru.netology.nmedia.api.ServiceAPI
import ru.netology.nmedia.dto.Post
import java.io.IOException

class PostPagingSource(private val serviceAPI: ServiceAPI) : PagingSource<Int, Post>() {
    override fun getRefreshKey(state: PagingState<Int, Post>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        try {
            val resul = when (params) {
                is LoadParams.Refresh -> {
                    serviceAPI.getLatest(params.loadSize)
                }

                is LoadParams.Append -> {
                    serviceAPI.getBefore(id = params.key, count = params.loadSize)
                }

                is LoadParams.Prepend -> return LoadResult.Page(
                    data = emptyList(), nextKey = null, prevKey = params.key
                )
            }
            if (!resul.isSuccessful) {
                throw HttpException(resul)
            }
            val data = resul.body().orEmpty()
            return LoadResult.Page(
                data = data,
                prevKey = params.key,
                nextKey = data.lastOrNull()?.id
            )
        } catch (e: IOException) {
            return LoadResult.Error(e)
        }

    }

}