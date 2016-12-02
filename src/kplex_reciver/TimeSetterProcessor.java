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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import reactive_server.http_server.http.HTTPStatus;
import reactive_server.http_server.http.HttpProcessor;
import reactive_server.http_server.http.HttpRequest;
import reactive_server.http_server.http.HttpResponse;

public class TimeSetterProcessor extends HttpProcessor
{
	private KplexReciver reciver;
	public TimeSetterProcessor(KplexReciver reciver)
	{
		this.reciver = reciver;
	}

	@Override
	public HttpResponse onGet(HttpRequest request){return unsupportedMethod(request);}	
	@Override
	public HttpResponse onPut(HttpRequest request){return unsupportedMethod(request);}
	@Override
	public HttpResponse onPost(HttpRequest request)
	{		
		try
		{
			String date = new String(request.getData(),UTF8);
			Date d = dateFormat.parse(date);
			String setDate = toDate.format(d);
			
			String[] cmd = new String[]{"date","-u","--set="+setDate+""};
			InputStream is = Runtime.getRuntime().exec(cmd).getErrorStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));			
			if(reader.readLine() != null)
				throw new Exception();
			
			reciver.restart();
			return constructStatus(HTTPStatus.ST200, request);
		}
		catch (Exception e)
		{
			return constructStatus(HTTPStatus.ST500, request);
		}		
	}
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final SimpleDateFormat dateFormat;
	private static final SimpleDateFormat toDate;
	static
	{
		dateFormat = new SimpleDateFormat("dd.MM.yyyy,HH:mm:ss,z", Locale.ENGLISH);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		toDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		toDate.setTimeZone(TimeZone.getTimeZone("UTC"));	
	}
}
