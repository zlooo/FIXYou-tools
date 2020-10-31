package io.github.zlooo.spec.generator.xml


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
                assertThat(result.getRepeatingGroups().get(146)).contains(668, 869, 870)
        //Parties component - component that has repeating group in it, also repeating group inside repeating group
        result.getRepeatingGroups().containsKey(453)
        Assertions.
                assertThat(result.getRepeatingGroups().get(453)).containsExactlyInAnyOrder(448, 447, 452, 802)
    }
}
