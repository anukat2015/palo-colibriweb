package com.proclos.opensmc.survey;

import java.util.List;

import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

import com.proclos.opensmc.survey.engine.SurveyEngine;

public class SurveyTest extends SeamTest
{
	@Test
	public void testConfigurations()
	{
		try
		{
			ISurveyEngine engine = new SurveyEngine("statusPresens", System.getProperty("user.dir") + "/webapp/WEB-INF/config/surveys", null);
			// ISurveyEngine engine = new SurveyEngine("anamnesePsychosozial",System.getProperty("user.dir")+"/webapp/WEB-INF/config/surveys");
			// ISurveyEngine engine = new SurveyEngine("statusSomatisch",System.getProperty("user.dir")+"/webapp/WEB-INF/config/surveys");
			IContainer container = engine.getNextContainer();
			while(container != null)
			{
				List<IQuestion> questions = container.getQuestions();
				System.out.println("Questions for container " + container.getId() + ": " + container.getText() + " " + container.getLayout().toString());
				for(IQuestion q : questions)
				{
					System.out.println("Question " + q.getId() + " (" + q.getType().toString() + "," + q.isEnabled() + "," + q.getLayout().toString() + ":");
					System.out.println("  " + q.getText());
					for(IResponse r : q.getItems())
					{
						System.out.println("    Response " + r.getId() + ": " + r.getValue() + " " + r.getLabel() + " " + r.isEnabled());
					}
				}
				container = engine.getNextContainer();
			}

		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			assert false;
		}
	}
}
