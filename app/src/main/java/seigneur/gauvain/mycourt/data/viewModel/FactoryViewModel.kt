package seigneur.gauvain.mycourt.data.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class FactoryViewModel


/**
 * BASED ON :
 * https://github.com/googlesamples/android-architecture-components/blob/master/GithubBrowserSample/app/src/main/java/com/android/example/github/viewmodel/GithubViewModelFactory.kt
 *
 * Kotlin Conversion issue :
 * without adding @JvmSuppressWildcards to Provider<ViewModel>> Dagger can't create multi-binding of instance of View model
 * Article which explains this problems :
 * https://blog.kotlin-academy.com/understanding-dagger-2-multibindings-viewmodel-8418eb372848
 */
@Inject
constructor(private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass] ?: creators.entries.firstOrNull {
            modelClass.isAssignableFrom(it.key)
        }?.value ?: throw IllegalArgumentException("unknown model class $modelClass")
        try {
            @Suppress("UNCHECKED_CAST")
            return creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}

