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
        JAXBElement<FixType> fix50sp2 = JAXBContext.newInstance("io.github.zlooo.spec.generator.xml.model").createUnmarshaller().unmarshal(XMLModelTest.STANDARD_DICTIONARIES.find { file -> file.getName() == "FIX50SP2.xml" })

        when:
        def result = dictionaryFileProcessor.process(fix50sp2.getValue())

        then:
        //InstrumentExtension component - component that apart from fields also has nested component and nested component is repeating group, yeah fucked up case but that's life
        //NoRelatedSym group contains this component so it should contain it's fields
        result.getRepeatingGroups().containsKey(146)
        Assertions.
                assertThat(result.getRepeatingGroups().get(146)).contains(new FixSpec.FieldNumberType(668, FieldType.LONG), new FixSpec.FieldNumberType(869, FieldType.DOUBLE), new FixSpec.FieldNumberType(870, FieldType.GROUP))
        //Parties component - component that has repeating group in it, also repeating group inside repeating group
        result.getRepeatingGroups().containsKey(453)
        Assertions.
                assertThat(result.getRepeatingGroups().get(453)).
                containsExactlyInAnyOrder(new FixSpec.FieldNumberType(448, FieldType.CHAR_ARRAY), new FixSpec.FieldNumberType(447, FieldType.CHAR), new FixSpec.FieldNumberType(452, FieldType.LONG),
                                          new FixSpec.FieldNumberType(802, FieldType.GROUP))
    }
}
