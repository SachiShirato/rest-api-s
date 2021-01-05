package com.example.demo;
import org.xml.sax.Attributes;
import org.xml.sax.HandlerBase;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
 
public class samplexml extends DefaultHandler{
	
	
//	String path = "/ts3.xml";
//	InputStream is = getClass().getResourceAsStream(path);
	
	
	
	
    boolean startItem = false;
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {//[1]
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();//[2]
        SAXParser saxParser = saxParserFactory.newSAXParser();//[3]
        saxParser.parse(new File("src/test/resources/ts3.xml"), new HandlerBase());//[4]
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {//[20]
        System.out.println("[21] 要素開始 = " + qName);//[21]
        if (qName.equals("item"))//[22]
            startItem = true;//[23]
    }
    public void characters(char[] ch, int offset, int length) {//[30]
        if (startItem)//[31]
            System.out.println("[32] テキストデータ = " + new String(ch, offset, length));
    }
    public void endElement(String uri, String localName, String qName) {//[40]
        System.out.println("[41] 要素終了 = " + qName);
        startItem = false;//[42]
    }
 
}