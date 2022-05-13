package com.tlc.crm.product.api.models;

import com.tlc.validator.TlcModel;
import com.tlc.validator.type.Group.Create;
import com.tlc.validator.type.Group.Update;
import com.tlc.validator.type.Group.Delete;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class Product implements TlcModel {

    @NotNull(groups = {Update.class, Delete.class}, message = "i18n.product.invalid.id")
    private Long id;

    @NotEmpty(groups = {Create.class, Update.class}, message = "i18n.product.invalid.name")
    private String name;

    @NotNull(groups = {Create.class, Update.class}, message = "i18n.common.org.invalid.id")
    private Long orgId;

    @Size(max = 200, groups = {Create.class, Update.class}, message = "i18n.product.invalid.description")
    private String description;

    @NotEmpty(groups = {Create.class, Update.class}, message = "i18n.product.invalid.skuNumber")
    private String skuNumber;

    @NotEmpty(groups = {Create.class, Update.class}, message = "i18n.product.invalid.productCode")
    private String productCode;

    @NotNull(groups = {Create.class, Update.class}, message = "i18n.product.category.invalid")
    @Valid
    private Category category;

    @NotEmpty(groups = {Create.class, Update.class}, message = "i18n.product.pricetag.invalid")
    @Valid
    private List<PriceTag> priceTags;

    @Override
    public Long id() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Long orgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getSkuNumber() {
        return skuNumber;
    }

    public void setSkuNumber(String skuNumber) {
        this.skuNumber = skuNumber;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<PriceTag> getPriceTags() {
        return this.priceTags;
    }

    public void addPriceTag(PriceTag priceTag) {
        this.priceTags = this.priceTags == null ? new ArrayList<>() : this.priceTags;

        this.priceTags.add(priceTag);
    }

    public void setPriceTags(List<PriceTag> priceTags) {
        this.priceTags = priceTags;
    }

    @Override
    public Object identity() {
        return null;
    }
}
