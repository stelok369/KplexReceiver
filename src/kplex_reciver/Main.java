/****************************************************************************
**                                                                         **
** Copyright (C) 2016 Smoliy Artem                                         **
** Contact: strelok369@yandex.ru                                           **
**                                                                         **
** This file is part of KplexReciver.                                      **
**                                                                         **
** KplexReciver is free software: you can redistribute it and/or modify    **
** it under the terms of the GNU General Public License as published by    **
** the Free Software Foundation, either version 3 of the License, or       **
** (at your option) any later version.                                     **
**                                                                         **
** KplexReciver is distributed in the hope that it will be useful,         **
** but WITHOUT ANY WARRANTY; without even the implied warranty of          **
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the            **
** GNU General Public License for more details.                            **
**                                                                         **
** You should have received a copy of the GNU General Public License       **
** along with KplexReciver. If not, see <http://www.gnu.org/licenses/>.    **
**                                                                         **
*****************************************************************************/

package kplex_reciver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import kplex_reciver.KplexReciver.KplexMessageListener;
import kplex_reciver.KplexReciver.KplexStatus;
import kplex_reciver.KplexReciver.KplexStatusListener;
import reactive_server.http_server.HTTPReactiveServer;
import reactive_server.http_server.Utils;
import reactive_server.http_server.content.ContentProvider;
import reactive_server.http_server.http.ContentType;

public class Main
{
	private static class NmeaReciverTest implements KplexMessageListener
	{
		@Override
		public void onMessage(NmeaMessage msg)
		{
			System.out.println("Nmea: "+msg.toString());
		}
	}
	private static class NmeaStatusTest implements KplexStatusListener
	{
		@Override
		public void onStatus(KplexStatus status)
		{
			System.out.println("KplexStatus: "+status.toString());
		}		
	}
	
	public static void main(String[] args) throws IOException
	{
		Utils.setMainClass(Main.class);
		//HTTPReactiveServer server = new HTTPReactiveServer("0.0.0.0", 8080, 1);
		HTTPReactiveServer server = new HTTPReactiveServer("0.0.0.0", 80, 1);
		server.setContentProvider(new ContentProvider());
						
		server.getContentProvider().addContentFromResource("/",           "web_content/index.html", ContentType.CT_TXT_HTML);
		server.getContentProvider().addContentFromResource("/index.css",  "web_content/index.css",  ContentType.CT_TXT_CSS);
		server.getContentProvider().addContentFromResource("/jScroll.js", "web_content/jScroll.js", ContentType.CT_APP_JS);
		server.getContentProvider().addContentFromResource("/dataReciver.js", "web_content/dataReciver.js", ContentType.CT_APP_JS);
		
		NmeaDataWebSocket nmeaData = new NmeaDataWebSocket();
		StatusWebSocket nmeaStatus = new StatusWebSocket();
		server.getContentProvider().addContent("/data", nmeaData);
		server.getContentProvider().addContent("/status", nmeaStatus);
		
		NmeaStorage storage = new NmeaStorage();
		server.getContentProvider().addContent("/last",	new LastMessagesProcessor(storage));
		
		KplexReciver reciver = new KplexReciver();
		reciver.registerMessageListener(nmeaData);
		reciver.registerMessageListener(storage);
		reciver.registerMessageListener(new NmeaReciverTest());
		reciver.registerStatusListener(nmeaStatus);
		reciver.registerStatusListener(new NmeaStatusTest());
		
		TimeSetterProcessor timeSetter = new TimeSetterProcessor(reciver);
		server.getContentProvider().addContent("/settime", timeSetter);
		
		server.start();
		dispatch();
	}
	
	public static void dispatch() throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line = "";
		while((line = reader.readLine()) != null)
		{
			if(line.equals("e") || line.equals("exit"))
				break;
		}
	}
}
