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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import kplex_receiver.KplexReceiver.KplexStatus;
import kplex_receiver.KplexReceiver.KplexStatusListener;
import reactive_server.http_server.websocket.WebSocketConnector;
import reactive_server.http_server.websocket.WebSocketMessage;
import reactive_server.http_server.websocket.WebSocketProcessor;

public class StatusWebSocket extends WebSocketConnector implements KplexStatusListener
{
	private ArrayList<StatusWebSocketProcessor> processors = new ArrayList<StatusWebSocket.StatusWebSocketProcessor>();
	private synchronized void removeProcessor(StatusWebSocketProcessor p){processors.remove(p);}
	private synchronized StatusWebSocketProcessor addNewProcessor()
	{
		StatusWebSocketProcessor p = new StatusWebSocketProcessor();
		processors.add(p);
		return p;
	}
	
	private final static SimpleDateFormat dateFormat;
	static
	{
		dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss z", Locale.ENGLISH);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	private KplexStatus status = new KplexStatus(false, "Wait for start...");
	private Thread timerThread;
	public StatusWebSocket()
	{
		timerThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(true)
				{
					try
					{
						long mills = System.currentTimeMillis();
						long delay = (mills/1000+1)*1000-mills;
						Thread.sleep(delay);
						String date = dateFormat.format(new Date(System.currentTimeMillis()));
						String msgText;
						synchronized (status)
						{
							msgText = date + " " + status.toString();
						}						
						WebSocketMessage msg = new WebSocketMessage(msgText);
						synchronized (processors)
						{
							for(StatusWebSocketProcessor p : processors)
								p.send(msg);
						}						
					}
					catch (InterruptedException e){}
				}
			}
		});
		timerThread.start();
	}
	
	public class StatusWebSocketProcessor extends WebSocketProcessor
	{
		@Override
		public void onMessage(WebSocketMessage msg){}
		
		@Override
		public void onClose(WebSocketMessage msg){}

		@Override
		public void onDisconnect()
		{
			removeProcessor(this);
			System.out.println("TimerWebSocket: "+processors.size());
		}
	}
	
	@Override
	public WebSocketProcessor getWebSocketProcessor()
	{
		return addNewProcessor();
	}
	@Override
	public void onStatus(KplexStatus status){synchronized (this.status){this.status = status;}}	
}
