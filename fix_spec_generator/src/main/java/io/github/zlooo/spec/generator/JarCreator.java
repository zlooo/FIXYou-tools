package io.github.zlooo.spec.generator;

import com.squareup.javapoet.JavaFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

@Slf4j
@RequiredArgsConstructor
public class JarCreator {

    private static final int BUFFER_SIZE = 1000;
    private static final String CLASS_FILE_NAME = "FixSpec.class";
    private static final String SLASH = "/";
    private final CodeCompiler codeCompiler;

    public void compileAndCreateJar(JavaFile fixSpecJavaFile, File outputDir, String jarName, String packageName, String[] version) {
        codeCompiler.compile(fixSpecJavaFile);
        final byte[] buffer = new byte[BUFFER_SIZE];
        final File compiledFixSpec = new File(CLASS_FILE_NAME);
        try (final JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputDir.toPath().resolve(jarName).toFile()), createManifest(version));
             final FileInputStream compiledClassInputStream = new FileInputStream(compiledFixSpec)) {
            final JarEntry entry = new JarEntry(packageName.replaceAll("\\.", SLASH) + SLASH + CLASS_FILE_NAME);
            jarOutputStream.putNextEntry(entry);
            int bytesRead = 0;
            while ((bytesRead = compiledClassInputStream.read(buffer)) > 0) {
                jarOutputStream.write(buffer, 0, bytesRead);
            }
            jarOutputStream.closeEntry();
        } catch (IOException e) {
            log.error("Exception while writing a jar file", e);
        } finally {
            if (!compiledFixSpec.delete()) {
                log.warn("Could not delete compiled class {}, please do it manually", compiledFixSpec);
            }
        }
    }

    private Manifest createManifest(String[] version) {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
        manifest.getMainAttributes().put(new Attributes.Name("Generated-By"), "FIXYou spec generator");
        manifest.getMainAttributes().put(new Attributes.Name("FIXYou-Generator-Version"), String.join(" ", version));
        return manifest;
    }
}
