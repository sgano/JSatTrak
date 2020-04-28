import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
 
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.event.MouseInputAdapter;
 
public class MouseDragXorExample {
    public static void main(String[] args) {
        try {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setColor(Color.BLUE);
            g.drawRect(200, 10, 50, 50);
            g.setColor(Color.MAGENTA);
            g.fillRect(100, 100, 20, 50);
            g.setColor(Color.RED);
            g.fillOval(200, 200, 80, 100);
            
            JLabel label = new JLabel(new ImageIcon(image));
            RectangleDragMouseListener listener = new RectangleDragMouseListener(g);
            label.addMouseListener(listener);
            label.addMouseMotionListener(listener);
            frame.add(label);
            frame.pack();
            frame.setVisible(true);            
        }
        catch (Exception e) {e.printStackTrace();}        
    }
 
    private static class RectangleDragMouseListener extends MouseInputAdapter {
        private Point point1 = null;
        private Point point2 = null;
        
        private Graphics2D g;
         
        public RectangleDragMouseListener(Graphics2D g) {
            this.g = g;
        }
        
        public void mousePressed(MouseEvent e) {
            point1 = e.getPoint();
        }
        
        public void mouseReleased(MouseEvent e) {
            if (point2 != null)
                drawRect(e);
            point2 = null;
        }
        
        public void mouseDragged(MouseEvent e) {
            g.setColor(Color.BLACK);
            g.setXORMode(Color.WHITE);
            if (point2 != null)
                drawRect(e);
            point2 = e.getPoint();
            drawRect(e);
        }
        
        private void drawRect(MouseEvent e) {
            int x = Math.min(point1.x, point2.x);
            int y = Math.min(point1.y, point2.y);
            int width = Math.abs(point1.x - point2.x);
            int height = Math.abs(point1.y - point2.y);
            g.drawRect(x, y, width, height);            
            e.getComponent().repaint(x, y, width+1, height+1);
        }
    }
}
