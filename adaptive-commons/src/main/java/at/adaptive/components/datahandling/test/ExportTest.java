package at.adaptive.components.datahandling.test;

import java.io.FileOutputStream;
import java.util.List;

import at.adaptive.components.datahandling.common.ColumnDefinition;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.dataexport.xls.XLSExport;

public class ExportTest extends DataHandlingTest
{
	public static void main(String[] args)
	{
		try
		{
			ExportTest exportTest = new ExportTest();

			for(int i = 0; i < 3; i++)
			{
				exportTest.test();
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void test() throws Exception
	{
		List<TestBean> exportList = createExportList(5000);
		// InputStream templateFile = new FileInputStream("c:\\CommissionAgreement.xls");
		ColumnDefinitionContainer columnDefinitionContainer = new ColumnDefinitionContainer(TestBean.class, true);
		// columnDefinitionContainer.addColumnDefinition(new ColumnDefinition("Text", "string"));
		columnDefinitionContainer.addColumnDefinition(new ColumnDefinition("Name", "string"));
		columnDefinitionContainer.addColumnDefinition(new ColumnDefinition("Integer", "integer"));
		columnDefinitionContainer.addColumnDefinition(new ColumnDefinition("Date", "date"));

		long l1 = System.currentTimeMillis();
		XLSExport<TestBean> export = new XLSExport<TestBean>(columnDefinitionContainer);
		export.export(exportList, new FileOutputStream("c:\\test_out.xls"));
		export.close();
		long l2 = System.currentTimeMillis();
		System.err.println("Execution time: " + (l2 - l1) + " ms.");
		// FileOutputStream out = new FileOutputStream("c:\\test_out.xlsx");
		// XLSXExport<TestBean> export = new XLSXExport<TestBean>(columnDefinitionContainer);
		// export.export(exportList, out);

		// VCardExport<TestBean> export = new VCardExport<TestBean>(columnDefinitionContainer);
		// export.export(exportList, new FileOutputStream("c:\\test_out.vcf"));

		// TemplateDefinition templateDefinition = new TemplateDefinition(columnDefinitionContainer);
		// templateDefinition.setHeaderLine(27);
		// templateDefinition.setStartLine(28);
		// // templateDefinition.setSecondLine(true);
		// templateDefinition.setSumLine(29);

		// List<TestBean> exportList = createExportList(10);
		// InputStream templateFile = new FileInputStream("c:\\CommissionAgreement.xls");
		// ColumnDefinitionContainer columnDefinitionContainer = new ColumnDefinitionContainer(TestBean.class, true);
		// columnDefinitionContainer.addColumnDefinition(new ColumnDefinition("Monat", "string"));
		// columnDefinitionContainer.addColumnDefinition(new ColumnDefinition("Umsatzziel", "integer"));
		// // columnDefinitionContainer.addColumnDefinition(new ColumnDefinition("Datum", "date"));
		// TemplateDefinition templateDefinition = new TemplateDefinition(columnDefinitionContainer);
		// templateDefinition.setHeaderLine(24);
		// templateDefinition.setStartLine(25);
		// templateDefinition.setSumLine(26);

		// XLSTemplateExport<TestBean> export = new XLSTemplateExport<TestBean>(templateFile, templateDefinition);

		// export.export(exportList, new FileOutputStream("c:\\test_out.xls"));
	}
}
