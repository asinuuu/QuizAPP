package alan.alan.quizapp;

public class Question {

    private String text;
    private boolean answer;
    private String hint;

    public Question(String text, boolean answer, String hint) {
        this.text = text;
        this.answer = answer;
        this.hint = hint;
    }

    public String getText() {

        return text;
    }

    public boolean getAnswer() {

        return answer;
    }

    public String getHint() {

        return hint;
    }
}
