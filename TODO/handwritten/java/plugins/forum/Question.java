package plugins.forum;

public class Question
{
	public String key;
	public String question;
	public String answer;

	public Question(String key, String question, String answer)
	{
		this.key = key;
		this.question = question;
		this.answer = answer;
	}

	public String getKey()
	{
		return key;
	}

	public String getQuestion()
	{
		return question;
	}

	public String getAnswer()
	{
		return answer;
	}
}
