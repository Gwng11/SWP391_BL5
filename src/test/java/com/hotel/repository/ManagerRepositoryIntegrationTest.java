package com.hotel.repository;

import com.hotel.dal.DBContext;
import com.hotel.entity.HousekeepingTask;
import com.hotel.entity.MaintenanceTicket;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.service.ManagerService;
import com.hotel.ultis.Constants;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="HMS_IT",matches="1")
class ManagerRepositoryIntegrationTest {
    private long roomTypeId;private long readyRoomId;private long blockedRoomId;private long outRoomId;private long occupiedRoomId;private long reservationId;private long managerId;private long staffId;private ManagerService service;

    @BeforeEach void setUp()throws Exception{
        managerId=userId("manager.demo@hotel.vn");staffId=userId("staff@hotel.vn");String code="IT"+Long.toString(System.nanoTime(),36).toUpperCase();
        try(Connection cn=new DBContext().getConnection()){
            roomTypeId=insert(cn,"INSERT INTO room_types(type_code,type_name,max_adults,max_children,base_price,amenities_json,images_json) VALUES (?, ?,2,1,100000,'[]','[]')",code,"Integration "+code);
            readyRoomId=insertRoom(cn,code+"A","AVAILABLE","READY");blockedRoomId=insertRoom(cn,code+"B","BLOCKED","READY");outRoomId=insertRoom(cn,code+"C","OUT_OF_SERVICE","READY");occupiedRoomId=insertRoom(cn,code+"D","OCCUPIED","READY");reservationId=insertCheckedInStay(cn,code);
        }
        service=new ManagerService();
    }

    @AfterEach void tearDown()throws Exception{
        try(Connection cn=new DBContext().getConnection()){
            try(PreparedStatement ps=cn.prepareStatement("DELETE FROM housekeeping_tasks WHERE room_id IN (?,?,?,?)")){ids(ps);ps.executeUpdate();}
            try(PreparedStatement ps=cn.prepareStatement("DELETE FROM maintenance_tickets WHERE room_id IN (?,?,?,?)")){ids(ps);ps.executeUpdate();}
            try(PreparedStatement ps=cn.prepareStatement("DELETE FROM room_assignments WHERE room_id IN (?,?,?,?)")){ids(ps);ps.executeUpdate();}
            try(PreparedStatement ps=cn.prepareStatement("DELETE FROM reservation_rooms WHERE reservation_id=?")){ps.setLong(1,reservationId);ps.executeUpdate();}
            try(PreparedStatement ps=cn.prepareStatement("DELETE FROM reservations WHERE reservation_id=?")){ps.setLong(1,reservationId);ps.executeUpdate();}
            try(PreparedStatement ps=cn.prepareStatement("DELETE FROM room_rates WHERE room_type_id=?")){ps.setLong(1,roomTypeId);ps.executeUpdate();}
            try(PreparedStatement ps=cn.prepareStatement("DELETE FROM rooms WHERE room_type_id=?")){ps.setLong(1,roomTypeId);ps.executeUpdate();}
            try(PreparedStatement ps=cn.prepareStatement("DELETE FROM room_types WHERE room_type_id=?")){ps.setLong(1,roomTypeId);ps.executeUpdate();}
        }
    }

    @Test void onlyAvailableInventoryCanBeSearchedOrAssigned(){RoomRepository repo=new RoomRepository();assertEquals(1,repo.countSellableByType(roomTypeId));assertEquals(1,repo.findAssignableRooms(roomTypeId).size());}

    @Test void deactivatingTypeForcesUnoccupiedRoomsOutOfServiceButKeepsOccupiedRoom(){service.setRoomTypeActive(roomTypeId,false);assertEquals("OUT_OF_SERVICE",service.getRoom(readyRoomId).getOperationalStatus());assertEquals("OUT_OF_SERVICE",service.getRoom(blockedRoomId).getOperationalStatus());assertEquals("OUT_OF_SERVICE",service.getRoom(outRoomId).getOperationalStatus());assertEquals("OCCUPIED",service.getRoom(occupiedRoomId).getOperationalStatus());assertThrows(IllegalStateException.class,()->service.changeOperationalStatus(readyRoomId,"AVAILABLE"));assertTrue(new RoomTypeRepository().findAllActive().stream().noneMatch(t->t.getRoomTypeId()==roomTypeId));}

    @Test void checkoutAfterTypeDeactivationMovesOccupiedRoomOutOfService(){service.setRoomTypeActive(roomTypeId,false);new RoomAssignmentRepository().releaseAllForReservation(reservationId,"Checked out");Room room=service.getRoom(occupiedRoomId);assertEquals("OUT_OF_SERVICE",room.getOperationalStatus());assertFalse(room.isActive());}

    @Test void pricingAmenitiesAndImagesPersist(){service.addAmenity(roomTypeId,"WiFi");service.addImage(roomTypeId,"https://img.test/room.jpg");LocalDate from=LocalDate.now().plusDays(1);service.updatePricing(roomTypeId,new BigDecimal("125000"),from,from.plusDays(2),new BigDecimal("150000"),true);RoomType type=service.getRoomType(roomTypeId);assertEquals("[\"WiFi\"]",type.getAmenitiesJson());assertEquals("[\"https://img.test/room.jpg\"]",type.getImagesJson());assertEquals(new BigDecimal("125000.00"),type.getBasePrice());assertEquals(3,service.getRates(roomTypeId,from,from.plusDays(2)).size());}

    @Test void inspectionPassMakesRoomReadyAndFailureCreatesMaintenanceIssue(){
        setCleaning(readyRoomId,"DIRTY");long pass=service.createHousekeepingTask(readyRoomId,null,staffId,"CHECKOUT_CLEANING","NORMAL",null,null,managerId);service.startHousekeeping(pass,staffId);service.completeCleaning(pass,staffId,"cleaned");service.inspectHousekeeping(pass,staffId,true,"ok");Room passed=service.getRoom(readyRoomId);assertEquals("READY",passed.getCleaningStatus());assertEquals("AVAILABLE",passed.getOperationalStatus());
        setCleaning(blockedRoomId,"DIRTY");service.changeOperationalStatus(blockedRoomId,"AVAILABLE");long fail=service.createHousekeepingTask(blockedRoomId,null,staffId,"DEEP_CLEANING","HIGH",null,null,managerId);service.startHousekeeping(fail,staffId);service.completeCleaning(fail,staffId,"cleaned");service.inspectHousekeeping(fail,staffId,false,"Air conditioner leaking");assertEquals("MAINTENANCE",service.getRoom(blockedRoomId).getOperationalStatus());assertFalse(service.getMaintenanceTickets(null,null,blockedRoomId,null).isEmpty());
    }

    @Test void housekeepingListMapsTasksWithNullableFields(){long taskId=service.createHousekeepingTask(readyRoomId,null,null,"CHECKOUT_CLEANING","NORMAL",null,"mapping smoke",managerId);HousekeepingTask task=service.getHousekeepingTasks(null,readyRoomId,null).stream().filter(t->t.getHousekeepingTaskId()==taskId).findFirst().orElseThrow();assertEquals("PENDING",task.getStatusCode());assertNull(task.getAssignedStaffUserId());assertNotNull(task.getRoomNumber());}

    @Test void maintenanceCanResolveCloseAndReopen(){long ticket=service.reportMaintenance(readyRoomId,staffId,"Broken lamp","Lamp does not work","NORMAL",null);service.assignMaintenance(ticket,"HIGH",staffId);service.startMaintenance(ticket,staffId);service.resolveMaintenance(ticket,staffId,"Lamp replaced");service.closeMaintenance(ticket);MaintenanceTicket closed=service.getMaintenanceTicket(ticket);assertEquals("CLOSED",closed.getStatusCode());assertEquals("AVAILABLE",service.getRoom(readyRoomId).getOperationalStatus());service.reopenMaintenance(ticket,staffId);assertEquals("ASSIGNED",service.getMaintenanceTicket(ticket).getStatusCode());assertEquals("MAINTENANCE",service.getRoom(readyRoomId).getOperationalStatus());}

    @Test void reassigningResolvedMaintenanceClearsLifecycleTimestamps(){long ticket=service.reportMaintenance(readyRoomId,staffId,"Noisy fan","Fan needs another repair","NORMAL",null);service.assignMaintenance(ticket,"HIGH",staffId);service.startMaintenance(ticket,staffId);service.resolveMaintenance(ticket,staffId,"First repair attempt");assertNotNull(service.getMaintenanceTicket(ticket).getResolvedAt());service.assignMaintenance(ticket,"URGENT",staffId);MaintenanceTicket reassigned=service.getMaintenanceTicket(ticket);assertEquals("ASSIGNED",reassigned.getStatusCode());assertNull(reassigned.getStartedAt());assertNull(reassigned.getResolvedAt());assertNull(reassigned.getClosedAt());}

    @Test void dashboardAndEmptyReportReturnRealValues(){assertNotNull(service.getDashboard(LocalDate.now()));assertNotNull(service.getReport(LocalDate.of(2000,1,1),LocalDate.of(2000,1,2)));}

    private long userId(String email)throws Exception{try(Connection cn=new DBContext().getConnection();PreparedStatement ps=cn.prepareStatement("SELECT user_id FROM users WHERE email=?")){ps.setString(1,email);try(ResultSet rs=ps.executeQuery()){assertTrue(rs.next(),"Missing seed account "+email);return rs.getLong(1);}}}
    private long insertRoom(Connection cn,String number,String operation,String cleaning)throws Exception{try(PreparedStatement ps=cn.prepareStatement("INSERT INTO rooms(room_type_id,room_number,floor_number,operational_status,cleaning_status,is_active) VALUES (?,?,99,?,?,1)",Statement.RETURN_GENERATED_KEYS)){ps.setLong(1,roomTypeId);ps.setString(2,number);ps.setString(3,operation);ps.setString(4,cleaning);ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){rs.next();return rs.getLong(1);}}}
    private long insertCheckedInStay(Connection cn,String code)throws Exception{
        long customerId;try(PreparedStatement ps=cn.prepareStatement("SELECT TOP 1 customer_id FROM customers ORDER BY customer_id");ResultSet rs=ps.executeQuery()){assertTrue(rs.next(),"Missing customer seed");customerId=rs.getLong(1);}
        long id;try(PreparedStatement ps=cn.prepareStatement("INSERT INTO reservations(customer_id,created_by_user_id,booking_code,source_code,status_code,check_in_date,check_out_date,adult_count,child_count,room_subtotal,service_total,tax_amount,total_amount,deposit_required) VALUES (?,?,?,'RECEPTIONIST','CHECKED_IN',CAST(GETDATE() AS date),DATEADD(day,1,CAST(GETDATE() AS date)),1,0,100000,0,0,100000,0)",Statement.RETURN_GENERATED_KEYS)){ps.setLong(1,customerId);ps.setLong(2,managerId);ps.setString(3,"IT-STAY-"+code);ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){rs.next();id=rs.getLong(1);}}
        long lineId;try(PreparedStatement ps=cn.prepareStatement("INSERT INTO reservation_rooms(reservation_id,room_type_id,quantity,adult_count,child_count,nightly_price_snapshot,number_of_nights,line_total) VALUES (?,?,1,1,0,100000,1,100000)",Statement.RETURN_GENERATED_KEYS)){ps.setLong(1,id);ps.setLong(2,roomTypeId);ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){rs.next();lineId=rs.getLong(1);}}
        try(PreparedStatement ps=cn.prepareStatement("INSERT INTO room_assignments(reservation_room_id,room_id,assigned_by_user_id) VALUES (?,?,?)")){ps.setLong(1,lineId);ps.setLong(2,occupiedRoomId);ps.setLong(3,managerId);ps.executeUpdate();}
        return id;
    }
    private long insert(Connection cn,String sql,String a,String b)throws Exception{try(PreparedStatement ps=cn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){ps.setString(1,a);ps.setString(2,b);ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){rs.next();return rs.getLong(1);}}}
    private void setCleaning(long roomId,String status){new RoomRepository().updateStatus(roomId,service.getRoom(roomId).getOperationalStatus(),status);}
    private void ids(PreparedStatement ps)throws Exception{ps.setLong(1,readyRoomId);ps.setLong(2,blockedRoomId);ps.setLong(3,outRoomId);ps.setLong(4,occupiedRoomId);}
}
