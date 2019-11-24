package gui.prueckl.draw;

import java.awt.*;
import java.awt.image.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Action;

import org.jdesktop.application.*;

import com.alee.extended.layout.*;
import com.alee.extended.panel.*;
import com.alee.laf.button.*;

import core.*;
import gui.*;

public class DrawingEditor extends TUIPanel {

	public DrawingPanel getDrawingPanel() {
		return currentDrawing;
	}

	private ActionMap drawinPanelActions;
	private DrawingPanel currentDrawing;
	private JScrollPane scrollPane;
	private WebButton deleteButton;
	public DrawingEditor() {
		this.currentDrawing = new DrawingPanel();

		ApplicationAction load = (ApplicationAction) TActionsFactory.getAction("loadVariable");
		load.putValue(TActionsFactory.GROUP, "DrawEditor");
		PropertyChangeListener list = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(TActionsFactory.DATA_LOADED)) {
					remove(currentDrawing);
					currentDrawing = null;
					currentDrawing = (DrawingPanel) evt.getNewValue();
					currentDrawing.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
					scrollPane.setViewportView(currentDrawing);
					deleteButton.setAction(drawinPanelActions.get("deleteFigure"));
				}
			}
		};
		load.addPropertyChangeListener(list);

		Action save = TActionsFactory.getAction("saveVariable");
		save.putValue(TActionsFactory.GROUP, "DrawEditor");
		save.putValue(TActionsFactory.INPUT_DATA, currentDrawing);

		setToolBar(Arrays.asList(load, save));

		// edit actions
		Action getImage = TActionsFactory.getAction("getImageFromClipBoard");
		getImage.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals(TActionsFactory.DATA_LOADED)) {
				getDrawingPanel().setBackgroundImage((BufferedImage) evt.getNewValue());
				// update send ??

			}
		});

		Action sendImage = TActionsFactory.getAction("sendImageToClipboard");
		sendImage.addPropertyChangeListener(evt -> {
			System.out.println(evt.getPropertyName());
		});

		GroupPanel group = TUIUtils.getButtonGroup();
		drawinPanelActions = Alesia.getInstance().getContext().getActionMap(currentDrawing);
		this.deleteButton = TUIUtils.getWebButtonForToolBar(drawinPanelActions.get("deleteFigure"));
		group.add(deleteButton);
		group.add(TUIUtils.getWebButtonForToolBar(getImage));
		group.add(TUIUtils.getWebButtonForToolBar(sendImage));
		group.add(TUIUtils.getWebButtonForToolBar(new FigureProperties(this)));
		getToolBarPanel().add(group, LineLayout.END);

		 // align buttons
		 addToolBarAction(new AlignFigure(this, AlignFigure.TOP), new AlignFigure(this, AlignFigure.LEFT),
		 new AlignFigure(this, AlignFigure.JUSTIFY_HORIZONTAL),
		 new AlignFigure(this, AlignFigure.JUSTIFY_VERTICAL));
		
		 group = TUIUtils.getButtonGroup();
		 // figures and conn
		 JToggleButton sel = TUIUtils.getWebToggleButtonForToolBar(null, group);
		 sel.setIcon(TResources.getSmallIcon("selection"));
		 sel.addItemListener(new ItemListener() {
		 public void itemStateChanged(ItemEvent e) {
		 DrawingPanel.select = ((AbstractButton) e.getSource()).isSelected();
		 Drawable.activeDrawableClass = null;
		 }
		 });
		
		 TUIUtils.getWebToggleButtonForToolBar(Drawable.getAction(RectangleFigure.class), group);
		 TUIUtils.getWebToggleButtonForToolBar(Drawable.getAction(EllipseFigure.class), group);
		 TUIUtils.getWebToggleButtonForToolBar(Drawable.getAction(LineConn.class), group);
		 TUIUtils.getWebToggleButtonForToolBar(Drawable.getAction(CurveConn.class), group);
		 getToolBar().add(group);

		// set the preferred size to the size of the screem
		currentDrawing.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		scrollPane = new JScrollPane(currentDrawing);
		setBodyComponent(scrollPane);

		// 191011: startting coding again in my new camp in Heidenauer. after almost 1 month of my transfer from
		// hamburger straﬂe

		// auto selection at star
		// sel.doClick();
	}

}
