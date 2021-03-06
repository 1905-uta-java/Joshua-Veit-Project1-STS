package com.revature.project1.service;

import java.util.Date;
import java.util.List;

import com.revature.project1.dao.ReimbursementDAO;
import com.revature.project1.models.ReimbursementRequest;

public class ReimbursementRequestService {
	
	ReimbursementDAO dao;
	
	public ReimbursementRequestService(ReimbursementDAO dao) {
		this.dao = dao;
	}
	
	public List<ReimbursementRequest> getSubordinateRequests(int managerID) {
		
		return dao.getRequestsForManager(managerID);
	}
	
	public List<ReimbursementRequest> getUsersRequests(int employeeId) {
		
		return dao.getRequestsForEmployee(employeeId);
	}
	
	public void submitRequest(int employeeId, float amount) {
		
		ReimbursementRequest newRequest = new ReimbursementRequest(0, employeeId, amount, new Date(), 0, false);
		
		dao.createReimbursementRequest(newRequest);
	}
	
	public boolean resolveReimbursementRequest(int requestId, boolean approve, int managerId) {
		
		List<ReimbursementRequest> subordinateRequests = getSubordinateRequests(managerId);
		
		ReimbursementRequest request = null;
		
		for(ReimbursementRequest req : subordinateRequests) {
			
			if(req.getRequestID() == requestId) {
				request = req;
				break;
			}
		}
		
		if(request == null)
			return false;
		
		request.setManagerID(managerId);
		request.setWasApproved(approve);
		
		dao.updateReimbursementRequest(request);
		
		return true;
	}
	
	public ReimbursementRequest getReimbursementRequest(int requestID) {
		
		return dao.getReimbursementRequest(requestID);
	}
	
	public void removeRequest(int requestID) {
		
		dao.removeReimbursementRequest(requestID);
	}
}
