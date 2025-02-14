package androidx.preference;

/**
 * @author chenf()
 * @date 2025-02-14 17:46
 */
public class PreferenceHelper {
    public static void notifyValueChanged(Preference preference) {
        preference.onSetInitialValue(null);
    }
}
