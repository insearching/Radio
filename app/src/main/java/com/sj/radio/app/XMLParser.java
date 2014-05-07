package com.sj.radio.app;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

public class XMLParser {

    XmlPullParser parser;
    public XMLParser(String xml) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        parser = factory.newPullParser();
        parser.setInput(new StringReader(xml));
    }

    public String getValue(String key){
        return parser.getAttributeValue(null, key);
    }
}
