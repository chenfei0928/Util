package io.github0928.script;

import java.io.File;
import java.lang.module.ModuleDescriptor;
import java.util.HashMap;
import java.util.Map;

public class WPSClean {
    public static void main(String[] args) {
        var appData = System.getenv("APPDATA");
        var files = new File(appData + "\\kingsoft\\wps\\addons\\pool\\win-i386").listFiles();
        Map<String, Map<ModuleDescriptor.Version, File>> namesFile = new HashMap<>();
        for (File file : files) {
            var fileName = file.getName();
            var split = fileName.split("_");
            String name;
            ModuleDescriptor.Version version;
            if (split.length == 2) {
                name = split[0];
                version = ModuleDescriptor.Version.parse(split[1]);
            } else {
                var versionStr = split[split.length - 1];
                name = fileName.substring(0, fileName.length() - versionStr.length());
                version = ModuleDescriptor.Version.parse(split[split.length - 1]);
            }
            if (namesFile.containsKey(name)) {
                namesFile.get(name).put(version, file);
            } else {
                Map<ModuleDescriptor.Version, File> versions = new HashMap<>();
                versions.put(version, file);
                namesFile.put(name, versions);
            }
        }

        namesFile.forEach((name, versionFileMap) -> {
            if (versionFileMap.size() == 1)
                return;
            var maxVersion = versionFileMap.keySet().stream().max(ModuleDescriptor.Version::compareTo);
            versionFileMap.remove(maxVersion.get());
            for (File file : versionFileMap.values()) {
                System.out.println("delete: " + file);
                deleteDir(file);
            }
        });
    }

    static void deleteDir(File dir) {
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
