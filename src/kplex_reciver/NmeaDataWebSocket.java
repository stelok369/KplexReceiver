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

import java.util.ArrayList;
import kplex_reciver.KplexReciver.KplexMessageListener;
import reactive_server.http_server.websocket.WebSocketConnector;
import reactive_server.http_server.websocket.WebSocketMessage;
import reactive_server.http_server.websocket.WebSocketProcessor;

public class NmeaDataWebSocket extends WebSocketConnector implements KplexMessageListener
{
	private ArrayList<NmeaDataWebSocketProcessor> processors = new ArrayList<NmeaDataWebSocketProcessor>();
	private synchronized void removeProcessor(NmeaDataWebSocketProcessor p){processors.remove(p);}
	private synchronized NmeaDataWebSocketProcessor getNewProcessor()
	{
		NmeaDataWebSocketProcessor p = new NmeaDataWebSocketProcessor();
		processors.add(p);
		return p;
	}
	
	private class NmeaDataWebSocketProcessor extends WebSocketProcessor
	{
		@Override
		public void onMessage(WebSocketMessage msg){}
		@Override
		public void onClose(WebSocketMessage msg){}
		@Override
		public void onDisconnect()
		{
			removeProcessor(this);
		}		
	}
	
	@Override
	public WebSocketProcessor getWebSocketProcessor(){return getNewProcessor();}
	@Override
	public void onMessage(NmeaMessage msg)
	{
		WebSocketMessage wmsg = new WebSocketMessage(msg.toString());
		synchronized(processors)
		{
			for(NmeaDataWebSocketProcessor p : processors)
				p.send(wmsg);
		}
	}
}
