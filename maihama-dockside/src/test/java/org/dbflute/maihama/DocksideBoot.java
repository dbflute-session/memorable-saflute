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
package org.dbflute.maihama;

import org.dbflute.tomcat.TomcatBoot;

/**
 * @author jflute
 */
public class DocksideBoot {

    public static void main(String[] args) {
        TomcatBoot boot = new TomcatBoot(8088, "/dockside");
        // #for_now jflute JSP failure on latest Tomcat so it uses old version now (2023/11/01)
        //boot.useMetaInfoResourceDetect().useWebFragmentsDetect(jarName -> {
        //    return jarName.contains("swagger-ui");
        //});
        boot.asDevelopment().bootAwait();
    }

    /* 
     [JSP failure]
     // TomcatBoot-0.6.2 / Tomcat-8.5.30
     org.apache.jasper.JasperException: /WEB-INF/view/login/login.jsp (line: [1], column: [1]) null
        at org.apache.jasper.compiler.DefaultErrorHandler.jspError(DefaultErrorHandler.java:41)
        at org.apache.jasper.compiler.ErrorDispatcher.dispatch(ErrorDispatcher.java:291)
        at org.apache.jasper.compiler.ErrorDispatcher.jspError(ErrorDispatcher.java:97)
        at org.apache.jasper.compiler.Parser.processIncludeDirective(Parser.java:348)
        at org.apache.jasper.compiler.Parser.addInclude(Parser.java:399)
        at org.apache.jasper.compiler.Parser.parse(Parser.java:139)
        at org.apache.jasper.compiler.ParserController.doParse(ParserController.java:244)
        at org.apache.jasper.compiler.ParserController.parse(ParserController.java:105)
        at org.apache.jasper.compiler.Compiler.generateJava(Compiler.java:203)
        at org.apache.jasper.compiler.Compiler.compile(Compiler.java:374)
        at org.apache.jasper.compiler.Compiler.compile(Compiler.java:351)
        at org.apache.jasper.compiler.Compiler.compile(Compiler.java:335)
        at org.apache.jasper.JspCompilationContext.compile(JspCompilationContext.java:601)
      or (changed in trial...why?)
     java.lang.NullPointerException
       at org.apache.jsp.WEB_002dINF.view.login.login_jsp._jspInit(login_jsp.java:91)
       at org.apache.jasper.runtime.HttpJspBase.init(HttpJspBase.java:49)
    
     // TomcatBoot-0.5.6 / Tomcat-8.5.14
     (same)
    
     // TomcatBoot-0.3.2 / Tomcat-8.0.21
     (JSP works but cannot use swagger because useMetaInfoResourceDetect() is unsupported)
     */
}
