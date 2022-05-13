package com.tlc.crm.contact.api.models;

import com.tlc.validator.TlcModel;
import com.tlc.validator.type.Group.Create;
import com.tlc.validator.type.Group.Update;
import com.tlc.validator.type.Group.Delete;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Abishek
 * @version 1.0
 */
public class Contact implements TlcModel {

    @NotNull(groups = {Update.class, Delete.class}, message = "i18n.contact.invalid.id")
    private Long id;

    @NotEmpty(groups = {Create.class, Update.class}, message = "i18n.contact.invalid.firstName")
    private String firstName;

    private String lastName;

    @NotEmpty(groups = {Create.class, Update.class}, message = "i18n.contact.email.invalid")
    @Valid
    private List<ContactEmail> emails;

    @NotEmpty(groups = {Create.class, Update.class}, message = "i18n.contact.mobile.invalid")
    @Valid
    private List<ContactMobile> mobiles;

    @NotNull(groups = {Create.class, Update.class}, message = "i18n.contact.source.invalid")
    @Valid
    private ContactSource contactSource;

    @NotNull(groups = {Create.class, Update.class}, message = "i18n.contact.stage.invalid")
    @Valid
    private LifeCycleStage lifeCycleStage;

    @NotNull(groups = {Create.class, Update.class}, message = "i18n.contact.company.invalid")
    @Valid
    private Company company;

    @NotNull(groups = {Create.class, Update.class}, message = "i18n.common.org.invalid.id")
    private Long orgId;

    private Long companyId;

    private Long ownerId;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long orgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    @Override
    public Long id() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<ContactEmail> getEmails() {
        return emails;
    }

    public void setEmails(List<ContactEmail> emails) {
        this.emails = emails;
    }

    public void addEmail(ContactEmail contactEmail) {
        this.emails = this.emails == null ? new ArrayList<>() : this.emails;

        this.emails.add(contactEmail);
    }

    public List<ContactMobile> getMobiles() {
        return mobiles;
    }

    public void addMobile(ContactMobile contactMobile) {
        this.mobiles = this.mobiles == null ? new ArrayList<>() : this.mobiles;

        this.mobiles.add(contactMobile);
    }


    public void setMobiles(List<ContactMobile> mobiles) {
        this.mobiles = mobiles;
    }

    public ContactSource getContactSource() {
        return contactSource;
    }

    public void setContactSource(ContactSource contactSource) {
        this.contactSource = contactSource;
    }

    public LifeCycleStage getLifeCycleStage() {
        return lifeCycleStage;
    }

    public void setLifeCycleStage(LifeCycleStage lifeCycleStage) {
        this.lifeCycleStage = lifeCycleStage;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public Object identity() {
        return null;
    }
}
