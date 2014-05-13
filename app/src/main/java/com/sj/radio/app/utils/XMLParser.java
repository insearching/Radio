package com.sj.radio.app.utils;

import com.sj.radio.app.entity.AuthResponse;
import com.sj.radio.app.entity.Radio;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class XMLParser {

    XmlPullParser parser;

    public XMLParser(String xml) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        parser = factory.newPullParser();
        parser.setInput(new StringReader(xml));
    }

    public AuthResponse getAuthResponse() {
        AuthResponse response = new AuthResponse();
        String currentTag = null;
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        response = new AuthResponse();
                        break;
                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();
                        if (currentTag.equalsIgnoreCase(KeyMap.CODE)) {
                            response.setCode(Integer.parseInt(parser.nextText()));
                        }
                        if (currentTag.equalsIgnoreCase(KeyMap.TOKEN)) {
                            response.setToken(parser.nextText());
                        }
                        if (currentTag.equalsIgnoreCase(KeyMap.MESSAGE)) {
                            response.setMessage(parser.nextText());
                        }
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(response.getCode() == -1)
            response = null;
        return response;
    }

    public ArrayList<Radio> getRadioList() {
        ArrayList<Radio> radios = new ArrayList<Radio>();
        String currentTag = null;
        Radio currentRadio = null;
        boolean done = false;
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();
                        if (currentTag.equalsIgnoreCase(KeyMap.RADIO)) {
                            currentRadio = new Radio();
                        }
                        else if(currentRadio != null){
                            if (currentTag.equalsIgnoreCase(KeyMap.ID)) {
                                currentRadio.setId(Integer.parseInt(parser.nextText()));
                            }
                            if (currentTag.equalsIgnoreCase(KeyMap.NAME)) {
                                currentRadio.setName(parser.nextText());
                            }
                            if (currentTag.equalsIgnoreCase(KeyMap.COUNTRY)) {
                                currentRadio.setCountry(parser.nextText());
                            }
                            if (currentTag.equalsIgnoreCase(KeyMap.URL)) {
                                currentRadio.setUrl(parser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        currentTag = parser.getName();
                        if (currentTag.equalsIgnoreCase(KeyMap.RADIO) && currentRadio != null) {
                            radios.add(currentRadio);
                        } else if (currentTag.equalsIgnoreCase(KeyMap.RADIOS)) {
                            done = true;
                        }
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return radios;
    }

}