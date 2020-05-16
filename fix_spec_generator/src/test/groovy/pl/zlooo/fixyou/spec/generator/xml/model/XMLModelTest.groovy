package pl.zlooo.fixyou.spec.generator.xml.model

import spock.lang.Specification

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

class XMLModelTest extends Specification {

    public static final List<File> STANDARD_DICTIONARIES = getDictionaries()
    private static final Unmarshaller unmarshaller = JAXBContext.newInstance("pl.zlooo.fixyou.spec.generator.xml.model").createUnmarshaller()

    static List<File> getDictionaries() {
        def testResources = ClassLoader.getSystemResources("").toList().stream().filter({ url -> url.toExternalForm().contains("test") }).filter({ url -> url.toExternalForm().contains("resources") }).map({ url -> url.toURI() }).collect(
                Collectors.toList())
        def result = []
        for (URI uri : testResources) {
            Files.walk(Paths.get(uri), 1).map({ path -> path.toFile() }).filter({ file -> file.getName().endsWith(".xml") }).filter({ file -> file.getName().contains("FIX") }).forEach({ file -> result.add(file) })
        }
        return result
    }

    def "should unmarshal standard dictionaries"() {
        when:
        def result = unmarshaller.unmarshal(dictionary)

        then:
        result != null

        where:
        dictionary << STANDARD_DICTIONARIES
    }
}
