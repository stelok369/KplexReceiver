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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import kplex_receiver.KplexReceiver.KplexMessageListener;

public class NmeaStorage implements KplexMessageListener
{
	private final static String BASE_PATH = "/home/kplex_logs";
	private final static int FILES_COUNT = 100;
	//private final static int FILES_COUNT = 3;
	private final static long MAX_FILE_SIZE = 102400;//100kb
	//private final static long MAX_FILE_SIZE = 1048576; //1Mb
	private static class Storage
	{
		private static class FileEntry implements Comparable<FileEntry>
		{
			private Path path = null;
			public Path getPath(){return path;}
			private long lastUpdate = 0;
			
			public FileEntry(Path path)
			{
				this.path = path;
				this.lastUpdate = path.toFile().lastModified();
			}
			
			public long updateLastModified()
			{
				this.lastUpdate = path.toFile().lastModified();
				return this.lastUpdate;
			}

			@Override
			public int compareTo(FileEntry o)
			{
				if(lastUpdate < o.lastUpdate)
					return -1;
				else if(lastUpdate > o.lastUpdate)
					return 1;
				else
					return 0;
			}			
		}
		
		private ArrayDeque<FileEntry> files = new ArrayDeque<FileEntry>();
		public Storage() throws IOException
		{
			ArrayList<FileEntry> list = new ArrayList<FileEntry>();
			for(int i=0; i<FILES_COUNT; i++)
			{
				File file = new File(BASE_PATH+"/"+Integer.toString(i)+".log");
				if(!file.exists())
					file.createNewFile();
				list.add(new FileEntry(file.toPath()));
			}
			Collections.sort(list);
			for(FileEntry e : list)
				files.add(e);
		}
		
		public void push(byte[] bytes)
		{
			FileEntry e = files.getFirst();
			if(e.getPath().toFile().length()+bytes.length > MAX_FILE_SIZE)
			{
				e = files.pollLast();
				try
				{
					PrintWriter writer = new PrintWriter(e.getPath().toFile());
					writer.print("");
					writer.close();
				}
				catch (FileNotFoundException ex){ex.printStackTrace();}
				files.addFirst(e);
			}
			try{Files.write(e.getPath(), bytes, StandardOpenOption.APPEND);}
			catch (IOException e1){e1.printStackTrace(); /*//TODO status fail;*/}
			e.updateLastModified();
		}
		
		public byte[] getLastNLines(long N)
		{
			if(N>=0)
			{				
				ArrayDeque<ArrayDeque<byte[]>> result = new ArrayDeque<ArrayDeque<byte[]>>();
				long remainToFind = N;
				for(FileEntry e : files)
				{
					ArrayDeque<byte[]> lines = new ArrayDeque<byte[]>();
					try
					{
						byte[] bytes = Files.readAllBytes(e.getPath());
						int rnState = 0;
						ByteArrayOutputStream line = new ByteArrayOutputStream();
						
						for(byte b : bytes)
						{
							line.write(b);
							if(b == '\r' && rnState == 0)
								rnState++;
							else if(b == '\n' && rnState == 1)
								rnState++;
							else
								rnState = 0;
							
							if(rnState == 2)
							{
								if(lines.size()>=remainToFind)
									lines.removeFirst();
								lines.addLast(line.toByteArray());
								line.reset();
								rnState=0;
							}
						}
					}
					catch (IOException ex1){ex1.printStackTrace();}
					result.addFirst(lines);
					remainToFind -= lines.size();
					if(remainToFind <= 0)
						break;
				}
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				for(ArrayDeque<byte[]> arr : result)
				{
					for(byte[] line : arr)
						bos.write(line,0,line.length);
				}
				return bos.toByteArray();
			}
			else
			{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				Iterator<FileEntry> it = files.descendingIterator();
				while(it.hasNext())
				{
					FileEntry e = it.next();
					try
					{
						byte[] bytes = Files.readAllBytes(e.getPath());
						bos.write(bytes);
					}
					catch (IOException ex){ex.printStackTrace();}
				}
				return bos.toByteArray();
			}
		}
	}
	
	private Storage storage;
	public NmeaStorage() throws IOException
	{		
		storage = new Storage();
	}
		
	public byte[] getLastNLines(long N)
	{
		synchronized (storage)
		{
			return storage.getLastNLines(N);
		}		
	}
	
	@Override
	public void onMessage(NmeaMessage msg)
	{
		synchronized (storage)
		{
			storage.push((msg.toString()+"\r\n").getBytes(UTF8));
		}	
	}
	private final static Charset UTF8 = Charset.forName("UTF-8");
}
