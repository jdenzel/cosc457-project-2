package infinitebasketball;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainMenu extends JFrame {

    private CardLayout cardLayout;
    private JPanel centerPanel; 
    private List<JButton> sidebarButtons = new ArrayList<>();

    public MainMenu() {
        initComponents();
        Theme.apply(this);
        if (!sidebarButtons.isEmpty()) updateMenuColors(sidebarButtons.get(0));
    }

    private void initComponents() {
        setTitle("Infinite Basketball League Management System");
        setSize(1250, 850); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Theme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(1200, 60));
        JLabel lblTitle = new JLabel("  INFINITE BASKETBALL ADMIN");
        lblTitle.setFont(Theme.FONT_HEADER);
        lblTitle.setForeground(Theme.ACCENT);
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // Sidebar
        JPanel menuPanel = new JPanel();
        // Reduced row count since we removed a button
        menuPanel.setLayout(new GridLayout(11, 1, 5, 5)); 
        menuPanel.setPreferredSize(new Dimension(240, 800));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 

        // --- BUTTON CREATION ---
        JButton btnHome = createNavButton("Dashboard");
        JButton btnReports = createNavButton("View Reports");
        JButton btnSchedule = createNavButton("Schedule");
        JButton btnScores = createNavButton("Game Scores");
        JButton btnTeams = createNavButton("Teams & Rosters");
        
        // Separator
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.GRAY);
        
        JButton btnWorkers = createNavButton("Workers");
        // Removed btnStaffing
        
        JButton btnExit = new JButton("Exit");
        btnExit.setBackground(new Color(150, 0, 0)); 
        btnExit.setForeground(Color.WHITE);

        // --- ADDING TO MENU ---
        menuPanel.add(btnHome);
        menuPanel.add(btnReports);
        menuPanel.add(btnSchedule);
        menuPanel.add(btnScores);
        menuPanel.add(btnTeams);
        
        menuPanel.add(new JLabel("----------------------------", SwingConstants.CENTER));
        
        menuPanel.add(btnWorkers);
        
        menuPanel.add(new JLabel("")); // Spacer
        menuPanel.add(new JLabel("")); // Spacer
        menuPanel.add(btnExit);

        add(menuPanel, BorderLayout.WEST);

        // Center Content
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBorder(BorderFactory.createLineBorder(Theme.ACCENT, 1)); 

        centerPanel.add(createHomeCard(), "HOME");
        centerPanel.add(new ViewReports(), "REPORTS");
        centerPanel.add(new ManageSchedule(), "SCHEDULE");
        centerPanel.add(new UpdateScore(), "SCORES");
        centerPanel.add(new ManageTeams(), "TEAMS");
        centerPanel.add(new ManageWorkers(), "WORKERS");
        // Removed AssignStaff card
        
        add(centerPanel, BorderLayout.CENTER);

        // Listeners
        btnHome.addActionListener(e -> { cardLayout.show(centerPanel, "HOME"); updateMenuColors(btnHome); });
        btnReports.addActionListener(e -> { cardLayout.show(centerPanel, "REPORTS"); updateMenuColors(btnReports); });
        btnSchedule.addActionListener(e -> { cardLayout.show(centerPanel, "SCHEDULE"); updateMenuColors(btnSchedule); });
        btnScores.addActionListener(e -> { cardLayout.show(centerPanel, "SCORES"); updateMenuColors(btnScores); });
        btnTeams.addActionListener(e -> { cardLayout.show(centerPanel, "TEAMS"); updateMenuColors(btnTeams); });
        btnWorkers.addActionListener(e -> { cardLayout.show(centerPanel, "WORKERS"); updateMenuColors(btnWorkers); });
        
        btnExit.addActionListener(e -> System.exit(0));
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        sidebarButtons.add(btn);
        return btn;
    }

    private void updateMenuColors(JButton activeBtn) {
        for (JButton btn : sidebarButtons) {
            if (btn == activeBtn) {
                btn.setBackground(Theme.PRIMARY); 
                btn.setForeground(Theme.ACCENT);
                btn.setFont(Theme.FONT_BOLD);
            } else {
                btn.setBackground(Theme.BACKGROUND); 
                btn.setForeground(Color.LIGHT_GRAY);
                btn.setFont(Theme.FONT_NORMAL);
            }
        }
    }

    private JPanel createHomeCard() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BACKGROUND);
        JLabel l = new JLabel("Welcome to the League Management System", SwingConstants.CENTER);
        l.setFont(Theme.FONT_HEADER);
        l.setForeground(Theme.TEXT_MAIN);
        l.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        p.add(l, BorderLayout.NORTH);

        try {
            java.net.URL imgURL = getClass().getResource("ibl-win.jpeg");
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image img = icon.getImage();
                int width = 800;
                int height = (int) ((double) icon.getIconHeight() / icon.getIconWidth() * width);
                Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                JLabel imgLabel = new JLabel(new ImageIcon(newImg));
                imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
                p.add(imgLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return p;
    }

    public static void main(String[] args) {
        SetupDatabase.init();
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}