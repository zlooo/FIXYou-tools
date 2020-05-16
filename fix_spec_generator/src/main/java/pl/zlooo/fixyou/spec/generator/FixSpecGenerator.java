package pl.zlooo.fixyou.spec.generator;

import com.squareup.javapoet.JavaFile;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import pl.zlooo.fixyou.spec.generator.xml.DictionaryFileProcessor;
import pl.zlooo.fixyou.spec.generator.xml.model.FixType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
@Data
@CommandLine.Command(name = "fix_spec_generator", mixinStandardHelpOptions = true, versionProvider = FixSpecGenerator.VersionProvider.class)
@RequiredArgsConstructor
public class FixSpecGenerator implements Callable<Integer> {

    @CommandLine.Option(names = {"-f", "--file"}, description = "Quickfix xml dictionary file", required = true)
    private List<File> files;
    @CommandLine.Option(names = {"-p", "--package"}, description = "Package name to use for generated code", required = true)
    private String packageName;
    @CommandLine.Option(names = {"-o", "--output"}, description = "Output directory, defaults to ${DEFAULT-VALUE}", defaultValue = ".")
    private File outputDir;
    @CommandLine.Option(names = {"-j", "--jar"}, description = "Name of jar that will contain compiled FixSpec class. If empty just sources will be generated")
    private String jarName;
    private final DictionaryFileProcessor dictionaryFileProcessor;
    private final CodeGenerator codeGenerator;
    private final JarCreator jarCreator;

    public static void main(String[] args) {
        System.exit(new CommandLine(new FixSpecGenerator(new DictionaryFileProcessor(), new CodeGenerator(), new JarCreator(new CodeCompiler()))).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        final Unmarshaller unmarshaller = JAXBContext.newInstance("pl.zlooo.fixyou.spec.generator.xml.model").createUnmarshaller();
        final List<DictionaryFileProcessor.Result> singleFileResults = new ArrayList<>(files.size());
        for (final File file : files) {
            final JAXBElement<FixType> unmarshallingResult = (JAXBElement<FixType>) unmarshaller.unmarshal(file);
            final DictionaryFileProcessor.Result processingResult = dictionaryFileProcessor.process(unmarshallingResult.getValue());
            DictionaryFileProcessingResultUtils.validate(processingResult);
            singleFileResults.add(processingResult);
        }
        final DictionaryFileProcessor.Result reducedResult =
                singleFileResults.stream().reduce(DictionaryFileProcessingResultUtils.resultAccumulator()).orElseThrow(() -> new FixSpecGeneratorException("No results? Provided files are empty or what?"));
        final JavaFile fixSpecJavaFile = codeGenerator.generateFixSpecSourceCode(reducedResult, packageName);
        log.info("Writing generated FixSpec class to directory {}", outputDir);
        fixSpecJavaFile.writeTo(outputDir);
        if (jarName != null) {
            log.info("Compiling and packaging FixSpec to {}", jarName);
            jarCreator.compileAndCreateJar(fixSpecJavaFile, outputDir, jarName, packageName, new VersionProvider().getVersion());
        }
        return 0;
    }

    public static final class VersionProvider implements CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() throws Exception {
            return new String[]{"0.0.1"};
        }
    }
}
