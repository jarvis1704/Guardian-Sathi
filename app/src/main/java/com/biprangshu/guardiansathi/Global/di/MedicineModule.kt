package com.biprangshu.guardiansathi.Global.di

import com.biprangshu.guardiansathi.Global.core.data.MedicineRepositoryImpl
import com.biprangshu.guardiansathi.Global.core.domain.MedicineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MedicineModule {

    @Binds
    @Singleton
    abstract fun bindMedicineRepository(
        medicineRepositoryImpl: MedicineRepositoryImpl
    ): MedicineRepository
}
