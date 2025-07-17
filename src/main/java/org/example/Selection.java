// Full version of Selection.java with added filters: High Date, Low Date, %LowHigh, Change Low/High, Mark Low/High and date picker

package org.example;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.json.JSONArray;
import org.json.JSONObject;


import org.jdatepicker.impl.*; // for date picker
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;

public class Selection {
    private JFrame frame;
    private JTable table;
    private ButtonGroup reportGroup;
    private JTextField fromRSI, toRSI, fromMACD, toMACD, symbolFilterField, fromLowHighPct, toLowHighPct;
    private JTextField fromChange, toChange, fromMark, toMark;
    private JDatePickerImpl fromLowDatePicker, toLowDatePicker, fromHighDatePicker, toHighDatePicker;
    private JPanel rsiPanel, macdPanel, filterPanel;
    private JLabel rowCountLabel;
    private JTextField fromMarkDiff;
    private JTextField toMarkDiff;
    private JCheckBox sp500Checkbox;

    private JCheckBox myIndexCheckbox;
    private JLabel clockIconLabel;


    private JTextField fromVolatilityPct, toVolatilityPct;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Selection().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Report Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        JPanel reportSelectionPanel = new JPanel(new GridLayout(0, 1));
        reportSelectionPanel.setBorder(BorderFactory.createTitledBorder("1. Select Report"));

        JRadioButton salesBtn = new JRadioButton("Near 52-Week Low");
        JRadioButton polygonLiveBtn = new JRadioButton("Polygon Live Snapshot");
//        JRadioButton inventoryBtn = new JRadioButton("Inventory Report");
//        JRadioButton employeesBtn = new JRadioButton("Employee Report");
//        JRadioButton industryBarChartBtn = new JRadioButton("Industry Bar Chart");


        salesBtn.setActionCommand("near52low");
        polygonLiveBtn.setActionCommand("polygonlive");
//        inventoryBtn.setActionCommand("inventory");
//        employeesBtn.setActionCommand("employees");
//        industryBarChartBtn.setActionCommand("industrychart");


        reportGroup = new ButtonGroup();
        reportGroup.add(salesBtn);
        reportGroup.add(polygonLiveBtn);
//        reportGroup.add(inventoryBtn);
//        reportGroup.add(employeesBtn);
//        reportGroup.add(industryBarChartBtn);

        reportSelectionPanel.add(salesBtn);
        reportSelectionPanel.add(polygonLiveBtn);
//        reportSelectionPanel.add(inventoryBtn);
//        reportSelectionPanel.add(employeesBtn);
//        reportSelectionPanel.add(industryBarChartBtn);
        stylePanel(reportSelectionPanel);



        JButton loadButton = new JButton("Load Report");
        loadButton.addActionListener(e -> {
            String reportType = getSelectedReport();
            if (reportType == null) {
                JOptionPane.showMessageDialog(frame, "Please select a report.");
                return;
            }

            if ("industrychart".equals(reportType)) if ("industrychart".equals(reportType)) {
                List<Vector<String>> rows = new ArrayList<>();
                try (Scanner scanner = new Scanner(new File("/Users/baps/Documents/Stocks/ts/52lowData.txt"))) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] values = line.split("\t");
                        if (values.length > 21 && !values[21].trim().isEmpty() && !values[2].trim().isEmpty()) {  // 🟩 FIXED LINE
                            Vector<String> row = new Vector<>(Arrays.asList(values));
                            rows.add(row);
                        }  // 🟩 ADDED safeguard
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                showIndustryBarChart(rows);
                return;
            }


            // Handle other report types here...
        });

        filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder("2. Filters (Sales Report Only)"));

        rsiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rsiPanel.add(new JLabel("From RSI:"));
        fromRSI = new JTextField(5);
        rsiPanel.add(fromRSI);
        rsiPanel.add(new JLabel("To RSI:"));
        toRSI = new JTextField(5);
        rsiPanel.add(toRSI);

        macdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        macdPanel.add(new JLabel("From MACD:"));
        fromMACD = new JTextField(5);
        macdPanel.add(fromMACD);
        macdPanel.add(new JLabel("To MACD:"));
        toMACD = new JTextField(5);
        macdPanel.add(toMACD);

        JPanel symbolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        symbolPanel.add(new JLabel("Filter Symbol:"));
        symbolFilterField = new JTextField(10);
        symbolPanel.add(symbolFilterField);

        JPanel lowDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fromLowDatePicker = createDatePicker();
        toLowDatePicker = createDatePicker();
        lowDatePanel.add(new JLabel("Low Date From:"));
        lowDatePanel.add(fromLowDatePicker);
        lowDatePanel.add(new JLabel("To:"));
        lowDatePanel.add(toLowDatePicker);

        JPanel highDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fromHighDatePicker = createDatePicker();
        toHighDatePicker = createDatePicker();
        highDatePanel.add(new JLabel("High Date From:"));
        highDatePanel.add(fromHighDatePicker);
        highDatePanel.add(new JLabel("To:"));
        highDatePanel.add(toHighDatePicker);

        JPanel pctPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pctPanel.add(new JLabel("From %LowHigh:"));
        fromLowHighPct = new JTextField(5);
        pctPanel.add(fromLowHighPct);
        pctPanel.add(new JLabel("To %LowHigh:"));
        toLowHighPct = new JTextField(5);
        pctPanel.add(toLowHighPct);

        JPanel changePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        changePanel.add(new JLabel("Change From:"));
        fromChange = new JTextField(5);
        changePanel.add(fromChange);
        changePanel.add(new JLabel("To:"));
        toChange = new JTextField(5);
        changePanel.add(toChange);

        JPanel markPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        markPanel.add(new JLabel("Mark From:"));
        fromMark = new JTextField(5);
        markPanel.add(fromMark);
        markPanel.add(new JLabel("To:"));
        toMark = new JTextField(5);
        markPanel.add(toMark);

        JPanel markDiffPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        markDiffPanel.add(new JLabel("Mark Diff From:"));
        fromMarkDiff = new JTextField(5);
        markDiffPanel.add(fromMarkDiff);
        markDiffPanel.add(new JLabel("To:"));
        toMarkDiff = new JTextField(5);
        markDiffPanel.add(toMarkDiff);

        JPanel volatilityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        volatilityPanel.add(new JLabel("Volatility % From:"));
        fromVolatilityPct = new JTextField(5);
        volatilityPanel.add(fromVolatilityPct);
        volatilityPanel.add(new JLabel("To:"));
        toVolatilityPct = new JTextField(5);
        volatilityPanel.add(toVolatilityPct);


        sp500Checkbox = new JCheckBox("Include only S&P 500 companies");
        sp500Checkbox.setBackground(new java.awt.Color(230, 242, 255));


        myIndexCheckbox = new JCheckBox("Include only MyIndex companies");
        myIndexCheckbox.setBackground(new java.awt.Color(230, 242, 255));



        filterPanel.add(rsiPanel);
        filterPanel.add(macdPanel);
        filterPanel.add(symbolPanel);
        filterPanel.add(lowDatePanel);
        filterPanel.add(highDatePanel);
        filterPanel.add(pctPanel);
        filterPanel.add(changePanel);
        filterPanel.add(markPanel);
        filterPanel.add(markDiffPanel);
        filterPanel.add(volatilityPanel);
        filterPanel.add(sp500Checkbox);
        filterPanel.add(myIndexCheckbox);
        stylePanel(filterPanel);
        filterPanel.setVisible(false);

        salesBtn.addActionListener(e -> filterPanel.setVisible(true));
        polygonLiveBtn.addActionListener(e -> {
            filterPanel.setVisible(true);
            sp500Checkbox.setVisible(true);
            myIndexCheckbox.setVisible(true);
        });
        // ✅ Make the window full screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(screenSize);
//        inventoryBtn.addActionListener(e -> filterPanel.setVisible(false));
//        employeesBtn.addActionListener(e -> filterPanel.setVisible(false));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createTitledBorder("3. Actions"));


        JButton exportButton = new JButton("Export to Excel");

        buttonPanel.add(loadButton);
        buttonPanel.add(exportButton);
        stylePanel(buttonPanel);

        loadButton.addActionListener(e -> {
            String reportType = getSelectedReport();
            if (reportType != null) {
                showSampleData(reportType);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a report.");
            }
        });

        exportButton.addActionListener(e -> exportTableToExcel());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(reportSelectionPanel);
        topPanel.add(filterPanel);
        topPanel.add(buttonPanel);

        frame.add(topPanel, BorderLayout.NORTH);

        table = new JTable() {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 240, 255));
                }
                return c;
            }
        };

        table.setAutoCreateRowSorter(true);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setRowHeight(22);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Report Output"));

        rowCountLabel = new JLabel("Total Rows: 0");
        rowCountLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(rowCountLabel, BorderLayout.SOUTH);
        frame.add(centerPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private JDatePickerImpl createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        return new JDatePickerImpl(datePanel, new DateComponentFormatter());
    }

    private void stylePanel(JPanel panel) {
        panel.setBackground(new java.awt.Color(230, 242, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    private String getSelectedReport() {
        ButtonModel selected = reportGroup.getSelection();
        return (selected != null) ? selected.getActionCommand() : null;
    }

    private void showSampleData(String type) {
        Vector<String> columns = new Vector<>();
        Vector<Vector<String>> data = new Vector<>();

        switch (type) {
            case "near52low":
                Near52Low near52Low = new Near52Low();
                columns.addAll(near52Low.getNear52LowColumns());

                double fromR = parseDouble(fromRSI.getText(), 0);
                double toR = parseDouble(toRSI.getText(), 100);

                double lowhighperLow = parseDouble(fromLowHighPct.getText(), 0);
                double lowhighperHigh = parseDouble(toLowHighPct.getText(), 100);

                double fromChangeLow = parseDouble(fromChange.getText(), -100);
                double fromChangeHigh = parseDouble(toChange.getText(), 100);

                double fromMarkLow = parseDouble(fromMark.getText(), 0);
                double fromMarkHigh = parseDouble(toMark.getText(), 10000000);

                double fromMarkDiffLow = parseDouble(fromMarkDiff.getText(), Double.NEGATIVE_INFINITY);
                double fromMarkDiffHigh = parseDouble(toMarkDiff.getText(), Double.POSITIVE_INFINITY);

                double fromVolatility = parseDouble(fromVolatilityPct.getText(), 0);
                double toVolatility = parseDouble(toVolatilityPct.getText(), 100);

                String symbolFilter = symbolFilterField.getText().trim();

                String filePath = "/Users/baps/Documents/Stocks/ts/52lowData.txt";
                List<Vector<String>> rows = near52Low.readNear52LowFromTSV(filePath, fromR, toR, symbolFilter);

                boolean add=true;
                boolean filterSP500 = sp500Checkbox.isSelected();
                Map<String, String> companies = Index.IndexCompanies();

                boolean filterMyIndex = myIndexCheckbox.isSelected();
                Map<String, String> myIndexCompanies = MyIndex.MyIndexCompanies();


                for (Vector<String> row : rows) {
                    add=true;
                    if (row.size() > 5) {

                        double rsi = parseDouble(row.get(6), -1);
                        if ((rsi >= fromR && rsi <= toR) || rsi== -1){
                           //
                        }else{
                            add=false;
                        }

                        double lhper = parseDouble(row.get(18), -1);
                        if ((lhper >= lowhighperLow && lhper <= lowhighperHigh) ){
                            //
                        }else{
                            add=false;
                        }

                        double changeamt = parseDouble(row.get(3), -1);
                        if ((changeamt >= fromChangeLow && changeamt <= fromChangeHigh) ){
                            //
                        }else{
                            add=false;
                        }

                        double markamt = parseDouble(row.get(16), -1);

                            if ((markamt >= fromMarkLow && markamt <= fromMarkHigh)) {
                                //
                            } else {
                                add = false;
                            }

                        double volatilityPct = parseDouble(row.get(10), -1); // or the correct index where volatility % is stored
                        if (volatilityPct < fromVolatility || volatilityPct > toVolatility) {
                            add = false;
                        }

                        double markDiff =  parseDouble(row.get(19), -1);

                            if (!(markDiff >= fromMarkDiffLow && markDiff <= fromMarkDiffHigh)) {
                                add = false;

                        }

                    }


                    if (filterSP500) {
                        String smb= row.get(0);
                        String ic = companies.get(smb);

                        if( ic==null ) {
                            add = false;
                        }
                    }


                    if (filterMyIndex) {
                        String smb= row.get(0);
                        String ic = myIndexCompanies.get(smb);

                        if( ic==null ) {
                            add = false;
                        }
                    }



                if (add) {
                    data.add(row);
                }

                }

                table.setModel(new DefaultTableModel(data, columns));
                TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
                int[] numericCols = {2, 3, 4, 5, 6, 7, 8, 11, 12};
                for (int col : numericCols) {
                    sorter.setComparator(col, (o1, o2) -> compareAsDouble(o1, o2));
                }
                table.setRowSorter(sorter);
                rowCountLabel.setText("Total Rows: " + data.size());
                break;

            case "polygonlive":
                columns.addAll(Arrays.asList("Symbol", "Name",   "Last", "DayDiff", "Change %", "Open", "High", "Low", "Close", "Volume",
                       "PrevOpen", "PrevHigh", "PrevClose", "PrevVol", "After-Hours","Day Range Bar"));

//                List<Vector<String>> polygonData = fetchPolygonSnapshotData();
//                data.addAll(polygonData);

                List<Vector<String>> polygonData = fetchPolygonSnapshotData();

                boolean PfilterSP500 = sp500Checkbox.isSelected();
                boolean PfilterMyIndex = myIndexCheckbox.isSelected();
                Map<String, String> Pcompanies = Index.IndexCompanies();
                Map<String, String> PmyIndexCompanies = MyIndex.MyIndexCompanies();


                String PsymbolFilter = symbolFilterField.getText().trim().toLowerCase();
                double PfromMarkLow = parseDouble(fromMark.getText(), 0);
                double PfromMarkHigh = parseDouble(toMark.getText(), Double.MAX_VALUE);
                double PfromMarkDiffLow = parseDouble(fromMarkDiff.getText(), Double.NEGATIVE_INFINITY);
                double PfromMarkDiffHigh = parseDouble(toMarkDiff.getText(), Double.POSITIVE_INFINITY);

                for (Vector<String> row : polygonData) {
                    try {
                        String symbol = row.get(0).toLowerCase();
                        double mark = parseDouble(row.get(2), -1);         // Last price (Mark)
                        double prevClose = parseDouble(row.get(12), -1);   // Prev Close
                        double markDiff = mark - prevClose;
                        double high = parseDouble(row.get(6), -1);
                        double low = parseDouble(row.get(7), -1);

                        boolean matches = true;

                        if (!PsymbolFilter.isEmpty() && !symbol.contains(PsymbolFilter)) {
                            matches = false;
                        }

                        if (mark < PfromMarkLow || mark > PfromMarkHigh) {
                            matches = false;
                        }

                        if (markDiff < PfromMarkDiffLow || markDiff > PfromMarkDiffHigh) {
                            matches = false;
                        }

                        // ✅ Filter S&P 500
                        if (PfilterSP500 && !Pcompanies.containsKey(row.get(0))) {
                            matches = false;
                        }

                        // ✅ Filter MyIndex
                        if (PfilterMyIndex && !PmyIndexCompanies.containsKey(row.get(0))) {
                            matches = false;
                        }


                        if (matches) {
                            data.add(row);
                        }

                    } catch (Exception ignored) {}
                }


                DefaultTableModel polygonModel = new DefaultTableModel(data, columns);
                table.setModel(polygonModel);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                table.getColumnModel().getColumn(15).setCellRenderer(new RangeBarRenderer());
              //  table.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Ensures alignment

                TableRowSorter<TableModel> polygonSorter = new TableRowSorter<>(polygonModel);
                int[] numericPolygonCols = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,14}; // Indexes of numeric columns
                for (int col : numericPolygonCols) {
                    polygonSorter.setComparator(col, (o1, o2) -> compareAsDouble(o1, o2));
                }
                table.setRowSorter(polygonSorter);
                rowCountLabel.setText("Total Rows: " + data.size());
                break;

            case "employees":
                columns.add("ID");
                columns.add("Name");
                columns.add("Department");
                columns.add("Joining Date");

                data.add(row("101", "John Smith", "Sales", "2021-01-15"));
                data.add(row("102", "Alice Brown", "Inventory", "2022-03-01"));
                data.add(row("103", "David Lee", "HR", "2020-08-25"));
                table.setModel(new DefaultTableModel(data, columns));
                rowCountLabel.setText("Total Rows: " + data.size());
                break;
        }
    }

    private double parseDouble(String text, double defaultValue) {
        try {
            return Double.parseDouble(text.trim().replaceAll("[^0-9.\\-]", ""));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Vector<String> row(String... values) {
        Vector<String> row = new Vector<>();
        for (String value : values) {
            row.add(value);
        }
        return row;
    }

    private int compareAsDouble(Object o1, Object o2) {
        try {
            double d1 = Double.parseDouble(o1.toString().replaceAll("[^0-9.\\-]", ""));
            double d2 = Double.parseDouble(o2.toString().replaceAll("[^0-9.\\-]", ""));
            return Double.compare(d1, d2);
        } catch (Exception e) {
            return o1.toString().compareTo(o2.toString());
        }
    }

    private void exportTableToExcel() {
        TableModel model = table.getModel();
        if (model.getRowCount() == 0 || model.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(frame, "No data to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Excel File");
        chooser.setSelectedFile(new java.io.File("report.xlsx"));

        int userChoice = chooser.showSaveDialog(frame);
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            String filePath = chooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Report");

                Row header = sheet.createRow(0);
                for (int i = 0; i < model.getColumnCount(); i++) {
                    header.createCell(i).setCellValue(model.getColumnName(i));
                }

                for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
                    Row row = sheet.createRow(rowIndex + 1);
                    for (int colIndex = 0; colIndex < model.getColumnCount(); colIndex++) {
                        Object value = model.getValueAt(rowIndex, colIndex);
                        row.createCell(colIndex).setCellValue(value != null ? value.toString() : "");
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    workbook.write(fos);
                }

                JOptionPane.showMessageDialog(frame, "Exported to Excel:\n" + filePath);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Export failed:\n" + ex.getMessage());
            }
        }
    }

    private void showIndustryBarChart(List<Vector<String>> rows) {
        Map<String, Double> industryTotals = new HashMap<>();
        for (Vector<String> row : rows) {
            try {
                String industry = row.get(21).trim();
                String rawChange = row.get(3).trim();
                if (!industry.isEmpty() && !rawChange.isEmpty()) {
                    double netChange = Double.parseDouble(rawChange.replaceAll("[^0-9.\\-]", ""));
                    industryTotals.merge(industry, netChange, Double::sum);
                }
            } catch (Exception ignored) {}
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : industryTotals.entrySet()) {
            dataset.addValue(entry.getValue(), "Net Change Sum", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Total Net Change by Industry",
                "Industry",
                "Net Change",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));
        renderer.setBarPainter(new StandardBarPainter());

        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame chartFrame = new JFrame("Industry Chart");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.add(chartPanel);
        chartFrame.setSize(800, 600);
        chartFrame.setVisible(true);
    }

    private List<Vector<String>> fetchPolygonSnapshotData() {
        List<Vector<String>> rows = new ArrayList<>();
        Map<String, String> tickerNameMap = new HashMap<>();

        try {
            // Fetch all ticker names via pagination
            String nextUrl = "https://api.polygon.io/v3/reference/tickers?market=stocks&active=true&limit=1000&apiKey=8CFFkEI2zMfN7xBIkeuJz1qlJ4UJ0iRM";

            while (nextUrl != null) {
                URL url = new URL(nextUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) json.append(line);
                in.close();

                JSONObject obj = new JSONObject(json.toString());
                JSONArray results = obj.optJSONArray("results");
                if (results != null) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject t = results.getJSONObject(i);
                        String ticker = t.optString("ticker");
                        String name = t.optString("name");
                        if (!ticker.isEmpty() && !name.isEmpty()) {
                            tickerNameMap.put(ticker, name);
                        }
                    }
                }

                String nextToken = obj.optString("next_url", null);
                nextUrl = (nextToken != null && !nextToken.isEmpty()) ? nextToken + "&apiKey=8CFFkEI2zMfN7xBIkeuJz1qlJ4UJ0iRM" : null;
            }

            // Fetch snapshot data
            String urlStr = "https://api.polygon.io/v2/snapshot/locale/us/markets/stocks/tickers?apiKey=8CFFkEI2zMfN7xBIkeuJz1qlJ4UJ0iRM";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) json.append(line);
            in.close();

            JSONObject obj = new JSONObject(json.toString());
            JSONArray tickers = obj.getJSONArray("tickers");

            for (int i = 0; i < tickers.length(); i++) {
                JSONObject t = tickers.getJSONObject(i);
                JSONObject day = t.optJSONObject("day");
                JSONObject lastTrade = t.optJSONObject("lastTrade");
                JSONObject prevDay = t.optJSONObject("prevDay");

                String ticker = t.optString("ticker");
                String name = tickerNameMap.getOrDefault(ticker, "(Unknown)");

                double last = lastTrade != null ? lastTrade.optDouble("p", 0.0) : 0.0;
                double prevClose = prevDay != null ? prevDay.optDouble("c", 0.0) : 0.0;
                double afterHoursDelta = (last > 0 && prevClose > 0) ? last - prevClose : 0.0;
                double open = day.optDouble("o");
                double dayDiff = 0;
                if (open > 0 ){
                    dayDiff = last - prevClose;
                }


                Vector<String> row = new Vector<>();
                row.add(ticker);                                           // Symbol
                row.add(name);                                             // Name
                row.add(String.format("%.2f", last));
                row.add(String.format("%.2f", dayDiff));
                row.add(String.format("%.2f", t.optDouble("todaysChangePerc")));
                row.add(String.format("%.2f", day.optDouble("o")));
                row.add(String.format("%.2f", day.optDouble("h")));
                row.add(String.format("%.2f", day.optDouble("l")));
                row.add(String.format("%.2f", day.optDouble("c")));
                row.add(String.format("%.0f", day.optDouble("v")));
                row.add(String.format("%.2f", prevDay.optDouble("o")));
                row.add(String.format("%.2f", prevDay.optDouble("h")));
                row.add(String.format("%.2f", prevClose));
                row.add(String.format("%.0f", prevDay.optDouble("v")));
                row.add(String.format("%.2f", afterHoursDelta));

                // ✅ Add range bar
                double high = day.optDouble("h");
                double low = day.optDouble("l");
                double range = high - low;
                String bar = "N/A";

                if (range > 0 && last >= low && last <= high) {
                    int barLength = 20;
                    int pos = (int) ((last - low) / range * (barLength - 1));

                    StringBuilder barBuilder = new StringBuilder();

                    // ✅ Fixed-width labels for alignment
                    String lowStr = String.format("%7.2f", low);   // e.g., " 180.00"
                    String highStr = String.format("%7.2f", high); // e.g., " 200.00"

                    barBuilder.append(lowStr).append(" "); // pad low
                    barBuilder.append("|");

                    for (int j = 0; j < barLength; j++) {
                        if (j == pos) {
                            barBuilder.append("V");
                        } else {
                            barBuilder.append("-");
                        }
                    }

                    barBuilder.append("| ").append(highStr); // pad high

                    bar = barBuilder.toString();
                }
                row.add(bar);  // Add bar as the last column


                rows.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rows;
    }


    static class RangeBarRenderer extends JLabel implements TableCellRenderer {

        public RangeBarRenderer() {
            setOpaque(true);
            setFont(new Font("Monospaced", Font.PLAIN, 12)); // fixed-width font
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {

            String bar = (value != null) ? value.toString() : "";
            setText(bar);

            // Ensure consistent background on selection
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            // Extract bar range and marker position
            int vIndex = bar.indexOf("V");
            int barStart = bar.indexOf("|");
            int barEnd = bar.lastIndexOf("|");

            Color color = Color.DARK_GRAY; // default

            if (vIndex > barStart && barStart != -1 && barEnd > barStart) {
                double position = (double) (vIndex - barStart - 1) / (barEnd - barStart - 2);

                if (position <= 0.33) {
                    color = new Color(0, 150, 0); // green (near low)
                } else if (position <= 0.66) {
                    color = new Color(255, 140, 0); // yellow/orange (mid)
                } else {
                    color = new Color(200, 0, 0); // red (near high)
                }
            }

            // ⚠️ Important: setForeground LAST to override table defaults
            setForeground(color);

            return this;
        }
    }
}
