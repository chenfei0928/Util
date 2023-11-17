package io.github0928.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author chenf()
 * @date 2023-11-02 15:52
 */
class GitUpdate {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 4
    );
    private static final File targetDir = new File("D:\\open_source");

    public static void main(String[] args) throws IOException {
        System.out.println("工作目录：" + targetDir);
        Set<String> ignoreDirs;
        try (var reader = new BufferedReader(new FileReader(new File(targetDir, "ignore.txt")))) {
            ignoreDirs = reader.lines().collect(Collectors.toSet());
        }
        System.out.println("忽略的Git工程：" + ignoreDirs);
        var files = new ArrayList<>(Arrays.stream(
                targetDir.listFiles(f -> f.isDirectory() && !ignoreDirs.contains(f.getName()))
        ).toList());
        AtomicInteger countdown = new AtomicInteger(0);
        Collections.shuffle(files);
        files.parallelStream()
                .map(file -> EXECUTOR.submit(() -> {
                    try {
                        return new GitDir(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .map(it -> {
                    try {
                        return it.get();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .sorted((gitDir, t1) -> -Integer.compare(gitDir.localBranch.size(), t1.localBranch.size()))
                .toList()
                .parallelStream()
                .map(gitDir -> EXECUTOR.submit(() -> {
                    try {
                        checkToSync(gitDir);
                    } catch (Throwable e) {
                        System.err.println("项目：" + gitDir.dir + " 同步失败：\n" + e);
                    }
                    System.out.println("当前进度项目：" + countdown.incrementAndGet() + "/" + files.size());
                    return gitDir;
                }))
                .forEach(integerFuture -> {
                    try {
                        integerFuture.get();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static void checkToSync(GitDir gitDir) throws IOException, InterruptedException {
        System.out.println("同步Git目录：" + gitDir.dir);
        syncGit(gitDir);
        System.out.println("同步Git目录：" + gitDir.dir + " 完成");
    }

    private static void syncGit(GitDir gitDir) throws IOException, InterruptedException {
        var gitPrefix = gitDir.gitPrefix;
        Set<String> defBranch = gitDir.defBranch;
        // 将未映射到本地的所有远程分支映射到本地
        for (String remote : defBranch) {
            String branchName = remote.substring(remote.indexOf('/') + 1);
            Runtime.getRuntime()
                    .exec(gitPrefix + "branch --track " + branchName + " " + remote + " ")
                    .waitFor();
        }
        // 更新所有本地分支
        gitDir.calculateLocalBranch();
        gitDir.localBranch.forEach(branch -> {
            try {
                syncGitSomeoneBranch(gitDir, branch);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 更新当前分支
        syncGitCurrentBranch(gitDir);
    }

    private static void syncGitSomeoneBranch(GitDir gitDir, String branchName) throws IOException, InterruptedException {
        System.out.println("同步Git目录：" + gitDir.dir + " 分支：" + branchName);
        var gitPrefix = gitDir.gitPrefix;
        Runtime.getRuntime()
                .exec(gitPrefix + "-c diff.mnemonicprefix=false -c core.quotepath=false --no-optional-locks fetch origin " + branchName + ":" + branchName)
                .waitFor();
    }

    private static void syncGitCurrentBranch(GitDir gitDir) throws IOException, InterruptedException {
        System.out.println("同步Git目录：" + gitDir.dir + " 当前分支");
        var gitPrefix = gitDir.gitPrefix;
        for (String s : new String[]{
                "checkout -- .",
                "clean -xdf",
                "reset --hard",
                "pull --all",
        }) {
            Runtime.getRuntime()
                    .exec(gitPrefix + s)
                    .waitFor();
        }
    }

    private static class GitDir {
        final File dir;
        final String gitPrefix;
        Set<String> localBranch;
        final Set<String> defBranch;

        private GitDir(File dir) throws IOException, InterruptedException {
            this.dir = dir;
            gitPrefix = "git --work-tree={git_dir} --git-dir={git_dir}/.git ".replace(
                    "{git_dir}", dir.getAbsolutePath()
            );
            calculateLocalBranch();
            defBranch = checkDefBranch();
        }

        void calculateLocalBranch() throws IOException {
            // 读取本地分支
            try (var reader = Runtime.getRuntime()
                    .exec("git branch".replace("git ", gitPrefix))
                    .inputReader()) {
                localBranch = reader.lines()
                        // 替换当前分支
                        .map(s -> s.strip().replace("* ", ""))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());
            }
        }

        private Set<String> checkDefBranch() throws IOException, InterruptedException {
            // 抓取远端分支
            Runtime.getRuntime()
                    .exec(gitPrefix + "--no-optional-locks -c color.branch=false -c color.diff=false -c color.status=false -c diff.mnemonicprefix=false -c core.quotepath=false -c credential.helper=sourcetree fetch --prune origin")
                    .waitFor();
            // 读取所有本地不存在的远程分支到本地作为映射
            // 读取远程分支
            Set<String> remoteBranches;
            try (var reader = Runtime.getRuntime()
                    .exec("git branch -r".replace("git ", gitPrefix))
                    .inputReader()) {
                remoteBranches = reader.lines()
                        .map(String::strip)
                        // 过滤在已经映射到本地的分支
                        .filter(s -> !s.contains("->") && !s.isEmpty())
                        .collect(Collectors.toSet());
            }
            // 求差集
            return remoteBranches.stream()
                    .filter(s -> !localBranch.contains(s.substring(s.indexOf('/') + 1)))
                    .collect(Collectors.toSet());
        }
    }
}
