package io.github.zlooo.spec.generator.xml


import org.assertj.core.api.Assertions
import pl.zlooo.fixyou.model.FieldType
import pl.zlooo.fixyou.model.FixSpec
import io.github.zlooo.spec.generator.xml.model.XMLModelTest
import spock.lang.Specification

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement

class DictionaryFileProcessorTest extends Specification {

    private DictionaryFileProcessor dictionaryFileProcessor = new DictionaryFileProcessor()

    def "should process default fix 50sp2 dictionary"() {
        setup:
        JAXBElement<io.github.zlooo.spec.generator.xml.model.FixType> fix50sp2 = JAXBContext.newInstance("pl.zlooo.fixyou.spec.generator.xml.model").createUnmarshaller().unmarshal(XMLModelTest.STANDARD_DICTIONARIES.find { file -> file.getName() == "FIX50SP2.xml" })

        when:
        def result = dictionaryFileProcessor.process(fix50sp2.getValue())

        then:
        //InstrumentExtension component - component that apart from fields also has nested component and nested component is repeating group, yeah fucked up case but that's life
        //NoRelatedSym group contains this component so it should contain it's fields
        result.getRepeatingGroups().containsKey(146)
        Assertions.
                assertThat(result.getRepeatingGroups().get(146)).contains(new FixSpec.FieldNumberTypePair(FieldType.LONG, 668), new FixSpec.FieldNumberTypePair(FieldType.DOUBLE, 869), new FixSpec.FieldNumberTypePair(FieldType.GROUP, 870))
        //Parties component - component that has repeating group in it, also repeating group inside repeating group
        result.getRepeatingGroups().containsKey(453)
        Assertions.
                assertThat(result.getRepeatingGroups().get(453)).
                containsExactlyInAnyOrder(new FixSpec.FieldNumberTypePair(FieldType.CHAR_ARRAY, 448), new FixSpec.FieldNumberTypePair(FieldType.CHAR, 447), new FixSpec.FieldNumberTypePair(FieldType.LONG, 452),
                                          new FixSpec.FieldNumberTypePair(FieldType.GROUP, 802))
    }
}
