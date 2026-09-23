package com.example.nfe.infrastructure.xml;

import com.example.nfe.domain.NfeEmission;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.stereotype.Component;

import java.io.StringWriter;

/**
 * Generates the NF-e XML document for a {@link NfeEmission} aggregate using
 * the JAXB model and the {@link NfeXmlMapper}. No signing, no validation
 * against XSD and no SEFAZ concerns yet.
 */
@Component
public class NfeXmlGenerator {

    private final NfeXmlMapper mapper;

    public NfeXmlGenerator(NfeXmlMapper mapper) {
        this.mapper = mapper;
    }

    public String generate(NfeEmission emission) {
        Nfe nfe = mapper.toXml(emission);
        return marshal(nfe);
    }

    private String marshal(Nfe nfe) {
        try {
            JAXBContext context = JAXBContext.newInstance(Nfe.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            StringWriter writer = new StringWriter();
            marshaller.marshal(nfe, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new IllegalStateException("Failed to generate NF-e XML", e);
        }
    }
}
