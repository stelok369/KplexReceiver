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

var uiMain;
function uiMainInit()
{
	uiMain = new UiMain();
}

function UiMain()
{
	this.swCatData = document.getElementById("catData");
	this.swCatSettings = document.getElementById("catSettings");

	this.dataPanel = document.getElementById("panelData");
	this.settingsPanel = document.getElementById("panelSettings");
	
	this.swCatData.addEventListener('change', this.onModeChanged.bind(this), false);
	this.swCatSettings.addEventListener('change', this.onModeChanged.bind(this), false);

	this.swCatData.checked = true;
	this.ShowPanel(this.dataPanel);
}

UiMain.prototype.ShowPanel = function(panel)
{
	if(this.lastPanelShown != null)
	{
		this.lastPanelShown.style.left = "100%";
		setTimeout(() =>
		{
			//this.lastPanelShown.style.display = "none";
			this.lastPanelShown.style.opacity = "0";
			//panel.style.display = "block";
			setTimeout(() =>
			{
				panel.style.left="0";
				panel.style.opacity = "1";
				this.lastPanelShown = panel;
			}, 30);
		},300);
	}
	else
	{
		//panel.style.display = "block";
		panel.style.left = "0";
		panel.style.opacity = "1";
		this.lastPanelShown = panel;
	}
}

UiMain.prototype.onModeChanged = function()
{
	if(this.swCatData.checked)
		this.ShowPanel(this.dataPanel);
	else if(this.swCatSettings.checked)
		this.ShowPanel(this.settingsPanel);
}
