package net.core.corenotifications;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;

final class Environment {

    private static Path path;

    static {
        var path = System.getProperty("user.home") + File.separator + ".net.core" + File.separator + "corenotifications";

        if (OS.isWindows())
            path = System.getenv("APPDATA") + File.separator + ".net.core"  + File.separator + "corenotifications";
        Environment.path = Paths.get(path);
    }

    public static Path getAppData() {
        try {
            var dir = Files.createDirectories(path);
            if (OS.isWindows())
                hide(dir.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }

    private static void hide(@NotNull Path path) throws IOException {
        var dosFileAttributeView = Files.getFileAttributeView(path, DosFileAttributeView.class);
        var dosFileAttributes = dosFileAttributeView.readAttributes();

        if (!dosFileAttributes.isHidden())
            dosFileAttributeView.setHidden(true);
    }
}
