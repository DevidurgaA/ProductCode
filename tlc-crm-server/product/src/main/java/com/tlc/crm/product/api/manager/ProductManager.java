package com.tlc.crm.product.api.manager;

import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.common.config.AuditEntry;
import com.tlc.crm.product.api.models.Category;
import com.tlc.crm.product.api.models.PriceTag;
import com.tlc.crm.product.api.models.Product;
import com.tlc.crm.product.sql.resource.MPOPRODUCT;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.Table;
import com.tlc.validator.TlcModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProductManager extends AbstractConfigManager<Product> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductManager.class);

    private static class Instance {
        private static final ProductManager INSTANCE = new ProductManager();
    }

    private ProductManager() {
    }

    public static ProductManager getInstance() {
        return ProductManager.Instance.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Table getTable() {
        return Table.get(MPOPRODUCT.TABLE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadRowIntoContainer(final TlcModel relatedEntity, final Product model,
                                     final Row existingRow, final DataContainer dataContainer) {
        final PriceTagManager priceTagManager = PriceTagManager.getInstance();
        final Category category = model.getCategory();
        final List<PriceTag> priceTags = model.getPriceTags();
        final Row productRow = null == existingRow ? new Row(getTable()) : existingRow;

        productRow.set(MPOPRODUCT.NAME, model.getName());
        productRow.set(MPOPRODUCT.DESCRIPTION, model.getDescription());
        productRow.set(MPOPRODUCT.SKU_NUMBER, model.getSkuNumber());
        productRow.set(MPOPRODUCT.PRODUCT_CODE, model.getProductCode());
        productRow.set(MPOPRODUCT.CATEGORY_ID, model.getCategory().id());

        if (productRow.isNewRow()) {
            productRow.setOrgId(model.orgId());
            model.setId(productRow.getPKValue());
            dataContainer.addNewRow(productRow);
        } else {
            dataContainer.updateRow(productRow);
        }

        if (null != category && null != category.id()) {
            CategoryManager.getInstance().loadRowIntoContainer(category, dataContainer);
        }

        if (!priceTags.isEmpty()) {
            priceTagManager.loadRowsIntoContainer(model, priceTags, dataContainer);
        }

        if (!productRow.isNewRow()) {
            priceTagManager.deleteUnmappedPriceTags(model.orgId(), model.id(), model.getPriceTags(), dataContainer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Product convertRowToModel(final Row row) {
        final Product product = new Product();

        product.setId(row.getPKValue());
        product.setName(row.get(MPOPRODUCT.NAME));
        product.setOrgId(row.getOrgId());
        product.setDescription(row.get(MPOPRODUCT.DESCRIPTION));
        product.setSkuNumber(row.get(MPOPRODUCT.SKU_NUMBER));
        product.setProductCode(row.get(MPOPRODUCT.PRODUCT_CODE));
        product.setCategory(CategoryManager.getInstance().get(product.orgId(), (Long) row.get(MPOPRODUCT.CATEGORY_ID)));
        product.setPriceTags(PriceTagManager.getInstance().getPriceTagsOfProduct(product.orgId(), product.id()));

        return product;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Product partialGet(final Long orgId, final Long id) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuditEntry auditEntry(final Product model) {
        return null;
    }
}
