package io.github.zlooo.spec.generator.xml

import io.github.zlooo.fixyou.model.FieldType
import io.github.zlooo.fixyou.model.FixSpec
import io.github.zlooo.spec.generator.xml.model.FixType
import io.github.zlooo.spec.generator.xml.model.XMLModelTest
import org.assertj.core.api.Assertions
import spock.lang.Specification

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement

class DictionaryFileProcessorTest extends Specification {

    private DictionaryFileProcessor dictionaryFileProcessor = new DictionaryFileProcessor()

    def "should process default fix 50sp2 dictionary"() {
        setup:
        JAXBElement<FixType> fix50sp2 = JAXBContext.newInstance("io.github.zlooo.spec.generator.xml.model").createUnmarshaller().unmarshal(XMLModelTest.STANDARD_DICTIONARIES.find { file -> file.getName() == "FIX50_sp2.xml" })

        when:
        def result = dictionaryFileProcessor.process(fix50sp2.getValue())

        then:
        //InstrumentExtension component - component that apart from fields also has nested component and nested component is repeating group, yeah fucked up case but that's life
        //NoRelatedSym group contains this component so it should contain it's fields
        result.getRepeatingGroups().containsKey(146)
        Assertions.assertThat(result.getRepeatingGroups().get(146)).contains(new FixSpec.FieldNumberType(668, FieldType.LONG), new FixSpec.FieldNumberType(869, FieldType.DOUBLE), new FixSpec.FieldNumberType(870, FieldType.GROUP))
        //Parties component - component that has repeating group in it, also repeating group inside repeating group
        result.getRepeatingGroups().containsKey(453)
        Assertions.
                assertThat(result.getRepeatingGroups().get(453)).
                containsExactlyInAnyOrder(new FixSpec.FieldNumberType(448, FieldType.CHAR_ARRAY), new FixSpec.FieldNumberType(447, FieldType.CHAR), new FixSpec.FieldNumberType(452, FieldType.LONG),
                                          new FixSpec.FieldNumberType(802, FieldType.GROUP))
    }

    def "should process default fixt 1.1 dictionary"() {
        setup:
        JAXBElement<FixType> fix50sp2 = JAXBContext.newInstance("io.github.zlooo.spec.generator.xml.model").createUnmarshaller().unmarshal(XMLModelTest.STANDARD_DICTIONARIES.find { file -> file.getName() == "FIXT11.xml" })

        when:
        def result = dictionaryFileProcessor.process(fix50sp2.getValue())

        then:
        Assertions.
                assertThat(result.getHeaderFieldsNumberToTypes()).
                contains(Map.entry(8, FieldType.CHAR_ARRAY), Map.entry(9, FieldType.LONG), Map.entry(35, FieldType.CHAR_ARRAY), Map.entry(49, FieldType.CHAR_ARRAY), Map.entry(56, FieldType.CHAR_ARRAY), Map.entry(34, FieldType.LONG),
                         Map.entry(43, FieldType.BOOLEAN), Map.entry(97, FieldType.BOOLEAN), Map.entry(52, FieldType.TIMESTAMP), Map.entry(627, FieldType.GROUP))
        Assertions.assertThat(result.getBodyFieldsNumbersToTypes()).doesNotContainKeys(8, 9, 35, 49, 56, 34, 43, 97, 52, 627)
    }

    def "should process all standard fix dictionaries without exception"(){
        setup:
        JAXBElement<FixType> dictionary = JAXBContext.newInstance("io.github.zlooo.spec.generator.xml.model").createUnmarshaller().unmarshal(XMLModelTest.STANDARD_DICTIONARIES.find { file -> file.getName() == dictionaryFile })

        when:
        def result = dictionaryFileProcessor.process(dictionary.getValue())

        then:
        noExceptionThrown()
        result!=null

        where:
        dictionaryFile<<["FIX40.xml", "FIX41.xml", "FIX42.xml", "FIX43.xml", "FIX44.xml", "FIX50.xml", "FIX50_sp1.xml", "FIX50_sp2.xml", "FIXT11.xml"]
    }
}
