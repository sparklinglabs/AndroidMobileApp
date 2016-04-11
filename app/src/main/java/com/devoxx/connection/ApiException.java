package com.devoxx.connection;

import com.devoxx.connection.model.ErrorMessageModel;

import java.io.IOException;

public class ApiException extends IOException {
	private ErrorMessageModel errorMessageModel;

	public ApiException(ErrorMessageModel errorMessageModel) {
		this.errorMessageModel = errorMessageModel;
	}

	public ErrorMessageModel getErrorMessageModel() {
		return errorMessageModel;
	}
}
