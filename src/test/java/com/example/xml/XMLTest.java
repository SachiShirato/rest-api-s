package com.example.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

public interface XMLTest {

	default Document changeStringToDocument(String xmlString) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;

			builder = factory.newDocumentBuilder();

			Document document = builder.parse(new InputSource(new StringReader(xmlString)));
			document.setXmlStandalone(true);

			return document;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	default String changeDocumentToString(Document document) {

		DOMSource source = new DOMSource(document);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
			StreamResult result = new StreamResult(new StringWriter());
			transformer.transform(source, result);
			String str1 = result.getWriter().toString();
			return str1;
		} catch (TransformerException e) {
			e.printStackTrace();
			return null;
		}

	}

	default String pathToString(String path) {

		try (InputStream is = getClass().getResourceAsStream(path);
				BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

			StringBuilder sb = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				sb.append(line + System.lineSeparator());

			}
			return sb.toString();

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	default String getXmlEvaluate(String path, Document document) {
		try {

			return XPathFactory.newInstance().newXPath().evaluate(path, document);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return null;
		}
	}



	default <T> boolean check(T putMQmassage, T getMQmassage, List<String> list) {

		// TODO
		Diff diff = DiffBuilder.compare(getMQmassage).withTest(putMQmassage)
				.withNodeFilter(node -> !list.contains(node.getNodeName())).build();

		java.util.Iterator<Difference> iter = diff.getDifferences().iterator();
		int size = 0;
		while (iter.hasNext()) {
			System.out.println(iter.next().toString());
			size++;
		}

		// TODO
		return (size == 0);

	}

}
