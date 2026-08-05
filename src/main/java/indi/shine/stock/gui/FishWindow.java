/*
package indi.shine.stock.gui;

import ai.plantdata.script.util.other.StringUtils;
import ai.plantdata.script.util.other.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.sun.jna.Pointer;
import indi.shine.stock.bean.po.DayKline;
import indi.shine.stock.common.InitBiz;
import indi.shine.stock.gui.bean.GuiStock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.sun.jna.Native.getWindowPointer;
import static indi.shine.stock.common.biz.DataCenterBiz.parseDayKline;
import static indi.shine.stock.env.EnvConfig.kLineUrl;

*/
/**
 * 股票悬浮窗（Windows透明+鼠标穿透+右侧固定）
 *//*

@Slf4j
public class FishWindow extends JFrame {

    private int fontSize = 10;
    // 行间距
    private int rowSpacing = 5;
    // 宽度
    private final int windowWidth = 95;
    // 顶部高度
    private int windowTopOffsetY = 250;
    private Font font =  new Font("微软雅黑", Font.PLAIN, fontSize);
    // 托盘
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    // 刷新间隔（单位：毫秒，可自定义）
    private final int REFRESH_INTERVAL = 200;
    private final JPanel panel = new JPanel();

    private ArrayList<GuiStock> stockList  = new ArrayList<>();
    // 定时任务线程池（核心：实时刷新）
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public FishWindow() {
        // 托盘
        initSystemTray();
        fetchStockList();
        // 基本窗口
        initBasicWindow();
        initUI();
        // 启动实时刷新任务
        startRefreshTask();
        // 关键：窗口可见后，延迟执行透明/穿透配置（确保displayable）
        SwingUtilities.invokeLater(this::initWindowsTransparent);
    }

    */
/**
     * 步骤1：仅初始化基础窗口属性（不涉及句柄操作）
     *//*

    private void initBasicWindow() {
        // 不显示任务栏窗口
        this.setType(Window.Type.UTILITY);
        setTitle("股票悬浮窗");
        setUndecorated(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0));

        // 窗口位置：右侧固定（先计算位置，不依赖句柄）
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int windowHeight = stockList.size() * (fontSize + rowSpacing) + 20;
        setBounds(screenSize.width - windowWidth - 15, windowTopOffsetY, windowWidth, windowHeight);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 窗口关闭监听（释放托盘+线程池）
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 移除托盘图标
                if (systemTray != null && trayIcon != null) {
                    systemTray.remove(trayIcon);
                }
                // 关闭定时线程池
                if (scheduler != null && !scheduler.isShutdown()) {
                    scheduler.shutdownNow();
                }
                System.exit(0);
            }
        });
    }

    private void initUI() {
        panel.setOpaque(false);
        GridLayout gridLayout = new GridLayout(0, 2, 0, rowSpacing);
        panel.setLayout(gridLayout);
        renderStockPanel();
        */
/*JButton configBtn = new JButton("配置");
        configBtn.setOpaque(false);
        configBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        configBtn.addActionListener(new ConfigListener());
        panel.add(configBtn);*//*

        add(panel);
    }

    private void renderStockPanel() {
        panel.removeAll();
        for (GuiStock stock : stockList) {
            JLabel label = new JLabel(stock.name);
            label.setForeground(new Color(102,51,0));
            label.setFont(font);
            panel.add(label);
            label = new JLabel(String.valueOf(stock.chg));
            if (stock.chg < 0) {
                label.setForeground(new Color(34, 139, 34));
            } else {
                label.setForeground(new Color(139, 0, 0));
            }
            label.setFont(font);
            panel.add(label);
        }
        // 刷新面板（线程安全）
        panel.revalidate();
        panel.repaint();
    }

    private void startRefreshTask() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                fetchStockList();
                log.info("refresh stock list...");
                SwingUtilities.invokeLater(() -> {
                    renderStockPanel();
                    resizeWindow();
                });
            } catch (Exception e) {
                e.printStackTrace();
                log.error("股票数据刷新失败：{}", e.getMessage());
            }
        }, 0, REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
    }

    */
/**
     * 新增：刷新窗口高度（适配股票数量/字体变化）
     *//*

    private void resizeWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int newHeight = stockList.size() * (fontSize + rowSpacing) + 20;
        setBounds(screenSize.width - windowWidth - 15, windowTopOffsetY, windowWidth, newHeight);
    }

    */
/**
     * 步骤3：延迟初始化Windows透明+鼠标穿透（核心修复：确保displayable）
     *//*

    private void initWindowsTransparent() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return;
        }
        // 双重确认：等待窗口变为displayable（避免极端情况）
        if (!this.isDisplayable()) {
            try {
                Thread.sleep(100); // 短暂等待
                if (!this.isDisplayable()) {
                    JOptionPane.showMessageDialog(null, "窗口未完成初始化，无法配置透明");
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            // 1. 兼容AWTUtilities设置透明（需窗口displayable）
            Class<?> hwndUtil = Class.forName("com.sun.awt.AWTUtilities");
            hwndUtil.getMethod("setWindowOpaque", Window.class, boolean.class)
                    .invoke(null, this, false);
            // 获取窗口句柄
            Pointer pointer = getWindowPointer(this);
            WinDef.HWND hwnd = new WinDef.HWND(pointer);
            int exStyle = com.sun.jna.platform.win32.User32.INSTANCE.GetWindowLong(hwnd, com.sun.jna.platform.win32.User32.GWL_EXSTYLE);
            exStyle |= 0x20 | 0x8000000; // WS_EX_TRANSPARENT + WS_EX_NOACTIVATE
            com.sun.jna.platform.win32.User32.INSTANCE.SetWindowLong(hwnd, com.sun.jna.platform.win32.User32.GWL_EXSTYLE, exStyle);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Windows透明配置失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    // 配置监听器
    private class ConfigListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String fontSizeStr = JOptionPane.showInputDialog("请输入字体大小（当前：" + fontSize + "）：", fontSize);
            if (fontSizeStr != null && !fontSizeStr.isEmpty()) {
                try {
                    fontSize = Integer.parseInt(fontSizeStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "字体大小必须为数字！");
                }
            }

            String rowSpacingStr = JOptionPane.showInputDialog("请输入行间距（像素，当前：" + rowSpacing + "）：", rowSpacing);
            if (rowSpacingStr != null && !rowSpacingStr.isEmpty()) {
                try {
                    rowSpacing = Integer.parseInt(rowSpacingStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "行间距必须为数字！");
                }
            }

            getContentPane().removeAll();
            initUI();
            revalidate();
            repaint();
        }
    }

    private void fetchStockList() {
        stockList.clear();
        String fileName = "fish-codes.txt";
        File file = new File(fileName);
        Set<String> codes = new HashSet<>();
        if (file.exists()) {
            try {
                for (String line : FileUtils.readLines(file)) {
                    if (StringUtils.hasText(line)) {
                        codes.add(line.trim());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            ClassLoader classLoader = FishWindow.class.getClassLoader();
            try (InputStream is = classLoader.getResourceAsStream(fileName);
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (StringUtils.hasText(line)) {
                        codes.add(line.trim());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (String code : codes) {
            stockList.add(guiStock(code));
        }
        stockList.sort((s1, s2) -> Double.compare(s2.chg, s1.chg));
    }

    private GuiStock guiStock(String code) {
        String url = kLineUrl(code, 1);
        String rs = HttpUtil.sendGet(url);
        JSONObject data = JSONObject.parseObject(rs).getJSONObject("data");
        GuiStock stock = new GuiStock(data.getString("name"));
        String kline = data.getJSONArray("klines").getString(0);
        DayKline dayKline = parseDayKline(kline);
        stock.chg = dayKline.chg;
        return stock;
    }

    */
/**
     * 新增：初始化系统托盘（右下角图标+右键菜单）
     *//*

    private void initSystemTray() {
        // 1. 判断系统是否支持托盘
        if (!SystemTray.isSupported()) {
            log.warn("当前系统不支持系统托盘，任务栏图标无法隐藏");
            return;
        }
        systemTray = SystemTray.getSystemTray();
        // 2. 加载托盘图标
        Image trayImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/gui.png"));
        // 3. 创建右键菜单（退出选项）
        PopupMenu popupMenu = new PopupMenu();
        MenuItem exitItem = new MenuItem("Exit");
        // 绑定退出事件
        exitItem.addActionListener((ActionEvent e) -> {
            // 触发窗口关闭逻辑
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        popupMenu.add(exitItem);
        // 4. 创建托盘图标
        trayIcon = new TrayIcon(trayImage, "股票悬浮窗", popupMenu);
        trayIcon.setImageAutoSize(true); // 自动适配图标大小
        // 可选：双击托盘图标显示/隐藏窗口
        trayIcon.addActionListener((ActionEvent e) -> {
            this.setVisible(!this.isVisible());
        });
        // 5. 添加托盘图标到系统托盘
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            log.error("添加系统托盘图标失败", e);
        }
    }

    public static void main(String[] args) {
        InitBiz.initHttpProxy();
        SwingUtilities.invokeLater(() -> {
            // 显示窗口（触发displayable状态）
            new FishWindow().setVisible(true);
        });
    }
}
*/
