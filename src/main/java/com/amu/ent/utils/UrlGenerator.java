
package com.amu.ent.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlGenerator {

	@Value("${config.server.client.url}") private String serverURL;
	@Value("${config.context.path}") private String contextPath;
	public String goTo(HttpServletRequest request, String then) {
		return serverURL;
	}

}
