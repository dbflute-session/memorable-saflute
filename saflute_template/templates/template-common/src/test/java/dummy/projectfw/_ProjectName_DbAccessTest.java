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
package ${packageName}.projectfw;

import ${packageName}.dbflute.exbhv.MemberBhv;
import ${packageName}.dbflute.exentity.Member;
import ${packageName}.unit.UnitCommonPjContainerTestCase;
import org.dbflute.cbean.result.ListResultBean;

/**
 * @author saflute_template
 */
public class ${ProjectName}DbAccessTest extends UnitCommonPjContainerTestCase {

    protected MemberBhv memberBhv;

    public void test_dbaccess_Tx() {
        // ${_DS_} Arrange ${_DS_}
        // ${_DS_} Act ${_DS_}
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
        });

        // ${_DS_} Assert ${_DS_}
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            log(member);
        }
    }
}
