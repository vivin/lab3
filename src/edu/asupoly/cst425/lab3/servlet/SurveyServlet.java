/*CST425Lab3.Vivin Paliath & Paul Spaude, Oct 2012*/

package edu.asupoly.cst425.lab3.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.Enumeration;
import edu.asupoly.cst425.lab3.domain.*;
import edu.asupoly.cst425.lab3.service.RenderingService;


@SuppressWarnings("serial")
public class SurveyServlet extends HttpServlet
{
	private static final String PREFS = "User Preferences";					
	private static final String QUIT = "Cancel Answers and Logout";			
	private static final String NEXT = "Next Question";
	private static final String FINISH = "Finish Survey";
	private static final String QUITPREFS = "Save Preferences and Return";  
	private static final String SURVEY = "Enter Survey";
	private static final String MATCHES = "See your matches";
	private static final String LOGINRETURN = "Return to LogIn";
	private static final String COOKIENAME = "e425MatchPrefs";
	
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
				setupSurveyAndResultsFiles(surveyFileName, resultsFileName, context);
			
			} catch (IOException ioe) {
				log.info("Error in file retrieval.");
				ioe.printStackTrace(); // never a good idea to swallow exceptions!
				throw new ServletException(ioe);
			} catch (ClassNotFoundException ce)	{
				log.info("Error loading results from file.");
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
		HttpSession session = req.getSession(true); 
		
		String userName = req.getParameter("username");
		if (userName == null || userName.isEmpty())
		{																
			getResponse = RenderingService.renderLogin(rc, response);
		}
		else
		{
			log.info("Error: GET recieved and not supported. Setting 405 error!");
			getResponse = RenderingService.renderErrorScreen(response, req.getRequestURI(), 
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
		RenderingConfiguration rc = new RenderingConfiguration.RenderingConfigurationBuilder(req.getRequestURI()).surveyResults(results).build(); //setup render config
		
		//Check for username (login or home screen state)
		String username = req.getParameter("username");
		if (sessionCont == null && username != null)
		{			
			if (username == null || username.isEmpty())
			{				
				handleLogin(req, res, response, rc, username);
			}
			else
			{					
				handleHomeScreen(req, res, response, username);
			}
		}
		else
		{	
			UserSurveyResult userState = null;
			
			if (sessionCont != null)
			{
			  userState = (UserSurveyResult) sessionCont.getAttribute(sessionCont.getId());
			}
						 
			 Cookie cookie = null;
			 boolean displayVerticalPref = true;

			//Check for cookies and edit display preferences if found and horizontal is true
			Cookie[] cookies = req.getCookies();
			if (cookies != null)
			{
				for (int i=0; i < cookies.length; i++)
				{
					if (cookies[i].getName().equals(COOKIENAME))
					{
						if (cookies[i].getValue().equals("false"))
						{
						  displayVerticalPref = false;
						  cookie = cookies[i];
						}
					}
				}
			}

			//Check value of submit button
			String submit = req.getParameter("submit");

			if (  submit == null )
			{	
				log.info("Submit or userState was not found! Error in page linking or malicious user.");
				if (req.isRequestedSessionIdValid()) 
				{
					sessionCont.invalidate();	//erase conversational state
				}
				results.removeUserSurveyResultForUser(userState.getUser());
				response.append(RenderingService.renderErrorScreen(res, req.getRequestURI(), "You were linked here by accident", false));				
			}
			else if (submit.equals(QUIT) || submit.equals(LOGINRETURN))
			{
				handleUserQuit(req, res, response, sessionCont, rc, userState);
			}
			else if (submit.equals(PREFS))
			{
				log.info("User Modifying Question Display Preferences");
				response.append(RenderingService.renderUserPreferencesPage(rc, res)); //go to preferences
			}
			else if (submit.equals(MATCHES) || submit.equals(FINISH))
			{
				int pageNumber = userState.getStartPageNumber();
				log.info("User finished survey or selected debrief from home. Displaying matches screen.");
				
				handleDebriefScreen(req, res, response, sessionCont, userState,
						displayVerticalPref, submit, pageNumber);
			}
			else if ( submit.equals(NEXT) || submit.equals(QUITPREFS) || submit.equals(SURVEY) )
			{
				int pageNumber = userState.getStartPageNumber();
				
				if (submit.equals(NEXT) && pageNumber < _survey.getNumPages() )
				{   					
                    handleQuestions(req, res, response, userState,
							displayVerticalPref, pageNumber);
				}
				else if (submit.equals(SURVEY) && pageNumber < 1 )
				{
					response.append(RenderingService.renderQuestion(rc = new RenderingConfiguration.	//renders the first page of the survey
							RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(1).
							user(userState.getUser()).verticalDisplay(displayVerticalPref).surveyResults(results).build(), res));
					
					log.info("Using starting the survey. Page number is 1");

					userState.setStartPageNumber(1);  //page number is 1 as is first page
				}
				else if ( submit.equals(QUITPREFS) )
				{					
					String vertPref = "true";			//true is default
					String displayPref = req.getParameter("displayPreference");

					handleSelectPreferences(req, res, response, userState,
							cookie, displayVerticalPref, pageNumber, vertPref,
							displayPref);				
				}
				else
				{					
					log.info("Error cmd was not an expected type. Cmd: " +submit +".");					
					if (req.isRequestedSessionIdValid()) 
					{
						sessionCont.invalidate();	//erase conversational state
					}
					results.removeUserSurveyResultForUser(userState.getUser());
					response.append(RenderingService.renderErrorScreen(res, req.getRequestURI(), "", false));  //set non-user error for now	
				}
			} //end if/else check quit/preferences/surveyQ's/results
		} //end if/else check session

		res.setStatus(responseCode);
		res.setContentType("text/html");
		PrintWriter out= res.getWriter();
		out.println(response.toString());

	} //end doPost method
	
	
	private void setupSurveyAndResultsFiles(String surveyFileName,
			String resultsFileName, ServletContext context) throws IOException, ClassNotFoundException 
	{
		if ( new File (context.getRealPath(surveyFileName)).isFile() )
		{
			_survey = new Survey(surveyFileName, context);			//load survey from file specified in web.xml
			
			if (resultsFileName == null) 
			{ 
				resultsFileName = "/WEB-INF/classes/results";						
				log.info("Error: InitParameter for 'resultsFile' not found in web.xml!\n" +
						"Using default location: /%TOMCAT_HOME" +resultsFileName);
			}
			
			results = new SurveyResults(_survey, context.getRealPath(resultsFileName));   //location of results is specified in web.xml
									
			if ( new File(context.getRealPath(resultsFileName)).isFile() )
			{
				log.info("Restoring results file.");
				results.restore();
			} 
			else
			{
				log.info("No results found creating new file");
				boolean result = new File(context.getRealPath(resultsFileName)).createNewFile();
				if (!result)
				{
					log.info("Could Not Create a Results file!!! ERROR!");
					throw new IOException();
				}
			} //end setup results storage file init and location				
		}
		else
		{
			log.info("Error: File not found! File= " +context.getRealPath(surveyFileName) +". \n");									
		}
	} //end setupSurveyAndResultsFiles
	
	
	private void handleLogin(HttpServletRequest req, HttpServletResponse res, StringBuilder response,
			RenderingConfiguration rc, String username) 
	{
		if (username == null)
		{
			response.append(RenderingService.renderLogin(rc, res));			//display login
			log.info("Displaying login.");					
		}
		else
		{
			response.append(RenderingService.renderLoginError(rc, res,
					"Name cannot be blank! Please enter your first name followed by your last name.")); //prompt until valid userName
			log.info("Invalid login, entry was blank.");
		}
	} //end handleLogin
	
	
	private void handleHomeScreen(HttpServletRequest req, HttpServletResponse res,
			StringBuilder response, String username) 
	{
		RenderingConfiguration rc;
		HttpSession newSession = req.getSession(true);		//create new session if not found
		User newUser = new User(username);
		UserSurveyResult userResults = results.getUserSurveyResultForUser(newUser);  //get results if user previously took survey (but session has expired)
		rc = new RenderingConfiguration.RenderingConfigurationBuilder(req.getRequestURI()).surveyResults(results).user(newUser).build();

		if (userResults == null)
		{
			log.info("New or Returning user who hasn't completed the survey.");
			userResults = new UserSurveyResult(newSession.getId(), newUser, 0);
			results.addUserSurveyResult(userResults);                  
			response.append(RenderingService.renderUserHome(res, rc, true)); 	//display HTML for home screen as new user
		}
		else
		{
			log.info("Returing user who has completed the survey");
		    newUser = userResults.getUser();	
		    userResults.setStartPageNumber(-1);
			response.append(RenderingService.renderUserHome(res, rc, false));	//display HTML for home screen as is a returning user
		}

		
		newSession.setAttribute(newSession.getId(), userResults); //store the session object
				  
	} //end handleHomeScreen
	
	
	private void handleUserQuit(HttpServletRequest req, HttpServletResponse res, StringBuilder response,
			HttpSession sessionCont, RenderingConfiguration rc,
			UserSurveyResult userState) 
	{
		log.info("User quit. Displaying log in screen and erasing progress/store state information.");	
		if (req.isRequestedSessionIdValid()) 
		{
			sessionCont.invalidate();	//erase conversational state
		}
		
		if (userState != null)
		{
			results.removeUserSurveyResultForUser(userState.getUser());
		}
		response.append(RenderingService.renderLogin(rc, res));    //go to login (user logged out)
	} //end handleUserQuit
	
	
	private void handleDebriefScreen(HttpServletRequest req, HttpServletResponse res,
			StringBuilder response, HttpSession sessionCont,
			UserSurveyResult userState, boolean displayVerticalPref,
			String submit, int pageNumber) throws IOException
	{
		if ( (pageNumber == _survey.getNumPages() && submit.equals(FINISH)) || (pageNumber == -1 && submit.equals(MATCHES)) )
		{
			boolean noRadioClicked = false;
			
			if (submit.equals(FINISH))
			{
				try 
				{
					int answer = Integer.parseInt(req.getParameter("answer"));
					SurveyItem surveyItem = _survey.getSurveyItem(pageNumber);
					userState.setAnswerForSurveyItem(surveyItem, answer);                    
				
					results.persistUserSurveyResultForUser(userState.getUser());	//persist results
					results.save();
				} catch (NumberFormatException n)
				{					
					noRadioClicked = true;					 //handle no radio clicked
				} catch (IOException i)
				{
					log.info("Error saving the results file.");
				}				
			}

			if (noRadioClicked == false)
			{
				if (req.isRequestedSessionIdValid()) 
				{
					sessionCont.invalidate();	//erase conversational state
				}
				
				response.append(RenderingService.renderDebriefingScreen(new RenderingConfiguration.
		            RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(pageNumber).
		            user(userState.getUser()).verticalDisplay(displayVerticalPref).surveyResults(results).build(), res));    //go to matches/debrief screen
			}
			else
			{
				response.append(RenderingService.renderQuestion(new RenderingConfiguration.	//renders next page in the survey
						RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(pageNumber).
						user(userState.getUser()).verticalDisplay(displayVerticalPref).surveyResults(results).build(), res));
				log.info("User taking survey. Next page number is: " + pageNumber +".");
			}
		}
		else
		{					
			if (req.isRequestedSessionIdValid()) 
			{
				sessionCont.invalidate();	//erase conversational state
			}
			results.removeUserSurveyResultForUser(userState.getUser());
			response.append(RenderingService.renderErrorScreen(res, req.getRequestURI(), "", false));  //set non-user error for now
		}
	} //end handleDebriefScreen
	
	
	private void handleQuestions(HttpServletRequest req, HttpServletResponse res,
			StringBuilder response, UserSurveyResult userState,
			boolean displayVerticalPref, int pageNumber)  
	{
		try
		{
			int answer = Integer.parseInt(req.getParameter("answer"));
			SurveyItem surveyItem = _survey.getSurveyItem(pageNumber);
			userState.setAnswerForSurveyItem(surveyItem, answer);
		}catch (NumberFormatException n)
		{
			pageNumber--;
		}		

		pageNumber++;
		response.append(RenderingService.renderQuestion(new RenderingConfiguration.	//renders next page in the survey
				RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(pageNumber).
				user(userState.getUser()).verticalDisplay(displayVerticalPref).surveyResults(results).build(), res));
		log.info("User taking survey. Next page number is: " + pageNumber +".");
		userState.setStartPageNumber(pageNumber);
	}
	
	
	private void handleSelectPreferences(HttpServletRequest req, HttpServletResponse res,
			StringBuilder response,
			UserSurveyResult userState, Cookie cookie,
			boolean displayVerticalPref, int pageNumber, String vertPref,
			String displayPref) 
	{
		if (displayPref.equals("horizontal") && displayVerticalPref == true)
		{
			displayVerticalPref = false;
			vertPref = "false";
		}
		else if (displayPref.equals("vertical") && displayVerticalPref == false)
		{
			displayVerticalPref = true;
			vertPref = "true";
		}

		if (cookie != null)
		{
			cookie.setValue(vertPref);		//cookie found change the value if allowed
			res.addCookie(cookie);
		}
		else
		{
			Cookie prefsCookie = new Cookie(COOKIENAME, vertPref);	 //set a new cookie with preference
			res.addCookie(prefsCookie);
		}
		
		log.info("User modified preferences, stored cookie if allowed, and now returning to previous page: " +pageNumber +".");

		if (pageNumber < 1)
		{
			boolean newUser = false;
			if (pageNumber == 0) newUser=true;
			response.append(RenderingService.renderUserHome(res, new RenderingConfiguration.				//go back/render preferences screen
					RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(pageNumber).
					user(userState.getUser()).verticalDisplay(displayVerticalPref).surveyResults(results).build(), newUser));
		}
		else if (pageNumber > _survey.getNumPages())
		{
			response.append(RenderingService.renderDebriefingScreen(new RenderingConfiguration.
					RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(pageNumber).
					user(userState.getUser()).verticalDisplay(displayVerticalPref).surveyResults(results).build(), res)); //TODO see if this is needed can't rmbr if displayed or not
		}
		else if (pageNumber > 0 && pageNumber <= _survey.getNumPages())
		{
			response.append(RenderingService.renderQuestion(new RenderingConfiguration.
					RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(pageNumber).
					user(userState.getUser()).verticalDisplay(displayVerticalPref).surveyResults(results).build(), res));	//go back/render to survey question previously at
		}
	} //end handleSelectPreferences	
	
} //end SurveyServlet