/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-08-06 14:30
 */
package io.github.chenfei0928.util.kotlin

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.forEach

fun PreferenceFragmentCompat.forEachChild(block: (Preference) -> Unit) {
    preferenceScreen.forEachChild(block)
}

/**
 * 对某[PreferenceGroup]的每一个子[Preference]，进行处理。
 * 如果某个子[Preference]是[PreferenceGroup] ，也会处理该子[PreferenceGroup]的子[Preference]。
 *
 * 此处含有递归调用，但无法被修改为尾递归优化（调用自身后无其他操作的递归叫尾递归），因为其还要继续遍历同级preference
 * 同时也无法被优化为inline函数（inline不能包含递归调用），不要尝试浪费力气优化该Function穿参
 */
private fun PreferenceGroup.forEachChild(block: (Preference) -> Unit) {
    forEach { preference ->
        block(preference)
        if (preference is PreferenceGroup) {
            preference.forEachChild(block)
        }
    }
}
