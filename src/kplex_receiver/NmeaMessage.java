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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NmeaMessage
{
	private long time;
	
	private String message;
	
	private final static SimpleDateFormat dateFormat;
	static
	{
		dateFormat = new SimpleDateFormat("dd.MM.yyyy,HH:mm:ss,z", Locale.ENGLISH);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public NmeaMessage(long time, String message)
	{
		this.time = time;
		this.message = message;
	}
	
	public String toString()
	{
		return dateFormat.format(new Date(time)) + "," +message.trim();//+ (s.replaceAll("\r", "/r").replaceAll("\n", "/n"));
	}
	
	public void print()
	{		
		System.out.println("Nmea message: \""+toString()+"\"");
	}
}
