package quizClasses;

import java.util.ArrayList;
import java.util.Random;

public class Questions {
	
	private String[] answers = new String[4];
	private String question;
	private String correctAnswer;
	
	public String getAnswer(int i) {
		return answers[i];
	}
	public String getQuestion() {
		return question;
	}
	public String getCorrectAnswer() {
		return correctAnswer;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Questions) || obj == null) return false;
		if(((Questions)obj).getQuestion().equals(this.question)) return true;
		else return false;
	}
	
	public Questions(String[] answers, String question, String correctAnswer) {
		this.answers = answers;
		shuffleAnswers();
		this.question = question;
		this.correctAnswer = correctAnswer;
	}
	
	private void shuffleAnswers() {
		ArrayList<String> answersCopy = new ArrayList<String>();
		for(int i = 0; i < answers.length; i++) {
			answersCopy.add(answers[i]);
		}
		for(int i = 0; i < 4; i++) {
			answers[i] = answersCopy.get(new Random().nextInt(answersCopy.size()));
			answersCopy.remove(answers[i]);
		}
	}
}
