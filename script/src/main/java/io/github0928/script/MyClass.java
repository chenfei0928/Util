package io.github0928.script;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MyClass {
    public static void main(String[] args) {
        var appData = System.getenv("APPDATA");
        var files = new File(appData + "\\kingsoft\\wps\\addons\\pool\\win-i386").listFiles();
        Map<String, Map<Runtime.Version, File>> namesFile = new HashMap<>();
        for (File file : files) {
            var fileName = file.getName();
            var split = fileName.split("_");
            String name;
            Runtime.Version version;
            if (split.length == 2) {
                name = split[0];
                version = Runtime.Version.parse(split[1]);
            } else {
                var versionStr = split[split.length - 1];
                name = fileName.substring(0, fileName.length() - versionStr.length());
                version = Runtime.Version.parse(split[split.length - 1]);
            }
            if (namesFile.containsKey(name)) {
                namesFile.get(name).put(version, file);
            } else {
                Map<Runtime.Version, File> versions = new HashMap<>();
                versions.put(version, file);
                namesFile.put(name, versions);
            }
        }

        namesFile.forEach((name, versionFileMap) -> {
            if (versionFileMap.size() == 1)
                return;
            var maxVersion = versionFileMap.keySet().stream().max(Runtime.Version::compareTo);
            versionFileMap.remove(maxVersion.get());
            for (File file : versionFileMap.values()) {
                System.out.println("delete: " + file);
                deleteDir(file);
            }
        });
    }

    private static void deleteDir(File dir) {
        if (dir.isFile()) {
            dir.delete();
        } else {
            for (File file : dir.listFiles()) {
                deleteDir(file);
            }
            dir.delete();
        }
    }
}
