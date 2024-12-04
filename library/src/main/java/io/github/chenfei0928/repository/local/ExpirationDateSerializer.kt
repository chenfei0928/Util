package io.github.chenfei0928.repository.local

/**
 * 超时保质期序列化
 *
 * @author chenfei(chenfei@gmail.com)
 * @date 2022-01-12 10:25
 */
class ExpirationDateSerializer<T>(
    serializer: LocalSerializer<T>,
    private val timeout: Long
) : BaseExpirationDateSerializer<T>(serializer) {

    override fun check(localSavedTimeMillis: Long): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        return localSavedTimeMillis + timeout >= currentTimeMillis
    }
}