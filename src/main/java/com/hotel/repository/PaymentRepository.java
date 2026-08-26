package com.hotel.repository;

import com.hotel.entity.Payment;
import com.hotel.interfaces.IPaymentRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** F08 - đặt cọc, F14 - thanh toán cuối */
public class PaymentRepository extends BaseRepository implements IPaymentRepository {

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getLong("payment_id"));
        p.setReservationId(rs.getLong("reservation_id"));
        p.setInvoiceId(longOf(rs, "invoice_id"));
        p.setRecordedByUserId(longOf(rs, "recorded_by_user_id"));
        p.setPaymentType(rs.getString("payment_type"));
        p.setMethodCode(rs.getString("method_code"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setCurrencyCode(rs.getString("currency_code"));
        p.setStatusCode(rs.getString("status_code"));
        p.setProviderName(rs.getString("provider_name"));
        p.setProviderReference(rs.getString("provider_reference"));
        p.setFailureReason(rs.getString("failure_reason"));
        p.setPaidAt(tsOf(rs, "paid_at"));
        p.setCreatedAt(tsOf(rs, "created_at"));
        return p;
    }

    @Override
    public long insert(Payment p) {
        String sql = "INSERT INTO payments (reservation_id, invoice_id, recorded_by_user_id, payment_type, "
                   + "method_code, amount, currency_code, status_code, provider_name, provider_reference, paid_at) "
                   + "VALUES (?,?,?,?,?,?,?,?,?,?, CASE WHEN ? = 'SUCCESS' THEN SYSUTCDATETIME() END)";
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                preventDuplicateActiveVnPay(cn, p);
                long id;
                try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, p.getReservationId());
                    bindLong(ps, 2, p.getInvoiceId());
                    bindLong(ps, 3, p.getRecordedByUserId());
                    ps.setString(4, p.getPaymentType());
                    ps.setString(5, p.getMethodCode());
                    ps.setBigDecimal(6, p.getAmount());
                    ps.setString(7, p.getCurrencyCode() == null ? "VND" : p.getCurrencyCode());
                    ps.setString(8, p.getStatusCode());
                    ps.setString(9, p.getProviderName());
                    ps.setString(10, p.getProviderReference());
                    ps.setString(11, p.getStatusCode());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); id = rs.getLong(1); }
                }
                cn.commit();
                return id;
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } catch (RuntimeException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    /** Chặn double-click tạo hai giao dịch VNPay còn hiệu lực cho cùng một khoản thu. */
    private void preventDuplicateActiveVnPay(Connection cn, Payment payment) throws SQLException {
        if (!"VNPAY".equals(payment.getProviderName()) || !"PENDING".equals(payment.getStatusCode())) return;
        String sql = "SELECT TOP 1 payment_id FROM payments WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE reservation_id=? AND payment_type=? AND provider_name='VNPAY' "
                + "AND status_code='PENDING' AND created_at >= DATEADD(MINUTE,-20,SYSUTCDATETIME())";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, payment.getReservationId());
            ps.setString(2, payment.getPaymentType());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    throw new IllegalStateException("Đang có một giao dịch VNPay chờ xử lý; vui lòng hoàn tất hoặc thử lại sau 20 phút");
            }
        }
    }

    @Override
    public Payment findById(long paymentId) {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<Payment> findByReservation(long reservationId) {
        String sql = "SELECT * FROM payments WHERE reservation_id = ? ORDER BY created_at DESC";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Payment> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public BigDecimal sumSuccess(long reservationId, String paymentType) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE reservation_id = ? "
                   + "AND status_code = 'SUCCESS'" + (paymentType != null ? " AND payment_type = ?" : "");
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            if (paymentType != null) ps.setString(2, paymentType);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getBigDecimal(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public Payment findByProviderReference(String providerName, String providerReference) {
        String sql = "SELECT * FROM payments WHERE provider_name=? AND provider_reference=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, providerName);
            ps.setString(2, providerReference);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public boolean completeDeposit(long paymentId, String providerReference) {
        String lockSql = "SELECT p.reservation_id, p.amount, p.payment_type, p.status_code, "
                + "r.total_amount, r.deposit_required, r.status_code AS reservation_status "
                + "FROM payments p WITH (UPDLOCK, HOLDLOCK) "
                + "JOIN reservations r WITH (UPDLOCK, HOLDLOCK) ON r.reservation_id = p.reservation_id "
                + "WHERE p.payment_id = ?";
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                long reservationId;
                BigDecimal amount;
                BigDecimal totalAmount;
                BigDecimal depositRequired;
                String reservationStatus;
                try (PreparedStatement ps = cn.prepareStatement(lockSql)) {
                    ps.setLong(1, paymentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Giao dịch không tồn tại");
                        if ("SUCCESS".equals(rs.getString("status_code"))) {
                            cn.rollback();
                            return false;
                        }
                        if (!"PENDING".equals(rs.getString("status_code")))
                            throw new IllegalStateException("Giao dịch không còn ở trạng thái chờ xử lý");
                        if (!"DEPOSIT".equals(rs.getString("payment_type")))
                            throw new IllegalStateException("Giao dịch không phải thanh toán tiền cọc");
                        reservationId = rs.getLong("reservation_id");
                        amount = rs.getBigDecimal("amount");
                        totalAmount = rs.getBigDecimal("total_amount");
                        depositRequired = rs.getBigDecimal("deposit_required");
                        reservationStatus = rs.getString("reservation_status");
                    }
                }
                if (!"PENDING".equals(reservationStatus) && !"CONFIRMED".equals(reservationStatus))
                    throw new IllegalStateException("Đơn không ở trạng thái nhận đặt cọc");

                BigDecimal totalPaid = sumSuccess(cn, reservationId, null);
                if (amount.compareTo(totalAmount.subtract(totalPaid)) > 0)
                    throw new IllegalStateException("Giao dịch vượt số tiền còn phải thanh toán");

                markSuccess(cn, paymentId, providerReference);
                BigDecimal depositPaid = sumSuccess(cn, reservationId, "DEPOSIT");
                if ("PENDING".equals(reservationStatus) && depositPaid.compareTo(depositRequired) >= 0) {
                    try (PreparedStatement ps = cn.prepareStatement(
                            "UPDATE reservations SET status_code='CONFIRMED', updated_at=SYSUTCDATETIME() "
                                    + "WHERE reservation_id=? AND status_code='PENDING'")) {
                        ps.setLong(1, reservationId);
                        ps.executeUpdate();
                    }
                }
                cn.commit();
                return true;
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } catch (RuntimeException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public boolean completeFinalPayment(long paymentId, String providerReference) {
        String lockSql = "SELECT p.reservation_id, p.invoice_id, p.amount, p.payment_type, p.status_code, "
                + "i.total_amount, i.paid_amount, i.status_code AS invoice_status "
                + "FROM payments p WITH (UPDLOCK, HOLDLOCK) "
                + "JOIN invoices i WITH (UPDLOCK, HOLDLOCK) ON i.invoice_id = p.invoice_id "
                + "WHERE p.payment_id = ?";
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                long invoiceId;
                BigDecimal amount;
                BigDecimal total;
                BigDecimal paid;
                try (PreparedStatement ps = cn.prepareStatement(lockSql)) {
                    ps.setLong(1, paymentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Giao dịch hoặc hóa đơn không tồn tại");
                        if ("SUCCESS".equals(rs.getString("status_code"))) {
                            cn.rollback();
                            return false;
                        }
                        if (!"PENDING".equals(rs.getString("status_code")))
                            throw new IllegalStateException("Giao dịch không còn ở trạng thái chờ xử lý");
                        if (!"FINAL_PAYMENT".equals(rs.getString("payment_type")))
                            throw new IllegalStateException("Giao dịch không phải thanh toán hóa đơn cuối");
                        String invoiceStatus = rs.getString("invoice_status");
                        if (!"ISSUED".equals(invoiceStatus) && !"PARTIALLY_PAID".equals(invoiceStatus))
                            throw new IllegalStateException("Hóa đơn không ở trạng thái có thể thanh toán");
                        invoiceId = rs.getLong("invoice_id");
                        amount = rs.getBigDecimal("amount");
                        total = rs.getBigDecimal("total_amount");
                        paid = rs.getBigDecimal("paid_amount");
                    }
                }
                BigDecimal outstanding = total.subtract(paid).max(BigDecimal.ZERO);
                if (amount.compareTo(outstanding) != 0)
                    throw new IllegalStateException("Số tiền giao dịch không còn khớp số dư hóa đơn");

                markSuccess(cn, paymentId, providerReference);
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE invoices SET paid_amount=total_amount, status_code='PAID', "
                                + "updated_at=SYSUTCDATETIME() WHERE invoice_id=?")) {
                    ps.setLong(1, invoiceId);
                    ps.executeUpdate();
                }
                cn.commit();
                return true;
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } catch (RuntimeException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    private BigDecimal sumSuccess(Connection cn, long reservationId, String paymentType) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WITH (HOLDLOCK) "
                + "WHERE reservation_id=? AND status_code='SUCCESS'"
                + (paymentType == null ? "" : " AND payment_type=?");
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            if (paymentType != null) ps.setString(2, paymentType);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    private void markSuccess(Connection cn, long paymentId, String providerReference) throws SQLException {
        String sql = "UPDATE payments SET status_code='SUCCESS', paid_at=SYSUTCDATETIME(), "
                + "provider_reference=COALESCE(provider_reference, ?), failure_reason=NULL "
                + "WHERE payment_id=? AND status_code='PENDING'";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, providerReference);
            ps.setLong(2, paymentId);
            if (ps.executeUpdate() != 1)
                throw new IllegalStateException("Giao dịch đã được xử lý trước đó");
        }
    }

    @Override
    public void markSuccess(long paymentId, String providerReference) {
        String sql = "UPDATE payments SET status_code = 'SUCCESS', paid_at = SYSUTCDATETIME(), "
                   + "provider_reference = COALESCE(provider_reference, ?) WHERE payment_id = ? AND status_code = 'PENDING'";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, providerReference);
            ps.setLong(2, paymentId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void markFailed(long paymentId, String reason) {
        String sql = "UPDATE payments SET status_code = 'FAILED', failure_reason = ? WHERE payment_id = ? AND status_code = 'PENDING'";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setLong(2, paymentId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }
}
