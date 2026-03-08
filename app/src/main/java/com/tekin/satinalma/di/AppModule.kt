/*
 * AppModule.kt
 * Hilt dependency injection modülü.
 * Repository bağlamaları ve uygulama genelinde singleton nesneler burada tanımlanır.
 */
package com.tekin.satinalma.di

import com.tekin.satinalma.data.repository.PurchaseRepositoryImpl
import com.tekin.satinalma.domain.repository.PurchaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Uygulama geneli dependency injection modülü
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindPurchaseRepository(
        impl: PurchaseRepositoryImpl
    ): PurchaseRepository
}
