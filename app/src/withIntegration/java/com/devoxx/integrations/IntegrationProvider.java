package com.devoxx.integrations;

import com.devoxx.integrations.huntly.HuntlyIntegrationController;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

@EBean
public class IntegrationProvider {
	@Bean(HuntlyIntegrationController.class)
	IntegrationController integrationController;

	public IntegrationController provideIntegrationController() {
		return integrationController;
	}
}
