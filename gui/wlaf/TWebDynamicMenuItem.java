package gui.wlaf;

import java.awt.event.*;

import action.*;

import com.alee.extended.menu.*;

import core.*;

public class TWebDynamicMenuItem extends WebDynamicMenuItem {
	
//	private TAbstractAction abstractAction;

	public TWebDynamicMenuItem(TAbstractAction ta) {
		super();
//		this.abstractAction = ta;
		icon = TResources.getIcon((String) ta.getValue(TAbstractAction.ICON_ID),24);
		action = new ActionListener() {
			@Override     
			public void actionPerformed(ActionEvent e) {
//				ActionEvent ae = new ActionEvent("", 0, "");
//				abstractAction.actionPerformed(ae);
			}
		};
	}
}
