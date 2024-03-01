package io.github.chenfei0928.data

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-07-05 18:39
 */
enum class ProtobufType(
    // 使用ProtobufLite作为基类生成示例类
    internal val useProtobufLite: Boolean,
    // 不使用Lite运行时，仅当[useLite]为true时有效
    internal val useFullDependencies: Boolean,
) {
    /**
     * Lite版实体类配合Lite版运行时
     */
    Lite(true, false),

    /**
     * Lite版实体类配合标准版运行时
     */
    LiteWithFullDependencies(true, true),

    /**
     * 标准版实体类和标准版运行时
     */
    Full(false, true);

    // 只有Lite需要添加混淆表
    internal val includeProguardRule: Boolean =
        useProtobufLite
}
