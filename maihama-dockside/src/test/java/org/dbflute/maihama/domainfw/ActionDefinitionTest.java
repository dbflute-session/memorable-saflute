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
package org.dbflute.maihama.domainfw;

import java.io.File;

import org.dbflute.maihama.domainfw.police.ActionComponentPolice;
import org.dbflute.maihama.domainfw.police.HotDeployDestroyerPolice;
import org.dbflute.maihama.domainfw.police.NonActionExtendsActionPolice;
import org.dbflute.maihama.domainfw.police.NonWebHasWebReferencePolice;
import org.dbflute.maihama.domainfw.police.WebPackageNinjaReferencePolice;
import org.dbflute.maihama.unit.UnitDocksideContainerTestCase;
import org.dbflute.saflute.web.action.login.LoginManager;
import org.dbflute.utflute.seasar.s2container.InjectedResourceDefinitionPolice;

/**
 * @author jflute
 */
public class ActionDefinitionTest extends UnitDocksideContainerTestCase {

    public void test_checkActionUrlPattern() {
        checkActionUrlPattern();
    }

    public void test_component() throws Exception {
        policeStoryOfJavaClassChase(new ActionComponentPolice(tp -> getComponent(tp)));
    }

    public void test_hotDeployDestroyer() throws Exception {
        policeStoryOfJavaClassChase(new HotDeployDestroyerPolice(tp -> getComponent(tp)));
    }

    public void test_nonActionExtendsAction() throws Exception {
        policeStoryOfJavaClassChase(new NonActionExtendsActionPolice());
    }

    public void test_nonWebHasWebReference() throws Exception {
        policeStoryOfJavaClassChase(new NonWebHasWebReferencePolice() {
            @Override
            protected void check(File srcFile, Class<?> clazz, String packageKeyword) {
                if (LoginManager.class.isAssignableFrom(clazz)) { // specially except
                    return;
                }
                super.check(srcFile, clazz, packageKeyword);
            }
        });
    }

    public void test_webPackageNinjaReferencePolice() throws Exception {
        policeStoryOfJavaClassChase(new WebPackageNinjaReferencePolice());
    }

    public void test_injectedResourceDefinitionPolice() throws Exception {
        policeStoryOfJavaClassChase(new InjectedResourceDefinitionPolice().shouldBeProtectedField(field -> {
            return true; /* means all fields */
        }));
    }
}
