package io.github.chenfei0928.util

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

/**
 * @author chenf()
 * @date 2026-01-20 11:01
 */
class ExistTaskProvider<T : Task>(
    override val value: T
) : TaskProvider<T>, AbsProvider<T>() {
    override fun configure(action: Action<in T>) = action.execute(value)
    override fun getName(): String = value.name
}
