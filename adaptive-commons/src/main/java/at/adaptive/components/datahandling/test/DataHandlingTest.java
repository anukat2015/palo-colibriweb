package at.adaptive.components.datahandling.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DataHandlingTest
{
	protected static List<TestBean> createExportList(int size)
	{
		List<TestBean> exportList = new ArrayList<TestBean>(size);
		Random random = new Random();
		for(int i = 0; i < size; i++)
		{
			TestBean testBean = new TestBean();
			String string = Long.toString(Math.abs(random.nextLong()), 36);
			testBean.setString(string);
			testBean.setDate(new Date());
			testBean.setInteger((int)(Math.random() * 1000));
			exportList.add(testBean);
		}
		return exportList;
	}
}
