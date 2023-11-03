package io.github0928.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author chenf()
 * @date 2023-11-02 15:52
 */
class GitUpdate {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2
    );
    private static final File targetDir = new File("D:\\open_source");

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("工作目录：" + targetDir);
        Set<String> ignoreDirs;
        try (var reader = new BufferedReader(new FileReader(new File(targetDir, "ignore.txt")))) {
            ignoreDirs = reader.lines().collect(Collectors.toSet());
        }
        System.out.println("忽略的Git工程：" + ignoreDirs);
        var dirs = targetDir.listFiles(f -> f.isDirectory() && !ignoreDirs.contains(f.getName()));
//        for (File dir : dirs) {
//            checkToSync(dir);
//        }
        Arrays.stream(dirs)
                .parallel()
                .map(file -> EXECUTOR.submit(() -> {
                    checkToSync(file);
                    return 1;
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

    private static void checkToSync(File gitDir) throws IOException, InterruptedException {
        System.out.println("同步Git目录：" + gitDir);
        syncGit(gitDir);
        System.out.println("清理部分错误提交的文件夹目录：" + gitDir);
    }

    private static void syncGit(File gitDir) throws IOException, InterruptedException {
        var gitPrefix = getGitPrefix(gitDir);
        var cmd_fetch = "git --no-optional-locks -c color.branch=false -c color.diff=false -c color.status=false -c diff.mnemonicprefix=false -c core.quotepath=false -c credential.helper=sourcetree fetch --prune origin".replace(
                "git ", gitPrefix
        );
        // 抓取远端分支
        Runtime.getRuntime().exec(cmd_fetch).waitFor();
        // 读取所有本地不存在的远程分支到本地作为映射
        // 读取远程分支
        Set<String> remoteBranches = Runtime.getRuntime().exec("git branch -r".replace("git ", gitPrefix))
                .inputReader()
                .lines()
                .map(String::strip)
                // 过滤在已经映射到本地的分支
                .filter(s -> !s.contains("->") && !s.isEmpty())
                .collect(Collectors.toSet());
        // 读取本地分支
        Set<String> localBranch = Runtime.getRuntime().exec("git branch".replace("git ", gitPrefix))
                .inputReader()
                .lines()
                // 替换当前分支
                .map(s -> s.strip().replace("* ", ""))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        // 求差集
        Set<String> defBranch = remoteBranches.stream()
                .filter(s -> !localBranch.contains(s.substring(s.indexOf('/') + 1)))
                .collect(Collectors.toSet());
        // 将未映射到本地的所有远程分支映射到本地
        for (String remote : defBranch) {
            String branchName = remote.substring(remote.indexOf('/') + 1);
            Runtime.getRuntime().exec(gitPrefix + "branch --track " + branchName + " " + remote + " ").waitFor();
        }
        // 更新所有本地分支
        Runtime.getRuntime().exec("git branch".replace("git ", gitPrefix))
                .inputReader()
                .lines()
                .map(s -> s.strip().replace("* ", ""))
                .filter(s -> !s.isEmpty())
                .forEach(branch -> {
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
        System.out.println("同步Git目录：" + gitDir + " 完成");
    }

    private static void syncGitSomeoneBranch(File gitDir, String branchName) throws IOException, InterruptedException {
        System.out.println("同步Git目录：" + gitDir + " 分支：" + branchName);
        var gitPrefix = getGitPrefix(gitDir);
        Runtime.getRuntime()
                .exec(gitPrefix + "-c diff.mnemonicprefix=false -c core.quotepath=false --no-optional-locks fetch origin " + branchName + ":" + branchName)
                .waitFor();
    }

    private static void syncGitCurrentBranch(File gitDir) throws IOException, InterruptedException {
        System.out.println("同步Git目录：" + gitDir + " 当前分支");
        var gitPrefix = getGitPrefix(gitDir);
        for (String s : new String[]{
                "checkout -- .",
                "clean -xdf",
                "reset --hard",
                "pull --all",
        }) {
            Runtime.getRuntime().exec(gitPrefix + s).waitFor();
        }
    }

    private static String getGitPrefix(File gitDir) {
        return "git --work-tree={git_dir} --git-dir={git_dir}/.git ".replace(
                "{git_dir}", gitDir.getAbsolutePath()
        );
    }
}
