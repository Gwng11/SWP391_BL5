package com.hotel.interfaces;

import com.hotel.entity.RoomAssignment;
import java.util.List;

public interface IRoomAssignmentRepository {
    List<RoomAssignment> findCurrentByReservation(long reservationId);
    List<RoomAssignment> historyByReservation(long reservationId);
    int countCurrentByReservationRoom(long reservationRoomId);
    /** F11: gán phòng - insert assignment + set phòng OCCUPIED (transaction) */
    void assign(long reservationRoomId, long roomId, long byUserId);
    /** F11: đổi phòng - trả phòng cũ (DIRTY) + gán phòng mới (transaction) */
    void changeRoom(long roomAssignmentId, long newRoomId, long byUserId, String reason);
    /** F13: trả toàn bộ phòng khi check-out, phòng chuyển AVAILABLE + DIRTY */
    void releaseAllForReservation(long reservationId, String reason);
}
