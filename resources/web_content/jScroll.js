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

function JScroll(iframe, scrollMinSize, scrollMaxSize, scrollOffset, vScrollClass, vScrollBarClass, hScrollClass, hScrollBarClass)
{
	this.scrollMinSize = scrollMinSize;
	this.scrollMinLength = scrollMaxSize*2;
	this.scrollOffset = scrollOffset;
	
	this.scrolledManual = false;
	this.onScrollManual = null;
	
	/*******************************WRAPPERS************************************/
	this.iframe = iframe;
	var root = iframe.parentElement;
	
	var wrap0 = document.createElement("div");
	wrap0.style.position = "relative";
	wrap0.style.padding = "0";
	wrap0.style.margin = "0";
	wrap0.style.width = "100%";
	wrap0.style.height = "100%";
	
	var wrap1 = document.createElement("div");
	wrap1.style.position = "absolute";
	wrap1.style.left = "0";
	wrap1.style.right = scrollMinSize+"px";
	wrap1.style.top = "0";
	wrap1.style.bottom = scrollMinSize+"px";
	wrap1.style.overflow = "hidden";
	wrap1.style.padding = "0";
	wrap1.style.margin = "0";
	this.wrap1 = wrap1;
	
	var wrap2 = document.createElement("div");
	wrap2.style.position = "absolute";
	wrap2.style.left = "0";
	wrap2.style.top = "0";
	wrap2.style.bottom = "0";
	wrap2.style.right = "0";
	wrap2.style.padding = "0";
	wrap2.style.margin = "0";
	this.wrap2 = wrap2;
	
	var wrap3 = document.createElement("div");
	wrap3.style.position = "relative";
	wrap3.style.padding = "0";
	wrap3.style.margin = "0";
	wrap3.style.width = "100%";
	wrap3.style.height = "100%";
	
	root.appendChild(wrap0);
	wrap0.appendChild(wrap1);
	wrap1.appendChild(wrap2);
	wrap2.appendChild(wrap3);
	wrap3.appendChild(iframe);
	/*******************************WRAPPERS************************************/
	
	/*******************************VERTICAL************************************/
	this.vScrollArea = document.createElement("div");
	this.vScrollArea.className = vScrollClass;
	this.vScrollArea.style.bottom = scrollMinSize+"px";
	this.vScrollArea.style.overflow = "hidden";
	wrap0.appendChild(this.vScrollArea);
	this.vScrollArea.addEventListener("mousedown", this.onMouseDownVerticalArea.bind(this));
		
	this.vScrollBar = document.createElement("div");
	this.vScrollBar.className = vScrollBarClass;	
	this.vScrollArea.appendChild(this.vScrollBar);
	this.vScrollActive = false;
	this.vScrollDragOfs = 0;
	this.vScrollDragStart = 0;
	this.vScrollBar.addEventListener("mousedown", this.onMouseDownVerticalBar.bind(this));
	/*******************************VERTICAL************************************/
	
	/*******************************HORIZONTAL**********************************/
	this.hScrollArea = document.createElement("div");
	this.hScrollArea.className = hScrollClass;
	this.hScrollArea.style.right = scrollMinSize+"px";
	this.hScrollArea.style.overflow = "hidden";
	wrap0.appendChild(this.hScrollArea);
	this.hScrollArea.addEventListener("mousedown", this.onMouseDownHorizontalArea.bind(this));
	
	this.hScrollBar = document.createElement("div");
	this.hScrollBar.className = hScrollBarClass;
	this.hScrollArea.appendChild(this.hScrollBar);
	this.hScrollActive = false;
	this.hScrollDragOfs= 0;
	this.hScrollDragStart = 0;
	this.hScrollBar.addEventListener("mousedown", this.onMouseDownHorizontalBar.bind(this));
	/*******************************HORIZONTAL**********************************/
		
	//iframe.contentWindow.addEventListener('input', (function(){this.refresh();/*console.log("keyup")*/}).bind(this));
	iframe.contentWindow.addEventListener("scroll", (function(){this.refresh();}).bind(this));
	iframe.contentWindow.addEventListener("resize", (function(){this.refresh();}).bind(this));
	
	document.addEventListener("mousemove", this.onMouseMoveGlobal.bind(this));
	iframe.contentWindow.addEventListener("mousemove", this.onMouseMoveGlobal.bind(this));
	document.addEventListener("mouseup",   this.onMouseUpGlobal.bind(this));
	iframe.contentWindow.addEventListener("mouseup",   this.onMouseUpGlobal.bind(this));
	
	iframe.contentWindow.addEventListener("wheel", (function(event){this.setScrolledManual();this.refresh();}).bind(this));
	iframe.contentWindow.addEventListener("mousewheel", (function(event){this.setScrolledManual();this.refresh();}).bind(this));
	iframe.contentWindow.addEventListener("onmousewheel", (function(event){this.setScrolledManual();this.refresh();}).bind(this));
	
	setInterval((function(){this.refresh();}).bind(this),30); //TODO bad
	
	this.nativeScrollWidth = 0;
	this.isInit = false;
	iframe.addEventListener('load', (function(){this.refresh();}).bind(this));
}

JScroll.prototype.getScrollBarWidth = function()
{
	var inner = document.createElement('p');
	inner.style.width = "100%";
	inner.style.height = "200px";

	var outer = document.createElement('div');
	outer.style.position = "absolute";
	outer.style.top = "0px";
	outer.style.left = "0px";
	outer.style.visibility = "hidden";
	outer.style.width = "200px";
	outer.style.height = "150px";
	outer.style.overflow = "hidden";
	outer.appendChild(inner);

	document.body.appendChild(outer);
	var w1 = inner.offsetWidth;
	outer.style.overflow = 'scroll';
	var w2 = inner.offsetWidth;
	if(w1 == w2)
		w2 = outer.clientWidth;

	document.body.removeChild(outer);

	return (w1 - w2);
}

JScroll.prototype.init = function()
{
	if(this.isInit)
		return;
	
	this.nativeScrollWidth = this.getScrollBarWidth();
	this.wrap2.style.right = -this.nativeScrollWidth+'px';
	this.wrap2.style.bottom = -this.nativeScrollWidth+'px';
	this.isInit = true;
}
JScroll.prototype.refresh = function()
{
	this.init();
	if(this.scrolledManual)
	{
		if(this.onScrollManual)
			this.onScrollManual();
		this.scrolledManual = false;
	}
	/****************************VERTICAL********************************************/
	var vSize = this.iframe.contentWindow.document.documentElement.scrollHeight;
	var vSizeClient = this.wrap2.clientHeight-this.nativeScrollWidth;
	var vSizeScroll = this.vScrollArea.clientHeight-2*this.scrollOffset;
	var vMinRatio = this.scrollMinLength/vSizeScroll;
	var vRatio = Math.max(vMinRatio, Math.min(vSizeClient/vSize, 1));
	var vSizeBar = vRatio*vSizeScroll;
	this.vScrollBar.style.height = vSizeBar + "px";
	
	//if(false)
	if(vSize < vSizeClient)
	{
		this.vScrollArea.style.display = "none";
		this.wrap1.style.right = "0";
		this.hScrollArea.style.right = "0";
	}
	else
	{
		this.vScrollArea.style.display = "block";
		this.wrap1.style.right = this.scrollMinSize+"px";
		this.hScrollArea.style.right = this.scrollMinSize+"px";
		
		var vPos = this.iframe.contentWindow.pageYOffset;
		var vS1 = vSize-vSizeClient;
		var vS2 = vSizeScroll-vSizeBar;	
		this.vScrollBar.style.top = Math.max(0,Math.min((vPos/vS1),1))*vS2 + this.scrollOffset + "px";
	}
	/****************************VERTICAL********************************************/
		
	/****************************HORIZOTAL*-*****************************************/
	var hSize = this.iframe.contentWindow.document.documentElement.scrollWidth;		
	var hSizeClient = this.wrap2.clientWidth-this.nativeScrollWidth;
	var hSizeScroll = this.hScrollArea.clientWidth-2*this.scrollOffset;
	var hMinRatio = this.scrollMinLength/hSizeScroll;
	var hRatio = Math.max(hMinRatio, Math.min(hSizeClient/hSize, 1));
	var hSizeBar = hRatio*hSizeScroll;
	this.hScrollBar.style.width = hSizeBar + "px";
	
	
	//console.log("rh "+hSize+" "+hSizeClient);
	//if(false)
	if(hSize <= hSizeClient)
	{
		this.hScrollArea.style.display = "none";
		this.wrap1.style.bottom = "0";
		this.vScrollArea.style.bottom = "0";
	}
	else
	{
		this.hScrollArea.style.display = "block";
		this.wrap1.style.bottom = this.scrollMinSize+"px";
		this.vScrollArea.style.bottom = this.scrollMinSize+"px";
		
		var hPos = this.iframe.contentWindow.pageXOffset;
		var hS1 = hSize-hSizeClient;
		var hS2 = hSizeScroll-hSizeBar;	
		this.hScrollBar.style.left = Math.max(0,Math.min((hPos/hS1),1))*hS2 + this.scrollOffset + "px";
	}
	/****************************HORIZOTAL*-*****************************************/
}

JScroll.prototype.onMouseMoveGlobal = function(event)
{
	if(this.vScrollActive)
	{
		this.init();
		this.setScrolledManual();
		var dpos = event.screenY - this.vScrollDragStart;
		var newpos = dpos + this.vScrollDragOfs;
		var ratio = Math.max(0, Math.min(newpos/(this.vScrollArea.clientHeight-2*this.scrollOffset-this.vScrollBar.clientHeight),1));
		this.scrollV(ratio);
	}
	else if(this.hScrollActive)
	{
		this.init();
		this.setScrolledManual();
		var dpos = event.screenX - this.hScrollDragStart;
		var newpos = dpos + this.hScrollDragOfs;
		var ratio = Math.max(0, Math.min(newpos/(this.hScrollArea.clientWidth-2*this.scrollOffset-this.hScrollBar.clientWidth),1));
		this.scrollH(ratio);
	}
}
JScroll.prototype.scrollV = function(ratio)
{
	this.init();
	var vSize = this.iframe.contentWindow.document.documentElement.scrollHeight;
	var vSizeClient = this.wrap2.clientHeight-this.nativeScrollWidth;
	var vS1 = Math.max(0, vSize-vSizeClient);	
	this.iframe.contentWindow.scrollTo(this.iframe.contentWindow.pageXOffset, ratio*vS1);
	this.refresh(); //TODO
}
JScroll.prototype.scrollH = function(ratio)
{
	this.init();
	var hSize = this.iframe.contentWindow.document.documentElement.scrollWidth;		
	var hSizeClient = this.wrap2.clientWidth-this.nativeScrollWidth;
	var hS1 = Math.max(0, hSize-hSizeClient);
	this.iframe.contentWindow.scrollTo(ratio*hS1, this.iframe.contentWindow.pageYOffset);
	this.refresh(); //TODO
}

JScroll.prototype.onMouseUpGlobal = function(event)
{
	this.vScrollActive = false;
	this.hScrollActive = false;
}
JScroll.prototype.onMouseDownVerticalBar = function(event)
{
	this.vScrollActive = true;
	this.vScrollDragOfs = this.vScrollBar.getBoundingClientRect().top-this.vScrollArea.getBoundingClientRect().top;
	this.vScrollDragStart = event.screenY;
	event.stopPropagation();
}
JScroll.prototype.onMouseDownVerticalArea = function(event)
{
	this.init();
	this.setScrolledManual();
	var pos = event.clientY - this.vScrollArea.getBoundingClientRect().top - this.vScrollBar.clientHeight/2;
	var ratio = Math.max(0, Math.min((pos-this.scrollOffset)/(this.vScrollArea.clientHeight-2*this.scrollOffset-this.vScrollBar.clientHeight),1));
	this.scrollV(ratio);
	setTimeout((function(){this.onMouseDownVerticalBar(event);}).bind(this), 50);
}

JScroll.prototype.onMouseDownHorizontalBar = function(event)
{
	this.hScrollActive = true;
	this.hScrollDragOfs = this.hScrollBar.getBoundingClientRect().left-this.hScrollArea.getBoundingClientRect().left;
	this.hScrollDragStart = event.screenX;
	event.stopPropagation();
}
JScroll.prototype.onMouseDownHorizontalArea = function(event)
{
	this.init();
	this.setScrolledManual();
	var pos = event.clientX - this.hScrollArea.getBoundingClientRect().left - this.hScrollBar.clientWidth/2;
	var ratio = Math.max(0, Math.min((pos-this.scrollOffset)/(this.hScrollArea.clientWidth-2*this.scrollOffset-this.hScrollBar.clientWidth),1));
	this.scrollH(ratio);
	setTimeout((function(){this.onMouseDownHorizontalBar(event)}).bind(this), 50);
}
JScroll.prototype.setScrolledManual = function()
{
	this.scrolledManual = true;
	//setTimeout((function(){this.scrolledManual = false;}).bind(this),100);
}
