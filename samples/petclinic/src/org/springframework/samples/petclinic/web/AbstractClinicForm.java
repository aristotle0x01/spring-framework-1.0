package org.springframework.samples.petclinic.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.ApplicationContextException;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * JavaBean abstract base class for petclinic-aware form controllers.
 * Provides convenience methods for subclasses.
 *
 * @author Ken Krebs
 */
abstract public class AbstractClinicForm extends SimpleFormController {

	private Clinic clinic;

	public void setClinic(Clinic clinic) {
		this.clinic = clinic;
	}

	protected Clinic getClinic() {
		return this.clinic;
	}

	public void afterPropertiesSet() throws Exception {
		if (clinic == null)
			throw new ApplicationContextException("Must set clinic bean property on " + getClass());
	}

	/**
	 * Set up a custom property editor for the application's date format.
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, null, new CustomDateEditor(dateFormat, false));
	}

	/**
	 * Method disallows duplicate form submission.
	 * Typically used to prevent duplicate insertion of <code>Entity</code>s
	 * into the datastore. Shows a new form with an error message.
	 */
	protected ModelAndView disallowDuplicateFormSubmission(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		BindException errors = new BindException(formBackingObject(request), getCommandName());
		errors.reject("duplicateFormSubmission", null, "Duplicate form submission");
		return showForm(request, response, errors);
	}

}
