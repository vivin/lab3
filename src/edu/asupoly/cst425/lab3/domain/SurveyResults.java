package edu.asupoly.cst425.lab3.domain;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

public final class SurveyResults {

    private transient Map<User, UserSurveyResult> userSurveyResults;
    private volatile Map<User, UserSurveyResult> completedUserSurveyResults;
    private Lock fileLock;
    private Survey survey;

    public SurveyResults(Survey survey) {
        this.survey = survey;
        userSurveyResults = new ConcurrentHashMap<User, UserSurveyResult>();
        completedUserSurveyResults = new ConcurrentHashMap<User, UserSurveyResult>();
    }

    public void recordUserSurveyResult(UserSurveyResult userSurveyResult) {
        userSurveyResults.put(userSurveyResult.getUser(), userSurveyResult);
    }

    public UserSurveyResult getUserSurveyResultForUser(User user) {
        return userSurveyResults.get(user);
    }

    public void removeUserSurveyResultForUser(User user) {
        userSurveyResults.remove(user);
    }

    public void persistUserSurveyResultForUser(User user) {
        completedUserSurveyResults.put(user, userSurveyResults.get(user));
    }

    public void save(String filename) throws IOException {
        fileLock.lock();

        File file = new File(filename);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

        synchronized (completedUserSurveyResults) {
            objectOutputStream.writeObject(completedUserSurveyResults);
        }

        objectOutputStream.close();

        fileLock.unlock();
    }

    public void restore(String filename) throws IOException, ClassNotFoundException {
        fileLock.lock();

        File file = new File(filename);
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        userSurveyResults = new ConcurrentHashMap<User, UserSurveyResult>();

        synchronized (completedUserSurveyResults) {
            completedUserSurveyResults = (ConcurrentHashMap<User, UserSurveyResult>) objectInputStream.readObject();
        }

        objectInputStream.close();

        fileLock.unlock();
    }

    public Map<User, UserSurveyResult> getCompletedUserSurveyResults() {
        return Collections.unmodifiableMap(completedUserSurveyResults);
    }

    public Set<User> score(User user) {

        Set<User> scoredUsers = new TreeSet<User>();

        List<SurveyItem> surveyItems = new ArrayList<SurveyItem>();
        for(int i = 0; i < survey.getNumPages(); i++) {
            surveyItems.add(survey.getSurveyItem(i));
        }

        synchronized (completedUserSurveyResults) {
            Set<User> users = completedUserSurveyResults.keySet();

            for(User userToScore : users) {

                if(userToScore != user) {
                    User scoredUser = new User(userToScore.getFirstName(), userToScore.getLastName());
                    int matchingAnswers = 0;

                    for(SurveyItem surveyItem : surveyItems) {
                        UserSurveyResult mainUserSurveyResult = userSurveyResults.get(user);
                        UserSurveyResult userToScoreUserSurveyResult = userSurveyResults.get(userToScore);

                        if(mainUserSurveyResult.getAnswerForSurveyItem(surveyItem) == userToScoreUserSurveyResult.getAnswerForSurveyItem(surveyItem)) {
                            matchingAnswers++;
                        }
                    }

                    scoredUser.setMatchingAnswers(matchingAnswers);
                    scoredUsers.add(scoredUser);
                }
            }
        }

        return scoredUsers;
    }
}
