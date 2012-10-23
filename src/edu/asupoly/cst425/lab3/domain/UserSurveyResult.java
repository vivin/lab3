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

    private final User user;
    private final String id;
    private int pageNmbr;
    private Map<SurveyItem, Integer> answers;
        

    public UserSurveyResult(String id, User user, int beginPageNmbr) 
    {
    	this.id = id;
        this.user = user;
        this.setPageNmbr(beginPageNmbr);
        answers = new ConcurrentHashMap<SurveyItem, Integer>(new LinkedHashMap<SurveyItem, Integer>());
    }
    
    public String getId() {
		return id;
	}

    public User getUser() {
        return user;
    }	

	public synchronized int getPageNmbr() {
		return pageNmbr;
	}

	public synchronized void setPageNmbr(int pageNmbr) {
		this.pageNmbr = pageNmbr;
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

    public void clear() {
         answers = new ConcurrentHashMap<SurveyItem, Integer>(new LinkedHashMap<SurveyItem, Integer>());
    }

}
