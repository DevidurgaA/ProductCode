package com.tlc.crm.common.action.secure.list;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;

import java.util.*;

/**
 * @author Abishek
 * @version 1.0
 */
public class ListStructure
{
    private final Long providerId;
    private final Map<String, Field> fieldsMap;
    private final List<Field> fieldList;
    private final Field primaryField;
    private final Map<String, Transformer> transformerMap;

    public ListStructure(Long providerId, List<Field> fields)
    {
        this.providerId = Objects.requireNonNull(providerId);
        this.fieldsMap = new HashMap<>();
        this.transformerMap = new HashMap<>();

        this.fieldList = List.copyOf(fields);
        for (Field field : fieldList)
        {
            final String fieldName = field.name();
            fieldsMap.put(fieldName, field);
            final Transformer transformer = field.transformer();
            if(transformer != null)
            {
                transformerMap.put(fieldName, transformer);
            }
        }
        final Optional<Field> primaryField = fields.stream().filter(Field::primary).findFirst();
        if(primaryField.isPresent())
        {
            this.primaryField = primaryField.get();
        }
        else
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Primary Field not found!!!!");
        }
    }

    public Field getPrimaryField()
    {
        return primaryField;
    }

    public Long getProviderId()
    {
        return providerId;
    }

    public Field getField(String fieldName)
    {
        return fieldsMap.get(fieldName);
    }

    public Transformer getTransformer(String fieldName)
    {
        return transformerMap.get(fieldName);
    }

    public List<Field> getFields()
    {
        return fieldList;
    }
}