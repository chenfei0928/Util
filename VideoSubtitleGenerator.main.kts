#!/usr/bin/env kotlin

import java.io.File
import java.util.concurrent.TimeUnit

// ===================== 核心配置（无需修改，自动匹配同目录文件） =====================

val appDataRoaming = System.getenv("APPDATA")
val local = System.getenv("LOCALAPPDATA")
val userHome = System.getenv("USERPROFILE")

val workType = args[0]
val CURRENT_DIR = File(args[1])
val WHISPER_CPP = File("""C:\Program Files\DAUM\PotPlayer\Module\Whisper\Vulkan\main64.exe""")
val FASTER_WHISPER =
    File(appDataRoaming, """\PotPlayerMini64\Engine\Faster-Whisper-XXL\faster-whisper-xxl.exe""")
val MODEL = File(appDataRoaming, """\PotPlayerMini64\Model\ggml-large-v3-turbo.bin""")
val FFMPEG = File("""D:\Program Files\MediaCoder\codecs64\ffmpeg.exe""")

// 支持的视频格式
val VIDEO_FORMATS = setOf("mp4", "mkv", "mov", "avi", "flv", "wmv")
// 已处理标记：存在这些文件则跳过
val SKIP_FLAGS = setOf(".srt", ".wav", "_字幕版.mp4")

fun main() {
    println("==============================================")
    println("🎬 本地视频批量字幕生成工具 (Kotlin)")
    println("📂 当前目录：${CURRENT_DIR.absolutePath}")
    println("🚀 使用模型：ggml-large-v3-turbo")
    println("==============================================\n")

    // 遍历目录所有视频
    fun File.listVideosWithChildDir(): List<File> = listFiles()?.flatMap { file ->
        if (file.isDirectory) file.listVideosWithChildDir()
        else if (VIDEO_FORMATS.contains(file.extension.lowercase())) listOf(file)
        else emptyList()
    } ?: emptyList()

    val videos = CURRENT_DIR.listVideosWithChildDir()

    if (videos.isEmpty()) {
        println("❌ 未找到任何视频文件！")
        return
    }

    println("✅ 找到 ${videos.size} 个视频，开始检查是否已处理...\n")

    videos.shuffled().forEachIndexed { index, video ->
        val baseName = video.nameWithoutExtension
        val alreadyProcessed = SKIP_FLAGS.any { flag ->
            video.parentFile.resolve("$baseName$flag").exists()
        }

        if (alreadyProcessed) {
            println("⏭️  第 ${index + 1}/${videos.size}：${video.name} → 已生成字幕，跳过")
            return@forEachIndexed
        }

        println("\n🎯 第 ${index + 1}/${videos.size}：开始处理 → ${video.name}")
        try {
            processVideo(video)
            println("✅ 完成：${video.name}")
        } catch (e: Exception) {
            println("❌ 失败：${video.name} → ${e.message}")
        }
    }

    println("\n🎉 所有视频处理完成！")
}

/**
 * 处理单个视频：提取音频 → 生成字幕 → 嵌入视频
 */
fun processVideo(video: File) {
    val base = video.nameWithoutExtension
    val audio = video.parentFile.resolve("$base.wav")
    val srt = video.parentFile.resolve("$base.srt")
    val outputVideo = video.parentFile.resolve("${base}_字幕版.mp4")

    try {
        // 1. 提取音频（whisper标准格式：16k 单声道）
        exec(
            video.parentFile,
            arrayOf(
                FFMPEG.absolutePath,
                "-i",
                video.absolutePath,
                "-ar",
                "16000",
                "-ac",
                "1",
                "-b:a",
                "128k",
                "-y",
                audio.absolutePath
            ),
            "提取音频"
        )

        // 2. 语音识别生成 SRT 字幕（二选一，默认用 Vulkan 加速版）
        generateSubtitle(audio, srt)

        // 3. 嵌入字幕到视频
        // exec(
        //     "$FFMPEG -i \"${video.absolutePath}\" -i \"${srt.absolutePath}\" -c copy -c:s mov_text -y \"${outputVideo.absolutePath}\"",
        //     "嵌入字幕"
        // )
    } finally {
        // 清理临时音频
        audio.delete()
    }
}

/**
 * 生成字幕（默认：Whisper.cpp-Vulkan 显卡加速）
 * 想用 faster-whisper-xxl 就注释掉第一行，打开第二行
 */
fun generateSubtitle(audio: File, srt: File) {
    // ========== 选择识别引擎 ==========
    if (workType.equals("gpu", true)) {
        // 1. Vulkan 加速（推荐：速度最快）
        exec(
            audio.parentFile,
            arrayOf(
                WHISPER_CPP.absolutePath,
                "-m",
                MODEL.absolutePath,
                "-f",
                audio.absolutePath,
                "-l",
                "zh",
                "-osrt",
                "-of",
                srt.nameWithoutExtension,
            ),
            "生成字幕"
        )
    } else if (workType.equals("cpu", true)) {
        // 2. faster-whisper-xxl（精准度更高）
        exec(
            audio.parentFile,
            arrayOf(
                FASTER_WHISPER.absolutePath,
                "--model",
                "large-v3-turbo",
                "--model_dir",
                MODEL.parentFile.absolutePath,
                "--output_format",
                "srt",
                "--output_dir",
                audio.parentFile.absolutePath,
                audio.absolutePath,
            ),
            "生成字幕"
        )
    }
}

/**
 * 执行系统命令（通用方法）
 */
fun exec(workDir:File, command: Array<String>, task: String) {
    println("🔨 $task → 执行：${command.contentToString()}")

    val process = ProcessBuilder(*command)
        .redirectErrorStream(true)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .directory(workDir)
        .start()

    // 等待执行完成（最长10分钟）
    val success = process.waitFor(100, TimeUnit.MINUTES)
    if (!success) {
        process.destroy()
        throw RuntimeException("$task 超时")
    }

    val code = process.exitValue()
    if (code != 0) {
        throw RuntimeException("$task 失败，退出码：$code")
    }
}

main()
