package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Address;
import iuh.house_keeping_service_be.repositories.projections.ZoneCoordinate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    List<Address> findByCustomer_CustomerId(String customerId);

    Optional<Address> findByCustomer_CustomerIdAndIsDefaultTrue(String customerId);

    // Find by customer ID
    List<Address> findByCustomer_CustomerIdOrderByCreatedAtDesc(String customerId);
    
    // Find customer's default address
    @Query("SELECT a FROM Address a WHERE a.customer.customerId = :customerId AND a.isDefault = true")
    Optional<Address> findDefaultAddressByCustomerId(@Param("customerId") String customerId);
    
    
    // Validate customer address ownership
    @Query("SELECT COUNT(a) > 0 FROM Address a WHERE a.addressId = :addressId AND a.customer.customerId = :customerId")
    boolean validateCustomerAddressOwnership(@Param("addressId") String addressId,
                                           @Param("customerId") String customerId);
    
    // Get address with customer info
    @Query("SELECT a FROM Address a " +
           "JOIN FETCH a.customer c " +
           "WHERE a.addressId = :addressId")
    Optional<Address> findAddressWithCustomer(@Param("addressId") String addressId);

    @Query("SELECT new iuh.house_keeping_service_be.repositories.projections.ZoneCoordinate(" +
            "AVG(a.latitude), AVG(a.longitude)) " +
            "FROM Address a " +
            "WHERE a.ward = :ward AND a.city = :city " +
            "AND a.latitude IS NOT NULL AND a.longitude IS NOT NULL")
    Optional<ZoneCoordinate> findAverageCoordinateByWardAndCity(@Param("ward") String ward,
                                                                    @Param("city") String city);

    @Query("SELECT new iuh.house_keeping_service_be.repositories.projections.ZoneCoordinate(" +
           "AVG(a.latitude), AVG(a.longitude)) " +
           "FROM Address a " +
           "JOIN EmployeeWorkingZone ewz ON a.ward = ewz.ward AND a.city = ewz.city " +
           "WHERE ewz.employee.employeeId = :employeeId " +
           "AND a.latitude IS NOT NULL AND a.longitude IS NOT NULL")
    Optional<ZoneCoordinate> findAverageCoordinateByEmployeeZones(@Param("employeeId") String employeeId);
}