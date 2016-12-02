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

var dataReceiver;
function dataReceiverInit()
{
	var dataIframe = document.getElementById("dataIframe");
	var down = document.getElementById("dataToBottom");
	var clear = document.getElementById("dataClearButton");
	var timeField = document.getElementById("timePanel");
	
	var overlay = document.getElementById("statusOverlay");
	var statusPanel = document.getElementById("statusContainer");
	var statusIframe = document.getElementById("statusIframe");
	var statusCloseButton = document.getElementById("statusCloseButton");
	
	dataReceiver = new DataReceiver(dataIframe, down, clear, timeField, overlay, statusPanel, statusIframe, statusCloseButton);
}

function DataReceiver(dataIframe, down, clear, timeField, overlay, statusPanel, statusIframe, statusCloseButton)
{
	this.dataScroll = new JScroll(dataIframe, 16, 20, 2, "vScroll", "vScrollBar", "hScroll", "hScrollBar");	
	this.dataIframe = dataIframe;
	this.down = down;
	this.clear = clear;
	this.timeField = timeField;	
	this.doc = dataIframe.contentWindow.document;
	
	this.statusScroll = new JScroll(statusIframe, 16, 20, 2, "vScroll", "vScrollBar", "hScroll", "hScrollBar");
	this.statusIframe = statusIframe;
	this.statusDoc = statusIframe.contentWindow.document;
	this.overlay = overlay;
	this.statusPanel = statusPanel;
	statusCloseButton.addEventListener('click', this.hideStatus.bind(this));
	
	this.dataScroll.onScrollManual = (function(){this.down.checked = false;}).bind(this);
	this.down.addEventListener('change', (function(){if(this.down.checked){this.dataScroll.scrollV(1);}}).bind(this));
	this.dataIframe.contentWindow.addEventListener('resize', (function(){if(this.down.checked){this.dataScroll.scrollV(1);}}).bind(this));
	
	this.doc.open('text/html', 'replace');
	//word-break: break-all;
	this.doc.write('<head><style type="text/css">body{overflow: scroll; font-family:monospace; font-size:13px; color: #ddd; padding:5px; margin:0;}</style></head><body></body>');
	var cnt = 0;
	this.dataScroll.scrollV(1);
	this.dataScroll.refresh();
	this.doc.close();
	
	this.statusDoc.open('text/html', 'replace');
	this.statusDoc.write('<head><style type="text/css">body{overflow: scroll; font-family:monospace; font-size:13px; color: #ddd; padding:15px; margin:0;}</style></head><body></body>');
	this.statusScroll.refresh();
	this.statusDoc.close();	
		
	this.clear.addEventListener('click', this.onClear.bind(this));
	
	var thisHost = window.location.hostname;
	var thisPort = window.location.port;
	this.websocketBase = "ws://"+thisHost+":"+thisPort;
	
	this.time = "--:--:--";
	this.date = "----.--.--";
	this.timezone = "---";
	
	/******************/
	var pushSpan = (function(margin, content)
	{
		var span = document.createElement('span');
		span.style.marginRight = margin;
		span.innerHTML = content;
		this.timeField.appendChild(span);
		return span;
	}).bind(this);
	
	var m = "10px";
	this.timeField.innerHTML = "";	
	this.timeField.time = pushSpan(m, this.time);
	pushSpan(0,"data:");
	this.timeField.statusLabel = pushSpan(m, "ERR");
	this.timeField.statusLabel.className = "StatusLabel";
	this.timeField.statusLabel.addEventListener('click', this.showStatus.bind(this));
	this.timeField.date = pushSpan(m, this.date);
	this.timeField.timezone = pushSpan(m, this.timezone);
	/******************/
	
	/******************/
	var addStatusEntry = (function(label)
	{
		var labelDiv = this.statusDoc.createElement('div');
		labelDiv.style.color = "#F00";
		labelDiv.appendChild(this.statusDoc.createTextNode(label));
		this.statusDoc.body.appendChild(labelDiv);
		var statusDiv = this.statusDoc.createElement('div');
		statusDiv.style.marginLeft = "50px";				
		this.statusDoc.body.appendChild(statusDiv);
		return {'label' : labelDiv, 'status' : statusDiv};
		
	}).bind(this);
	
	this.statusDoc.body.innerHTML = "";
	this.statusStatus = addStatusEntry("Status Stream:");
	this.statusData = addStatusEntry("Data Stream:");
	this.statusKplex = addStatusEntry("Kplex:");
	/******************/
	
	/******************/
	var addZero = function(i){return(i<10)?"0"+i:i;}
	var now = new Date();
	var day = addZero(now.getUTCDate());
	var month = addZero(now.getUTCMonth()+1);
	var year = now.getUTCFullYear();
	
	var hour = addZero(now.getUTCHours());
	var minutes = addZero(now.getUTCMinutes());
	var seconds = addZero(now.getUTCSeconds());
	
	//dd.MM.yyyy,HH:mm:ss,z
	var dateStr = day+"."+month+"."+year+","+hour+":"+minutes+":"+seconds+",UTC";
	var timeXhr = new XMLHttpRequest();
	timeXhr.open('POST', 'settime', false);
	//timeXhr.timeout = 2000;
	timeXhr.send(dateStr);
	
	if(timeXhr.status != 200)
	{
		alert('Не удалось установить время\n'+timeXhr.status + ': ' + timeXhr.statusText);
		return;
	}
	/******************/
	
	/******************/
	var xhr = new XMLHttpRequest();
	xhr.open('GET', 'last?n=100', false);
	xhr.send();
	
	if(xhr.status != 200)
		alert('Не удалось получить последние сообщения\n'+xhr.status + ': ' + xhr.statusText);
	else
	{
		var resp = xhr.responseText+"";
		var lastLines = resp.split("\r\n");
		for(var i = 0; i<lastLines.length; i++)
			this.pushMessage(lastLines[i]);
	}
	/******************/
	
	this.timeOk = false;
	this.timeStatus = "Initializing...";
	this.kplexOk = false;
	this.kplexStatus = "Initializing...";
	this.timeSocket = null;	
	this.connectTime();
	
	this.dataOk = false;
	this.dataStatus = "Initializing...";
	this.dataSocket = null;
	this.connectData();
	
	this.updateStatus();
}

DataReceiver.prototype.onClear = function()
{
	this.doc.body.innerHTML = "";
}

/*****************************TIME********************************/
DataReceiver.prototype.connectTime = function()
{
	this.timeSocket = new WebSocket(this.websocketBase+"/status");
	this.timeSocket.onclose = this.onTimeError.bind(this);
	this.timeSocket.onmessage = this.onTimeMessage.bind(this);
}
DataReceiver.prototype.onTimeError = function(event)
{
	this.timeOk = false;
	this.updateStatus();
	setTimeout((function(){this.connectTime();}).bind(this),1000);
	
	this.timeOk = false;
	this.timeStatus = "Connection Lost";
	this.kplexOk = false;
	this.kplexStatus = "Unknown";
}
DataReceiver.prototype.onTimeMessage = function(event)
{
	var msg = event.data+"";
	
	var splitPos = 0;
	for(var i=0; i<3; i++)
	{
		splitPos = msg.indexOf(' ', splitPos+1);
		if(splitPos < 0)
			break;
	}
	
	var ok = false;
	do
	{
		if(splitPos < 0)
			break;
		var splitPos1 = msg.indexOf(' ', splitPos+1);
		if(splitPos1 < 0)
			break;
		
		var time = msg.substr(0, splitPos);
		var msgOk = msg.substr(splitPos+1, splitPos1-(splitPos+1));
		var msgStat = msg.substr(splitPos1+1, msg.length-(splitPos1+1));
		
		this.kplexOk = (msgOk == 'true');
		this.kplexStatus = msgStat;
		
		var parts = (time+"").split(" ");
		if(parts.length != 3)
			break;
		
		this.time = parts[1];
		this.date = parts[0];
		this.timezone = parts[2];
		this.timeOk = true;
		this.timeStatus = "OK";
		
		this.updateStatus();
		return;
	}
	while(false);
	
	this.timeOk = false;
	this.timeStatus = "Parsing Error";
	this.kplexOk = false;
	this.kplexStatus = "Unknown";
	
	this.updateStatus();
}
/*****************************TIME********************************/

/*****************************DATA********************************/
DataReceiver.prototype.connectData = function()
{
	this.dataSocket = new WebSocket(this.websocketBase+"/data");
	this.dataSocket.onopen = this.onDataOpen.bind(this);
	this.dataSocket.onclose = this.onDataError.bind(this);
	this.dataSocket.onmessage = this.onDataMessage.bind(this);
}
DataReceiver.prototype.onDataOpen = function()
{
	this.dataOk = true;
	this.dataStatus = "OK";
	this.updateStatus();
}
DataReceiver.prototype.onDataError = function(event)
{
	this.dataOk = false;
	this.dataStatus = "Connection Lost";
	this.updateStatus();
	setTimeout((function(){this.connectData();}).bind(this),1000);
}
DataReceiver.prototype.onDataMessage = function(event)
{
	var str = ""+event.data;	
	this.pushMessage(str);
}
DataReceiver.prototype.pushMessage = function(msg)
{
	if(msg.length === 0)
		return;
	var str = msg;	
	var split = 0;
	for(var i=0; i<3; i++)
	{
		split = str.indexOf(',', split+1)
		if(split < 0)
			break;
	}
	
	if(split < 0)
	{
		var p = this.doc.createTextNode(str);
		this.doc.body.appendChild(p);
	}
	else
	{
		var str1 = str.substr(0,split);
		var str2 = str.substr(split, str.length-split);
		
		var s1 = this.doc.createElement('span');
		s1.style.color = "#F84";
		var p1 = this.doc.createTextNode(str1);
		s1.appendChild(p1);
		this.doc.body.appendChild(s1);		
		
		var p2 = this.doc.createTextNode(str2);
		this.doc.body.appendChild(p2);
	}
	
	this.doc.body.appendChild(this.doc.createElement('br'));
	if(this.down.checked)
		this.dataScroll.scrollV(1);
	else
		this.dataScroll.refresh();
}
/*****************************DATA********************************/

/*****************************STATUS******************************/
DataReceiver.prototype.updateStatus = function()
{	
	var ok = (this.dataOk && this.kplexOk && this.timeOk);
	
	this.timeField.time.innerHTML = this.time;
	this.timeField.statusLabel.innerHTML = ok ? "OK" : "ERR";
	this.timeField.statusLabel.style.color = ok ? "#0F0" : "#F00";
	this.timeField.date.innerHTML = this.date;
	this.timeField.timezone.innerHTML = this.timezone;
	
	var refreshEntry = (function(statusEntry, ok, status)
	{
		statusEntry.label.style.color = ok ? "#0F0" : "#F00";
		statusEntry.status.innerHTML = "";
		
		var parts = (status+"").split('\n');
		if(parts.length > 0)
			statusEntry.status.appendChild(this.statusDoc.createTextNode(parts[0]));
		else
			statusEntry.status.appendChild(this.statusDoc.createTextNode(" "));
		for(var i=1; i<parts.length; i++)
		{
			statusEntry.status.appendChild(this.statusDoc.createElement('br'));
			statusEntry.status.appendChild(this.statusDoc.createTextNode(parts[i]));
		}		
	}).bind(this);
	 
	refreshEntry(this.statusStatus, this.timeOk,  this.timeStatus);
	refreshEntry(this.statusData,   this.dataOk,  this.dataStatus);
	refreshEntry(this.statusKplex,  this.kplexOk, this.kplexStatus);
}
DataReceiver.prototype.showStatus = function()
{
	this.overlay.style.display = "block";
	setTimeout((function()
	{
		this.overlay.style.opacity = 1;
		setTimeout((function(){this.statusPanel.style.top = "0";}).bind(this),500);
	}).bind(this),50);
}
DataReceiver.prototype.hideStatus = function()
{
	this.statusPanel.style.top = "-100%";
	setTimeout((function()
	{
		this.overlay.style.opacity = 0;
		setTimeout((function()
		{
			this.overlay.style.display = "none";
		}).bind(this), 510);
	}).bind(this),300);
}
/*****************************STATUS******************************/

