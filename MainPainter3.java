import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.net.*;

public class MainPainter3
{
    JFrame frame;
    ArrayList<ObjectOutputStream> clientOutputStreams;
    ArrayList<ObjectInputStream> clientInputStreams;

    ArrayList<Point> pointsBuffer = new ArrayList<Point>();
    ArrayList<Path> paths = new ArrayList<Path>();
    Graphics2D g2D;
    MyPanel p;
    int thickness = 15;
    Color penColor = Color.BLACK;

    int choice;
    ObjectInputStream is;
    ObjectOutputStream os;

    public MainPainter3(int choice1, String IP, String port)
    {
        choice = choice1;
        frame = new JFrame();
        if (choice == 1){
            frame.setTitle("Server");
            clientOutputStreams = new ArrayList<ObjectOutputStream>();
            clientInputStreams = new ArrayList<ObjectInputStream>();
        }
        else
            frame.setTitle("Client");
        JPanel buttons = new JPanel();
        JPanel colors = new JPanel();
        JPanel thickness = new JPanel();

        buttons.setLayout(new BorderLayout());
        colors.setLayout(new FlowLayout());
        thickness.setLayout(new FlowLayout());

        JButton b1 = new JButton();  colors.add(b1);        b1.setBackground(Color.RED);    b1.setPreferredSize(new Dimension(50, 50));
        JButton b2 = new JButton();  colors.add(b2);        b2.setBackground(Color.GREEN);  b2.setPreferredSize(new Dimension(50, 50));
        JButton b3 = new JButton();  colors.add(b3);        b3.setBackground(Color.BLUE);   b3.setPreferredSize(new Dimension(50, 50));
        JButton b4 = new JButton();  colors.add(b4);        b4.setBackground(Color.WHITE);  b4.setPreferredSize(new Dimension(50, 50));
        JButton b5 = new JButton();  colors.add(b5);        b5.setBackground(Color.BLACK);  b5.setPreferredSize(new Dimension(50, 50));
        JButton b6 = new JButton();  thickness.add(b6);     b6.setBackground(Color.WHITE);  b6.setPreferredSize(new Dimension(50, 50));     b6.setIcon(new ImageIcon("images/10.png"));    b6.setName("10");
        JButton b7 = new JButton();  thickness.add(b7);     b7.setBackground(Color.WHITE);  b7.setPreferredSize(new Dimension(50, 50));     b7.setIcon(new ImageIcon("images/15.png"));    b7.setName("15");
        JButton b8 = new JButton();  thickness.add(b8);     b8.setBackground(Color.WHITE);  b8.setPreferredSize(new Dimension(50, 50));     b8.setIcon(new ImageIcon("images/20.png"));    b8.setName("20");
        JButton b9 = new JButton();  thickness.add(b9);     b9.setBackground(Color.WHITE);  b9.setPreferredSize(new Dimension(50, 50));     b9.setIcon(new ImageIcon("images/25.png"));    b9.setName("25");
        JButton b10 = new JButton(); thickness.add(b10);    b10.setBackground(Color.WHITE); b10.setPreferredSize(new Dimension(50, 50));    b10.setIcon(new ImageIcon("images/30.png"));   b10.setName("30");

        for(Component x: colors.getComponents()){
            JButton jb = (JButton) x;
            jb.addActionListener(new ColorChanged());
        }
        buttons.add(BorderLayout.WEST, colors);
        for(Component x: thickness.getComponents()){
            JButton jb = (JButton) x;
            jb.addActionListener(new ThicknessChanged());
        }
        buttons.add(BorderLayout.EAST, thickness);

        p = new MyPanel();
        frame.getContentPane().add(BorderLayout.CENTER, p);
        frame.getContentPane().add(BorderLayout.SOUTH, buttons);
        p.addMouseListener(p);
        p.addMouseMotionListener(p);

        JMenuBar MenuBar = new JMenuBar();
        JMenu menu = new JMenu("Action");
        JMenuItem menuitem1 = new JMenuItem("Clear");
        JMenuItem menuitem2 = new JMenuItem("Save");
        JMenuItem menuitem3 = new JMenuItem("Load");
        JMenuItem menuitem4 = new JMenuItem("Exit");
        frame.setJMenuBar(MenuBar);
        MenuBar.add(menu);

        if(choice==1){
            menu.add(menuitem1);    menu.addSeparator();    menuitem1.addActionListener(new MenuItemClicked());           
            menu.add(menuitem3);    menu.addSeparator();    menuitem3.addActionListener(new MenuItemClicked()); 
        }
        menu.add(menuitem2);    menu.addSeparator();    menuitem2.addActionListener(new MenuItemClicked());
        menu.add(menuitem4);    menuitem4.addActionListener(new MenuItemClicked()); 

        frame.setSize(600,600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(0);
        if(choice==1)
            frame.setVisible(true);

        Thread t = new Thread(new setupNetworking(choice, IP, Integer.parseInt(port)));
        t.start();
    }

    public class setupNetworking implements Runnable{
        int choice;
        String IP;
        int Port;
        public setupNetworking(int choice1, String IP1, int Port1) {
            choice = choice1;
            IP = IP1;
            Port = Port1;
        }

        public void run(){
            if (choice == 1) {
                try {
                    ServerSocket ss = new ServerSocket(Port);
                    while(true) {
                        Thread t = new Thread(new ClientHandler(ss));
                        t.start();
                    }
                } catch (Exception e) { 
                    JOptionPane.showMessageDialog(frame, "Unable to listen to port "+Port, "Fail To Start", JOptionPane.ERROR_MESSAGE);
                    CoPainter cp = new CoPainter();
                    frame.dispose(); 
                }
            }
            else if (choice == 2) {
                try {
                    Socket s = new Socket(IP, Port);
                    os = new ObjectOutputStream(s.getOutputStream());
                    is = new ObjectInputStream(s.getInputStream());
                    System.out.println("Connection established!");
                    frame.setVisible(true);
                    Thread t = new Thread(new InReader());
                    t.start();
                } catch (Exception e) { 
                    JOptionPane.showMessageDialog(frame, "Unable to connect to Host", "Fail To Start", JOptionPane.ERROR_MESSAGE);
                    CoPainter cp = new CoPainter();
                    frame.dispose(); 
                }   
            }
        }
    }

    public class InReader implements Runnable {
        public synchronized void run() {
            try{
                while (true) {
                    Object o = is.readObject();
                    if(o instanceof String){
                        String command = (String) o;
                        if(o.equals("CLEAR")){
                            clearCanvas();
                        }else if(o.equals("QUIT")){
                            JOptionPane.showMessageDialog(frame, "Host is gone! CoPainter will now close.", "Connection Dropped", JOptionPane.ERROR_MESSAGE);
                            System.exit(1);
                        }
                    }
                    else{
                        Path pt = new Path((ArrayList<Point>) o, (Color) is.readObject(), (int) is.readObject());
                        paths.add(pt);
                        p.repaint();
                    }
                }
            }
            catch(IOException e){}
            catch(ClassNotFoundException e){
            }
        }
    }

    public class ClientHandler implements Runnable{
        Socket s;
        ObjectInputStream iis;
        ObjectOutputStream oos;
        boolean notClosed = true;

        public ClientHandler(ServerSocket S) {
            try{
                s = S.accept();
                iis = new ObjectInputStream(s.getInputStream());
                oos = new ObjectOutputStream(s.getOutputStream());
                clientOutputStreams.add(oos);
                System.out.println("Connection established!");
                updateClient(oos);
            } catch (Exception e) { e.toString(); }
        }

        public synchronized void run() {         
            try{
                while(notClosed){
                    Object o = iis.readObject();
                    if(o instanceof Integer){
                        clientOutputStreams.remove(oos);
                        s.close();
                        notClosed = false;
                    }else{
                        Path pt = new Path((ArrayList<Point>) o, (Color) iis.readObject(), (int) iis.readObject());
                        paths.add(pt);
                        p.repaint();
                        echo(pt);
                    }
                }
            }
            catch(Exception e){e.toString();}
        }
    }

    class MyPanel extends JPanel implements MouseListener, MouseMotionListener {   //An inner class
        public synchronized void paintComponent(Graphics g) {
            try{
                g.setColor(Color.white);   //Erase the previous figures
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(penColor);
                if(g instanceof Graphics2D) {
                    g2D = (Graphics2D) g;
                    g2D.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                }
                
                for(Path pt: paths){
                    g2D.setStroke(new BasicStroke(pt.thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    pt.paint(g);
                }
                if(pointsBuffer.size()!=0){
                    Point prevPoint = null;
                    for (Point p: pointsBuffer) {
                        if (prevPoint != null) {
                            g.setColor(penColor);
                            g2D.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g.drawLine(prevPoint.x, prevPoint.y, p.x, p.y);
                        }
                        prevPoint = p;
                    }
                }
            }
            catch(java.util.ConcurrentModificationException e){}
        }

        @Override
        public void mouseDragged(MouseEvent event) {
            pointsBuffer.add(event.getPoint());
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent event) {
        }

        @Override
        public void mouseClicked(MouseEvent event) {
        }

        @Override
        public void mouseEntered(MouseEvent event) {
        }

        @Override
        public void mouseExited(MouseEvent event) {
        }

        @Override
        public void mousePressed(MouseEvent event) {   
            pointsBuffer.add(event.getPoint());
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            ArrayList<Point> points = new ArrayList<Point>(pointsBuffer);
            Path temp = new Path(points, penColor, thickness);
            paths.add(temp);
            if(choice==1){
                
                if(clientOutputStreams.size()!=0)
                    echo(temp);
            }
            else{
                try{
                    os.reset();
                    sendSerializedPath(temp, os);
                    os.flush();
                }
                catch(IOException e){}
            }
            pointsBuffer.clear();
            temp = null;
        }
    }

    public class ColorChanged implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            JButton x = (JButton) e.getSource();
            penColor = x.getBackground();
        }
    }

    public class ThicknessChanged implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            JButton x = (JButton) e.getSource();
            thickness = Integer.parseInt(x.getName());
        }
    }

    public class MenuItemClicked implements ActionListener{
        public void actionPerformed(ActionEvent event) {
            if (event.getActionCommand().equals("Clear")){
                int n = JOptionPane.showConfirmDialog(frame, "Are you sure you want to clear the frame?","Clear?", JOptionPane.YES_NO_OPTION);
                if(n==0){
                    clearCanvas();
                    for(ObjectOutputStream a :clientOutputStreams){
                        try{
                            a.writeObject("CLEAR");
                        } 
                        catch(Exception e){e.toString();}
                    }
                }
            }

            if (event.getActionCommand().equals("Save")){
                saveCurrentImage();
            }

            if (event.getActionCommand().equals("Load")){
                File x = null;
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File("Saves"));
                javax.swing.filechooser.FileFilter filter = new FileNameExtensionFilter("Serialized Image","ser");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    x = chooser.getSelectedFile();
                    try{
                        paths.clear();
                        FileInputStream f = new FileInputStream(x);
                        ObjectInputStream ois = new ObjectInputStream(f);
                        int count = (int) ois.readObject();
                        for(int i=0; i<count; i++) {
                            Path pt = new Path((ArrayList<Point>) ois.readObject(), (Color) ois.readObject(), (int) ois.readObject());
                            paths.add(pt);
                        }
                        p.repaint();
                        ois.close();
                        for(ObjectOutputStream a :clientOutputStreams){
                            a.writeObject("CLEAR");
                            updateClient(a);
                        }
                    }
                    catch (Exception e) {
                        e.toString();
                    }

                }
            }

            if (event.getActionCommand().equals("Exit")){
                if(choice==1){
                    if(paths.size()!=0){
                        int n = JOptionPane.showConfirmDialog(frame, "Save before quiting?","Quit?", JOptionPane.YES_NO_OPTION);
                        if(n==0)
                            saveCurrentImage();
                    }
                    for(ObjectOutputStream a :clientOutputStreams){
                        try{
                            a.writeObject("QUIT");
                        } 
                        catch(Exception e){e.toString();}
                    }
                    System.exit(1);
                }
                else{
                    try{
                        os.writeObject(0);
                        System.exit(1);
                    }
                    catch(Exception e){e.toString();}
                }
            }
        }

        public void saveCurrentImage(){
            File x = null;
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("Saves"));
            javax.swing.filechooser.FileFilter filter = new FileNameExtensionFilter("Serialized Image","ser");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showSaveDialog(frame);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                x = chooser.getSelectedFile();
                if(!chooser.getSelectedFile().getAbsolutePath().endsWith(".ser"))
                    x = new File(chooser.getSelectedFile() + ".ser");
                try {
                    FileOutputStream filestream = new FileOutputStream(x);
                    ObjectOutputStream oos = new ObjectOutputStream(filestream);
                    oos.writeObject(paths.size());
                    for(Path pt: paths){
                        sendSerializedPath(pt, oos);
                    }
                    oos.close();
                    JOptionPane.showMessageDialog(frame, "Image Saved!");
                }
                catch (Exception e) {
                    e.toString();
                }
            }
        }
    }

    public class Path implements Serializable{
        ArrayList<Point> pathPoints;
        Color pathColor;
        int thickness;

        public Path(ArrayList<Point> Points, Color color, int thick){
            this.pathPoints = Points;
            this.pathColor = color;
            this.thickness = thick;
        }

        public void paint(Graphics g) {
            Point prevPoint = null;
            for (Point p: this.pathPoints) {
                if (prevPoint != null) {
                    g.setColor(pathColor);
                    g.drawLine(prevPoint.x, prevPoint.y, p.x, p.y);
                }
                prevPoint = p;
            }
        }
    }

    public void echo(Path pt){
        for(ObjectOutputStream a :clientOutputStreams){
            try{
                a.reset();
                sendSerializedPath(pt, a);
                a.flush();
            } 
            catch(Exception e){e.toString();}
        }
    }

    public void updateClient(ObjectOutputStream a){
        try{
            for(Path pt: paths){
                a.reset();
                sendSerializedPath(pt, a);
                a.flush();
            }    
        } 
        catch(Exception e){e.toString();}
    }

    public void clearCanvas(){
        paths.clear();
        p.repaint();
    }

    public void sendSerializedPath(Path pt, ObjectOutputStream os) throws IOException{
        os.writeObject(pt.pathPoints);
        os.writeObject(pt.pathColor);
        os.writeObject(pt.thickness);
    }

    public static void main(String[] args){
        MainPainter3 mp = new MainPainter3(Integer.parseInt(args[0]), args[1], args[2]);
    }
}