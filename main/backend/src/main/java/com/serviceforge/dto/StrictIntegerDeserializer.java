package com.serviceforge.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;

/**
 * ETSTCC-13074: accepts JSON integer tokens only (rejects decimals, strings, booleans, overflow).
 * Contextual so one class yields a Long or an Integer depending on the annotated property type.
 * Error messages are fixed text and never echo the offending token.
 */
public class StrictIntegerDeserializer extends JsonDeserializer<Number> implements ContextualDeserializer {

    private final boolean asInteger;

    public StrictIntegerDeserializer() {
        this(false);
    }

    private StrictIntegerDeserializer(boolean asInteger) {
        this.asInteger = asInteger;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null) {
            Class<?> raw = property.getType().getRawClass();
            return new StrictIntegerDeserializer(raw == Integer.class || raw == int.class);
        }
        return this;
    }

    @Override
    public Number deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() != JsonToken.VALUE_NUMBER_INT) {
            throw JsonMappingException.from(p, "Expected an integer value");
        }
        switch (p.getNumberType()) {
            case INT:
                return asInteger ? (Number) p.getIntValue() : (Number) (long) p.getIntValue();
            case LONG:
                if (asInteger) {
                    throw JsonMappingException.from(p, "Integer value out of range");
                }
                return p.getLongValue();
            default:
                throw JsonMappingException.from(p, "Integer value out of range");
        }
    }
}
