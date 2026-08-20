package com.hotel.service;

import com.hotel.entity.HousekeepingTask;
import com.hotel.entity.MaintenanceTicket;
import com.hotel.entity.ManagerDashboard;
import com.hotel.entity.ManagementReport;
import com.hotel.entity.Room;
import com.hotel.entity.RoomRate;
import com.hotel.entity.RoomType;
import com.hotel.entity.User;
import com.hotel.interfaces.IHousekeepingRepository;
import com.hotel.interfaces.IMaintenanceRepository;
import com.hotel.interfaces.IManagementRepository;
import com.hotel.interfaces.IRoomRateRepository;
import com.hotel.interfaces.IRoomRepository;
import com.hotel.interfaces.IRoomTypeRepository;
import com.hotel.interfaces.IUserRepository;
import com.hotel.repository.HousekeepingRepository;
import com.hotel.repository.MaintenanceRepository;
import com.hotel.repository.ManagementRepository;
import com.hotel.repository.RoomRateRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import com.hotel.repository.UserRepository;
import com.hotel.ultis.CodeGenerator;
import com.hotel.ultis.Constants;
import com.hotel.ultis.JsonArrayUtil;
import com.hotel.ultis.ValidationUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

/** Application service for Manager UCs 48, 54, 57-66. */
public class ManagerService {
    private static final Set<String> OPERATIONAL_STATUSES = Set.of(
            Constants.ROOM_AVAILABLE, Constants.ROOM_BLOCKED, Constants.ROOM_OUT_OF_SERVICE,
            Constants.ROOM_MAINTENANCE, Constants.ROOM_INACTIVE);
    private static final Set<String> TASK_TYPES = Set.of(
            "CHECKOUT_CLEANING", "STAYOVER_CLEANING", "DEEP_CLEANING");
    private static final Set<String> PRIORITIES = Set.of("LOW", "NORMAL", "HIGH", "URGENT");

    private final IRoomTypeRepository roomTypeRepo;
    private final IRoomRepository roomRepo;
    private final IRoomRateRepository rateRepo;
    private final IHousekeepingRepository housekeepingRepo;
    private final IMaintenanceRepository maintenanceRepo;
    private final IManagementRepository managementRepo;
    private final IUserRepository userRepo;

    public ManagerService() {
        this(new RoomTypeRepository(), new RoomRepository(), new RoomRateRepository(),
                new HousekeepingRepository(), new MaintenanceRepository(), new ManagementRepository(),
                new UserRepository());
    }

    public ManagerService(IRoomTypeRepository roomTypeRepo, IRoomRepository roomRepo,
                          IRoomRateRepository rateRepo, IHousekeepingRepository housekeepingRepo,
                          IMaintenanceRepository maintenanceRepo, IManagementRepository managementRepo,
                          IUserRepository userRepo) {
        this.roomTypeRepo = roomTypeRepo;
        this.roomRepo = roomRepo;
        this.rateRepo = rateRepo;
        this.housekeepingRepo = housekeepingRepo;
        this.maintenanceRepo = maintenanceRepo;
        this.managementRepo = managementRepo;
        this.userRepo = userRepo;
    }

    public List<RoomType> getRoomTypes() { return roomTypeRepo.findAll(); }
    public List<RoomType> getActiveRoomTypes() { return roomTypeRepo.findAllActive(); }
    public RoomType getRoomType(long id) { return requireRoomType(id); }

    public long createRoomType(RoomType type) {
        validateRoomType(type);
        type.setAmenitiesJson(normalizeJson(type.getAmenitiesJson()));
        type.setImagesJson(normalizeJson(type.getImagesJson()));
        return roomTypeRepo.insert(type);
    }

    public void updateRoomType(RoomType type) {
        RoomType existing = requireRoomType(type.getRoomTypeId());
        validateRoomType(type);
        if (type.getAmenitiesJson() == null) type.setAmenitiesJson(existing.getAmenitiesJson());
        if (type.getImagesJson() == null) type.setImagesJson(existing.getImagesJson());
        roomTypeRepo.update(type);
    }

    public void setRoomTypeActive(long id, boolean active) {
        requireRoomType(id);
        roomTypeRepo.setActive(id, active);
    }

    public List<String> getAmenities(long roomTypeId) { return JsonArrayUtil.parse(requireRoomType(roomTypeId).getAmenitiesJson()); }
    public List<String> getImages(long roomTypeId) { return JsonArrayUtil.parse(requireRoomType(roomTypeId).getImagesJson()); }

    public void addAmenity(long roomTypeId, String value) {
        RoomType type=requireRoomType(roomTypeId);roomTypeRepo.updateAmenities(roomTypeId,JsonArrayUtil.addUnique(type.getAmenitiesJson(),value));
    }
    public void updateAmenity(long roomTypeId,int index,String value) {
        RoomType type=requireRoomType(roomTypeId);roomTypeRepo.updateAmenities(roomTypeId,JsonArrayUtil.replace(type.getAmenitiesJson(),index,value));
    }
    public void removeAmenity(long roomTypeId,int index) {
        RoomType type=requireRoomType(roomTypeId);roomTypeRepo.updateAmenities(roomTypeId,JsonArrayUtil.remove(type.getAmenitiesJson(),index));
    }
    public void addImage(long roomTypeId,String path) {
        validateImagePath(path);RoomType type=requireRoomType(roomTypeId);roomTypeRepo.updateImages(roomTypeId,JsonArrayUtil.addUnique(type.getImagesJson(),path));
    }
    public void updateImage(long roomTypeId,int index,String path) {
        validateImagePath(path);RoomType type=requireRoomType(roomTypeId);roomTypeRepo.updateImages(roomTypeId,JsonArrayUtil.replace(type.getImagesJson(),index,path));
    }
    public void removeImage(long roomTypeId,int index) {
        RoomType type=requireRoomType(roomTypeId);roomTypeRepo.updateImages(roomTypeId,JsonArrayUtil.remove(type.getImagesJson(),index));
    }
    public void moveImage(long roomTypeId,int index,int direction) {
        if(direction!=1&&direction!=-1)throw new IllegalArgumentException("Hướng sắp xếp không hợp lệ");
        RoomType type=requireRoomType(roomTypeId);roomTypeRepo.updateImages(roomTypeId,JsonArrayUtil.move(type.getImagesJson(),index,direction));
    }

    public List<Room> getRooms(Long roomTypeId,Integer floor,String status){return roomRepo.findAll(roomTypeId,floor,status);}
    public Room getRoom(long id){Room room=roomRepo.findById(id);if(room==null)throw new IllegalArgumentException("Phòng không tồn tại");return room;}
    public long createRoom(Room room){validateRoom(room);RoomType type=requireRoomType(room.getRoomTypeId());room.setOperationalStatus(type.isActive()?Constants.ROOM_AVAILABLE:Constants.ROOM_OUT_OF_SERVICE);room.setCleaningStatus(Constants.CLEAN_DIRTY);room.setActive(type.isActive());return roomRepo.insert(room);}
    public void updateRoom(Room room){Room existing=getRoom(room.getRoomId());validateRoom(room);RoomType type=requireRoomType(room.getRoomTypeId());roomRepo.update(room);if(!type.isActive())roomRepo.updateOperationalStatus(room.getRoomId(),Constants.ROOM_OCCUPIED.equals(existing.getOperationalStatus())?Constants.ROOM_OCCUPIED:Constants.ROOM_OUT_OF_SERVICE,false);}

    public void changeOperationalStatus(long roomId,String status){
        if(!OPERATIONAL_STATUSES.contains(status))throw new IllegalArgumentException("Trạng thái vận hành không hợp lệ");
        Room room=getRoom(roomId);
        if(Constants.ROOM_OCCUPIED.equals(room.getOperationalStatus()))
            throw new IllegalStateException("Không thể đổi trạng thái vận hành của phòng đang có khách");
        RoomType type=requireRoomType(room.getRoomTypeId());
        if(!type.isActive()&&!Constants.ROOM_OUT_OF_SERVICE.equals(status))throw new IllegalStateException("Loại phòng đã ngừng hoạt động; phòng phải ở OUT_OF_SERVICE");
        boolean active=type.isActive()&&!Constants.ROOM_INACTIVE.equals(status);
        roomRepo.updateOperationalStatus(roomId,status,active);
    }

    public List<RoomRate> getRates(long typeId,LocalDate from,LocalDate to){requireActiveRoomType(typeId);validateDateRange(from,to,false);return rateRepo.findRateList(typeId,from,to);}
    public void updatePricing(long typeId,BigDecimal basePrice,LocalDate from,LocalDate to,BigDecimal nightlyPrice,boolean stopSell){
        requireActiveRoomType(typeId);requireNonNegative(basePrice,"Giá cơ bản");
        boolean hasDaily=from!=null||to!=null||nightlyPrice!=null;
        if(hasDaily){validateDateRange(from,to,true);requireNonNegative(nightlyPrice,"Giá theo ngày");}
        rateRepo.updatePricing(typeId,basePrice,hasDaily?from:null,hasDaily?to:null,hasDaily?nightlyPrice:null,stopSell);
    }

    public List<User> getServiceStaff(){return userRepo.findActiveByRole(Constants.ROLE_SERVICE_STAFF);}
    public List<HousekeepingTask> getHousekeepingTasks(String status,Long roomId,Long staffId){return housekeepingRepo.findAll(status,roomId,staffId);}
    public HousekeepingTask getHousekeepingTask(long id){HousekeepingTask t=housekeepingRepo.findById(id);if(t==null)throw new IllegalArgumentException("Housekeeping task không tồn tại");return t;}
    public long createHousekeepingTask(long roomId,Long reservationId,Long staffId,String type,String priority,LocalDateTime scheduled,String notes,long managerId){
        Room room=getRoom(roomId);validateTaskType(type);validatePriority(priority);if(staffId!=null)requireStaff(staffId);
        if(housekeepingRepo.hasActiveTask(roomId))throw new IllegalStateException("Phòng đã có housekeeping task đang hoạt động");
        if(Constants.ROOM_OCCUPIED.equals(room.getOperationalStatus())&&!"STAYOVER_CLEANING".equals(type))throw new IllegalStateException("Phòng đang có khách chỉ được tạo stayover cleaning");
        HousekeepingTask task=new HousekeepingTask();task.setRoomId(roomId);task.setReservationId(reservationId);task.setAssignedStaffUserId(staffId);task.setCreatedByUserId(managerId);task.setTaskType(type);task.setPriorityCode(priority);task.setScheduledAt(scheduled);task.setNotes(trim(notes));return housekeepingRepo.insert(task);
    }
    public void assignHousekeeping(long taskId,long staffId){getHousekeepingTask(taskId);requireStaff(staffId);housekeepingRepo.assign(taskId,staffId);}
    public void startHousekeeping(long taskId,long staffId){getHousekeepingTask(taskId);housekeepingRepo.start(taskId,staffId);}
    public void completeCleaning(long taskId,long staffId,String notes){getHousekeepingTask(taskId);housekeepingRepo.completeCleaning(taskId,staffId,trim(notes));}
    public void inspectHousekeeping(long taskId,long staffId,boolean passed,String notes){if(!passed&&ValidationUtil.isBlank(notes))throw new IllegalArgumentException("Cần ghi rõ vấn đề khi inspection thất bại");getHousekeepingTask(taskId);housekeepingRepo.inspect(taskId,staffId,passed,trim(notes),CodeGenerator.maintenanceTicketCode());}

    public List<MaintenanceTicket> getMaintenanceTickets(String status,String priority,Long roomId,Long staffId){return maintenanceRepo.findAll(status,priority,roomId,staffId);}
    public MaintenanceTicket getMaintenanceTicket(long id){MaintenanceTicket t=maintenanceRepo.findById(id);if(t==null)throw new IllegalArgumentException("Maintenance issue không tồn tại");return t;}
    public long reportMaintenance(long roomId,long reporterId,String title,String description,String priority,Long staffId){
        getRoom(roomId);if(ValidationUtil.isBlank(title)||ValidationUtil.isBlank(description))throw new IllegalArgumentException("Tiêu đề và mô tả là bắt buộc");if(title.trim().length()>150)throw new IllegalArgumentException("Tiêu đề tối đa 150 ký tự");validatePriority(priority);if(staffId!=null)requireStaff(staffId);
        MaintenanceTicket t=new MaintenanceTicket();t.setRoomId(roomId);t.setReportedByUserId(reporterId);t.setAssignedStaffUserId(staffId);t.setTicketCode(CodeGenerator.maintenanceTicketCode());t.setTitle(title.trim());t.setDescription(description.trim());t.setPriorityCode(priority);return maintenanceRepo.insert(t);
    }
    public void assignMaintenance(long id,String priority,long staffId){getMaintenanceTicket(id);validatePriority(priority);requireStaff(staffId);maintenanceRepo.assign(id,priority,staffId);}
    public void startMaintenance(long id,long staffId){getMaintenanceTicket(id);maintenanceRepo.start(id,staffId);}
    public void resolveMaintenance(long id,long staffId,String note){if(ValidationUtil.isBlank(note))throw new IllegalArgumentException("Repair note là bắt buộc");getMaintenanceTicket(id);maintenanceRepo.resolve(id,staffId,note.trim());}
    public void reopenMaintenance(long id,Long staffId){getMaintenanceTicket(id);if(staffId!=null)requireStaff(staffId);maintenanceRepo.reopen(id,staffId);}
    public void closeMaintenance(long id){getMaintenanceTicket(id);maintenanceRepo.close(id);}

    public ManagerDashboard getDashboard(LocalDate date){return managementRepo.loadDashboard(date==null?LocalDate.now():date);}
    public ManagementReport getReport(LocalDate from,LocalDate to){validateDateRange(from,to,false);return managementRepo.loadReport(from,to);}

    private RoomType requireRoomType(long id){RoomType type=roomTypeRepo.findById(id);if(type==null)throw new IllegalArgumentException("Loại phòng không tồn tại");return type;}
    private RoomType requireActiveRoomType(long id){RoomType type=requireRoomType(id);if(!type.isActive())throw new IllegalStateException("Loại phòng đã ngừng hoạt động");return type;}
    private User requireStaff(long id){User user=userRepo.findById(id);if(user==null||!Constants.ROLE_SERVICE_STAFF.equals(user.getRoleCode())||!"ACTIVE".equals(user.getStatusCode()))throw new IllegalArgumentException("Nhân viên được phân công không hợp lệ");return user;}
    private void validateRoomType(RoomType t){if(ValidationUtil.isBlank(t.getTypeCode())||ValidationUtil.isBlank(t.getTypeName()))throw new IllegalArgumentException("Mã và tên loại phòng là bắt buộc");t.setTypeCode(t.getTypeCode().trim().toUpperCase());t.setTypeName(t.getTypeName().trim());if(!t.getTypeCode().matches("[A-Z0-9_-]{2,20}"))throw new IllegalArgumentException("Mã loại phòng chỉ gồm chữ, số, _ hoặc -");if(t.getMaxAdults()<=0||t.getMaxChildren()<0)throw new IllegalArgumentException("Sức chứa không hợp lệ");requireNonNegative(t.getBasePrice(),"Giá cơ bản");if(t.getRoomSizeM2()!=null&&t.getRoomSizeM2().signum()<=0)throw new IllegalArgumentException("Diện tích phải lớn hơn 0");}
    private void validateRoom(Room r){requireRoomType(r.getRoomTypeId());if(ValidationUtil.isBlank(r.getRoomNumber()))throw new IllegalArgumentException("Số phòng là bắt buộc");r.setRoomNumber(r.getRoomNumber().trim());if(r.getRoomNumber().length()>20)throw new IllegalArgumentException("Số phòng tối đa 20 ký tự");if(r.getFloorNumber()!=null&&(r.getFloorNumber()<-10||r.getFloorNumber()>200))throw new IllegalArgumentException("Số tầng không hợp lệ");if(r.getNotes()!=null&&r.getNotes().length()>500)throw new IllegalArgumentException("Ghi chú tối đa 500 ký tự");}
    private void validateImagePath(String path){if(ValidationUtil.isBlank(path)||path.length()>500)throw new IllegalArgumentException("Đường dẫn ảnh không hợp lệ");String lower=path.toLowerCase();boolean source=lower.startsWith("https://")||lower.startsWith("http://")||lower.startsWith("/uploads/");if(!source||!(lower.matches(".*\\.(jpg|jpeg|png|webp)(\\?.*)?$")))throw new IllegalArgumentException("Ảnh phải là URL/upload JPG, PNG hoặc WEBP");}
    private void validateDateRange(LocalDate from,LocalDate to,boolean future){if(from==null||to==null)throw new IllegalArgumentException("Ngày bắt đầu và kết thúc là bắt buộc");if(to.isBefore(from))throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu");if(ChronoUnit.DAYS.between(from,to)>366)throw new IllegalArgumentException("Khoảng ngày tối đa 366 ngày");if(future&&from.isBefore(LocalDate.now()))throw new IllegalArgumentException("Chỉ được cấu hình giá từ hôm nay trở đi");}
    private void validateTaskType(String value){if(!TASK_TYPES.contains(value))throw new IllegalArgumentException("Loại housekeeping task không hợp lệ");}
    private void validatePriority(String value){if(!PRIORITIES.contains(value))throw new IllegalArgumentException("Mức ưu tiên không hợp lệ");}
    private void requireNonNegative(BigDecimal value,String label){if(value==null||value.signum()<0)throw new IllegalArgumentException(label+" không được âm");}
    private String normalizeJson(String json){return json==null?"[]":JsonArrayUtil.toJson(JsonArrayUtil.parse(json));}
    private String trim(String value){return ValidationUtil.isBlank(value)?null:value.trim();}
}
