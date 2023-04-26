package io.github.chenfei0928.widget.recyclerview.binder;

import androidx.databinding.Observable;
import androidx.databinding.ObservableList;
import androidx.databinding.ObservableMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

/**
 * @author chenf()
 * @date 2023-02-17 16:28
 */
class EnvironmentObservableRegister {
    static void register(Object observer, Object callback) {
        if (observer instanceof Observable) {
            ((Observable) observer).addOnPropertyChangedCallback((Observable.OnPropertyChangedCallback) callback);
        } else if (observer instanceof ObservableList) {
            ((ObservableList<?>) observer).addOnListChangedCallback((ObservableList.OnListChangedCallback) callback);
        } else if (observer instanceof ObservableMap) {
            ((ObservableMap<?, ?>) observer).addOnMapChangedCallback((ObservableMap.OnMapChangedCallback) callback);
        } else if (observer instanceof LiveData) {
            ((LiveData<?>) observer).observeForever((Observer) callback);
        }
    }

    static void unregister(Object observer, Object callback) {
        if (observer instanceof Observable) {
            ((Observable) observer).removeOnPropertyChangedCallback((Observable.OnPropertyChangedCallback) callback);
        } else if (observer instanceof ObservableList) {
            ((ObservableList<?>) observer).removeOnListChangedCallback((ObservableList.OnListChangedCallback) callback);
        } else if (observer instanceof ObservableMap) {
            ((ObservableMap<?, ?>) observer).removeOnMapChangedCallback((ObservableMap.OnMapChangedCallback) callback);
        } else if (observer instanceof LiveData) {
            ((LiveData<?>) observer).removeObserver((Observer) callback);
        }
    }
}
