package fcatools.conexpng.gui.lattice;

import java.awt.Graphics;
import java.util.Set;

import javax.swing.JPanel;

public class Label extends JPanel implements LatticeGraphElement {

    /**
     *
     */
    private static final long serialVersionUID = -2090868180188362366L;
    private int x;
    private int y;
    private Set<String> set;
    private boolean isObjectLabel;
    // private static Font font = new Font("Monospaced", Font.PLAIN, 12);

    public Label(Set<String> set, Node node, boolean isObjectLabel) {
        this.set = set;
        this.isObjectLabel = isObjectLabel;
        this.setBounds(x, y, 15, 15);

    }

    public void paint(Graphics g) {
    }

    public void update(int x, int y, boolean first) {
        this.x = x;
        this.y = y;

        if (getParent() != null) {
            getParent().repaint();
        }
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
        setBounds(x, y, 15, 15);
    }

    /**
     * Set x and y coordiantes with respect to the label type.
     * @param x
     * @param y
     */
    public void setXYWRTLabelType(int x, int y){
        if(isObjectLabel){
            this.x = x;
            this.y = y + 5*LatticeView.radius;
        }else{
            this.x = x;
            this.y = y - LatticeView.radius;
        }
        setBounds(x, y, 15, 15);
    }

    public void setSet(Set<String> set) {
        this.set = set;
    }

    public Set<String> getSet() {
        return set;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String elementsToString() {
        String s = "";
        for (String t : set) {
            s = s + t + ", ";
        }
        if (!s.isEmpty())
            s = " " + s.substring(0, s.length() - 2) + " ";
        return s;
    }

    // public void changed() {
    // setBounds(x, y, 30, 20);
    //
    // }
}
