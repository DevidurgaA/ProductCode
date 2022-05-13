package com.tlc.crm.contact.api.models;

import com.tlc.validator.TlcModel;
import com.tlc.validator.type.Group.Create;
import com.tlc.validator.type.Group.Update;
import com.tlc.validator.type.Group.Delete;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * <p>
 *     A model that represents the contact source
 * </p>
 */
public class ContactSource implements TlcModel {

    @NotNull(groups = {Update.class, Delete.class}, message = "i18n.contact.source.invalid.id")
    private Long id;

    @NotNull(groups = {Create.class, Update.class}, message = "i18n.common.org.invalid.id")
    private Long orgId;

    @NotEmpty(groups = {Create.class, Update.class}, message = "i18n.contact.source.invalid.name")
    private String value;

    public Long orgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Long id() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Object identity() {
        return null;
    }
}
