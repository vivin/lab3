package edu.asupoly.cst425.lab3.domain;

/**
 * Created with IntelliJ IDEA.
 * User: vivin
 * Date: 10/20/12
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public final class User implements Comparable<User> {
    private String firstName;
    private String lastName;
    private int matchingAnswers;

    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.matchingAnswers = 0;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public int getMatchingAnswers() {
        return this.matchingAnswers;
    }

    public void setMatchingAnswers(int matchingAnswers) {
        this.matchingAnswers = matchingAnswers;
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

        User other = (User) obj;

        if(firstName == null) {
            if(other.firstName != null) {
                return false;
            }
        } else if(!firstName.equals(other.firstName)) {
            return false;
        }

        if(lastName == null) {
            if(other.lastName != null) {
                return false;
            }
        } else if(!lastName.equals(other.lastName)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());

        return result;
    }

    @Override
    public int compareTo(User other) {
        return this.matchingAnswers - other.matchingAnswers;
    }
}
