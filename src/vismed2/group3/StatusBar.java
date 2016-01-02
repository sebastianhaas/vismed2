package vismed2.group3;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

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

    public void setMessage(String message) {
        label.setText(message);
    }     
}
