package net.jr.lexer.expr;

import net.jr.util.IOUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GraphvizViewer extends JFrame implements MouseWheelListener, KeyListener {

    private static final long serialVersionUID = 98654312L;

    private JPanel jPanel;

    private JScrollPane jScrollPane;

    private boolean ctrl = false;

    private int imageW, imageH;

    private double scale = 1.0;

    public GraphvizViewer(BufferedImage image) {
        super();
        this.imageH = image.getHeight();
        this.imageW = image.getWidth();
        setSize(400, 300);
        setLayout(new BorderLayout());
        jPanel = new JPanel() {

            private static final long serialVersionUID = 1329844114L;

            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                ((Graphics2D) g).scale(scale, scale);
                g2d.drawImage(image, 0, 0, null);
            }
        };
        jPanel.setPreferredSize(new Dimension(imageW, imageH));
        jScrollPane = new JScrollPane(jPanel);
        add(jScrollPane, BorderLayout.CENTER);

        jScrollPane.addMouseWheelListener(this);
        addKeyListener(this);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (ctrl) {
            scale += e.getWheelRotation() * e.getScrollAmount() * -0.1;
            jPanel.setPreferredSize(new Dimension((int) (imageW * scale), (int) (imageH * scale)));
            jScrollPane.getViewport().setViewSize(jPanel.getPreferredSize());
            repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrl = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrl = false;
        }
    }

    public static GraphvizViewer show(String graph) throws IOException, InterruptedException {

        Process process = new ProcessBuilder()
                .command("dot", "-Tpng")
                .start();

        new Thread(() -> {
            try {
                OutputStream pOut = process.getOutputStream();
                IOUtil.copy(new ByteArrayInputStream(graph.getBytes()), pOut);
                pOut.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        ).start();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        new Thread(() -> {
            try {
                IOUtil.copy(process.getInputStream(), baos);
                baos.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        int result = process.waitFor();
        if (result != 0) {
            throw new RuntimeException("process exit code : " + result);
        }

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
        GraphvizViewer viewer = new GraphvizViewer(image);
        viewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        viewer.setVisible(true);
        return viewer;
    }
}
