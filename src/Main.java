import interfaces.CircleAlchemy;
import interfaces.PauseMenu;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

void main() {

    CircleAlchemy game = new CircleAlchemy();
    PauseMenu[] pauseMenu = {null};

    JFrame frame = new JFrame();
    frame.setUndecorated(true);
    frame.setTitle("Circle Alchemy");

    pauseMenu[0] = new PauseMenu(game);

    JLayeredPane layered = new JLayeredPane();
    layered.setLayout(new OverlayLayout(layered));
    layered.add(game, JLayeredPane.DEFAULT_LAYER);
    layered.add(pauseMenu[0], JLayeredPane.PALETTE_LAYER);

    frame.add(layered);

    try {

        frame.setIconImage(ImageIO.read(new File("icon.png")));

    } catch (IOException e) {

        throw new RuntimeException(e);

    }

    final boolean[] isFullscreen = {true};

    Action togglePause = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {

            game.isPaused = !game.isPaused;
            pauseMenu[0].setVisible(game.isPaused);

            if (!game.isPaused) {

                game.requestFocusInWindow();

            }

        }

    };

    Action toggleFullscreen = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {

            frame.dispose();

            if (isFullscreen[0]) {

                frame.setUndecorated(false);
                frame.setExtendedState(JFrame.NORMAL);
                frame.setSize(1280, 720);
                frame.setLocationRelativeTo(null);

            } else {

                frame.setUndecorated(true);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            }

            isFullscreen[0] = !isFullscreen[0];
            frame.setVisible(true);

        }


    };

    game.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "pause");
    game.getActionMap().put("pause", togglePause);

    game.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "fullscreen");
    game.getActionMap().put("fullscreen", toggleFullscreen);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.setVisible(true);

}