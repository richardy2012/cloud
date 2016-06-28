package com.csf.cloud.resource;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.StringTokenizer;

import com.csf.cloud.util.JavaLogging;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;

/**
 * Created by soledede.weng on 2016/6/2.
 * <p>
 * ResourseTool
 * cpu used , memeory used info
 */
public class ResourseTool {

    private static Logger log = null;

    private static long lastIdleCpuTime = 0;
    private static long lastTotalCpuTime = 0;

    private static String osName = System.getProperty("os.name");

    private static int cpuCores = Runtime.getRuntime().availableProcessors();


    private static final int CPUTIME = 30;


    private static final int FAULTLENGTH = 10;

    private static String linuxVersion = null;


    static {
        log = JavaLogging.log();
        Process process = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader brStat = null;
        try {
            process = Runtime.getRuntime().exec("cat /proc/version");
            is = process.getInputStream();
            isr = new InputStreamReader(is);
            brStat = new BufferedReader(isr);
            linuxVersion = ResMonitorInfo.getLinuxVersion(brStat.readLine());
        } catch (IOException e) {
            linuxVersion = "";
        } finally {
            freeResource(is, isr, brStat);
        }

    }


    public static ResMonitorInfo getResMonitorInfo() {


        int kb = 1024;

        // Available memory
        long totalMemory = Runtime.getRuntime().totalMemory() / kb;
        // Free memory
        long freeMemory = Runtime.getRuntime().freeMemory() / kb;
        // Maximal available memory
        long maxMemory = Runtime.getRuntime().maxMemory() / kb;

        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Operating system (OS)
        String osName = System.getProperty("os.name");
        // Total physical memory
        long totalMemorySize = osmxb.getTotalPhysicalMemorySize() / kb;
        // Free physical memory
        long freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize() / kb;
        // Usage physical memory
        long usedMemory = (osmxb.getTotalPhysicalMemorySize() - osmxb
                .getFreePhysicalMemorySize())
                / kb;
        double memRatio = (double) usedMemory / (double) totalMemorySize;

        // Total thread active counts
        ThreadGroup parentThread;
        int totalThread = 0;
        double cpuRatio = 0;
        try {
            for (parentThread = Thread.currentThread().getThreadGroup(); parentThread.getParent() != null; parentThread = parentThread.getParent())
                ;
            totalThread = parentThread.activeCount();


            if (osName.toLowerCase().startsWith("windows")) {
                cpuRatio = getCpuRatioForWindows();
            } else {
                cpuRatio = getCpuRateForLinux();
            }

        } catch (Exception e) {
            log.error("read cpu or memory info failed!", e.getCause());
        }
        return new ResMonitorInfo(totalMemory, freeMemory, maxMemory,
                osName, totalMemorySize, freePhysicalMemorySize, usedMemory,
                totalThread, memRatio, cpuRatio
        );
    }

    private static double getCpuRateForLinux() {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader brStat = null;
        StringTokenizer tokenStat = null;
        try {
            System.out.println("Get usage rate of CUP , linux version: " + linuxVersion);

            Process process = Runtime.getRuntime().exec("top -b -n 1");
            is = process.getInputStream();
            isr = new InputStreamReader(is);
            brStat = new BufferedReader(isr);

            if (linuxVersion.equals("2.4")) {
                brStat.readLine();
                brStat.readLine();
                brStat.readLine();
                brStat.readLine();

                tokenStat = new StringTokenizer(brStat.readLine());
                tokenStat.nextToken();
                tokenStat.nextToken();
                String user = tokenStat.nextToken();
                tokenStat.nextToken();
                String system = tokenStat.nextToken();
                tokenStat.nextToken();
                String nice = tokenStat.nextToken();

                System.out.println(user + " , " + system + " , " + nice);

                user = user.substring(0, user.indexOf("%"));
                system = system.substring(0, system.indexOf("%"));
                nice = nice.substring(0, nice.indexOf("%"));

                float userUsage = Float.parseFloat(user);
                float systemUsage = Float.parseFloat(system);
                float niceUsage = Float.parseFloat(nice);

                return (userUsage + systemUsage + niceUsage) / 100;
            } else {
                brStat.readLine();
                brStat.readLine();

                tokenStat = new StringTokenizer(brStat.readLine());
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                String cpuIdle = tokenStat.nextToken();


                System.out.println("CPU idle : " + cpuIdle);
                Float idle = new Float(cpuIdle.substring(0, cpuIdle.indexOf("%")));

                return (1 - idle / 100);
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            freeResource(is, isr, brStat);
            return 1;
        } finally {
            freeResource(is, isr, brStat);
        }
    }

    private static void freeResource(InputStream is, InputStreamReader isr, BufferedReader br) {
        try {
            if (is != null)
                is.close();
            if (isr != null)
                isr.close();
            if (br != null)
                br.close();
        } catch (IOException ioe) {
            log.error("close stream failed!", ioe.getMessage());
        }
    }


    /**
     * Get cpu ratio.
     *
     * @return
     */
    private static double getCpuRatioForWindows() {
        try {
            String procCmd = System.getenv("windir")
                    + "\\system32\\wbem\\wmic.exe process get Caption,CommandLine,"
                    + "KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
            long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));
            Thread.sleep(CPUTIME);
            long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
            if (c0 != null && c1 != null) {
                long idletime = c1[0] - c0[0];
                long busytime = c1[1] - c0[1];
                return (double) (busytime) / (double) (busytime + idletime);
            } else {
                return 0.0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0;
        }
    }

    /**
     * @return cpu rate
     */
    private static long[] readCpu(final Process proc) {
        long[] retn = new long[2];
        try {
            proc.getOutputStream().close();
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line = input.readLine();
            if (line == null || line.length() <= FAULTLENGTH) {
                return null;
            }
            int capidx = line.indexOf("Caption");
            int cmdidx = line.indexOf("CommandLine");
            int rocidx = line.indexOf("ReadOperationCount");
            int umtidx = line.indexOf("UserModeTime");
            int kmtidx = line.indexOf("KernelModeTime");
            int wocidx = line.indexOf("WriteOperationCount");
            long idletime = 0;
            long kneltime = 0;
            long usertime = 0;
            while ((line = input.readLine()) != null) {
                if (line.length() < wocidx) {
                    continue;
                }
                // Order field：Caption,CommandLine,KernelModeTime,ReadOperationCount,
                // ThreadCount,UserModeTime,WriteOperation
                String caption = Bytes.substring(line, capidx, cmdidx - 1)
                        .trim();
                String cmd = Bytes.substring(line, cmdidx, kmtidx - 1).trim();
                if (cmd.contains("wmic.exe")) {
                    continue;
                }
                if (caption.equals("System Idle Process") || caption.equals("System")) {
                    String idl = Bytes.substring(line, kmtidx, rocidx - 1).trim();
                    String id2 = Bytes.substring(line, umtidx, wocidx - 1).trim();
                    if (isNumeric(idl))
                        idletime += Long.valueOf(idl);
                    if (isNumeric(id2))
                        idletime += Long.valueOf(id2);
                    continue;
                }

                String knel1 = Bytes.substring(line, kmtidx, rocidx - 1).trim();
                String knel2 = Bytes.substring(line, umtidx, wocidx - 1).trim();
                if (isNumeric(knel1))
                    kneltime += Long.valueOf(knel1);
                if (isNumeric(knel2))
                    usertime += Long.valueOf(knel2);
            }
            retn[0] = idletime;
            retn[1] = kneltime + usertime;
            return retn;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                proc.getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static MemoryInfo getMemInfo() throws IOException,
            InterruptedException {
        MemoryInfo memInfo = new MemoryInfo();
        if (!osName.toLowerCase().startsWith("windows")) {
            long totalMem = 0;
            long freeMem = 0;


            Process pro = null;
            Runtime r = Runtime.getRuntime();

            String command = "cat /proc/meminfo";
            pro = r.exec(command);

            int count = 0;
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    pro.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] memInfos = line.split("\\s+");
                if (memInfos[0].toLowerCase().startsWith("MemTotal".toLowerCase())) {
                    totalMem = Long.valueOf(memInfos[1]).longValue();
                    memInfo.memTotal_$eq(totalMem);
                    count++;
                } else if (memInfos[0].toLowerCase().startsWith(
                        "MemFree".toLowerCase())) {
                    freeMem = Long.valueOf(memInfos[1]).longValue();
                    memInfo.memFree_$eq(freeMem);
                    count++;
                } else if (memInfos[0].toLowerCase().startsWith(
                        "SwapTotal".toLowerCase())) {
                    memInfo.swapFree_$eq(Long.valueOf(memInfos[1]).longValue());
                    count++;
                } else if (memInfos[0].toLowerCase().startsWith(
                        "SwapFree".toLowerCase())) {
                    memInfo.swapFree_$eq(Long.valueOf(memInfos[1]).longValue());
                    count++;
                }
                if (count == 4) {
                    memInfo.memUsage_$eq(1 - (double) freeMem
                            / (double) totalMem);
                    break;
                }

            }
        }
        return memInfo;

    }

    public static CpuInfo getCpuInfo() throws IOException, InterruptedException,
            RuntimeException {
        if (osName.toLowerCase().startsWith("windows")) return new CpuInfo(ResourseTool.cpuCores, 0.0);
        long[] l = getCpuInfoFromLinux();
        if (l == null)
            throw new RuntimeException();
        if (lastIdleCpuTime == 0 || lastTotalCpuTime == 0) {// first fetch
            lastIdleCpuTime = l[0];
            lastTotalCpuTime = l[1];
            Thread.sleep(1000);
            l = getCpuInfoFromLinux();
        }
        /*CpuInfo cpuInfo = new CpuInfo(ResourseTool.cpuCores, (double) 1 - (l[0] - lastIdleCpuTime)
                / (double) (l[1] - lastTotalCpuTime));*/
        double cpuTotal = l[1] - lastTotalCpuTime;
        double cpuIdle = l[0] - lastIdleCpuTime;
        double cpuUsageRate = (cpuTotal - cpuIdle) / cpuTotal;
        CpuInfo cpuInfo = new CpuInfo(ResourseTool.cpuCores, cpuUsageRate);
        lastIdleCpuTime = l[0];
        lastTotalCpuTime = l[1];
        return cpuInfo;
    }


    private static long[] getCpuInfoFromLinux() throws IOException {
        long[] l = new long[2];
        Process pro;
        Runtime r = Runtime.getRuntime();
        String command = "cat /proc/stat";
        pro = r.exec(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                pro.getInputStream()));
        String line = null;
        long idleCpuTime = 0, totalCpuTime = 0;
        while ((line = in.readLine()) != null) {
            if (line.toLowerCase().startsWith("cpu")) {
                line = line.trim();
                String[] temp = line.split("\\s+");
                idleCpuTime += Long.valueOf(temp[4]).longValue();
                for (int i = 0; i < temp.length; i++) {
                    if (!temp[i].toLowerCase().startsWith("cpu")) {
                        totalCpuTime += Long.valueOf(temp[i]).longValue();
                    }
                }
            } else if (idleCpuTime != 0L && totalCpuTime != 0L) {
                l[0] = idleCpuTime;
                l[1] = totalCpuTime;
                break;
            }
        }
        in.close();
        pro.destroy();
        return l;
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.equalsIgnoreCase("")) return false;
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        //System.out.println(System.getProperty("os.name"));
        // System.out.println(cpuCores);
        ResMonitorInfo monitorInfo = ResourseTool.getResMonitorInfo();
        System.out.print(monitorInfo.toString());
    }

    public static void testssss() {
        System.out.println("come in....");
    }

    public static String testS() {
        return "what's";
    }
}
