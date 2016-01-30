package org.dbflute.maihama.app.logic;

import org.dbflute.maihama.unit.UnitCommonPjContainerTestCase;

/**
 * @author jflute
 */
public class OneWayCryptoLogicTest extends UnitCommonPjContainerTestCase {

    public void test_encrypt_basic() throws Exception {
        // ## Arrange ##
        OneWayCryptoLogic logic = new OneWayCryptoLogic();
        inject(logic);

        // ## Act ##
        String encrypted = logic.encrypt("sea");

        // ## Assert ##
        log(encrypted);
        assertNotNull(encrypted);
        assertEquals("6fa11fb296a828adbe6d82956d28ba7035fa2c65", encrypted);
    }
}
