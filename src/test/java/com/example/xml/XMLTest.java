package com.example.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

import com.example.test.MqXmlTestMainSotu;

public interface XMLTest {

	default Document changeStringToDocument(String xmlString)
			throws ParserConfigurationException, SAXException, IOException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		builder = factory.newDocumentBuilder();

		Document document = builder.parse(new InputSource(new StringReader(xmlString)));
		document.setXmlStandalone(true);

		return document;

	}

	default String changeDocumentToString(Document document) throws TransformerException {

		StreamResult result = new StreamResult(new StringWriter());
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), result);
		return result.getWriter().toString();
	}

	default String pathToString(String path) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)))) {

			StringBuilder sb = new StringBuilder();
			String line;

			sb.append(br.readLine());

			while ((line = br.readLine()) != null) {
				sb.append(System.lineSeparator() + line);
			}
			return sb.toString();
		}
	}

	default String getXmlEvaluate(String path, Document document) throws XPathExpressionException {

		return XPathFactory.newInstance().newXPath().evaluate(path, document);
	}

	default <T> boolean check(T putMQmassage, T getMQmassage, List<String> list) {

		Diff diff = DiffBuilder.compare(getMQmassage).withTest(getMQmassage)
				.withNodeFilter(node -> !list.contains(node.getNodeName())).build();

//	    Diff diff = DiffBuilder.compare(getMQmassage).withTest(getMQmassage)
//	    	      .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
//	    	      .checkForSimilar().build();

//		return diff.hasDifferences();
		java.util.Iterator<Difference> iter = diff.getDifferences().iterator();
		int size = 0;
		while (iter.hasNext()) {
			System.out.println(iter.next().toString());
			size++;
		}
		return (size == 0);
	}

	default String createMQMAssageBody() throws IOException {
		String path = MqXmlTestMainSotu.path;
		return pathToString(path);
	}

	default String createBreakeServiceid(String body, String serviceid) throws Exception {
		return body.replace("DF200", serviceid);
	}

	default String createBreakeRc(String body, String rc) throws Exception {
		return body.replace("<RC>R</RC>", "<RC>" + rc + "</RC>");
	}

	default String createBreakeRequestid(String body, String requestid) throws Exception {
		return body.replace("<REQUESTID>R</REQUESTID>", "<REQUESTID>" + requestid + "</REQUESTID>");
	}

	default String createBreakeEndtag(String body, String endtag) throws Exception {
		return body.replace("</" + endtag + ">", "<" + endtag + ">");
	}

	default String createBreakeBody(String body) throws Exception {
		return body.replace("CENTER xmlns=\"http://www.acom.co.jp/ACOM\"",
				"CENTER xmlns=\"http://www.acom.co.jp/ACOMMM\"");
	}
}
