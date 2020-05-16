package pl.zlooo.fixyou.spec.generator;

import com.squareup.javapoet.JavaFile;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.Collections;

class CodeCompiler {

    /**
     * Output class file should pop up in working directory, after packaging to jar {@link JarCreator#compileAndCreateJar(JavaFile, File, String, String, String[])} should delete it
     */
    void compile(JavaFile fixSpecJavaFile) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, null, null, Collections.singleton(fixSpecJavaFile.toJavaFileObject()));
        if (!task.call()) {
            throw new FixSpecGeneratorException(
                    "Compilation errors? What's unusual to say the least, after all this code is generated. This means either bug in JavaPoet(https://github.com/square/javapoet), which is used to generate source file or in this program. " +
                    "Please report it along with compiler output which should be on stderr");
        }
    }
}
