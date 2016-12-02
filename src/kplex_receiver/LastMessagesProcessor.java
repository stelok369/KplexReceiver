/****************************************************************************
**                                                                         **
** Copyright (C) 2016 Smoliy Artem                                         **
** Contact: strelok369@yandex.ru                                           **
**                                                                         **
** This file is part of KplexReceiver.                                     **
**                                                                         **
** KplexReceiver is free software: you can redistribute it and/or modify   **
** it under the terms of the GNU General Public License as published by    **
** the Free Software Foundation, either version 3 of the License, or       **
** (at your option) any later version.                                     **
**                                                                         **
** KplexReceiver is distributed in the hope that it will be useful,        **
** but WITHOUT ANY WARRANTY; without even the implied warranty of          **
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the            **
** GNU General Public License for more details.                            **
**                                                                         **
** You should have received a copy of the GNU General Public License       **
** along with KplexReceiver. If not, see <http://www.gnu.org/licenses/>.   **
**                                                                         **
*****************************************************************************/

package kplex_receiver;

import reactive_server.http_server.http.ContentType;
import reactive_server.http_server.http.HTTPStatus;
import reactive_server.http_server.http.HttpProcessor;
import reactive_server.http_server.http.HttpRequest;
import reactive_server.http_server.http.HttpResponse;

public class LastMessagesProcessor extends HttpProcessor
{
	private NmeaStorage storage;
	public LastMessagesProcessor(NmeaStorage storage)
	{
		this.storage = storage;
	}
	
	@Override
	public HttpResponse onGet(HttpRequest request)
	{
		String nStr = request.getParams().getParam("n");
				
		int n = -1;
		if(nStr != null)
		{
			try{n = Integer.parseInt(nStr);}
			catch (Exception e){return constructStatus(HTTPStatus.ST400, request);}
		}
		return new HttpResponse(HTTPStatus.ST200, storage.getLastNLines(n), ContentType.CT_TXT_PLAIN);
	}

	@Override
	public HttpResponse onPost(HttpRequest request){return unsupportedMethod(request);}
	@Override
	public HttpResponse onPut(HttpRequest request){return unsupportedMethod(request);}
}
