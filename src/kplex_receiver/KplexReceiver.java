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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class KplexReceiver
{
	public interface KplexMessageListener
	{
		public void onMessage(NmeaMessage msg);
	}
	public interface KplexStatusListener
	{
		public void onStatus(KplexStatus status);
	}
	public static class KplexStatus
	{
		private boolean ok;
		public boolean isOk(){return ok;}
		private String status;
		public String getStatus(){return status;}
		public KplexStatus(boolean ok, String status)
		{
			this.ok = ok;
			this.status = status;
		}
		@Override
		public String toString()
		{
			return ok+" "+status;
		}
	}
	
	private ArrayList<KplexMessageListener> listeners = new ArrayList<KplexMessageListener>();
	public synchronized void registerMessageListener(KplexMessageListener listener){listeners.add(listener);}
	public synchronized void unregisterMessageListener(KplexMessageListener listener){listeners.remove(listener);}
	
	private ArrayList<KplexStatusListener> statusListeners = new ArrayList<KplexStatusListener>();
	public synchronized void registerStatusListener(KplexStatusListener status){statusListeners.add(status);}
	public synchronized void unregisterStatusListener(KplexStatusListener status){statusListeners.remove(status);}
	
	private void onMessage(NmeaMessage msg)
	{
		synchronized (listeners)
		{
			for(KplexMessageListener listener : listeners)
				listener.onMessage(msg);
		}
	}
	
	private void onStatus(KplexStatus status)
	{
		synchronized (statusListeners)
		{
			for(KplexStatusListener listener : statusListeners)
				listener.onStatus(status);
		}
	}
	
	private static ScheduledExecutorService delayExec = Executors.newScheduledThreadPool(1);
	private class KplexProcess implements Runnable
	{
		@Override
		public void run()
		{
			while(true)
			{
				while(!Thread.interrupted())
				{
					ProcessBuilder pb = new ProcessBuilder("kplex");
					String status = "";
					try
					{
						Process process = pb.start();
						InputStream stdout = process.getInputStream();
						InputStream stderr = process.getErrorStream();
						BufferedReader messageReader = new BufferedReader(new InputStreamReader(stdout));						
						BufferedReader errorReader = new BufferedReader(new InputStreamReader(stderr));
						
						ScheduledFuture<?> statusFuture = delayExec.schedule(new Runnable()
						{							
							@Override
							public void run(){onStatus(new KplexStatus(true, "OK"));}
						}, 200, TimeUnit.MILLISECONDS);						
						
						String line;
						while((line = messageReader.readLine()) != null)
							onMessage(new NmeaMessage(System.currentTimeMillis(), line));
						
						statusFuture.cancel(true);						
						StringBuilder statusBuilder = new StringBuilder();
						while((line = errorReader.readLine()) != null)
							statusBuilder.append(line).append('\n');
						status = statusBuilder.toString();
					}
					catch (IOException e){e.printStackTrace();}
					onStatus(new KplexStatus(false, status));
					try{Thread.sleep(100);} catch (InterruptedException e){}
				}
			}
		}	
	}
		
	private Thread kplexThread = null;
	public void restart()
	{
		if(kplexThread == null)
		{
			kplexThread = new Thread(new KplexProcess());
			kplexThread.start();
		}
		else
			kplexThread.interrupt();
	}
}
