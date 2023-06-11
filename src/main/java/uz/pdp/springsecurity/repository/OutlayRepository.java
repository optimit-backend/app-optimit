package uz.pdp.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.springsecurity.entity.Outlay;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface OutlayRepository extends JpaRepository<Outlay, UUID> {

    List<Outlay> findAllByOutlayCategoryId(UUID outlayCategory_id);

    List<Outlay> findAllByDateIsBetweenAndBranch_Id(Date firs_date, Date second_date, UUID branch_id);

    List<Outlay> findAllByDateAndBranch_Id(Date firs_date, UUID branch_id);

    @Query(value = "SELECT * FROM outlay o WHERE DATE(o.date) = ?1 and o.branch_id = ?2", nativeQuery = true)
    List<Outlay> findAllByDate(java.sql.Date date, UUID branch_id);

    @Query(value = "select * from outlay o inner join branches b on o.branch_id = b.id where b.business_id = ?1 and o.date = ?2", nativeQuery = true)
    List<Outlay> findAllByDateAndBusinessId(UUID business_id, java.sql.Date date);

    List<Outlay> findAllByBranch_Id(UUID branch_id);

    List<Outlay> findAllByBranch_BusinessId(UUID branch_id);

    List<Outlay> findAllByCreatedAtBetweenAndBranchId(Timestamp startDate, Timestamp endDate, UUID branch_id);

    List<Outlay> findAllByCreatedAtBetweenAndBranch_BusinessId(Timestamp startDate, Timestamp endDate, UUID businessId);

    List<Outlay> findAllByBranch_IdAndOutlayCategoryId(UUID branch_id, UUID categoryId);

    List<Outlay> findAllByCreatedAtBetweenAndBranchIdAndOutlayCategoryId(Timestamp from, Timestamp to, UUID branch_id, UUID outlayCategory_id);

    @Query(value = "select * from outlay inner join branches b on b.business_id = ?1", nativeQuery = true)
    List<Outlay> findAllByBusinessId(UUID businessId);

    List<Outlay> findAllByDateBetweenAndBranchId(Date firstDate, Date secondDate, UUID uuid);

    @Query(value = "SELECT SUM(total_sum) FROM outlay WHERE created_at BETWEEN ?1 AND  ?2 AND branch_id = ?3", nativeQuery = true)
    Double outlayByCreatedAtBetweenAndBranchId(Timestamp from, Timestamp to, UUID branchId);

    @Query(value = "SELECT SUM(o.totalSum) FROM Outlay o WHERE o.branch.business.id = :businessId AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Double outlayByCreatedAtBetweenAndBusinessId(@Param("businessId") UUID branchId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);
}
