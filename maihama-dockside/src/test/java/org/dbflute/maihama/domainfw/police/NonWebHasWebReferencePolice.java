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
package org.dbflute.maihama.domainfw.police;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.saflute.web.servlet.cookie.CookieManager;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.dbflute.saflute.web.servlet.request.ResponseManager;
import org.dbflute.saflute.web.servlet.session.SessionManager;
import org.dbflute.utflute.core.filesystem.FilesystemPlayer;
import org.dbflute.utflute.core.policestory.javaclass.PoliceStoryJavaClassHandler;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.6.0B (2015/12/27 Sunday)
 */
public class NonWebHasWebReferencePolice implements PoliceStoryJavaClassHandler {

    public void handle(File srcFile, Class<?> clazz) {
        check(srcFile, clazz, getLogicKeyword());
    }

    protected String getLogicKeyword() {
        return ".app.logic.";
    }

    protected void check(File srcFile, Class<?> clazz, String packageKeyword) {
        if (!clazz.getName().contains(packageKeyword)) {
            return;
        }
        // checked when also creator process so only small check here
        final String webPackageKeyword = getWebPackageKeyword();
        new FilesystemPlayer().readLine(srcFile, "UTF-8", line -> {
            if (line.startsWith("import ")) {
                final String imported = extractImported(line);
                if (imported.contains(webPackageKeyword)) {
                    throwNonWebHasWebReferenceException(clazz, imported);
                }
                if (isWebComponent(imported)) {
                    throwNonWebHasWebReferenceException(clazz, imported);
                }
            }
        });
    }

    protected String getWebPackageKeyword() {
        return ".app.web.";
    }

    protected String extractImported(String line) {
        return Srl.substringFirstFront(Srl.ltrim(Srl.substringFirstRear(line, "import "), "static "), ";");
    }

    protected boolean isWebComponent(String imported) {
        return Srl.equalsPlain(imported // is class name
                , RequestManager.class.getName() // lastaflute request
                , ResponseManager.class.getName() // lastaflute response
                , SessionManager.class.getName() // lastaflute session
                , CookieManager.class.getName() // lastaflute cookie
                , HttpServletRequest.class.getName() // servlet request
                , HttpServletResponse.class.getName() // servlet response
                , HttpSession.class.getName() // servlet session
                );
    }

    protected void throwNonWebHasWebReferenceException(Class<?> componentType, Object target) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Web reference from the non-web object.");
        br.addItem("Advice");
        br.addElement("Non-web object should not refer web resources,");
        br.addElement(" e.g. classes under 'app.web' package, RequestManager.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    public class SeaLogic {");
        br.addElement("        @Resource");
        br.addElement("        private RequestManager requestManager; // *Bad");
        br.addElement("");
        br.addElement("        public void land(SeaForm form) { // *Bad");
        br.addElement("            ...");
        br.addElement("        }");
        br.addElement("    }");
        br.addItem("Logic");
        br.addElement(componentType);
        br.addItem("Web Reference");
        br.addElement(target);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }
}
