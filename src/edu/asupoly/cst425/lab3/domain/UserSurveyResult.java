package edu.asupoly.cst425.lab3.domain;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vivin
 * Date: 10/20/12
 * Time: 2:44 PM
 */
public final class UserSurveyResult implements Serializable {

    private final User user;
    private transient int startPageNumber;
    private Map<SurveyItem, Integer> answers;
        
    public UserSurveyResult(User user, int startPageNumber) {
        this.user = user;
        this.startPageNumber = startPageNumber;
        answers = new ConcurrentHashMap<SurveyItem, Integer>(new LinkedHashMap<SurveyItem, Integer>());
    }
    
    public User getUser() {
        return user;
    }	

	public int getStartPageNumber() {
		return startPageNumber;
	}

	public void setStartPageNumber(int startPageNumber) {
		this.startPageNumber = startPageNumber;
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
        int answer = -1;

        if(answers.containsKey(surveyItem)) {
            answer = answers.get(surveyItem);
        }

        return answer;
    }
}
