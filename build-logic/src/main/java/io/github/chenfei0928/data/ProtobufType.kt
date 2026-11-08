package io.github.chenfei0928.data

/**
 * @param useProtobufLite 使用ProtobufLite作为基类生成示例类
 * @param useFullDependencies 如果使用ProtobufLite作为基类时（即[useProtobufLite]为true时），
 * 是否使用Lite或Full运行时
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-07-05 18:39
 */
enum class ProtobufType(
    internal val useProtobufLite: Boolean,
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
