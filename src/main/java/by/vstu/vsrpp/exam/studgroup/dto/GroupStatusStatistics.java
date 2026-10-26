package by.vstu.vsrpp.exam.studgroup.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class GroupStatusStatistics {

    private final int shifr;
    private final String nazvanie;
    private final Map<String, Long> statusCounts = new LinkedHashMap<>();

    public GroupStatusStatistics(int shifr, String nazvanie) {
        this.shifr = shifr;
        this.nazvanie = nazvanie;
    }

    public void addCount(String status, Long count) {
        statusCounts.merge(status, count, Long::sum);
    }

    public int getShifr() {
        return shifr;
    }

    public String getNazvanie() {
        return nazvanie;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }
}
