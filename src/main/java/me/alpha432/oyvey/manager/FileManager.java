package me.alpha432.oyvey.manager;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.ImageUtil;
import me.alpha432.oyvey.util.shader.EfficientTexture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileManager extends Feature {
    private static final Logger logger = LogManager.getLogger(FileManager.class);
    private static FileManager INSTANCE;

    private final Path base = this.getMkDirectory(this.getRoot(), "Xuluplus");
    private final Path config = this.getMkDirectory(this.base, "config");
    private final Path images = this.getMkDirectory(this.base, "images");

    private final List<EfficientTexture> imageList = new ArrayList<>();

    private FileManager() {
        this.getMkDirectory(this.base, "pvp");
        for (Module.Category category : OyVey.moduleManager.getCategories()) {
            this.getMkDirectory(this.config, category.getName());
        }

        this.initImages(this.images);
    }

    public static FileManager getInstance() {
        if (INSTANCE == null) INSTANCE = new FileManager();
        return INSTANCE;
    }

    private void initImages(Path path) {
        File f = path.toFile();

        if (f.isDirectory()) {
            for (File file : Objects.requireNonNull(f.listFiles(
                    (dir, name) -> name.endsWith("png") || name.endsWith("jpg") ))) {
                try {
                    //EfficientTexture texture = new EfficientTexture(ImageUtil.cacheBufferedImage(ImageUtil.createFlipped(ImageUtil.bufferedImageFromFile(file)), file.getName()) );
                    EfficientTexture texture = new EfficientTexture(ImageUtil.createFlipped(
                            ImageUtil.bufferedImageFromFile(file)));

                    this.imageList.add(texture);

                } catch (IOException e) {
                    FileManager.logger.error("Failed to load image " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean appendTextFile(String data, String file) {
        try {
            Path path = Paths.get(file);
            Files.write(path, Collections.singletonList(data), StandardCharsets.UTF_8, Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.out.println("WARNING: Unable to write file: " + file);
            return false;
        }
        return true;
    }

    public static List<String> readTextFileAllLines(String file) {
        try {
            Path path = Paths.get(file);
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("WARNING: Unable to read file, creating new file: " + file);
            FileManager.appendTextFile("", file);
            return Collections.emptyList();
        }
    }

    private String[] expandPath(String fullPath) {
        return fullPath.split(":?\\\\\\\\|\\/");
    }

    private Stream<String> expandPaths(String... paths) {
        return Arrays.stream(paths).map(this::expandPath).flatMap(Arrays::stream);
    }

    private Path lookupPath(Path root, String... paths) {
        return Paths.get(root.toString(), paths);
    }

    private Path getRoot() {
        return Paths.get("");
    }

    private void createDirectory(Path dir) {
        try {
            if (!Files.isDirectory(dir)) {
                if (Files.exists(dir)) {
                    Files.delete(dir);
                }
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path getMkDirectory(Path parent, String... paths) {
        if (paths.length < 1) {
            return parent;
        }
        Path dir = this.lookupPath(parent, paths);
        this.createDirectory(dir);
        return dir;
    }

    public Path getBasePath() {
        return this.base;
    }

    public Path getBaseResolve(String... paths) {
        String[] names = this.expandPaths(paths).toArray(String[]::new);
        if (names.length < 1) {
            throw new IllegalArgumentException("missing path");
        }
        return this.lookupPath(this.getBasePath(), names);
    }

    public Path getMkBaseResolve(String... paths) {
        Path path = this.getBaseResolve(paths);
        this.createDirectory(path.getParent());
        return path;
    }

    public Path getConfig() {
        return this.getBasePath().resolve("config");
    }

    public Path getCache() {
        return this.getBasePath().resolve("cache");
    }

    public Path getMkBaseDirectory(String... names) {
        return this.getMkDirectory(this.getBasePath(), this.expandPaths(names).collect(Collectors.joining(File.separator)));
    }

    public Path getMkConfigDirectory(String... names) {
        return this.getMkDirectory(this.getConfig(), this.expandPaths(names).collect(Collectors.joining(File.separator)));
    }

    public List<EfficientTexture> getImageList() {
        return imageList;
    }
}

