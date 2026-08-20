package com.hotel.repository;

import com.hotel.entity.HousekeepingTask;
import com.hotel.interfaces.IHousekeepingRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** F17 - task assignment, staff progress and inspection transaction. */
public class HousekeepingRepository extends BaseRepository implements IHousekeepingRepository {
    private static final String SELECT = "SELECT h.*, r.room_number, u.full_name AS staff_name "
            + "FROM housekeeping_tasks h JOIN rooms r ON r.room_id=h.room_id "
            + "LEFT JOIN users u ON u.user_id=h.assigned_staff_user_id ";

    private HousekeepingTask map(ResultSet rs) throws SQLException {
        HousekeepingTask t = new HousekeepingTask();
        t.setHousekeepingTaskId(rs.getLong("housekeeping_task_id"));
        t.setRoomId(rs.getLong("room_id"));
        t.setReservationId(longOf(rs, "reservation_id"));
        t.setAssignedStaffUserId(longOf(rs, "assigned_staff_user_id"));
        t.setCreatedByUserId(longOf(rs, "created_by_user_id"));
        t.setTaskType(rs.getString("task_type"));
        t.setPriorityCode(rs.getString("priority_code"));
        t.setStatusCode(rs.getString("status_code"));
        t.setScheduledAt(tsOf(rs, "scheduled_at"));
        t.setStartedAt(tsOf(rs, "started_at"));
        t.setCompletedAt(tsOf(rs, "completed_at"));
        t.setNotes(rs.getString("notes"));
        t.setInspectionStatus(rs.getString("inspection_status"));
        t.setInspectionNotes(rs.getString("inspection_notes"));
        t.setInspectedByUserId(longOf(rs, "inspected_by_user_id"));
        t.setInspectedAt(tsOf(rs, "inspected_at"));
        t.setCreatedAt(tsOf(rs, "created_at"));
        t.setUpdatedAt(tsOf(rs, "updated_at"));
        t.setRoomNumber(rs.getString("room_number"));
        t.setStaffName(rs.getString("staff_name"));
        return t;
    }

    @Override
    public List<HousekeepingTask> findAll(String statusCode, Long roomId, Long staffUserId) {
        StringBuilder sql = new StringBuilder(SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (statusCode != null && !statusCode.isBlank()) { sql.append("AND h.status_code=? "); params.add(statusCode); }
        if (roomId != null) { sql.append("AND h.room_id=? "); params.add(roomId); }
        if (staffUserId != null) { sql.append("AND h.assigned_staff_user_id=? "); params.add(staffUserId); }
        sql.append("ORDER BY CASE h.priority_code WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'NORMAL' THEN 3 ELSE 4 END, "
                + "COALESCE(h.scheduled_at,h.created_at)");
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i=0;i<params.size();i++) ps.setObject(i+1, params.get(i));
            try (ResultSet rs=ps.executeQuery()) {
                List<HousekeepingTask> list=new ArrayList<>(); while(rs.next()) list.add(map(rs)); return list;
            }
        } catch(SQLException e){ throw wrap(e); }
    }

    @Override
    public HousekeepingTask findById(long taskId) {
        try(Connection cn=getConnection(); PreparedStatement ps=cn.prepareStatement(SELECT+"WHERE h.housekeeping_task_id=?")){
            ps.setLong(1,taskId); try(ResultSet rs=ps.executeQuery()){ return rs.next()?map(rs):null; }
        }catch(SQLException e){throw wrap(e);}
    }

    @Override
    public boolean hasActiveTask(long roomId) {
        String sql="SELECT COUNT(*) FROM housekeeping_tasks WHERE room_id=? AND status_code IN ('PENDING','ASSIGNED','IN_PROGRESS')";
        try(Connection cn=getConnection();PreparedStatement ps=cn.prepareStatement(sql)){
            ps.setLong(1,roomId);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1)>0;}
        }catch(SQLException e){throw wrap(e);}
    }

    @Override
    public long insert(HousekeepingTask task) {
        String sql="INSERT INTO housekeeping_tasks (room_id,reservation_id,assigned_staff_user_id,created_by_user_id,"
                +"task_type,priority_code,status_code,scheduled_at,notes) VALUES (?,?,?,?,?,?,?,?,?)";
        try(Connection cn=getConnection()){
            cn.setAutoCommit(false);cn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try{
                try(PreparedStatement check=cn.prepareStatement("SELECT COUNT(*) FROM housekeeping_tasks WITH (UPDLOCK,HOLDLOCK) WHERE room_id=? AND status_code IN ('PENDING','ASSIGNED','IN_PROGRESS')")){check.setLong(1,task.getRoomId());try(ResultSet rs=check.executeQuery()){rs.next();if(rs.getInt(1)>0)throw new IllegalStateException("Phòng đã có housekeeping task đang hoạt động");}}
                long id;try(PreparedStatement ps=cn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
                    ps.setLong(1,task.getRoomId()); bindLong(ps,2,task.getReservationId()); bindLong(ps,3,task.getAssignedStaffUserId());
                    bindLong(ps,4,task.getCreatedByUserId()); ps.setString(5,task.getTaskType()); ps.setString(6,task.getPriorityCode());
                    ps.setString(7,task.getAssignedStaffUserId()==null?"PENDING":"ASSIGNED"); bindTs(ps,8,task.getScheduledAt());
                    ps.setString(9,task.getNotes()); ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){rs.next();id=rs.getLong(1);}
                }
                cn.commit();return id;
            }catch(SQLException|RuntimeException e){cn.rollback();throw e;}finally{cn.setAutoCommit(true);}
        }catch(SQLException e){throw wrap(e);}
    }

    @Override
    public void assign(long taskId,long staffUserId){
        String sql="UPDATE housekeeping_tasks SET assigned_staff_user_id=?,status_code='ASSIGNED',updated_at=SYSUTCDATETIME() "
                +"WHERE housekeeping_task_id=? AND status_code IN ('PENDING','ASSIGNED','IN_PROGRESS')";
        executeTaskUpdate(sql,staffUserId,taskId,"Task không thể phân công/reassign");
    }

    @Override
    public void start(long taskId,long staffUserId){
        try(Connection cn=getConnection()){
            cn.setAutoCommit(false);try{
                long roomId=taskRoom(cn,taskId,staffUserId,true);
                try(PreparedStatement ps=cn.prepareStatement("UPDATE housekeeping_tasks SET status_code='IN_PROGRESS',started_at=COALESCE(started_at,SYSUTCDATETIME()),updated_at=SYSUTCDATETIME() WHERE housekeeping_task_id=?")){ps.setLong(1,taskId);ps.executeUpdate();}
                setRoom(cn,roomId,"CLEANING",null);cn.commit();
            }catch(SQLException|RuntimeException e){cn.rollback();throw e;}finally{cn.setAutoCommit(true);}
        }catch(SQLException e){throw wrap(e);}
    }

    @Override
    public void completeCleaning(long taskId,long staffUserId,String notes){
        try(Connection cn=getConnection()){
            cn.setAutoCommit(false);try{
                long roomId=taskRoom(cn,taskId,staffUserId,false);
                try(PreparedStatement ps=cn.prepareStatement("UPDATE housekeeping_tasks SET completed_at=SYSUTCDATETIME(),notes=COALESCE(?,notes),updated_at=SYSUTCDATETIME() WHERE housekeeping_task_id=?")){ps.setString(1,notes);ps.setLong(2,taskId);ps.executeUpdate();}
                setRoom(cn,roomId,"CLEAN",null);cn.commit();
            }catch(SQLException|RuntimeException e){cn.rollback();throw e;}finally{cn.setAutoCommit(true);}
        }catch(SQLException e){throw wrap(e);}
    }

    @Override
    public void inspect(long taskId,long staffUserId,boolean passed,String notes,String ticketCode){
        try(Connection cn=getConnection()){
            cn.setAutoCommit(false);try{
                long roomId;
                try(PreparedStatement ps=cn.prepareStatement("SELECT room_id FROM housekeeping_tasks WITH (UPDLOCK) WHERE housekeeping_task_id=? AND assigned_staff_user_id=? AND status_code='IN_PROGRESS' AND completed_at IS NOT NULL")){
                    ps.setLong(1,taskId);ps.setLong(2,staffUserId);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalStateException("Task chưa hoàn tất cleaning hoặc không thuộc nhân viên này");roomId=rs.getLong(1);}
                }
                try(PreparedStatement ps=cn.prepareStatement("UPDATE housekeeping_tasks SET status_code='COMPLETED',inspection_status=?,inspection_notes=?,inspected_by_user_id=?,inspected_at=SYSUTCDATETIME(),updated_at=SYSUTCDATETIME() WHERE housekeeping_task_id=?")){
                    ps.setString(1,passed?"PASS":"FAIL");ps.setString(2,notes);ps.setLong(3,staffUserId);ps.setLong(4,taskId);ps.executeUpdate();
                }
                String currentOperation;boolean typeActive;
                try(PreparedStatement ps=cn.prepareStatement("SELECT r.operational_status,rt.is_active type_active FROM rooms r JOIN room_types rt ON rt.room_type_id=r.room_type_id WHERE r.room_id=?")){ps.setLong(1,roomId);try(ResultSet rs=ps.executeQuery()){rs.next();currentOperation=rs.getString(1);typeActive=rs.getBoolean(2);}}
                if(passed){
                    boolean issueOpen;
                    try(PreparedStatement ps=cn.prepareStatement("SELECT COUNT(*) FROM maintenance_tickets WHERE room_id=? AND status_code NOT IN ('CLOSED','CANCELLED')")){ps.setLong(1,roomId);try(ResultSet rs=ps.executeQuery()){rs.next();issueOpen=rs.getInt(1)>0;}}
                    String operation="OCCUPIED".equals(currentOperation)?"OCCUPIED":!typeActive?"OUT_OF_SERVICE":issueOpen?"MAINTENANCE":"AVAILABLE";
                    setRoom(cn,roomId,"READY",operation);
                }else{
                    setRoom(cn,roomId,"CLEAN","OCCUPIED".equals(currentOperation)?"OCCUPIED":typeActive?"MAINTENANCE":"OUT_OF_SERVICE");
                    try(PreparedStatement ps=cn.prepareStatement("INSERT INTO maintenance_tickets (room_id,reported_by_user_id,ticket_code,title,description,priority_code,status_code) VALUES (?,?,?,?,?,'HIGH','OPEN')")){
                        ps.setLong(1,roomId);ps.setLong(2,staffUserId);ps.setString(3,ticketCode);ps.setString(4,"Housekeeping inspection failed");ps.setString(5,notes==null?"Issue found during room inspection":notes);ps.executeUpdate();
                    }
                }
                cn.commit();
            }catch(SQLException|RuntimeException e){cn.rollback();throw e;}finally{cn.setAutoCommit(true);}
        }catch(SQLException e){throw wrap(e);}
    }

    private long taskRoom(Connection cn,long taskId,long staffUserId,boolean start) throws SQLException{
        String allowed=start?"('ASSIGNED','IN_PROGRESS')":"('IN_PROGRESS')";
        try(PreparedStatement ps=cn.prepareStatement("SELECT room_id FROM housekeeping_tasks WITH (UPDLOCK) WHERE housekeeping_task_id=? AND assigned_staff_user_id=? AND status_code IN "+allowed)){
            ps.setLong(1,taskId);ps.setLong(2,staffUserId);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalStateException("Task không thuộc nhân viên hoặc sai trạng thái");return rs.getLong(1);}
        }
    }
    private void setRoom(Connection cn,long roomId,String cleaning,String operation)throws SQLException{
        String sql=operation==null?"UPDATE rooms SET cleaning_status=?,updated_at=SYSUTCDATETIME() WHERE room_id=?":"UPDATE rooms SET cleaning_status=?,operational_status=?,updated_at=SYSUTCDATETIME() WHERE room_id=?";
        try(PreparedStatement ps=cn.prepareStatement(sql)){ps.setString(1,cleaning);if(operation==null)ps.setLong(2,roomId);else{ps.setString(2,operation);ps.setLong(3,roomId);}ps.executeUpdate();}
    }
    private void executeTaskUpdate(String sql,long a,long b,String message){
        try(Connection cn=getConnection();PreparedStatement ps=cn.prepareStatement(sql)){ps.setLong(1,a);ps.setLong(2,b);if(ps.executeUpdate()==0)throw new IllegalStateException(message);}catch(SQLException e){throw wrap(e);}
    }
}
