package com.tlc.crm.contact.api.models;

import com.tlc.validator.TlcModel;
import com.tlc.validator.type.Group.Create;
import com.tlc.validator.type.Group.Update;
import com.tlc.validator.type.Group.Delete;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * <p>
 *     A model that represents the life cycle stage
 * </p>
 */
public class LifeCycleStage implements TlcModel {

    @NotNull(groups = {Update.class, Delete.class}, message = "i18n.contact.stage.invalid.id")
    private Long id;

    @NotEmpty(groups = {Create.class, Update.class}, message = "i18n.contact.stage.invalid.name")
    private String name;

    @NotNull(groups = {Create.class, Update.class}, message = "i18n.common.org.invalid.id")
    private Long orgId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long orgId() {
        return this.orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }
}
