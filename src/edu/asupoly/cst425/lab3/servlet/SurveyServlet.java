/*CST425Lab3.Vivin Paliath & Paul Spaude, Oct 2012*/
package edu.asupoly.cst425.lab3.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.logging.Logger;
import edu.asupoly.cst425.lab3.domain.ConversationState;
import edu.asupoly.cst425.lab3.domain.RenderingConfiguration;
import edu.asupoly.cst425.lab3.domain.Survey;
import edu.asupoly.cst425.lab3.domain.SurveyResults;
import edu.asupoly.cst425.lab3.domain.User;
import edu.asupoly.cst425.lab3.domain.UserSurveyResult;
import edu.asupoly.cst425.lab3.service.RenderingService;


import java.util.Enumeration;



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
				System.out.println("NULL AND EMPTY");
				if (username == null && req.getParameter("start").equals("start"))
				{
					response.append(RenderingService.renderLogin(rc));			//display login 
					System.out.println("START");
					if (req.getParameter("submit").equals("Start Now!"))
					{
						System.out.println("\n***START NOW SELECTED***\n");
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
				int setStartPageNmbr = -1; //default to returning user choice page				
				
				rc = new RenderingConfiguration.RenderingConfigurationBuilder(req.getRequestURI()).user(newUser).build();
				
				if (userResults == null)
				{
					System.out.println("NEW USER");
					userResults = new UserSurveyResult(newUser);
					results.addUserSurveyResult(userResults);
					setStartPageNmbr = 0;	//start at beginning of the survey									
					response.append(RenderingService.renderUserHome(rc, true)); 	//display HTML for home screen as new user
				}		
				else
				{
					System.out.println("RETURNING USER");
					//display HTML for home screen as is a returning user					
					response.append(RenderingService.renderUserHome(rc, false));					
				}
								
				ConversationState newState = new ConversationState(newSession.getId(), newUser, setStartPageNmbr); //setup object to store info about session
				newSession.setAttribute(newSession.getId(), newState); //store the session object					
			}
		}
		else
		{
			//TODO Move these to consts up top
			final String PREFS = "User Preferences";					//these are "cmds" that are used in SurveySurvlet and also the user sees these as buttons
			final String QUIT = "Cancel Answers and Logout";			
			final String NEXT = "Next Question";
			final String FINISH = "Finish Survey";
			final String QUITPREFS = "Save Preferences and Return";
		  
			final String SURVEY = "Enter Survey";
			final String MATCHES = "See your matches";
			final String HOMERETN = "Return to UserHome";
			final String LOGINRETN = "Return to LogIn";
			
			
			
			System.out.println("CONTINUING CONVERSATION USER");
			ConversationState stateCont = (ConversationState) sessionCont.getAttribute(sessionCont.getId()); //TODO Check for cookie here for PREFERENCES
			
						
			
			
			String submit = req.getParameter("submit");
			
			if (submit == null || stateCont == null )  //TODO take submit off of here when ready 
			{
				response.append(RenderingService.renderErrorScreen(req.getRequestURI(), "Submit or stateCont was null", false));
				submit = "Cancel Answers and Go to Login";
			}						
			
			
			if (submit.equals(QUIT) || submit.equals(LOGINRETN))
			{
				System.out.println("QUIT SELECTED\n");				
				if (req.isRequestedSessionIdValid()) sessionCont.invalidate();	//erase conversational state
				response.append(RenderingService.renderLogin(rc));    //go to login (user logged out)
			}
			else if (submit.equals(PREFS))
			{
				System.out.println("USER PREFERENCES SELECTED");
				response.append(RenderingService.renderUserPreferencesPage(rc)); //go to preferences
			}
			else if (submit.equals(MATCHES) || submit.equals(FINISH))
			{
				System.out.println("DEBRIEF SELECTED");
				System.out.println("StatePages= " +stateCont.getPageNmbr() +" | SurveyPageNumber= " +_survey.getNumPages() +". \n");
				if (stateCont.getPageNmbr() == _survey.getNumPages() && submit.equals(FINISH)) 
				{		//TODO persist user results
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
				System.out.println("NEXT/SURVEY/QUITPREFS SELECTED");
				
				
				
				if (submit.equals(NEXT) && stateCont.getPageNmbr() < _survey.getNumPages() )
				{   System.out.println("NEXT PAGE");
					//store result 
//					Enumeration en = req.getParameterNames();
//					System.out.print("\n\nParams: ");
//					while (en.hasMoreElements())
//					{
//						String param = (String) en.nextElement();
//						System.out.print(param + " = " +req.getParameter(param) +" .\n\n");
//					}
					
					//render next question
					int nextPage = stateCont.getPageNmbr()+1;
					response.append(RenderingService.renderQuestion(rc = new RenderingConfiguration.
							RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(nextPage).
							user(stateCont.getUser()).verticalDisplay(true).surveyResults(results).build()));
					System.out.println("Next page number is " + nextPage);
					stateCont.setPageNmbr(nextPage);
				}
				else if (submit.equals(SURVEY) && stateCont.getPageNmbr() < 1 )
				{
					System.out.println("FIRST PAGE");
					//render first page
					int nextPage = 1;
					response.append(RenderingService.renderQuestion(rc = new RenderingConfiguration.
							RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(nextPage).
							user(stateCont.getUser()).verticalDisplay(true).surveyResults(results).build()));
					System.out.println("Next page number is " + nextPage);
					stateCont.setPageNmbr(nextPage);
				}
				else if ( submit.equals(QUITPREFS) )
				{					
					System.out.println("QUIT PREFS");
														
					int lastPage = stateCont.getPageNmbr();
					
					if (lastPage < 1)
					{
						boolean newUser = false;
						if (lastPage == 0) newUser=true;
						response.append(RenderingService.renderUserHome(rc = new RenderingConfiguration.
								RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(lastPage).
								user(stateCont.getUser()).verticalDisplay(true).surveyResults(results).build(), newUser));
					}
					else if (lastPage == _survey.getNumPages())
					{						
						response.append(RenderingService.renderDebriefingScreen(rc = new RenderingConfiguration.
								RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(lastPage).
								user(stateCont.getUser()).verticalDisplay(true).surveyResults(results).build())); //TODO see if this is needed can't rmbr if displayed or not 
					}
					else if (lastPage > 0 && lastPage <= _survey.getNumPages())
					{
						response.append(RenderingService.renderQuestion(rc = new RenderingConfiguration.
								RenderingConfigurationBuilder(req.getRequestURI()).currentQuestion(lastPage).
								user(stateCont.getUser()).verticalDisplay(true).surveyResults(results).build()));
					}
					
					System.out.println("Same page number is " + lastPage);					
				}
				else
				{
					System.out.println("ERROR HERE");
					//error handling here
				}			
			} //end if/else check quit/preferences/surveyQ's/results
		} //end if/else check session
		
		res.setStatus(responseCode);
		res.setContentType("text/html");
		PrintWriter out= res.getWriter();
		out.println(response.toString());
				
	} //end doPost method
} //end SurveyServlet