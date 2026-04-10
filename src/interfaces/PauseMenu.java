package interfaces;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PauseMenu extends JPanel {

    public PauseMenu(CircleAlchemy game) {

        setOpaque(false);
        setLayout(new GridBagLayout());

        GridBagConstraints gridBuilder = new GridBagConstraints();
        gridBuilder.gridx  = 0;
        gridBuilder.fill   = GridBagConstraints.HORIZONTAL;
        gridBuilder.insets = new Insets(8, 0, 8, 0);

        JLabel title = new JLabel("Paused", SwingConstants.CENTER);
        title.setFont(new Font("Trebuchet MS", Font.BOLD, 136));
        title.setForeground(Color.WHITE);
        gridBuilder.gridy = 0;
        add(title, gridBuilder);

        //A button to resume the simulation.
        JButton resume = makeButton("Resume");

        resume.addActionListener(_ -> {

            game.isPaused = false;
            setVisible(false);

        });

        gridBuilder.gridy = 1;
        add(resume, gridBuilder);

        //A button for the settings.
        JButton settings = makeButton("Settings");
        settings.addActionListener(_ -> {

            //Settings panel goes here later Guh

        });

        gridBuilder.gridy = 2;
        add(settings, gridBuilder);

        //A button to quit the simulation.
        JButton quit = makeButton("Quit");

        quit.addActionListener(_ -> System.exit(0));

        gridBuilder.gridy = 3;
        add(quit, gridBuilder);

        setVisible(false);

    }

    private JButton makeButton(String text) {

        int r, g, b, fontSize, buttonWidth, buttonHeight;
        r = 60;
        g = 40;
        b = 60;
        fontSize = 64;
        buttonWidth = (int) ((fontSize * 3.5) + (20 * Math.log(fontSize + 1)));
        buttonHeight = (int) ((fontSize * 1.2) + (5 * Math.log(fontSize + 1)));

        JButton button = new JButton(text);
        button.setFont(new Font("Trebuchet MS", Font.BOLD, fontSize));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(r, g, b));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));

        button.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent e) {

                button.setBackground(new Color(r + 40, g + 40, b + 40));

            }

            public void mouseExited(MouseEvent e) {

                button.setBackground(new Color(r, g, b));

            }

        });

        return button;

    }

    @Override
    protected void paintComponent(Graphics g) {

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, getWidth(), getHeight());

    }

}