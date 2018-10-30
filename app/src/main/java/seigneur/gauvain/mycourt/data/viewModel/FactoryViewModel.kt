package seigneur.gauvain.mycourt.data.viewModel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class FactoryViewModel


/**
 * Kotlin Conversion issue :
 * without adding @JvmSuppressWildcards to Provider<ViewModel>> Dagger can't create multi-binding of instance of View model
 * Article which explains this problems :
 * https://blog.kotlin-academy.com/understanding-dagger-2-multibindings-viewmodel-8418eb372848
 *
 * TODO - GIST which propose a lighter solution : https://gist.github.com/krage/058074b40d0819c4b73e43ab9d1afdde
 * TODO - OR ANOTHER gist : https://gist.github.com/Elforama/969c2de0b3227f927fbf3f65654acf63
 *
 */

@Inject
constructor(private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        var creator: Provider<out ViewModel>? = creators[modelClass]
        if (creator == null) {
            for ((key, value) in creators) {
                if (modelClass.isAssignableFrom(key)) {
                    creator = value
                    break
                }
            }
        }
        if (creator == null) {
            throw IllegalArgumentException("FactoryViewModel: unknown model class $modelClass")
        }
        try {
            return creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}
