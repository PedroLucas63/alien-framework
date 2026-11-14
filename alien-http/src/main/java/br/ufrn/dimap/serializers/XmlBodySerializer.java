package br.ufrn.dimap.serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class XmlBodySerializer implements IHttpBodySerializer {
    private final XmlMapper xmlMapper;

    public XmlBodySerializer() {
        this.xmlMapper = new XmlMapper();
    }

    public XmlBodySerializer(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    @Override
    public String contentType(Object body) {
        return "application/xml; charset=utf-8";
    }

    @Override
    public boolean canSerialize(Object body) {
        return body != null && body.getClass().isAnnotationPresent(JacksonXmlRootElement.class);
    }

    @Override
    public InputStream serialize(Object body) {
        try {
            byte[] xmlBytes = xmlMapper.writeValueAsBytes(body);
            return new ByteArrayInputStream(xmlBytes);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("Failed to serialize body to XML", e);
        }
    }
}
