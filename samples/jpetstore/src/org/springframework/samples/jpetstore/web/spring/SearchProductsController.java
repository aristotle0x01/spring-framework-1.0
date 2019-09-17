package org.springframework.samples.jpetstore.web.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.samples.jpetstore.domain.logic.PetStoreFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author Juergen Hoeller
 * @since 30.11.2003
 */
public class SearchProductsController implements Controller {

	private PetStoreFacade petStore;

	public void setPetStore(PetStoreFacade petStore) {
		this.petStore = petStore;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (request.getParameter("search") != null) {
			String keyword = request.getParameter("keyword");
			if (keyword == null || keyword.length() == 0) {
				return new ModelAndView("Error", "message", "Please enter a keyword to search for, then press the search button.");
			}
			else {
				PagedListHolder productList = new PagedListHolder(this.petStore.searchProductList(keyword.toLowerCase()));
				productList.setPageSize(4);
				request.getSession().setAttribute("SearchProductsController_productList", productList);
				return new ModelAndView("SearchProducts", "productList", productList);
			}
		}
		else {
			String page = request.getParameter("page");
			PagedListHolder productList = (PagedListHolder) request.getSession().getAttribute("SearchProductsController_productList");
			if ("next".equals(page)) {
				productList.nextPage();
			}
			else if ("previous".equals(page)) {
				productList.previousPage();
			}
			return new ModelAndView("SearchProducts", "productList", productList);
		}
	}

}
