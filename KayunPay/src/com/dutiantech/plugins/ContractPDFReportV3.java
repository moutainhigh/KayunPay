package com.dutiantech.plugins;

import java.io.IOException;
import javax.servlet.ServletOutputStream;

import com.dutiantech.model.Contracts;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.tool.xml.ElementList;



/**
 * 电子合同 pdf 文件输出类
 * 
 * @author stonexk
 *
 */
public class ContractPDFReportV3 extends PDFReport {

	public ContractPDFReportV3(ServletOutputStream os) {
		super(os);
	}

	/**
	 * 输出电子合同
	 * 
	 * @throws DocumentException
	 */
	public void generatePDF() throws DocumentException {
		
		 
		Contracts contracts = (Contracts) contractMap.get("contracts");

		String content = contracts.getStr("content");
		
		Paragraph context = new Paragraph();
		
		try {
			ElementList elementList = MyXMLWorkerHelper.parseToElementList(content, null);
			 for (Element element : elementList) {  
		            context.add(element);  
		        }  
		        context.setSpacingBefore(10f);  
		        document.add(context);  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		//document.add(createParagraph(content, textfontBig, Paragraph.ALIGN_LEFT));

		document.close();
		
	}
	

}
