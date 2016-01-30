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
package org.dbflute.maihama.app.web.member.purchase;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.maihama.app.web.base.DocksideBaseAction;
import org.dbflute.maihama.app.web.member.MemberSearchRowBean;
import org.dbflute.maihama.dbflute.exbhv.MemberBhv;
import org.dbflute.maihama.dbflute.exbhv.PurchaseBhv;
import org.dbflute.maihama.dbflute.exentity.Member;
import org.dbflute.maihama.dbflute.exentity.Purchase;
import org.dbflute.maihama.domainfw.action.DocksideLoginRequired;
import org.dbflute.maihama.projectfw.web.paging.PagingNavi;
import org.seasar.struts.annotation.ActionForm;
import org.seasar.struts.annotation.Execute;

/**
 * 会員購入一覧アクション。
 * @author jflute
 */
@DocksideLoginRequired
public class MemberPurchaseListAction extends DocksideBaseAction {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          DI Component
    //                                          ------------
    @ActionForm
    @Resource
    protected MemberPurchaseListForm memberPurchaseListForm;

    @Resource
    protected MemberBhv memberBhv;

    @Resource
    protected PurchaseBhv purchaseBhv;

    // -----------------------------------------------------
    //                                          Display Data
    //                                          ------------
    public MemberSearchRowBean headerBean;
    public List<MemberPurchaseSearchRowBean> beanList;
    public PagingNavi pagingNavi = createPagingNavi();

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute(validator = false, urlPattern = "{memberId}/{pageNumber}")
    public String index() {
        Integer memberId = memberPurchaseListForm.memberId;
        assertGetParameterExists(memberId);
        Integer pageNumber = memberPurchaseListForm.pageNumber;
        assertGetParameterExists(pageNumber);

        Member member = selectMember(memberId);
        headerBean = new MemberSearchRowBean();
        headerBean.memberId = member.getMemberId();
        headerBean.memberName = member.getMemberName();

        PagingResultBean<Purchase> purchasePage = selectPurchasePage(memberId, pageNumber);
        beanList = new ArrayList<MemberPurchaseSearchRowBean>();
        for (Purchase purchase : purchasePage) {
            MemberPurchaseSearchRowBean bean = new MemberPurchaseSearchRowBean();
            bean.purchaseId = purchase.getPurchaseId();
            bean.purchaseDatetime = purchase.getPurchaseDatetime();
            bean.productName = purchase.getProduct().get().getProductName();
            bean.purchasePrice = purchase.getPurchasePrice();
            bean.purchaseCount = purchase.getPurchaseCount();
            bean.paymentComplete = purchase.isPaymentCompleteFlgTrue();
            beanList.add(bean);
        }
        preparePagingNavi(pagingNavi, purchasePage, memberId);

        return path_MemberPurchase_MemberPurchaseListJsp;
    }

    @Execute(validator = false)
    public String doDelete() {
        Integer memberId = memberPurchaseListForm.memberId;
        assertGetParameterExists(memberId);
        Long purchaseId = memberPurchaseListForm.purchaseId;
        assertGetParameterExists(purchaseId);

        Purchase purchase = new Purchase();
        purchase.setPurchaseId(purchaseId);
        purchaseBhv.deleteNonstrict(purchase); // ここは排他制御なしの例 by jflute
        return index();
    }

    // ===================================================================================
    //                                                                               Logic
    //                                                                               =====
    protected Member selectMember(Integer memberId) {
        return memberBhv.selectEntityWithDeletedCheck(cb -> {
            cb.query().setMemberId_Equal(memberId);
        });
    }

    protected PagingResultBean<Purchase> selectPurchasePage(Integer memberId, Integer pageNumber) {
        int pageSize = 4; // 本当はコンフィグなどから取得するのが好ましい by jflute
        return purchaseBhv.selectPage(cb -> {
            cb.setupSelect_Product();
            cb.query().setMemberId_Equal(memberId);
            cb.query().addOrderBy_PurchaseDatetime_Desc();
            cb.paging(pageSize, pageNumber);
        });
    }
}
