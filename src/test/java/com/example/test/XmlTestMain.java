package com.example.test;

import static jp.co.acom.fehub.mq.QUEUE.getList;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import jp.co.acom.fehub.mq.QMFH01Test;
import jp.co.acom.fehub.xml.XMLCENTERTest;

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
		System.out.println(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[2]", "@SVR"), document));

	}
}