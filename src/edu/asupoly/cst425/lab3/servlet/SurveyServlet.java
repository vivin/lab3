/**
 * 
 */
package edu.asupoly.cst425.lab3.servlet;

import javax.servlet.*;
import javax.servlet.http.*;

import edu.asupoly.cst425.lab3.domain.Survey;
import edu.asupoly.cst425.lab3.domain.SurveyItem;

import java.io.*;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class SurveyServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(SurveyServlet.class.getName());
	// The survey itself is a Singleton tied to the servlet, and immutable, so we are OK
	private Survey _survey;
	
	// XXX for you to consider: How will you track each user's answers to survey questions?
	// What data structure do you need and what is it's scope?
	
	public void init(ServletConfig config) throws ServletException {
		// if you forget this your getServletContext() will get a NPE! 
		super.init(config);
		
		// XXX You need to initialize _survey from a file using the Survey(filename)
		// constructor in Survey.java. This below is a hack for demo purposes, remove!
		try {
			_survey = new Survey();
		} catch (IOException ioe) {
			ioe.printStackTrace(); // never a good idea to swallow exceptions!
			throw new ServletException(ioe);
		}
		log.info("Survey is: " + _survey.toString());
	}

	// XXX There is no doGet here. Add one. What should it do (think HTTP)?
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res) 
		throws ServletException, IOException	{

		int responseCode = HttpServletResponse.SC_OK; // we'll assume OK
		
		// Follow our pattern - process request information first
		// First comes the username 
		String username = req.getParameter("username");
		if (username == null || username.isEmpty()) {
			// XXX we always expect username - what should we do here?
		}
		
		// Did the user submit answers or quit the survey?
		// XXX this demo only considers the case where the user submits answers

		// This will be the page number of the page we just came from
		String pageNum = req.getParameter("pagenum");
		int pageNo = 0; // 0 means we came from before the survey
		if (pageNum != null && !pageNum.isEmpty()) {
			try {
				pageNo = Integer.parseInt(pageNum);
			} catch (Exception exc) {
				// XXX somebody passed us a parameter value that wasn't an integer
			}
		} 
		
		/* XXX Next you have to implement the following logic to store answers:
		 * 1. If the page number is out of bounds, take an appropriate error action
		 * 2. If the page is number 0, it was the landing page, what do you do?
		 * 3. If the page was the last page of the survey, persist the user's answers
		 *    to a non-volatile store (of your choosing), then redirect her/him to a 
		 *    debriefing page which you must create as a separate servlet. That servlet
		 *    should list the answers the user gave and rank order the best matches
		 * 4. Otherwise you came from a "normal" survey page, save the answers for 
		 *    that user and that page in conversational state (not the persistent store).
		*/
		if (pageNo < 0 || pageNo > _survey.getNumPages()) {
			// XXX somebody passed us an integer, but it was out of survey range
			// What error correction will you take?
		} else if (pageNo == 0) {
			// XXX you came from the initial page, skip the answer logic
		} else { 
			// XXX check to see if there were responses on a page you were just on
			// if there were, stash them in conversational state for that user.
		}
		
		// Now we can move on to the response

		// OK, worry about generating the next page
		// XXX The work below assumes you clicked the Submit button on the demo
		// You will have to create an alternate response flow to handle "Quit"
		pageNo++;
		System.out.println("Next page number is " + pageNo);
		log.fine("Next page number is " + pageNo);
		
		StringBuffer sb = new StringBuffer();
		sb.append("<HTML>\n<HEAD>\n<TITLE>CST425 Lab 2 Given Survey</TITLE>\n</HEAD>\n<BODY>\n");
		
		// logic: is the survey done? If yes then save and go to the landing page
		if (pageNo > _survey.getNumPages()) {
			// XXX save the survey answers to the file
			// survey is complete, erase the conversational state (XXX you do that) and 
			// print a debrief page (XXX you add a link back to the homepage)
			sb.append("<em>You have finished the survey</em>\n");
		} else { // No, survey is not done: write the next survey question out		
			SurveyItem item = _survey.getSurveyItem(pageNo);
			sb.append("<p>\nSurvey page: " + pageNo + " for user " + username + "<br/>\n");
			sb.append(item.getQuestion() + "\n\n");
			sb.append("</p>\n<FORM ACTION=\"" + req.getRequestURI() + "\" METHOD=\"POST\">\n");
			// XXX These hidden parameters must go!
			sb.append("<INPUT TYPE=\"hidden\" NAME=\"pagenum\" VALUE=\"" + pageNo + "\"/>\n");
			sb.append("<INPUT TYPE=\"hidden\" NAME=\"username\" VALUE=\"" + username + "\"/>\n");

			// XXX You must take into account the user's individual preferences for rendering! 
			String[] choices = item.getChoices();
			for (int i = 0; choices != null && i < choices.length; i++) {
				// The answers Map stores choices by number
				sb.append("<INPUT TYPE=\"radio\" NAME=\"answer\" VALUE=\"" + i + "\">" + choices[i] + "</INPUT>\n<br/>\n");
			}
			sb.append("<INPUT TYPE=\"submit\" VALUE=\"Submit\"/>\n<br/>\n");
			sb.append("</FORM>\n"); 
		}
		sb.append("</BODY>\n</HTML>");
		// if we made it this far, we should be OK...
		res.setStatus(responseCode);
		res.setContentType("text/html");
		PrintWriter out= res.getWriter();
		out.println(sb.toString());
	}
}