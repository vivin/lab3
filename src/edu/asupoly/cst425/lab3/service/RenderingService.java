package edu.asupoly.cst425.lab3.service;

import edu.asupoly.cst425.lab3.domain.*;

import java.util.Set;

public class RenderingService {

    private static final String HEADER = "<html>\n\t<head>\n\t\t<title>e425Match.com: Find your next CST425 lab partner online!</title>\n\t</head>\n\t<body>\n";
    private static final String FOOTER = "\n\t</body>\n</html>";

    public static String renderLogin(RenderingConfiguration renderingConfiguration) {
        StringBuilder stringBuilder = new StringBuilder(HEADER);

        stringBuilder.append("\t<form name=\"loginForm\" method=\"POST\" action=\"").append(renderingConfiguration.getFormURL()).append("\">\n");
        stringBuilder.append("\t\tPlease enter your name: <input type=\"text\" name=\"username\" /><br />");
        stringBuilder.append("\t\t<input type=\"submit\" value=\"Submit\" />");
        stringBuilder.append("\t</form>");

        stringBuilder.append(FOOTER);

        return stringBuilder.toString();
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

            stringBuilder.append("\t\t<input type=\"radio\" value=\"").append(i).append("\" ").append(checked).append(" />").append(choice);

            if(renderingConfiguration.isVerticalDisplay()) {
                stringBuilder.append("<br />\n");
            }
        }

        String buttonText = "Next Question";
        if(currentQuestion == (survey.getNumPages() - 1)) {
            buttonText = "Finish Survey";
        }

        stringBuilder.append("\t\t<input type=\"submit\" value=\"").append(buttonText).append("\" /> ");
        stringBuilder.append("<a href=\"LINK TO QUIT\">Quit Survey</a> "); //TODO : PUT CORRECT LINK TO "QUIT" PAGE HERE
        stringBuilder.append("<a href=\"LINK TO PREFERENCES\">User Preferences</a>\n"); //TODO: PUT CORRECT LINK TO "USER PREFERENCES" PAGE HERE
        stringBuilder.append("\t</form");

        stringBuilder.append(FOOTER);

        return stringBuilder.toString();
    }

    public static String renderUserPreferencesPage(RenderingConfiguration renderingConfiguration) {
        StringBuilder stringBuilder = new StringBuilder(HEADER);

        stringBuilder.append("\tPlease select your display preference:<br /><br />\n\n");
        stringBuilder.append("\t<form name=\"Form\" method=\"POST\" action=\"").append(renderingConfiguration.getFormURL()).append("\">\n");
        stringBuilder.append("I'd like my answers to be displayed in the following format: ");
        stringBuilder.append("\t\t<select name=\"displayPreference\">\n");
        stringBuilder.append("\t\t\t<option value=\"vertical\">Vertical</option>\n");
        stringBuilder.append("\t\t\t<option value=\"horizontal\">Horizontal</option>\n");
        stringBuilder.append("\t\t</select><br /><br />\n\n");

        stringBuilder.append("\t\t<input type=\"submit\" value=\"Submit\" />");
        stringBuilder.append("\t</form>");

        stringBuilder.append(FOOTER);

        return stringBuilder.toString();
    }

    public static String renderDebriefingScreen(RenderingConfiguration renderingConfiguration) {
        User user = renderingConfiguration.getUser();
        SurveyResults surveyResults = renderingConfiguration.getSurveyResults();

        Set<User> rankedUsers = surveyResults.score(user);

        StringBuilder stringBuilder = new StringBuilder(HEADER);
        stringBuilder.append("Here is a list of partners ranked in descending order by how best they match you:<br /><br />");
        stringBuilder.append("\t<table>\n\t\t<tr>\n\t\t\t<td>User</td><td>Score</td>\n\t\t</tr>\n");
        for(User rankedUser : rankedUsers) {
            stringBuilder.append("\t\t<tr>\n");
            stringBuilder.append("\t\t\t<td>").append(user.getName()).append("</td><td>").append(user.getMatchingAnswers()).append("</td>\n");
            stringBuilder.append("\t\t</tr>\n");
        }
        stringBuilder.append("\t</table>");

        stringBuilder.append(FOOTER);

        return stringBuilder.toString();
    }
}
