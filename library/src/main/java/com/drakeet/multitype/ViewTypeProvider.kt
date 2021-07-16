package com.drakeet.multitype

interface ViewTypeProvider {
    /**
     * 记录某条数据将由哪种binder去渲染这条数据
     */
    val binderClazz: Class<out ItemViewDelegate<*, *>>?
}
