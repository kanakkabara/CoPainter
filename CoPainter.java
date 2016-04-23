import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CoPainter
{
    JFrame frame = new JFrame("Collaborative Painter");
    JTextField hostName = new JTextField();
    JTextField port = new JTextField();
    JLabel host = new JLabel("Host");
    JLabel portL = new JLabel("Port");
    JButton startHost = new JButton("Start as a host");
    JButton connectHost = new JButton("Connect to a host");

    public CoPainter()
    {
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout( 3, 2, 5, 10));
        panel.add(host);
        panel.add(hostName);
        panel.add(portL);
        panel.add(port);
        panel.add(startHost);
        panel.add(connectHost);

        startHost.addActionListener(new StartAsHost());
        connectHost.addActionListener(new StartAsClient());

        frame.getContentPane().add(panel);
        frame.setSize(300,150);
        frame.setLocationRelativeTo(null);
        frame.setVisible( true);
    }

    public static void main(String[] args){
        CoPainter Main = new CoPainter();
    }

    public class StartAsHost implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            if(port.getText().equals(""))
                JOptionPane.showMessageDialog(frame, "Please enter a port!");
            else{
                MainPainter3 mp = new MainPainter3(1, hostName.getText(), port.getText());
                frame.dispose();
            }
        }
    }

    public class StartAsClient implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            if(port.getText().equals(""))
                JOptionPane.showMessageDialog(frame, "Please enter a port!");
            if(hostName.getText().equals(""))
                JOptionPane.showMessageDialog(frame, "Please enter a Host IP");
            if(!(port.getText().equals(""))&&!(hostName.getText().equals(""))){
                MainPainter3 mp = new MainPainter3(2, hostName.getText(), port.getText());
                frame.dispose();
            }
        }
    }
}
