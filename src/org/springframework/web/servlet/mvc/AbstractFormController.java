/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.web.servlet.mvc;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>Form controller that autopopulates a form bean from the request.
 * This, either using a new bean instance per request, or using the same bean
 * when the <code>sessionForm</code> property has been set to <code>true</code>.
 * This class is the base class for both framework subclasses like
 * {@link SimpleFormController SimpleFormController} and
 * {@link AbstractWizardFormController AbstractWizardFormController}, and
 * custom form controllers you can provide yourself.</p>
 * <p>Both form- input-views and after-submission-views have to be provided
 * programmatically. To provide those views using configuration properties,
 * use the {@link SimpleFormController SimpleFormController}.</p>
 *
 * <p>Subclasses need to override showForm to prepare the form view, and
 * processFormSubmission to handle submit requests. For the latter, binding errors
 * like type mismatches will be reported via the given "errors" holder.
 * For additional custom form validation, a validator (property inherited from
 * BaseCommandController) can be used, reporting via the same "errors" instance.</p>
 *
 * <p>Comparing this Controller to the Struts notion of the <code>Action</code>
 * shows us that with Spring, you can use any ordinary JavaBeans or database
 * backed JavaBean without having to implement a framework specific class
 * (in case of Struts, this is <code>ActionForm</code>). More complex properties
 * of JavaBeans (Dates, Locales, but also your own application specific
 * or compound types) can be represented and submitted to the controller, by
 * using the notion of <code>java.beans.PropertyEditors</code>. For more information on that
 * subject, see the workflow of this controller and the explanation of the
 * {@link BaseCommandController BaseCommandController}.</p>
 *
 * <p><b><a name="workflow">Workflow
 * (<a href="BaseCommandController.html#workflow">and that defined by superclass</a>):</b><br>
 * <ol>
 *  <li>GET request on the controller is received</li>
 *  <li>call to {@link #formBackingObject formBackingObject()} which by default,
 *      returns an instance of the commandClass that has been configured
 *      (see the properties the superclass exposes), but can also be overriden
 *      to - for instance - retrieve an object from the database (that - for
 *      instance - needs to be modified using the form)</li>
 *  <li>call to {@link #initBinder initBinder()} which allows you to register
 *      custom editors for certain fields (often properties of non-primitive
 *      or non-Sring types) or the command class. This render appropriate
 *      Strings for for instance locales</li>
 *  <li>binding of the {@link org.springframework.web.bind.ServletRequestDataBinder ServletRequestDataBinder}
 *      in the request to be able to use the property editors in the form rendering
 *      (<i>only if <code>bindOnNewForm</code> is set to <code>true</code></i>)</li>
 *  <li>call to {@link #showForm(HttpServletRequest, HttpServletResponse, BindException) showForm()}
 *      to return a View that should be rendered (typically the view that renders
 *      the form). This method has be overridden in extending classes</li>
 *  <li>call to {@link #referenceData referenceData()} to allow you to bind
 *      any relevant reference data you might need when editing a form
 *      (for instance a List of Locale objects you're going to let the user
 *      select one from)</li>
 *  <li>model gets exposed and view gets rendered. Continue after user has filled
 *      in form</li>
 *  <li>POST request on the controller is received</li>
 *  <li>if <code>sessionForm</code> is not set, {@link #formBackingObject formBackingObject()}
 *      is called to retrieve a form object. Otherwise, the controller tries to
 *      find the command object which is already bound in the session. If it cannot
 *      find the object, it'll do a call to {@link #handleInvalidSubmit handleInvalidSubmit}
 *      which - by default - tries to create a new form object and resubmit the form</li>
 *  <li>controller tries to put all parameters from the request into the
 *      JavaBeans (command object) and if <code>validateOnBinding</code> is
 *      set, validation will occur</li>
 *  <li>call to {@link #onBindAndValidate onBindAndValidate()} which allows
 *      you to do custom processing after binding and validation (for instance
 *      to perform database persistency)</li>
 *  <li>call to {@link #processFormSubmission processFormSubmission} which, in implementing
 *      classes returns a sort of successview, for instance congratulating
 *      the user with a successfull form submission</li>
 * </ol>
 * </p>
 *
 * <p>Note that by default POST requests are treated as form submissions. This can be
 * customized by overriding isFormSubmission. Custom binding can be achieved either
 * by registering custom property editors before binding in an initBinder
 * implementation, or by custom bean population from request parameters after binding
 * in an onBindAndValidate implementation.</p>
 *
 * <p>In session form mode, a submission without an existing form object in the
 * session is considered invalid, like in case of a resubmit/reload by the browser.
 * The handleInvalidSubmit method is invoked then, trying a resubmit by default.
 * It can be overridden in subclasses to show respective messages or redirect to a
 * new form, in order to avoid duplicate submissions. The form object in the session
 * can be considered a transaction token in this case.</p>
 *
 * <p>Note that views should never retrieve form beans from the session but always
 * from the request, as prepared by the form controller. Remember that some view
 * technologies like Velocity cannot even access a HTTP session.</p>
 *
 * <p><b><a name="config">Exposed configuration properties</a>
 * (<a href="BaseCommandController.html#config">and those defined by superclass</a>):</b><br>
 * <table border="1">
 *  <tr>
 *      <td><b>name</b></td>
 *      <td><b>default</b></td>
 *      <td><b>description</b></td>
 *  </tr>
 *  <tr>
 *      <td>bindOnNewForm</td>
 *      <td>false</td>
 *      <td>Indicates whether to bind servletrequestparameters as well when
 *          creating a new form. If set to <code>true</code> this will happen,
 *          if set to <code>false</code>, the parameters will only be bound on
 *          formsubmissions</td>
 *  </tr>
 *  <tr>
 *      <td>sessionForm</td>
 *      <td>false</td>
 *      <td>Indicates whether or not the command object should be bound onto
 *          the session when a user asks for a new form. This allows you
 *          to for instance retrieve an object from the database, let the
 *          user edit it, and then persist it again. If this is set to false,
 *          a new command object will be created on all requests (both
 *          requests for the form and submissions of the form)</td>
 *  </tr>
 * </table>
 * </p>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Alef Arendsen
 * @see SimpleFormController
 * @see AbstractWizardFormController
 */
public abstract class AbstractFormController extends BaseCommandController {

	private boolean bindOnNewForm = false;

	private boolean sessionForm = false;

	/**
	 * Create a new AbstractFormController.
	 * <p>Subclasses should set the following properties, either in the constructor
	 * or via a BeanFactory: commandName, commandClass, bindOnNewForm, sessionForm.
	 * Note that commandClass doesn't need to be set when overriding
	 * formBackingObject, as the latter determines the class anyway.
	 * @see #setCommandName
	 * @see #setCommandClass
	 * @see #setBindOnNewForm
	 * @see #setSessionForm
	 * @see #formBackingObject
	 */
	public AbstractFormController() {
		super();
	}

	/**
	 * Set if request parameters should be bound to the form object
	 * in case of a non-submitting request, i.e. a new form.
	 */
	public final void setBindOnNewForm(boolean bindOnNewForm) {
		this.bindOnNewForm = bindOnNewForm;
	}

	/**
	 * Return if request parameters should be bound in case of a new form.
	 */
	protected final boolean isBindOnNewForm() {
		return bindOnNewForm;
	}

	/**
	 * Activate resp. deactivate session form mode. In session form mode,
	 * the form is stored in the session to keep the form object instance
	 * between requests, instead of creating a new one on each request.
	 * <p>This is necessary for either wizard-style controllers that populate a
	 * single form object from multiple pages, or forms that populate a persistent
	 * object that needs to be identical to allow for tracking changes.
	 */
	public final void setSessionForm(boolean sessionForm) {
		this.sessionForm = sessionForm;
	}

	/**
	 * Return if session form mode is activated.
	 */
	protected final boolean isSessionForm() {
		return sessionForm;
	}

	/**
	 * Return the name of the session attribute that holds
	 * the form object for this controller.
	 * @return the name of the form session attribute,
	 * or null if not in session form mode.
	 */
	protected final String getFormSessionAttributeName() {
		return isSessionForm() ? getClass() + ".form." + getCommandName() : null;
	}

	/**
	 * Handles two cases: form submissions and showing a new form.
	 * Delegates the decision between the two to isFormSubmission,
	 * always treating requests without existing form session attribute
	 * as new form when using session form mode.
	 */
	protected final ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if (isFormSubmission(request)) {
		  if (isSessionForm() && request.getSession().getAttribute(getFormSessionAttributeName()) == null) {
			  // cannot submit a session form if no form object is in the session
			  return handleInvalidSubmit(request, response);
		  }
			// process submit
			Object command = getCommand(request);
			ServletRequestDataBinder binder = bindAndValidate(request, command);
			return processFormSubmission(request, response, command, binder.getErrors());
		}
		else {
			return showNewForm(request, response);
		}
	}

	/**
	 * Determine if the given request represents a form submission.
	 * <p>Default implementation treats a POST request as form submission.
	 * Note: If the form session attribute doesn't exist when using session form
	 * mode, the request is always treated as new form by handleRequestInternal.
	 * <p>Subclasses can override this to use a custom strategy, e.g. a specific
	 * request parameter (assumably a hidden field or submit button name).
	 * @param request current HTTP request
	 * @return if the request represents a form submission
	 */
	protected boolean isFormSubmission(HttpServletRequest request) {
		return "POST".equals(request.getMethod());
	}

	/**
	 * Show a new form. Prepares a backing object for the current form
	 * and the given request, including checking its validity.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return the prepared form view
	 * @throws Exception in case of an invalid new form object
	 */
	protected final ModelAndView showNewForm(HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		// show new form
		logger.debug("Displaying new form");
		Object formObject = formBackingObject(request);
		if (formObject == null) {
			throw new ServletException("Form object returned by formBackingObject() may not be null");
		}
		if (!checkCommand(formObject)) {
			throw new ServletException("Form object returned by formBackingObject() must match commandClass");
		}
		// bind without validation, to allow for prepopulating a form, and for
		// convenient error evaluation in views (on both first attempt and resubmit)
		ServletRequestDataBinder binder = createBinder(request, formObject);
		if (isBindOnNewForm()) {
			logger.debug("Binding to new form");
			binder.bind(request);
		}
		return showForm(request, response, binder.getErrors());
	}

	/**
	 * Retrieve a backing object for the current form from the given request.
	 * <p>The properties of the form object will correspond to the form field values
	 * in your form view. This object will be exposed in the model under the specified
	 * command name, to be accessed under that name in the view: for example, with
	 * a "spring:bind" tag. The default command name is "command".
	 * <p>Note that you need to activate session form mode to reuse the form-backing
	 * object across the entire form workflow. Else, a new instance of the command
	 * class will be created for each submission attempt, just using this backing
	 * object as template for the initial form.
	 * <p>Default implementation calls BaseCommandController.createCommand,
	 * creating a new empty instance of the command class.
	 * Subclasses can override this to provide a preinitialized backing object.
	 * @param request current HTTP request
	 * @return the backing objact
	 * @throws Exception in case of invalid state or arguments
	 * @see #setCommandName
	 * @see #setCommandClass
	 * @see #createCommand
	 */
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return createCommand();
	}

	/**
	 * Prepare the form model and view, including reference and error data.
	 * Can show a configured form page, or generate a programmatic form view.
	 * <p>A typical implementation will call showForm(request,errors,"myView")
	 * to prepare the form view for a specific view name.
	 * <p>Note: If you decide to have a "formView" property specifying the
	 * view name, consider using SimpleFormController.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param errors validation errors holder
	 * @return the prepared form view, or null if handled directly
	 * @throws Exception in case of invalid state or arguments
	 * @see #showForm(HttpServletRequest, BindException, String)
	 * @see SimpleFormController#setFormView
	 */
	protected abstract ModelAndView showForm(HttpServletRequest request, HttpServletResponse response,
	                                         BindException errors) throws Exception;

	/**
	 * Prepare model and view for the given form, including reference and errors.
	 * In session form mode: Re-puts the form object in the session when returning
	 * to the form, as it has been removed by getCommand.
	 * Can be used in subclasses to redirect back to a specific form page.
	 * @param request current HTTP request
	 * @param errors validation errors holder
	 * @param viewName name of the form view
	 * @return the prepared form view
	 * @throws Exception in case of invalid state or arguments
	 */
	protected final ModelAndView showForm(HttpServletRequest request, BindException errors, String viewName)
	    throws Exception {
		return showForm(request, errors, viewName, null);
	}

	/**
	 * Prepare model and view for the given form, including reference and errors,
	 * adding a controller-specific control model.
	 * In session form mode: Re-puts the form object in the session when returning
	 * to the form, as it has been removed by getCommand.
	 * Can be used in subclasses to redirect back to a specific form page.
	 * @param request current HTTP request
	 * @param errors validation errors holder
	 * @param viewName name of the form view
	 * @param controlModel model map containing controller-specific control data
	 * (e.g. current page in wizard-style controllers).
	 * @return the prepared form view
	 * @throws Exception in case of invalid state or arguments
	 */
	protected final ModelAndView showForm(HttpServletRequest request, BindException errors, String viewName,
	                                      Map controlModel) throws Exception {
		if (isSessionForm()) {
			request.getSession().setAttribute(getFormSessionAttributeName(), errors.getTarget());
		}
		Map model = errors.getModel();
		Map referenceData = referenceData(request, errors.getTarget(), errors);
		if (referenceData != null) {
			model.putAll(referenceData);
		}
		if (controlModel != null) {
			model.putAll(controlModel);
		}
		return new ModelAndView(viewName, model);
	}

	/**
	 * Create a reference data map for the given request, consisting of
	 * bean name/bean instance pairs as expected by ModelAndView.
	 * <p>Default implementation returns null.
	 * Subclasses can override this to set reference data used in the view.
	 * @param request current HTTP request
	 * @param command form object with request parameters bound onto it
	 * @param errors validation errors holder
	 * @return a Map with reference data entries, or null if none
	 * @throws Exception in case of invalid state or arguments
	 * @see ModelAndView
	 */
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		return null;
	}

	/**
	 * Handle an invalid submit request, e.g. when in session form mode but no form object
	 * was found in the session (like in case of an invalid resubmit by the browser).
	 * <p>Default implementation simply tries to resubmit the form with a new form object.
	 * This should also work if the user hit the back button, changed some form data,
	 * and resubmitted the form.
	 * <p>Note: To avoid duplicate submissions, you need to override this method.
	 * Either show some "invalid submit" message, or call showNewForm for resetting the
	 * form (prepopulating it with the current values if "bindOnNewForm" is true).
	 * In this case, the form object in the session serves as transaction token.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a prepared view, or null if handled directly
	 * @throws Exception in case of errors
	 * @see #showNewForm
	 * @see #setBindOnNewForm
	 */
	protected ModelAndView handleInvalidSubmit(HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		Object command = formBackingObject(request);
		ServletRequestDataBinder binder = bindAndValidate(request, command);
		return processFormSubmission(request, response, command, binder.getErrors());
	}

	/**
	 * Return the form object for the given request.
	 * <p>Calls formBackingObject if not in session form mode. Else, retrieves the
	 * form object from the session. Note that the form object gets removed from
	 * the session, but it will be re-added when showing the form for resubmission.
	 * @param request current HTTP request
	 * @return object form to bind onto
	 * @throws Exception in case of invalid state or arguments
	 * @see #formBackingObject
	 */
	protected final Object getCommand(HttpServletRequest request) throws Exception {
		if (!isSessionForm()) {
			return formBackingObject(request);
		}
		HttpSession session = request.getSession(false);
		if (session == null) {
			throw new ServletException("Must have session when trying to bind");
		}
		Object formObject = session.getAttribute(getFormSessionAttributeName());
		session.removeAttribute(getFormSessionAttributeName());
		if (formObject == null) {
			throw new ServletException("Form object not found in session");
		}
		return formObject;
	}

	/**
	 * Process form submission request. Called by handleRequestInternal in case
	 * of a form submission.
	 * <p>Subclasses can override this to provide custom submission handling
	 * like triggering a custom action. They can also provide custom validation
	 * and call showForm or proceed with the submission accordingly.
	 * <p>Can call errors.getModel() to populate the ModelAndView model with the command
	 * and the Errors instance, under the specified bean name.
	 * @param request current servlet request
	 * @param response current servlet response
	 * @param command form object with request parameters bound onto it
	 * @param errors holder without errors (subclass can add errors if it wants to)
	 * @return the prepared model and view, or null
	 * @throws Exception in case of errors
	 * @see #isFormSubmission
	 * @see #showForm
	 * @see org.springframework.validation.Errors
	 */
	protected abstract ModelAndView processFormSubmission(HttpServletRequest request,	HttpServletResponse response,
	                                                      Object command, BindException errors)
			throws Exception;

}
