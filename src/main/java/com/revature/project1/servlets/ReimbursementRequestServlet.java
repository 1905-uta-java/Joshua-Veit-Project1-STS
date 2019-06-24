package com.revature.project1.servlets;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.project1.auth.AuthToken;
import com.revature.project1.models.ReimbursementRequest;
import com.revature.project1.service.AuthService;
import com.revature.project1.service.EmployeeService;
import com.revature.project1.service.ReimbursementRequestService;
import com.revature.project1.service.ServiceManager;
import com.revature.project1.util.InputCheckingUtil;

/**
 * Servlet implementation class ReimbursementRequestServlet
 */
public class ReimbursementRequestServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
	private AuthService authService = ServiceManager.getAuthService();
	private ReimbursementRequestService reqService = ServiceManager.getReimbursementRequestService();
	private EmployeeService empService = ServiceManager.getEmpService();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReimbursementRequestServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ObjectMapper om = new ObjectMapper();
		
		AuthToken token = authService.getValidToken(request, response);
		
		if(token == null)
			return;
		
		String source = request.getParameter("source");
		
		if(source == null || source.isEmpty()) {
			response.getWriter().write("missing source");
			response.setStatus(400);
			return;
		}
		
		if("self".equals(source)) {
			
			List<ReimbursementRequest> requests = reqService.getUsersRequests(token.getUserId());
			
			String requestsString = om.writeValueAsString(requests);
			
			response.getWriter().write(requestsString);
			response.setStatus(200);
			
		} else if("subordinates".equals(source)) {
			
			List<ReimbursementRequest> requests = reqService.getSubordinateRequests(token.getUserId());
			
			String requestsString = om.writeValueAsString(requests);
			
			response.getWriter().write(requestsString);
			response.setStatus(200);
			
		} else if("subordinate".equals(source)) {
			
			String employeeIDString = request.getParameter("employeeID");
			
			if(employeeIDString == null || employeeIDString.isEmpty()) {
				
				response.getWriter().write("missing employeeID");
				response.setStatus(400);
				return;
			}
				
			
			int employeeID;
		 
			
			try {
				
				employeeID = Integer.parseInt(employeeIDString);
				
			} catch(NumberFormatException e) {
				
				response.getWriter().write("invalid employeeID");
				response.setStatus(400);
				return;
			}
			
			if(!empService.isManagerOf(token.getUserId(), employeeID)) {
				
				response.getWriter().write("illegal access - not manager of employee");
				response.setStatus(403);
				return;
			}
			
			List<ReimbursementRequest> requests = reqService.getUsersRequests(employeeID);
			
			String requestsString = om.writeValueAsString(requests);
			
			response.getWriter().write(requestsString);
			response.setStatus(200);
			
		} else {
			
			response.getWriter().write("invalid source");
			response.setStatus(400);
		}
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ObjectMapper om = new ObjectMapper();
		
		AuthToken token = authService.getValidToken(request, response);
		
		if(token == null)
			return;
		
		String amountString = request.getParameter("amount");
		
		if(amountString == null || amountString.isEmpty()) {
			
			response.getWriter().write("missing amount");
			response.setStatus(400);
			return;
		}
		
		float amount;
		
		try {
			
			amount = Float.parseFloat(amountString);
			
		} catch(NumberFormatException e) {
			
			response.getWriter().write("invalid amount");
			response.setStatus(400);
			return;
		}
		
		if(amount <= 0f) {
			
			response.getWriter().write("invalid amount");
			response.setStatus(400);
			return;
		}
		
		reqService.submitRequest(token.getUserId(), amount);
		response.setStatus(200);
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ObjectMapper om = new ObjectMapper();
		
		AuthToken token = authService.getValidToken(request, response);
		
		if(token == null)
			return;
		
		String requestIDString = request.getParameter("requestID");
		
		if(requestIDString == null || requestIDString.isEmpty()) {
			
			response.getWriter().write("missing requestID");
			response.setStatus(400);
			return;
		}
		
		int requestID;
		
		try {
			
			requestID = Integer.parseInt(requestIDString);
			
		} catch(NumberFormatException e) {
			
			response.getWriter().write("invalid requestID");
			response.setStatus(400);
			return;
		}
		
		if(requestID <= 0) {
			
			response.getWriter().write("invalid requestID");
			response.setStatus(400);
			return;
		}
		
		String approveString = request.getParameter("approve");
		
		if(approveString == null || approveString.isEmpty()) {
			
			response.getWriter().write("missing approve");
			response.setStatus(400);
			return;
		}
		
		if(!InputCheckingUtil.isStringBoolean(approveString)) {
			
			response.getWriter().write("invalid approve");
			response.setStatus(400);
			return;
		}
		
		boolean approve = Boolean.parseBoolean(approveString);
		
		if(!reqService.resolveReimbursementRequest(requestID, approve, token.getUserId())) {
			
			response.getWriter().write("illegal access - not manager of submitting employee");
			response.setStatus(403);
			return;
		}
		
		response.setStatus(200);
	}
}
