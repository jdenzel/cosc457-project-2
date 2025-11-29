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
        
        // Initialize: Set the first button (Home) as Active
        if (!sidebarButtons.isEmpty()) {
            updateMenuColors(sidebarButtons.get(0));
        }
    }

    private void initComponents() {
        setTitle("Infinite Basketball League Management System");
        setSize(1200, 800); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. HEADER ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Theme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(1200, 60));
        
        JLabel lblTitle = new JLabel("  INFINITE BASKETBALL ADMIN");
        lblTitle.setFont(Theme.FONT_HEADER);
        lblTitle.setForeground(Theme.ACCENT); // This uses the Sports Gold
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. SIDEBAR ---
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(10, 1, 5, 5));
        menuPanel.setPreferredSize(new Dimension(220, 800));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 

        JButton btnHome = createNavButton("Dashboard");
        JButton btnWorkers = createNavButton("Workers");
        JButton btnTeams = createNavButton("Teams & Rosters");
        JButton btnSchedule = createNavButton("Schedule");
        JButton btnStaffing = createNavButton("Staff Assignments");
        JButton btnScores = createNavButton("Update Scores");
        JButton btnReports = createNavButton("View Reports");
        
        JButton btnExit = new JButton("Exit");
        btnExit.setBackground(new Color(150, 0, 0)); 
        btnExit.setForeground(Color.WHITE);

        menuPanel.add(btnHome);
        menuPanel.add(btnWorkers);
        menuPanel.add(btnTeams);
        menuPanel.add(btnSchedule);
        menuPanel.add(btnStaffing);
        menuPanel.add(btnScores);
        menuPanel.add(btnReports);
        menuPanel.add(new JLabel("")); 
        menuPanel.add(new JLabel("")); 
        menuPanel.add(btnExit);

        add(menuPanel, BorderLayout.WEST);

        // --- 3. CONTENT AREA ---
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBorder(BorderFactory.createLineBorder(Theme.ACCENT, 1)); 

        // Initialize Panels
        JPanel homeCard = createHomeCard(); // UPDATED METHOD
        JPanel workersCard = new ManageWorkers();
        JPanel teamsCard = new ManageTeams();
        JPanel scheduleCard = new ManageSchedule();
        JPanel staffingCard = new AssignStaff();
        JPanel scoresCard = new UpdateScore();
        JPanel reportsCard = new ViewReports();

        centerPanel.add(homeCard, "HOME");
        centerPanel.add(workersCard, "WORKERS");
        centerPanel.add(teamsCard, "TEAMS");
        centerPanel.add(scheduleCard, "SCHEDULE");
        centerPanel.add(staffingCard, "STAFFING");
        centerPanel.add(scoresCard, "SCORES");
        centerPanel.add(reportsCard, "REPORTS");

        add(centerPanel, BorderLayout.CENTER);

        // --- 4. ACTIONS ---
        btnHome.addActionListener(e -> { cardLayout.show(centerPanel, "HOME"); updateMenuColors(btnHome); });
        btnWorkers.addActionListener(e -> { cardLayout.show(centerPanel, "WORKERS"); updateMenuColors(btnWorkers); });
        btnTeams.addActionListener(e -> { cardLayout.show(centerPanel, "TEAMS"); updateMenuColors(btnTeams); });
        btnSchedule.addActionListener(e -> { cardLayout.show(centerPanel, "SCHEDULE"); updateMenuColors(btnSchedule); });
        btnStaffing.addActionListener(e -> { cardLayout.show(centerPanel, "STAFFING"); updateMenuColors(btnStaffing); });
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

    // --- UPDATED DASHBOARD CARD WITH IMAGE ---
    private JPanel createHomeCard() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BACKGROUND);

        // 1. Welcome Message (Top)
        JLabel l = new JLabel("Welcome to the League Management System", SwingConstants.CENTER);
        l.setFont(Theme.FONT_HEADER);
        l.setForeground(Theme.TEXT_MAIN);
        l.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        p.add(l, BorderLayout.NORTH);

        // 2. The Image (Center)
        try {
            // This looks for 'ibl-win.jpeg' in the SAME package/folder as this class
            java.net.URL imgURL = getClass().getResource("ibl-win.jpeg");
            
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                
                // --- IMAGE RESIZING LOGIC ---
                // We resize it to 800px width while keeping aspect ratio
                Image img = icon.getImage();
                int width = 800;
                // Calculate height to keep aspect ratio
                int height = (int) ((double) icon.getIconHeight() / icon.getIconWidth() * width);
                
                Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                JLabel imgLabel = new JLabel(new ImageIcon(newImg));
                imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
                
                p.add(imgLabel, BorderLayout.CENTER);
            } else {
                // Fallback text if file is missing
                JLabel error = new JLabel("(Image 'ibl-win.jpeg' not found in package)", SwingConstants.CENTER);
                error.setForeground(Color.RED);
                p.add(error, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return p;
    }

    public static void main(String[] args) {
        SetupDatabase.init();
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}