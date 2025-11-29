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
        menuPanel.setLayout(new GridLayout(11, 1, 5, 5)); // Increased row count
        menuPanel.setPreferredSize(new Dimension(240, 800));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 

        JButton btnHome = createNavButton("Dashboard");
        JButton btnWorkers = createNavButton("Workers");
        JButton btnTeams = createNavButton("Teams & Rosters");
        JButton btnSchedule = createNavButton("Schedule");
        JButton btnStaffing = createNavButton("Staff Assignments");
        JButton btnPlayerStats = createNavButton("Player Stats"); // NEW BUTTON
        JButton btnScores = createNavButton("Game Scores");
        JButton btnReports = createNavButton("View Reports");
        
        JButton btnExit = new JButton("Logout");
        btnExit.setBackground(new Color(150, 0, 0)); 
        btnExit.setForeground(Color.WHITE);

        menuPanel.add(btnHome);
        menuPanel.add(btnWorkers);
        menuPanel.add(btnTeams);
        menuPanel.add(btnSchedule);
        menuPanel.add(btnStaffing);
        menuPanel.add(btnPlayerStats); // ADDED HERE
        menuPanel.add(btnScores);
        menuPanel.add(btnReports);
        menuPanel.add(new JLabel("")); 
        menuPanel.add(new JLabel("")); 
        menuPanel.add(btnExit);

        add(menuPanel, BorderLayout.WEST);

        // Center Content
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBorder(BorderFactory.createLineBorder(Theme.ACCENT, 1)); 

        centerPanel.add(createHomeCard(), "HOME");
        centerPanel.add(new ManageWorkers(), "WORKERS");
        centerPanel.add(new ManageTeams(), "TEAMS");
        centerPanel.add(new ManageSchedule(), "SCHEDULE");
        centerPanel.add(new AssignStaff(), "STAFFING");
        centerPanel.add(new ManagePlayerStats(), "PLAYER_STATS"); // ADDED HERE
        centerPanel.add(new UpdateScore(), "SCORES");
        centerPanel.add(new ViewReports(), "REPORTS");

        add(centerPanel, BorderLayout.CENTER);

        // Listeners
        btnHome.addActionListener(e -> { cardLayout.show(centerPanel, "HOME"); updateMenuColors(btnHome); });
        btnWorkers.addActionListener(e -> { cardLayout.show(centerPanel, "WORKERS"); updateMenuColors(btnWorkers); });
        btnTeams.addActionListener(e -> { cardLayout.show(centerPanel, "TEAMS"); updateMenuColors(btnTeams); });
        btnSchedule.addActionListener(e -> { cardLayout.show(centerPanel, "SCHEDULE"); updateMenuColors(btnSchedule); });
        btnStaffing.addActionListener(e -> { cardLayout.show(centerPanel, "STAFFING"); updateMenuColors(btnStaffing); });
        btnPlayerStats.addActionListener(e -> { cardLayout.show(centerPanel, "PLAYER_STATS"); updateMenuColors(btnPlayerStats); });
        btnScores.addActionListener(e -> { cardLayout.show(centerPanel, "SCORES"); updateMenuColors(btnScores); });
        btnReports.addActionListener(e -> { cardLayout.show(centerPanel, "REPORTS"); updateMenuColors(btnReports); });
        
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