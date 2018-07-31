package com.ykb.tdp.testsuite.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

public class ValidationUtility {
	
	 public static ValidationError fromBindingErrors(Errors errors) {
	        ValidationError error = new ValidationError("Validation failed. " + errors.getErrorCount() + " error(s)");
	        for (ObjectError objectError : errors.getAllErrors()) {
	            error.addValidationError(objectError.getDefaultMessage());
	        }
	        return error;
	    }

}
