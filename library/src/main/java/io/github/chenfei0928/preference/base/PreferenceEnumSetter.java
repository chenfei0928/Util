package io.github.chenfei0928.preference.base;

import android.content.Context;

import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;

/**
 * @author chenf()
 * @date 2026-01-23 10:24
 */
public interface PreferenceEnumSetter<E extends Enum<E>> {
    Context getContext();

    void setEntries(CharSequence[] entries);

    void setEntryValues(CharSequence[] entryValues);

    final class ListPreferenceImpl<E extends Enum<E>> implements PreferenceEnumSetter<E> {
        private final ListPreference listPreference;

        public ListPreferenceImpl(ListPreference listPreference) {
            this.listPreference = listPreference;
        }

        @Override
        public Context getContext() {
            return listPreference.getContext();
        }

        @Override
        public void setEntries(CharSequence[] entries) {
            listPreference.setEntries(entries);
        }

        @Override
        public void setEntryValues(CharSequence[] entryValues) {
            listPreference.setEntryValues(entryValues);
        }
    }

    final class MultiSelectListPreferenceImpl<E extends Enum<E>> implements PreferenceEnumSetter<E> {
        private final MultiSelectListPreference multiSelectListPreference;

        public MultiSelectListPreferenceImpl(MultiSelectListPreference multiSelectListPreference) {
            this.multiSelectListPreference = multiSelectListPreference;
        }

        @Override
        public Context getContext() {
            return multiSelectListPreference.getContext();
        }

        @Override
        public void setEntries(CharSequence[] entries) {
            multiSelectListPreference.setEntries(entries);
        }

        @Override
        public void setEntryValues(CharSequence[] entryValues) {
            multiSelectListPreference.setEntryValues(entryValues);
        }
    }
}
