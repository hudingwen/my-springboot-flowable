
package com.hudingwen.myspringbootflowable.controller;

import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.common.security.DefaultPrivileges;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping( "/app" )
public class RemoteAccountResource {

	/**
	 * 重新工作流认证路由
	 * 修改前:return FLOWABLE.CONFIG.contextModelerRestRoot + '/rest/account';
	 * 修改后:return FLOWABLE.CONFIG.contextRoot +  '/app/rest/account';
	 * @return
	 */
	@RequestMapping(value = "/rest/account", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
	public UserRepresentation getAccount() {
		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setFirstName("admin");
		userRepresentation.setLastName("admin");
		userRepresentation.setFullName("admin");
		userRepresentation.setId("admin");
		List<String> pris = new ArrayList<>();
		pris.add(DefaultPrivileges.ACCESS_MODELER);
		pris.add(DefaultPrivileges.ACCESS_IDM);
		pris.add(DefaultPrivileges.ACCESS_ADMIN);
		pris.add(DefaultPrivileges.ACCESS_TASK);
		pris.add(DefaultPrivileges.ACCESS_REST_API);
		userRepresentation.setPrivileges(pris);

		return userRepresentation;
	}
}
