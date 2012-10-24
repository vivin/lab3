package edu.asupoly.cst425.lab3.domain;

import java.io.Serializable;

public final class User implements Comparable<User>, Serializable {
    private String name;
    private int matchingAnswers;

    public User(String name) {
        this.name = name;
        this.matchingAnswers = 0;
    }

    public String getName() {
        return this.name;
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

        if(name == null) {
            if(other.name != null) {
                return false;
            }
        } else if(!name.equals(other.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());

        return result;
    }

    @Override
    public int compareTo(User other) {
        return other.matchingAnswers - this.matchingAnswers;
    }
}
