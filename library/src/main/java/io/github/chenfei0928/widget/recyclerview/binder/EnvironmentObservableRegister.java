package io.github.chenfei0928.widget.recyclerview.binder;

import androidx.databinding.Observable;
import androidx.databinding.ObservableList;
import androidx.databinding.ObservableMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

/**
 * @author chenf()
 * @date 2023-02-17 16:28
 * @noinspection unchecked, rawtypes
 */
class EnvironmentObservableRegister {
    private EnvironmentObservableRegister() {
    }

    static void register(Object observer, Object callback) {
        if (observer instanceof Observable o) {
            o.addOnPropertyChangedCallback((Observable.OnPropertyChangedCallback) callback);
        } else if (observer instanceof ObservableList<?> o) {
            o.addOnListChangedCallback((ObservableList.OnListChangedCallback) callback);
        } else if (observer instanceof ObservableMap<?, ?> o) {
            o.addOnMapChangedCallback((ObservableMap.OnMapChangedCallback) callback);
        } else if (observer instanceof LiveData<?> o) {
            o.observeForever((Observer) callback);
        }
    }

    static void unregister(Object observer, Object callback) {
        if (observer instanceof Observable o) {
            o.removeOnPropertyChangedCallback((Observable.OnPropertyChangedCallback) callback);
        } else if (observer instanceof ObservableList<?> o) {
            o.removeOnListChangedCallback((ObservableList.OnListChangedCallback) callback);
        } else if (observer instanceof ObservableMap<?, ?> o) {
            o.removeOnMapChangedCallback((ObservableMap.OnMapChangedCallback) callback);
        } else if (observer instanceof LiveData<?> o) {
            o.removeObserver((Observer) callback);
        }
    }
}
