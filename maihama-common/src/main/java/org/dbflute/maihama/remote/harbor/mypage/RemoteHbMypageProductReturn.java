package org.dbflute.maihama.remote.harbor.mypage;

import org.dbflute.maihama.dbflute.exentity.Product;
import org.seasar.struts.annotation.Required;

/**
 * @author jflute
 */
public class RemoteHbMypageProductReturn {

    @Required
    public final String productName;
    @Required
    public final Integer regularPrice;

    public RemoteHbMypageProductReturn(Product product) {
        this.productName = product.getProductName();
        this.regularPrice = product.getRegularPrice();
    }

    @Override
    public String toString() {
        return "{" + productName + ", " + regularPrice + "}";
    }
}
