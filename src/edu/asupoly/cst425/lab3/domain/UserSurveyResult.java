package edu.asupoly.cst425.lab3.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vivin
 * Date: 10/20/12
 * Time: 2:44 PM
 * To change this template use File | Settings | File Templates.
 */
public final class UserSurveyResult {

    private User user;
    private Map<SurveyItem, Integer> answers;

    public UserSurveyResult(User user) {
        this.user = user;
        answers = new ConcurrentHashMap<SurveyItem, Integer>(new LinkedHashMap<SurveyItem, Integer>());
    }

    public User getUser() {
        return user;
    }

    public void setAnswerForSurveyItem(SurveyItem surveyItem, int answer) {
        if(answer < 0) {
            throw new IllegalArgumentException("Answer has to be a positive integer");
        }

        if(answer > surveyItem.getChoices().length) {
            throw new IllegalArgumentException("Supplied answer was " + answer + ", but there are only " + surveyItem.getChoices().length + " choices for the supplied survey-item.");
        }

        answers.put(surveyItem, answer);
    }

    public int getAnswerForSurveyItem(SurveyItem surveyItem) {
        return answers.get(surveyItem);
    }

    public void clear() {
         answers = new ConcurrentHashMap<SurveyItem, Integer>(new LinkedHashMap<SurveyItem, Integer>());
    }

}
