package com.hotel.repository;

import com.hotel.entity.MaintenanceTicket;
import com.hotel.interfaces.IMaintenanceRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** F19 - reported issue, assignment, repair and Manager close/reopen workflow. */
public class MaintenanceRepository extends BaseRepository implements IMaintenanceRepository {
    private static final String SELECT = "SELECT m.*,r.room_number,u.full_name AS staff_name "
            + "FROM maintenance_tickets m JOIN rooms r ON r.room_id=m.room_id "
            + "LEFT JOIN users u ON u.user_id=m.assigned_staff_user_id ";

    private MaintenanceTicket map(ResultSet rs) throws SQLException {
        MaintenanceTicket t=new MaintenanceTicket();
        t.setMaintenanceTicketId(rs.getLong("maintenance_ticket_id"));t.setRoomId(rs.getLong("room_id"));
        t.setReportedByUserId(rs.getLong("reported_by_user_id"));t.setAssignedStaffUserId(longOf(rs,"assigned_staff_user_id"));
        t.setTicketCode(rs.getString("ticket_code"));t.setTitle(rs.getString("title"));t.setDescription(rs.getString("description"));
        t.setPriorityCode(rs.getString("priority_code"));t.setStatusCode(rs.getString("status_code"));
        t.setReportedAt(tsOf(rs,"reported_at"));t.setStartedAt(tsOf(rs,"started_at"));t.setResolvedAt(tsOf(rs,"resolved_at"));
        t.setClosedAt(tsOf(rs,"closed_at"));t.setResolutionNote(rs.getString("resolution_note"));t.setUpdatedAt(tsOf(rs,"updated_at"));
        t.setRoomNumber(rs.getString("room_number"));t.setStaffName(rs.getString("staff_name"));return t;
    }

    @Override
    public List<MaintenanceTicket> findAll(String status,String priority,Long roomId,Long staffId){
        StringBuilder sql=new StringBuilder(SELECT+"WHERE 1=1 ");List<Object> params=new ArrayList<>();
        if(status!=null&&!status.isBlank()){sql.append("AND m.status_code=? ");params.add(status);}
        if(priority!=null&&!priority.isBlank()){sql.append("AND m.priority_code=? ");params.add(priority);}
        if(roomId!=null){sql.append("AND m.room_id=? ");params.add(roomId);}
        if(staffId!=null){sql.append("AND m.assigned_staff_user_id=? ");params.add(staffId);}
        sql.append("ORDER BY CASE m.priority_code WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'NORMAL' THEN 3 ELSE 4 END,m.reported_at DESC");
        try(Connection cn=getConnection();PreparedStatement ps=cn.prepareStatement(sql.toString())){
            for(int i=0;i<params.size();i++)ps.setObject(i+1,params.get(i));try(ResultSet rs=ps.executeQuery()){List<MaintenanceTicket> list=new ArrayList<>();while(rs.next())list.add(map(rs));return list;}
        }catch(SQLException e){throw wrap(e);}
    }

    @Override
    public MaintenanceTicket findById(long id){
        try(Connection cn=getConnection();PreparedStatement ps=cn.prepareStatement(SELECT+"WHERE m.maintenance_ticket_id=?")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?map(rs):null;}}catch(SQLException e){throw wrap(e);}
    }

    @Override
    public long insert(MaintenanceTicket t){
        String sql="INSERT INTO maintenance_tickets (room_id,reported_by_user_id,assigned_staff_user_id,ticket_code,title,description,priority_code,status_code) VALUES (?,?,?,?,?,?,?,?)";
        try(Connection cn=getConnection()){
            cn.setAutoCommit(false);try{
                long id;try(PreparedStatement ps=cn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
                    ps.setLong(1,t.getRoomId());ps.setLong(2,t.getReportedByUserId());bindLong(ps,3,t.getAssignedStaffUserId());ps.setString(4,t.getTicketCode());ps.setString(5,t.getTitle());ps.setString(6,t.getDescription());ps.setString(7,t.getPriorityCode());ps.setString(8,t.getAssignedStaffUserId()==null?"OPEN":"ASSIGNED");ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){rs.next();id=rs.getLong(1);}
                }
                try(PreparedStatement ps=cn.prepareStatement("UPDATE r SET operational_status=CASE WHEN rt.is_active=1 THEN 'MAINTENANCE' ELSE 'OUT_OF_SERVICE' END,updated_at=SYSUTCDATETIME() FROM rooms r JOIN room_types rt ON rt.room_type_id=r.room_type_id WHERE r.room_id=? AND r.operational_status<>'OCCUPIED'")){ps.setLong(1,t.getRoomId());ps.executeUpdate();}
                cn.commit();return id;
            }catch(SQLException|RuntimeException e){cn.rollback();throw e;}finally{cn.setAutoCommit(true);}
        }catch(SQLException e){throw wrap(e);}
    }

    @Override
    public void assign(long id,String priority,long staffId){
        update("UPDATE maintenance_tickets SET priority_code=?,assigned_staff_user_id=?,status_code='ASSIGNED',started_at=NULL,resolved_at=NULL,closed_at=NULL,updated_at=SYSUTCDATETIME() WHERE maintenance_ticket_id=? AND status_code IN ('OPEN','ASSIGNED','IN_PROGRESS','RESOLVED')",priority,staffId,id,"Issue không thể phân công");
    }

    @Override
    public void start(long id,long staffId){
        update("UPDATE maintenance_tickets SET status_code='IN_PROGRESS',started_at=COALESCE(started_at,SYSUTCDATETIME()),updated_at=SYSUTCDATETIME() WHERE maintenance_ticket_id=? AND assigned_staff_user_id=? AND status_code IN ('ASSIGNED','IN_PROGRESS')",id,staffId,"Issue không thuộc nhân viên hoặc sai trạng thái");
    }

    @Override
    public void resolve(long id,long staffId,String note){
        update("UPDATE maintenance_tickets SET status_code='RESOLVED',resolution_note=?,resolved_at=SYSUTCDATETIME(),updated_at=SYSUTCDATETIME() WHERE maintenance_ticket_id=? AND assigned_staff_user_id=? AND status_code='IN_PROGRESS'",note,id,staffId,"Issue chưa ở trạng thái đang sửa hoặc không thuộc nhân viên");
    }

    @Override
    public void reopen(long id,Long staffId){
        String sql="UPDATE maintenance_tickets SET assigned_staff_user_id=?,status_code=?,resolved_at=NULL,closed_at=NULL,updated_at=SYSUTCDATETIME() OUTPUT inserted.room_id WHERE maintenance_ticket_id=? AND status_code IN ('RESOLVED','CLOSED')";
        try(Connection cn=getConnection()){
            cn.setAutoCommit(false);try{
                long roomId;try(PreparedStatement ps=cn.prepareStatement(sql)){bindLong(ps,1,staffId);ps.setString(2,staffId==null?"OPEN":"ASSIGNED");ps.setLong(3,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalStateException("Chỉ issue RESOLVED/CLOSED mới mở lại được");roomId=rs.getLong(1);}}
                try(PreparedStatement ps=cn.prepareStatement("UPDATE r SET operational_status=CASE WHEN r.operational_status='OCCUPIED' THEN 'OCCUPIED' WHEN rt.is_active=1 THEN 'MAINTENANCE' ELSE 'OUT_OF_SERVICE' END,updated_at=SYSUTCDATETIME() FROM rooms r JOIN room_types rt ON rt.room_type_id=r.room_type_id WHERE r.room_id=?")){ps.setLong(1,roomId);ps.executeUpdate();}
                cn.commit();
            }catch(SQLException|RuntimeException e){cn.rollback();throw e;}finally{cn.setAutoCommit(true);}
        }catch(SQLException e){throw wrap(e);}
    }

    @Override
    public void close(long id){
        try(Connection cn=getConnection()){
            cn.setAutoCommit(false);try{
                long roomId;try(PreparedStatement ps=cn.prepareStatement("SELECT room_id FROM maintenance_tickets WITH (UPDLOCK) WHERE maintenance_ticket_id=? AND status_code='RESOLVED'")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalStateException("Issue phải RESOLVED trước khi đóng");roomId=rs.getLong(1);}}
                try(PreparedStatement ps=cn.prepareStatement("UPDATE maintenance_tickets SET status_code='CLOSED',closed_at=SYSUTCDATETIME(),updated_at=SYSUTCDATETIME() WHERE maintenance_ticket_id=?")){ps.setLong(1,id);ps.executeUpdate();}
                int open;String clean;String currentOperation;boolean typeActive;try(PreparedStatement ps=cn.prepareStatement("SELECT (SELECT COUNT(*) FROM maintenance_tickets WHERE room_id=? AND status_code NOT IN ('CLOSED','CANCELLED')) AS open_count,r.cleaning_status,r.operational_status,rt.is_active type_active FROM rooms r JOIN room_types rt ON rt.room_type_id=r.room_type_id WHERE r.room_id=?")){ps.setLong(1,roomId);ps.setLong(2,roomId);try(ResultSet rs=ps.executeQuery()){rs.next();open=rs.getInt("open_count");clean=rs.getString("cleaning_status");currentOperation=rs.getString("operational_status");typeActive=rs.getBoolean("type_active");}}
                String op="OCCUPIED".equals(currentOperation)?"OCCUPIED":!typeActive?"OUT_OF_SERVICE":open==0&&("READY".equals(clean)||"INSPECTED".equals(clean))?"AVAILABLE":"MAINTENANCE";
                try(PreparedStatement ps=cn.prepareStatement("UPDATE rooms SET operational_status=?,updated_at=SYSUTCDATETIME() WHERE room_id=?")){ps.setString(1,op);ps.setLong(2,roomId);ps.executeUpdate();}
                cn.commit();
            }catch(SQLException|RuntimeException e){cn.rollback();throw e;}finally{cn.setAutoCommit(true);}
        }catch(SQLException e){throw wrap(e);}
    }

    private void update(String sql,Object... params){
        String message=(String)params[params.length-1];
        try(Connection cn=getConnection();PreparedStatement ps=cn.prepareStatement(sql)){
            for(int i=0;i<params.length-1;i++)ps.setObject(i+1,params[i]);if(ps.executeUpdate()==0)throw new IllegalStateException(message);
        }catch(SQLException e){throw wrap(e);}
    }
}
