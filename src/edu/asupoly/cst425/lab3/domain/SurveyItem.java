/**
 * 
 */
package edu.asupoly.cst425.lab3.domain;


import java.util.Arrays;

/**
 * @author kgary
 *
 */
public final class SurveyItem {
	private String question;
	private String[] choices;
	
	public SurveyItem(String q, String[] cs) {
		question = q;
		choices = cs;
	}

	public String getQuestion() {
		return question;
	}

	public String[] getChoices() {
		return choices;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\nQuestion: " + getQuestion());
		for (int i = 0; choices != null && i < choices.length; i++) {
			sb.append("\n\tChoice: " + choices[i]);
		}
		return sb.toString();
	}

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(obj == null) {
            return false;
        }

        if(getClass() != obj.getClass()) {
            return false;
        }

        SurveyItem other = (SurveyItem) obj;

        if(question == null) {
            if(other.question != null) {
                return false;
            }
        } else if(!question.equals(other.question)) {
            return false;
        }

        if(choices == null) {
            if(other.choices != null) {
                return false;
            }
        } else if(!Arrays.equals(choices, other.choices)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((question == null) ? 0 : question.hashCode());
        result = prime * result + ((choices == null) ? 0 : Arrays.hashCode(choices));

        return result;
    }

}
