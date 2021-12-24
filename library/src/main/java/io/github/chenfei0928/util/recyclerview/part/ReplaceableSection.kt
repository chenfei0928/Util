package io.github.chenfei0928.util.recyclerview.part

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-06-29 14:45
 */
interface ReplaceableSection<Bean> {
    val section: Any
    var data: List<Bean>
}
