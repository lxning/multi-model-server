package com.amazonaws.ml.mms.openapi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Schema {

    private String type;
    private String format;
    private String name;
    private List<String> required;
    private Map<String, Property> properties;
    private boolean isSimple;
    private String description;
    private Object example;
    private Property additionalProperties;
    private String discriminator;
    private String defaultValue;

    public Schema() {}

    public Schema(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }

    public void addProperty(Property property) {
        if (properties == null) {
            properties = new LinkedHashMap<>();
        }
        properties.put(property.getName(), property);
        if (property.isRequired()) {
            if (required == null) {
                required = new ArrayList<>();
            }
            required.add(property.getName());
        }
    }

    public boolean isSimple() {
        return isSimple;
    }

    public void setSimple(boolean simple) {
        isSimple = simple;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getExample() {
        return example;
    }

    public void setExample(Object example) {
        this.example = example;
    }

    public Property getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Property additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}