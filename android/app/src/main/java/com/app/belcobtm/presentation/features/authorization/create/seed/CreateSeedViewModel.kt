package com.app.belcobtm.presentation.features.authorization.create.seed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
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
    val invalidSeedErrorMessage: MutableLiveData<Int?> = MutableLiveData()
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

    fun createWallet(seed: String, phone: String, password: String) {
        invalidSeedErrorMessage.value = null
        if (isValidSeed(seed)) {
            createWalletLiveData.value = LoadingData.Loading()
            saveSeedAndCreateWallet(seed, phone, password)
        } else {
            invalidSeedErrorMessage.value = R.string.seed_pharse_paste_error_message
        }
    }

    private fun saveSeedAndCreateWallet(seed: String, phone: String, password: String) {
        saveSeedUseCase.invoke(
            params = seed,
            onSuccess = {
                seedLiveData.value = seed
                createWallet(phone, password)
            },
            onError = {
                createWalletLiveData.value = LoadingData.Error(it)
            }
        )
    }

    private fun createWallet(phone: String, password: String) {
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