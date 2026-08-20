package com.hotel.repository;

import com.hotel.entity.ManagerDashboard;
import com.hotel.entity.ManagementReport;
import com.hotel.interfaces.IManagementRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/** F23/F24 - live, auditable management aggregates. */
public class ManagementRepository extends BaseRepository implements IManagementRepository {
    @Override
    public ManagerDashboard loadDashboard(LocalDate date){
        ManagerDashboard d=new ManagerDashboard();
        try(Connection cn=getConnection()){
            try(PreparedStatement ps=cn.prepareStatement("SELECT SUM(CASE WHEN operational_status='OCCUPIED' THEN 1 ELSE 0 END) occupied, SUM(CASE WHEN is_active=1 AND operational_status IN ('AVAILABLE','OCCUPIED') THEN 1 ELSE 0 END) operational FROM rooms");ResultSet rs=ps.executeQuery()){rs.next();d.setOccupiedRooms(rs.getInt("occupied"));d.setOperationalRooms(rs.getInt("operational"));}
            d.setArrivals(count(cn,"SELECT COUNT(*) FROM reservations WHERE check_in_date=? AND status_code IN ('CONFIRMED','CHECKED_IN')",date));
            d.setDepartures(count(cn,"SELECT COUNT(*) FROM reservations WHERE check_out_date=? AND status_code IN ('CONFIRMED','CHECKED_IN','CHECKED_OUT')",date));
            d.setNewReservations(count(cn,"SELECT COUNT(*) FROM reservations WHERE CAST(booked_at AS date)=?",date));
            try(PreparedStatement ps=cn.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM payments WHERE status_code='SUCCESS' AND CAST(paid_at AS date)=?")){ps.setDate(1,Date.valueOf(date));try(ResultSet rs=ps.executeQuery()){rs.next();d.setRevenue(rs.getBigDecimal(1));}}
            d.setPendingHousekeepingTasks(count(cn,"SELECT COUNT(*) FROM housekeeping_tasks WHERE status_code IN ('PENDING','ASSIGNED','IN_PROGRESS')",null));
            d.setUnresolvedMaintenanceIssues(count(cn,"SELECT COUNT(*) FROM maintenance_tickets WHERE status_code NOT IN ('CLOSED','CANCELLED')",null));
            return d;
        }catch(SQLException e){throw wrap(e);}
    }

    @Override
    public ManagementReport loadReport(LocalDate from,LocalDate to){
        ManagementReport r=new ManagementReport();r.setFromDate(from);r.setToDate(to);
        try(Connection cn=getConnection()){
            String booked="SELECT COALESCE(SUM(rr.quantity * DATEDIFF(day, CASE WHEN res.check_in_date < ? THEN ? ELSE res.check_in_date END, CASE WHEN res.check_out_date > DATEADD(day,1,?) THEN DATEADD(day,1,?) ELSE res.check_out_date END)),0) FROM reservations res JOIN reservation_rooms rr ON rr.reservation_id=res.reservation_id WHERE res.status_code IN ('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT') AND res.check_in_date<=? AND res.check_out_date>?";
            try(PreparedStatement ps=cn.prepareStatement(booked)){ps.setDate(1,Date.valueOf(from));ps.setDate(2,Date.valueOf(from));ps.setDate(3,Date.valueOf(to));ps.setDate(4,Date.valueOf(to));ps.setDate(5,Date.valueOf(to));ps.setDate(6,Date.valueOf(from));try(ResultSet rs=ps.executeQuery()){rs.next();r.setBookedRoomNights(rs.getInt(1));}}
            String reservation="SELECT COUNT(*) total,SUM(CASE WHEN status_code='CANCELLED' THEN 1 ELSE 0 END) cancelled,COALESCE(SUM(CASE WHEN status_code<>'CANCELLED' THEN total_amount ELSE 0 END),0) revenue FROM reservations WHERE CAST(booked_at AS date) BETWEEN ? AND ?";
            try(PreparedStatement ps=cn.prepareStatement(reservation)){bindRange(ps,from,to);try(ResultSet rs=ps.executeQuery()){rs.next();r.setReservations(rs.getInt("total"));r.setCancellations(rs.getInt("cancelled"));r.setReservationRevenue(rs.getBigDecimal("revenue"));}}
            String payments="SELECT COUNT(*) total,COALESCE(SUM(amount),0) amount FROM payments WHERE status_code='SUCCESS' AND CAST(paid_at AS date) BETWEEN ? AND ?";
            try(PreparedStatement ps=cn.prepareStatement(payments)){bindRange(ps,from,to);try(ResultSet rs=ps.executeQuery()){rs.next();r.setPaymentTransactions(rs.getInt("total"));r.setSuccessfulPayments(rs.getBigDecimal("amount"));}}
            String services="SELECT COUNT(*) total,SUM(CASE WHEN status_code='COMPLETED' THEN 1 ELSE 0 END) completed FROM service_requests WHERE CAST(requested_at AS date) BETWEEN ? AND ?";
            try(PreparedStatement ps=cn.prepareStatement(services)){bindRange(ps,from,to);try(ResultSet rs=ps.executeQuery()){rs.next();r.setServiceRequests(rs.getInt("total"));r.setCompletedServices(rs.getInt("completed"));}}
            if(r.getReservationRevenue()==null)r.setReservationRevenue(BigDecimal.ZERO);if(r.getSuccessfulPayments()==null)r.setSuccessfulPayments(BigDecimal.ZERO);return r;
        }catch(SQLException e){throw wrap(e);}
    }
    private int count(Connection cn,String sql,LocalDate date)throws SQLException{try(PreparedStatement ps=cn.prepareStatement(sql)){if(date!=null)ps.setDate(1,Date.valueOf(date));try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}}}
    private void bindRange(PreparedStatement ps,LocalDate from,LocalDate to)throws SQLException{ps.setDate(1,Date.valueOf(from));ps.setDate(2,Date.valueOf(to));}
}
