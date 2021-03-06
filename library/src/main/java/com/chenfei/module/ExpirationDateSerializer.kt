package com.chenfei.module

class ExpirationDateSerializer<T>(
        serializer: LocalSerializer<T>,
        private val timeout: Long
) : BaseExpirationDateSerializer<T>(serializer) {

    override fun check(localSavedTimeMillis: Long): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        return localSavedTimeMillis + timeout >= currentTimeMillis
    }
}
