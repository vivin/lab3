/*CST425Lab3.Vivin Paliath & Paul Spaude, Oct 2012*/
package edu.asupoly.cst425.lab3.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.logging.Logger;
import edu.asupoly.cst425.lab3.domain.RenderingConfiguration;
import edu.asupoly.cst425.lab3.domain.Survey;
import edu.asupoly.cst425.lab3.domain.SurveyResults;
import edu.asupoly.cst425.lab3.domain.User;
import edu.asupoly.cst425.lab3.domain.UserSurveyResult;
import edu.asupoly.cst425.lab3.service.RenderingService;


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
						resultsFileName = "/WEB-INF/classes/results";						
						log.info("Error: InitParameter for 'resultsFile' not found in web.xml!\n" +
								"Using default location: /%TOMCAT_HOME" +resultsFileName);
					}
					
					results = new SurveyResults(_survey, context.getRealPath(resultsFileName));   //location of results is specified in web.xml
											
					if ( new File(context.getRealPath(resultsFileName)).isFile() )
					{
						System.out.println("Restoring results file.");
						results.restore();
					} 
					else
					{
						System.out.println("No results found creating new file");
					//	boolean result = new File(context.getRealPath(resultsFileName)).createNewFile();	//TODO FIX FILE STUFF (PERMISSIONS ISSUE?)
					//	System.out.println("***Created new file: " +result );
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
				System.out.println("NULL AND EMPTY");
				if (username == null && req.getParameter("start").equals("start"))
				{
					response.append(RenderingService.renderLogin(rc));			//display login 
					System.out.println("START");
					if (req.getParameter("submit").equals("Start Now!"))
					{
						System.out.println("START NOW SELECTED\n");
					}
				}
				else
				{
					response.append(RenderingService.renderLoginError(rc, 
							"Name cannot be blank! Please enter your first name followed by your last name.")); //prompt until valid userName
					System.out.println("BLANK LOGIN");
				}
			}
			else
			{
				System.out.println("Checking USER");
				HttpSession newSession = req.getSession(true);		//create new session if not found
				User newUser = new User(username);				
				UserSurveyResult userResults = results.getUserSurveyResultForUser(newUser);  //get results if user previously took survey (but session has expired) 
								
				rc = new RenderingConfiguration.RenderingConfigurationBuilder(req.getRequestURI()).user(newUser).build();
				
				if (userResults == null)
				{
					System.out.println("NEW USER");
					userResults = new UserSurveyResult(newSession.getId(), newUser, 0);
					results.addUserSurveyResult(userResults);														
					response.append(RenderingService.renderUserHome(rc, true)); 	//display HTML for home screen as new user
				}		
				else
				{
					System.out.println("RETURNING USER");
					userResults = new UserSurveyResult(newSession.getId(), newUser, -1);							
					response.append(RenderingService.renderUserHome(rc, false));	//display HTML for home screen as is a returning user							
				}
				
				newSession.setAttribute(newSession.getId(), userResults); //store the session object					
			}
		}
		else
		{
			//TODO Move these to consts up top
			final String PREFS = "User Preferences";					
			final String QUIT = "Cancel Answers and Logout";			
			final String NEXT = "Next Question";
			final String FINISH = "Finish Survey";
			final String QUITPREFS = "Save Preferences and Return";
		  
			final String SURVEY = "Enter Survey";
			final String MATCHES = "See your matches";
			final String HOMERETN = "Return to UserHome";
			final String LOGINRETN = "Return to LogIn";
			
			final String COOKIENAME = "e425MatchPrefs";
			
			//TODO REMOVE SYS OUTS
			System.out.println("CONTINUING CONVERSATION USER");
			UserSurveyResult userState = (UserSurveyResult) sessionCont.getAttribute(sessionCont.getId()); 
			Cookie cookie = null;
			boolean displayVerticalPref = true;
			
			//Check for cookies and edit display preferences if found and horizontal is true
			Cookie[] cookies = req.getCookies();			
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
					
			//Check value of submit button 
			String submit = req.getParameter("submit");
			
			if (submit == null || userState == null )  
			{
				System.out.println("submit or userState is null!\n");
				response.append(RenderingService.renderErrorScreen(req.getRequestURI(), "You were linked here by accident", false)); 
				submit = "Cancel Answers and Go to Login";
			}			
			else if (submit.equals(QUIT) || submit.equals(LOGINRETN))
			{
				System.out.println("QUIT SELECTED\n");				
				if (req.isRequestedSessionIdValid()) sessionCont.invalidate();	//erase conversational state
				results.removeUserSurveyResultForUser(userState.getUser());
				response.append(RenderingService.renderLogin(rc));    //go to login (user logged out)
			}
			else if (submit.equals(PREFS))
			{
				System.out.println("USER PREFERENCES SELECTED");
				response.append(RenderingService.renderUserPreferencesPage(rc)); //go to preferences
			}
			else if (submit.equals(MATCHES) || submit.equals(FINISH))
			{
				int pageNmbr = userState.getPageNmbr();
				System.out.println("DEBRIEF SELECTED");
				System.out.println("StatePages= " +pageNmbr +" | SurveyPageNumber= " +_survey.getNumPages() +". \n");
												
				if ( (pageNmbr == _survey.getNumPages() && submit.equals(FINISH)) || (pageNmbr == -1 && submit.equals(MATCHES)) ) 
				{	
					results.persistUserSurveyResultForUser(userState.getUser());	//persist results 
					results.save();
					
					response.append(RenderingService.renderDebriefingScreen(rc));	//go to matches/debrief screen
				}			
				else
				{
					userState.setPageNmbr(0); //set page number to 0 an error or malicious user 
					response.append(RenderingService.renderErrorScreen(req.getRequestURI(), "", false));  //set non-user error for now
				}
			}
			else if ( submit.equals(NEXT) || submit.equals(QUITPREFS) || submit.equals(SURVEY) )
			{
				System.out.println("NEXT/SURVEY/QUITPREFS SELECTED");
				
				int pageNmbr = userState.getPageNmbr();
				
				if (submit.equals(NEXT) && pageNmbr < _survey.getNumPages() )
				{   System.out.println("NEXT PAGE");
					
					
					//TODO STORE RESULTS OF LAST QUESTION 
					
				
					pageNmbr = pageNmbr+1;
					response.append(RenderingService.renderQuestion(rc = new RenderingConfiguration.	//renders next page in the survey
							RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(pageNmbr).
							user(userState.getUser()).verticalDisplay(displayVerticalPref).surveyResults(results).build()));
					System.out.println("Next page number is " + pageNmbr);
					userState.setPageNmbr(pageNmbr);
					
					
					
				}
				else if (submit.equals(SURVEY) && pageNmbr < 1 )
				{
					System.out.println("FIRST PAGE");					
					
					response.append(RenderingService.renderQuestion(rc = new RenderingConfiguration.	//renders the first page of the survey
							RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(1).
							user(userState.getUser()).verticalDisplay(displayVerticalPref).surveyResults(results).build()));
					System.out.println("Next page number is " + 1);
					
					userState.setPageNmbr(1);  //page number is 1 as is first page
				}
				else if ( submit.equals(QUITPREFS) )
				{					
					System.out.println("QUIT PREFS");
					String vertPref = "true";			//true is default
					String displayPref = req.getParameter("displayPreference");					
					
					if (displayPref.equals("horizontal") && displayVerticalPref == true)
					{
						displayVerticalPref = false;
						vertPref = "false";
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
					
										
					if (pageNmbr < 1)
					{
						boolean newUser = false;
						if (pageNmbr == 0) newUser=true;
						response.append(RenderingService.renderUserHome(rc = new RenderingConfiguration.				//go back/render preferences screen
								RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(pageNmbr).
								user(userState.getUser()).verticalDisplay(displayVerticalPref).surveyResults(results).build(), newUser));
					}
					else if (pageNmbr > _survey.getNumPages())
					{						
						response.append(RenderingService.renderDebriefingScreen(rc = new RenderingConfiguration.
								RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(pageNmbr).
								user(userState.getUser()).verticalDisplay(displayVerticalPref).surveyResults(results).build())); //TODO see if this is needed can't rmbr if displayed or not 
					}
					else if (pageNmbr > 0 && pageNmbr <= _survey.getNumPages())
					{
						response.append(RenderingService.renderQuestion(rc = new RenderingConfiguration.
								RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(pageNmbr).
								user(userState.getUser()).verticalDisplay(displayVerticalPref).surveyResults(results).build()));	//go back/render to survey question previously at
					}
					
					System.out.println("Same page number is " + pageNmbr);					
				}
				else
				{
					System.out.println("ERROR HERE");
					userState.setPageNmbr(0); //set page number to 0 an error or malicious user 
					response.append(RenderingService.renderErrorScreen(req.getRequestURI(), "", false));  //set non-user error for now
					//TODO error handling here
				}			
			} //end if/else check quit/preferences/surveyQ's/results
		} //end if/else check session
		
		res.setStatus(responseCode);
		res.setContentType("text/html");
		PrintWriter out= res.getWriter();
		out.println(response.toString());
				
	} //end doPost method
} //end SurveyServlet