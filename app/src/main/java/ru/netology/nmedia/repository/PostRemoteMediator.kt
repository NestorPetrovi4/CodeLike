package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.netology.nmedia.api.ServiceAPI
import ru.netology.nmedia.dao.PostDAO
import ru.netology.nmedia.dao.PostRemoteKeyDAO
import ru.netology.nmedia.db.AppDB
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.error.ApiError
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val serviceAPI: ServiceAPI,
    private val postDAO: PostDAO,
    private val postRemoteKeyDAO: PostRemoteKeyDAO,
    private val appDB: AppDB
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val id = postRemoteKeyDAO.max()
            val resul = when (loadType) {
                LoadType.REFRESH -> {
                    if (id == null) {
                        serviceAPI.getLatest(state.config.pageSize)
                    } else {
                        serviceAPI.getAfter(id, state.config.pageSize)
                    }
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDAO.min() ?: return MediatorResult.Success(false)
                    serviceAPI.getBefore(id, state.config.pageSize)
                }
            }
            if (!resul.isSuccessful) {
                throw HttpException(resul)
            }
            val body = resul.body() ?: throw ApiError(resul.message())
            if (body.isEmpty()) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            appDB.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        if (id == null) {
                            postRemoteKeyDAO.save(
                                listOf(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id
                                    ),
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        body.last().id
                                    )
                                )
                            )
                        } else {
                            postRemoteKeyDAO.save(
                                listOf(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id
                                    )
                                )
                            )

                        }
                    }

                    LoadType.PREPEND -> {
                        postRemoteKeyDAO.save(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        postRemoteKeyDAO.save(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id
                            )
                        )
                    }

                }

                postDAO.save(body.map { PostEntity.fromDTO(it) })
            }

            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }

    }

}