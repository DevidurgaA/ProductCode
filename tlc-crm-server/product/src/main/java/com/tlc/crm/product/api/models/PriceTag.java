package com.tlc.crm.product.api.models;

import com.tlc.validator.TlcModel;
import com.tlc.validator.type.Group.Create;
import com.tlc.validator.type.Group.Delete;
import com.tlc.validator.type.Group.Update;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PriceTag implements TlcModel {

    @NotNull(groups = {Update.class, Delete.class}, message = "i18n.product.pricetag.invalid.id")
    private Long id;

    @NotNull(groups = {Create.class, Update.class}, message = "i18n.product.currency.invalid")
    @Valid
    private Currency currency;

    @Min(value = 0, groups= {Create.class, Update.class}, message = "i18n.product.pricetag.invalid.price")
    private Double price;

    @NotNull(groups = {Create.class, Update.class}, message = "i18n.common.org.invalid.id")
    private Long orgId;

    @NotNull(groups = {Update.class}, message = "i18n.product.invalid.id")
    private Long productId;

    @Override
    public Long id() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long orgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getProductId() {
        return this.productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    @Override
    public Object identity() {
        return null;
    }
}
