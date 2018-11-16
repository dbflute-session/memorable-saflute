/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.maihama.app.web.product;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.maihama.app.web.base.DocksideBaseAction;
import org.dbflute.maihama.dbflute.allcommon.CDef;
import org.dbflute.maihama.dbflute.exbhv.ProductBhv;
import org.dbflute.maihama.dbflute.exentity.Product;
import org.dbflute.saflute.web.action.response.JsonResponse;
import org.seasar.struts.annotation.ActionForm;
import org.seasar.struts.annotation.Execute;

/**
 * @author jflute
 */
public class ProductListAction extends DocksideBaseAction {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @ActionForm
    @Resource
    protected ProductSearchForm productSearchForm;

    @Resource
    private ProductBhv productBhv;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute(validator = false)
    public JsonResponse index() {
        PagingResultBean<Product> page = selectProductPage();
        List<ProductSearchRowBean> beans = page.stream().map(product -> {
            return mappingToBean(product);
        }).collect(Collectors.toList());
        return asJson(beans);
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    private PagingResultBean<Product> selectProductPage() {
        return productBhv.selectPage(cb -> {
            cb.setupSelect_ProductStatus();
            cb.setupSelect_ProductCategory();
            cb.specify().derivedPurchase().max(purchaseCB -> {
                purchaseCB.specify().columnPurchaseDatetime();
            }, Product.ALIAS_latestPurchaseDate);
            if (productSearchForm.productName != null) {
                cb.query().setProductName_LikeSearch(productSearchForm.productName, op -> op.likeContain());
            }
            if (productSearchForm.purchaseMemberName != null) {
                cb.query().existsPurchase(purchaseCB -> {
                    purchaseCB.query().queryMember().setMemberName_LikeSearch(productSearchForm.purchaseMemberName, op -> op.likeContain());
                });
            }
            if (productSearchForm.productStatus != null) {
                cb.query().setProductStatusCode_Equal_AsProductStatus(CDef.ProductStatus.of(productSearchForm.productStatus).get());
            }
            cb.query().addOrderBy_ProductName_Asc();
            cb.query().addOrderBy_ProductId_Asc();
            cb.paging(4, productSearchForm.pageNumber != null ? productSearchForm.pageNumber : 1);
        });
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    private ProductSearchRowBean mappingToBean(Product product) {
        ProductSearchRowBean bean = new ProductSearchRowBean();
        bean.productId = product.getProductId();
        bean.productName = product.getProductName();
        product.getProductStatus().alwaysPresent(status -> {
            bean.productStatus = status.getProductStatusName();
        });
        product.getProductCategory().alwaysPresent(category -> {
            bean.productCategory = category.getProductCategoryName();
        });
        bean.regularPrice = product.getRegularPrice();
        bean.latestPurchaseDate = product.getLatestPurchaseDate();
        return bean;
    }
}
