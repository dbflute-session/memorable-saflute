package org.dbflute.maihama.remote.harbor;

import javax.annotation.Resource;

import org.dbflute.maihama.remote.harbor.base.RemoteHbPagingReturn;
import org.dbflute.maihama.remote.harbor.product.RemoteHbProductRowReturn;
import org.dbflute.maihama.remote.harbor.product.RemoteHbProductSearchParam;
import org.dbflute.maihama.unit.UnitCommonPjContainerTestCase;
import org.dbflute.remoteapi.mock.MockHttpClient;
import org.dbflute.saflute.web.servlet.request.RequestManager;

/**
 * @author jflute
 */
public class RemoteHarborBhvTest extends UnitCommonPjContainerTestCase {

    @Resource
    private RequestManager requestManager;

    public void test_requestProductList_basic() {
        // ## Arrange ##
        RemoteHbProductSearchParam param = new RemoteHbProductSearchParam();
        param.productName = "S";
        String json = "{pageSize=4, currentPageNumber=1, allRecordCount=20, allPageCount=5, rows=[]}";
        MockHttpClient client = MockHttpClient.create(response -> {
            response.peekRequest(request -> {
                assertContainsAll(request.getBody().get(), "productName", param.productName);
            });
            response.asJsonDirectly(json, request -> true);
        });
        registerMock(client);
        RemoteHarborBhv bhv = new RemoteHarborBhv(requestManager);
        inject(bhv);

        // ## Act ##
        RemoteHbPagingReturn<RemoteHbProductRowReturn> ret = bhv.requestProductList(param);

        // ## Assert ##
        assertEquals(4, ret.pageSize);
        assertEquals(5, ret.allPageCount);
        assertEquals(20, ret.allRecordCount);
        assertEquals(5, ret.allPageCount);
        assertEquals(0, ret.rows.size());
    }
}
