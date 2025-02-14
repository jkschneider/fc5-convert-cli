package dev.ebullient.fc5.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.slugify.Slugify;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.Templates;
import io.quarkus.qute.TemplateData;

public class MarkdownWriter {

    private static Slugify slugify;

    public static Slugify slugifier() {
        Slugify s = slugify;
        if (s == null) {
            s = new Slugify();
            s.withLowerCase(true);
            slugify = s;
        }
        return s;
    }

    final Templates templates;
    final Path output;

    public MarkdownWriter(Path output, Templates templates) {
        this.output = output;
        this.templates = templates;
    }

    public <T extends BaseType> void writeFiles(List<T> elements, String typeName) throws IOException {
        if (elements.isEmpty()) {
            return;
        }
        List<FileMap> fileMappings = new ArrayList<>();
        String dirName = typeName.toLowerCase();

        Log.outPrintln("⏱ Writing " + typeName);
        elements.forEach(x -> {
            FileMap fileMap = new FileMap(x.getName(), slugifier().slugify(x.getName()));
            try {
                switch (dirName) {
                    case "backgrounds":
                        writeFile(fileMap, dirName, templates.renderBackground((BackgroundType) x));
                        break;
                    case "classes":
                        writeFile(fileMap, dirName, templates.renderClass((ClassType) x));
                        break;
                    case "feats":
                        writeFile(fileMap, dirName, templates.renderFeat((FeatType) x));
                        break;
                    case "items":
                        writeFile(fileMap, dirName, templates.renderItem((ItemType) x));
                        break;
                    case "monsters":
                        writeFile(fileMap, dirName, templates.renderMonster((MonsterType) x));
                        break;
                    case "races":
                        writeFile(fileMap, dirName, templates.renderRace((RaceType) x));
                        break;
                    case "spells":
                        writeFile(fileMap, dirName, templates.renderSpell((SpellType) x));
                        break;
                }
            } catch (IOException e) {
                throw new WrappedIOException(e);
            }
            fileMappings.add(fileMap);
        });
        writeFile(new FileMap(typeName, dirName), dirName, templates.renderIndex(typeName, fileMappings));
        Log.outPrintln("  ✅ " + (fileMappings.size() + 1) + " files.");
    }

    void writeFile(FileMap fileMap, String type, String content) throws IOException {
        Path targetDir = Paths.get(output.toString(), type);
        targetDir.toFile().mkdirs();

        Path target = targetDir.resolve(fileMap.fileName);

        Files.write(target, content.getBytes(StandardCharsets.UTF_8));
        Log.debugf("      %s", target);
    }

    @TemplateData
    public static class FileMap {
        final String title;
        final String fileName;

        FileMap(String title, String fileName) {
            this.title = title;
            this.fileName = fileName + ".md";
        }

        public String getTitle() {
            return title;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public static class WrappedIOException extends RuntimeException {
        WrappedIOException(IOException cause) {
            super(cause);
        }
    }
}
