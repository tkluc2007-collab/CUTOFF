import java.lang.management.ManagementFactory;

/** Emits portable machine facts used by scripts/run_all.sh. */
public final class MachineInfo {
    private MachineInfo() { }

    public static void main(String[] args) {
        var os = ManagementFactory.getOperatingSystemMXBean();
        String cpu = System.getenv("PROCESSOR_IDENTIFIER");
        if (cpu == null || cpu.isBlank()) cpu = System.getProperty("os.arch") + " (model unavailable)";
        long ram = os instanceof com.sun.management.OperatingSystemMXBean extended
                ? extended.getTotalMemorySize() : -1;
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("CPU: " + cpu);
        System.out.println("Cores: " + os.getAvailableProcessors());
        System.out.println("RAM: " + (ram < 0 ? "unavailable" : ram + " bytes"));
        System.out.println("Java: " + System.getProperty("java.runtime.version"));
    }
}
