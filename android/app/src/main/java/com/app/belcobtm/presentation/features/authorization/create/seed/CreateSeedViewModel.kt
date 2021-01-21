package com.app.belcobtm.presentation.features.authorization.create.seed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.authorization.interactor.CreateSeedUseCase
import com.app.belcobtm.domain.authorization.interactor.CreateWalletUseCase
import com.app.belcobtm.domain.authorization.interactor.SaveSeedUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class CreateSeedViewModel(
    private val createPhraseUseCase: CreateSeedUseCase,
    private val saveSeedUseCase: SaveSeedUseCase,
    private val createWalletUseCase: CreateWalletUseCase
) : ViewModel() {

    companion object {
        const val SEED_PHRASE_SIZE = 12
    }

    val seedLiveData: MutableLiveData<String> = MutableLiveData()
    val createWalletLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    fun createSeed() {
        createPhraseUseCase.invoke(
            params = Unit,
            onSuccess = { seedLiveData.value = it }
        )
    }

    fun saveSeed(seed: String) {
        saveSeedUseCase.invoke(
            params = seed,
            onSuccess = { seedLiveData.value = seed }
        )
    }

    fun createWallet(phone: String, password: String) {
        createWalletLiveData.value = LoadingData.Loading()
        createWalletUseCase.invoke(
            params = CreateWalletUseCase.Params(phone, password),
            onSuccess = { createWalletLiveData.value = LoadingData.Success(it) },
            onError = { createWalletLiveData.value = LoadingData.Error(it) }
        )
    }

    fun isValidSeed(seed: String): Boolean =
        processSeed(seed).size == SEED_PHRASE_SIZE

    private fun processSeed(seed: String) =
        seed.replace(CreateSeedFragment.CHAR_NEXT_LINE, CreateSeedFragment.CHAR_SPACE)
            .splitToSequence(CreateSeedFragment.CHAR_SPACE)
            .filter { it.isNotEmpty() }
            .toList()
}