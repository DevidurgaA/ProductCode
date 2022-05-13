package com.tlc.crm.contact.api.models;

import com.tlc.validator.TlcModel;
import com.tlc.validator.type.Group.Create;
import com.tlc.validator.type.Group.Update;
import com.tlc.validator.type.Group.Delete;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * <p>
 *     A model that represents an email data
 * </p>
 */
public class ContactEmail implements TlcModel {

    @NotNull(groups = {Delete.class}, message = "i18n.contact.email.invalid.id")
    private Long id;

    @NotEmpty(groups = {Create.class, Update.class}, message = "i18n.contact.email.invalid.type")
    private String type;

    @NotEmpty(groups = {Create.class, Update.class}, message = "i18n.contact.email.invalid.value")
    @Email(groups = {Create.class, Update.class}, message = "i18n.contact.email.invalid.format")
    private String value;

    @NotNull(groups = {Create.class, Update.class}, message = "i18n.common.org.invalid.id")
    private Long orgId;

    private Long contactId;


    public Long orgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Long id() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Object identity() {
        return null;
    }
}
