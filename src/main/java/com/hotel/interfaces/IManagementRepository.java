package com.hotel.interfaces;

import com.hotel.entity.ManagerDashboard;
import com.hotel.entity.ManagementReport;
import java.time.LocalDate;

public interface IManagementRepository {
    ManagerDashboard loadDashboard(LocalDate businessDate);
    ManagementReport loadReport(LocalDate from, LocalDate to);
}
