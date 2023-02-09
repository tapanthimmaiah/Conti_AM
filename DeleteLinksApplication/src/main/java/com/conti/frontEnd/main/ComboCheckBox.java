package com.conti.frontEnd.main;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class ComboCheckBox {
	public JComboBox<CheckableItem> makeUI() {
		CheckableItem[] m = { new CheckableItem("Accepted", false), new CheckableItem("Analysis", false),
				new CheckableItem("Complete", false), new CheckableItem("Concept Covered", false),
				new CheckableItem("In Clarification", false), new CheckableItem("In Work", false),
				new CheckableItem("New", false), new CheckableItem("Obsolete", false),
				new CheckableItem("Quote Covered", false), new CheckableItem("Ready for Review", false),
				new CheckableItem("Rejected", false), new CheckableItem("Released", false) };

		return new JComboCheckBox<CheckableItem>(m);
	}

	class CheckableItem {
		public final String text;
		public boolean selected;

		protected CheckableItem(String text, boolean selected) {
			this.text = text;
			this.selected = selected;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	class CheckBoxCellRenderer1<E extends CheckableItem> implements ListCellRenderer<E> {
		private final JLabel label = new JLabel(" ");
		private final JCheckBox check = new JCheckBox(" ");

		@Override
		public Component getListCellRendererComponent(JList list, CheckableItem value, int index, boolean isSelected,
				boolean cellHasFocus) {
			ListModel model = list.getModel();
			if (index < 0) {
				label.setText(getDataStringRepresentation(model));
				return label;
			} else {
				check.setText(Objects.toString(value, "null"));
				check.setSelected(value.selected);
				if (isSelected) {
					check.setBackground(list.getSelectionBackground());
					check.setForeground(list.getSelectionForeground());
				} else {
					check.setBackground(list.getBackground());
					check.setForeground(list.getForeground());
				}
				return check;
			}
		}

		String getDataStringRepresentation(ListModel model) {
			List<String> sl = new ArrayList<>();
			for (int i = 0; i < model.getSize(); i++) {
				Object o = model.getElementAt(i);
				if (o instanceof CheckableItem && ((CheckableItem) o).selected) {
					sl.add(o.toString());
				}
			}
			return sl.stream().sorted().collect(Collectors.joining(", "));
		}
	}

	class JComboCheckBox<E extends CheckableItem> extends JComboBox<E> {
		private boolean shouldntClose;
		private transient ActionListener listener;

		public JComboCheckBox() {
			super();
		}

		public JComboCheckBox(E[] m) {
			super(m);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(200, 20);
		}

		@Override
		public void updateUI() {
			setRenderer(null);
			removeActionListener(listener);
			super.updateUI();
			listener = e -> {
				if (e.getModifiers() == InputEvent.BUTTON1_MASK) {

					updateItem(getSelectedIndex());
					shouldntClose = true;
				}
			};
			setRenderer(new CheckBoxCellRenderer1<CheckableItem>());
			addActionListener(listener);
			getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "space-key-select");
			getActionMap().put("space-key-select", new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {

					Accessible a = getAccessibleContext().getAccessibleChild(0);
					if (a instanceof BasicComboPopup) {
						BasicComboPopup pop = (BasicComboPopup) a;
						int i = pop.getList().getSelectedIndex();

						updateItem(i);
					}
				}
			});
		}

		private void updateItem(int index) {
			if (isPopupVisible()) {
				E item = getItemAt(index);
				item.selected ^= true;
				removeItemAt(index);
				insertItemAt(item, index);
				setSelectedItem(item);
			}
		}

		@Override
		public void setPopupVisible(boolean v) {
			if (shouldntClose) {
				shouldntClose = false;
				return;
			} else {
				super.setPopupVisible(v);
			}
		}
	}
}