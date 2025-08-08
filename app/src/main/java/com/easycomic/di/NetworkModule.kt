package com.easycomic.di

import com.easycomic.data.remote.SardineWebDavDataSource
import com.easycomic.data.remote.WebDavDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    @Binds
    @Singleton
    abstract fun bindWebDavDataSource(impl: SardineWebDavDataSource): WebDavDataSource
}
