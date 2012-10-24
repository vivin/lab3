package edu.asupoly.cst425.lab3.service;

import edu.asupoly.cst425.lab3.domain.*;
import java.util.List;
import java.util.Set;

public class RenderingService {

    private static final String HEADER = "<html>\n\t<head>\n\t\t<title>e425Match.com: Find your next CST425 lab partner online!</title>\n\t</head>\n\t<body>\n";
    private static final String FOOTER = "\n\t</body>\n</html>";
   
    private static final String PREFS = "User Preferences";					
	private static final String QUIT = "Cancel Answers and Logout";			
	private static final String NEXT = "Next Question";
	private static final String FINISH = "Finish Survey";
	private static final String QUITPREFS = "Save Preferences and Return";  
	private static final String SURVEY = "Enter Survey";
	private static final String MATCHES = "See your matches";
	private static final String LOGINRETURN = "Return to LogIn";
	private static final String COOKIENAME = "e425MatchPrefs";    
    private static final String LOGIN = "Login to e425Match";			  //not a cmd
  
    public static String renderLogin(RenderingConfiguration renderingConfiguration) 
    {
        StringBuilder stringBuilder = new StringBuilder(HEADER);

        stringBuilder.append("\t<form name=\"loginForm\" method=\"POST\" action=\"").append(renderingConfiguration.getFormURL()).append("\">\n");
        stringBuilder.append("\t\tPlease enter your name: <input type=\"text\" name=\"username\" /><br />");
        stringBuilder.append("\t\t<input type=\"submit\" value=\"" +LOGIN +"\" />");
        stringBuilder.append("\t</form>");

        stringBuilder.append(FOOTER);

        return stringBuilder.toString();
    }
    
    public static String renderUserHome(RenderingConfiguration renderingConfiguration, boolean userNew)
    {
    	StringBuilder sb = new StringBuilder(HEADER);
    	
    	if (userNew) {sb.append("\t<h2>Welcome ").append(renderingConfiguration.getUser().getName()).append("</h2>\n");}
    	else { sb.append("\t<h2>Welcome ").append(renderingConfiguration.getUser().getName()).append("</h2>\n"); }    	
    	sb.append("\t<form name=\"valuedUserForm\" method=\"POST\" action=\"").append(renderingConfiguration.getFormURL()).append("\">\n");
    	sb.append("\t\t<p>What would you like to do?</p>\n");
    	sb.append("\t\t<input type=\"hidden\" name=\"blank\" value=\"\" />\n");
    	sb.append("\t\t<input type=\"submit\" name=\"submit\" value=\"" +LOGINRETURN +"\" />\n"); //TODO Change these to be more meaningful
    	sb.append("\t\t<input type=\"submit\" name=\"submit\" value=\"" +PREFS +"\" />\n");
        sb.append("\t\t<input type=\"submit\" name=\"submit\" value=\"" +SURVEY +"\" />\n");
        if (!userNew) sb.append("\t\t<input type=\"submit\" name=\"submit\" value=\"" +MATCHES +"\" />\n");
    	sb.append("\t</form>");
    	
    	sb.append(FOOTER);

        return sb.toString();    	
    }

    public static String renderQuestion(RenderingConfiguration renderingConfiguration) {
        User user = renderingConfiguration.getUser();
        SurveyResults surveyResults = renderingConfiguration.getSurveyResults();
        int currentQuestion = renderingConfiguration.getCurrentQuestion();

        Survey survey = surveyResults.getSurvey();
        SurveyItem surveyItem = survey.getSurveyItem(currentQuestion);
        assert surveyItem != null;

        UserSurveyResult userSurveyResult = surveyResults.getUserSurveyResultForUser(user);
        int answer = userSurveyResult.getAnswerForSurveyItem(surveyItem);
        StringBuilder stringBuilder = new StringBuilder(HEADER);

        stringBuilder.append("\t<form name=\"questionForm\" method=\"POST\" action=\"").append(renderingConfiguration.getFormURL()).append("\">\n");
        stringBuilder.append(surveyItem.getQuestion()).append("<br /><br />\n\n");

        int i = 0;
        for(String choice : surveyItem.getChoices()) {

            String checked = "";
            if(answer == i) {
                checked = "checked=\"true\"";
            }

            stringBuilder.append("\t\t<input type=\"radio\" name=\"answer\" value=\"").append(i).append("\" ").append(checked).append(" />\n").append(choice);

            if(renderingConfiguration.isVerticalDisplay()) {
                stringBuilder.append("<br />\n");
            }

            i++;
        }

        String buttonText = NEXT;
        if(currentQuestion == (survey.getNumPages())) {
            buttonText = FINISH;
        }

        stringBuilder.append("\t\t<input type=\"submit\" name=\"submit\" value=\"").append(buttonText).append("\" />\n");
        stringBuilder.append("\t\t<input type=\"submit\" name=\"submit\" value=\"" +PREFS +"\" />\n");
        stringBuilder.append("\t\t<input type=\"submit\" name=\"submit\" value=\"" +QUIT +"\" />\n");
        stringBuilder.append("\t</form");

        stringBuilder.append(FOOTER);

        return stringBuilder.toString();
    }

    public static String renderUserPreferencesPage(RenderingConfiguration renderingConfiguration) {
        StringBuilder stringBuilder = new StringBuilder(HEADER);

        stringBuilder.append("\t<h3>Please select your display preference:</h3><br />\n\n");
        stringBuilder.append("\t<form name=\"UserPrefs\" method=\"POST\" action=\"").append(renderingConfiguration.getFormURL()).append("\">\n");
        stringBuilder.append("\t<p>I'd like my answers to be displayed in the following format: </p>\n");
        stringBuilder.append("\t\t<select name=\"displayPreference\">\n");
        stringBuilder.append("\t\t\t<option value=\"vertical\">Vertical</option>\n");
        stringBuilder.append("\t\t\t<option value=\"horizontal\">Horizontal</option>\n");
        stringBuilder.append("\t\t</select><br /><br />\n\n");

        stringBuilder.append("\t\t<input type=\"submit\" name=\"submit\" value=\"" +QUITPREFS +"\" /\n>");
        stringBuilder.append("\t</form>");

        stringBuilder.append(FOOTER);

        return stringBuilder.toString();
    }

    public static String renderDebriefingScreen(RenderingConfiguration renderingConfiguration) {
        User user = renderingConfiguration.getUser();
        SurveyResults surveyResults = renderingConfiguration.getSurveyResults();

        List<User> rankedUsers = surveyResults.score(user);

        StringBuilder stringBuilder = new StringBuilder(HEADER);
        stringBuilder.append("Here is a list of partners ranked in descending order by how best they match you:<br /><br />");
        stringBuilder.append("\t<table>\n\t\t<tr>\n\t\t\t<td>User</td><td>Score</td>\n\t\t</tr>\n");
        for(User rankedUser : rankedUsers) {
            stringBuilder.append("\t\t<tr>\n");
            stringBuilder.append("\t\t\t<td>").append(rankedUser.getName()).append("</td><td>").append(rankedUser.getMatchingAnswers()).append("</td>\n");
            stringBuilder.append("\t\t</tr>\n");
        }
        stringBuilder.append("\t</table>");
        
        stringBuilder.append("\t<br /><form name=\"ReturnToLoginForm\" method=\"POST\" action=\"").append(renderingConfiguration.getFormURL()).append("\"\n");
        stringBuilder.append("\t\t<input type=\"hidden\" value=\"\" />\n");
        stringBuilder.append("\t\t<p><input name=\"submit\" type=\"submit\" value=\"" +LOGINRETURN +"\" /></p>\n");
        stringBuilder.append("\t</form>"); 

        stringBuilder.append(FOOTER);

        return stringBuilder.toString();
    }
    
    public static String renderErrorScreen(String homeUrl, String specificErrorMsg, boolean userError) {
    	StringBuilder sb = new StringBuilder("<html>\n\t<head>\n\t\t<title>e425Match.com Error</title>\n\t</head>\n\t<body>\n");
    	
    	if (userError)
    	{
    		sb.append("\t<h2>Error Processing Your Request! See Below for Details.</h2>\n");
    	}
    	else
    	{
    		sb.append("\t<h2>Oops, this is embarrassing. We experienced an error!</h2>\n");
    	}
    	
    	if (specificErrorMsg == null || specificErrorMsg.equals("") )
    	{
    		sb.append("\t<p>We'll fix this shortly. If this problem persists please contact e425Match.com for assistance.</p>\n");	//generic error message
    	}
    	else
    	{
    		sb.append("\t<p>" +specificErrorMsg +"<p>\n");	//customizable error message
    	}
    	
    	sb.append("\t<br /><form name=\"ReturnToLoginForm\" method=\"POST\" action=\"").append(homeUrl).append("\"\n");
    	sb.append("\t\t<input type=\"hidden\" value=\"\" />\n");
    	sb.append("\t\t<p><input name=\"submit\" type=\"submit\" value=\"" +LOGINRETURN +"\" /></p>\n");
    	sb.append("\t</form>");    
    	
    	sb.append(FOOTER);    	
    	
    	return sb.toString();    	
    }
    
    public static String renderLoginError(RenderingConfiguration renderingConfiguration, String specificErrorMsg) 
    {
    	StringBuilder sb = new StringBuilder("<html>\n\t<head>\n\t\t<title>e425Match.com SignIn Error</title>\n\t</head>\n\t<body>\n");
    	
    	sb.append("\t<h2>Error Processing Your Request! See Below for Details.</h2>\n");    
    	sb.append("\t<p>" + specificErrorMsg +"<p>\n");	//customizable error message
    	 
    	sb.append("\t<form name=\"ReturnTologinForm\" method=\"POST\" action=\"").append(renderingConfiguration.getFormURL()).append("\">\n");  //give user chance to login right in error screen
        sb.append("\t\t<p>Enter your name: <input type=\"text\" name=\"username\" /></p>\n");
        sb.append("\t\t<p><input type=\"submit\" name=\"submit\" value=\"" +LOGIN +"\" /></p>");
        sb.append("\n\t</form>");   	
    	
    	sb.append(FOOTER);  
    	
    	return sb.toString();    	
    }    
   
}
