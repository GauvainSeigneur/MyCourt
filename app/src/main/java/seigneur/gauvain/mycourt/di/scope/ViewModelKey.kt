package seigneur.gauvain.mycourt.di.scope

import androidx.lifecycle.ViewModel

import dagger.MapKey
import kotlin.reflect.KClass

/**
 * Created by Philippe on 02/03/2018.
 */

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

/*
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@MapKey
public @interface ViewModelKey {
    Class<? extends ViewModel> value();
}
 */