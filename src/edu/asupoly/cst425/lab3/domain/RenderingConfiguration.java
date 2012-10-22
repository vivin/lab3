package edu.asupoly.cst425.lab3.domain;

public class RenderingConfiguration {

    private User user;
    private SurveyResults surveyResults;
    private boolean verticalDisplay = true;
    private String formURL;
    private int currentQuestion = 0;

    public static class RenderingConfigurationBuilder {

        private User user;
        private SurveyResults surveyResults;
        private boolean verticalDisplay;
        private String formURL;
        private int currentQuestion;

        public RenderingConfigurationBuilder(String formURL) {
            this.formURL = formURL;
        }

        public RenderingConfigurationBuilder user(User user) {
            this.user = user;
            return this;
        }

        public RenderingConfigurationBuilder surveyResults(SurveyResults surveyResults) {
            this.surveyResults = surveyResults;
            return this;
        }

        public RenderingConfigurationBuilder verticalDisplay(boolean verticalDisplay) {
            this.verticalDisplay = verticalDisplay;
            return this;
        }

        public RenderingConfigurationBuilder currentQuestion(int currentQuestion) {
            this.currentQuestion = currentQuestion;
            return this;
        }

        public RenderingConfiguration build() {
            return new RenderingConfiguration(this);
        }
    }

    private RenderingConfiguration(RenderingConfigurationBuilder renderingConfigurationBuilder) {
        this.user = renderingConfigurationBuilder.user;
        this.surveyResults = renderingConfigurationBuilder.surveyResults;
        this.verticalDisplay = renderingConfigurationBuilder.verticalDisplay;
        this.formURL = renderingConfigurationBuilder.formURL;
        this.currentQuestion = renderingConfigurationBuilder.currentQuestion;
    }

    public User getUser() {
        return user;
    }

    public SurveyResults getSurveyResults() {
        return surveyResults;
    }

    public boolean isVerticalDisplay() {
        return verticalDisplay;
    }

    public String getFormURL() {
        return formURL;
    }

    public int getCurrentQuestion() {
        return currentQuestion;
    }
}
