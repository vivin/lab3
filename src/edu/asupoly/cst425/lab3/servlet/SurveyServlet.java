/*CST425Lab3.Vivin Paliath & Paul Spaude, Oct 2012*/
package edu.asupoly.cst425.lab3.servlet;

import javax.servlet.*;
import javax.servlet.http.*;

import edu.asupoly.cst425.lab3.domain.ConversationState;
import edu.asupoly.cst425.lab3.domain.RenderingConfiguration;
import edu.asupoly.cst425.lab3.domain.RenderingConfiguration.RenderingConfigurationBuilder;
import edu.asupoly.cst425.lab3.domain.Survey;
import edu.asupoly.cst425.lab3.domain.SurveyItem;
import edu.asupoly.cst425.lab3.domain.SurveyResults;
import edu.asupoly.cst425.lab3.domain.User;
import edu.asupoly.cst425.lab3.domain.UserSurveyResult;
import edu.asupoly.cst425.lab3.service.RenderingService;

import java.io.*;
import java.util.logging.Logger;


@SuppressWarnings("serial")
public class SurveyServlet extends HttpServlet
{
	private static Logger log = Logger.getLogger(SurveyServlet.class.getName());	
	private Survey _survey;
	private SurveyResults results;
		
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
		
		String surveyFileName = getServletConfig().getInitParameter("surveyfile");	//get fileName+path from initParam in web.xml	
		String resultsFileName = getServletConfig().getInitParameter("resultfile"); //""
		ServletContext context = getServletContext();							//get the context this servlet is running in	
		
		
		if (surveyFileName == null)
		{
			log.info("Error: InitParameter for 'surveyfile' not found in web.xml!\n");
		}		
		else
		{
			try 
			{				
				if ( new File (context.getRealPath(surveyFileName)).isFile() )
				{
					_survey = new Survey(surveyFileName, context);			//load survey from file specified in web.xml
					
					if (resultsFileName == null) 
					{ 
						String resultDefaultLoc = "/WEB-INF/classes/results.txt";
						results = new SurveyResults(_survey, resultDefaultLoc);		//location of results will be stored in default location
						log.info("Error: InitParameter for 'resultsFile' not found in web.xml!\n" +
								"Using default location: /%TOMCAT_HOME" +resultDefaultLoc);
					}
					else
					{
						results = new SurveyResults(_survey, resultsFileName);   //location of results is specified in web.xml
						
						if ( new File (context.getRealPath(resultsFileName)).isFile() )
						{
							results.restore();
						} //end check for results pre-existing
						
					} //end setup results storage file init and location				
				}
				else
				{
					log.info("Error: File not found! File= " +context.getRealPath(surveyFileName) +". \n");									
				}
			} catch (IOException ioe)
			{
				ioe.printStackTrace(); // never a good idea to swallow exceptions!
				throw new ServletException(ioe);
			} catch (ClassNotFoundException ce)
			{
				ce.printStackTrace();
				throw new ServletException(ce);
			}
		}
		log.info("Survey is: " + _survey.toString());
	} //end init

	
	public void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
	{	
		String getResponse;
		RenderingConfiguration rc = new RenderingConfiguration.RenderingConfigurationBuilder(req.getRequestURI()).build();
		
		String userName = req.getParameter("username");
		if (userName == null || userName.isEmpty())
		{
																//TODO Session stuff here. Figure out later. Othwerwise just error out. 
			getResponse = RenderingService.renderLogin(rc);
		}
		else
		{		
			getResponse = RenderingService.renderErrorScreen(req.getRequestURI(), 
					"HTTP Status 405 - HTTP method GET is not supported by this URL", true);				
		}
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType("text/html");
		PrintWriter out= response.getWriter();
		out.println(getResponse.toString());
	}
	
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException	
	{

		int responseCode = HttpServletResponse.SC_OK; 	 // we'll assume OK
			
		StringBuilder response = new StringBuilder("");	 //string to output (HTML)
		HttpSession sessionCont = req.getSession(false); //gets the current session if null; there is no session
		RenderingConfiguration rc = new RenderingConfiguration.RenderingConfigurationBuilder(req.getRequestURI()).build(); //setup render config	
						
		if (sessionCont == null )
		{					
			String username = req.getParameter("username");
			
			if (username == null || username.isEmpty()) 
			{	
				System.out.println("\n\n***NULL AND EMPTY***\n\n");
				if (username == null && req.getParameter("start").equals("start"))
				{
					response.append(RenderingService.renderLogin(rc));			//display login 
					System.out.println("\n\n***START***\n\n");
					if (req.getParameter("submit").equals("Start Now!"))
					{
						System.out.println("\n\n***START NOW SELECTED***\n\n");
					}
				}
				else
				{
					response.append(RenderingService.renderLoginError(rc, 
							"Name cannot be blank! Please enter your first name followed by your last name.")); //prompt until valid userName
					System.out.println("\n\n***BLANK LOGIN***\n\n");
				}
			}
			else
			{
				System.out.println("\n\n***Checking USER***\n\n");
				HttpSession newSession = req.getSession(true);		//create new session if not found
				User newUser = new User(username);				
				UserSurveyResult userResults = results.getUserSurveyResultForUser(newUser);  //get results if user previously took survey (but session has expired) 
				int setStartPageNmbr = -1; //default to returning user choice page				
				
				rc = new RenderingConfiguration.RenderingConfigurationBuilder(req.getRequestURI()).user(newUser).build();
				
				if (userResults == null)
				{
					System.out.println("\n\n***NEW USER***\n\n");
					userResults = new UserSurveyResult(newUser);
					results.addUserSurveyResult(userResults);
					setStartPageNmbr = 0;	//start at beginning of the survey									
					response.append(RenderingService.renderUserHome(rc, true)); 	//display HTML for home screen as new user
				}		
				else
				{
					System.out.println("\n\n***RETURNING USER***\n\n");
					//display HTML for home screen as is a returning user					
					response.append(RenderingService.renderUserHome(rc, false));					
				}
								
				ConversationState newState = new ConversationState(newSession.getId(), newUser, setStartPageNmbr); //setup object to store info about session
				newSession.setAttribute(newSession.getId(), newState); //store the session object					
			}
		}
		else
		{
			//TODO Move these to conts up top
			final String PREFS = "User Preferences";					//these are "cmds" that are used in SurveySurvlet and also the user sees these as buttons
			final String QUIT = "Cancel Answers and Logout";			//TODO change this to save answers and go home?
			final String NEXT = "Next Question";
			final String FINISH = "Finish Survey";
			final String QUITPREFS = "Save Preferences and Return";
		  
			final String SURVEY = "Enter Survey";
			final String MATCHES = "See your matches";
			final String HOMERETN = "Return to UserHome";
			final String LOGINRETN = "Return to LogIn";
			final String LOGIN = "Login to e425Match";			  //not a cmd
			
			
			System.out.println("\n\n***CONTINUING CONVERSATION USER***\n\n");
			ConversationState stateCont = (ConversationState) sessionCont.getAttribute(sessionCont.getId()); //TODO Check for cookie here for PREFERENCES
					
			String submit = req.getParameter("submit");
			
			if (submit == null || stateCont == null )  //TODO take submit off of here when ready 
			{
				response.append(RenderingService.renderErrorScreen(req.getRequestURI(), "Submit or stateCont was null", false));
				submit = "Cancel Answers and Go to Login";
			}			
			
			
			rc = new RenderingConfiguration.
					RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(stateCont.getPageNmbr()).user(stateCont.getUser()).
					verticalDisplay(true).surveyResults(results).build();		//TODO move this			
			
			
			if (submit.equals(QUIT) || submit.equals(LOGINRETN))
			{
				System.out.println("\n\n***QUIT SELECTED***\n\n");				
				if (req.isRequestedSessionIdValid()) sessionCont.invalidate();	//erase conversational state
				response.append(RenderingService.renderLogin(rc));    //go to login (user logged out)
			}
			else if (submit.equals(PREFS))
			{
				System.out.println("\n\n***USER PREFERENCES SELECTED***\n\n");
				response.append(RenderingService.renderUserPreferencesPage(rc)); //go to preferences
			}
			else if (submit.equals(MATCHES) || submit.equals(FINISH))
			{
				System.out.println("\n\n***DEBRIEF SELECTED***\n\n"); 
				if (stateCont.getPageNmbr() == _survey.getNumPages() && submit.equals(FINISH)) 
				{
					response.append(RenderingService.renderDebriefingScreen(rc));	//go to matches/debrief screen
				}
				else if (stateCont.getPageNmbr() == -1 && submit.equals(MATCHES))
				{
					response.append(RenderingService.renderDebriefingScreen(rc));	//go to matches/debrief screen
				}
				else
				{
					stateCont.setPageNmbr(0); //set page number to 0 an error or malicious user 
					response.append(RenderingService.renderErrorScreen(req.getRequestURI(), "", false));  //set non-user error for now
				}
			}
			else if ( submit.equals(NEXT) || submit.equals(QUITPREFS) || submit.equals(SURVEY) )
			{
				//render q's or start page for returning users. 
				
				System.out.println("\n\n***NEXT/SURVEY/QUITPREFS SELECTED***\n\n");
				//Next Question
				
				//TODO Save Preferences and Return  goes to Next Question
				
				String username = stateCont.getUser().getName();
			
				
				// This will be the page number of the page we just came from
				String pageNum = req.getParameter("pagenum");
				int pageNo = 0; // 0 means we came from before the survey
				if (pageNum != null && !pageNum.isEmpty()) 
				{
					try 
					{
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
				if (pageNo < 0 || pageNo > _survey.getNumPages()) 
				{
					// XXX somebody passed us an integer, but it was out of survey range
					// What error correction will you take?
				} else if (pageNo == 0) 
				{
					// XXX you came from the initial page, skip the answer logic
				} else 
				{ 
					// XXX check to see if there were responses on a page you were just on
					// if there were, stash them in conversational state for that user.
				}
				
				// Now we can move on to the response
		
				// OK, worry about generating the next page
				// XXX The work below assumes you clicked the Submit button on the demo
				// You will have to create an alternate response flow to handle "Quit"
				pageNo++;
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
			} //end if/else check quit/preferences/surveyQ's/results
		} //end if/else check session
		
		res.setStatus(responseCode);
		res.setContentType("text/html");
		PrintWriter out= res.getWriter();
		out.println(response.toString());
				
	} //end doPost method
} //end SurveyServlet