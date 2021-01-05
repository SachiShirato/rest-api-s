package com.example.test;

import static com.example.mq.QUEUE.getList;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.example.mq.QMFH01Test;

public class XmlTestMain implements QMFH01Test, XMLCENTERTest {

	@BeforeEach
	void setUpAll() throws Exception {
		mqtoEmpty(getList());
	}

	@Test
	protected void test() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {

		String path = "/ts3.xml";
		String str1 = pathToString(path);
		System.out.println(str1);

		Document document = changeStringToDocument(str1);
		System.out.println(getXmlEvaluate(xmlGlbPath("TIMESTAMP","TS[2]","@SVR"), document));


	}
}