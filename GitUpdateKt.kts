package io.github0928.script

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val EXECUTOR: ExecutorService = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors() * 4
)

private val targetDir = File("/mnt/f/open_source")

targetDir.runCommand("git", "--version").waitFor()
targetDir.runCommand("git", "config", "--global", "-l").waitFor()
println("工作目录：$targetDir")
val ignoreDirs = File(targetDir, "ignore.txt").readLines()
println("忽略的Git工程：$ignoreDirs")

val files: Array<File> = targetDir.listFiles { f ->
    f.isDirectory && f.name !in ignoreDirs
}!!
files.shuffle()
val filesList = Collections.synchronizedList(files.toMutableList())
files.map {
    EXECUTOR.submit {
        try {
            checkToSync(GitDir(it))
        } catch (e: Throwable) {
            System.err.println("项目：$it 同步失败：$e")
        }
        filesList.remove(it)
        println("剩余进度项目：${filesList.size}/${files.size}")
        if (filesList.size < 10) {
            println("剩余项目：$filesList")
        }
    }
}.forEach {
    it.get()
}
EXECUTOR.shutdown()

@Throws(IOException::class, InterruptedException::class)
private fun checkToSync(gitDir: GitDir) {
    println("同步Git目录：${gitDir.dir}")
    syncGit(gitDir)
    println("同步Git目录：${gitDir.dir} 完成")
}

@Throws(IOException::class, InterruptedException::class)
private fun syncGit(gitDir: GitDir) {
    val defBranch = gitDir.defBranch
    // 将未映射到本地的所有远程分支映射到本地
    for (remote in defBranch) {
        val branchName = remote.substring(remote.indexOf('/') + 1)
        gitDir.dir.runCommand(
            "git", "branch", "--track", branchName, remote
        ).waitFor()
    }
    // 更新所有本地分支
    gitDir.reCalculateLocalBranch()
    gitDir.localBranch.forEach { branch ->
        syncGitSomeoneBranch(gitDir, branch)
    }

    // 更新当前分支
    syncGitCurrentBranch(gitDir)
}

@Throws(IOException::class, InterruptedException::class)
private fun syncGitSomeoneBranch(gitDir: GitDir, branchName: String) {
    println("同步Git目录：" + gitDir.dir + " 分支：" + branchName)
    gitDir.dir.runCommand(
        "git",
        "--no-optional-locks",
        "-c",
        "diff.mnemonicprefix=false",
        "-c",
        "core.quotepath=false",
        "fetch",
        "origin",
        "$branchName:$branchName"
    ).waitFor()
}

@Throws(IOException::class, InterruptedException::class)
private fun syncGitCurrentBranch(gitDir: GitDir) {
    println("同步Git目录：" + gitDir.dir + " 当前分支")
    gitDir.dir.runCommand("git", "checkout", "--", ".").waitFor()
    gitDir.dir.runCommand("git", "clean", "-xdf").waitFor()
    gitDir.dir.runCommand("git", "reset", "--hard").waitFor()
    gitDir.dir.runCommand("git", "pull", "--all").waitFor()
}

fun File.runCommand(vararg command: String): Process {
    return ProcessBuilder()
        .command(*command)
        .directory(this)
        .redirectErrorStream(true)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .start()
}

fun Process.lines(): ProcessLinesSequence = ProcessLinesSequence(this)

inline fun <T> Process.use(
    block: Process.() -> T
): T = try {
    block()
} finally {
    destroy()
}

class ProcessLinesSequence(
    private val process: Process
) : Sequence<String> by process.inputLineSequence(), Closeable {

    override fun close() {
        process.destroy()
    }

    companion object {
        private fun Process.inputLineSequence() =
            inputStream.bufferedReader().lineSequence()
    }
}

private class GitDir(
    val dir: File
) {
    var localBranch: Set<String> = calculateLocalBranch()
    var defBranch: Set<String> = checkDefBranch()

    fun reCalculateLocalBranch() {
        localBranch = calculateLocalBranch()
    }

    // 读取本地分支
    @Throws(IOException::class)
    private fun calculateLocalBranch() =
        dir.runCommand("git", "branch").lines().use { reader ->
            // 替换当前分支
            reader.map {
                it.trim().replace("* ", "")
            }.filter {
                it.isNotEmpty()
            }.toSet()
        }

    @Throws(IOException::class, InterruptedException::class)
    private fun checkDefBranch(): Set<String> {
        // 抓取远端分支
        dir.runCommand(
            "git",
            "--no-optional-locks",
            "-c",
            "color.branch=false",
            "-c",
            "color.diff=false",
            "-c",
            "color.status=false",
            "-c",
            "diff.mnemonicprefix=false",
            "-c",
            "core.quotepath=false",
            "-c",
            "credential.helper=sourcetree",
            "fetch",
            "--prune",
            "origin"
        ).waitFor()
        // 读取所有本地不存在的远程分支到本地作为映射
        // 读取远程分支
        return dir.runCommand(
            "git", "branch", "-r"
        ).lines().use { reader ->
            // 过滤在已经映射到本地的分支
            reader.map { obj: String ->
                obj.trim()
            }.filter { s: String ->
                !s.contains("->") && s.isNotEmpty()
            }.toSet()
        }.filter { s: String ->
            // 求差集
            s.substringAfter('/') !in localBranch
        }.toSet()
    }
}
