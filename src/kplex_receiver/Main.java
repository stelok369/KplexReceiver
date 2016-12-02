/****************************************************************************
**                                                                         **
** Copyright (C) 2016 Smoliy Artem                                         **
** Contact: strelok369@yandex.ru                                           **
**                                                                         **
** This file is part of KplexReceiver.                                      **
**                                                                         **
** KplexReceiver is free software: you can redistribute it and/or modify    **
** it under the terms of the GNU General Public License as published by    **
** the Free Software Foundation, either version 3 of the License, or       **
** (at your option) any later version.                                     **
**                                                                         **
** KplexReceiver is distributed in the hope that it will be useful,         **
** but WITHOUT ANY WARRANTY; without even the implied warranty of          **
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the            **
** GNU General Public License for more details.                            **
**                                                                         **
** You should have received a copy of the GNU General Public License       **
** along with KplexReceiver. If not, see <http://www.gnu.org/licenses/>.    **
**                                                                         **
*****************************************************************************/

package kplex_receiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kplex_receiver.KplexReceiver.KplexMessageListener;
import kplex_receiver.KplexReceiver.KplexStatus;
import kplex_receiver.KplexReceiver.KplexStatusListener;
import reactive_server.http_server.HTTPReactiveServer;
import reactive_server.http_server.Utils;
import reactive_server.http_server.content.ContentProvider;
import reactive_server.http_server.http.ContentType;

public class Main
{
	private static class NmeaReceiverTest implements KplexMessageListener
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
		server.getContentProvider().addContentFromResource("/dataReceiver.js", "web_content/dataReceiver.js", ContentType.CT_APP_JS);
		
		NmeaDataWebSocket nmeaData = new NmeaDataWebSocket();
		StatusWebSocket nmeaStatus = new StatusWebSocket();
		server.getContentProvider().addContent("/data", nmeaData);
		server.getContentProvider().addContent("/status", nmeaStatus);
		
		NmeaStorage storage = new NmeaStorage();
		server.getContentProvider().addContent("/last",	new LastMessagesProcessor(storage));
		
		KplexReceiver receiver = new KplexReceiver();
		receiver.registerMessageListener(nmeaData);
		receiver.registerMessageListener(storage);
		receiver.registerMessageListener(new NmeaReceiverTest());
		receiver.registerStatusListener(nmeaStatus);
		receiver.registerStatusListener(new NmeaStatusTest());
		
		TimeSetterProcessor timeSetter = new TimeSetterProcessor(receiver);
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
