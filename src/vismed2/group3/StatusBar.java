package vismed2.group3;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

/**
 * An implementation of an easy status bar for use in the application's main
 * window's bottom.
 * 
 * @author Sebastian Haas
 * @author Alexander Tatowsky
 *
 */
public class StatusBar extends JPanel {

	private static final long serialVersionUID = -2125289895690391964L;
	private JLabel label;

	public StatusBar() {
		super();
		setBorder(new BevelBorder(BevelBorder.LOWERED));
		label = new JLabel("Ready");
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setFont(new Font("Sans", Font.PLAIN, 12));
		add(label);
	}

	/**
	 * Sets the message to be displayed. Take care to only invoke from GUI
	 * thread.
	 * 
	 * @param message
	 *            The message to be displayed.
	 */
	public void setMessage(String message) {
		label.setText(message);
	}
}
