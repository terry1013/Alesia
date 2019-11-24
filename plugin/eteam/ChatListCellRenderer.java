package plugin.eteam;

import java.awt.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import core.datasource.*;

public class ChatListCellRenderer extends JPanel implements ListCellRenderer {

	private JLabel avatarLeft;
	private JLabel attach;
	private String patt = "<html><b>$name</b> $date<br>$text</html>";
	private Record me;
	private String myname;
	private ImageIcon myphoto;
	private static DateFormat dateFormat = new SimpleDateFormat("E dd hh:mm a");
	

	public ChatListCellRenderer() {
		super(new BorderLayout(4, 4));
		setBorder(new EmptyBorder(4, 4, 4, 4));
		this.avatarLeft = new JLabel();
		Dimension d = new Dimension(42, 42);
		avatarLeft.setPreferredSize(d);
		this.attach = new JLabel();
		attach.setVerticalAlignment(JLabel.TOP);
		add(avatarLeft, BorderLayout.NORTH);
		add(attach, BorderLayout.CENTER);
		
		this.me = ConnectionManager.getAccessTo("m_address_book").exist("m_abname = 'terry'");
		this.myname = (String) me.getFieldValue("m_abname");
		this.myphoto = new ImageIcon((byte[]) me.getFieldValue("m_abphoto"));
		myphoto = new ImageIcon(myphoto.getImage().getScaledInstance(42, 42, Image.SCALE_FAST));
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		setBackground(list.getBackground());
		Record r = (Record) value;
		Date d = new Date((Long) r.getFieldValue("e_msid"));
		String tx = patt.replace("$name", myname);
		tx += tx.replace("$date", dateFormat.format(d));
		tx += tx.replace("$text", (String) r.getFieldValue("e_mstext"));
		avatarLeft.setIcon(myphoto);
		avatarLeft.setText(tx);
		byte at[] = (byte[]) r.getFieldValue("e_msdata");  
		if (((String) r.getFieldValue("e_msdata_type")).equals("image")) {
			attach.setIcon(new ImageIcon(at));
		}
		return this;
	}
}
