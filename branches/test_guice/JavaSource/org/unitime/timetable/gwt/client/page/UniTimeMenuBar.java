/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.client.page;

import java.util.List;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.Pages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.Client.GwtPageChangeEvent;
import org.unitime.timetable.gwt.client.Client.GwtPageChangedHandler;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.MenuServiceAsync;
import org.unitime.timetable.gwt.shared.MenuInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Tomas Muller
 */
public class UniTimeMenuBar extends Composite {
	protected final MenuServiceAsync iService = GWT.create(MenuService.class);
	
	private MenuBar iMenu;
	private SimplePanel iSimple = null;
	
	private int iLastScrollLeft = 0, iLastScrollTop = 0, iLastClientWidth = 0;
	private Timer iMoveTimer;
	
	public UniTimeMenuBar(boolean absolute) {
		iMenu = new MenuBar();
		iMenu.addStyleName("unitime-NoPrint");
		iMenu.addStyleName("unitime-Menu");
		initWidget(iMenu);
		
		if (absolute) {
			DOM.setStyleAttribute(iMenu.getElement(), "position", "absolute");
			move(false);
			iMoveTimer = new Timer() {
				@Override
				public void run() {
					move(true);
				}
			};
			Window.addResizeHandler(new ResizeHandler() {
				@Override
				public void onResize(ResizeEvent event) {
					delayedMove();
				}
			});
			Window.addWindowScrollHandler(new Window.ScrollHandler() {
				@Override
				public void onWindowScroll(ScrollEvent event) {
					delayedMove();
				}
			});
			Client.addGwtPageChangedHandler(new GwtPageChangedHandler() {
				@Override
				public void onChange(GwtPageChangeEvent event) {
					delayedMove();
				}
			});
			iSimple = new SimplePanel();
			iSimple.setHeight("23px");
			new Timer() {
				@Override
				public void run() {
					delayedMove();
				}
			}.scheduleRepeating(5000);
		}
		
	}
	
	private void attach(final RootPanel rootPanel) {
		iService.getMenu(new AsyncCallback<List<MenuInterface>>() {
			@Override
			public void onSuccess(List<MenuInterface> result) {
				initMenu(iMenu, result, 0);
				if (iSimple != null)
					rootPanel.add(iSimple);
				rootPanel.add(UniTimeMenuBar.this);
				if (iSimple != null)
					iSimple.setHeight(String.valueOf(iMenu.getOffsetHeight()));
			}
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	private void move(boolean show) {
		iLastClientWidth = Window.getClientWidth();
		iLastScrollLeft = Window.getScrollLeft();
		iLastScrollTop = Window.getScrollTop();
		iMenu.setWidth(String.valueOf(iLastClientWidth - 2));
		DOM.setStyleAttribute(iMenu.getElement(), "left", String.valueOf(iLastScrollLeft));
		DOM.setStyleAttribute(iMenu.getElement(), "top", String.valueOf(iLastScrollTop));
		iMenu.setVisible(true);
	}
	
	private boolean needsMove() {
		return iLastClientWidth != Window.getClientWidth() ||
			iLastScrollLeft != Window.getScrollLeft() || iLastScrollTop != Window.getScrollTop();
	}
	
	private void delayedMove() {
		if (needsMove()) {
			iMenu.setVisible(false);
			iMoveTimer.schedule(100);
		}
	}
	
	private void initMenu(MenuBar menu, List<MenuInterface> items, int level) {
		MenuItemSeparator lastSeparator = null;
		for (final MenuInterface item: items) {
			if (item.isSeparator()) {
				lastSeparator = new MenuItemSeparator();
				menu.addSeparator(lastSeparator);
			} else if (item.hasSubMenus()) {
				if (item.getPage() == null) {
					MenuBar m = new MenuBar(true);
					initMenu(m, item.getSubMenus(), level + 1);
					menu.addItem(new MenuItem(item.getName().replace(" ", "&nbsp;"), true, m));
				} else {
					menu.addItem(new MenuItem(item.getName().replace(" ", "&nbsp;"), true, new Command() {
						@Override
						public void execute() {
							if (item.isGWT()) 
								//openPageAsync(item.getPage());
								openUrl(item.getName(), "gwt.jsp?page=" + item.getPage(), item.getTarget());
							else {
								openUrl(item.getName(), item.getPage(), item.getTarget());
							}
						}
					}));
					initMenu(menu, item.getSubMenus(), level);
				}
			} else {
				menu.addItem(new MenuItem(item.getName().replace(" ", "&nbsp;"), true, new Command() {
					@Override
					public void execute() {
						if (item.getPage() != null) {
							if (item.isGWT()) 
								//openPageAsync(item.getPage());
								openUrl(item.getName(), "gwt.jsp?page=" + item.getPage(), item.getTarget());
							else {
								openUrl(item.getName(), item.getPage(), item.getTarget());
							}
						}
					}
				}));
			}
		}
		if (level == 0 && lastSeparator != null) {
			lastSeparator.setStyleName("unitime-BlankSeparator");
			lastSeparator.setWidth("100%");
		}
	}
	
	protected void openUrl(final String name, final String url, String target) {
		if (target == null)
			LoadingWidget.getInstance().show();
		if ("dialog".equals(target)) {
			UniTimeFrameDialog.openDialog(name, url);
		} else if ("download".equals(target)) {
			ToolBox.open(url);
		} else {
			ToolBox.open(GWT.getHostPageBaseURL() + url);
		}
	}
	
	protected void openPageAsync(final String page) {
		LoadingWidget.getInstance().show();
		if (RootPanel.get("UniTimeGWT:Body") == null) {
			ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=" + page);
			return;
		}
		RootPanel.get("UniTimeGWT:Body").clear();
		RootPanel.get("UniTimeGWT:Body").getElement().setInnerHTML(null);
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				openPage(page);
				LoadingWidget.getInstance().hide();
			}
			public void onFailure(Throwable reason) {
				Label error = new Label("Failed to load the page (" + reason.getMessage() + ")");
				error.setStyleName("unitime-ErrorMessage");
				RootPanel.get("UniTimeGWT:Body").add(error);
				LoadingWidget.getInstance().hide();
			}
		});
	}
	
	protected void openPage(String page) {
		try {
			for (Pages p: Pages.values()) {
				if (p.name().equals(page)) {
					LoadingWidget.getInstance().setMessage("Loading " + p.title() + " ...");
					UniTimePageLabel.getInstance().setTitle(p.title());
					RootPanel.get("UniTimeGWT:Body").add(p.widget());
					return;
				}
			}
			Label error = new Label("Failed to load the page (" + (page == null ? "page not provided" : "page " + page + " not registered" ) + ")");
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("UniTimeGWT:Body").add(error);
		} catch (Exception e) {
			Label error = new Label("Failed to load the page (" + e.getMessage() + ")");
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("UniTimeGWT:Body").add(error);
		}
	}
	
	public void insert(final RootPanel panel) {
		if ("hide".equals(Window.Location.getParameter("menu")))
			panel.setVisible(false);
		else
			attach(panel);
	}

}