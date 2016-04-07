package com.devoxx.integrations;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

@EBean
public class IntegrationProvider {
	@Bean(EmptyIntegrationController.class)
	IntegrationController integrationController;

	public IntegrationController provideIntegrationController() {
		return integrationController;
	}
}
