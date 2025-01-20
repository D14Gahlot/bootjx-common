package com.boot.jx.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.doc.ticket.CustomerTicketDoc;
import com.boot.jx.postman.doc.ticket.CustomerTicketStatus;
import com.boot.jx.postman.doc.ticket.CustomerTicketType;
import com.boot.jx.postman.store.QuickStore;

@RestController
public class AdminTicketController {

	@Autowired
	private QuickStore quickStore;

	@RequestMapping(value = "/api/tmpl/ticket", method = { RequestMethod.POST })
	public ApiResponse<CustomerTicketDoc, Object> ticket(HttpMethod method,
			@RequestBody CustomerTicketDoc customerTicket) throws InstantiationException, IllegalAccessException {
		return quickStore.submit(method, customerTicket, CustomerTicketDoc.class, "Ticket");
	}

	@RequestMapping(value = "/api/tmpl/ticket", method = { RequestMethod.DELETE, RequestMethod.GET })
	public ApiResponse<CustomerTicketDoc, Object> ticket(HttpMethod method, @RequestParam(required = false) String id)
			throws InstantiationException, IllegalAccessException {
		return quickStore.submit(method, id, CustomerTicketDoc.class, "Ticket");
	}

	@RequestMapping(value = "/api/tmpl/ticket/types", method = { RequestMethod.POST })
	public ApiResponse<CustomerTicketType, Object> ticketTypes(HttpMethod method,
			@RequestBody CustomerTicketType customerTicketType) throws InstantiationException, IllegalAccessException {
		return quickStore.submit(method, customerTicketType, CustomerTicketType.class, "Ticket Type");
	}

	@RequestMapping(value = "/api/tmpl/ticket/types", method = { RequestMethod.DELETE, RequestMethod.GET })
	public ApiResponse<CustomerTicketType, Object> ticketTypes(HttpMethod method,
			@RequestParam(required = false) String id) throws InstantiationException, IllegalAccessException {
		return quickStore.submit(method, id, CustomerTicketType.class, "Ticket Type");
	}

	@RequestMapping(value = "/api/tmpl/ticket/status", method = { RequestMethod.POST })
	public ApiResponse<CustomerTicketStatus, Object> ticketStatus(HttpMethod method,
			@RequestBody CustomerTicketStatus customerTicketStatus)
			throws InstantiationException, IllegalAccessException {
		return quickStore.submit(method, customerTicketStatus, CustomerTicketStatus.class, "Ticket Status");
	}

	@RequestMapping(value = "/api/tmpl/ticket/status", method = { RequestMethod.DELETE, RequestMethod.GET })
	public ApiResponse<CustomerTicketStatus, Object> ticketStatus(HttpMethod method,
			@RequestParam(required = false) String id) throws InstantiationException, IllegalAccessException {
		return quickStore.submit(method, id, CustomerTicketStatus.class, "Ticket Status");
	}

}
