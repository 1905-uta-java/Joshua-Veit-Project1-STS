package com.revature.project1.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.project1.auth.AuthToken;
import com.revature.project1.models.Employee;
import com.revature.project1.service.AuthService;
import com.revature.project1.service.EmployeeService;
import com.revature.project1.service.ServiceManager;
import com.revature.project1.util.InputCheckingUtil;
import com.revature.project1.util.PasswordUtil;

/**
 * Servlet implementation class EmployeeServlet
 */
public class EmployeeServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
	private AuthService authService = ServiceManager.getAuthService();
	private EmployeeService empService = ServiceManager.getEmpService();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmployeeServlet() {
        super();
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		AuthToken token = authService.getValidToken(request, response);
		
		if(token == null)
			return;
		
		String source = request.getParameter("source");
		
		if(source == null || source.isEmpty()) {
			response.getWriter().write("missing source");
			response.setStatus(400);
			return;
		}

		ObjectMapper om = new ObjectMapper();
		
		if("self".equals(source)) {
			
			Employee emp = empService.getEmployee(token.getUserId());
			
			String empString = om.writeValueAsString(emp);
			
			response.getWriter().write(empString);
			response.setStatus(200);
			
		} else if("subordinates".equals(source)) {
			
			List<Employee> subordinates = empService.getSubordinates(token.getUserId());
			
			String subordinatesString = om.writeValueAsString(subordinates);
			
			response.getWriter().write(subordinatesString);
			response.setStatus(200);
			
		} else {
			
			response.getWriter().write("invalid source");
			response.setStatus(400);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ObjectMapper om = new ObjectMapper();
		
		AuthToken token = authService.getValidToken(request, response);
		
		if(token == null)
			return;
		
		String updateType = request.getParameter("updateType");
		
		if(updateType == null || updateType.isEmpty()) {
			response.getWriter().write("missing updateType");
			response.setStatus(400);
			return;
		}
		
		if("email".equals(updateType)) {
			
			String email = request.getParameter("email");
			
			if(email == null || email.isEmpty()) {

				response.getWriter().write("missing email");
				response.setStatus(400);
				return;
			}
			
			if(!InputCheckingUtil.isEmailValid(email)) {

				response.getWriter().write("invalid email");
				response.setStatus(400);
				return;
			}
			
			if(empService.isEmailTaken(email)) {

				response.getWriter().write("email taken");
				response.setStatus(400);
				return;
			}
			
			String password = request.getParameter("password");
			
			if(password == null || password.isEmpty()) {
				
				response.getWriter().write("missing password");
				response.setStatus(400);
				return;
			}
			
			if(authService.verifyPassword(password, empService.getEmployee(token.getUserId()).getEmail()) == null){
				
				response.getWriter().write("incorrect password");
				response.setStatus(403);
				return;
			}
			
			Employee emp = empService.getEmployee(token.getUserId());
			
			emp.setEmail(email);
			
			empService.updateEmployee(emp);
			
			response.setStatus(200);
			
		} else if("password".equals(updateType)) {
			
			String oldPassword = request.getParameter("oldPassword");

			if(oldPassword == null || oldPassword.isEmpty()) {
				
				response.getWriter().write("missing oldPassword");
				response.setStatus(400);
				return;
			}
			
			Employee emp = empService.getEmployee(token.getUserId());
			
			if(authService.verifyPassword(oldPassword, emp.getEmail()) == null) {
				
				response.getWriter().write("incorrect oldPassword");
				response.setStatus(400);
				return;
			}
			
			String newPassword = request.getParameter("newPassword");
			
			if(newPassword == null || newPassword.isEmpty()) {
				
				response.getWriter().write("missing newPassword");
				response.setStatus(400);
				return;
			}
			
			if(!PasswordUtil.isValidPassword(newPassword)) {

				response.getWriter().write("invalid newPassword");
				response.setStatus(400);
				return;
			}
			
			empService.updatePassword(token.getUserId(), newPassword);
			
			response.setStatus(200);
			
		} else if("data".equals(updateType)) {
			
			String empString = request.getParameter("employee");
			
			if(empString == null || empString.isEmpty()) {

				response.getWriter().write("missing employee data");
				response.setStatus(400);
				return;
			}
			
			System.out.println("Updated Employee: " + empString);
			
			Employee emp;
			
			try {
				
				emp = om.readValue(empString, Employee.class);
				
			} catch (IOException e) {
				
				response.getWriter().write("invalid employee data");
				response.setStatus(400);
				return;
			}
			
			Employee storedEmp = empService.getEmployee(token.getUserId());
			
			storedEmp.setFirstName(emp.getFirstName());
			storedEmp.setLastName(emp.getLastName());
			storedEmp.setPhone(emp.getPhone());
			storedEmp.setAddress(emp.getAddress());
			storedEmp.setCity(emp.getCity());
			storedEmp.setState(emp.getState());
			storedEmp.setCountry(emp.getCountry());
			storedEmp.setPostalCode(emp.getPostalCode());
			
			empService.updateEmployee(storedEmp);
			
			response.setStatus(200);
			
		} else {
			
			response.getWriter().write("invalid updateType");
			response.setStatus(400);
		}
	}

}
