import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.*;

public class PlotGenerator {

    static final String TABLES_DIR = "results/tables/";
    static final String PLOTS_DIR = "results/plots/";

    public static void main(String[] args) throws IOException {
        new File(PLOTS_DIR).mkdirs();

        workload1();
        workload2();
        workload3();
        workload4();

        System.out.println("All plots written to " + PLOTS_DIR);
    }


    static void workload1() throws IOException {
        List<Map<String, String>> rows = readCsv(TABLES_DIR + "workload1_random_access.csv");
        Map<String, Series> byStructure = groupSeries(rows, "structure", "n", "avg_time_ms");
        renderLineChart(PLOTS_DIR + "workload1_time_vs_n.png",
                "Workload 1: Random Access - Time vs n",
                "n (elements)", "avg time (ms) for 10,000 get() calls",
                true, false, byStructure);
    }

    static void workload2() throws IOException {
        List<Map<String, String>> rows = readCsv(TABLES_DIR + "workload2_search.csv");

        Map<String, Series> timeSeries = groupSeries(rows, "structure", "n", "avg_time_ms");
        renderLineChart(PLOTS_DIR + "workload2_time_vs_n.png",
                "Workload 2: Search - Time vs n",
                "n (elements)", "avg time (ms) for 1,000 contains() calls",
                true, false, timeSeries);

        Map<String, Series> compSeries = groupSeries(rows, "structure", "n", "total_comparisons");
        renderLineChart(PLOTS_DIR + "workload2_comparisons_vs_n.png",
                "Workload 2: Search - Comparisons vs n",
                "n (elements)", "total comparisons (1,000 queries)",
                true, true, compSeries);
    }

    static void workload3() throws IOException {
        List<Map<String, String>> rows = readCsv(TABLES_DIR + "workload3_insert_remove.csv");

        for (String op : new String[]{"insert", "remove"}) {
            List<Map<String, String>> filtered = new ArrayList<>();
            for (Map<String, String> r : rows) if (r.get("operation").equals(op)) filtered.add(r);

            // group key = structure + " (" + position + ")"
            Map<String, Series> series = new LinkedHashMap<>();
            for (Map<String, String> r : filtered) {
                String key = r.get("structure") + " (" + r.get("position") + ")";
                double n = Double.parseDouble(r.get("n"));
                double t = Double.parseDouble(r.get("avg_time_ms"));
                series.computeIfAbsent(key, k -> new Series(key)).add(n, t);
            }
            for (Series s : series.values()) s.sortByX();

            renderLineChart(PLOTS_DIR + "workload3_" + op + "_time_vs_n.png",
                    "Workload 3: " + Character.toUpperCase(op.charAt(0)) + op.substring(1) + " - Time vs n",
                    "n (initial size)", "avg time (ms) for 1,000 " + op + "s",
                    true, false, series);
        }
    }

    static void workload4() throws IOException {
        List<Map<String, String>> rows = readCsv(TABLES_DIR + "workload4_priority_processing.csv");

        Map<String, Series> timeSeries = new LinkedHashMap<>();
        Series insertS = new Series("insert (n ops)");
        Series extractS = new Series("extractMin (n ops)");
        for (Map<String, String> r : rows) {
            double n = Double.parseDouble(r.get("n"));
            insertS.add(n, Double.parseDouble(r.get("avg_insert_time_ms")));
            extractS.add(n, Double.parseDouble(r.get("avg_extract_time_ms")));
        }
        timeSeries.put(insertS.name, insertS);
        timeSeries.put(extractS.name, extractS);
        renderLineChart(PLOTS_DIR + "workload4_time_vs_n.png",
                "Workload 4: Min-Heap - Time vs n",
                "n", "avg total time (ms)",
                true, false, timeSeries);

        Map<String, Series> compSeries = new LinkedHashMap<>();
        Series comp = new Series("comparisons");
        for (Map<String, String> r : rows) {
            comp.add(Double.parseDouble(r.get("n")), Double.parseDouble(r.get("total_comparisons")));
        }
        compSeries.put(comp.name, comp);
        renderLineChart(PLOTS_DIR + "workload4_comparisons_vs_n.png",
                "Workload 4: Min-Heap - Comparisons vs n",
                "n", "total comparisons (n inserts + n extracts)",
                true, true, compSeries);
    }

   

    static class Series {
        String name;
        List<double[]> points = new ArrayList<>(); // [x, y]
        Series(String name) { this.name = name; }
        void add(double x, double y) { points.add(new double[]{x, y}); }
        void sortByX() { points.sort(Comparator.comparingDouble(p -> p[0])); }
    }

    static Map<String, Series> groupSeries(List<Map<String, String>> rows, String groupCol, String xCol, String yCol) {
        Map<String, Series> map = new LinkedHashMap<>();
        for (Map<String, String> r : rows) {
            String key = r.get(groupCol);
            double x = Double.parseDouble(r.get(xCol));
            double y = Double.parseDouble(r.get(yCol));
            map.computeIfAbsent(key, Series::new).add(x, y);
        }
        for (Series s : map.values()) s.sortByX();
        return map;
    }

    static List<Map<String, String>> readCsv(String path) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        List<String> lines = Files.readAllLines(new File(path).toPath());
        if (lines.isEmpty()) return rows;
        String[] header = lines.get(0).split(",");
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;
            String[] parts = lines.get(i).split(",");
            Map<String, String> row = new LinkedHashMap<>();
            for (int c = 0; c < header.length && c < parts.length; c++) {
                row.put(header[c].trim(), parts[c].trim());
            }
            rows.add(row);
        }
        return rows;
    }

    static final Color[] PALETTE = {
            new Color(0x1f77b4), new Color(0xd62728), new Color(0x2ca02c),
            new Color(0x9467bd), new Color(0xff7f0e), new Color(0x17becf)
    };

    static void renderLineChart(String outPath, String title, String xLabel, String yLabel,
                                 boolean logX, boolean logY, Map<String, Series> series) throws IOException {
        int width = 900, height = 600;
        int marginLeft = 90, marginRight = 220, marginTop = 60, marginBottom = 70;
        int plotW = width - marginLeft - marginRight;
        int plotH = height - marginTop - marginBottom;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        
        double xMin = Double.MAX_VALUE, xMax = -Double.MAX_VALUE;
        double yMin = Double.MAX_VALUE, yMax = -Double.MAX_VALUE;
        for (Series s : series.values()) {
            for (double[] p : s.points) {
                double xv = logX ? Math.log10(Math.max(p[0], 1e-9)) : p[0];
                double yv = logY ? Math.log10(Math.max(p[1], 1e-9)) : p[1];
                xMin = Math.min(xMin, xv); xMax = Math.max(xMax, xv);
                yMin = Math.min(yMin, yv); yMax = Math.max(yMax, yv);
            }
        }
        if (xMin == xMax) { xMin -= 1; xMax += 1; }
        if (yMin == yMax) { yMin -= 1; yMax += 1; }
        double yPad = (yMax - yMin) * 0.1;
        yMin -= yPad; yMax += yPad;

       
        g.setColor(Color.BLACK);
        g.drawLine(marginLeft, marginTop, marginLeft, marginTop + plotH);
        g.drawLine(marginLeft, marginTop + plotH, marginLeft + plotW, marginTop + plotH);

     
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fmTitle = g.getFontMetrics();
        g.drawString(title, (width - fmTitle.stringWidth(title)) / 2, 30);

      
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(xLabel, marginLeft + plotW / 2 - fm.stringWidth(xLabel) / 2, height - 20);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(-Math.PI / 2);
        g2.drawString(yLabel, -(marginTop + plotH / 2 + fm.stringWidth(yLabel) / 2), 25);
        g2.dispose();

       
        TreeSet<Double> xValues = new TreeSet<>();
        for (Series s : series.values()) for (double[] p : s.points) xValues.add(p[0]);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (double xv : xValues) {
            double xt = logX ? Math.log10(Math.max(xv, 1e-9)) : xv;
            int px = marginLeft + (int) ((xt - xMin) / (xMax - xMin) * plotW);
            g.setColor(new Color(230, 230, 230));
            g.drawLine(px, marginTop, px, marginTop + plotH);
            g.setColor(Color.BLACK);
            g.drawLine(px, marginTop + plotH, px, marginTop + plotH + 5);
            String label = formatNumber(xv);
            int lw = g.getFontMetrics().stringWidth(label);
            g.drawString(label, px - lw / 2, marginTop + plotH + 20);
        }

        // Y ticks: 6 evenly spaced ticks
        int yTicks = 6;
        for (int i = 0; i <= yTicks; i++) {
            double yt = yMin + (yMax - yMin) * i / yTicks;
            int py = marginTop + plotH - (int) ((yt - yMin) / (yMax - yMin) * plotH);
            g.setColor(new Color(230, 230, 230));
            g.drawLine(marginLeft, py, marginLeft + plotW, py);
            g.setColor(Color.BLACK);
            g.drawLine(marginLeft - 5, py, marginLeft, py);
            double realY = logY ? Math.pow(10, yt) : yt;
            String label = formatNumber(realY);
            int lw = g.getFontMetrics().stringWidth(label);
            g.drawString(label, marginLeft - 10 - lw, py + 4);
        }

        
        int colorIdx = 0;
        int legendY = marginTop;
        for (Series s : series.values()) {
            Color color = PALETTE[colorIdx % PALETTE.length];
            g.setColor(color);
            g.setStroke(new BasicStroke(2.2f));

            Integer prevPx = null, prevPy = null;
            for (double[] p : s.points) {
                double xt = logX ? Math.log10(Math.max(p[0], 1e-9)) : p[0];
                double yt = logY ? Math.log10(Math.max(p[1], 1e-9)) : p[1];
                int px = marginLeft + (int) ((xt - xMin) / (xMax - xMin) * plotW);
                int py = marginTop + plotH - (int) ((yt - yMin) / (yMax - yMin) * plotH);
                if (prevPx != null) {
                    g.draw(new Line2D.Double(prevPx, prevPy, px, py));
                }
                g.fill(new Ellipse2D.Double(px - 3.5, py - 3.5, 7, 7));
                prevPx = px; prevPy = py;
            }

            
            g.setColor(color);
            g.fillRect(marginLeft + plotW + 20, legendY, 14, 14);
            g.setColor(Color.BLACK);
            g.drawString(s.name, marginLeft + plotW + 40, legendY + 12);
            legendY += 24;

            colorIdx++;
        }

        g.dispose();
        ImageIO.write(img, "png", new File(outPath));
        System.out.println("wrote " + outPath);
    }

    static String formatNumber(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v) && Math.abs(v) < 1_000_000) {
            long lv = (long) v;
            if (Math.abs(lv) >= 1000) return String.format("%,d", lv);
            return String.valueOf(lv);
        }
        if (Math.abs(v) >= 1000) return String.format("%,.0f", v);
        if (Math.abs(v) >= 1) return String.format("%.2f", v);
        return String.format("%.4f", v);
    }
}