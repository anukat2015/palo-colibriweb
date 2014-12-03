package at.adaptive.components.datahandling.test;

import java.io.FileInputStream;
import java.util.List;

import at.adaptive.components.datahandling.common.ColumnDefinition;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.dataimport.xls.XLSImport;

public class ImportTest extends DataHandlingTest
{
	public static void main(String[] args)
	{
		try
		{
			// List<TestBean> exportList = createExportList(12);
			// InputStream templateFile = new FileInputStream("c:\\CommissionAgreement.xls");
			ColumnDefinitionContainer columnDefinitionContainer = new ColumnDefinitionContainer(TestBean.class, true);
			columnDefinitionContainer.addColumnDefinition(new ColumnDefinition(0, "SID", "string"));
			// columnDefinitionContainer.addColumnDefinition(new ColumnDefinition(24, "HEROLD Salesrep", null));
			// columnDefinitionContainer.addColumnDefinition(new ColumnDefinition(25, "Besteller", null));

			FileInputStream in = new FileInputStream("C:\\Users\\Bernie\\Desktop\\Neuer Ordner (3)\\Potential N1001.xls");
			XLSImport<TestBean> dataImport = new XLSImport<TestBean>(columnDefinitionContainer);
			// dataImport.setEncoding("iso-8859-1");
			// XLSImport<Object[]> dataImport = new XLSImport<Object[]>(columnDefinitionContainer);
			List<TestBean> importList = dataImport.importData(in);
			for(TestBean object : importList)
			{
				// for(Object object : objects)
				// {
				// System.out.print(object);
				// }
				System.out.println(object.getString());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
