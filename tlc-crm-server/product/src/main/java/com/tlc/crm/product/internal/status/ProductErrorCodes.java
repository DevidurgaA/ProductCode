package com.tlc.crm.product.internal.status;

import com.tlc.commons.code.ErrorCodeGroup;
import com.tlc.commons.code.ErrorCodeProvider;

/**
 *
 */
public enum ProductErrorCodes implements ErrorCodeProvider {

    PRODUCT_NOT_FOUND(0x20),
    PRICE_TAG_NOT_FOUND(0x21),
    CURRENCY_NOT_FOUND(0x22),
    CATEGORY_NOT_FOUND(0x23);

    private final int code;

    ProductErrorCodes(int localCode) {
        this.code = ProductErrorCodes.ProductErrorCodeGroup.GROUP.getConvertedCode(localCode);
    }

    @Override
    public int getCode()
    {
        return code;
    }

    private static class ProductErrorCodeGroup implements ErrorCodeGroup {
        private static final ErrorCodeGroup GROUP = new ProductErrorCodes.ProductErrorCodeGroup();
        @Override
        public int getPrefix()
        {
            return 0x20_0_0001;
        }
    }

}
