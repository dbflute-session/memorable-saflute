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
package ${packageName}.app.web.login;

import javax.annotation.Resource;

import ${packageName}.app.web.IndexAction;
import ${packageName}.app.web.base.${AppName}BaseAction;
import ${packageName}.domainfw.action.${AppName}PerformLogin;
import ${packageName}.domainfw.action.${AppName}UserBean;
import org.seasar.struts.annotation.ActionForm;
import org.seasar.struts.annotation.Execute;


/**
 * The action of member's Login.
 * @author saflute_template
 */
public class LoginIndexAction extends ${AppName}BaseAction {

	// ===================================================================================
	//                                                                           Attribute
	//                                                                           =========
	// -----------------------------------------------------
	//                                          DI Component
	//                                          ------------
	@ActionForm
	@Resource
	public LoginForm loginForm;

	// ===================================================================================
	//                                                                             Execute
	//                                                                             =======
	@Execute(validator = false)
	public String index() {
		${AppName}UserBean userBean = getUserBean();
		if (userBean.isLogin()) {
			return redirect(IndexAction.class);
		}
		return path_Login_LoginJsp;
	}

	@${AppName}PerformLogin
	@Execute(validator=true, input = path_Login_LoginJsp)
	public String doLogin() {
		memberLoginLogic.login(loginForm.email, loginForm.password, loginForm.isAutoLoginTrue());
		return redirect(IndexAction.class);
	}
}